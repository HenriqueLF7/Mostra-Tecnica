import { CONFIG } from "./config.js";

/** Desenha os zumbis. Cria as divs conforme o servidor manda e reaproveita as antigas. */
export class HordaView {
  #container;
  #elementos = [];
  #zumbis = [];
  #frame = 0;

  constructor(container) {
    this.#container = container;
  }

  atualizar(zumbis) {
    this.#zumbis = zumbis;
  }

  avancarFrame() {
    this.#frame = (this.#frame + 1) % CONFIG.FRAMES_ZUMBI;
  }

  renderizar(centroPlayerX) {
    while (this.#elementos.length < this.#zumbis.length) {
      this.#elementos.push(this.#criarElemento());
    }

    this.#elementos.forEach((el, i) => {
      const zumbi = this.#zumbis[i];

      if (!zumbi) {
        el.style.display = "none";
        return;
      }

      el.style.display = "block";
      el.style.left = zumbi.x + "px";
      el.style.top = zumbi.y + "px";

      // vira o sprite para olhar na direção do player
      const olhaParaEsquerda = zumbi.x + CONFIG.ZUMBI_METADE_LARGURA > centroPlayerX;
      el.style.transform = olhaParaEsquerda ? "scaleX(-1)" : "scaleX(1)";

      // cada zumbi com um frame diferente, pra não andarem todos sincronizados
      const frame = (this.#frame + i) % CONFIG.FRAMES_ZUMBI;
      el.style.backgroundPosition = (frame / (CONFIG.FRAMES_ZUMBI - 1)) * 100 + "% 0%";
    });
  }

  #criarElemento() {
    const el = document.createElement("div");
    el.className = "zumbi";
    this.#container.appendChild(el);
    return el;
  }
}
