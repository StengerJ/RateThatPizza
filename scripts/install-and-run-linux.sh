#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="$ROOT_DIR/.env"

if [ "$(uname -s)" != "Linux" ]; then
  echo "This script is intended for a Linux server." >&2
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

install_package() {
  if command -v apt-get >/dev/null 2>&1; then
    $SUDO apt-get update
    $SUDO apt-get install -y "$@"
  elif command -v dnf >/dev/null 2>&1; then
    $SUDO dnf install -y "$@"
  elif command -v yum >/dev/null 2>&1; then
    $SUDO yum install -y "$@"
  elif command -v apk >/dev/null 2>&1; then
    $SUDO apk add --no-cache "$@"
  else
    echo "Could not find a supported package manager to install: $*" >&2
    exit 1
  fi
}

ensure_curl() {
  if ! command -v curl >/dev/null 2>&1; then
    install_package curl ca-certificates
  fi
}

install_docker() {
  ensure_curl
  echo "Installing Docker Engine and the Docker Compose plugin..."
  curl -fsSL https://get.docker.com -o /tmp/get-docker.sh
  $SUDO sh /tmp/get-docker.sh
}

start_docker() {
  if command -v systemctl >/dev/null 2>&1; then
    $SUDO systemctl enable --now docker
  elif command -v service >/dev/null 2>&1; then
    $SUDO service docker start
  fi
}

random_hex() {
  if command -v openssl >/dev/null 2>&1; then
    openssl rand -hex "${1:-32}"
  else
    date +%s%N | sha256sum | awk '{print $1}'
  fi
}

create_env_file() {
  if [ -f "$ENV_FILE" ]; then
    echo "Using existing .env file."
    return
  fi

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
PGH_FRONTEND_BASE_URL=${PGH_FRONTEND_BASE_URL:-http://localhost:4200}

PGH_ADMIN_EMAIL=${PGH_ADMIN_EMAIL:-admin@pgh-pizza.local}
PGH_ADMIN_PASSWORD=${PGH_ADMIN_PASSWORD:-$admin_password}
PGH_ADMIN_DISPLAY_NAME="${PGH_ADMIN_DISPLAY_NAME:-PGH Admin}"

FRONTEND_PORT=${FRONTEND_PORT:-4200}
BACKEND_PORT=${BACKEND_PORT:-8080}
POSTGRES_PORT=${POSTGRES_PORT:-5432}

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
}

if ! command -v docker >/dev/null 2>&1; then
  install_docker
fi

start_docker

if ! $SUDO docker compose version >/dev/null 2>&1; then
  install_docker
  start_docker
fi

create_env_file

cd "$ROOT_DIR"
$SUDO docker compose up -d --build

echo
echo "PGH-Pizza containers are starting."
echo "Frontend: http://localhost:${FRONTEND_PORT:-4200}"
echo "Backend:  http://localhost:${BACKEND_PORT:-8080}/api"
echo "Postgres: localhost:${POSTGRES_PORT:-5432}"
echo
echo "Use '$SUDO docker compose ps' to check container status."
