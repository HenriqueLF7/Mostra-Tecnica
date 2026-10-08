/** Guarda quais teclas de movimento (W A S D) estão apertadas. */
export class Teclado {
  #teclas = { w: false, a: false, s: false, d: false };
  #aoRecarregar = () => {};
  #aoMudar = () => {};

  constructor(documento = document) {
    documento.addEventListener("keydown", (e) => {
      if (e.key.toLowerCase() === "r" && !e.repeat) this.#aoRecarregar();
      this.#marcar(e, true);
    });
    documento.addEventListener("keyup", (e) => this.#marcar(e, false));

    // Trocou de aba/janela com a tecla apertada: o keyup nunca chega e o personagem andaria sozinho.
    window.addEventListener("blur", () => {
      const antes = this.comando;
      this.limpar();
      if (this.comando !== antes) this.#aoMudar();
    });
  }

  #marcar(evento, apertada) {
    const tecla = evento.key.toLowerCase();

    if (tecla in this.#teclas) {
      const antes = this.comando;
      this.#teclas[tecla] = apertada;

      if (this.comando !== antes) this.#aoMudar();
    }
  }

  /** Chama o callback quando a tecla R é apertada. */
  aoRecarregar(callback) { this.#aoRecarregar = callback; }

  /** Chama o callback quando o conjunto de teclas de movimento apertadas MUDA. */
  aoMudar(callback) { this.#aoMudar = callback; }

  get cima() { return this.#teclas.w; }
  get esquerda() { return this.#teclas.a; }
  get baixo() { return this.#teclas.s; }
  get direita() { return this.#teclas.d; }

  get andando() {
    return this.cima || this.esquerda || this.baixo || this.direita;
  }

  /** Teclas apertadas como texto para o servidor: "W", "AS", "WD"... ou "-" se nenhuma. */
  get comando() {
    const texto = (this.cima ? "W" : "")
      + (this.esquerda ? "A" : "")
      + (this.baixo ? "S" : "")
      + (this.direita ? "D" : "");

    return texto || "-";
  }

  limpar() {
    for (const tecla in this.#teclas) {
      this.#teclas[tecla] = false;
    }
  }
}
