package http;

import java.util.List;
import java.util.Locale;
import java.util.function.DoubleFunction;

import static http.Html.escapar;

public class Graficos {
    private static final String COR_BARRA = "#e0457b";
    private static final String COR_TEXTO = "#444";
    private static final String COR_GRADE = "#ddd";
    private static final String COR_FUNDO = "#fff";

    // tons derivados de COR_BARRA, do mais escuro ao mais claro, para fatias de pizza
    private static final String COR_FATIA_1 = "#8a1c47";
    private static final String COR_FATIA_2 = COR_BARRA;
    private static final String COR_FATIA_3 = "#f08cb0";
    private static final String COR_FATIA_4 = "#f9d3e1";
    private static final String[] PALETA_PIZZA = {COR_FATIA_1, COR_FATIA_2, COR_FATIA_3, COR_FATIA_4};

    private static final Locale BR = Locale.forLanguageTag("pt-BR");

    public static String barrasHorizontais(List<String> rotulos, List<Double> valores, DoubleFunction<String> formato) {
        if (rotulos.isEmpty()) return "<p>Sem dados para exibir.</p>";

        int largura = 640, margemEsq = 190, margemDir = 90, alturaLinha = 30;
        int altura = rotulos.size() * alturaLinha + 10;
        int areaUtil = largura - margemEsq - margemDir;
        double max = maximo(valores);

        StringBuilder sb = abrirSvg(largura, altura);
        for (int i = 0; i < rotulos.size(); i++) {
            int y = i * alturaLinha + 5;
            double valor = valores.get(i);
            int larguraBarra = (int) Math.round(valor / max * areaUtil);

            sb.append("<text x=\"").append(margemEsq - 8).append("\" y=\"").append(y + 16)
                    .append("\" text-anchor=\"end\" font-size=\"13\" fill=\"").append(COR_TEXTO).append("\">")
                    .append(escapar(cortar(rotulos.get(i), 26))).append("</text>");
            sb.append("<rect x=\"").append(margemEsq).append("\" y=\"").append(y)
                    .append("\" width=\"").append(larguraBarra).append("\" height=\"22\" rx=\"3\" fill=\"")
                    .append(COR_BARRA).append("\"><title>").append(escapar(rotulos.get(i))).append(": ")
                    .append(escapar(formato.apply(valor))).append("</title></rect>");
            sb.append("<text x=\"").append(margemEsq + larguraBarra + 6).append("\" y=\"").append(y + 16)
                    .append("\" font-size=\"13\" fill=\"").append(COR_TEXTO).append("\">")
                    .append(escapar(formato.apply(valor))).append("</text>");
        }
        sb.append("</svg>");
        return sb.toString();
    }

    public static String barrasVerticais(List<String> rotulos, List<Double> valores, DoubleFunction<String> formato) {
        if (rotulos.isEmpty()) return "<p>Sem dados para exibir.</p>";

        int largura = 520, altura = 280, margemLateral = 20, margemBase = 40, margemTopo = 30;
        int n = rotulos.size();
        int passo = (largura - 2 * margemLateral) / n;
        int larguraBarra = passo * 6 / 10;
        int alturaUtil = altura - margemBase - margemTopo;
        double max = maximo(valores);

        StringBuilder sb = abrirSvg(largura, altura);
        sb.append("<line x1=\"").append(margemLateral).append("\" y1=\"").append(altura - margemBase)
                .append("\" x2=\"").append(largura - margemLateral).append("\" y2=\"").append(altura - margemBase)
                .append("\" stroke=\"").append(COR_GRADE).append("\"/>");

        for (int i = 0; i < n; i++) {
            double valor = valores.get(i);
            int alturaBarra = (int) Math.round(valor / max * alturaUtil);
            int x = margemLateral + i * passo + (passo - larguraBarra) / 2;
            int y = altura - margemBase - alturaBarra;
            int centro = x + larguraBarra / 2;

            sb.append("<rect x=\"").append(x).append("\" y=\"").append(y).append("\" width=\"").append(larguraBarra)
                    .append("\" height=\"").append(alturaBarra).append("\" rx=\"3\" fill=\"").append(COR_BARRA).append("\"/>");
            sb.append("<text x=\"").append(centro).append("\" y=\"").append(y - 6)
                    .append("\" text-anchor=\"middle\" font-size=\"13\" fill=\"").append(COR_TEXTO).append("\">")
                    .append(escapar(formato.apply(valor))).append("</text>");
            sb.append("<text x=\"").append(centro).append("\" y=\"").append(altura - margemBase + 20)
                    .append("\" text-anchor=\"middle\" font-size=\"13\" fill=\"").append(COR_TEXTO).append("\">")
                    .append(escapar(rotulos.get(i))).append("</text>");
        }
        sb.append("</svg>");
        return sb.toString();
    }

