package http;

import view.Layout;
import java.nio.file.Files;
import java.nio.file.Path;

public class ViewsView {
    public static String pagina() {
        try {
            String html = Files.readString(Path.of("web", "views.html"));
            String js = Files.readString(Path.of("web", "views.js"));
            return Layout.pagina("Views do Banco", html
                    + "\n<script src=\"https://cdn.jsdelivr.net/npm/chart.js\"></script>"
                    + "\n<script>document.addEventListener('DOMContentLoaded', () => {" + js + "});</script>");
        } catch (Exception e) {
            return Layout.pagina("Views do Banco", "<p>Erro ao carregar a página de views.</p>");
        }
    }
}