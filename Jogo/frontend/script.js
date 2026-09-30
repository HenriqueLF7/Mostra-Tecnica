const player = document.getElementById("player");


let jogando = false;
const telaInicial = document.getElementById("telaInicial");

let playerX = 100;
let playerY = 100;

// --- Zumbis (vários, criados conforme o servidor manda) ---
const gameEl = document.getElementById("game");
const FRAMES_ZUMBI = 8;

let zumbisEstado = [];      // [{x, y}, ...] vindo do servidor
const elementosZumbi = [];  // divs já criados
let frameZumbi = 0;

function renderizarZumbis() {
  while (elementosZumbi.length < zumbisEstado.length) {
    const el = document.createElement("div");
    el.className = "zumbi";
    gameEl.appendChild(el);
    elementosZumbi.push(el);
  }

  const centroPlayer = playerX + 45;

  elementosZumbi.forEach((el, i) => {
    const z = zumbisEstado[i];

    if (!z) {
      el.style.display = "none";
      return;
    }

    el.style.display = "block";
    el.style.left = z.x + "px";
    el.style.top = z.y + "px";
    el.style.transform = z.x + 28 > centroPlayer ? "scaleX(-1)" : "scaleX(1)";

    // cada zumbi com um frame diferente, pra não andarem todos sincronizados
    const f = (frameZumbi + i) % FRAMES_ZUMBI;
    el.style.backgroundPosition = (f / (FRAMES_ZUMBI - 1)) * 100 + "% 0%";
  });
}

setInterval(function () {
  if (!jogando) return;
  frameZumbi = (frameZumbi + 1) % FRAMES_ZUMBI;
}, 100);

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
  if (!jogando) return; 
  if (event.button !== 0) return;

  const centroX = playerX + 45;
  const ladoMira = mouseX < centroX ? "esquerda" : "direita";

  player.classList.add("mirando");
  player.classList.toggle("mirando-esquerda", ladoMira === "esquerda");
  player.classList.toggle("mirando-direita", ladoMira === "direita");
  atirar(ladoMira);
});

document.addEventListener("mouseup", function (event) {
  if (event.button !== 0) return;

  player.classList.remove("mirando", "mirando-esquerda", "mirando-direita");
});

const spriteAndando = new Image();
spriteAndando.src = "Imagens/player.png";
const spriteParado = new Image();
spriteParado.src = "Imagens/idle.png";

// --- Escolha de gênero ---
const spriteAndandoFem = new Image();
spriteAndandoFem.src = "Imagens/player_fem.png";
const spriteParadoFem = new Image();
spriteParadoFem.src = "Imagens/idle_fem.png";

let generoEscolhido = "masculino";

const cardMasculino = document.querySelector(".masculino");
const cardFeminino = document.querySelector(".feminino");

function escolherGenero(genero) {
  generoEscolhido = genero;

  player.classList.toggle("skin-fem", genero === "feminino");
  cardMasculino.classList.toggle("selecionado", genero === "masculino");
  cardFeminino.classList.toggle("selecionado", genero === "feminino");
}

cardMasculino.addEventListener("click", () => escolherGenero("masculino"));
cardFeminino.addEventListener("click", () => escolherGenero("feminino"));

// começa com o masculino marcado
escolherGenero("masculino");

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

// --- Tiro ---
let enviandoTiro = false;

function atirar(direcao) {
  if (enviandoTiro) return;

  enviandoTiro = true;

  const corpo =
    direcao + ";" + window.innerWidth + ";" + window.innerHeight +
    ";" + Math.round(mouseX) + ";" + Math.round(mouseY);

  fetch("/tiro", {
    method: "POST",
    body: corpo,
  })
    .catch((erro) => console.error("Erro ao atirar:", erro))
    .finally(() => {
      enviandoTiro = false;
    });
}

function atualizarTiros(listaTiros) {
  document.querySelectorAll(".tiro").forEach((el) => el.remove());

  if (!Array.isArray(listaTiros)) return;

    listaTiros.forEach((t) => {
    const elemento = document.createElement("div");
    elemento.className = "tiro";
    elemento.style.left = t.x + "px";
    elemento.style.top = t.y + "px";
    elemento.style.transform = "rotate(" + (t.angulo || 0) + "deg)";
    document.getElementById("game").appendChild(elemento);
  });
}

