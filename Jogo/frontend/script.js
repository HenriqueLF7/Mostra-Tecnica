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

function enviarComando(comando) {

    const corpo = comando + ";" + window.innerWidth + ";" + window.innerHeight;

    fetch("/player", {

        method: "POST",
        body: corpo

    })

    .then(response => response.json())

    .then((data) => {
      if (data && typeof data.x === "number" && typeof data.y === "number") {
        const larguraMax = window.innerWidth - 50;
        const alturaMax = window.innerHeight - 70;

        playerX = Math.max(0, Math.min(data.x, larguraMax));
        playerY = Math.max(0, Math.min(data.y, alturaMax));

        atualizarTela();
      }
    });
}

function atualizarTela() {
  player.style.left = playerX + "px";
  player.style.top = playerY + "px";

  zumbi.style.left = zumbiX + "px";
  zumbi.style.top = zumbiY + "px";
}

setInterval(function () {
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
    });
}, 50);

setInterval(function () {
  if (teclas.w) {
    enviarComando("W");
  } else if (teclas.s) {
    enviarComando("S");
  }

  if (teclas.a) {
    enviarComando("A");
  } else if (teclas.d) {
    enviarComando("D");
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

  // =============================
  // 2 MINUTOS
  // =============================

  if (tempo >= 120) {
    cronometro.style.color = "yellow";

    cronometro.style.border = "1px solid yellow";

    cronometro.style.transition = "0.50s";

    cronometro.style.boxShadow = "inset 0 3px 20px rgba(230, 226, 2, 0.747)";
  }

  // =============================
  // 2:30
  // =============================

  if (tempo >= 150) {
    cronometro.style.color = "red";

    cronometro.style.border = "1px solid red";

    cronometro.style.transition = "0.50s";

    cronometro.style.boxShadow = "inset 0 3px 20px rgba(230, 2, 2, 0.75)";
  }

  // =============================
  // 3 MINUTOS
  // =============================

  if (tempo >= 180) {
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
// POSIÇÃO INICIAL
// =============================

atualizarTela();
