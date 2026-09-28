package http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import util.Resposta;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Serve arquivos de uma pasta do disco (ex: as imagens PNG geradas pelo Python).
 * Exemplo: new ArquivoEstaticoHandler("/graficos", Path.of("graficos_gerados"))
 * responde GET /graficos/top_produtos.png com o arquivo graficos_gerados/top_produtos.png
 */
public class ArquivoEstaticoHandler implements HttpHandler {
    private final String prefixo;
    private final Path pasta;

    public ArquivoEstaticoHandler(String prefixo, Path pasta) {
        this.prefixo = prefixo;
        this.pasta = pasta.toAbsolutePath().normalize();
    }

    @Override
    public void handle(HttpExchange troca) throws IOException {
        if (!troca.getRequestMethod().equals("GET")) {
            Resposta.naoEncontrado(troca);
            return;
        }

        String caminho = troca.getRequestURI().getPath();
        String nomeArquivo = caminho.substring(prefixo.length()).replaceFirst("^/", "");
        Path arquivo = pasta.resolve(nomeArquivo).normalize();

        // segurança: impede acessar arquivos fora da pasta (ex: /graficos/../../senha.txt)
        if (!arquivo.startsWith(pasta) || !Files.isRegularFile(arquivo)) {
            Resposta.naoEncontrado(troca);
            return;
        }

        byte[] conteudo = Files.readAllBytes(arquivo);
        troca.getResponseHeaders().set("Content-Type", tipoConteudo(nomeArquivo));
        troca.getResponseHeaders().set("Cache-Control", "no-store"); // sempre pega a versão mais nova
        troca.sendResponseHeaders(200, conteudo.length);
        try (OutputStream saida = troca.getResponseBody()) {
            saida.write(conteudo);
        }
    }

    private String tipoConteudo(String nomeArquivo) {
        String nome = nomeArquivo.toLowerCase();
        if (nome.endsWith(".png")) return "image/png";
        if (nome.endsWith(".css")) return "text/css; charset=utf-8";
        return "application/octet-stream";
    }
}