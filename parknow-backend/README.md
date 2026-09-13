# ParkNow Backend Foundation (Phase 1)

This repository contains the backend foundation for **ParkNow - Smart Parking Reservation & Management Platform**, built using **Java 17+** and **Spring Boot 3+**.

---

## 🏗️ Technical Specifications

* **Java Version**: 17 OpenJDK
* **Framework**: Spring Boot 3.2.4
* **Build Tool**: Apache Maven
* **Data Access**: Spring Data JPA & Hibernate 6
* **Validation**: Hibernate Validator (`@Valid`)
* **Databases Supported**:
  * **MySQL 8.0** (Production / Active profile `prod`)
  * **H2 In-Memory Database** (Development default profile `dev`)

---

## 📁 Package Architecture

```
com.parknow/
├── ParkNowApplication.java      # Spring Boot Main Entrypoint
├── config/
│   └── CorsConfig.java          # WebMvcConfigurer CORS configuration for React frontend
├── controller/
│   └── HealthController.java    # GET /api/health Endpoint
├── dto/
│   └── response/
│       ├── ApiResponse.java     # Centralized Generic Response DTO
│       └── HealthResponse.java  # Health Check Response Contract
└── exception/
    ├── ErrorResponse.java       # Centralized Error Format DTO
    ├── ResourceNotFoundException.java
    └── GlobalExceptionHandler.java # @RestControllerAdvice Global Exception Handler
```

---

## ⚙️ Configuration & Environment Variables

| Variable | Description | Default (Dev) | Default (Prod) |
| :--- | :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile (`dev` or `prod`) | `dev` | `prod` |
| `PORT` | Server HTTP Listening Port | `8080` | `8080` |
| `DB_URL` | JDBC Connection String | `jdbc:h2:mem:parknowdb...` | `jdbc:mysql://localhost:3306/parknow_db` |
| `DB_USERNAME` | Database Username | `sa` | `root` |
| `DB_PASSWORD` | Database Password | `""` | `rootpassword` |

---

## 🚀 Running the Project Locally

### Prerequisites
* JDK 17+ installed (`java -version`)
* Maven 3.9+ installed or accessible via Maven Wrapper

### Steps:

1. **Compile & Test**:
   ```bash
   mvn test
   ```

2. **Run in Development Mode (H2 In-Memory DB)**:
   ```bash
   mvn spring-boot:run
   ```
   Or explicitly set profile:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

3. **Run in Production Mode (MySQL DB)**:
   ```bash
   SPRING_PROFILES_ACTIVE=prod DB_URL=jdbc:mysql://localhost:3306/parknow_db DB_USERNAME=root DB_PASSWORD=yourpassword mvn spring-boot:run
   ```

---

## 🔍 Verification Endpoint

### Health Check Endpoint:
```http
GET http://localhost:8080/api/health
```

### Response:
```json
{
  "status": "UP",
  "service": "ParkNow API"
}
```
