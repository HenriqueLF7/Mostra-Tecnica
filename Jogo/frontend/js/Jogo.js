import { ApiClient } from "./ApiClient.js";
import { CONFIG } from "./config.js";
import { Cronometro } from "./Cronometro.js";
import { HordaView } from "./HordaView.js";
import { Hud } from "./Hud.js";
import { Mira } from "./Mira.js";
import { PlayerView } from "./PlayerView.js";
import { RankingView } from "./RankingView.js";
import { Teclado } from "./Teclado.js";
import { TelaFim } from "./TelaFim.js";
import { TelaInicial } from "./TelaInicial.js";
import { TirosView } from "./TirosView.js";

/**
 * Classe principal do front: cria as peças, liga uma na outra e
 * mantém o "relógio" do jogo (envio de comandos e leitura do estado).
 */
export class Jogo {
  #api = new ApiClient();
  #teclado = new Teclado();
  #mira = new Mira();

  #player = new PlayerView(document.getElementById("player"));
  #horda = new HordaView(document.getElementById("game"));
  #tiros = new TirosView(document.getElementById("game"));
  #hud = new Hud();
  #ranking = new RankingView();
  #telaInicial = new TelaInicial();
  #telaFim = new TelaFim();
  #cronometro = new Cronometro(() => this.#fimDeJogo());

  #jogando = false;

  // trava para não empilhar requisições do mesmo tipo
  #emAndamento = { vertical: false, horizontal: false, tiro: false, estado: false };

  /** Liga tudo e deixa o servidor pausado, mostrando o menu. */
  iniciar() {
    PlayerView.precarregarSprites();
    this.#ligarEventos();
    this.#iniciarRelogios();

    this.#telaInicial.selecionarGenero("masculino");
    this.#telaInicial.mostrar();
    this.#cronometro.esconder();

    this.#api.reiniciar();
    this.#atualizarRanking();
    this.#renderizar();
  }

  // ------------------------------------------------------------
  // Eventos
  // ------------------------------------------------------------

  #ligarEventos() {
    this.#teclado.aoRecarregar(() => this.#recarregar());

    this.#mira.aoMover(() => this.#player.olharPara(this.#mira.x));
    this.#mira.aoPressionar(() => this.#aoClicar());
    this.#mira.aoSoltar(() => this.#player.pararDeMirar());

