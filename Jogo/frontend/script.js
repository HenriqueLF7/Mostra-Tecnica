const player = document.getElementById("player");

let jogando = false;
const telaInicial = document.getElementById("telaInicial");

let playerX = 100;
let playerY = 100;

// --- Zumbis (vários, criados conforme o servidor manda) ---
const gameEl = document.getElementById("game");
const FRAMES_ZUMBI = 8;

let zumbisEstado = []; // [{x, y}, ...] vindo do servidor
const elementosZumbi = []; // divs já criados
let frameZumbi = 0;

// --- Constante da Ranking ( Puxado do HTML ) ---
const rankingBody = document.getElementById("rankingBody");

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
const spriteAtirando = new Image();
spriteAtirando.src = "Imagens/firehomem.png";

// --- Animação do tiro (firehomem.png, 40ms por frame) ---
const FRAMES_TIRO = 2; // firehomem.png: tiro pra frente + recuo da arma
// cada quadro mede 925x674 px; com 100px de altura (altura do player) fica com ~137px de largura
const ALTURA_PLAYER = 100;
const LARGURA_PLAYER = 90;
const LARGURA_QUADRO_TIRO = Math.round((ALTURA_PLAYER * 925) / 674);
// quando o player olha pra esquerda (espelhado), desloca o elemento para o personagem não "pular"
const DESLOCAMENTO_ESQUERDA = LARGURA_QUADRO_TIRO - LARGURA_PLAYER - 1;
const DURACAO_TIRO = 500; // a animação inteira dura meio segundo
const VELOCIDADE_TIRO = DURACAO_TIRO / FRAMES_TIRO; // tempo de cada quadro (250ms com 2 quadros)

let atirandoAnimacao = false;
let frameTiro = 0;
let intervaloTiro = null;

function desenharFrameTiro() {
  const colPercent = FRAMES_TIRO > 1 ? (frameTiro / (FRAMES_TIRO - 1)) * 100 : 0;
  player.style.backgroundPosition = colPercent + "% 0%";
}

let timeoutTiro = null; // (não usado mais: a animação termina sozinha após o último quadro)

function iniciarAnimacaoTiro() {
  atirandoAnimacao = true;
  frameTiro = 0;

  // se atirar de novo durante a animação, ela recomeça do primeiro quadro
  player.style.backgroundImage = 'url("Imagens/firehomem.png")';
  player.style.width = LARGURA_QUADRO_TIRO + "px";
  player.style.backgroundSize = FRAMES_TIRO * 100 + "% 100%";
  player.style.imageRendering = "pixelated";
  desenharFrameTiro();

  clearInterval(intervaloTiro);
  // toca os quadros UMA vez (40ms cada) e termina, sem loop
  intervaloTiro = setInterval(function () {
    frameTiro++;
    if (frameTiro >= FRAMES_TIRO) {
      pararAnimacaoTiro();
      return;
    }
    desenharFrameTiro();
  }, VELOCIDADE_TIRO);
}

function pararAnimacaoTiro() {
  if (!atirandoAnimacao) return;
  atirandoAnimacao = false;
  clearInterval(intervaloTiro);
  clearTimeout(timeoutTiro);

  // remove os estilos inline para voltar ao sprite de idle/andando do CSS
  player.style.backgroundImage = "";
  player.style.backgroundSize = "";
  player.style.width = "";
  player.style.imageRendering = "";
  atualizarFrame();
}

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

  // enquanto atira, a animação do tiro (40ms) controla o sprite
  if (atirandoAnimacao) return;

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
    direcao +
    ";" +
    window.innerWidth +
    ";" +
    window.innerHeight +
    ";" +
    Math.round(mouseX) +
    ";" +
    Math.round(mouseY);

  fetch("/tiro", {
    method: "POST",
    body: corpo,
  })
    .then((response) => {
      // só anima quando o servidor realmente soltou o tiro (bolinha)
      if (response.ok) iniciarAnimacaoTiro();
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
  const deslocamento =
    atirandoAnimacao && direcaoAtual === "esquerda" ? DESLOCAMENTO_ESQUERDA : 0;
  player.style.left = playerX - deslocamento + "px";
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

function aplicarCorCronometro(cor, sombraInterna) {
  cronometro.style.color = cor;
  cronometro.style.boxShadow =
    "0 0 0 2px " +
    cor +
    ", 0 0 0 5px #000000, inset 3px 3px 0 " +
    sombraInterna +
    ", inset -3px -3px 0 #050505";
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
    aplicarCorCronometro("#32dd10", "#23af07");
  }

  if (tempo >= 80) {
    cronometro.style.animation = "tremor 1s infinite";
  }

  if (tempo >= 90) {
    fimDeJogo();
  }
}

// =============================
// FUNÇAO DE ATUALIZAR RANKING
// =============================

async function atualizarRanking() {
  try {
    const resposta = await fetch("/ranking");

    const ranking = await resposta.json();

    rankingBody.innerHTML = "";

    ranking.forEach((jogador, index) => {
      const linha = document.createElement("tr");

      const minutos = Math.floor(jogador.tempo / 60);

      const segundos = jogador.tempo % 60;

      const tempoFormatado =
        String(minutos).padStart(2, "0") +
        ":" +
        String(segundos).padStart(2, "0");

      linha.innerHTML = `
                <td>${index + 1}</td>
                <td>${jogador.nome}</td>
                <td>${jogador.kills}</td>
                <td>${tempoFormatado}</td>
            `;

      rankingBody.appendChild(linha);
    });
  } catch (erro) {
    console.error("Erro ao carregar ranking:", erro);
  }
}

// =============================
// INÍCIO E FIM DO JOGO
// =============================

async function fimDeJogo() {
  if (!jogando) return;

  jogando = false;

  clearInterval(intervaloCronometro);

  teclas.w = teclas.a = teclas.s = teclas.d = false;

  cronometro.style.display = "none";

  try {
    const resposta = await fetch("/parar", {
      method: "POST",
    });

    const resultado = await resposta.json();

    console.log("Resultado da partida:", resultado);

    // Mostra o resultado na tela de Game Over
    // Troque os IDs abaixo pelos IDs dos seus elementos,
    // caso eles sejam diferentes.
    document.getElementById("resultadoKills").textContent = resultado.kills;

    const minutos = Math.floor(resultado.tempo / 60);
    const segundos = resultado.tempo % 60;

    document.getElementById("resultadoTempo").textContent =
      String(minutos).padStart(2, "0") +
      ":" +
      String(segundos).padStart(2, "0");
  } catch (erro) {
    console.error("Erro ao obter resultado da partida:", erro);
  }

  telaFim.style.display = "flex";
}

async function iniciarJogo() {
  // Pega o nome digitado pelo jogador
  const nome = document.getElementById("nome").value.trim();

  // Servidor: player no centro, zumbi longe, kills e vidas zerados
  await fetch("/reiniciar", {
    method: "POST",
    body: window.innerWidth + ";" + window.innerHeight,
  });

  // Envia o nome para o servidor
  await fetch("/iniciar", {
    method: "POST",
    body: nome || "Jogador",
  });

  // cliente: zera tudo
  tempo = 0;
  multiplicadorTempo = 1;

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

  atualizarRanking();
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

atualizarRanking();
atualizarTela();