    public static String linha(List<String> rotulos, List<Double> valores, DoubleFunction<String> formato) {
        if (rotulos.isEmpty()) return "<p>Sem dados para exibir.</p>";

        int largura = 700, altura = 300, margemEsq = 90, margemDir = 20, margemTopo = 20, margemBase = 40;
        int n = rotulos.size();
        int areaX = largura - margemEsq - margemDir;
        int areaY = altura - margemTopo - margemBase;
        double max = maximo(valores);

        StringBuilder sb = abrirSvg(largura, altura);

        for (int k = 0; k <= 2; k++) {
            double fracao = k / 2.0;
            int y = (int) Math.round(altura - margemBase - fracao * areaY);
            sb.append("<line x1=\"").append(margemEsq).append("\" y1=\"").append(y).append("\" x2=\"")
                    .append(largura - margemDir).append("\" y2=\"").append(y).append("\" stroke=\"")
                    .append(COR_GRADE).append("\"/>");
            sb.append("<text x=\"").append(margemEsq - 8).append("\" y=\"").append(y + 4)
                    .append("\" text-anchor=\"end\" font-size=\"12\" fill=\"").append(COR_TEXTO).append("\">")
                    .append(escapar(formato.apply(max * fracao))).append("</text>");
        }

        StringBuilder pontos = new StringBuilder();
        StringBuilder circulos = new StringBuilder();
        int saltoRotulo = Math.max(1, (int) Math.ceil(n / 8.0));

        for (int i = 0; i < n; i++) {
            int x = (n == 1) ? margemEsq + areaX / 2 : margemEsq + i * areaX / (n - 1);
            int y = (int) Math.round(altura - margemBase - valores.get(i) / max * areaY);
            pontos.append(x).append(",").append(y).append(" ");

            circulos.append("<circle cx=\"").append(x).append("\" cy=\"").append(y)
                    .append("\" r=\"4\" fill=\"").append(COR_BARRA).append("\"><title>")
                    .append(escapar(rotulos.get(i))).append(": ").append(escapar(formato.apply(valores.get(i))))
                    .append("</title></circle>");

            if (i % saltoRotulo == 0) {
                sb.append("<text x=\"").append(x).append("\" y=\"").append(altura - margemBase + 20)
                        .append("\" text-anchor=\"middle\" font-size=\"12\" fill=\"").append(COR_TEXTO).append("\">")
                        .append(escapar(rotulos.get(i))).append("</text>");
            }
        }

        sb.append("<polyline points=\"").append(pontos).append("\" fill=\"none\" stroke=\"").append(COR_BARRA)
                .append("\" stroke-width=\"2\"/>");
        sb.append(circulos);
        sb.append("</svg>");
        return sb.toString();
    }

