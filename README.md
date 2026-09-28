# Coffee n' Sweets

Sistema de gerenciamento e automação de pedidos para cafeterias, restaurantes e derivados

A proposta desse repositório é integrar e registrar dados de um banco MySQL com uma interface programada sem o uso de frameworks/ferramentas ORM. O nosso projeto resolve problemas práticos de gerenciamento de pedidos de estabelecimentos comerciais, além de manter registro de dados de fornecedores, mesas, preços etc

## O que o sistema faz

- **Cadastro de clientes** com criação, edição, exclusão e listagem.
- **Cadastro de produtos** com as mesmas operações.
- **Dashboard analítico** com quatro consultas SQL e gráficos:
    - faturamento por dia e forma de pagamento (`GROUP BY` + `SUM`);
    - os 10 produtos mais vendidos (`JOIN` + `SUM` + `ORDER BY` + `LIMIT`);
    - baristas acima da média de produtividade (`JOIN` + `HAVING` com subconsulta);
    - grãos de cada café e seus fornecedores (`JOIN` entre cinco tabelas).
- **Gráficos em Python**: barras, linha e boxplot gerados com matplotlib a partir dos resultados das consultas.
- **Validação em duas frentes**: o formulário confere o formato dos dados antes de enviar, e o banco aplica as próprias regras com `CHECK`, `UNIQUE` e chaves estrangeiras. Quando o banco recusa uma operação, a mensagem chega traduzida na tela em vez de virar uma exceção no console.
- **Integridade referencial em ação**: excluir um cliente remove os telefones dele em cascata, mas é bloqueado se houver pedidos registrados. Excluir uma mesa deixa os pedidos daquela mesa com o campo nulo, sem apagar o histórico de vendas.

**Tecnologias:** Java 21, MySQL 8.0, JDBC (`mysql-connector-j`), HTML e CSS. Python 3 com matplotlib para os gráficos.

## Arquitetura

A aplicação é dividida em camadas, e cada uma tem um assunto só. O handler não escreve SQL, o DAO não escreve HTML, e a view não sabe o que é uma requisição HTTP.

```
Navegador
    │  HTTP
    ▼
Servidor ──► Handler ──┬──► DAO ──► Conexao ──► MySQL
                       │
                       └──► View ──► Layout ──► Resposta ──► Navegador
```

O caminho de uma requisição, do começo ao fim:

1. O navegador pede `/clientes`.
2. O `Servidor` encaminha para o `ClienteHandler`.
3. O handler consulta o `ClienteDAO`, que executa o `SELECT` e devolve uma `List<Cliente>`.
4. A `ClienteView` transforma a lista numa tabela HTML.
5. O `Layout` envolve a tabela na página completa.
6. A `Resposta` escreve os bytes no navegador.

Sobre os objetos que trafegam entre as camadas: as classes de `model` espelham tabelas do banco e servem ao CRUD, enquanto as de `dto` carregam resultados de consultas com `JOIN` e agregações, que não correspondem a nenhuma tabela. O `CafesEGraosDTO`, por exemplo, junta dados de quatro tabelas diferentes.

### Gráficos com Python

Os gráficos do dashboard são desenhados por um script Python, seguindo o que foi utilizado na disciplina de estatística. O Java continua sendo o único a acessar o banco: o Python não tem conexão com o MySQL, ele só recebe os resultados já consultados e desenha as imagens. Assim, todo o SQL permanece em um lugar só (`ConsultasDAO`) e o requisito de enviar SQL diretamente ao banco, sem ORM, continua valendo.

```
DashboardHandler ──► ConsultasDAO ──► MySQL
       │
       └──► GraficosPython ──(JSON via stdin)──► python/graficos.py
                                                        │
                                                        ▼
Navegador ◄── ArquivoEstaticoHandler (/graficos/...) ◄── graficos_gerados/*.png
```

1. O `DashboardHandler` executa as consultas do `ConsultasDAO`.
2. O `GraficosPython` converte os resultados em JSON e chama `python/graficos.py` como um processo do sistema, enviando o JSON pela entrada padrão.
3. O script grava as imagens PNG em `graficos_gerados/`.
4. A `DashboardView` monta a página com tags `<img>` apontando para `/graficos/...`, e o `ArquivoEstaticoHandler` entrega os arquivos ao navegador.

Gráficos gerados: os 10 produtos mais vendidos (barras horizontais), faturamento diário (linha), distribuição do faturamento diário por forma de pagamento (boxplot) e baristas acima da média (barras horizontais).

