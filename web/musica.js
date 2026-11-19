const CHAVE = "estado-musica";
const arquivo = "musica1.mp3";

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

// retoma a posição salva e, se estava tocando, continua
audio.addEventListener("loadedmetadata", () => {
    if (mesmaMusica && salvo.tempo) audio.currentTime = salvo.tempo;

    if (salvo.tocando !== false) {
        audio.play().catch(() => {
            document.addEventListener("click", () => audio.play(), { once: true });
        });
    }
});

setInterval(salvarEstado, 500);
window.addEventListener("pagehide", salvarEstado);

window.alternarMusica = () => {
    if (audio.paused) {
        audio.play().catch((e) => console.error("Erro ao tocar:", e));
    } else {
        audio.pause();
    }
    salvarEstado();
};

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