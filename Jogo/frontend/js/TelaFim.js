import { formatarTempo } from "./utils.js";

/** Tela de Game Over com o resultado da partida. */
export class TelaFim {
  #tela = document.getElementById("telaFim");
  #kills = document.getElementById("resultadoKills");
  #tempo = document.getElementById("resultadoTempo");
  #botaoVoltar = document.getElementById("b2");

  mostrar() { this.#tela.style.display = "flex"; }
  esconder() { this.#tela.style.display = "none"; }

  exibirResultado(resultado) {
    this.#kills.textContent = resultado.kills;
    this.#tempo.textContent = formatarTempo(resultado.tempo);
  }

  aoVoltar(callback) {
    this.#botaoVoltar.addEventListener("click", callback);
  }
}
