#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="$ROOT_DIR/.env"

APP_USER="${PGH_APP_USER:-pghpizza}"
APP_GROUP="${PGH_APP_GROUP:-$APP_USER}"
INSTALL_DIR="${PGH_INSTALL_DIR:-/opt/pgh-pizza}"
FRONTEND_WEB_ROOT="${PGH_FRONTEND_WEB_ROOT:-/var/www/pgh-pizza}"
SYSTEMD_ENV_FILE="${PGH_SYSTEMD_ENV_FILE:-/etc/pgh-pizza/pgh-pizza.env}"
BACKEND_SERVICE_NAME="${PGH_BACKEND_SERVICE_NAME:-pgh-pizza-backend}"
POSTGRES_CONTAINER="${PGH_POSTGRES_CONTAINER:-pgh-pizza-postgres}"
POSTGRES_VOLUME="${PGH_POSTGRES_VOLUME:-pgh-pizza-postgres-data}"
NGINX_SITE_NAME="${PGH_NGINX_SITE_NAME:-pgh-pizza}"

if [ "$(uname -s)" != "Linux" ]; then
  echo "This script is intended for a Linux server." >&2
  exit 1
fi

if ! command -v systemctl >/dev/null 2>&1; then
  echo "This script needs systemd, but systemctl was not found." >&2
  exit 1
fi

if [ "$(id -u)" -ne 0 ] && ! command -v sudo >/dev/null 2>&1; then
  echo "Run this script as root or install sudo for the current user." >&2
  exit 1
fi

SUDO=""
if [ "$(id -u)" -ne 0 ]; then
  SUDO="sudo"
fi

detect_package_manager() {
  if command -v apt-get >/dev/null 2>&1; then
    echo "apt"
  elif command -v dnf >/dev/null 2>&1; then
    echo "dnf"
  elif command -v yum >/dev/null 2>&1; then
    echo "yum"
  elif command -v apk >/dev/null 2>&1; then
    echo "apk"
  else
    echo "unknown"
  fi
}

install_package() {
  case "$(detect_package_manager)" in
    apt)
      $SUDO apt-get update
      $SUDO apt-get install -y "$@"
      ;;
    dnf)
      $SUDO dnf install -y "$@"
      ;;
    yum)
      $SUDO yum install -y "$@"
      ;;
    apk)
      $SUDO apk add --no-cache "$@"
      ;;
    *)
      echo "Could not find a supported package manager to install: $*" >&2
      exit 1
      ;;
  esac
}

ensure_curl() {
  if ! command -v curl >/dev/null 2>&1; then
    install_package curl ca-certificates
  fi
}

install_docker() {
  ensure_curl
  echo "Installing Docker Engine..."
  curl -fsSL https://get.docker.com -o /tmp/get-docker.sh
  $SUDO sh /tmp/get-docker.sh
}

start_docker() {
  $SUDO systemctl enable --now docker
}

ensure_docker() {
  if ! command -v docker >/dev/null 2>&1; then
    install_docker
  fi

  start_docker
}

node_major_version() {
  node -p "Number(process.versions.node.split('.')[0])" 2>/dev/null || echo 0
}

install_nodejs() {
  if command -v node >/dev/null 2>&1 && [ "$(node_major_version)" -ge 22 ]; then
    return
  fi

  ensure_curl
  case "$(detect_package_manager)" in
    apt)
      echo "Installing Node.js 22..."
      if [ -n "$SUDO" ]; then
        curl -fsSL https://deb.nodesource.com/setup_22.x | sudo bash -
      else
        curl -fsSL https://deb.nodesource.com/setup_22.x | bash -
      fi
      install_package nodejs
      ;;
    dnf|yum)
      echo "Installing Node.js 22..."
      if [ -n "$SUDO" ]; then
        curl -fsSL https://rpm.nodesource.com/setup_22.x | sudo bash -
      else
        curl -fsSL https://rpm.nodesource.com/setup_22.x | bash -
      fi
      install_package nodejs
      ;;
    apk)
      install_package nodejs npm
      ;;
    *)
      echo "Install Node.js 22, then rerun this script." >&2
      exit 1
      ;;
  esac
}

