package view;

public class Layout {
    public static String pagina(String titulo, String conteudo) {
        return """
                <!doctype html>
                <html lang="pt-br">
                <head>
                  <meta charset="utf-8">
                  <title>%s</title>
                  <link rel="stylesheet" href="/estilo.css">
                </head>
                <body>
                  <nav>
                    <a href="/clientes">Clientes</a>
                    <a href="/produtos">Produtos</a>
                    <a href="/dashboard">Dashboard</a>
                  </nav>
                  <h1>%s</h1>
                  %s
                </body>
                </html>
                """.formatted(titulo, titulo, conteudo);
    }
}