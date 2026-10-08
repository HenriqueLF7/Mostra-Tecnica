package jogo.logica;

/** Dados de uma partida em andamento: quem joga, quando começou e quantos zumbis morreram. */
public class Partida {

    private int jogadorId = -1;
    private String nomeJogador = "";
    private long inicio = 0;
    private long fim = 0;
    private int kills = 0;
    private boolean salva = false;

    public void iniciar(int jogadorId, String nomeJogador, long agora) {
        this.jogadorId = jogadorId;
        this.nomeJogador = nomeJogador;
        this.inicio = agora;
        this.fim = 0;
        this.salva = false;
    }

    /** Congela o tempo da partida (só na primeira chamada). */
    public void finalizar(long agora) {
        if (fim == 0) {
            fim = agora;
        }
    }

    public void registrarKill() {
        kills++;
    }

    public boolean isIniciada() {
        return jogadorId != -1;
    }

    public boolean isSalva() {
        return salva;
    }

    public void marcarComoSalva() {
        salva = true;
    }

    public int getJogadorId() {
        return jogadorId;
    }

    public String getNomeJogador() {
        return nomeJogador;
    }

    public int getKills() {
        return kills;
    }

    public long segundosDecorridos(long agora) {
        if (inicio == 0) {
            return 0;
        }

        long ate = fim > 0 ? fim : agora;
        return (ate - inicio) / 1000;
    }
}