install_java() {
  if command -v java >/dev/null 2>&1 && java -version 2>&1 | grep -q 'version "21'; then
    return
  fi

  case "$(detect_package_manager)" in
    apt)
      install_package openjdk-21-jdk
      ;;
    dnf|yum)
      install_package java-21-openjdk-devel
      ;;
    apk)
      install_package openjdk21
      ;;
    *)
      echo "Install Java 21, then rerun this script." >&2
      exit 1
      ;;
  esac
}

install_host_dependencies() {
  install_package curl ca-certificates openssl nginx
  install_java
  install_nodejs
}

random_hex() {
  if command -v openssl >/dev/null 2>&1; then
    openssl rand -hex "${1:-32}"
  else
    date +%s%N | sha256sum | awk '{print $1}'
  fi
}

default_frontend_base_url() {
  local frontend_port="${FRONTEND_PORT:-80}"
  local public_host="${PGH_SERVER_NAME:-localhost}"
  local scheme="http"

  if [ "${PGH_ENABLE_HTTPS:-false}" = "true" ]; then
    scheme="https"
  fi

  if [ "$public_host" = "_" ]; then
    public_host="localhost"
  fi

  if [ "$frontend_port" = "80" ]; then
    echo "$scheme://$public_host"
  else
    echo "$scheme://$public_host:$frontend_port"
  fi
}

env_key_exists() {
  local key="$1"
  [ -f "$ENV_FILE" ] && grep -Eq "^${key}=" "$ENV_FILE"
}

append_env_if_missing() {
  local key="$1"
  local value="$2"

  if ! env_key_exists "$key"; then
    printf '%s=%s\n' "$key" "$value" >> "$ENV_FILE"
  fi
}

load_env_file() {
  if [ -f "$ENV_FILE" ]; then
    set -a
    # shellcheck source=/dev/null
    . "$ENV_FILE"
    set +a
  fi
}