function atualizarTela() {
  player.style.left = playerX + "px";
  player.style.top = playerY + "px";

  atualizarDirecaoPeloMouse();
  renderizarZumbis();
}

function atualizarVida(pontos) {
  document.querySelectorAll("#hud-vida .vida").forEach((bolinha, i) => {
    bolinha.classList.toggle("perdida", i >= pontos);
  });
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

      if (data && Array.isArray(data.zumbis)) {
        zumbisEstado = data.zumbis;
      }

      atualizarTiros(data.tiros);
      atualizarVida(data.pontosVida);

      document.getElementById("hud-onda").textContent =
        "ONDA " + Math.max(1, data.onda);

      if (jogando && data.pontosVida <= 0) {
        fimDeJogo();
      }

      atualizarTela();
    })
    .catch((erro) => console.error("Erro ao buscar estado:", erro))
    .finally(() => {
      buscandoEstado = false;
    });
}, 50);

setInterval(function () {
  if (!jogando) return; 
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
let intervaloCronometro = null;

const cronometro = document.getElementById("cronometro");
const btnTempo5x = document.getElementById("btnTempo5x");

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

function aplicarCorCronometro(cor, sombraInterna) {
  cronometro.style.color = cor;
  cronometro.style.boxShadow =
    "0 0 0 2px " + cor + ", 0 0 0 5px #000000, inset 3px 3px 0 " +
    sombraInterna + ", inset -3px -3px 0 #050505";
  cronometro.style.setProperty("--cor-alerta", cor);
}

function tickCronometro() {
  tempo += multiplicadorTempo;
  if (tempo > 90) tempo = 90;

  const minutos = String(Math.floor(tempo / 60)).padStart(2, "0");
  const segundos = String(tempo % 60).padStart(2, "0");
  cronometro.textContent = minutos + ":" + segundos;

  if (tempo >= 70) {
    aplicarCorCronometro("#ff2d2d", "#4a0000");
  } else if (tempo >= 35) {
    aplicarCorCronometro("#ffb020", "#7a4a00");
  } else {
    aplicarCorCronometro("#42d624", "#42d624");
  }

  if (tempo >= 80) {
    cronometro.style.animation = "tremor 1s infinite";
  }

  if (tempo >= 90) {
    fimDeJogo();
  }
}

// =============================
// INÍCIO E FIM DO JOGO
// =============================

function fimDeJogo() {
  if (!jogando) return;
  jogando = false;

  clearInterval(intervaloCronometro);
  fetch("/parar", { method: "POST" });   // servidor congela zumbi e player

  teclas.w = teclas.a = teclas.s = teclas.d = false;

  cronometro.style.display = "none";
  btnTempo5x.disabled = true;
  telaFim.style.display = "flex";
}

async function iniciarJogo() {
  // servidor: player no centro, zumbi longe, kills e vidas zerados
  await fetch("/reiniciar", {
    method: "POST",
    body: window.innerWidth + ";" + window.innerHeight,
  });
  await fetch("/iniciar", { method: "POST" });

  // cliente: zera tudo
  tempo = 0;
  multiplicadorTempo = 1;
  btnTempo5x.textContent = "5X TEMPO";
  btnTempo5x.classList.remove("ativo");
  btnTempo5x.disabled = false;

  cronometro.textContent = "00:00";
  cronometro.style.animation = "none";
  cronometro.style.display = "block";
  aplicarCorCronometro("#42d624", "#42d624");

  telaInicial.style.display = "none";
  telaFim.style.display = "none";

  jogando = true;

  clearInterval(intervaloCronometro);
  intervaloCronometro = setInterval(tickCronometro, 1000);
}

btnRecomecar.addEventListener("click", iniciarJogo);

function paraTelaInicial() {
  telaFim.style.display = "none";
  telaInicial.style.display = "flex";
}

// =============================
// AO CARREGAR A PÁGINA
// =============================

// mostra a tela inicial e deixa o servidor pausado, com tudo no lugar
telaInicial.style.display = "flex";
cronometro.style.display = "none";

fetch("/reiniciar", {
  method: "POST",
  body: window.innerWidth + ";" + window.innerHeight,
});

atualizarTela();