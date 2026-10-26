# SnapSales (versão web)

Controle de encomendas, clientes, produtos e estoque.
Projeto Java Web **Spring REST** + telas em HTML/JavaScript, banco de dados **MySQL** acessado por **JDBC** (`JdbcTemplate`).

## Como rodar

1. Deixe o **MySQL** ligado (porta 3306).
2. Informe a senha do seu MySQL: crie o arquivo `src/main/resources/application-local.properties` com a linha
   `spring.datasource.password=SUA_SENHA` (se o usuario nao for `root`, acrescente `spring.datasource.username=SEU_USUARIO`).
   Esse arquivo nao vai para o Git. Se preferir, edite direto `spring.datasource.password` no `application.properties`.
3. Abra a pasta no NetBeans como projeto Maven e execute `SnapsalesApplication` (ou `mvn spring-boot:run`).
4. Acesse http://localhost:8080

Na primeira execução o banco `snapsales` é criado sozinho, as tabelas vêm do `src/main/resources/schema.sql`
e o sistema abre vazio: cadastre pelas telas. Se preferir dados de exemplo, rode o script `banco/dados-exemplo.sql` no MySQL Workbench (opcional).
Para zerar: `DROP DATABASE snapsales;` e rode de novo.

## Testes

`mvn test` (ou botão direito no projeto > Test no NetBeans). Os testes do plano não precisam de banco.
Os testes do plano (01 a 10) estão identificados pelo `@DisplayName`; os testes "Extra" cobrem as regras novas.

## Estrutura (pacotes em `br.com.snapsales`)

| Pacote | Papel |
|---|---|
| `models` | Classes do domínio (Cliente, Produto, Encomenda...). Puras, sem anotação de banco. |
| `repositories` | Acesso ao banco: as **interfaces** (`ClienteRepository`...) e as classes com SQL/JDBC (`ClienteRepositoryJdbc`...). |
| `services` | Regras de negócio e validações (e as exceções `RegraNegocioException`, `NaoEncontradoException`). |
| `controllers` | API REST (`/api/...`) e os `...DTO` (formato JSON trocado com as telas). |
| `factories`, `utils` | `EncomendaFactory` (calcula o total com frete) e `ValidadorUtil`. |

Telas: `src/main/resources/static` (html, css, js). Tabelas: `src/main/resources/schema.sql`.

## API

| Recurso | Rotas |
|---|---|
| Clientes | `GET/POST /api/clientes`, `PUT/DELETE /api/clientes/{id}` |
| Produtos | `GET/POST /api/produtos`, `PUT/DELETE /api/produtos/{id}`, `PATCH /api/produtos/{id}/ativo` |
| Categorias | `GET/POST /api/categorias` |
| Encomendas | `GET/POST /api/encomendas`, `GET/PUT/DELETE /api/encomendas/{id}`, `PATCH /api/encomendas/{id}/status` |
| Movimentações | `GET/POST /api/movimentacoes`, `PUT/DELETE /api/movimentacoes/{id}` |

Erros voltam como JSON `{"erro": "mensagem"}` (400 regra de negócio, 404 não encontrado, 409 registro vinculado a outros dados).

## Regras de negócio

- Total da encomenda = itens + frete (frete só quando é entrega, não retirada).
- Encomenda só aceita item com quantidade menor ou igual ao estoque.
- O estoque é baixado quando a encomenda é **criada**. Editar ajusta a diferença, e excluir uma encomenda não entregue devolve o estoque. O status só avança (Em preparação > Pronto > Entregue).
- Movimentação manual: entrada soma e saída subtrai do estoque; editar ou excluir desfaz o efeito antigo. Saída exige descrição.
- Não é possível excluir cliente ou produto que já tenha encomenda/movimentação (erro 409 com mensagem na tela).
