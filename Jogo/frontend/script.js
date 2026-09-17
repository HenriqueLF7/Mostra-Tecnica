const player = document.getElementById("player");

let playerX = 100;
let playerY = 100;

const zumbi = document.getElementById("zumbi");

let zumbiX = 500;
let zumbiY = 300;

const teclas = {
  w: false,
  a: false,
  s: false,
  d: false,
};

const telaFim = document.getElementById("telaFim");
const btnRecomecar = document.getElementById("btnRecomecar");

document.addEventListener("keydown", function (event) {
  const tecla = event.key.toLowerCase();

  if (tecla === "w") teclas.w = true;
  if (tecla === "a") teclas.a = true;
  if (tecla === "s") teclas.s = true;
  if (tecla === "d") teclas.d = true;
});

document.addEventListener("keyup", function (event) {
  const tecla = event.key.toLowerCase();

  if (tecla === "w") teclas.w = false;
  if (tecla === "a") teclas.a = false;
  if (tecla === "s") teclas.s = false;
  if (tecla === "d") teclas.d = false;
});

// --- Direção pelo mouse (mira) ---
let mouseX = window.innerWidth / 2;
let mouseY = window.innerHeight / 2;

document.addEventListener("mousemove", function (event) {
  mouseX = event.clientX;
  mouseY = event.clientY;
  atualizarDirecaoPeloMouse();
});

function atualizarDirecaoPeloMouse() {
  // metade da largura/altura do player, baseado nos mesmos valores
  // usados no clamp (90 de largura, 100 de altura)
  const centroX = playerX + 45;
  definirDirecao(mouseX < centroX ? "esquerda" : "direita");
}

document.addEventListener("mousedown", function (event) {
  if (event.button !== 0) return;

  const centroX = playerX + 45;
  const ladoMira = mouseX < centroX ? "esquerda" : "direita";

  player.classList.add("mirando");
  player.classList.toggle("mirando-esquerda", ladoMira === "esquerda");
  player.classList.toggle("mirando-direita", ladoMira === "direita");
});

document.addEventListener("mouseup", function (event) {
  if (event.button !== 0) return;

  player.classList.remove("mirando", "mirando-esquerda", "mirando-direita");
});

const spriteAndando = new Image();
spriteAndando.src = "Imagens/player.png";
const spriteParado = new Image();
spriteParado.src = "Imagens/idle.png";

const FRAME_COLS = 8;
const VELOCIDADE_ANIMACAO = 90;

let frameAtual = 0;
let estadoAtual = null;
let direcaoAtual = null;

function definirEstado(estado) {
  if (estado === estadoAtual) return;
  estadoAtual = estado;

  player.classList.toggle("idle", estado === "idle");
  player.classList.toggle("andando", estado === "andando");

  if (estado === "idle") {
    frameAtual = 0;
    // a direção NÃO é mais resetada aqui: o personagem continua
    // olhando para onde o mouse está, mesmo parado
  }
}

function definirDirecao(direcao) {
  if (direcao === direcaoAtual) return;
  direcaoAtual = direcao;
  player.style.transform = direcao === "esquerda" ? "scaleX(-1)" : "scaleX(1)";
}

function atualizarFrame() {
  const colPercent = (frameAtual / (FRAME_COLS - 1)) * 100;
  player.style.backgroundPosition = colPercent + "% 0%";
}

setInterval(function () {
  const estaAndando = teclas.w || teclas.a || teclas.s || teclas.d;

  if (estaAndando) {
    definirEstado("andando");
    frameAtual = (frameAtual + 1) % FRAME_COLS;
    atualizarFrame();
  } else {
    definirEstado("idle");
  }
}, VELOCIDADE_ANIMACAO);

// --- Envio de comandos, agora com trava por eixo pra evitar empilhar requisições ---
let enviandoVertical = false;
let enviandoHorizontal = false;

function enviarComando(comando, eixo) {
  const emAndamento =
    eixo === "vertical" ? enviandoVertical : enviandoHorizontal;
  if (emAndamento) return; // já tem uma requisição desse eixo em voo, não manda outra

  if (eixo === "vertical") enviandoVertical = true;
  else enviandoHorizontal = true;

  const corpo = comando + ";" + window.innerWidth + ";" + window.innerHeight;

  fetch("/player", {
    method: "POST",
    body: corpo,
  })
    .then((response) => response.json())
    .then((data) => {
      if (data && typeof data.x === "number" && typeof data.y === "number") {
        const larguraMax = window.innerWidth - 90;
        const alturaMax = window.innerHeight - 100;

        playerX = Math.max(0, Math.min(data.x, larguraMax));
        playerY = Math.max(0, Math.min(data.y, alturaMax));

        atualizarTela();
      }
    })
    .catch((erro) => console.error("Erro ao enviar comando:", erro))
    .finally(() => {
      if (eixo === "vertical") enviandoVertical = false;
      else enviandoHorizontal = false;
    });
}

