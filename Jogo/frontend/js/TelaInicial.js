/** Menu inicial: nome, escolha de gênero e botão de começar. */
export class TelaInicial {
  #tela = document.getElementById("telaInicial");
  #campoNome = document.getElementById("nome");
  #botaoComecar = document.getElementById("btnRecomecar");
  #campoWrapper = document.querySelector(".campo-nome");
  #erro = document.getElementById("erro-nome");
  #cartoes = {
    masculino: document.querySelector(".masculino"),
    feminino: document.querySelector(".feminino"),
  };

  mostrar() { this.#tela.style.display = "flex"; }
  esconder() { this.#tela.style.display = "none"; }

  getNome() {
    return this.#campoNome.value.trim();
  }

  /** O nome é obrigatório: sem nome, não começa e o campo fica vermelho. */
  aoComecar(callback) {
    const tentar = () => {
      if (!this.getNome()) {
        this.#mostrarErro();
        return;
      }

      this.#limparErro();
      callback();
    };

    this.#botaoComecar.addEventListener("click", tentar);

    this.#campoNome.addEventListener("keydown", (e) => {
      if (e.key === "Enter") tentar();
    });

    this.#campoNome.addEventListener("input", () => this.#limparErro());
  }

  #mostrarErro() {
    this.#campoWrapper.classList.remove("erro");
    void this.#campoWrapper.offsetWidth; // reinicia a animação de tremer
    this.#campoWrapper.classList.add("erro");
    this.#erro.classList.add("visivel");
    this.#campoNome.focus();
  }

  #limparErro() {
    this.#campoWrapper.classList.remove("erro");
    this.#erro.classList.remove("visivel");
  }

  /** Marca o cartão escolhido e avisa quem quiser saber qual gênero foi escolhido. */
  aoEscolherGenero(callback) {
    for (const [genero, cartao] of Object.entries(this.#cartoes)) {
      cartao.addEventListener("click", () => {
        this.selecionarGenero(genero);
        callback(genero);
      });
    }
  }

  selecionarGenero(genero) {
    for (const [nome, cartao] of Object.entries(this.#cartoes)) {
      cartao.classList.toggle("selecionado", nome === genero);
    }
  }
}