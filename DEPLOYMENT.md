# Production Deployment Guide: Smart Quotation Generator

This guide provides end-to-end instructions for deploying the **Smart Quotation Generator** full-stack business web application:
- **Frontend**: Vercel or Netlify (React + Vite + TypeScript)
- **Backend**: Render, Railway, Fly.io, or AWS (Java 21 Spring Boot)
- **Database**: Managed PostgreSQL (Render Postgres, Neon, Supabase, or AWS RDS)

---

## Architecture Overview

```
[ User Browser (Desktop / Mobile) ]
             |
             v
 [ Frontend (Vercel / Netlify CDN) ]
             |
             | HTTPS API Requests
             v
[ Backend (Render / Spring Boot 3 / Java 21) ]
             |
             | Persistent JDBC Connection
             v
[ Managed PostgreSQL 16 (Neon / Supabase / Render) ]
```

---

## Part 1: Provision the PostgreSQL Database

Before launching the backend, you need a PostgreSQL instance.

### Option A: Render Managed PostgreSQL (Recommended with Render Backend)
1. In the Render Dashboard, click **New +** -> **PostgreSQL**.
2. Name: `smartquote-db`
3. Database: `smartquote`
4. User: `smartquote`
5. Region: Select the region closest to your users.
6. Click **Create Database**.
7. Copy the **Internal Database URL** (if deploying backend on Render) or **External Database URL**.

### Option B: Neon Serverless Postgres or Supabase
1. Create a project at [Neon.tech](https://neon.tech) or [Supabase.com](https://supabase.com).
2. Copy the PostgreSQL connection URI.
   Format: `jdbc:postgresql://<host>:5432/<dbname>?sslmode=require`

---

## Part 2: Deploy Backend to Render

### 1. Create Web Service on Render
1. In Render Dashboard, click **New +** -> **Web Service**.
2. Connect your GitHub repository: `project_java`.
3. Configure the service settings:
   - **Name**: `smart-quotation-backend`
   - **Region**: Same region as your database.
   - **Branch**: `main`
   - **Root Directory**: `backend`
   - **Runtime**: **Docker** (Render will automatically detect `backend/Dockerfile`)
   - **Instance Type**: Starter / Free

### 2. Configure Environment Variables
In the Render Service **Environment** tab, add:

| Key | Example Value | Description |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` | Activates production configuration with Flyway |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://<host>:5432/smartquote?sslmode=require` | PostgreSQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | `smartquote` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | `<your-db-password>` | Database password |
| `JWT_SECRET` | `generate-a-random-64-character-hex-string` | 256-bit secret key |
| `JWT_EXPIRATION_MS` | `86400000` | 24 hours in milliseconds |
| `CORS_ALLOWED_ORIGINS` | `https://your-frontend.vercel.app` | Your deployed frontend URL |
| `PORT` | `8080` | Server listen port |

### 3. Deploy and Verify
1. Click **Deploy Web Service**.
2. Once deployed, verify health:
   ```bash
   curl https://<your-backend-url>.onrender.com/api/v1/health
   # Expected response: {"success":true,"message":"Operation successful","data":{"status":"UP",...}}
   ```
3. View Swagger API documentation:
   `https://<your-backend-url>.onrender.com/swagger-ui.html`

---

## Part 3: Deploy Frontend to Vercel

### 1. Import Repository
1. In the Vercel Dashboard, click **Add New...** -> **Project**.
2. Import your GitHub repository.

### 2. Configure Build & Project Settings
- **Framework Preset**: Vite
- **Root Directory**: `frontend` (Click **Edit** and choose `frontend`)
- **Build Command**: `npm run build`
- **Output Directory**: `dist`
- **Install Command**: `npm install`

### 3. Environment Variables
Add the following variable:

| Key | Value | Notes |
|---|---|---|
| `VITE_API_BASE_URL` | `https://<your-backend-url>.onrender.com` | **No trailing slash** |

### 4. Deploy
1. Click **Deploy**.
2. Vercel automatically deploys the site with client-side SPA routing governed by `frontend/vercel.json`.
3. Copy your live Vercel domain (e.g. `https://smartquote.vercel.app`).
4. **Important**: Go back to your Render backend environment variables and ensure `CORS_ALLOWED_ORIGINS` includes your Vercel URL!

---

## Part 4: Alternative Frontend Deployment on Netlify

If you prefer Netlify:
1. Click **Add new site** -> **Import an existing project**.
2. Base directory: `frontend`
3. Build command: `npm run build`
4. Publish directory: `dist` (or `frontend/dist`)
5. In **Site Configuration** -> **Environment Variables**:
   `VITE_API_BASE_URL` = `https://<your-backend-url>.onrender.com`
6. `frontend/public/_redirects` ensures all React Router routes resolve to `index.html`.

---

## Part 5: Database Schema Migrations & Backups

### Migrations
- Flyway automatically runs all migration scripts in `classpath:db/migration` (`V1__init_schema.sql`, etc.) on backend startup when `SPRING_PROFILES_ACTIVE=prod`.
- No manual SQL script execution is required.

### Backup Strategy
To create a database backup:
```bash
pg_dump -U smartquote -h <host> -d smartquote -F c -b -v -f smartquote_backup.dump
```

To restore from a backup:
```bash
pg_restore -U smartquote -h <host> -d smartquote -v -c smartquote_backup.dump
```

---

## Part 6: Troubleshooting

| Issue | Diagnosis | Solution |
|---|---|---|
| `CORS error in browser console` | Frontend origin is blocked by backend | Add your frontend domain to `CORS_ALLOWED_ORIGINS` in backend environment variables and restart backend. |
| `Cannot connect to database` | Bad JDBC URL or network restriction | Ensure URL format begins with `jdbc:postgresql://` and includes `?sslmode=require` if required by cloud DB provider. |
| `White screen on route refresh` | SPA redirect rule missing | Ensure `vercel.json` (for Vercel) or `_redirects` (for Netlify) is deployed in the frontend build. |
| `Flyway schema error` | Existing tables conflict | Ensure the database is clean for the initial migration, or set `flyway.baseline-on-migrate: true`. |

