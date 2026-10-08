/** Menu inicial: nome, escolha de gênero e botão de começar. */
export class TelaInicial {
  #tela = document.getElementById("telaInicial");
  #campoNome = document.getElementById("nome");
  #botaoComecar = document.getElementById("btnRecomecar");
  #cartoes = {
    masculino: document.querySelector(".masculino"),
    feminino: document.querySelector(".feminino"),
  };

  mostrar() { this.#tela.style.display = "flex"; }
  esconder() { this.#tela.style.display = "none"; }

  getNome() {
    return this.#campoNome.value.trim();
  }

  aoComecar(callback) {
    this.#botaoComecar.addEventListener("click", callback);
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
