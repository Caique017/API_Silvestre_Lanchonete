# 🥪 Silvestre Lanchonete - API

API RESTful desenvolvida em **Java com Spring** para gerenciar o sistema da **Silvestre Lanchonete**, com autenticação via JWT, integração com Google OAuth2 e banco de dados PostgreSQL. A API está hospedada no **Google Cloud Run**.

## 🌐 URL Base da API

https://api-docker-141213034707.us-central1.run.app

## 📦 Tecnologias Utilizadas

- Java 21
- Spring Boot
- PostgreSQL
- JWT (manual)
- OAuth2 com Google (para login e cadastro)
- Spring Security
- Spring Data JPA
- Google Cloud Run (hospedagem)

## 👥 Perfis de Acesso

- **Cliente:**
    - Criar conta, fazer login
    - Visualizar produtos
    - Criar pedidos

- **Administrador:**
    - Gerenciar produtos (CRUD)
    - Visualizar e atualizar pedidos

---

## 📚 Rotas da API

### 🛒 **ProductController**

| Método | Rota              | Descrição                  |
|--------|-------------------|----------------------------|
| GET    | `/products`       | Listar todos os produtos   |
| POST   | `/products`       | Criar novo produto (admin) |
| PUT    | `/products/{id}`  | Atualizar produto (admin)  |
| DELETE | `/products/{id}`  | Deletar produto (admin)    |

---

### 📦 **OrderController**

| Método | Rota                      | Descrição                |
|--------|---------------------------|--------------------------|
| GET    | `/orders`                 | Listar pedidos           |
| POST   | `/orders`                 | Criar pedido    |
| PUT    | `/orders/status/{id}`     | Atualizar status do pedido (admin)|

---

### 🔐 **AuthController**

| Método | Rota                                | Descrição                                |
|--------|-------------------------------------|------------------------------------------|
| POST   | `/auth/register`                    | Cadastro de cliente                      |
| POST   | `/auth/login`                       | Login com email e senha                  |
| POST   | `/auth/forgot-password`             | Enviar email de recuperação              |
| POST   | `/auth/reset-password`              | Redefinir senha                          |
| POST   | `/auth/validate-code`               | Validar código de recuperação            |
| POST   | `/auth/update-token`                | Atualizar token JWT                      |
| GET    | `/auth/register/google`             | Iniciar cadastro com Google              |
| GET    | `/auth/register/google/authorized`  | Retorno do cadastro com Google           |
| GET    | `/auth/login/google`                | Iniciar login com Google                 |
| GET    | `/auth/login/google/authorized`     | Retorno do login com Google              |

---

## 📲 Integração com WhatsApp

Assim que o cliente finaliza um pedido pelo frontend, os dados são formatados e enviados para o WhatsApp via `link api.whatsapp.com`, com todos os detalhes do pedido.

---

## ▶️ Como Rodar Localmente

```bash
# Clone o repositório
git clone https://github.com/seu-usuario/silvestre-lanchonete-api.git

# Acesse o projeto
cd silvestre-lanchonete-api

# Configure o banco de dados e variáveis de ambiente em application.properties ou application.yml

# Rode o projeto com Maven ou pelo Spring Boot Plugin
./mvnw spring-boot:run