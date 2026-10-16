# Smart Quotation Generator (Production Edition)

A modern, full-stack, multi-tenant quotation management web application built with **Java 21 / Spring Boot 3** and **React / TypeScript / Tailwind CSS**.

Transformed from a legacy desktop Java Applet and local CSV storage into a cloud-ready, responsive SaaS solution that runs securely in any modern web browser on desktops, laptops, and mobile devices.

---

## Architecture Overview

```
                         +-----------------------------+
                         |      Client Web Browser     |
                         |   (Chrome, Safari, Edge)    |
                         +--------------+--------------+
                                        | HTTPS / REST
                                        v
+-----------------------------+                  +-----------------------------+
|    React 18 / TypeScript    |                  |    Spring Boot 3 / Java 21   |
|   Vite + Tailwind CSS       |                  |    REST API Layer (/api/v1) |
|   TanStack Query + Zod      |                  +--------------+--------------+
+-----------------------------+                                 |
                                        +-----------------------+-----------------------+
                                        |                       |                       |
                                        v                       v                       v
                         +--------------+-----+   +-------------+-------+   +-----------+-----------+
                         | Security & JWT Auth |   | Math Engine & PDFBox |   | Spring Data JPA / SQL |
                         | Multi-tenant Guard |   | High-Precision Math |   | Flyway Migrations     |
                         +--------------------+   +---------------------+   +-----------+-----------+
                                                                                        |
                                                                                        v
                                                                            +-----------+-----------+
                                                                            |     PostgreSQL 16     |
                                                                            |  (or H2 in dev mode)  |
                                                                            +-----------------------+
```

---

## Key Features

1. **Browser Native & Zero Install**: No Java runtime or browser plugins required on client devices.
2. **Multi-Tenant Business Accounts**: Complete account isolation. Each company manages its own customers, quotes, default tax rates, and branding.
3. **Exact Monetary Calculations**:
   - High-precision `BigDecimal` calculations on the backend with explicit `RoundingMode.HALF_UP`.
   - Zero trust in client-side calculations: server always recalculates and validates line amounts, discounts, taxable amounts, and GST.
4. **Printable Branded PDF Export**: Server-side vector PDF generation powered by Apache PDFBox with company logo, validity dates, itemized table, and customizable terms.
5. **Full Quotation Lifecycle**: Supports `DRAFT`, `SENT`, `ACCEPTED`, `REJECTED`, and `EXPIRED` status transitions with validation.
6. **One-Click Duplication**: Clone existing quotations into new draft sequences instantly.
7. **Interactive Dashboard**: Real-time sales metrics, pipeline values, and recent activity log.
8. **OpenAPI / Swagger 3.0 Documentation**: Interactive API testing available at `/swagger-ui.html`.

---

## Directory Structure

```
project_java/
├── backend/                             # Java 21 Spring Boot Backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/smartquote/     # Layered architecture (controller, service, repository, entity, dto, security)
│   │   │   └── resources/               # application.yml, Flyway migrations (db/migration/V1__init_schema.sql)
│   │   └── test/java/com/smartquote/    # JUnit 5 & Mockito test suite
│   ├── pom.xml                          # Maven build configuration
│   ├── Dockerfile                       # Multi-stage production container build
│   └── .env.example
├── frontend/                            # React 18 + Vite + TypeScript Frontend
│   ├── src/
│   │   ├── components/layout/           # Navbar, Sidebar, AppLayout
│   │   ├── context/                     # AuthContext session provider
│   │   ├── pages/                       # Landing, Login, Register, Dashboard, Quotations, Settings
│   │   ├── services/                    # REST API client
│   │   └── types/                       # TypeScript interfaces
│   ├── public/
│   ├── package.json
│   ├── vite.config.ts
│   ├── tailwind.config.js
│   ├── vercel.json                      # Vercel SPA routing
│   └── .env.example
├── legacy/                              # Preserved original applet & server files for archival
├── docker-compose.yml                   # Container orchestration (PostgreSQL + Backend + Frontend)
├── DEPLOYMENT.md                        # Step-by-step production deployment guide
├── SECURITY.md                          # Security architecture and threat model
└── README.md
```

