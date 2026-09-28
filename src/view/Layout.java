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
                </head>
                <body>
                  <header class="topo">
                    <div class="topo-conteudo">
                      <span class="marca"><span class="marca-icone">☕</span> Coffee n' Sweets</span>
                      <nav class="menu">
                        <a href="/clientes">Clientes</a>
                        <a href="/produtos">Produtos</a>
                        <a href="/dashboard">Dashboard</a>
                      </nav>
                    </div>
                  </header>
                  <section class="cabecalho"><h1>%s</h1></section>
                  <main class="conteudo">
                  %s
                  </main>
                  <footer class="rodape">♡ Coffee n' Sweets · feito com Java, MySQL, HTML, CSS e um pouco de açúcar ♡</footer>
                </body>
                </html>
                """.formatted(titulo, estilo(), titulo, conteudo);
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
