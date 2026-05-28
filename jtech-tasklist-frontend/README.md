# JTech Tasklist Frontend

SPA em Vue 3 para autenticacao, gerenciamento de listas e acompanhamento de tarefas.

## Stack

- Vue 3 + TypeScript
- Pinia
- Vue Router
- Vuetify
- Vitest + Vue Test Utils
- Vite

## Funcionalidades entregues

- Tela de login com persistencia de sessao
- Dashboard com listas e tarefas
- Persistencia local por usuario com `localStorage`
- Integracao com a API Spring Boot
- Fallback para sessao simulada quando a API estiver indisponivel
- Testes de store, rotas e views principais

## Como executar

```powershell
npm install
npm run dev
```

Aplicacao padrao em `http://localhost:5173`.

## Integracao com backend

Por padrao o frontend tenta autenticar e sincronizar com a API.

Quando a API nao responde, o login cai automaticamente para uma sessao simulada e o board continua funcionando localmente com persistencia por usuario no navegador.

## Scripts

```powershell
npm run dev
npm run build
npm run test:unit -- --run
npm run lint
```

## Testes cobertos

- fluxo de login com sucesso e erro
- fallback para sessao simulada
- persistencia de sessao restaurada
- guards de rota
- CRUD local do board em modo simulado
- interacoes principais do dashboard
