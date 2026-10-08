import { CONFIG } from "./config.js";
import { Corpo } from "./Corpo.js";

/** O boneco na tela: posição, direção, animação do sprite e skin. */
export class PlayerView {
  #elemento;
  #x = 100;
  #y = 100;
  #corpo = new Corpo(100, 100);
  #pularProximoEstado = false;
  #invulneravel = false;
  #frame = 0;
  #estado = null;
  #direcao = null;
  #genero = "masculino";

  // animação do tiro
  #atirando = false;
  #frameTiro = 0;
  #intervaloTiro = null;

  constructor(elemento) {
    this.#elemento = elemento;
    this.definirPosicao(this.#x, this.#y);
  }

  /** Carrega os sprites antes, para não piscar na primeira vez que aparecem. */
  static precarregarSprites() {
    const arquivos = ["player.png", "idle.png", "player_fem.png", "idle_fem.png",
      "firehomem.png", "firefem.png"];

    for (const arquivo of arquivos) {
      new Image().src = "Imagens/" + arquivo;
    }
  }

  get x() { return this.#x; }
  get y() { return this.#y; }
  get centroX() { return this.#x + CONFIG.PLAYER_LARGURA / 2; }

  definirPosicao(x, y) {
    this.#x = x;
    this.#y = y;
    this.#aplicarPosicao();
  }

  /** Chegou o estado do servidor (posição + velocidade do player). */
  receberEstado(dados, agora) {
    this.#corpo.receber(dados.x, dados.y, dados.vx, dados.vy, agora, this.#pularProximoEstado);
    this.#pularProximoEstado = false;
  }

  /** Na próxima resposta o personagem vai direto para a posição (ex.: início da partida). */
  pularNoProximoEstado() {
    this.#pularProximoEstado = true;
  }

  /** A cada frame da tela: anda suavemente e atualiza o elemento. `dt` em segundos. */
  atualizar(agora, dt) {
    this.#corpo.atualizar(agora, dt);

    const larguraMax = window.innerWidth - CONFIG.PLAYER_LARGURA;
    const alturaMax = window.innerHeight - CONFIG.PLAYER_ALTURA;

    this.definirPosicao(
      Math.round(Math.max(0, Math.min(this.#corpo.x, larguraMax))),
      Math.round(Math.max(0, Math.min(this.#corpo.y, alturaMax)))
    );
  }

  /** Enquanto o player está invulnerável (depois de levar dano) ele pisca. */
  definirInvulneravel(invulneravel) {
    if (invulneravel === this.#invulneravel) return;

    this.#invulneravel = invulneravel;
    this.#elemento.classList.toggle("piscando", invulneravel);
  }

  #aplicarPosicao() {
    // Olhando pra esquerda (espelhado), o sprite do tiro é mais largo que o player:
    // desloca o elemento para o personagem não "pular" de lugar.
    const deslocamento =
      this.#atirando && this.#direcao === "esquerda" ? this.#deslocamentoEsquerda : 0;

    this.#elemento.style.left = this.#x - deslocamento + "px";
    this.#elemento.style.top = this.#y + "px";
  }

  get #spriteTiro() {
    return CONFIG.TIRO_SPRITES[this.#genero];
  }

  get #larguraQuadroTiro() {
    const { quadroLargura, quadroAltura } = this.#spriteTiro;
    return Math.round((CONFIG.PLAYER_ALTURA * quadroLargura) / quadroAltura);
  }

  get #deslocamentoEsquerda() {
    return this.#larguraQuadroTiro - CONFIG.PLAYER_LARGURA - 1;
  }

  definirGenero(genero) {
    this.#genero = genero === "feminino" ? "feminino" : "masculino";
    this.#elemento.classList.toggle("skin-fem", genero === "feminino");
  }

  /** O personagem olha para o lado onde o mouse está. */
  ladoDoMouse(mouseX) {
    return mouseX < this.centroX ? "esquerda" : "direita";
  }

  olharPara(mouseX) {
    const direcao = this.ladoDoMouse(mouseX);

    if (direcao === this.#direcao) return;

    this.#direcao = direcao;
    this.#elemento.style.transform = direcao === "esquerda" ? "scaleX(-1)" : "scaleX(1)";
    this.#aplicarPosicao();
  }

  mirar(lado) {
    this.#elemento.classList.add("mirando");
    this.#elemento.classList.toggle("mirando-esquerda", lado === "esquerda");
    this.#elemento.classList.toggle("mirando-direita", lado === "direita");
  }

  pararDeMirar() {
    this.#elemento.classList.remove("mirando", "mirando-esquerda", "mirando-direita");
  }

  /** Toca a animação do tiro UMA vez (sem loop). Atirar de novo recomeça do primeiro quadro. */
  animarTiro() {
    this.#atirando = true;
    this.#frameTiro = 0;

    const estilo = this.#elemento.style;
    estilo.backgroundImage = 'url("Imagens/' + this.#spriteTiro.arquivo + '")';
    estilo.width = this.#larguraQuadroTiro + "px";
    estilo.backgroundSize = CONFIG.TIRO_FRAMES * 100 + "% 100%";
    estilo.imageRendering = "pixelated";
    this.#desenharFrameTiro();
    this.#aplicarPosicao();

    clearInterval(this.#intervaloTiro);
    this.#intervaloTiro = setInterval(() => {
      this.#frameTiro++;

      if (this.#frameTiro >= CONFIG.TIRO_FRAMES) {
        this.#pararAnimacaoTiro();
        return;
      }

      this.#desenharFrameTiro();
    }, CONFIG.TIRO_DURACAO_MS / CONFIG.TIRO_FRAMES);
  }

  #desenharFrameTiro() {
    const percentual = CONFIG.TIRO_FRAMES > 1
      ? (this.#frameTiro / (CONFIG.TIRO_FRAMES - 1)) * 100
      : 0;

    this.#elemento.style.backgroundPosition = percentual + "% 0%";
  }

  #pararAnimacaoTiro() {
    if (!this.#atirando) return;

    this.#atirando = false;
    clearInterval(this.#intervaloTiro);

    // remove os estilos inline para voltar ao sprite de idle/andando do CSS
    const estilo = this.#elemento.style;
    estilo.backgroundImage = "";
    estilo.backgroundSize = "";
    estilo.width = "";
    estilo.imageRendering = "";

    this.#aplicarPosicao();
    this.#atualizarFrame();
  }

  /** Chamado a cada passo da animação: anda (troca de frame) ou fica parado. */
  animar(andando) {
    // enquanto atira, a animação do tiro controla o sprite
    if (this.#atirando) return;

    if (andando) {
      this.#definirEstado("andando");
      this.#frame = (this.#frame + 1) % CONFIG.FRAMES_PLAYER;
      this.#atualizarFrame();
    } else {
      this.#definirEstado("idle");
    }
  }

  #definirEstado(estado) {
    if (estado === this.#estado) return;

    this.#estado = estado;
    this.#elemento.classList.toggle("idle", estado === "idle");
    this.#elemento.classList.toggle("andando", estado === "andando");

    if (estado === "idle") {
      this.#frame = 0;
    }
  }

  #atualizarFrame() {
    const percentual = (this.#frame / (CONFIG.FRAMES_PLAYER - 1)) * 100;
    this.#elemento.style.backgroundPosition = percentual + "% 0%";
  }
}
