package jogo.logica;

import jogo.banco.JogadorRepository;
import jogo.banco.PartidaRepository;
import jogo.modelo.Lado;
import jogo.modelo.Movimento;
import jogo.modelo.Player;
import jogo.modelo.ResultadoPartida;
import jogo.modelo.Tela;
import jogo.modelo.Tiro;
import jogo.modelo.Zombie;
import jogo.util.Json;
import jogo.util.JsonObject;
import jogo.util.JsonSerializavel;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * Coordena o jogo: junta player, zumbis, tiros, ondas e partida.
 *
 * Os métodos são synchronized porque o loop do jogo e as requisições HTTP
 * rodam em threads diferentes e mexem no mesmo estado.
 */
public class Jogo implements JsonSerializavel {

    private static final Tela TELA_PADRAO = new Tela(1280, 720);
    private static final long COOLDOWN_TIRO_MS = 1000;
    private static final int TAMANHO_PENTE = 12;
    private static final long DURACAO_RECARGA_MS = 1500;
    private static final String NOME_PADRAO = "Jogador";

    private final JogadorRepository jogadores;
    private final PartidaRepository partidas;

    private final Horda horda = new Horda();
    private final GerenciadorDeOndas ondas = new GerenciadorDeOndas();
    private final Arma arma = new Arma(COOLDOWN_TIRO_MS, TAMANHO_PENTE, DURACAO_RECARGA_MS);
    private final List<Tiro> tiros = new ArrayList<>();

    private Player player = new Player(100, 100);
    private Partida partida = new Partida();
    private Tela tela = TELA_PADRAO;
    private boolean ativo = false;

    public Jogo(JogadorRepository jogadores, PartidaRepository partidas) {
        this.jogadores = jogadores;
        this.partidas = partidas;
    }

    // ------------------------------------------------------------
    // Ciclo de vida da partida
    // ------------------------------------------------------------

    /** Zera tudo e deixa o jogo pausado. Se novaTela for null, mantém o tamanho anterior. */
    public synchronized void reiniciar(Tela novaTela) {
        ativo = false;

        if (novaTela != null) {
            tela = novaTela;
        }

        player = Player.noCentro(tela);
        partida = new Partida();
        tiros.clear();
        horda.limpar();
        ondas.reiniciar();
        arma.reiniciar();
    }

    /** Registra o jogador no banco e começa a partida. */
    public synchronized void iniciar(String nome) throws SQLException {
        String nomeFinal = (nome == null || nome.isBlank()) ? NOME_PADRAO : nome.trim();

        int jogadorId = jogadores.obterOuCriar(nomeFinal);

        partida.iniciar(jogadorId, nomeFinal, System.currentTimeMillis());
        ativo = true;
    }

    /** Encerra a partida (salvando no banco) e devolve o resultado. */
    public synchronized ResultadoPartida parar() {
        if (ativo) {
            salvarPartida();
        }
        ativo = false;

        return new ResultadoPartida(
                partida.getKills(),
                partida.segundosDecorridos(System.currentTimeMillis())
        );
    }

    private void salvarPartida() {
        if (partida.isSalva() || !partida.isIniciada()) {
            return;
        }

        long agora = System.currentTimeMillis();
        partida.finalizar(agora);
        partida.marcarComoSalva();

        long tempo = partida.segundosDecorridos(agora);

        try {
            partidas.salvar(partida.getJogadorId(), partida.getKills(), (int) tempo);

            System.out.println("PARTIDA SALVA | Jogador: " + partida.getNomeJogador()
                    + " | Kills: " + partida.getKills()
                    + " | Tempo: " + tempo + "s");
        } catch (SQLException e) {
            System.out.println("ERRO AO SALVAR PARTIDA:");
            e.printStackTrace();
        }
    }

    // ------------------------------------------------------------
    // Ações do jogador (vêm das requisições HTTP)
    // ------------------------------------------------------------

