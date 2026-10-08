import { CONFIG } from "./config.js";

/**
 * A posição DESENHADA de algo que o servidor controla (player, zumbi, tiro).
 *
 * O servidor manda posição + velocidade ~30 vezes por segundo, mas a tela
 * atualiza 60+ vezes por segundo. Entre uma resposta e outra o desenho
 * continua andando na velocidade informada, e quando chega uma resposta
 * nova o erro é corrigido aos poucos (sem "tranco").
 */
export class Corpo {
  x;
  y;

  #servidorX;
  #servidorY;
  #vx = 0;
  #vy = 0;
  #recebidoEm = 0;

  constructor(x, y, agora = performance.now()) {
    this.x = x;
    this.y = y;
    this.receber(x, y, 0, 0, agora);
  }

  /** Chegou uma posição nova do servidor. Com `pular`, o desenho vai direto para ela. */
  receber(x, y, vx, vy, agora, pular = false) {
    this.#servidorX = x;
    this.#servidorY = y;
    this.#vx = vx || 0;
    this.#vy = vy || 0;
    this.#recebidoEm = agora;

    if (pular) {
      this.x = x;
      this.y = y;
    }
  }

  /** Chamado a cada frame da tela. `dt` em segundos. */
  atualizar(agora, dt) {
    const idade = Math.max(0, agora - this.#recebidoEm);
    const andando = idade <= CONFIG.EXTRAPOLACAO_MAX_MS;
    const segundos = Math.min(idade, CONFIG.EXTRAPOLACAO_MAX_MS) / 1000;

    // onde o servidor "deve estar agora", supondo que a velocidade continua igual
    const alvoX = this.#servidorX + this.#vx * segundos;
    const alvoY = this.#servidorY + this.#vy * segundos;

    if (andando) {
      this.x += this.#vx * dt;
      this.y += this.#vy * dt;
    }

    const erroX = alvoX - this.x;
    const erroY = alvoY - this.y;

    if (Math.hypot(erroX, erroY) > CONFIG.DISTANCIA_TELEPORTE) {
      this.x = alvoX;
      this.y = alvoY;
      return;
    }

    const correcao = 1 - Math.exp((-dt * 1000) / CONFIG.SUAVIZACAO_MS);
    this.x += erroX * correcao;
    this.y += erroY * correcao;
  }
}
