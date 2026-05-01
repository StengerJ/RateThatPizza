# PGH-Pizza

PGH-Pizza is split into two top-level projects:

- `frontend/`: Angular application.
- `backend/`: Spring Boot REST API.

## How The App Works

PGH-Pizza is a container-ready Angular and Spring Boot application. Users open the
frontend in their browser, and the Angular app handles public pages like Home, Ratings,
Blog, Apply, and About Me. Anonymous users can view public content and submit contributor
applications. Approved contributors can create ratings and blog posts, while admins can
approve or reject contributor applications.

In container mode, the frontend runs as an Nginx container. Nginx serves the compiled
Angular files and proxies every `/api` request to the backend container over the Docker
network. This lets the browser use the same origin for the UI and API calls, while the
backend remains a separate Spring Boot service.

The backend validates requests, checks JWT roles, enforces contributor ownership rules,
hashes passwords with BCrypt, stores only hashed password reset tokens, and uses Flyway to
create or validate the PostgreSQL schema. Spring Data JPA repositories handle database
access with parameter binding, so user input is treated as data rather than executable SQL.

Errors are handled quietly in the UI. Client errors are logged to the browser console and
sent to `POST /api/client-logs`; backend errors are written to console output and the
backend log file at `backend/logs/pgh-pizza-api.log` or the container log volume.

## Information Flow

![PGH-Pizza frontend/backend information flow](docs/pgh-pizza-flow.svg)

## Local Development

Start PostgreSQL:

```powershell
docker compose up -d postgres
```

Start the backend:

```powershell
cd backend
.\mvnw spring-boot:run
```

Start the frontend:

```powershell
cd frontend
npm start
```

The frontend calls `/api`. During `npm start`, Angular proxies that to
`http://localhost:8080`. In containers, Nginx proxies `/api` to the backend container.
The backend uses PostgreSQL with database `pgh_pizza`, user `postgres`, and password
`postgres` by default.

The default local admin is `admin@pgh-pizza.local` / `ChangeMe123!`. Override it with
`PGH_ADMIN_EMAIL`, `PGH_ADMIN_PASSWORD`, and `PGH_ADMIN_DISPLAY_NAME`.

## Linux Container Server

From the repo root on a Linux server, run:

```bash
bash scripts/install-and-run-linux.sh
```

The script installs Docker if needed, creates a local `.env` file with generated secrets,
builds the frontend and backend images, and starts three separate containers:

- `pgh-pizza-frontend` exposed on `${FRONTEND_PORT:-4200}`.
- `pgh-pizza-backend` exposed on `${BACKEND_PORT:-8080}`.
- `pgh-pizza-postgres` exposed on `${POSTGRES_PORT:-5432}`.

For a real server, set `PGH_FRONTEND_BASE_URL` in `.env` to the public frontend URL so
password reset links point to the correct host.