ensure_env_file() {
  if [ ! -f "$ENV_FILE" ]; then
    local postgres_password
    local jwt_secret
    local admin_password

    postgres_password="$(random_hex 24)"
    jwt_secret="$(random_hex 48)"
    admin_password="$(random_hex 12)"

    cat > "$ENV_FILE" <<EOF
PGH_POSTGRES_DB=${PGH_POSTGRES_DB:-pgh_pizza}
PGH_POSTGRES_USER=${PGH_POSTGRES_USER:-postgres}
PGH_POSTGRES_PASSWORD=${PGH_POSTGRES_PASSWORD:-$postgres_password}

PGH_JWT_SECRET=${PGH_JWT_SECRET:-$jwt_secret}
PGH_JWT_ISSUER=${PGH_JWT_ISSUER:-pgh-pizza-api}
PGH_JWT_EXPIRES_MINUTES=${PGH_JWT_EXPIRES_MINUTES:-120}
PGH_ADMIN_EMAIL=${PGH_ADMIN_EMAIL:-admin@pgh-pizza.local}
PGH_ADMIN_PASSWORD=${PGH_ADMIN_PASSWORD:-$admin_password}
PGH_ADMIN_DISPLAY_NAME="${PGH_ADMIN_DISPLAY_NAME:-PGH Admin}"

PGH_SERVER_NAME=${PGH_SERVER_NAME:-_}
PGH_FRONTEND_BASE_URL=${PGH_FRONTEND_BASE_URL:-$(default_frontend_base_url)}
PGH_ENABLE_HTTPS=${PGH_ENABLE_HTTPS:-false}
PGH_CERTBOT_EMAIL=${PGH_CERTBOT_EMAIL:-}
FRONTEND_PORT=${FRONTEND_PORT:-80}
BACKEND_PORT=${BACKEND_PORT:-8080}
SERVER_ADDRESS=${SERVER_ADDRESS:-127.0.0.1}
POSTGRES_PORT=${POSTGRES_PORT:-5432}
JAVA_OPTS=${JAVA_OPTS:-}

SMTP_ENABLED=${SMTP_ENABLED:-false}
SMTP_HOST=${SMTP_HOST:-localhost}
SMTP_PORT=${SMTP_PORT:-25}
SMTP_USERNAME=${SMTP_USERNAME:-}
SMTP_PASSWORD=${SMTP_PASSWORD:-}
SMTP_FROM=${SMTP_FROM:-}
SMTP_AUTH=${SMTP_AUTH:-false}
SMTP_STARTTLS=${SMTP_STARTTLS:-false}
EOF

    chmod 600 "$ENV_FILE"
    echo "Created .env with generated database, JWT, and admin secrets."
    echo "Initial admin email: ${PGH_ADMIN_EMAIL:-admin@pgh-pizza.local}"
    echo "Initial admin password: ${PGH_ADMIN_PASSWORD:-$admin_password}"
  else
    echo "Using existing .env file."
  fi

  load_env_file

  append_env_if_missing PGH_POSTGRES_DB "${PGH_POSTGRES_DB:-pgh_pizza}"
  append_env_if_missing PGH_POSTGRES_USER "${PGH_POSTGRES_USER:-postgres}"
  append_env_if_missing PGH_POSTGRES_PASSWORD "${PGH_POSTGRES_PASSWORD:-$(random_hex 24)}"
  append_env_if_missing PGH_JWT_SECRET "${PGH_JWT_SECRET:-$(random_hex 48)}"
  append_env_if_missing PGH_JWT_ISSUER "${PGH_JWT_ISSUER:-pgh-pizza-api}"
  append_env_if_missing PGH_JWT_EXPIRES_MINUTES "${PGH_JWT_EXPIRES_MINUTES:-120}"
  append_env_if_missing PGH_ADMIN_EMAIL "${PGH_ADMIN_EMAIL:-admin@pgh-pizza.local}"
  append_env_if_missing PGH_ADMIN_PASSWORD "${PGH_ADMIN_PASSWORD:-$(random_hex 12)}"
  append_env_if_missing PGH_ADMIN_DISPLAY_NAME "\"${PGH_ADMIN_DISPLAY_NAME:-PGH Admin}\""
  append_env_if_missing PGH_SERVER_NAME "${PGH_SERVER_NAME:-_}"
  append_env_if_missing PGH_FRONTEND_BASE_URL "${PGH_FRONTEND_BASE_URL:-$(default_frontend_base_url)}"
  append_env_if_missing PGH_ENABLE_HTTPS "${PGH_ENABLE_HTTPS:-false}"
  append_env_if_missing PGH_CERTBOT_EMAIL "${PGH_CERTBOT_EMAIL:-}"
  append_env_if_missing FRONTEND_PORT "${FRONTEND_PORT:-80}"
  append_env_if_missing BACKEND_PORT "${BACKEND_PORT:-8080}"
  append_env_if_missing SERVER_ADDRESS "${SERVER_ADDRESS:-127.0.0.1}"
  append_env_if_missing POSTGRES_PORT "${POSTGRES_PORT:-5432}"
  append_env_if_missing JAVA_OPTS "${JAVA_OPTS:-}"
  append_env_if_missing SMTP_ENABLED "${SMTP_ENABLED:-false}"
  append_env_if_missing SMTP_HOST "${SMTP_HOST:-localhost}"
  append_env_if_missing SMTP_PORT "${SMTP_PORT:-25}"
  append_env_if_missing SMTP_USERNAME "${SMTP_USERNAME:-}"
  append_env_if_missing SMTP_PASSWORD "${SMTP_PASSWORD:-}"
  append_env_if_missing SMTP_FROM "${SMTP_FROM:-}"
  append_env_if_missing SMTP_AUTH "${SMTP_AUTH:-false}"
  append_env_if_missing SMTP_STARTTLS "${SMTP_STARTTLS:-false}"

  chmod 600 "$ENV_FILE"
  load_env_file
}

ensure_app_user() {
  if ! getent group "$APP_GROUP" >/dev/null 2>&1; then
    $SUDO groupadd --system "$APP_GROUP"
  fi

  if ! id -u "$APP_USER" >/dev/null 2>&1; then
    $SUDO useradd \
      --system \
      --gid "$APP_GROUP" \
      --home-dir "$INSTALL_DIR" \
      --shell /usr/sbin/nologin \
      "$APP_USER"
  fi
}

stop_old_app_containers() {
  local container

  for container in pgh-pizza-backend pgh-pizza-frontend; do
    if $SUDO docker inspect "$container" >/dev/null 2>&1; then
      echo "Stopping old Docker app container: $container"
      $SUDO docker stop "$container" >/dev/null || true
    fi
  done
}

