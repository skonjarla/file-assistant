# AI File Assistant (Backend)

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/yourusername/ai-file-assistant/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A modern, AI-powered backend for file management, semantic search, and document understanding. Built with Spring Boot, PostgreSQL (pgvector), and integrates with Ollama, OpenAI/Groq, and more. **No frontend is included in this repository.**

---

## ✨ Features

- **Real-Time File Monitoring & Indexing**
  - Watches specified directories for file creation, modification, and deletion.
  - Automatically indexes new and changed files, and removes deleted files from the index.
  - Supports a wide range of file types: text, PDF, Office documents, images, and more.

- **Advanced File Processing**
  - Extracts metadata and content using Apache Tika.
  - Performs OCR on images and scanned documents using Tesseract.
  - Chunks large documents for efficient storage and retrieval.
  - Computes file hashes and tracks file attributes (owner, permissions, etc.).

- **Semantic & Hybrid Search**
  - Generates vector embeddings for file content using local (Ollama) or cloud (OpenAI/Groq) models.
  - Stores embeddings in PostgreSQL with pgvector for fast semantic search.
  - Supports hybrid search: combines semantic (vector) and keyword (Elasticsearch) search for best results.
  - Exposes search via REST API and MCP protocol.

- **AI-Powered Document Understanding**
  - Integrates with LLMs for:
    - Semantic Q&A over your files.
    - Document and image classification.
    - Summarization and extraction of key information.
    - Custom prompt workflows via MCP.

- **Task Management & Monitoring**
  - Tracks the status of file processing and indexing tasks.
  - Provides endpoints to monitor and query task progress.

- **Security**
  - All endpoints (including MCP) are protected by OAuth2/JWT (e.g., Keycloak).
  - Fine-grained role extraction and stateless session management.

- **Extensible Protocol Interface (MCP)**
  - Exposes prompt-driven, tool-augmented LLM workflows via the Model Context Protocol (MCP).
  - Allows clients to discover and invoke custom prompts and tools over a secure, authenticated endpoint.

---

## 🏗️ Architecture & Tech Stack

