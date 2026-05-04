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

## Linux Hybrid Server

From the repo root on a Linux server, run:

```bash
bash scripts/install-and-run-linux.sh
```

The script installs host dependencies, creates a local `.env` file with generated secrets,
starts PostgreSQL as a Docker container, builds the backend JAR, installs the backend as a
systemd service, builds the Angular frontend, and serves it with the host Nginx systemd
service.

- `pgh-pizza-postgres`: Docker container exposed only on `127.0.0.1:${POSTGRES_PORT:-5432}`.
- `pgh-pizza-backend`: systemd service exposed only on `127.0.0.1:${BACKEND_PORT:-8080}`.
- `nginx`: systemd service exposed publicly on `${FRONTEND_PORT:-80}` and proxying `/api`
  to the backend.

For a public server, point your domain's `A` record at the server, then set these values
in `.env` and rerun the script:

```bash
PGH_SERVER_NAME=yourdomain.com
PGH_FRONTEND_BASE_URL=http://yourdomain.com
FRONTEND_PORT=80
```

To have the script request a Let's Encrypt certificate through Certbot after DNS is
pointing at the server, also set:

```bash
PGH_ENABLE_HTTPS=true
PGH_CERTBOT_EMAIL=you@example.com
PGH_FRONTEND_BASE_URL=https://yourdomain.com
```

For DigitalOcean firewall rules, allow public inbound `80` and `443`, restrict `22` to
your IP, and keep `8080` and `5432` closed to the public internet.
