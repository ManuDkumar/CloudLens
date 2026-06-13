# CloudLens Metadata Extraction API

A Spring Boot microservice for file uploads and metadata extraction with S3-compatible storage.

## Tech Stack

- Java 17, Spring Boot 3.2.3, Maven
- PostgreSQL, Hibernate JPA, **Flyway migrations**
- AWS S3 SDK (compatible with S3, Cloudflare R2, MinIO)
- Spring Security, Thymeleaf
- **Caffeine cache** (rate limiting)
- **JUnit 5 + Mockito** (18 unit tests)

## Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL running locally
- AWS credentials (or S3-compatible) with bucket access

## Setup

### 1. Clone & configure

```bash
git clone <repo-url>
cd CloudLens
```

### 2. Local credentials

Create `src/main/resources/application-local.properties` (gitignored):

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/cloudlens
spring.datasource.username=postgres
spring.datasource.password=your_password
cloud.storage.access-key=YOUR_ACCESS_KEY
cloud.storage.secret-key=YOUR_SECRET_KEY
cloud.storage.region=eu-north-1
cloud.storage.bucket-name=your-bucket
```

The `local` profile is auto-included from `application.yml`.

### 3. Run

```bash
mvn spring-boot:run
```

Flyway runs migrations automatically on startup. App starts on `http://localhost:8081`.

### 4. Default admin

On first run (non-prod profile), an admin user is auto-created:
- **Username:** `admin`
- **Password:** `admin123`

## Password Requirements

- Minimum 8 characters
- At least one uppercase letter, one lowercase letter, and one digit

## Endpoints

### Web UI

| Method | Path | Description |
|--------|------|-------------|
| GET | `/login` | Login page |
| GET | `/signup` | Register page |
| POST | `/signup` | Create account |
| GET | `/` | Dashboard (file list) |
| POST | `/upload` | Upload a file |
| POST | `/update/{id}` | Edit file description |
| POST | `/delete/{id}` | Delete a file |
| POST | `/delete/bulk` | Bulk delete (admin) |
| GET | `/change-password` | Change password page |
| POST | `/change-password` | Update password |
| POST | `/delete-account` | Delete own account |
| GET | `/admin/users` | User management (admin) |
| POST | `/admin/users/delete/{username}` | Delete user (admin) |

### REST API (admin only)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/files/upload` | Upload a file |
| GET | `/api/v1/files/{id}` | Get file metadata |
| PUT | `/api/v1/files/{id}` | Update file description |
| DELETE | `/api/v1/files/{id}` | Delete a file |
| POST | `/api/v1/files/bulk-delete` | Bulk delete |

### Health

| Method | Path | Description |
|--------|------|-------------|
| GET | `/actuator/health` | Health check |

## Environment Variables

Override `application.yml` defaults via env vars:

- `DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD`
- `STORAGE_ACCESS_KEY`, `STORAGE_SECRET_KEY`
- `STORAGE_REGION`, `STORAGE_BUCKET` (or `AWS_REGION`, `S3_BUCKET_NAME`)
- `STORAGE_ENDPOINT` (for S3-compatible storage like Cloudflare R2)

## Running Tests

```bash
mvn test
```