---

## Prerequisites

- **Java**: OpenJDK 21 or later
- **Maven**: Version 3.8+ or later
- **Node.js**: Version 18+ or 20+
- **Docker & Docker Compose** (Optional, for containerized local workflows)

---

## Local Development Setup

### 1. Backend Setup

The backend is configured to run out-of-the-box in `dev` mode using an embedded file-backed H2 database if PostgreSQL is not running locally.

**Windows (PowerShell or Command Prompt):**
```cmd
cd backend
mvn spring-boot:run
```

**macOS / Linux:**
```bash
cd backend
mvn spring-boot:run
```

The server will start on `http://localhost:8080`.
- Health check: `http://localhost:8080/api/v1/health`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

### 2. Frontend Setup

In a new terminal window:

```bash
cd frontend
npm install
npm run dev
```

The frontend will start on `http://localhost:3000` (or `http://localhost:5173`). Open your browser to begin creating quotations!

---

## Running with Docker Compose

If you have Docker installed, you can start the entire stack (PostgreSQL + Backend + Frontend) with a single command:

```bash
docker compose up --build
```

- Frontend: `http://localhost:3000`
- Backend API: `http://localhost:8080`
- PostgreSQL: `localhost:5432`

---

## Automated Test Suites

### Backend Tests (JUnit 5 & Mockito)
Runs monetary calculation tests, tenant isolation tests, and integration tests:
```bash
cd backend
mvn test
```

### Frontend Tests (Vitest & React Testing Library)
Runs component rendering and calculation logic verification:
```bash
cd frontend
npm test
```

### Production Build Verification
To ensure production packaging succeeds:
```bash
# Frontend production bundle
cd frontend
npm run build

# Backend production fat JAR
cd backend
mvn clean package -DskipTests
```

---

## API Reference (`/api/v1`)

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `GET` | `/api/v1/health` | Service health status | No |
| `POST` | `/api/v1/auth/register` | Register new business user & company | No |
| `POST` | `/api/v1/auth/login` | Authenticate and obtain JWT token | No |
| `POST` | `/api/v1/auth/logout` | End session notification | Yes |
| `GET` | `/api/v1/auth/me` | Fetch authenticated user profile | Yes |
| `GET` | `/api/v1/dashboard` | Dashboard metrics & recent activity | Yes |
| `GET` | `/api/v1/customers` | List company customer address book | Yes |
| `POST` | `/api/v1/customers` | Add customer contact | Yes |
| `GET` | `/api/v1/quotations` | List paginated quotes with search & filter | Yes |
| `POST` | `/api/v1/quotations` | Create quotation (calculated on server) | Yes |
| `GET` | `/api/v1/quotations/{id}` | Get quotation details | Yes |
| `PUT` | `/api/v1/quotations/{id}` | Update draft or sent quotation | Yes |
| `POST` | `/api/v1/quotations/{id}/duplicate` | Duplicate quotation into a new draft | Yes |
| `PATCH` | `/api/v1/quotations/{id}/status` | Validate and update status | Yes |
| `GET` | `/api/v1/quotations/{id}/pdf` | Download branded vector PDF | Yes |
| `GET` | `/api/v1/settings/company` | Get company branding & defaults | Yes |
| `PUT` | `/api/v1/settings/company` | Update company settings | Yes |
| `POST` | `/api/v1/settings/company/logo` | Upload business logo image | Yes |

---

## Production Deployment

Detailed production deployment instructions for **Vercel / Netlify** (frontend), **Render / Railway / AWS** (backend), and **Managed PostgreSQL** are provided in [DEPLOYMENT.md](DEPLOYMENT.md).

---

## Security Policies

Security architecture, JWT handling, input validation, and multi-tenant isolation details are documented in [SECURITY.md](SECURITY.md).
