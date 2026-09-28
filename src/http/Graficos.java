package http;

import java.util.List;
import java.util.function.DoubleFunction;

import static http.Html.escapar;

public class Graficos {
    private static final String COR_BARRA = "#e0457b";
    private static final String COR_TEXTO = "#444";
    private static final String COR_GRADE = "#ddd";

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