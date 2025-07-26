# AI File Assistant - Installation Guide (Backend Only)

## Table of Contents
- [Prerequisites](#prerequisites)
- [Database & Services Setup](#database--services-setup)
- [System Dependencies](#system-dependencies)
- [Application Configuration](#application-configuration)
- [Running the Application](#running-the-application)
- [Verification](#verification)
- [Troubleshooting](#troubleshooting)
- [Uninstallation](#uninstallation)

---

## Prerequisites

### System Requirements
- **CPU**: 4+ cores (8+ recommended)
- **RAM**: 8GB+ (16GB+ recommended for local AI models)
- **Storage**: 20GB+ free disk space
- **OS**: Linux/macOS/Windows 10+ (Linux recommended for production)

### Required Software
- **Java**: OpenJDK 17 or higher
- **Maven**: 3.8+
- **Docker**: 20.10+ and Docker Compose 2.0+
- **Tesseract OCR**: 5.0+ (for image text extraction)
- **Ollama**: Latest stable (for local LLMs)
- **PostgreSQL**: 13+ with [pgvector](https://github.com/pgvector/pgvector)
- **(Optional) Elasticsearch**: 8+ (for enhanced search)
- **(Optional) Keycloak**: 22+ (for OAuth2/JWT security)

---

## Database & Services Setup

### Using Docker Compose (Recommended)

#### PostgreSQL + pgvector
```bash
cd docker/pgvector
# Start PostgreSQL with pgvector
docker-compose up -d
```
- Default user: `postgres`, password: `postgres`, db: `mydb`

#### Elasticsearch + Kibana (Optional)
```bash
cd docker/elasticsearch
# Start Elasticsearch and Kibana
docker-compose up -d
```
- Elasticsearch: [http://localhost:9200](http://localhost:9200)
- Kibana: [http://localhost:5601](http://localhost:5601)

### Manual PostgreSQL Setup
```bash
# Ubuntu/Debian example
sudo apt update
sudo apt install postgresql postgresql-contrib
# Enable pgvector
sudo -u postgres psql -c "CREATE EXTENSION IF NOT EXISTS vector;"
# Create database and user
sudo -u postgres createuser -P file_assistant
sudo -u postgres createdb -O file_assistant file_assistant
```

---

## System Dependencies

### Tesseract OCR
- **macOS**: `brew install tesseract`
- **Ubuntu/Debian**: `sudo apt install -y tesseract-ocr`
- **Windows**: Use Chocolatey or download installer

### Ollama (for local LLMs)
- [Install instructions](https://ollama.com/download)
- Start Ollama service: `ollama serve`
- Download models as needed: `ollama pull qwen3:14b`

---

## Keycloak Installation & Setup (for OAuth2/JWT Security)

Keycloak is recommended for authentication and authorization. The backend expects a valid JWT from an OAuth2 provider (e.g., Keycloak) for all API and MCP requests.

### Install Keycloak (Docker, Recommended)

```bash
# Pull and run Keycloak (standalone, demo mode)
docker run -d \
  --name keycloak \
  -p 8081:8080 \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  quay.io/keycloak/keycloak:24.0.2 start-dev
```
- Keycloak admin console: http://localhost:8081
- Default admin user: `admin` / `admin`

### Basic Realm & Client Setup
1. Log in to the Keycloak admin console.
2. Create a new **Realm** (e.g., `file-assistant`).
3. Create a new **Client**:
   - Client ID: `mcpfileclient` (or your preferred name)
   - Client Protocol: `openid-connect`
   - Root URL: `http://localhost:8080`
   - Access Type: `confidential` (for backend-to-backend)
   - Valid Redirect URIs: `*` (for development)
   - Save and note the **Client Secret** (if using confidential clients)
4. Create test users and assign roles as needed.

### Backend Integration
- Update `src/main/resources/application.properties`:
    ```properties
    spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:8081/realms/file-assistant/protocol/openid-connect/certs
    spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8081/realms/file-assistant
    jwt.auth.converter.resource-id=mcpfileclient
    jwt.auth.converter.principle-attribute=preferred_username
    ```
- Restart the backend after updating configuration.

For production, see the [Keycloak documentation](https://www.keycloak.org/docs/latest/server_installation/) for advanced setup, HTTPS, and scaling.

---

## Application Configuration

Edit `src/main/resources/application.properties`:
- **Database**: `spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password`
- **File Watch**: `file.watch.directories=/path/to/watch`
- **AI Models**: Ollama and OpenAI/Groq settings
- **Vector Store**: `vector.store.*`
- **Security**: OAuth2/JWT endpoints (Keycloak)
- **Elasticsearch**: `spring.elasticsearch.*`

Set secrets as environment variables (e.g., `OPENAI_API_KEY`).

---

## Running the Application

### Build & Run
```bash
mvn clean install
mvn spring-boot:run
```

---

## Verification
- Backend health: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
- API docs: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

## Troubleshooting
- **Database connection**: Check PostgreSQL/pgvector is running and credentials are correct
- **Ollama/LLM issues**: Ensure Ollama is running and models are downloaded
- **Elasticsearch**: Ensure service is running if enabled
- **Tesseract**: Ensure installed and in PATH
- **Security**: Check Keycloak config if using OAuth2/JWT

---

## Uninstallation
```bash
# Stop and remove containers
docker-compose down -v
# Remove application data
rm -rf ./data ./logs
```

---

For advanced configuration and troubleshooting, see comments in `application.properties` and the project README. 