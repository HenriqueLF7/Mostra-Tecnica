/** Toda a comunicação com o servidor Java fica aqui. */
export class ApiClient {
  #tamanhoJanela() {
    return window.innerWidth + ";" + window.innerHeight;
  }

  async #post(url, corpo = "") {
    return fetch(url, { method: "POST", body: corpo });
  }

  async buscarEstado() {
    const resposta = await fetch("/player");
    return resposta.json();
  }

  async enviarComando(comando) {
    const resposta = await this.#post("/player", comando + ";" + this.#tamanhoJanela());
    return resposta.json();
  }

  async atirar(lado, mouseX, mouseY) {
    const corpo = [lado, this.#tamanhoJanela(), Math.round(mouseX), Math.round(mouseY)].join(";");
    const resposta = await this.#post("/tiro", corpo);
    return resposta.json();
  }

  async recarregar() {
    const resposta = await this.#post("/recarregar");
    return resposta.json();
  }

  async reiniciar() {
    await this.#post("/reiniciar", this.#tamanhoJanela());
  }

  async iniciar(nome) {
    await this.#post("/iniciar", nome);
  }

  async parar() {
    const resposta = await this.#post("/parar");
    return resposta.json();
  }

  async buscarRanking() {
    const resposta = await fetch("/ranking");
    return resposta.json();
  }
}
