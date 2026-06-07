# CloudLens Metadata Extraction API

A Spring Boot microservice for file uploads and metadata extraction with S3 storage.

## Tech Stack

- Java 17, Spring Boot 3.2.3, Maven
- PostgreSQL, Hibernate JPA
- AWS S3 SDK
- Spring Security, Thymeleaf

## Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL running locally
- AWS credentials with S3 bucket access

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
cloud.storage.access-key=YOUR_AWS_ACCESS_KEY
cloud.storage.secret-key=YOUR_AWS_SECRET_KEY
cloud.storage.region=eu-north-1
cloud.storage.bucket-name=your-bucket
```

The `local` profile is auto-included from `application.yml`.

### 3. Run

```bash
mvn spring-boot:run
```

App starts on `http://localhost:8081`.

## Endpoints

| Method | Path              | Description            |
|--------|-------------------|------------------------|
| POST   | `/api/v1/files/upload` | Upload a file      |
| GET    | `/api/v1/files/{id}`   | Get file metadata  |
| POST   | `/auth/signup`    | Register user         |
| POST   | `/auth/login`     | Login                 |

## Environment Variables

Override `application.yml` defaults via env vars:

- `DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD`
- `AWS_ACCESS_KEY`, `AWS_SECRET_KEY`, `AWS_REGION`
- `S3_BUCKET_NAME`