    this.#telaInicial.aoEscolherGenero((genero) => this.#player.definirGenero(genero));
    this.#telaInicial.aoComecar(() => this.#iniciarPartida());
    this.#telaFim.aoVoltar(() => this.#voltarAoMenu());
  }

  #aoClicar() {
    if (!this.#jogando) return;

    const lado = this.#player.ladoDoMouse(this.#mira.x);

    this.#player.mirar(lado);
    this.#atirar(lado);
  }

  // ------------------------------------------------------------
  // Relógios (loops do front)
  // ------------------------------------------------------------

  #iniciarRelogios() {
    setInterval(() => {
      if (this.#jogando) this.#horda.avancarFrame();
    }, CONFIG.ANIMACAO_ZUMBI_MS);

    setInterval(() => {
      this.#player.animar(this.#teclado.andando);
    }, CONFIG.ANIMACAO_PLAYER_MS);

    setInterval(() => this.#enviarComandos(), CONFIG.ENVIAR_COMANDOS_MS);
    setInterval(() => this.#sincronizarEstado(), CONFIG.SINCRONIZAR_ESTADO_MS);
  }

  // ------------------------------------------------------------
  // Comunicação com o servidor
  // ------------------------------------------------------------

  #enviarComandos() {
    if (!this.#jogando) return;

    if (this.#teclado.cima) {
      this.#enviarComando("W", "vertical");
    } else if (this.#teclado.baixo) {
      this.#enviarComando("S", "vertical");
    }

    if (this.#teclado.esquerda) {
      this.#enviarComando("A", "horizontal");
    } else if (this.#teclado.direita) {
      this.#enviarComando("D", "horizontal");
    }
  }

  async #enviarComando(comando, eixo) {
    if (this.#emAndamento[eixo]) return;
    this.#emAndamento[eixo] = true;

    try {
      const dados = await this.#api.enviarComando(comando);

      if (dados && typeof dados.x === "number" && typeof dados.y === "number") {
        const larguraMax = window.innerWidth - CONFIG.PLAYER_LARGURA;
        const alturaMax = window.innerHeight - CONFIG.PLAYER_ALTURA;

        this.#player.definirPosicao(
          Math.max(0, Math.min(dados.x, larguraMax)),
          Math.max(0, Math.min(dados.y, alturaMax))
        );
        this.#renderizar();
      }
    } catch (erro) {
      console.error("Erro ao enviar comando:", erro);
    } finally {
      this.#emAndamento[eixo] = false;
    }
  }

  async #atirar(lado) {
    if (this.#emAndamento.tiro) return;
    this.#emAndamento.tiro = true;

    try {
      const resposta = await this.#api.atirar(lado, this.#mira.x, this.#mira.y);

      // só anima quando o servidor realmente soltou o tiro (não durante o cooldown)
      if (resposta.disparou) {
        this.#player.animarTiro();
      }
    } catch (erro) {
      console.error("Erro ao atirar:", erro);
    } finally {
      this.#emAndamento.tiro = false;
    }
  }

  async #recarregar() {
    if (!this.#jogando) return;

    try {
      await this.#api.recarregar();
    } catch (erro) {
      console.error("Erro ao recarregar:", erro);
    }
  }

  async #sincronizarEstado() {
    if (this.#emAndamento.estado) return;
    this.#emAndamento.estado = true;

    try {
      const dados = await this.#api.buscarEstado();

      if (typeof dados.x === "number" && typeof dados.y === "number") {
        this.#player.definirPosicao(dados.x, dados.y);
      }

      if (Array.isArray(dados.zumbis)) {
        this.#horda.atualizar(dados.zumbis);
      }

      this.#tiros.renderizar(dados.tiros);
      this.#hud.atualizarVida(dados.pontosVida);
      this.#hud.atualizarOnda(dados.onda);
      this.#hud.atualizarMunicao(dados.municao, dados.capacidade, dados.recarregando);

      if (this.#jogando && dados.pontosVida <= 0) {
        this.#fimDeJogo();
      }

      this.#renderizar();
    } catch (erro) {
      console.error("Erro ao buscar estado:", erro);
    } finally {
      this.#emAndamento.estado = false;
    }
  }

  async #atualizarRanking() {
    try {
      this.#ranking.renderizar(await this.#api.buscarRanking());
    } catch (erro) {
      console.error("Erro ao carregar ranking:", erro);
    }
  }

  // ------------------------------------------------------------
  // Início e fim da partida
  // ------------------------------------------------------------

  async #iniciarPartida() {
    try {
      await this.#api.reiniciar();
      await this.#api.iniciar(this.#telaInicial.getNome() || "Jogador");
    } catch (erro) {
      console.error("Erro ao iniciar a partida:", erro);
      return;
    }

    this.#cronometro.iniciar();
    this.#telaInicial.esconder();
    this.#telaFim.esconder();

    this.#jogando = true;
  }

  async #fimDeJogo() {
    if (!this.#jogando) return;
    this.#jogando = false;

    this.#cronometro.parar();
    this.#teclado.limpar();

    try {
      this.#telaFim.exibirResultado(await this.#api.parar());
    } catch (erro) {
      console.error("Erro ao obter resultado da partida:", erro);
    }

    this.#telaFim.mostrar();
  }

  #voltarAoMenu() {
    this.#telaFim.esconder();
    this.#telaInicial.mostrar();
    this.#atualizarRanking();
  }

  // ------------------------------------------------------------
  // Desenho
  // ------------------------------------------------------------

  #renderizar() {
    this.#player.olharPara(this.#mira.x);
    this.#horda.renderizar(this.#player.centroX);
  }
}
