# Coffee n' Sweets

Sistema de gerenciamento e automação de pedidos para cafeterias, restaurantes e derivados

A proposta desse repositório é integrar e registrar dados de um banco MySQL com uma interface programada sem o uso de frameworks/ferramentas ORM. O nosso projeto resolve problemas práticos de gerenciamento de pedidos de estabelecimentos comerciais, além de manter registro de dados de fornecedores, mesas, preços etc

## O que o sistema faz

- **Cadastro de clientes** com criação, edição, exclusão e listagem.
- **Cadastro de produtos** com as mesmas operações.
- **Validação em duas frentes**: o formulário confere o formato dos dados antes de enviar, e o banco aplica as próprias regras com `CHECK`, `UNIQUE` e chaves estrangeiras. Quando o banco recusa uma operação, a mensagem chega traduzida na tela em vez de virar uma exceção no console.
- **Integridade referencial em ação**: excluir um cliente remove os telefones dele em cascata, mas é bloqueado se houver pedidos registrados. Excluir uma mesa deixa os pedidos daquela mesa com o campo nulo, sem apagar o histórico de vendas.

**Tecnologias:** Java 21, MySQL 8.0, JDBC (`mysql-connector-j`), HTML e CSS.

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

## Estrutura do repositório

```
coffee_n_sweets/
├── Scripts/
│   ├── coffee_n_sweets.sql      criação das 17 tabelas e constraints
│   ├── insertions.sql           população do banco (30+ tuplas por tabela)
│   └── consultas.sql            as consultas analíticas do sistema
├── lib/
│   └── mysql-connector-j.jar    driver JDBC
├── src/
│   ├── main/                    Servidor (ponto de entrada) e Conexao
│   ├── http/                    handlers, views e utilitários de HTML
│   ├── view/                    Layout: a moldura comum das páginas
│   ├── util/                    Resposta: escrita da resposta HTTP
│   ├── dao/                     acesso ao banco, onde vive todo o SQL
│   ├── model/                   Cliente e Produto
│   └── dto/                     resultados das consultas analíticas
├── web/                         folha de estilo e recursos estáticos
├── config.properties.example    modelo de configuração do banco
└── pom.xml
```

## Como executar

**Pré-requisitos:** JDK 21 ou superior e MySQL 8.0 ou superior rodando localmente.

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

**4. Rode**

Execute a classe `main.Servidor` e abra:

```
http://localhost:8080/clientes
```

O servidor fica em execução até ser interrompido pela IDE.

## Equipe
Coffee N' Sweets é um projeto de código open-source desenvolvido por

- Telmo Melo (telmelo)
- Ian Felipe (AnzinFelipe)
- Thiago Neiva (neivals)
- Rafael Barboza (RafaCapetta) 
- Guilherme Rapela Medeiros (Guilherme-Rapela-Medeiros)