start_postgres_container() {
  local db_name="${PGH_POSTGRES_DB:-pgh_pizza}"
  local db_user="${PGH_POSTGRES_USER:-postgres}"
  local db_password="${PGH_POSTGRES_PASSWORD:-postgres}"
  local postgres_port="${POSTGRES_PORT:-5432}"

  $SUDO docker volume create "$POSTGRES_VOLUME" >/dev/null

  if $SUDO docker inspect "$POSTGRES_CONTAINER" >/dev/null 2>&1; then
    echo "Starting existing Postgres container: $POSTGRES_CONTAINER"
    $SUDO docker start "$POSTGRES_CONTAINER" >/dev/null
  else
    echo "Creating Postgres container: $POSTGRES_CONTAINER"
    $SUDO docker run -d \
      --name "$POSTGRES_CONTAINER" \
      --restart unless-stopped \
      -e POSTGRES_DB="$db_name" \
      -e POSTGRES_USER="$db_user" \
      -e POSTGRES_PASSWORD="$db_password" \
      -p "127.0.0.1:${postgres_port}:5432" \
      -v "$POSTGRES_VOLUME:/var/lib/postgresql/data" \
      postgres:16-alpine >/dev/null
  fi

  echo "Waiting for Postgres to be ready..."
  for _ in $(seq 1 45); do
    if $SUDO docker exec "$POSTGRES_CONTAINER" pg_isready -U "$db_user" -d "$db_name" >/dev/null 2>&1; then
      return
    fi

    sleep 2
  done

  echo "Postgres did not become ready in time. Recent logs:" >&2
  $SUDO docker logs --tail 80 "$POSTGRES_CONTAINER" >&2 || true
  exit 1
}

systemd_quote() {
  local value="$1"
  value="${value//\\/\\\\}"
  value="${value//\"/\\\"}"
  printf '"%s"' "$value"
}

write_env_var() {
  local key="$1"
  local value="$2"

  printf '%s=' "$key"
  systemd_quote "$value"
  printf '\n'
}

write_backend_env_file() {
  local db_name="${PGH_POSTGRES_DB:-pgh_pizza}"
  local db_user="${PGH_POSTGRES_USER:-postgres}"
  local db_password="${PGH_POSTGRES_PASSWORD:-postgres}"
  local postgres_port="${POSTGRES_PORT:-5432}"
  local backend_port="${BACKEND_PORT:-8080}"
  local server_address="${SERVER_ADDRESS:-127.0.0.1}"
  local db_url="${PGH_DB_URL:-jdbc:postgresql://127.0.0.1:${postgres_port}/${db_name}}"
  local tmp_file

  tmp_file="$(mktemp)"
  {
    write_env_var PGH_DB_URL "$db_url"
    write_env_var PGH_DB_USERNAME "$db_user"
    write_env_var PGH_DB_PASSWORD "$db_password"
    write_env_var PGH_FRONTEND_BASE_URL "${PGH_FRONTEND_BASE_URL:-$(default_frontend_base_url)}"
    write_env_var PGH_JWT_SECRET "${PGH_JWT_SECRET:-local-development-secret-change-me-local-development-secret}"
    write_env_var PGH_JWT_ISSUER "${PGH_JWT_ISSUER:-pgh-pizza-api}"
    write_env_var PGH_JWT_EXPIRES_MINUTES "${PGH_JWT_EXPIRES_MINUTES:-120}"
    write_env_var PGH_ADMIN_EMAIL "${PGH_ADMIN_EMAIL:-admin@pgh-pizza.local}"
    write_env_var PGH_ADMIN_PASSWORD "${PGH_ADMIN_PASSWORD:-ChangeMe123!}"
    write_env_var PGH_ADMIN_DISPLAY_NAME "${PGH_ADMIN_DISPLAY_NAME:-PGH Admin}"
    write_env_var PORT "$backend_port"
    write_env_var SERVER_ADDRESS "$server_address"
    write_env_var JAVA_OPTS "${JAVA_OPTS:-}"
    write_env_var SMTP_ENABLED "${SMTP_ENABLED:-false}"
    write_env_var SMTP_HOST "${SMTP_HOST:-localhost}"
    write_env_var SMTP_PORT "${SMTP_PORT:-25}"
    write_env_var SMTP_USERNAME "${SMTP_USERNAME:-}"
    write_env_var SMTP_PASSWORD "${SMTP_PASSWORD:-}"
    write_env_var SMTP_FROM "${SMTP_FROM:-}"
    write_env_var SMTP_AUTH "${SMTP_AUTH:-false}"
    write_env_var SMTP_STARTTLS "${SMTP_STARTTLS:-false}"
  } > "$tmp_file"

  $SUDO install -D -m 600 -o root -g root "$tmp_file" "$SYSTEMD_ENV_FILE"
  rm -f "$tmp_file"
}

