import { formatarTempo } from "./utils.js";

/** A tabela do Top 5 na tela inicial. */
export class RankingView {
  #corpo = document.getElementById("rankingBody");

  renderizar(ranking) {
    this.#corpo.innerHTML = "";

    ranking.forEach((jogador, i) => {
      const linha = document.createElement("tr");

      // mesma ordem das colunas do cabeçalho: # | JOGADOR | KILLS | TEMPO
      const colunas = [i + 1, jogador.nome, jogador.kills, formatarTempo(jogador.tempo)];

      for (const valor of colunas) {
        const celula = document.createElement("td");
        celula.textContent = valor; // textContent: o nome do jogador nunca vira HTML
        linha.appendChild(celula);
      }

      this.#corpo.appendChild(linha);
    });
  }
}
