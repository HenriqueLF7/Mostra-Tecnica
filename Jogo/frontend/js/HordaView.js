import { CONFIG } from "./config.js";
import { Corpo } from "./Corpo.js";

/**
 * Desenha os zumbis. Cada zumbi do servidor tem um id e guarda a sua própria
 * div: quando um morre, os outros continuam com o mesmo elemento (antes eles
 * "trocavam de lugar" porque a ligação era pela posição no array).
 */
export class HordaView {
  #container;
  #zumbis = new Map(); // id -> { el, corpo, paraEsquerda, frame }

  constructor(container) {
    this.#container = container;
  }

  /** Chegou o estado do servidor: cria zumbis novos, atualiza os que já existem e remove os que sumiram. */
  atualizar(zumbis, agora) {
    const vivos = new Set();

    for (const dados of zumbis) {
      vivos.add(dados.id);

      let zumbi = this.#zumbis.get(dados.id);

      if (!zumbi) {
        zumbi = this.#criar(dados, agora);
        this.#zumbis.set(dados.id, zumbi);
      }

      zumbi.corpo.receber(dados.x, dados.y, dados.vx, dados.vy, agora);
    }

    for (const [id, zumbi] of this.#zumbis) {
      if (!vivos.has(id)) {
        zumbi.el.remove();
        this.#zumbis.delete(id);
      }
    }
  }

  /** A cada frame da tela. `dt` em segundos. */
  renderizar(centroPlayerX, agora, dt) {
    const passoAnimacao = Math.floor(agora / CONFIG.ANIMACAO_ZUMBI_MS);

    for (const [id, zumbi] of this.#zumbis) {
      zumbi.corpo.atualizar(agora, dt);

      // vira o sprite para olhar na direção do player (com folga, para não ficar piscando de lado)
      const distancia = zumbi.corpo.x + CONFIG.ZUMBI_METADE_LARGURA - centroPlayerX;
      if (distancia > 6) zumbi.paraEsquerda = true;
      else if (distancia < -6) zumbi.paraEsquerda = false;

      this.#posicionar(zumbi);

      // cada zumbi com um frame diferente (somando o id), pra não andarem todos sincronizados
      const frame = (passoAnimacao + id) % CONFIG.FRAMES_ZUMBI;

      if (frame !== zumbi.frame) {
        zumbi.frame = frame;
        zumbi.el.style.backgroundPosition = (frame / (CONFIG.FRAMES_ZUMBI - 1)) * 100 + "% 0%";
      }
    }
  }

  limpar() {
    for (const zumbi of this.#zumbis.values()) zumbi.el.remove();
    this.#zumbis.clear();
  }

  #posicionar(zumbi) {
    const x = Math.round(zumbi.corpo.x);
    const y = Math.round(zumbi.corpo.y);

    zumbi.el.style.transform =
      "translate(" + x + "px," + y + "px) scaleX(" + (zumbi.paraEsquerda ? -1 : 1) + ")";
  }

  #criar(dados, agora) {
    const el = document.createElement("div");
    el.className = "zumbi";
    this.#container.appendChild(el);

    const zumbi = {
      el,
      corpo: new Corpo(dados.x, dados.y, agora),
      paraEsquerda: true,
      frame: -1,
    };

    this.#posicionar(zumbi); // já nasce no lugar certo (sem piscar no canto da tela)
    return zumbi;
  }
}
