# Auth Service API

API REST para cadastro de usuários, autenticação com JWT e acesso a recursos protegidos por perfil.

## Tecnologias

- Java 21 e Spring Boot
- Spring Security com tokens JWT (HS256)
- PostgreSQL 16, Spring Data JPA e Flyway
- Maven

## Executar com Docker Compose

Com Docker e Docker Compose instalados, execute na raiz do projeto:

```bash
docker compose up --build
```

O Compose inicia o PostgreSQL e a aplicação. As migrações do Flyway são aplicadas na inicialização. A API fica disponível em `http://localhost:8080`.

Para encerrar os serviços:

```bash
docker compose down
```

O banco usa um volume persistente chamado `postgres-data`. Para removê-lo junto com os contêineres, use `docker compose down -v`.

### Executar a aplicação localmente

É necessário ter Java 21 e um PostgreSQL acessível. A configuração padrão em `application.yml` usa:

| Configuração | Valor padrão |
| --- | --- |
| URL JDBC | `jdbc:postgresql://localhost:5432/authdb` |
| Usuário | `authuser` |
| Senha | `authpass` |
| Porta HTTP | `8080` |

Inicie apenas o banco com `docker compose up -d postgres` e, em outro terminal, execute:

```bash
./mvnw spring-boot:run
```

## Autenticação e autorização

As rotas `/auth/**` são públicas. As rotas `/users/**` exigem um JWT válido, e `/admin/**` exige o perfil `ADMIN`. Envie o token no cabeçalho:

```http
Authorization: Bearer <token>
```

O token é assinado com HS256, tem o e-mail do usuário como `subject` e expira após 3.600.000 ms (uma hora) por padrão. Para configurar em outro ambiente, defina `JWT_SECRET` e `JWT_EXPIRATION` (em milissegundos). Use um segredo próprio, forte e armazenado com segurança; o segredo presente no arquivo de configuração é apenas um valor padrão de desenvolvimento.

Usuários cadastrados recebem o perfil `USER`. A migração inicial também insere um usuário com e-mail `admin@email.com` e perfil `ADMIN`; a senha correspondente ao hash da migração não é configurada pelo Compose.

## Endpoints

Base URL: `http://localhost:8080`

| Método | Caminho | Acesso | Descrição |
| --- | --- | --- | --- |
| `POST` | `/auth/register` | Público | Cadastra um usuário com perfil `USER` |
| `POST` | `/auth/login` | Público | Valida as credenciais e retorna um JWT |
| `GET` | `/users/me` | JWT válido | Retorna o e-mail do usuário autenticado em texto |
| `GET` | `/admin/test` | JWT válido com perfil `ADMIN` | Rota de teste administrativa |

### `POST /auth/register`

Requisição (`application/json`):

```json
{
	"name": "Maria Silva",
	"email": "maria@example.com",
	"password": "senha-segura"
}
```

`name` e `password` não podem estar em branco; `email` deve ter formato de e-mail e também é obrigatório no banco. O cadastro armazena a senha com BCrypt e define o perfil `USER`.

Resposta (`200 OK`): `id` UUID, `name`, `email`, `role` e `createdAt`. A senha nunca é retornada.

Exemplo da resposta:

```json
{
	"id": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
	"name": "Maria Silva",
	"email": "maria@example.com",
	"role": "USER",
	"createdAt": "2026-10-21T12:34:56"
}
```

Se o e-mail já estiver cadastrado, retorna `409 Conflict`.

### `POST /auth/login`

Requisição (`application/json`):

```json
{
	"email": "maria@example.com",
	"password": "senha-segura"
}
```

Resposta (`200 OK`):

```json
{
	"accessToken": "<jwt>"
}
```

E-mail ou senha inválidos retornam `401 Unauthorized`.

### `GET /users/me`

Requer `Authorization: Bearer <token>`. Resposta (`200 OK`, texto):

```text
Usuário autenticado: maria@example.com
```

### `GET /admin/test`

Requer um JWT de usuário com perfil `ADMIN`. Resposta (`200 OK`, texto):

```text
Área ADMIN 🔥
```

Um usuário autenticado sem o perfil necessário não tem acesso à rota.

## Erros

As exceções de negócio mapeadas usam este formato JSON:

```json
{
	"status": 409,
	"error": "CONFLICT",
	"message": "Email já cadastrado",
	"timestamp": "2026-10-21T12:34:56.123"
}
```

| Situação | HTTP | `error` |
| --- | --- | --- |
| E-mail já cadastrado | `409 Conflict` | `CONFLICT` |
| Credenciais inválidas no login | `401 Unauthorized` | `UNAUTHORIZED` |

Campos inválidos na requisição são tratados pela validação do Spring. Respostas de autenticação/autorização negadas pelo Spring Security não são convertidas pelo handler de exceções acima para esse formato.

## Testes

Execute a suíte Maven com:

```bash
./mvnw test
```
