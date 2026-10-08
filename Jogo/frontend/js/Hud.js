/** Vidas, número da onda e munição na tela. */
export class Hud {
  #vidas = document.querySelectorAll("#hud-vida .vida");
  #onda = document.getElementById("hud-onda");
  #municaoBox = document.getElementById("hud-municao");
  #municaoAtual = document.getElementById("municao-atual");
  #municaoCapacidade = document.getElementById("municao-capacidade");

  atualizarVida(pontos) {
    this.#vidas.forEach((coracao, i) => {
      coracao.classList.toggle("perdida", i >= pontos);
    });
  }

  atualizarOnda(onda) {
    this.#onda.textContent = "ONDA " + Math.max(1, onda);
  }

  atualizarMunicao(municao, capacidade, recarregando) {
    if (typeof municao !== "number" || typeof capacidade !== "number") return;

    this.#municaoAtual.textContent = municao;
    this.#municaoCapacidade.textContent = capacidade;

    this.#municaoBox.classList.toggle("recarregando", Boolean(recarregando));
    this.#municaoBox.classList.toggle("vazio", municao === 0);
  }
}
