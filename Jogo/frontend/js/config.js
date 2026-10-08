// Constantes do jogo em um único lugar.
// Os tamanhos precisam bater com os do backend (Player.java / Zombie.java) e com o style.css.
export const CONFIG = Object.freeze({
  PLAYER_LARGURA: 90,
  PLAYER_ALTURA: 100,
  ZUMBI_METADE_LARGURA: 28,

  FRAMES_PLAYER: 8,
  FRAMES_ZUMBI: 8,

  ANIMACAO_PLAYER_MS: 90,
  ANIMACAO_ZUMBI_MS: 100,

  // Rede: o servidor calcula o jogo (~60x/s); o navegador busca o estado ~30x/s
  // e desenha a cada frame da tela, preenchendo o espaço entre uma resposta e outra.
  SINCRONIZAR_ESTADO_MS: 33,
  HEARTBEAT_ENTRADA_MS: 250, // reenvia as teclas apertadas, para o servidor saber que estamos vivos

  // Suavização do desenho
  SUAVIZACAO_MS: 60,          // quão rápido o desenho "alcança" a posição do servidor
  EXTRAPOLACAO_MAX_MS: 150,   // por quanto tempo continua andando sem resposta nova
  DISTANCIA_TELEPORTE: 250,   // erro maior que isso (ex.: reinício) = pula direto

  TEMPO_MAXIMO_S: 90,

  // Animação do tiro (Imagens/firehomem.png: tiro pra frente + recuo da arma).
  // Cada quadro mede 925x674 px; com a altura do player (100px) fica com ~137px de largura.
  TIRO_FRAMES: 2,
  TIRO_DURACAO_MS: 350, // a animação inteira dura meio segundo

  // Sprite do tiro de cada personagem. Cada quadro tem largura x altura em px
  // (a largura na tela é calculada a partir da altura do player).
  TIRO_SPRITES: Object.freeze({
    masculino: Object.freeze({ arquivo: "firehomem.png", quadroLargura: 925, quadroAltura: 674 }),
    feminino: Object.freeze({ arquivo: "firefem.png", quadroLargura: 480, quadroAltura: 446 }),
  }),
});
