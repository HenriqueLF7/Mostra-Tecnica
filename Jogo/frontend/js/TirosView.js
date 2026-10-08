/** Desenha os tiros que estão voando. */
export class TirosView {
  #container;

  constructor(container) {
    this.#container = container;
  }

  renderizar(tiros) {
    this.#container.querySelectorAll(".tiro").forEach((el) => el.remove());

    if (!Array.isArray(tiros)) return;

    for (const tiro of tiros) {
      const el = document.createElement("div");
      el.className = "tiro";
      el.style.left = tiro.x + "px";
      el.style.top = tiro.y + "px";
      el.style.transform = "rotate(" + (tiro.angulo || 0) + "deg)";
      this.#container.appendChild(el);
    }
  }
}
