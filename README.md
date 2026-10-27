# Sistema AKMOS: site + painel do revendedor

Um único projeto **Java 21 + Quarkus 3 + PostgreSQL** com três partes:

| Endereço | O que é | Quem usa |
|---|---|---|
| `/` | Site da AKMOS (kits e produtos) | Clientes |
| `/admin/` | Painel do revendedor: produtos, fotos, estoque e kits | João (com login) |
| `/admin/relatorio.html` | Relatório de cliques no WhatsApp | João (com login) |

Tudo o que o João muda no painel aparece no site na hora: preço, foto, kit novo, produto esgotado.

## Estrutura

```
src/main/java/br/unitins/akmos/
├── model/       Produto, Kit, Clique (tabelas do banco)
├── dto/         dados de entrada e saída da API
├── repository/  consultas ao banco
├── service/     regras (estoque não fica negativo, produto em kit não pode ser excluído...)
├── resource/    endpoints REST (/api/...)
├── config/      DadosIniciais (catálogo da revista) e SemJanelaDeLogin
└── exception/   NegocioException e o tradutor para JSON
src/main/resources/META-INF/resources/
├── index.html, style.css, site.js, img/   site público
└── admin/                                 painel e relatório
```

Na primeira vez que liga com o banco vazio, o sistema cadastra sozinho os 24 produtos e 8 kits da Revista Akmos 03/2026.

## Rodar no computador

1. Ligue o PostgreSQL. Com Docker:
   ```
   docker run --name akmos-postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=akmos -p 5432:5432 -d postgres:16
   ```
   Se o contêiner já existe, abra o Docker Desktop e dê play no `akmos-postgres`.
2. Na pasta do projeto, com o Java 21:
   ```
   export JAVA_HOME=$(/usr/libexec/java_home -v 21)
   mvn quarkus:dev
   ```
3. Abra no navegador:
   - Site: http://localhost:8080
   - Painel: http://localhost:8080/admin/ (usuário `admin`, senha `akmos2026`)

Não precisa mais do Live Server: o próprio Quarkus entrega o site.

## Publicar na internet (grátis)

O cliente acessa tudo por um link, sem instalar nada. Sugestão: **Neon** para o banco e **Render** para o sistema.

### 1. Banco de dados no Neon
1. Crie uma conta em https://neon.tech e um projeto chamado `akmos`.
2. Em **Connection string**, copie os dados. Ela tem este formato:
   `postgresql://USUARIO:SENHA@HOST/akmos?sslmode=require`

### 2. Código no GitHub
Crie um repositório (pode ser privado) e envie a pasta do projeto.

### 3. Sistema no Render
1. Crie uma conta em https://render.com → **New** → **Web Service** → escolha o repositório.
2. **Language:** Docker. **Instance type:** Free.
3. Em **Environment Variables**, adicione:

| Variável | Valor |
|---|---|
| `DB_URL` | `jdbc:postgresql://HOST/akmos?sslmode=require` (o HOST do Neon, com `jdbc:` na frente) |
| `DB_USUARIO` | o USUARIO do Neon |
| `DB_SENHA` | a SENHA do Neon |
| `ADMIN_SENHA` | a senha que o João vai usar no painel |

4. Clique em **Deploy**. Em alguns minutos o Render mostra o link, tipo `https://akmos.onrender.com`.
   - Site: `https://akmos.onrender.com`
   - Painel: `https://akmos.onrender.com/admin/`

> No plano grátis do Render, o sistema "dorme" depois de 15 minutos sem visitas. O primeiro acesso depois disso demora cerca de 1 minuto; os seguintes são rápidos. Antes de uma apresentação ou do teste com o cliente, abra o link uns minutos antes.

## API

| Método | Caminho | Login | O que faz |
|---|---|---|---|
| GET | `/api/produtos/publico?destaque=true` | não | Produtos do site |
| GET | `/api/kits/publico` | não | Kits do site |
| GET | `/api/produtos/{id}/imagem`, `/api/kits/{id}/imagem` | não | Foto enviada |
| POST | `/api/cliques` | não | Registra clique no WhatsApp |
| GET, POST | `/api/produtos` | admin | Lista todos / cadastra |
| GET, PUT, DELETE | `/api/produtos/{id}` | admin | Busca / altera / exclui |
| PATCH | `/api/produtos/{id}/estoque` | admin | Ajuste rápido: `{"ajuste": -1}` |
| PUT, DELETE | `/api/produtos/{id}/imagem` | admin | Envia foto (multipart, campo `imagem`) / remove |
| GET, POST, PUT, DELETE | `/api/kits`, `/api/kits/{id}` | admin | Mesmo padrão dos produtos |
| GET | `/api/cliques/resumo` | admin | Relatório de cliques |
| GET | `/api/auth/eu` | admin | Confere usuário e senha (tela de login) |

Documentação interativa: http://localhost:8080/q/swagger-ui

## Testes

```
mvn test
```

Usam um banco H2 em memória (não precisa do PostgreSQL). Conferem cadastro, validação, estoque que não fica negativo, envio de foto, preço do kit (soma ou especial), kit indisponível quando falta estoque, bloqueio de exclusão de produto que está em kit, login e o contador de cliques.

## Antes de entregar ao cliente

- O número do WhatsApp (`5563999564173`) está em `index.html` e `site.js`. Para trocar, use Ctrl+H em todos os arquivos.
- Defina uma senha forte em `ADMIN_SENHA`.
- Preencha endereço, Instagram e as respostas das Dúvidas no `index.html`.
