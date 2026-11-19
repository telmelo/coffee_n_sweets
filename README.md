# Coffee n' Sweets

Sistema de gerenciamento e automação de pedidos para cafeterias, restaurantes e derivados

A proposta desse repositório é integrar e registrar dados de um banco MySQL com uma interface programada sem o uso de frameworks/ferramentas ORM. O nosso projeto resolve problemas práticos de gerenciamento de pedidos de estabelecimentos comerciais, além de manter registro de dados de fornecedores, mesas, preços etc

## O que o sistema faz

- **CRUD de quatro cadastros**: clientes, produtos, mesas e funcionários, todos com criação, edição, exclusão e listagem. Os pedidos e seus itens também têm tela própria.
- **Procedimentos, funções e triggers na interface**: a página `/procedimentos` executa as procedures do banco e mostra o efeito delas, e os triggers aparecem nos resultados das outras telas (veja a seção [Integração com o banco](#integração-com-o-banco)).
- **Views com filtros** (`/views`): as duas views do banco, cada uma com indicadores, gráficos e tabela:
    - `vw_vendas_detalhadas`, filtrável por período, forma de pagamento e tipo de consumo;
    - `vw_producao_barista_equipamento`, filtrável por barista e equipamento.
- **Dashboard estatístico** (`/dashboard`), dividido em cinco abas (Visão Geral, Vendas & Estatísticas, Produtos, Clientes e Operação), com filtros de período e forma de pagamento:
    - indicadores: total de pedidos, faturamento, média, mediana, moda, variância, desvio padrão, percentual de pedidos para viagem, total de clientes e de produtos;
    - 15 gráficos interativos em Chart.js (linha, barras, barras empilhadas, pizza, histograma, entre outros) e um boxplot gerado em Python;
    - as oito consultas analíticas do projeto, apresentadas em gráficos e em tabelas.
- **Validação em duas frentes**: o formulário confere o formato dos dados antes de enviar, e o banco aplica as próprias regras com `CHECK`, `UNIQUE` e chaves estrangeiras. Quando o banco recusa uma operação, a mensagem chega traduzida na tela em vez de virar uma exceção no console.
- **Integridade referencial em ação**: excluir um cliente remove os telefones dele em cascata, mas é bloqueado se houver pedidos registrados. Excluir uma mesa deixa os pedidos daquela mesa com o campo nulo, sem apagar o histórico de vendas.
- **Música ambiente**: o site tem um player com faixas em `web/` (`MusicaHandler` e `musica.js`).

**Tecnologias:** Java 21, MySQL 8.0, JDBC (`mysql-connector-j`), HTML, CSS e JavaScript puro, com Chart.js e GSAP carregados por CDN. Python 3 com matplotlib para o boxplot.

## Integração com o banco

O banco tem 17 tabelas, mais a `Log_alteracao_preco`, criada junto com os triggers. Além das tabelas, o projeto usa:

| Objeto | Nome | O que faz |
|---|---|---|
| Índice | `idx_pedido_cliente_data` | acelera a subconsulta do último pedido de cada cliente |
| Índice | `idx_pedido_data_forma_valor` | torna o faturamento por data e forma de pagamento uma leitura só do índice |
| View | `vw_vendas_detalhadas` | um registro por item vendido, juntando pedido, cliente, atendente, mesa e produto |
| View | `vw_producao_barista_equipamento` | produção agregada por barista e equipamento |
| Função | `fn_gasto_total_cliente` | soma o que um cliente já gastou |
| Função | `fn_categoria_cliente` | classifica o cliente em Ouro, Prata, Bronze ou Novo pelo gasto total |
| Procedure | `sp_atualizar_status_pedido` | avança o pedido de Pendente para Em preparo e depois Pronto, recusando transições inválidas |
| Procedure | `sp_distribuir_itens_pendentes` | com um cursor, atribui cada item pendente ao barista e ao equipamento menos ocupados |
| Trigger | `trg_valortotal` e `trg_valortotal_deletar` | recalculam o `Valor_total` do pedido ao inserir ou remover itens |
| Trigger | `trg_logspreco` | registra em `Log_alteracao_preco` cada mudança de preço de um produto |
| Trigger | `trg_propagar_preco` | repassa o novo preço aos itens dos pedidos ainda em aberto |

Como ver isso funcionando pela interface:

- A categoria dos clientes (`fn_categoria_cliente`) alimenta o gráfico "Clientes por Categoria de Gasto" do dashboard.
- Alterar o preço de um produto na tela de produtos gera uma linha no histórico de preços, que aparece na aba Produtos do dashboard.
- Executar a distribuição de itens pendentes em `/procedimentos` cria registros em `Prepara`, e a aba Produção de `/views` mostra os números subindo.

## Arquitetura

A aplicação é dividida em camadas, e cada uma tem um assunto só. O handler não escreve SQL, o DAO não escreve HTML, e a view não sabe o que é uma requisição HTTP. Há dois tipos de página.

**Páginas renderizadas no servidor** (cadastros e procedimentos):

```
Navegador
    │  HTTP
    ▼
Servidor ──► Handler ──┬──► DAO ──► Conexao ──► MySQL
                       │
                       └──► View ──► Layout ──► Resposta ──► Navegador
```

1. O navegador pede `/clientes`.
2. O `Servidor` encaminha para o `ClienteHandler`.
3. O handler consulta o `ClienteDAO`, que executa o `SELECT` e devolve uma `List<Cliente>`.
4. A `ClienteView` transforma a lista numa tabela HTML.
5. O `Layout` envolve a tabela na página completa, com o menu, o `estilo.css` e o `app.js` embutidos.
6. A `Resposta` escreve os bytes no navegador.

**Páginas com API JSON** (dashboard e views):

```
Navegador ── GET /dashboard ──► DashboardView ──► Layout
    │                            (web/dashboard.html + web/dashboard.js)
    │
    └── fetch /api/dashboard?inicio=...&fim=...&forma=... ──► DashboardApiHandler ──► DAOs ──► MySQL
                                                                      │
        Chart.js desenha os gráficos ◄───────── JSON ◄───────────────┘
```

A página `/views` segue o mesmo caminho, com `ViewsView`, `web/views.js`, `/api/views` e o `ViewsApiHandler`. Os filtros viajam na URL, e os valores sempre entram nas consultas por `PreparedStatement`.

Sobre os objetos que trafegam entre as camadas: as classes de `model` espelham tabelas do banco e servem ao CRUD, enquanto as de `dto` carregam resultados de consultas com `JOIN` e agregações, que não correspondem a nenhuma tabela. O `CafesEGraosDTO`, por exemplo, junta dados de quatro tabelas diferentes.

### Boxplot com Python

O boxplot do faturamento diário é desenhado por um script Python, seguindo o que foi utilizado na disciplina de estatística. O Java continua sendo o único a acessar o banco: o Python não tem conexão com o MySQL, ele só recebe os dados já consultados e desenha a imagem. Assim, todo o SQL permanece nos DAOs e o requisito de enviar SQL diretamente ao banco, sem ORM, continua valendo.

```
Aba "Vendas & Estatísticas" ──► /api/boxplot ──► DashboardApiHandler ──► DashboardDAO ──► MySQL
                                                         │
                                                         └──► GraficosPython ──► python/Graficos.py
                                                                                      │
                                                                                      ▼
Navegador ◄── ArquivoEstaticoHandler (/graficos/...) ◄── graficos_gerados/faturamento-boxplot.png
```

O boxplot respeita os filtros de período e de forma de pagamento. Se o Python não estiver instalado ou o matplotlib estiver faltando, o dashboard avisa na própria aba, e todo o resto continua funcionando.

## Estrutura do repositório

```
coffee_n_sweets/
├── Scripts/
│   ├── coffee_n_sweets.sql      criação das 17 tabelas e constraints
│   ├── insertions.sql           população do banco
│   ├── consultas.sql            quatro consultas analíticas (agregações, JOIN, subconsulta)
│   ├── consultas2.sql           quatro consultas avançadas (LEFT/RIGHT JOIN, EXISTS, subconsulta correlacionada)
│   ├── indices.sql              os dois índices
│   ├── views.sql                as duas views
│   ├── funcoes.sql              funções armazenadas
│   ├── procedures.sql           procedimentos armazenados
│   └── triggers.sql             triggers e a tabela Log_alteracao_preco
├── lib/
│   └── mysql-connector-j.jar    driver JDBC
├── python/
│   └── Graficos.py              gera o boxplot (matplotlib) a partir de JSON
├── src/
│   ├── main/                    Servidor (ponto de entrada) e Conexao
│   ├── http/                    handlers, views, API JSON, Json, Formulario e utilitários de HTML
│   ├── view/                    Layout: a moldura comum das páginas
│   ├── util/                    Resposta: escrita da resposta HTTP
│   ├── dao/                     acesso ao banco, onde vive todo o SQL
│   ├── model/                   Cliente, Produto, Mesa, Funcionario e Pedido
│   └── dto/                     resultados das consultas, das views e dos procedimentos
├── web/
│   ├── estilo.css               folha de estilo, embutida pelo Layout
│   ├── app.js                   animações e interações comuns
│   ├── dashboard.html / .js     página e lógica do dashboard
│   ├── views.html / .js         página e lógica das views
│   ├── musica.js e *.mp3        player de música ambiente
│   └── chart.umd.js             biblioteca Chart.js
├── graficos_gerados/            imagem PNG criada em execução (ignorada pelo Git)
├── config.properties.example    modelo de configuração do banco
└── pom.xml
```

## Rotas

| Rota | O que é |
|---|---|
| `/clientes`, `/produtos`, `/mesas`, `/funcionarios` | cadastros com CRUD |
| `/procedimentos` | execução das procedures e visualização dos efeitos |
| `/views` | as duas views, com filtros, indicadores e gráficos |
| `/dashboard` | dashboard estatístico |
| `/api/dashboard`, `/api/views`, `/api/boxplot` | dados em JSON consumidos pelas páginas |
| `/graficos/...` | imagens geradas pelo Python |
| `/` | arquivos estáticos de `web/` |

## Como executar

**Pré-requisitos:**

- JDK 21 ou superior
- MySQL 8.0 ou superior rodando localmente
- Conexão com a internet, porque o Chart.js e o GSAP são carregados por CDN
- Python 3.8 ou superior, com a biblioteca matplotlib (necessário só para o boxplot)

**1. Crie e popule o banco**

Pelo terminal, na raiz do projeto:

```bash
mysql -u root -p < Scripts/coffee_n_sweets.sql
mysql -u root -p < Scripts/insertions.sql
mysql -u root -p < Scripts/indices.sql
mysql -u root -p < Scripts/views.sql
```

Os scripts `funcoes.sql`, `procedures.sql` e `triggers.sql` não usam `DELIMITER`, então não funcionam pelo cliente `mysql` em linha de comando. Execute esses três **como script** no DBeaver ou no console de banco do IntelliJ (no DBeaver, o botão "Execute SQL Script"), nesta ordem:

1. `funcoes.sql`
2. `procedures.sql`
3. `triggers.sql`

Os triggers ficam por último de propósito, para que a carga inicial de dados não os dispare. Os arquivos `consultas.sql` e `consultas2.sql` são só de leitura e podem ser rodados quando quiser.

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

Execute a classe `main.Servidor` **a partir da raiz do projeto** (é o comportamento padrão do IntelliJ). O `Layout` lê `web/estilo.css` e `web/app.js` com caminho relativo, e o caminho `python/Graficos.py` também é relativo a essa pasta. Depois abra:

```
http://localhost:8080/clientes
http://localhost:8080/views
http://localhost:8080/dashboard
```

O servidor fica em execução até ser interrompido pela IDE.

## Solução de problemas

| Sintoma | Causa provável | O que fazer |
|---|---|---|
| `Communications link failure` / `Connection refused` | O MySQL não está em execução | Inicie o serviço do MySQL e confira a porta em `config.properties` |
| `Unknown database 'coffee_n_sweets'` | Os scripts não foram executados nesse servidor | Rode `coffee_n_sweets.sql` e `insertions.sql` (passo 1) |
| Páginas sem estilo e sem animações | O servidor foi iniciado fora da raiz do projeto | Configure o diretório de trabalho da execução como a raiz do projeto |
| Erro de sintaxe ao rodar `funcoes.sql`, `procedures.sql` ou `triggers.sql` | O script foi executado pelo cliente `mysql` em linha de comando | Execute-o como script no DBeaver ou no IntelliJ |
| `already exists` ao rodar `views.sql` ou `triggers.sql` de novo | Esses scripts não são reexecutáveis | Apague antes o objeto correspondente (`drop view`, `drop trigger`) |
| Gráficos não aparecem | Sem internet, por causa do CDN do Chart.js | Confirme a conexão e recarregue com `Ctrl+F5` |
| O boxplot mostra um aviso de erro | Python ou matplotlib ausentes | Leia a mensagem no console da IDE e siga os dois itens abaixo |
| `ModuleNotFoundError: No module named 'matplotlib'` no console | matplotlib não instalado no Python usado pelo sistema | Rode `python -m pip install matplotlib` (ou com `py`) |
| Nenhum comando Python encontrado | Python fora do PATH | Instale o Python marcando "Add python.exe to PATH" e reinicie a IDE |
| Não é possível excluir um cliente ou produto | Ele já tem pedidos registrados (integridade referencial) | Comportamento esperado: só é possível excluir cadastros sem vendas |

## Equipe
Coffee N' Sweets é um projeto de código open-source desenvolvido por

- Telmo Melo (telmelo)
- Ian Felipe (AnzinFelipe)
- Thiago Neiva (neivals)
- Rafael Barboza (RafaCapetta)
- Guilherme Rapela Medeiros (Guilherme-Rapela-Medeiros)
