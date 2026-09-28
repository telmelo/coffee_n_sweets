package http;

import dto.BaristasDestaqueDTO;
import dto.FaturamentoDTO;
import dto.TopProdutosDTO;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

/**
 * Gera os gráficos chamando o script Python (python/graficos.py).
 *
 * O Java continua sendo o único a acessar o banco: aqui só serializamos o resultado
 * das consultas em JSON, enviamos ao Python pela entrada padrão (stdin) e o Python
 * grava as imagens PNG em PASTA_SAIDA.
 */
public class GraficosPython {
    public static final Path PASTA_SAIDA = Path.of("graficos_gerados");
    private static final Path SCRIPT = Path.of("python", "graficos.py");

    // No Windows o interpretador costuma ser "python" ou "py"; tentamos os dois
    private static final String[] COMANDOS_PYTHON = {"python", "py"};

    /** Devolve true se os gráficos foram gerados; false se o Python não estiver disponível ou falhar. */
    public static boolean gerar(List<FaturamentoDTO> faturamento,
                                List<TopProdutosDTO> topProdutos,
                                List<BaristasDestaqueDTO> baristas) {
        String json = montarJson(faturamento, topProdutos, baristas);

        for (String comando : COMANDOS_PYTHON) {
            try {
                ProcessBuilder construtor = new ProcessBuilder(comando, SCRIPT.toString(), PASTA_SAIDA.toString());
                construtor.redirectErrorStream(true); // junta erros do Python na mesma saída
                Process processo = construtor.start();

                try (OutputStream entrada = processo.getOutputStream()) {
                    entrada.write(json.getBytes(StandardCharsets.UTF_8));
                }

                String saida = new String(processo.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                int codigo = processo.waitFor();

                if (codigo == 0) {
                    return true;
                }
                // falhou com este comando (ex: atalho da Microsoft Store, ou biblioteca faltando):
                // mostra o motivo e tenta o próximo nome
                System.err.println("Falha ao executar '" + comando + "' (código " + codigo + "):\n" + saida);

            } catch (IOException e) {
                // comando não encontrado neste nome: tenta o próximo
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        System.err.println("Não foi possível gerar os gráficos com Python. Usando os gráficos SVG como alternativa.");
        return false;
    }

    private static String montarJson(List<FaturamentoDTO> faturamento,
                                     List<TopProdutosDTO> topProdutos,
                                     List<BaristasDestaqueDTO> baristas) {
        StringBuilder sb = new StringBuilder("{");

        sb.append("\"faturamento\":[");
        for (int i = 0; i < faturamento.size(); i++) {
            FaturamentoDTO f = faturamento.get(i);
            if (i > 0) sb.append(",");
            sb.append("{\"data\":").append(texto(f.getData().toString()))
                    .append(",\"forma\":").append(texto(f.getFormaPagamento()))
                    .append(",\"valor\":").append(f.getFaturamento()).append("}");
        }

        sb.append("],\"top_produtos\":[");
        for (int i = 0; i < topProdutos.size(); i++) {
            TopProdutosDTO p = topProdutos.get(i);
            if (i > 0) sb.append(",");
            sb.append("{\"nome\":").append(texto(p.getNome()))
                    .append(",\"total\":").append(p.getTotalVendido()).append("}");
        }

        sb.append("],\"baristas\":[");
        for (int i = 0; i < baristas.size(); i++) {
            BaristasDestaqueDTO b = baristas.get(i);
            if (i > 0) sb.append(",");
            sb.append("{\"nome\":").append(texto(b.getNome()))
                    .append(",\"itens\":").append(b.getItensPreparados()).append("}");
        }

        sb.append("]}");
        return sb.toString();
    }

    /** Converte um texto Java em string JSON (com aspas e caracteres especiais escapados). */
    private static String texto(String valor) {
        if (valor == null) return "\"\"";
        StringBuilder sb = new StringBuilder("\"");
        for (char c : valor.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        return sb.append("\"").toString();
    }
}