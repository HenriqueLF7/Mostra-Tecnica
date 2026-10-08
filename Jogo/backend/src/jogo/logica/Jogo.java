package jogo.logica;

import jogo.banco.JogadorRepository;
import jogo.banco.PartidaRepository;
import jogo.modelo.Lado;
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

    // Identifica este servidor e a "versão" do estado. O front usa os dois para
    // descartar respostas atrasadas que chegam fora de ordem.
    private final long sessao = System.currentTimeMillis();
    private long versao = 0;

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
        versao++;
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

    /**
     * Guarda para onde as teclas mandam o player andar. Quem move o player de
     * verdade é o {@link #atualizar}, a cada frame — assim o movimento não
     * depende de quantas requisições chegam por segundo.
     */
    public synchronized void definirDirecaoPlayer(int dx, int dy, Tela telaAtual) {
        if (telaAtual != null) {
            tela = telaAtual;
        }

        if (ativo) {
            player.definirDirecao(dx, dy, System.currentTimeMillis());
            versao++;
        }
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
        tiro.ifPresent(t -> versao++);
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

    /**
     * @param agora relógio em milissegundos
     * @param dt    tempo desde o frame anterior, em SEGUNDOS
     */
    public synchronized void atualizar(long agora, double dt) {
        versao++;
        arma.atualizar(agora);

        if (!ativo || !player.isVivo()) {
            return;
        }

        player.atualizar(dt, agora, tela);
        horda.atualizar(dt, player, tela, agora);

        processarTiros(dt);
        horda.removerMortos();

        ondas.atualizar(horda, player, tela, agora);

        verificarContatoComPlayer(agora);
    }

    private void processarTiros(double dt) {
        Iterator<Tiro> iterador = tiros.iterator();

        while (iterador.hasNext()) {
            Tiro tiro = iterador.next();
            tiro.mover(dt);

            if (tiro.saiuDaTela(tela)) {
                iterador.remove();
                continue;
            }

            if (acertouAlgumZumbi(tiro)) {
                iterador.remove();
            }
        }
    }

    /**
     * O tiro é rápido: em um frame ele anda ~16 px (ou mais, se o servidor
     * engasgar). Por isso testamos vários pontos do caminho, e não só o final,
     * para ele nunca "atravessar" um zumbi.
     */
    private boolean acertouAlgumZumbi(Tiro tiro) {
        double dx = tiro.getX() - tiro.getXAnterior();
        double dy = tiro.getY() - tiro.getYAnterior();
        int passos = Math.max(1, (int) Math.ceil(Math.sqrt(dx * dx + dy * dy) / 15));

        for (int i = 1; i <= passos; i++) {
            double px = tiro.getXAnterior() + dx * i / passos;
            double py = tiro.getYAnterior() + dy * i / passos;

            for (Zombie zumbi : horda.getZumbis()) {
                if (zumbi.isVivo() && zumbi.contemPonto(px, py)) {
                    zumbi.receberDano(tiro.getDano());

                    if (!zumbi.isVivo()) {
                        partida.registrarKill();
                    }
                    return true;
                }
            }
        }

        return false;
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
                .campo("sessao", sessao)
                .campo("versao", versao)
                .campo("x", player.getXExato())
                .campo("y", player.getYExato())
                .campo("vx", (int) Math.round(player.getVx()))
                .campo("vy", (int) Math.round(player.getVy()))
                .campo("invulneravel", player.isInvulneravel(System.currentTimeMillis()))
                .campoJson("zumbis", horda.toJson())
                .campo("onda", ondas.getOnda())
                .campo("pontosVida", player.getPontosVida())
                .campo("kills", partida.getKills())
                .campoJson("tiros", Json.array(tiros))
                .construir();
    }
}