    public synchronized void moverPlayer(Movimento movimento, Tela telaAtual) {
        if (ativo) {
            player.mover(movimento, telaAtual);
        }

        tela = telaAtual;
    }

    /** Atira reto para o lado que o player olha. A tela pode ser null. */
    public synchronized boolean atirar(Lado lado, Tela telaAtual) {
        if (telaAtual != null) {
            tela = telaAtual;
        }

        int alvoX = lado == Lado.ESQUERDA ? 0 : tela.getLargura();
        int alvoY = player.getY() + Tiro.MUZZLE_OFFSET_Y;

        return atirar(lado, null, alvoX, alvoY);
    }

    /** Atira em direção a um ponto (o mouse). A tela pode ser null. */
    public synchronized boolean atirar(Lado lado, Tela telaAtual, int alvoX, int alvoY) {
        if (telaAtual != null) {
            tela = telaAtual;
        }

        if (!ativo) {
            return false;
        }

        Optional<Tiro> tiro = arma.disparar(
                System.currentTimeMillis(), player, alvoX, alvoY, lado);

        tiro.ifPresent(tiros::add);
        return tiro.isPresent();
    }

    /** Começa a recarregar a arma (tecla R). Retorna false se não deu para recarregar. */
    public synchronized boolean recarregar() {
        if (!ativo || !player.isVivo()) {
            return false;
        }

        return arma.iniciarRecarga(System.currentTimeMillis());
    }

    public synchronized long getCooldownRestante() {
        return arma.cooldownRestante(System.currentTimeMillis());
    }

    /** Munição, capacidade e estado da recarga, para o HUD. */
    private JsonObject campoArma(JsonObject json) {
        long agora = System.currentTimeMillis();

        return json
                .campo("municao", arma.getMunicao())
                .campo("capacidade", arma.getCapacidade())
                .campo("recarregando", arma.isRecarregando())
                .campo("recargaRestante", arma.recargaRestante(agora));
    }

    // ------------------------------------------------------------
    // Um "frame" do jogo (chamado pelo GameLoop)
    // ------------------------------------------------------------

    public synchronized void atualizar(long agora) {
        arma.atualizar(agora);

        if (!ativo || !player.isVivo()) {
            return;
        }

        horda.perseguir(player);
        horda.separar(tela);

        processarTiros();
        horda.removerMortos();

        ondas.atualizar(horda, player, tela, agora);

        verificarContatoComPlayer(agora);
    }

    private void processarTiros() {
        Iterator<Tiro> iterador = tiros.iterator();

        while (iterador.hasNext()) {
            Tiro tiro = iterador.next();
            tiro.mover();

            if (tiro.saiuDaTela(tela)) {
                iterador.remove();
                continue;
            }

            for (Zombie zumbi : horda.getZumbis()) {
                if (zumbi.isVivo() && zumbi.contemPonto(tiro.getX(), tiro.getY())) {
                    zumbi.receberDano(tiro.getDano());
                    iterador.remove();

                    if (!zumbi.isVivo()) {
                        partida.registrarKill();
                    }
                    break;
                }
            }
        }
    }

    private void verificarContatoComPlayer(long agora) {
        for (Zombie zumbi : horda.getZumbis()) {
            if (zumbi.colideCom(player) && player.tentarReceberDano(1, agora)) {

                if (!player.isVivo()) {
                    ativo = false;
                    salvarPartida();
                }
                break;
            }
        }
    }

    // ------------------------------------------------------------
    // Estado para o front
    // ------------------------------------------------------------

    public synchronized String getTirosJson() {
        return Json.array(tiros);
    }

    @Override
    public synchronized String toJson() {
        return campoArma(JsonObject.novo())
                .campo("x", player.getX())
                .campo("y", player.getY())
                .campoJson("zumbis", horda.toJson())
                .campo("onda", ondas.getOnda())
                .campo("pontosVida", player.getPontosVida())
                .campo("kills", partida.getKills())
                .campoJson("tiros", Json.array(tiros))
                .construir();
    }
}