build_backend() {
  local jar_file

  echo "Building Spring Boot backend..."
  cd "$ROOT_DIR/backend"
  sed -i 's/\r$//' mvnw
  chmod +x mvnw
  ./mvnw -B -DskipTests package

  jar_file="$(find target -maxdepth 1 -type f -name '*.jar' ! -name '*plain.jar' | head -n 1)"
  if [ -z "$jar_file" ]; then
    echo "Could not find the built backend JAR under backend/target." >&2
    exit 1
  fi

  $SUDO install -d -m 755 -o "$APP_USER" -g "$APP_GROUP" "$INSTALL_DIR/backend"
  $SUDO install -d -m 755 -o "$APP_USER" -g "$APP_GROUP" "$INSTALL_DIR/backend/logs"
  $SUDO install -m 755 -o "$APP_USER" -g "$APP_GROUP" "$jar_file" "$INSTALL_DIR/backend/app.jar"
}

install_backend_service() {
  local java_bin
  local tmp_file

  java_bin="$(command -v java)"
  tmp_file="$(mktemp)"

  cat > "$tmp_file" <<EOF
[Unit]
Description=PGH-Pizza Spring Boot API
After=network-online.target docker.service
Wants=network-online.target
Requires=docker.service

[Service]
User=$APP_USER
Group=$APP_GROUP
WorkingDirectory=$INSTALL_DIR/backend
Environment=JAVA_BIN=$java_bin
Environment=APP_JAR=$INSTALL_DIR/backend/app.jar
EnvironmentFile=$SYSTEMD_ENV_FILE
ExecStart=/bin/sh -c 'exec "\$JAVA_BIN" \$JAVA_OPTS -jar "\$APP_JAR"'
SuccessExitStatus=143
Restart=always
RestartSec=10
NoNewPrivileges=true
PrivateTmp=true

[Install]
WantedBy=multi-user.target
EOF

  $SUDO install -m 644 -o root -g root "$tmp_file" "/etc/systemd/system/${BACKEND_SERVICE_NAME}.service"
  rm -f "$tmp_file"

  $SUDO systemctl daemon-reload
  $SUDO systemctl enable "$BACKEND_SERVICE_NAME" >/dev/null
  $SUDO systemctl restart "$BACKEND_SERVICE_NAME"
}

require_safe_directory_target() {
  local label="$1"
  local path="$2"

  case "$path" in
    ""|"/"|"/bin"|"/boot"|"/dev"|"/etc"|"/home"|"/lib"|"/lib64"|"/opt"|"/proc"|"/root"|"/run"|"/sbin"|"/sys"|"/tmp"|"/usr"|"/var"|"/var/www")
      echo "Refusing to clear unsafe $label path: $path" >&2
      exit 1
      ;;
  esac
}

build_frontend() {
  local frontend_dist="$ROOT_DIR/frontend/dist/frontend/browser"

  echo "Building Angular frontend..."
  cd "$ROOT_DIR/frontend"
  npm ci
  npm run build

  if [ ! -d "$frontend_dist" ]; then
    echo "Could not find built frontend assets at $frontend_dist." >&2
    exit 1
  fi

  require_safe_directory_target "frontend web root" "$FRONTEND_WEB_ROOT"
  $SUDO install -d -m 755 -o root -g root "$FRONTEND_WEB_ROOT"
  $SUDO find "$FRONTEND_WEB_ROOT" -mindepth 1 -maxdepth 1 -exec rm -rf {} +
  $SUDO cp -a "$frontend_dist"/. "$FRONTEND_WEB_ROOT"/
  $SUDO chown -R root:root "$FRONTEND_WEB_ROOT"
}

