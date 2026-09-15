// ============================================================================
// SpawnZumbis.java
// ============================================================================
// Esta classe tem responsabilidade única e bem definida: decidir QUANDO e
// COM QUE FORÇA um novo zumbi deve nascer, conforme o tempo de jogo passa
// (curva de dificuldade). É um ótimo exemplo de classe bem separada.
//
// >>> ATENÇÃO — isto não é sobre organização, é funcional:
// Eu não encontrei nenhum lugar em Main.java (nem em nenhum outro arquivo
// enviado) onde esta classe é instanciada ou usada. Ou seja: hoje ela existe,
// está pronta, mas NINGUÉM chama tentarSpawnar(). É provavelmente por isso
// que o frontend já sabe desenhar "zumbis extras" (a função
// atualizarZumbisExtras no script.js, e a classe .zumbi-extra no CSS), mas
// na prática nenhum zumbi extra nasce, porque:
//   1) o Main.java nunca cria um SpawnZumbis;
//   2) o JSON que o Main.java devolve no endpoint "/player" nunca inclui o
//      campo "zumbis" que o frontend está esperando (data.zumbis).
// Ver a observação detalhada no final de Main.java sobre como conectar isso.
// ============================================================================
public class SpawnZumbis {

    private final int larguraTela;
    private final int alturaTela;

    private double velocidadeInicial = 2.0;
    private double velocidadeMaxima = 6.0;

    private int intervaloSpawnInicialMs = 3000; // a cada 3s no começo
    private int intervaloSpawnMinimoMs = 600;   // a cada 0.6s no auge

    private long tempoMaximoProgressaoMs = 110_000; // atinge dificuldade máxima aos 1:50min.
    private long tempoInicio;
    private long ultimoSpawn;

    private final java.util.Random random = new java.util.Random();

    public SpawnZumbis(int larguraTela, int alturaTela) {
        this.larguraTela = larguraTela;
        this.alturaTela = alturaTela;
        this.tempoInicio = System.currentTimeMillis();
        this.ultimoSpawn = tempoInicio;
    }

    // Método principal desta classe: deve ser chamado periodicamente
    // (ex: dentro do loop de jogo, a cada 50ms). Devolve um Zombie novo
    // quando é a hora certa de nascer um, ou null se ainda não é a hora.
    // chamar isso periodicamente (ex: na sua thread de update, a cada 50ms)
    public Zombie tentarSpawnar() {
        long agora = System.currentTimeMillis();
        double progresso = calcularProgresso(agora);

        int intervaloAtual = calcularIntervaloAtual(progresso);

        if (agora - ultimoSpawn < intervaloAtual) {
            return null; // ainda não é hora
        }

        ultimoSpawn = agora;
        return criarZumbi(progresso);
    }

    // "Progresso" vai de 0.0 (início do jogo) até 1.0 (dificuldade máxima,
    // atingida em tempoMaximoProgressaoMs)
    private double calcularProgresso(long agora) {
        long decorrido = agora - tempoInicio;
        double progresso = (double) decorrido / tempoMaximoProgressaoMs;
        return Math.max(0.0, Math.min(1.0, progresso)); // trava entre 0 e 1
    }

    // Quanto maior o progresso, MENOR o intervalo entre spawns
    // (zumbis nascem mais rápido conforme o tempo passa)
    private int calcularIntervaloAtual(double progresso) {
        return (int) (intervaloSpawnInicialMs - progresso * (intervaloSpawnInicialMs - intervaloSpawnMinimoMs));
    }
}

    // Sorteia um lado da tela (topo, baixo, esquerda, direita) e devolve
    // uma posição [x, y] em cima dessa borda
    private int[] gerarPosicaoNaBorda() {
        int lado = random.nextInt(4); // 0=topo, 1=baixo, 2=esquerda, 3=direita
        int x, y;

        switch (lado) {
            case 0:
                x = random.nextInt(larguraTela);
                y = 0;
                break;
            case 1:
                x = random.nextInt(larguraTela);
                y = alturaTela;
                break;
            case 2:
                x = 0;
                y = random.nextInt(alturaTela);
                break;
            default:
                x = larguraTela;
                y = random.nextInt(alturaTela);
                break;
        }

        return new int[]{x, y};
    }

    // getters/setters pros parâmetros ajustáveis, caso queira balancear depois
    public void setVelocidadeInicial(double valor) { this.velocidadeInicial = valor; }
    public void setVelocidadeMaxima(double valor) { this.velocidadeMaxima = valor; }
    public void setIntervaloSpawnInicialMs(int valor) { this.intervaloSpawnInicialMs = valor; }
    public void setIntervaloSpawnMinimoMs(int valor) { this.intervaloSpawnMinimoMs = valor; }
}
