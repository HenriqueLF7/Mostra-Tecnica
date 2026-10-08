import { Corpo } from "./Corpo.js";

/** Desenha os tiros que estão voando (um elemento por tiro, reaproveitado entre as respostas). */
export class TirosView {
  #container;
  #tiros = new Map(); // id -> { el, corpo, angulo }

  constructor(container) {
    this.#container = container;
  }

  atualizar(tiros, agora) {
    const lista = Array.isArray(tiros) ? tiros : [];
    const vivos = new Set();

    for (const dados of lista) {
      vivos.add(dados.id);

      let tiro = this.#tiros.get(dados.id);

      if (!tiro) {
        tiro = this.#criar(dados, agora);
        this.#tiros.set(dados.id, tiro);
      }

      tiro.corpo.receber(dados.x, dados.y, dados.vx, dados.vy, agora);
    }

    for (const [id, tiro] of this.#tiros) {
      if (!vivos.has(id)) {
        tiro.el.remove();
        this.#tiros.delete(id);
      }
    }
  }

  renderizar(agora, dt) {
    for (const tiro of this.#tiros.values()) {
      tiro.corpo.atualizar(agora, dt);
      this.#posicionar(tiro);
    }
  }

  limpar() {
    for (const tiro of this.#tiros.values()) tiro.el.remove();
    this.#tiros.clear();
  }

  #posicionar(tiro) {
    tiro.el.style.transform =
      "translate(" + Math.round(tiro.corpo.x) + "px," + Math.round(tiro.corpo.y) + "px)"
      + " rotate(" + tiro.angulo + "deg)";
  }

  #criar(dados, agora) {
    const el = document.createElement("div");
    el.className = "tiro";
    this.#container.appendChild(el);

    const tiro = { el, corpo: new Corpo(dados.x, dados.y, agora), angulo: dados.angulo || 0 };
    this.#posicionar(tiro);
    return tiro;
  }
}
