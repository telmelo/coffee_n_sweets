package view;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Layout {
    public static String pagina(String titulo, String conteudo) {
        return """
                <!doctype html>
                <html lang="pt-br">
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <title>%s</title>
                  <style>%s</style>
                  <script>
                    try {
                      if (sessionStorage.getItem("cns-aberta")) document.documentElement.classList.add("ja-aberta");
                      sessionStorage.setItem("cns-aberta", "1");
                    } catch (e) {}
                  </script>
                  <script src="https://cdnjs.cloudflare.com/ajax/libs/gsap/3.12.5/gsap.min.js"></script>
                  <script src="https://cdnjs.cloudflare.com/ajax/libs/gsap/3.12.5/ScrollTrigger.min.js"></script>
                  <script>if (window.gsap) document.documentElement.classList.add("anima");</script>
                </head>
                <body>
                  <header class="topo">
                    <div class="topo-conteudo">
                      <span class="marca"><span class="marca-icone">☕</span> Coffee n' Sweets</span>
                      <nav class="menu">
                        <a href="/clientes">Clientes</a>
                        <a href="/produtos">Produtos</a>
                        <a href="/mesas">Mesas</a>
                        <a href="/funcionarios">Funcionários</a>
                        <a href="/procedimentos">Procedimentos</a>
                        <a href="/dashboard">Dashboard</a>
                        <button type="button" class="botao-musica" onclick="alternarMusica()">Tocar / Pausar</button>
                      </nav>
                    </div>
                  </header>
                  <section class="cabecalho"><h1>%s</h1></section>
                  <main class="conteudo">
                  %s
                  </main>
                  <footer class="rodape">♡ Coffee n' Sweets · feito com Java, MySQL, HTML, CSS e um pouco de açúcar ♡</footer>
                  <script>%s</script>
                  <script>%s</script>
                </body>
                </html>
                """.formatted(titulo, estilo(), titulo, conteudo, musica(), script());
    }

    /** Lê as animações e interações de web/app.js e embute no fim da página. */
    private static String script() {
        try {
            return Files.readString(Path.of("web", "app.js"));
        } catch (IOException e) {
            return "";
        }
    }

    /** Lê o controle da música de web/musica.js e embute na página, antes do app.js. */
    private static String musica() {
        try {
            return Files.readString(Path.of("web", "musica.js"));
        } catch (IOException e) {
            return "";
        }
    }

    /** Lê a folha de estilo de web/estilo.css e embute na página (não há rota para arquivos estáticos). */
    private static String estilo() {
        try {
            return Files.readString(Path.of("web", "estilo.css"));
        } catch (IOException e) {
            return "";
        }
    }
}