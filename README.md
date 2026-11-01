# Basic Shirt App

Aplicação web de uma loja virtual de roupas desenvolvida com Spring Boot, Spring MVC e Thymeleaf.

O projeto faz parte do trabalho prático da disciplina de Programação para Web e tem como objetivo aplicar conceitos de desenvolvimento web, arquitetura em camadas, persistência de dados, autenticação e autorização, além de boas práticas de acessibilidade.

---

## 🎯 Objetivo da Loja

O **Basic Shirt App** é uma aplicação web voltada para uma loja virtual de roupas.

O sistema permitirá que usuários naveguem pelos produtos disponíveis e realizem operações relacionadas à compra de produtos.

A aplicação contará com diferentes níveis de acesso:

- **Usuário comum:** poderá visualizar e consultar os produtos disponíveis.
- **Administrador:** terá acesso às funcionalidades de administração do sistema, incluindo operações de cadastro, consulta, alteração e exclusão (CRUD).

Entre as principais funcionalidades planejadas estão:

- Cadastro e gerenciamento de categorias;
- Cadastro e gerenciamento de produtos;
- Consulta de produtos;
- Cadastro e gerenciamento de usuários;
- Autenticação de usuários;
- Controle de acesso por perfil;
- Carrinho de compras;
- Criação e gerenciamento de pedidos.

---

## 🛠️ Tecnologias Utilizadas

### Backend

- **Java 25**
- **Spring Boot 4.1.1**
- **Spring MVC**
- **Spring Data JPA**
- **Spring Security**
- **Spring Validation**
- **Maven**

### Frontend

- **Thymeleaf**
- **HTML5**
- **CSS3**
- **JavaScript**

### Banco de Dados

- **PostgreSQL**

### Ferramentas

- **Git**
- **GitHub**
- **Visual Studio Code**

### Testes

O projeto possui suporte às ferramentas de testes do ecossistema Spring Boot, incluindo:

- JUnit
- Mockito

---

## ▶️ Como Executar o Projeto

### Pré-requisitos

Antes de executar o projeto, é necessário ter instalado:

- Java 25 ou superior;
- PostgreSQL;
- Git.

O projeto utiliza o **Maven Wrapper**, portanto não é necessário instalar o Maven separadamente.

### 1. Clonar o repositório

Linux/macOS:
```bash
git clone https://github.com/jonatasdcardoso/basic-shirt-app.git
```
Windows:
```cmd
git clone https://github.com/jonatasdcardoso/basic-shirt-app.git
```
### 2. Configurar Banco de Dados
Criar o PostgreSQL seguindos as intruções :
##### Crie o usuário : 
No PostgreSQL:
```SQL
CREATE USER loja_app WITH PASSWORD 'SUA_SENHA';
```
####  Criar Banco de Dados
```SQL
CREATE DATABASE loja_roupas OWNER loja_app;
```
####  Conceder Permissões
```SQL
GRANT ALL PRIVILEGES ON DATABASE loja_roupas TO loja_app;
```
##### Configuração da aplicação
No arquivo:
```diretorio:
src/main/resources/application.properties
```

utilize:
```properties
spring.application.name=basic-shirt-app

spring.datasource.url=jdbc:postgresql://localhost:5432/loja_roupas
spring.datasource.username=loja_app
spring.datasource.password=SUA_SENHA

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

#### Importante : 
O arquivo ```application.properties``` contém configurações locais e não deve ser enviado ao GitHub.
O projeto disponibiliza o arquivo ``` application-example.properties``` como modelo de configuração.