function atualizarTela() {
  player.style.left = playerX + "px";
  player.style.top = playerY + "px";

  zumbi.style.left = zumbiX + "px";
  zumbi.style.top = zumbiY + "px";

  atualizarDirecaoPeloMouse();
}

// --- Busca de estado, também com trava para não empilhar ---
let buscandoEstado = false;

setInterval(function () {
  if (buscandoEstado) return;
  buscandoEstado = true;

  fetch("/player")
    .then((response) => response.json())
    .then((data) => {
      if (data && typeof data.x === "number" && typeof data.y === "number") {
        playerX = data.x;
        playerY = data.y;
      }

      if (
        data &&
        typeof data.zumbiX === "number" &&
        typeof data.zumbiY === "number"
      ) {
        zumbiX = data.zumbiX;
        zumbiY = data.zumbiY;
      }

      atualizarTela();
    })
    .catch((erro) => console.error("Erro ao buscar estado:", erro))
    .finally(() => {
      buscandoEstado = false;
    });
}, 50);

setInterval(function () {
  if (teclas.w) {
    enviarComando("W", "vertical");
  } else if (teclas.s) {
    enviarComando("S", "vertical");
  }

  if (teclas.a) {
    enviarComando("A", "horizontal");
  } else if (teclas.d) {
    enviarComando("D", "horizontal");
  }
}, 50);

// =============================
// CRONÔMETRO
// =============================

let tempo = 0;
let multiplicadorTempo = 1;

const cronometro = document.getElementById("cronometro");
const btnTempo5x = document.getElementById("btnTempo5x");

// =============================
// BOTÃO 5X
// =============================

btnTempo5x.addEventListener("click", function () {
  if (multiplicadorTempo === 1) {
    multiplicadorTempo = 5;

    btnTempo5x.textContent = "5X ATIVO";
    btnTempo5x.classList.add("ativo");
  } else {
    multiplicadorTempo = 1;

    btnTempo5x.textContent = "5X TEMPO";
    btnTempo5x.classList.remove("ativo");
  }
});

// =============================
// CRONÔMETRO
// =============================

const intervaloCronometro = setInterval(function () {
  // Aumenta o tempo
  tempo += multiplicadorTempo;

  // Impede passar de 180
  if (tempo > 180) {
    tempo = 180;
  }

  // Calcula minutos
  const minutos = Math.floor(tempo / 60);

  // Calcula segundos
  const segundos = tempo % 60;

  // Formata para 00:00
  const minutosFormatados = String(minutos).padStart(2, "0");

  const segundosFormatados = String(segundos).padStart(2, "0");

  // Mostra na tela
  cronometro.textContent = minutosFormatados + ":" + segundosFormatados;

  // VERDE

  if (tempo < 90) {
    cronometro.style.color = "#00d92e";
    cronometro.style.boxShadow =
      "0 0 0 2px #006807, 0 0 0 5px #000000, inset 3px 3px 0 #06411a, inset -3px -3px 0 #050505";
    cronometro.style.setProperty("--cor-alerta", "#006807");
    cronometro.style.boxShadow.opacity = 0.5;
  }

  // AMARELO

  if (tempo >= 35) {
    cronometro.style.color = "#ffb020";
    cronometro.style.boxShadow =
      "0 0 0 2px #ffb020, 0 0 0 5px #000000, inset 3px 3px 0 #7a4a00, inset -3px -3px 0 #050505";
    cronometro.style.setProperty("--cor-alerta", "#ffb020");
    cronometro.style.boxShadow.opacity = 0.5;
  }

  // VERMELHO

  if (tempo >= 70) {
    cronometro.style.color = "#ff2d2d";
    cronometro.style.boxShadow =
      "0 0 0 2px #ff2d2d, 0 0 0 5px #000000, inset 3px 3px 0 #4a0000, inset -3px -3px 0 #050505";
    cronometro.style.setProperty("--cor-alerta", "#ff2d2d");
    cronometro.style.boxShadow.opacity = 0.5;
  }

  // FIM!

  if (tempo >= 80) {
    cronometro.style.animation = "tremor 1s infinite";
  }

  if (tempo >= 0) {
    clearInterval(intervaloCronometro);

    cronometro.textContent = "TEMPO ESGOTADO!";

    cronometro.style.zIndex = "10001";

    cronometro.style.display = "none";

    telaFim.style.transition = "0.50s";

    telaFim.style.display = "flex";

    cronometro.style.color = "red";

    cronometro.style.transition = "0.50s";

    btnTempo5x.disabled = true;
  }
}, 1000);

btnRecomecar.addEventListener("click", function () {
  location.reload();
});

// =============================
// IR PARA TELA INICIAL
// =============================

function paraTelaInicial() {
  telaFim.style.display = "none";
  telaInicial.style.display = "flex";
}

// =============================
// POSIÇÃO INICIAL
// =============================

atualizarTela();
