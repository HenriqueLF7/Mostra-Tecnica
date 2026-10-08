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

  // trava para não empilhar requisições de tiro
  #atirando = false;

  // Controle de teclas: só falamos com o servidor quando as teclas MUDAM
  // (e um "ainda estou aqui" de tempos em tempos). Uma requisição por vez,
  // sempre com o estado mais novo, para nunca chegarem fora de ordem.
  #entrada = { desejado: "-", enviado: "-", emVoo: false };

  // Para ignorar respostas atrasadas do servidor
  #sessao = null;
  #versao = -1;

  #ultimoFrame = 0;

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

    this.#ultimoFrame = performance.now();
    requestAnimationFrame((agora) => this.#desenhar(agora));
  }

  // ------------------------------------------------------------
  // Eventos
  // ------------------------------------------------------------

  #ligarEventos() {
    this.#teclado.aoRecarregar(() => this.#recarregar());
    this.#teclado.aoMudar(() => this.#aoMudarTeclas());

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
      this.#player.animar(this.#teclado.andando);
    }, CONFIG.ANIMACAO_PLAYER_MS);

    setInterval(() => this.#batimento(), CONFIG.HEARTBEAT_ENTRADA_MS);

    this.#repetirSincronizacao();
  }

  /** Busca o estado do servidor sem nunca empilhar pedidos: só pede de novo quando o anterior terminou. */
  async #repetirSincronizacao() {
    while (true) {
      const inicio = performance.now();

      await this.#sincronizarEstado();

      const espera = Math.max(0, CONFIG.SINCRONIZAR_ESTADO_MS - (performance.now() - inicio));
      await new Promise((resolver) => setTimeout(resolver, espera));
    }
  }

  // ------------------------------------------------------------
  // Teclas de movimento
  // ------------------------------------------------------------

  #aoMudarTeclas() {
    this.#entrada.desejado = this.#teclado.comando;

    if (this.#jogando) this.#despacharEntrada();
  }

  /** Reenvia as teclas apertadas, para o servidor não achar que o navegador travou. */
  #batimento() {
    if (!this.#jogando || !this.#teclado.andando) return;

    this.#entrada.enviado = null;
    this.#despacharEntrada();
  }

  async #despacharEntrada() {
    const entrada = this.#entrada;

    if (entrada.emVoo) return;
    entrada.emVoo = true;

    try {
      // se as teclas mudarem enquanto a requisição viaja, manda o estado novo logo em seguida
      while (entrada.enviado !== entrada.desejado) {
        entrada.enviado = entrada.desejado;

        const dados = await this.#api.enviarComando(entrada.enviado);
        this.#aplicarEstado(dados);
      }
    } catch (erro) {
      console.error("Erro ao enviar teclas:", erro);
    } finally {
      entrada.emVoo = false;
    }
  }

  // ------------------------------------------------------------
  // Comunicação com o servidor
  // ------------------------------------------------------------

  async #atirar(lado) {
    if (this.#atirando) return;
    this.#atirando = true;

    try {
      const resposta = await this.#api.atirar(lado, this.#mira.x, this.#mira.y);

      // só anima quando o servidor realmente soltou o tiro (não durante o cooldown)
      if (resposta.disparou) {
        this.#player.animarTiro();
      }
    } catch (erro) {
      console.error("Erro ao atirar:", erro);
    } finally {
      this.#atirando = false;
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
    try {
      this.#aplicarEstado(await this.#api.buscarEstado());
    } catch (erro) {
      console.error("Erro ao buscar estado:", erro);
    }
  }

  /** Recebe o estado do servidor (de qualquer requisição) e distribui para cada parte do jogo. */
  #aplicarEstado(dados) {
    const agora = performance.now();

    // servidor reiniciado: recomeça a contar as versões
    if (dados.sessao !== this.#sessao) {
      this.#sessao = dados.sessao;
      this.#versao = -1;
    }

    // resposta que ficou para trás (chegou depois de uma mais nova): ignora
    if (dados.versao < this.#versao) return;
    this.#versao = dados.versao;

    if (typeof dados.x === "number" && typeof dados.y === "number") {
      this.#player.receberEstado(dados, agora);
    }

    this.#player.definirInvulneravel(Boolean(dados.invulneravel));

    if (Array.isArray(dados.zumbis)) {
      this.#horda.atualizar(dados.zumbis, agora);
    }

    this.#tiros.atualizar(dados.tiros, agora);
    this.#hud.atualizarVida(dados.pontosVida);
    this.#hud.atualizarOnda(dados.onda);
    this.#hud.atualizarMunicao(dados.municao, dados.capacidade, dados.recarregando);

    if (this.#jogando && dados.pontosVida <= 0) {
      this.#fimDeJogo();
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

    // o personagem nasce no centro: o desenho vai direto para lá, sem deslizar
    this.#player.pularNoProximoEstado();
    this.#horda.limpar();
    this.#tiros.limpar();

    this.#jogando = true;

    // se alguma tecla já estava apertada ao começar, avisa o servidor
    this.#entrada.desejado = this.#teclado.comando;
    this.#entrada.enviado = null;
    this.#despacharEntrada();
  }

  async #fimDeJogo() {
    if (!this.#jogando) return;
    this.#jogando = false;

    this.#cronometro.parar();
    this.#teclado.limpar();
    this.#entrada.desejado = "-";

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
  // Desenho (um por frame da tela, ~60x/s)
  // ------------------------------------------------------------

  #desenhar(agora) {
    // segundos desde o frame anterior (limitado, para um travamento da aba não "teleportar" tudo)
    const dt = Math.min(Math.max((agora - this.#ultimoFrame) / 1000, 0), 0.05);
    this.#ultimoFrame = agora;

    this.#player.atualizar(agora, dt);
    this.#player.olharPara(this.#mira.x);
    this.#horda.renderizar(this.#player.centroX, agora, dt);
    this.#tiros.renderizar(agora, dt);

    requestAnimationFrame((proximo) => this.#desenhar(proximo));
  }
}
