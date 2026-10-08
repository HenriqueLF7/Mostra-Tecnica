package jogo.logica;

import jogo.modelo.Lado;
import jogo.modelo.Player;
import jogo.modelo.Tiro;

import java.util.Optional;

/**
 * A arma do player: controla o intervalo entre tiros, a munição do pente
 * e a recarga (manual com R ou automática quando o pente zera).
 */
public class Arma {

    private final long cooldownMs;
    private final int capacidade;
    private final long duracaoRecargaMs;

    private long ultimoDisparo;
    private int municao;
    private boolean recarregando;
    private long fimRecarga;

    public Arma(long cooldownMs, int capacidade, long duracaoRecargaMs) {
        this.cooldownMs = cooldownMs;
        this.capacidade = capacidade;
        this.duracaoRecargaMs = duracaoRecargaMs;
        reiniciar();
    }

    /** Volta ao estado inicial: pente cheio, pronta para atirar. */
    public void reiniciar() {
        this.ultimoDisparo = -cooldownMs;
        this.municao = capacidade;
        this.recarregando = false;
        this.fimRecarga = 0;
    }

    /**
     * Começa a recarregar. Não faz nada se já está recarregando
     * ou se o pente já está cheio.
     *
     * @return true se a recarga começou
     */
    public boolean iniciarRecarga(long agora) {
        if (recarregando || municao >= capacidade) {
            return false;
        }

        recarregando = true;
        fimRecarga = agora + duracaoRecargaMs;
        return true;
    }

    /** Termina a recarga quando o tempo passa. Chamado a cada frame. */
    public void atualizar(long agora) {
        if (recarregando && agora >= fimRecarga) {
            recarregando = false;
            municao = capacidade;
        }
    }

    public long cooldownRestante(long agora) {
        return Math.max(0, cooldownMs - (agora - ultimoDisparo));
    }

    public long recargaRestante(long agora) {
        return recarregando ? Math.max(0, fimRecarga - agora) : 0;
    }

    public int getMunicao() {
        return municao;
    }

    public int getCapacidade() {
        return capacidade;
    }

    public boolean isRecarregando() {
        return recarregando;
    }

    /**
     * Dispara se o cooldown já passou, há munição e a arma não está
     * recarregando. Se o último tiro esvaziar o pente, a recarga começa sozinha.
     */
    public Optional<Tiro> disparar(long agora, Player player,
                                   int alvoX, int alvoY, Lado lado) {
        atualizar(agora);

        if (recarregando || agora - ultimoDisparo < cooldownMs) {
            return Optional.empty();
        }

        if (municao <= 0) {
            iniciarRecarga(agora);
            return Optional.empty();
        }

        ultimoDisparo = agora;
        municao--;

        if (municao == 0) {
            iniciarRecarga(agora);
        }

        return Optional.of(Tiro.naPontaDaArma(player, alvoX, alvoY, lado));
    }
}