    public static String pizza(List<String> rotulos, List<Double> valores, DoubleFunction<String> formato) {
        if (rotulos.isEmpty()) return "<p>Sem dados para exibir.</p>";

        double total = valores.stream().mapToDouble(Double::doubleValue).sum();
        if (total <= 0) return "<p>Sem dados para exibir.</p>";

        int raio = 110, cx = 130, cy = 130;
        int legendaX = 290, alturaLinha = 28;
        int largura = 560;
        int altura = Math.max(2 * cy, rotulos.size() * alturaLinha + 20);

        int fatiasNaoVazias = 0;
        for (double v : valores) if (v > 0) fatiasNaoVazias++;

        StringBuilder sb = abrirSvg(largura, altura);
        double anguloInicial = -Math.PI / 2; // começa no topo e segue no sentido horário

        for (int i = 0; i < rotulos.size(); i++) {
            double valor = valores.get(i);
            if (valor <= 0) continue;

            String cor = PALETA_PIZZA[i % PALETA_PIZZA.length];
            String dica = "<title>" + escapar(rotulos.get(i)) + ": " + escapar(formato.apply(valor))
                    + " (" + escapar(percentual(valor, total)) + ")</title>";

            if (fatiasNaoVazias == 1) {
                // um arco de 360° tem início e fim no mesmo ponto e não é desenhado
                sb.append("<circle cx=\"").append(cx).append("\" cy=\"").append(cy).append("\" r=\"").append(raio)
                        .append("\" fill=\"").append(cor).append("\">").append(dica).append("</circle>");
                continue;
            }

            double angulo = valor / total * 2 * Math.PI;
            double anguloFinal = anguloInicial + angulo;
            int arcoGrande = angulo > Math.PI ? 1 : 0;

            sb.append("<path d=\"M ").append(cx).append(" ").append(cy)
                    .append(" L ").append(coordenada(cx + raio * Math.cos(anguloInicial)))
                    .append(" ").append(coordenada(cy + raio * Math.sin(anguloInicial)))
                    .append(" A ").append(raio).append(" ").append(raio).append(" 0 ").append(arcoGrande).append(" 1 ")
                    .append(coordenada(cx + raio * Math.cos(anguloFinal)))
                    .append(" ").append(coordenada(cy + raio * Math.sin(anguloFinal)))
                    .append(" Z\" fill=\"").append(cor).append("\" stroke=\"").append(COR_FUNDO)
                    .append("\" stroke-width=\"2\">").append(dica).append("</path>");

            anguloInicial = anguloFinal;
        }

        int topoLegenda = (altura - rotulos.size() * alturaLinha) / 2;
        for (int i = 0; i < rotulos.size(); i++) {
            double valor = valores.get(i);
            int y = topoLegenda + i * alturaLinha;
            sb.append("<rect x=\"").append(legendaX).append("\" y=\"").append(y + 4)
                    .append("\" width=\"14\" height=\"14\" rx=\"2\" fill=\"")
                    .append(PALETA_PIZZA[i % PALETA_PIZZA.length]).append("\" stroke=\"").append(COR_GRADE).append("\"/>");
            sb.append("<text x=\"").append(legendaX + 22).append("\" y=\"").append(y + 16)
                    .append("\" font-size=\"13\" fill=\"").append(COR_TEXTO).append("\">")
                    .append(escapar(cortar(rotulos.get(i), 20))).append(" — ")
                    .append(escapar(formato.apply(valor))).append(" (")
                    .append(escapar(percentual(valor, total))).append(")</text>");
        }

        sb.append("</svg>");
        return sb.toString();
    }

    static String percentual(double parte, double total) {
        return String.format(BR, "%.1f%%", total == 0 ? 0 : parte / total * 100);
    }

    private static String coordenada(double valor) {
        return String.format(Locale.ROOT, "%.2f", valor);
    }


    private static StringBuilder abrirSvg(int largura, int altura) {
        StringBuilder sb = new StringBuilder();
        sb.append("<svg viewBox=\"0 0 ").append(largura).append(" ").append(altura)
                .append("\" width=\"100%\" style=\"max-width:").append(largura)
                .append("px\" role=\"img\" xmlns=\"http://www.w3.org/2000/svg\">");
        return sb;
    }

    private static double maximo(List<Double> valores) {
        double max = valores.stream().mapToDouble(Double::doubleValue).max().orElse(1);
        return max <= 0 ? 1 : max;
    }

    private static String cortar(String texto, int limite) {
        if (texto == null) return "";
        return texto.length() <= limite ? texto : texto.substring(0, limite - 1) + "…";
    }
}