install_nginx_site() {
  local frontend_port="${FRONTEND_PORT:-80}"
  local backend_port="${BACKEND_PORT:-8080}"
  local server_name="${PGH_SERVER_NAME:-_}"
  local tmp_file

  tmp_file="$(mktemp)"
  cat > "$tmp_file" <<EOF
server {
    listen $frontend_port;
    server_name $server_name;

    root $FRONTEND_WEB_ROOT;
    index index.html;

    access_log /var/log/nginx/pgh-pizza-access.log;
    error_log /var/log/nginx/pgh-pizza-error.log;

    location /api/ {
        proxy_pass http://127.0.0.1:$backend_port/api/;
        proxy_http_version 1.1;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }

    location / {
        try_files \$uri \$uri/ /index.html;
    }
}
EOF

  if [ -d /etc/nginx/sites-available ] && [ -d /etc/nginx/sites-enabled ]; then
    $SUDO install -m 644 -o root -g root "$tmp_file" "/etc/nginx/sites-available/$NGINX_SITE_NAME"
    $SUDO ln -sfn "/etc/nginx/sites-available/$NGINX_SITE_NAME" "/etc/nginx/sites-enabled/$NGINX_SITE_NAME"
    $SUDO rm -f /etc/nginx/sites-enabled/default
  else
    $SUDO install -D -m 644 -o root -g root "$tmp_file" "/etc/nginx/conf.d/${NGINX_SITE_NAME}.conf"
  fi

  rm -f "$tmp_file"

  $SUDO nginx -t
  $SUDO systemctl enable nginx >/dev/null
  $SUDO systemctl reload-or-restart nginx
}

install_certbot() {
  case "$(detect_package_manager)" in
    apt)
      install_package certbot python3-certbot-nginx
      ;;
    dnf|yum)
      install_package certbot python3-certbot-nginx
      ;;
    *)
      echo "PGH_ENABLE_HTTPS=true needs certbot support. Install certbot with the nginx plugin, then rerun." >&2
      exit 1
      ;;
  esac
}

enable_https_if_requested() {
  local frontend_port="${FRONTEND_PORT:-80}"
  local server_name="${PGH_SERVER_NAME:-_}"

  if [ "${PGH_ENABLE_HTTPS:-false}" != "true" ]; then
    return
  fi

  if [ "$frontend_port" != "80" ]; then
    echo "PGH_ENABLE_HTTPS=true requires FRONTEND_PORT=80 for the Let's Encrypt HTTP challenge." >&2
    exit 1
  fi

  if [ "$server_name" = "_" ] || [ -z "$server_name" ]; then
    echo "Set PGH_SERVER_NAME to your public domain before enabling HTTPS." >&2
    exit 1
  fi

  if [ -z "${PGH_CERTBOT_EMAIL:-}" ]; then
    echo "Set PGH_CERTBOT_EMAIL before enabling HTTPS." >&2
    exit 1
  fi

  install_certbot
  $SUDO certbot --nginx \
    --non-interactive \
    --agree-tos \
    --redirect \
    --email "$PGH_CERTBOT_EMAIL" \
    -d "$server_name"
}

print_summary() {
  local frontend_port="${FRONTEND_PORT:-80}"
  local backend_port="${BACKEND_PORT:-8080}"
  local postgres_port="${POSTGRES_PORT:-5432}"
  local frontend_url
  local public_url="${PGH_FRONTEND_BASE_URL:-}"

  if [ "$frontend_port" = "80" ]; then
    frontend_url="http://localhost"
  else
    frontend_url="http://localhost:$frontend_port"
  fi

  if [ -z "$public_url" ]; then
    public_url="$frontend_url"
  fi

  echo
  echo "PGH-Pizza is deployed in hybrid mode."
  echo "Postgres: Docker container '$POSTGRES_CONTAINER' on 127.0.0.1:$postgres_port"
  echo "Backend:  systemd service '$BACKEND_SERVICE_NAME' on 127.0.0.1:$backend_port"
  echo "Frontend: nginx systemd service serving $FRONTEND_WEB_ROOT at $frontend_url"
  echo "Public URL configured for app links: $public_url"
  echo
  echo "Useful checks:"
  echo "  $SUDO docker logs -f $POSTGRES_CONTAINER"
  echo "  $SUDO journalctl -u $BACKEND_SERVICE_NAME -f"
  echo "  $SUDO systemctl status nginx"
  echo
  echo "For public web traffic, allow ports 80 and 443, keep 8080 and 5432 closed, and set PGH_SERVER_NAME plus PGH_FRONTEND_BASE_URL in .env for your domain."
}

install_host_dependencies
ensure_docker
ensure_env_file
ensure_app_user
stop_old_app_containers
start_postgres_container
write_backend_env_file
build_backend
install_backend_service
build_frontend
install_nginx_site
enable_https_if_requested
print_summary
