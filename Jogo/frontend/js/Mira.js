/** Acompanha o mouse: posição da mira e clique esquerdo. */
export class Mira {
  x = window.innerWidth / 2;
  y = window.innerHeight / 2;

  #aoMover = () => {};
  #aoPressionar = () => {};
  #aoSoltar = () => {};

  constructor(documento = document) {
    documento.addEventListener("mousemove", (e) => {
      this.x = e.clientX;
      this.y = e.clientY;
      this.#aoMover();
    });

    documento.addEventListener("mousedown", (e) => {
      if (e.button === 0) this.#aoPressionar();
    });

    documento.addEventListener("mouseup", (e) => {
      if (e.button === 0) this.#aoSoltar();
    });
  }

  aoMover(callback) { this.#aoMover = callback; }
  aoPressionar(callback) { this.#aoPressionar = callback; }
  aoSoltar(callback) { this.#aoSoltar = callback; }
}
