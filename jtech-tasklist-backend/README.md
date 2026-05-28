# JTech Tasklist Backend

API Spring Boot responsavel por autenticacao JWT, refresh token e CRUD de listas e tarefas.

## Stack

- Java 21
- Spring Boot 3.5.5
- Spring Security + JWT
- Spring Data JPA
- H2 para desenvolvimento rapido
- PostgreSQL para execucao local via Docker
- JUnit 5 + Mockito + Spring Boot Test

## Funcionalidades entregues

- Cadastro e login com JWT
- Refresh token em `/auth/refresh`
- CRUD de listas em `/api/v1/tasklists`
- CRUD de tarefas aninhadas nas listas
- Endpoints aderentes ao desafio em `/tasks`
- Isolamento por usuario nas consultas e alteracoes
- Relacao direta entre `Task` e `User`

## Perfis

- `dev`: usa H2 em memoria e `ddl-auto=update`
- `postgres`: usa PostgreSQL local e `ddl-auto=update`
- `test`: usa H2 em memoria para a suite automatizada

## Como executar

### Desenvolvimento rapido com H2

```powershell
./gradlew.bat bootRun
```

O perfil padrao e `dev`, portanto a API sobe usando H2 em memoria.

### PostgreSQL local com Docker

Suba o banco:

```powershell
Set-Location composer
docker compose up -d
```

Em outro terminal, execute a API com o perfil PostgreSQL:

```powershell
$env:PROFILE='postgres'
./gradlew.bat bootRun
```

Configuracao padrao do banco local:

- host: `localhost`
- porta: `5432`
- database: `sansys_database`
- usuario: `postgres`
- senha: `postgres`

Se precisar, os valores tambem podem ser sobrescritos com `DS_URL`, `DS_PORT`, `DS_DATABASE`, `DS_USER` e `DS_PASS`.

## Testes

```powershell
./gradlew.bat test
```

Observacao: o `buildDir` foi redirecionado para uma pasta ASCII temporaria para evitar falha de testes no Windows quando o workspace esta em caminho com acentos.

## Endpoints principais

- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/refresh`
- `GET /api/v1/tasklists`
- `POST /api/v1/tasklists`
- `POST /api/v1/tasklists/{tasklistId}/tasks`
- `GET /tasks`
- `POST /tasks`
- `GET /tasks/{taskId}`
- `PUT /tasks/{taskId}`
- `PATCH /tasks/{taskId}/status`
- `DELETE /tasks/{taskId}`