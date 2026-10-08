/** Guarda quais teclas de movimento (W A S D) estão apertadas. */
export class Teclado {
  #teclas = { w: false, a: false, s: false, d: false };
  #aoRecarregar = () => {};

  constructor(documento = document) {
    documento.addEventListener("keydown", (e) => {
      if (e.key.toLowerCase() === "r" && !e.repeat) this.#aoRecarregar();
      this.#marcar(e, true);
    });
    documento.addEventListener("keyup", (e) => this.#marcar(e, false));
  }

  #marcar(evento, apertada) {
    const tecla = evento.key.toLowerCase();

    if (tecla in this.#teclas) {
      this.#teclas[tecla] = apertada;
    }
  }

  /** Chama o callback quando a tecla R é apertada. */
  aoRecarregar(callback) { this.#aoRecarregar = callback; }

  get cima() { return this.#teclas.w; }
  get esquerda() { return this.#teclas.a; }
  get baixo() { return this.#teclas.s; }
  get direita() { return this.#teclas.d; }

  get andando() {
    return this.cima || this.esquerda || this.baixo || this.direita;
  }

  limpar() {
    for (const tecla in this.#teclas) {
      this.#teclas[tecla] = false;
    }
  }
}
