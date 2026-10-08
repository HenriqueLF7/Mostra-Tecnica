import { CONFIG } from "./config.js";
import { formatarTempo } from "./utils.js";

/** Cronômetro da partida (com o botão de acelerar 5x). Termina quando chega ao tempo máximo. */
export class Cronometro {
  #elemento = document.getElementById("cronometro");
  #botao5x = document.getElementById("btnTempo5x");
  #aoTerminar;

  #tempo = 0;
  #multiplicador = 1;
  #intervalo = null;

  constructor(aoTerminar) {
    this.#aoTerminar = aoTerminar;
    this.#botao5x.addEventListener("click", () => this.#alternar5x());
  }

  iniciar() {
    this.#tempo = 0;
    this.#multiplicador = 1;
    this.#atualizarBotao();
    this.#botao5x.disabled = false;

    this.#elemento.textContent = "00:00";
    this.#elemento.style.animation = "none";
    this.#elemento.style.display = "block";
    this.#aplicarCor("#42d624", "#42d624");

    clearInterval(this.#intervalo);
    this.#intervalo = setInterval(() => this.#tick(), 1000);
  }

  parar() {
    clearInterval(this.#intervalo);
    this.#elemento.style.display = "none";
    this.#botao5x.disabled = true;
  }

  esconder() {
    this.#elemento.style.display = "none";
  }

  #alternar5x() {
    this.#multiplicador = this.#multiplicador === 1 ? 5 : 1;
    this.#atualizarBotao();
  }

  #atualizarBotao() {
    const ativo = this.#multiplicador !== 1;
    this.#botao5x.textContent = ativo ? "5X ATIVO" : "5X TEMPO";
    this.#botao5x.classList.toggle("ativo", ativo);
  }

  #tick() {
    this.#tempo = Math.min(this.#tempo + this.#multiplicador, CONFIG.TEMPO_MAXIMO_S);
    this.#elemento.textContent = formatarTempo(this.#tempo);

    if (this.#tempo >= 70) {
      this.#aplicarCor("#ff2d2d", "#4a0000");
    } else if (this.#tempo >= 35) {
      this.#aplicarCor("#ffb020", "#7a4a00");
    } else {
      this.#aplicarCor("#42d624", "#42d624");
    }

    if (this.#tempo >= 80) {
      this.#elemento.style.animation = "tremor 1s infinite";
    }

    if (this.#tempo >= CONFIG.TEMPO_MAXIMO_S) {
      this.#aoTerminar();
    }
  }

  #aplicarCor(cor, sombraInterna) {
    this.#elemento.style.color = cor;
    this.#elemento.style.boxShadow =
      "0 0 0 2px " + cor +
      ", 0 0 0 5px #000000, inset 3px 3px 0 " + sombraInterna +
      ", inset -3px -3px 0 #050505";
    this.#elemento.style.setProperty("--cor-alerta", cor);
  }
}
