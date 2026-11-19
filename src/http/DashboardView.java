package http;

import java.nio.file.Files;
import java.nio.file.Path;
import view.Layout;

public class DashboardView {
    public static String pagina() {
        try {
            String htmlDashboard = Files.readString(Path.of("web", "dashboard.html"));
            String scriptDashboard = Files.readString(Path.of("web", "dashboard.js"));

            String conteudoFinal = htmlDashboard +
                    "\n<script src=\"https://cdn.jsdelivr.net/npm/chart.js\"></script>" +
                    "\n<script>" +
                    "document.addEventListener('DOMContentLoaded', () => {" +
                    scriptDashboard +
                    "});" +
                    "</script>";

            return Layout.pagina("Dashboard Analítico", conteudoFinal);
        } catch (Exception e) {
            return Layout.pagina("Dashboard Analítico", "<p>Erro ao carregar o dashboard.</p>");
        }
    }
}