# Irembo Test - Sec certificate App

## Live Demo

**URL:** [https://irembo.kulturman.com](https://irembo.kulturman.com)

### Demo Credentials

- **Regular User**
  - Email: `user@example.com`
  - Password: `password`

- **Admin User** (can onboard new users)
  - Email: `admin@example.com`
  - Password: `password`

## Tech Stack

### Backend
- Java 25
- Spring Boot 4.0.0
- PostgreSQL 16
- RabbitMQ
- JWT Authentication
- Flyway (Database Migrations)
- JPA/Hibernate
- OpenAPI/Swagger
- JaCoCo (Code Coverage)

### Frontend
- Angular 21
- PrimeNG
- Tailwind CSS
- RxJS

## Prerequisites

Before running this application, ensure you have the following installed:

- **Java 25** or higher
- **Maven 3.9+** (or use the included Maven wrapper)
- **Node.js 20+** and npm
- **Docker** and **Docker Compose** (for running PostgreSQL and RabbitMQ)
- **Git**

## Project Structure

```
.
├── src/                    # Backend Spring Boot application
│   ├── main/
│   │   ├── java/          # Java source code
│   │   └── resources/     # Application configuration and migrations
│   └── test/              # Backend tests
├── frontend/              # Angular frontend application
│   ├── src/              # Angular source code
│   └── dist/             # Build output
├── docker-compose.yml    # Local development infrastructure
├── docker-compose.prod.yml # Production deployment
└── pom.xml               # Maven configuration
```

## Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd IremboTest
```

### 2. Start Infrastructure Services

Start PostgreSQL and RabbitMQ using Docker Compose:

```bash
docker-compose up -d
```

This will start:
- **PostgreSQL** on `localhost:5420`
- **RabbitMQ** on `localhost:5670`
- **RabbitMQ Management UI** on `http://localhost:15670` (guest/guest)

### 3. Run the Backend

#### Option A: Using Maven Wrapper (Recommended)

```bash
./mvnw spring-boot:run
```

#### Option B: Using Maven

```bash
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

#### Backend Features:
- **API Base URL:** `http://localhost:8080/api`
- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **API Docs:** `http://localhost:8080/v3/api-docs`
- **Health Check:** `http://localhost:8080/actuator/health`

### 4. Run the Frontend

In a new terminal window:

```bash
cd frontend
npm install
npm start
```

The frontend will start on `http://localhost:4200`

## Testing

### Backend Tests

Run all backend tests with code coverage:

```bash
./mvnw test
```

The project uses:
- **Testcontainers** for integration tests (automatically starts PostgreSQL and RabbitMQ containers)
- **JaCoCo** for code coverage (minimum 80% coverage required)

View coverage report:
```bash
./mvnw test
open target/site/jacoco/index.html
```


## Building for Production

### Build Backend

```bash
./mvnw clean package -DskipTests
```

The JAR file will be created in `target/IremboTest-0.0.1-SNAPSHOT.jar`

### Build Frontend

```bash
cd frontend
npm run build
```

The build artifacts will be in `frontend/dist/`

## Running with Docker (Production)

Build and run the entire stack using Docker Compose:

```bash
docker-compose -f docker-compose.prod.yml up -d
```

## Configuration

### Backend Configuration

Configuration is managed through `src/main/resources/application.yml`

Key environment variables:

```bash
DATABASE_URL=jdbc:postgresql://localhost:5420/irembo
DATABASE_USERNAME=irembo
DATABASE_PASSWORD=irembo
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5670
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest
JWT_SECRET=your-256-bit-secret-key
BACK_URL=http://localhost:8080
```

### Frontend Configuration

The frontend uses a proxy configuration (`frontend/proxy.conf.json`) to forward API requests to the backend during development.

## Database Migrations

The application uses Flyway for database migrations. Migration scripts are located in:
```
src/main/resources/db/migration/
```

Migrations run automatically on application startup.

## Key Features

1. **User Authentication & Authorization**
   - JWT-based authentication
   - Role-based access control (Admin/User)

2. **User Onboarding**
   - Admin can onboard new users
   - Document upload and verification
   - QR code generation for verification

3. **Notification System**
   - Asynchronous notification processing via RabbitMQ
   - Email notifications for verification status

4. **Document Management**
   - Document upload and storage
   - PDF generation for verification certificates

5. **RESTful API**
   - Fully documented with OpenAPI/Swagger
   - Comprehensive validation

## API Documentation

Once the backend is running, access the interactive API documentation at:

**Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

This provides:
- All available endpoints
- Request/response schemas
- Interactive API testing
- Authentication workflows

## Development Workflow

1. Start infrastructure: `docker-compose up -d`
2. Start backend: `./mvnw spring-boot:run`
3. Start frontend: `cd frontend && npm start`
4. Access application: `http://localhost:4200`
5. Run tests: `./mvnw test` and `cd frontend && npm test`
