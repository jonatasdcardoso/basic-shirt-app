# Basic Shirt App

Aplicação web de uma loja virtual de roupas desenvolvida com **Spring Boot**, **Spring MVC**, **Spring Security** e **Thymeleaf**.

O projeto faz parte do trabalho prático da disciplina de **Programação para Web** e aplica conceitos de desenvolvimento web, arquitetura em camadas, persistência de dados relacional, autenticação e autorização por perfis, testes automatizados e boas práticas de acessibilidade.

---

## 🎯 Objetivo e Funcionalidades

O **Basic Shirt App** permite que clientes naveguem pelo catálogo de camisetas, cadastrem-se, gerenciem seu carrinho de compras e finalizem pedidos com baixa automática de estoque, além de oferecer um painel administrativo restrito.

### Perfis de Acesso

- **Visitante (Anônimo):** visualiza o catálogo de produtos (`/` e `/catalogo`), filtra por categorias, consulta detalhes dos itens (`/catalogo/{id}`) e acessa as telas de login (`/login`) e cadastro (`/cadastro`).
- **Cliente Autenticado (`ROLE_USER`):** adiciona produtos ao carrinho, altera quantidades, remove itens, realiza checkout escolhendo a forma de pagamento (`PIX`, `Cartão de Crédito`, `Cartão de Débito` ou `Boleto Bancário`), consulta seu histórico de pedidos (`/pedidos`) e edita seus dados e senha (`/minha-conta`).
- **Administrador (`ROLE_ADMIN`):** possui acesso completo ao **Dashboard Administrativo** (`/admin`) com indicadores de vendas (Receita Total, Total de Pedidos, Clientes e Ticket Médio), listagem de pedidos recentes e gerenciamento (CRUD) de categorias, produtos e estoque.

---

## 🏛️ Arquitetura em Camadas (Convenções Spring Boot)

O código segue estritamente a separação de responsabilidades do ecossistema Spring Boot, utilizando **injeção de dependências via construtor**, **DTOs de entrada/visualização**, **Bean Validation (`@Valid`)** e controle transacional (`@Transactional`) concentrado na camada de serviço:

```text
src/main/java/com/grouploja/basic_shirt_app/
├── config/          # Carga inicial de dados para homologação (DataInitializer)
├── controller/      # Controllers Web MVC (Thymeleaf) e REST (@RestController em /api/**)
├── dto/             # Objetos de transferência e validação de formulários/views
├── entiny/          # Entidades JPA e Enums de domínio (Usuario, Produto, Categoria, Carrinho, Pedido...)
├── repository/      # Interfaces Spring Data JPA (acessadas exclusivamente pelos Services)
├── security/        # Configuração do Spring Security, UserDetails, UserDetailsService e BCrypt
└── service/         # Regras de negócio, validações de domínio e transações (@Transactional)
```

```text
src/main/resources/
├── static/
│   ├── css/         # estilo.css (base/catálogo) e compra.css (autenticação, carrinho, checkout e admin)
│   └── img/         # Ilustração vetorial padrão dos produtos (camiseta.svg)
└── templates/
    ├── fragments/   # layout.html (cabeçalho dinâmico com contador do carrinho, alertas e rodapé)
    ├── catalogo/    # lista.html e detalhe.html (integrado ao formulário de adição ao carrinho)
    ├── auth/        # login.html, cadastro.html, minha-conta.html e acesso-negado.html
    ├── compra/      # carrinho.html, checkout.html, pedidos.html e pedido-detalhe.html
    └── admin/       # dashboard.html (KPIs, pedidos recentes e CRUD de produtos/categorias)
```

---

## 🗺️ Mapa de Rotas Web e Endpoints REST

### Páginas Web (Spring MVC + Thymeleaf)

| Rota | Método | Acesso | Descrição |
| :--- | :---: | :---: | :--- |
| `/` e `/catalogo` | `GET` | Público | Vitrine de produtos com filtro por categoria |
| `/catalogo/{id}` | `GET` | Público | Detalhes do produto, estoque e formulário **Adicionar ao Carrinho** |
| `/login` | `GET` / `POST` | Público | Autenticação de usuários via formulário com proteção CSRF |
| `/cadastro` | `GET` / `POST` | Público | Cadastro de novos clientes com validação e hash BCrypt |
| `/minha-conta` | `GET` / `POST` | Autenticado | Perfil do usuário logado e atualização segura de nome/senha |
| `/carrinho` | `GET` | Autenticado | Visualização do carrinho em duas colunas e resumo do pedido |
| `/carrinho/adicionar` | `POST` | Autenticado | Inclusão de produto no carrinho com validação de estoque |
| `/carrinho/itens/{id}/quantidade` | `POST` | Autenticado | Incremento/decremento de unidades do item no carrinho |
| `/carrinho/itens/{id}/remover` | `POST` | Autenticado | Remoção de item do carrinho |
| `/checkout` | `GET` | Autenticado | Revisão do pedido, endereço de entrega e seleção de pagamento |
| `/checkout/finalizar` | `POST` | Autenticado | Fechamento do pedido, baixa de estoque e limpeza do carrinho |
| `/pedidos` e `/pedidos/{id}` | `GET` | Autenticado | Histórico de compras do cliente e comprovante detalhado |
| `/admin` e `/admin/**` | `GET` / `POST` | `ADMIN` | Dashboard administrativo e gerenciamento de categorias/produtos |

### API REST (`/api/**`)

- **Público (`GET`):** `/api/produtos`, `/api/produtos/{id}`, `/api/categorias`, `/api/categorias/{id}`
- **Autenticado:** `/api/carrinho/{usuarioId}/**`, `/api/pedidos/{usuarioId}/**`
- **Restrito a `ADMIN`:** `POST`/`PUT`/`DELETE` em `/api/produtos/**` e `/api/categorias/**`, além de `/api/usuarios/**`

