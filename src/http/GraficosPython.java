package http;

import dao.DashboardDAO.PedidoLinha;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Chama o script Python que gera o boxplot (único gráfico que não é desenhado pelo Chart.js). */
public class GraficosPython {
    public static final Path PASTA_SAIDA = Path.of("graficos_gerados");
    private static final Path SCRIPT = Path.of("python", "graficos.py");

    private static final String[] COMANDOS_PYTHON = {"python", "python3", "py"};

    /** Gera graficos_gerados/faturamento_boxplot.png com os pedidos recebidos. Devolve false se o Python falhar. */
    public static synchronized boolean gerarBoxplot(List<PedidoLinha> pedidos) {
        String json = Json.escrever(Json.obj("faturamento", faturamentoDiarioPorForma(pedidos)));

        for (String comando : COMANDOS_PYTHON) {
            try {
                ProcessBuilder construtor = new ProcessBuilder(comando, SCRIPT.toString(), PASTA_SAIDA.toString());
                construtor.redirectErrorStream(true);
                Process processo = construtor.start();

                try (OutputStream entrada = processo.getOutputStream()) {
                    entrada.write(json.getBytes(StandardCharsets.UTF_8));
                }

                String saida = new String(processo.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                int codigo = processo.waitFor();

                if (codigo == 0) {
                    return true;
                }

                System.err.println("Falha ao executar '" + comando + "' (código " + codigo + "):\n" + saida);

            } catch (IOException e) {
                // comando não encontrado neste sistema: tenta o próximo
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        System.err.println("Não foi possível gerar o boxplot: Python (com matplotlib) não encontrado.");
        return false;
    }

    // soma o valor dos pedidos de cada (dia, forma de pagamento): é o "faturamento diário" do boxplot
    private static List<Object> faturamentoDiarioPorForma(List<PedidoLinha> pedidos) {
        Map<String, Double> somas = new TreeMap<>();
        for (PedidoLinha p : pedidos) {
            somas.merge(p.data() + "|" + p.forma(), p.valor(), Double::sum);
        }
        List<Object> linhas = new ArrayList<>();
        for (Map.Entry<String, Double> e : somas.entrySet()) {
            String[] chave = e.getKey().split("\\|");
            linhas.add(Json.obj("data", chave[0], "forma", chave[1], "valor", e.getValue()));
        }
        return linhas;
    }
}