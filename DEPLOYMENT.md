# ParkNow - Production Deployment Guide

This document details how to build, configure, and deploy the **ParkNow Smart Parking Reservation Platform** using Docker and Docker Compose.

---

## 🏗️ System Architecture & Services

The deployment consists of three containerized services connected via an internal Docker bridge network:

1. **`mysqldb`** (Database): MySQL 8.0 instance storing users, vehicles, parking lots, slots, reservations, sessions, and payments.
2. **`backend`** (Spring Boot API): Java 17 Spring Boot REST API running on port `8080`.
3. **`frontend`** (React SPA): Production Nginx web server serving Vite-compiled static assets on port `5173` (internal port `80`).

```
[ Client Browser ] ---> http://localhost:5173  (React Frontend / Nginx)
         |
         +-------------> http://localhost:8080/api (Spring Boot REST API)
                                    |
                                    v
                         [ MySQL Database :3306 ]
```

---

## 📋 Prerequisites

- **Docker Desktop** (v20.10+) or **Docker Engine** with Docker Compose v2+.
- Minimum **4 GB free RAM** allocated to Docker daemon.

---

## ⚙️ Environment Configuration

Environment variables are isolated using a `.env` file at the root of the project.

### 1. Create `.env` file from template:
```bash
cp .env.example .env
```

### 2. Environment Variables Reference:

| Parameter | Default Value | Description |
| :--- | :--- | :--- |
| `DB_NAME` | `parknow_db` | MySQL database name |
| `DB_USERNAME` | `root` | Database user |
| `DB_PASSWORD` | `rootpassword` | Database root password |
| `JWT_SECRET` | *64-character hex string* | Secret key for JWT signature validation |
| `JWT_EXPIRATION` | `86400000` | JWT token validity in milliseconds (24 hours) |
| `VITE_API_BASE_URL` | `http://localhost:8080/api` | Base REST API URL for React client |

> ⚠️ **Security Notice**: Never commit `.env` files to git repositories. `.env` is listed in `.gitignore`.

---

## 🚀 One-Step Launch Command

To build and start all containers in detached background mode:

```bash
docker compose up --build -d
```

### Monitoring Logs:
```bash
# View aggregated logs for all services
docker compose logs -f

# View logs for backend only
docker compose logs -f backend
```

### Stopping Services:
```bash
# Stop containers without removing persistent data
docker compose down

# Stop containers and purge MySQL volume
docker compose down -v
```

---

## ✅ Verification Protocol

Verify each component post-launch:

### 1. MySQL Health Check
```bash
docker inspect --format='{{json .State.Health}}' parknow-mysql
```
*Expected Output*: `"Status": "healthy"`

### 2. Backend Health API Endpoint
```bash
curl http://localhost:8080/api/health
```
*Expected Output*:
```json
{
  "status": "UP",
  "service": "ParkNow API"
}
```

### 3. Frontend Web Interface
- Open web browser to `http://localhost:5173`
- Confirm Landing Page renders cleanly.

### 4. Authentication Flow & Pre-Seeded Accounts
Log in using one of the automatically seeded default accounts:
- **Admin Portal**: `admin@parknow.com` / `admin123`
- **User Portal**: `alex@example.com` / `user123`

---

## 🔒 Security Best Practices

1. **Production Passwords**: Replace `rootpassword` with a strong 16+ character password in `.env`.
2. **JWT Secret Rotation**: Generate a secure random 256-bit key using openssl:
   ```bash
   openssl rand -hex 32
   ```
3. **SSL/TLS Termination**: In production cloud deployments (AWS ECS, DigitalOcean, Kubernetes), place an Nginx Reverse Proxy or AWS ALB with Let's Encrypt / ACM SSL certificates in front of port `5173` and `8080`.