- **Backend**: Java 17+, Spring Boot 3, Spring AI, JPA
- **Database**: PostgreSQL 13+ with [pgvector](https://github.com/pgvector/pgvector)
- **Search**: Elasticsearch 8+ (optional)
- **AI/LLM**: Ollama (local), OpenAI/Groq (cloud)
- **Text Extraction**: Apache Tika, Tesseract OCR
- **Security**: OAuth2/OIDC (Keycloak), JWT
- **Containerization**: Docker, Docker Compose

---

## 🤖 Model Context Protocol (MCP) with Spring AI

This project implements the [Model Context Protocol (MCP)](https://github.com/modelcontextprotocol/spec) using Spring AI's MCP server features. MCP provides a standardized, protocol-based interface for prompt-driven, tool-augmented LLM workflows.

### What is MCP?
- MCP is an open protocol for interacting with LLMs, tools, and prompt workflows in a structured, extensible way.
- It enables clients to discover, invoke, and interact with prompts, tools, and system capabilities over a single endpoint.

### How is MCP used here?
- **Spring AI MCP Server**: Enabled and configured via `application.properties` (see below).
- **Prompt Specifications**: Custom prompts for vector search queries, result formatting, and file search are registered as MCP prompt specifications in `MCPConfiguration.java`.
- **Tool Integration**: File search and vector search tools are exposed to the MCP server, allowing LLMs to invoke them as part of prompt workflows.
- **Prompt Templates**: Prompt templates for vector search and result formatting are provided in `src/main/resources/` (e.g., `vector_search_query_template.txt`, `vector_search_result_format_system_prompt.txt`).
- **Capabilities**: Supports synchronous (SYNC) prompt execution, tool invocation, and change notifications.
- **Endpoint**: The MCP server is available at `/mcp/message` (SSE endpoint).

### Security of MCP Endpoint
- The MCP endpoint (`/mcp/message`) is protected by Spring Security using OAuth2 Resource Server (JWT).
- **All requests require a valid JWT** (e.g., from Keycloak or another OIDC provider).
- The JWT is validated using the issuer and JWK set URI configured in `application.properties`.
- A custom `JwtConverter` extracts user identity and roles from the token, supporting Keycloak-style resource roles.
- Session management is stateless.
- **Example: Accessing MCP with a JWT**
    ```http
    GET /mcp/message
    Authorization: Bearer <your-jwt-token>
    ```
- **Relevant configuration:**
- Replace the values in `application.properties` with your Keycloak configuration: In this example, we're using Keycloak at `http://localhost:9090/realms/home`.
- Keycload port 9090
- Keycloak realm: `home`
- Keycloak client: `mcpfileclient`

    ```properties
    spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:9090/realms/home/protocol/openid-connect/certs
    spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:9090/realms/home
    jwt.auth.converter.resource-id=mcpfileclient
    jwt.auth.converter.principle-attribute=preferred_username
    ```
- You can further customize access control by editing `SecurityConfiguration.java`.

### Example MCP Configuration (application.properties)
```properties
spring.ai.mcp.server.name=ai-file-monitor
spring.ai.mcp.server.version=1.0.0
spring.ai.mcp.server.type=SYNC
spring.ai.mcp.server.sse-message-endpoint=/mcp/message
spring.ai.mcp.server.capabilities.tool=true
spring.ai.mcp.server.resource-change-notification=true
spring.ai.mcp.server.tool-change-notification=true
spring.ai.mcp.server.prompt-change-notification=true
```

### Extending MCP
- Add new prompt templates or tools in `MCPConfiguration.java`.
- Provide new prompt resources in `src/main/resources/`.
- Register additional capabilities or endpoints as needed.

For more details, see the [MCP specification](https://github.com/modelcontextprotocol/spec) and the Spring AI documentation.

---

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL 13+ with pgvector
- Docker & Docker Compose
- Tesseract OCR
- Ollama (for local LLMs)
- (Optional) Elasticsearch 8+, Keycloak

### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/ai-file-assistant.git
cd ai-file-assistant
```

### 2. Database & Services
- Use provided Docker Compose files:
  - `docker/pgvector/docker-compose.yml` for PostgreSQL + pgvector
  - `docker/elasticsearch/docker-compose.yml` for Elasticsearch + Kibana (optional)

### 3. Configure Application
- Edit `src/main/resources/application.properties` for DB, AI, security, and MCP settings
- Set environment variables for secrets (e.g., `OPENAI_API_KEY`)

### 4. Build & Run
```bash
mvn clean install
mvn spring-boot:run
```

---

## ⚙️ Configuration

All configuration is in `src/main/resources/application.properties`.
- **Database**: `spring.datasource.*`
- **AI Models**: Ollama, OpenAI/Groq keys and model names
- **File Watch**: `file.watch.directories=/path/to/watch`
- **Vector Store**: `vector.store.*`
- **Security**: OAuth2/JWT endpoints
- **Elasticsearch**: `spring.elasticsearch.*`
- **MCP**: `spring.ai.mcp.server.*` (see above for example)

---

## 🛠️ API Endpoints

### File Operations
- `POST /files/remove?id={fileId}` — Remove a file
- `GET /files/get?id={fileId}` — Get file metadata and chunks

### Task Monitoring
- `GET /index/tasks/{taskId}` — Get status of a specific task
- `GET /index/tasks/all` — Get status of all tasks

### (Other endpoints: semantic search, LLM chat, etc. — see OpenAPI/Swagger docs)

**API Docs:**
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI Spec: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## 🩺 Troubleshooting
- **CORS issues**: Configure allowed origins in Spring Boot
- **DB errors**: Check pgvector extension and DB credentials
- **Model errors**: Ensure Ollama is running and models are downloaded
- **Auth errors**: Check OAuth2/JWT provider and config

---

## 🤝 Contributing
1. Fork the repo
2. Create a feature branch
3. Commit and push your changes
4. Open a Pull Request

---

## 📄 License

MIT License. See [LICENSE](LICENSE) for details.

---

## 🙏 Acknowledgments
- [Spring Boot](https://spring.io/projects/spring-boot)
- [pgvector](https://github.com/pgvector/pgvector)
- [Ollama](https://ollama.ai/)
- [GROQ](https://groq.com/)
- [Apache Tika](https://tika.apache.org/)
- [Keycloak](https://www.keycloak.org/)
- [Model Context Protocol (MCP)](https://github.com/modelcontextprotocol/spec)
- And all open-source contributors! 