**Alternativa automática:** se o Python não estiver instalado, se o matplotlib estiver faltando ou se o script falhar, o dashboard exibe gráficos equivalentes em SVG, desenhados diretamente em Java (`Graficos`), e o motivo da falha aparece no console. O sistema continua funcionando sem o Python, só sem as imagens do matplotlib.

## Estrutura do repositório

```
coffee_n_sweets/
├── Scripts/
│   ├── coffee_n_sweets.sql      criação das 17 tabelas e constraints
│   ├── insertions.sql           população do banco (30+ tuplas por tabela)
│   └── consultas.sql            as consultas analíticas do sistema
├── lib/
│   └── mysql-connector-j.jar    driver JDBC
├── python/
│   └── graficos.py              gera os gráficos (matplotlib) a partir de JSON
├── src/
│   ├── main/                    Servidor (ponto de entrada) e Conexao
│   ├── http/                    handlers, views, gráficos e utilitários de HTML
│   ├── view/                    Layout: a moldura comum das páginas
│   ├── util/                    Resposta: escrita da resposta HTTP
│   ├── dao/                     acesso ao banco, onde vive todo o SQL
│   ├── model/                   Cliente e Produto
│   └── dto/                     resultados das consultas analíticas
├── web/                         folha de estilo e recursos estáticos
├── graficos_gerados/            imagens PNG criadas em execução (ignorada pelo Git)
├── config.properties.example    modelo de configuração do banco
└── pom.xml
```

## Como executar

**Pré-requisitos:**

- JDK 21 ou superior
- MySQL 8.0 ou superior rodando localmente
- Python 3.8 ou superior, com a biblioteca matplotlib (necessário para os gráficos em Python; sem ele o dashboard usa a alternativa em SVG)

**1. Crie e popule o banco**

```bash
mysql -u root -p < Scripts/coffee_n_sweets.sql
mysql -u root -p < Scripts/insertions.sql
```

**2. Configure o acesso**

```bash
cp config.properties.example config.properties
```

Abra o arquivo e preencha com o usuário e a senha do seu MySQL. Ele é ignorado pelo Git, então cada pessoa mantém as próprias credenciais.

**3. Registre o driver**

Se estiver usando o Maven, a dependência já está declarada no `pom.xml`. Ao abrir o projeto direto na IDE, adicione `lib/mysql-connector-j.jar` às dependências do módulo (no IntelliJ: File → Project Structure → Modules → Dependencies → `+` → JARs or directories).

**4. Instale as dependências do Python**

Confirme que o Python está instalado:

```bash
python --version
```

Depois instale o matplotlib:

```bash
python -m pip install matplotlib
```

No Windows, se o comando `python` abrir a Microsoft Store ou não for reconhecido, use `py` no lugar (`py --version` e `py -m pip install matplotlib`). Ao instalar o Python pelo site python.org, marque a opção **Add python.exe to PATH**. Depois de instalar, reinicie a IDE para que ela enxergue o PATH atualizado. Nenhum plugin da IDE é necessário: o Java executa o Python como um programa do sistema.

**5. Rode**

Execute a classe `main.Servidor` **a partir da raiz do projeto** (é o comportamento padrão do IntelliJ), porque o caminho `python/graficos.py` é relativo a ela. Depois abra:

```
http://localhost:8080/clientes
http://localhost:8080/produtos
http://localhost:8080/dashboard
```

O servidor fica em execução até ser interrompido pela IDE.

## Solução de problemas

| Sintoma | Causa provável | O que fazer |
|---|---|---|
| Dashboard mostra gráficos em SVG em vez das imagens do matplotlib | O Python não foi encontrado ou o script falhou | Leia a mensagem no console da IDE e siga os itens abaixo |
| `ModuleNotFoundError: No module named 'matplotlib'` no console | matplotlib não instalado no Python usado pelo sistema | Rode `python -m pip install matplotlib` (ou com `py`) |
| Nenhum comando Python encontrado | Python fora do PATH | Instale o Python marcando "Add python.exe to PATH" e reinicie a IDE |
| Script não encontrado (`graficos.py`) | Servidor iniciado fora da raiz do projeto | Configure o diretório de trabalho da execução como a raiz do projeto |
| Não é possível excluir um cliente ou produto | Ele já tem pedidos registrados (integridade referencial) | Comportamento esperado: só é possível excluir cadastros sem vendas |

## Equipe
Coffee N' Sweets é um projeto de código open-source desenvolvido por

- Telmo Melo (telmelo)
- Ian Felipe (AnzinFelipe)
- Thiago Neiva (neivals)
- Rafael Barboza (RafaCapetta)
- Guilherme Rapela Medeiros (Guilherme-Rapela-Medeiros)