const CHAVE = "estado-musica";

// música associada a cada página (primeiro segmento da URL)
const MUSICAS = {
    clientes: "musica1.mp3",
    produtos: "musica2.mp3",
    mesas: "musica3.mp3",
    funcionarios: "musica4.mp3",
    procedimentos: "musica5.mp3",
    dashboard: "musica6.mp3",
};

const pagina = location.pathname.split("/").filter(Boolean)[0] || "";
const arquivo = MUSICAS[pagina] || "musica1.mp3";

const salvo = JSON.parse(localStorage.getItem(CHAVE) || "{}");
const mesmaMusica = salvo.musica === arquivo;

const audio = new Audio("/audio/" + arquivo);
audio.loop = true;
audio.volume = 0.3;

function salvarEstado() {
    try {
        localStorage.setItem(
            CHAVE,
            JSON.stringify({
                musica: arquivo,
                tempo: audio.currentTime,
                tocando: !audio.paused,
            })
        );
    } catch (e) {}
}

// retoma a posição só se for a mesma faixa da página anterior
audio.addEventListener("loadedmetadata", () => {
    if (mesmaMusica && salvo.tempo) audio.currentTime = salvo.tempo;

    if (salvo.tocando !== false) {
        audio.play().catch(() => {
            // navegador bloqueou o autoplay: toca no primeiro clique na página
            document.addEventListener("click", () => audio.play(), { once: true });
        });
    }
});

setInterval(salvarEstado, 500);
window.addEventListener("pagehide", salvarEstado);

window.alternarMusica = () => {
    if (audio.paused) audio.play();
    else audio.pause();
    salvarEstado();
};

// ---------- estilo do botão ----------
(function estilizarBotaoMusica() {
    const css = `
    body .topo .botao-musica {
      position: fixed !important;
      right: 1.5rem !important;
      top: 0.75rem !important;
      transform: none !important;
      margin: 0 !important;
      z-index: 1000;
    }
  `;
    const estilo = document.createElement("style");
    estilo.id = "estilo-musica";
    estilo.textContent = css;
    document.head.appendChild(estilo);
})();