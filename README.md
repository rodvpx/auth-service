# Auth Service API

[![CI Spring Boot](https://github.com/rodvpx/auth-service/actions/workflows/ci.yml/badge.svg)](https://github.com/rodvpx/auth-service/actions/workflows/ci.yml)

API REST de cadastro de usuários, login com JWT e rotas protegidas por perfil. Projeto de portfólio com Java 21, Spring Boot 4, Spring Security, PostgreSQL 16, Flyway e Docker.

## Tecnologias

- Java 21 e Spring Boot 4.0.5
- Spring Security com JWT HS256
- Spring Data JPA, Bean Validation e Flyway
- PostgreSQL 16 e H2 nos testes
- Maven Wrapper, JUnit 5, Mockito, MockMvc e JaCoCo
- Springdoc OpenAPI (Swagger UI)
- Docker e Docker Compose

## Início rápido com Docker Compose

Pré-requisito: Docker com Docker Compose.

```bash
cp .env.example .env
# edite .env e troque senhas e segredos
docker compose up --build
```

A API fica em `http://localhost:8080` e o Swagger em `http://localhost:8080/swagger-ui.html`. As migrações do Flyway rodam na inicialização. Para encerrar:

```bash
docker compose down
```

Use `docker compose down -v` para remover também o volume do PostgreSQL.

## Documentação da API

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Na Swagger UI, clique em **Authorize**, cole apenas o token retornado pelo login e execute as rotas protegidas. O esquema `bearerAuth` já está configurado.

## Variáveis de ambiente

O perfil padrão tem valores apenas para desenvolvimento local. O perfil `prod` exige as variáveis de banco e `JWT_SECRET`, e a aplicação falha na inicialização se elas faltarem.

| Variável | Obrigatória em prod | Padrão local | Descrição |
| --- | --- | --- | --- |
| `DB_URL` | Sim | `jdbc:postgresql://localhost:5432/authdb` | URL JDBC do PostgreSQL |
| `DB_USER` | Sim | `authuser` | Usuário do banco |
| `DB_PASSWORD` | Sim | `authpass` | Senha do banco |
| `JWT_SECRET` | Sim | segredo de desenvolvimento | Segredo HS256, mínimo de 32 bytes |
| `JWT_EXPIRATION` | Não | `3600000` | Expiração do token em milissegundos |
| `ADMIN_EMAIL` | Sim | vazio | E-mail do admin inicial |
| `ADMIN_PASSWORD` | Sim | vazio | Senha do admin inicial |
| `SPRING_PROFILES_ACTIVE` | Não | `prod` no Compose | Perfil ativo da aplicação |
| `POSTGRES_DB` | Não | `authdb` | Nome do banco criado pelo contêiner |

Nunca commite o arquivo `.env`. Ele já está no `.gitignore`; use `.env.example` como modelo.

### Administrador inicial

A migração `V2` inseria um admin com hash fixo no repositório. Para não alterar o checksum de bancos existentes, `V1` e `V2` foram mantidas e a `V3` remove esse registro. O `AdminUserInitializer` cria o admin na inicialização a partir de `ADMIN_EMAIL` e `ADMIN_PASSWORD`, somente se o e-mail ainda não existir.

Se o admin já existir, a senha não é sobrescrita. Para trocar a senha, atualize o registro no banco ou remova o usuário e reinicie com as novas variáveis. Em produção, a ausência de `ADMIN_EMAIL` e `ADMIN_PASSWORD` faz a aplicação falhar de forma explícita.

## Executar localmente

Pré-requisitos: Java 21 e um PostgreSQL acessível. O caminho mais simples é subir apenas o banco com o Compose:

```bash
cp .env.example .env
docker compose up -d postgres
./mvnw spring-boot:run
```

Se você alterou `DB_USER`, `DB_PASSWORD` ou `POSTGRES_DB` no `.env`, exporte `DB_URL`, `DB_USER` e `DB_PASSWORD` antes de rodar a aplicação. O perfil padrão usa `localhost:5432`, usuário `authuser` e senha `authpass`.

## Endpoints

| Método | Rota | Acesso | Descrição |
| --- | --- | --- | --- |
| `POST` | `/auth/register` | Público | Cadastra um usuário com perfil `USER` |
| `POST` | `/auth/login` | Público | Autentica e retorna um JWT |
| `GET` | `/users/me` | JWT válido (`USER` ou `ADMIN`) | Retorna o e-mail autenticado |
| `GET` | `/admin/test` | JWT válido com perfil `ADMIN` | Rota de teste administrativa |
| `GET` | `/swagger-ui.html` | Público | Interface do Swagger |
| `GET` | `/v3/api-docs` | Público | Especificação OpenAPI |

Respostas de erro seguem o formato:

```json
{
  "status": 409,
  "error": "CONFLICT",
  "message": "Email já cadastrado",
  "timestamp": "2026-10-23T12:34:56.123"
}
```

Erros de validação retornam `400 BAD_REQUEST` e incluem a lista `fields`:

```json
{
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "Erro de validação",
  "timestamp": "2026-10-23T12:34:56.123",
  "fields": [
    { "field": "email", "message": "must be a well-formed email address" },
    { "field": "password", "message": "size must be between 8 and 72" }
  ]
}
```

## Fluxo de exemplo com curl

Os exemplos usam `jq` para extrair o token. O admin abaixo é o criado a partir do `.env`.

### 1. Registrar um usuário

```bash
curl -s -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Maria Silva","email":"maria@example.com","password":"senha-segura"}'
```

Resposta esperada: `200 OK` com `id`, `name`, `email`, `role` e `createdAt`. A senha nunca é retornada.

### 2. Fazer login e guardar o token

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"maria@example.com","password":"senha-segura"}' \
  | jq -r '.accessToken')
```

### 3. Chamar uma rota protegida

```bash
curl -s http://localhost:8080/users/me \
  -H "Authorization: Bearer $TOKEN"
```

Resposta esperada: `Usuário autenticado: maria@example.com`.

### 4. Chamar a rota de admin

Use as credenciais definidas em `ADMIN_EMAIL` e `ADMIN_PASSWORD`:

```bash
ADMIN_TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"troque-esta-senha-admin"}' \
  | jq -r '.accessToken')

curl -s http://localhost:8080/admin/test \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

Um usuário com perfil `USER` recebe `403 Forbidden`. Sem token, as rotas protegidas retornam `401 Unauthorized`, ambas em JSON.

## Testes e cobertura

Os testes usam o perfil `test` e H2 em memória. Não precisam de PostgreSQL nem de variáveis reais.

```bash
./mvnw clean verify
```

O relatório do JaCoCo é gerado em `target/site/jacoco/index.html`. A cobertura atual é de aproximadamente 99% das linhas e 90% dos branches. O build não falha por cobertura mínima neste momento.

Para rodar apenas os testes:

```bash
./mvnw test
```

## Segurança

- Senhas são armazenadas com BCrypt.
- A API é stateless, sem sessão HTTP.
- `/auth/**`, Swagger e OpenAPI são públicos; `/users/**` exige JWT e `/admin/**` exige `ADMIN`.
- Token ausente ou inválido retorna `401`; perfil insuficiente retorna `403`, com corpo JSON padronizado.
- Segredos vêm de variáveis de ambiente e o `.env` não é versionado.
- A imagem Docker final roda com o usuário de sistema `spring`, sem privilégios de root.