---

## 🔐 Segurança Implementada

- **Criptografia de Senhas:** todas as senhas são armazenadas com hash `BCryptPasswordEncoder` (nunca em texto puro).
- **Autenticação Baseada em Sessão:** implementada com `UsuarioDetailsService` e `UsuarioDetails` carregando usuários do banco por e-mail.
- **Autorização por Perfil (`RBAC`):** separação entre `PerfilUsuario.CLIENTE` (`ROLE_USER`) e `PerfilUsuario.ADMIN` (`ROLE_ADMIN`), com página amigável de acesso negado (`/acesso-negado` - HTTP 403).
- **Proteção contra CSRF e IDOR:** tokens CSRF automáticos em todos os formulários HTML Thymeleaf (`POST`) e identificação do comprador sempre obtida da sessão autenticada (`Authentication`), impedindo que um usuário acesse ou altere carrinho/pedidos de terceiros.

---

## 🛠️ Tecnologias Utilizadas

- **Java 25+** (Java 25 é a versão LTS de referência)
- **Spring Boot 4.1.1** (Spring MVC, Spring Data JPA, Spring Security, Bean Validation)
- **Thymeleaf** + `thymeleaf-extras-springsecurity6` + **HTML5 / CSS3**
- **PostgreSQL** (produção/desenvolvimento padrão) e **H2 Database** (testes automatizados e perfil rápido `dev`)
- **JUnit 5**, **Mockito** e **Spring MockMvc / Spring Security Test**
- **Maven Wrapper (`mvnw`)**

---

## ▶️ Como Executar o Projeto

### Opção A — Execução Rápida (sem precisar instalar PostgreSQL)

Ideal para avaliação rápida e homologação local usando banco H2 em memória (`application-dev.properties`):

Linux/macOS:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Windows (PowerShell / CMD):
```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

Acesse no navegador: **http://localhost:8080**

### Opção B — Execução com PostgreSQL

1. No PostgreSQL, crie o usuário e o banco de dados:
```sql
CREATE USER loja_app WITH PASSWORD 'SUA_SENHA';
CREATE DATABASE loja_roupas OWNER loja_app;
GRANT ALL PRIVILEGES ON DATABASE loja_roupas TO loja_app;
```

2. Crie o arquivo `src/main/resources/application.properties` (baseado em `application-example.properties`):
```properties
spring.application.name=basic-shirt-app

spring.datasource.url=jdbc:postgresql://localhost:5432/loja_roupas
spring.datasource.username=loja_app
spring.datasource.password=SUA_SENHA

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```
> **Importante:** O arquivo `application.properties` contém credenciais locais e está no `.gitignore` para não ser enviado ao GitHub.

3. Inicie a aplicação:
```powershell
.\mvnw.cmd spring-boot:run
```

### Contas Padrão para Homologação

Ao subir com o banco vazio, o `DataInitializer` cadastra automaticamente um catálogo inicial de camisetas e duas contas de teste:

| Perfil | E-mail | Senha |
| :--- | :--- | :--- |
| **Cliente (`ROLE_USER`)** | `cliente@basicshirt.com` | `cliente123` |
| **Administrador (`ROLE_ADMIN`)** | `admin@basicshirt.com` | `admin123` |

---

## 🧪 Testes Automatizados e Qualidade

A suíte de testes utiliza banco H2 isolado em memória (`src/test/resources/application.properties`), permitindo executar todos os testes em qualquer máquina sem depender de banco externo:

```powershell
.\mvnw.cmd test
```

### Cobertura da Suíte (`src/test/java/...`)

1. **`BasicShirtAppApplicationTests`:** valida a inicialização completa do contexto Spring Boot.
2. **`UsuarioServicoTest` (Unitários com JUnit 5 + Mockito):**
   - Cadastro de cliente com normalização de e-mail, perfil `CLIENTE` e criptografia `BCrypt`;
   - Rejeição de confirmação de senha divergente e de e-mails duplicados;
   - Atualização cadastral e troca de senha mediante validação da senha atual.
3. **`FluxoCompraServicoIntegracaoTest` (Integração de Serviços e Persistência JPA):**
   - Ciclo completo de adição ao carrinho, soma de quantidades, bloqueio por falta de estoque, fechamento de pedido, baixa automática no estoque do produto e esvaziamento do carrinho;
   - Bloqueio de finalização de carrinho vazio e isolamento de pedidos entre usuários distintos.
4. **`SegurancaEFluxoWebIntegracaoTest` (Integração Web, Thymeleaf e Spring Security com `MockMvc`):**
   - Acesso público às páginas do catálogo, detalhe de produto, login, cadastro e `GET /api/produtos`;
   - Redirecionamento obrigatório para `/login` em rotas protegidas (`/carrinho`, `/checkout`, `/pedidos`, `/minha-conta`, `/admin`);
   - Proteção ativa contra requisições `POST` sem token **CSRF** (`403 Forbidden`);
   - Cadastro via formulário web, falha de autenticação com senha inválida e login com sessão autenticada;
   - Jornada ponta a ponta pelas telas (Catálogo → Adicionar ao Carrinho → Alterar Quantidade → Meu Carrinho → Checkout → Confirmação de Pedido → Meus Pedidos → Minha Conta);
   - Controle de acesso por perfil (`403 Forbidden` para `ROLE_USER` em `/admin` e `POST /api/categorias`, e `200 OK` com gerenciamento liberado para `ROLE_ADMIN`).
5. **`DataInitializerTest`:** verifica a carga automática inicial de usuários e produtos de demonstração.
