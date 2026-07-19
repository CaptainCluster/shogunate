#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

# Defaults aligned with docker-compose.yml and backend/src/main/resources/application-local.yml
PGHOST="${PGHOST:-localhost}"
PGPORT="${PGPORT:-5433}"
PGUSER="${PGUSER:-shogunate}"
PGPASSWORD="${PGPASSWORD:-shogunate}"
PGDATABASE="${PGDATABASE:-shogunate}"
API_BASE_URL="${API_BASE_URL:-http://localhost:8080}"

MOCK_USER_USERNAME="${MOCK_USER_USERNAME:-firstmock_lastmock}"
MOCK_USER_PASSWORD="${MOCK_USER_PASSWORD:-TestPassword123!}"
MOCK_USER_ID="${MOCK_USER_ID:-a1111111-1111-4111-8111-111111111111}"

# Precomputed bcrypt (cost 10) for the default password above.
DEFAULT_MOCK_USER_PASSWORD_HASH='$2b$10$NWTY50j5qVYT0Bgr0GBV6O3qwPk4X57BpI1jm5IY0SxUUeGvnWgJ.'

export PGPASSWORD

log() {
  echo "[populate_test_data] $*"
}

die() {
  echo "[populate_test_data] ERROR: $*" >&2
  exit 1
}

psql_exec() {
  psql \
    --host="$PGHOST" \
    --port="$PGPORT" \
    --username="$PGUSER" \
    --dbname="$PGDATABASE" \
    --no-align \
    --tuples-only \
    "$@"
}

wait_for_postgres() {
  local attempts="${1:-30}"
  local delay_seconds="${2:-1}"

  for ((i = 1; i <= attempts; i++)); do
    if psql_exec --command='SELECT 1' >/dev/null 2>&1; then
      return 0
    fi
    sleep "$delay_seconds"
  done

  die "Postgres is not reachable at ${PGHOST}:${PGPORT}. Start it with: docker compose -f \"${ROOT_DIR}/docker-compose.yml\" up -d"
}

backend_is_up() {
  curl --silent --fail --max-time 2 "${API_BASE_URL}/v3/api-docs" >/dev/null 2>&1
}

resolve_password_hash() {
  if [[ "$MOCK_USER_PASSWORD" == "TestPassword123!" ]]; then
    printf '%s' "$DEFAULT_MOCK_USER_PASSWORD_HASH"
    return 0
  fi

  if command -v python3 >/dev/null 2>&1; then
    local generated_hash
    generated_hash="$(
      MOCK_USER_PASSWORD="$MOCK_USER_PASSWORD" python3 - <<'PY'
import os
import sys

password = os.environ["MOCK_USER_PASSWORD"]

try:
    import bcrypt
except ImportError:
    sys.exit(2)

print(bcrypt.hashpw(password.encode(), bcrypt.gensalt(rounds=10)).decode())
PY
    )" && {
      printf '%s' "$generated_hash"
      return 0
    }
  fi

  die "Custom MOCK_USER_PASSWORD requires python3 with the bcrypt package, or use the default password."
}

user_exists() {
  local count
  count="$(
    psql_exec --command="
SELECT COUNT(*)
FROM users
WHERE lower(username) = lower('${MOCK_USER_USERNAME}');
"
  )"
  count="$(printf '%s' "$count" | tr -d '[:space:]')"
  [[ "$count" != "0" ]]
}

create_user_via_sql() {
  local password_hash
  password_hash="$(resolve_password_hash)"

  psql \
    --host="$PGHOST" \
    --port="$PGPORT" \
    --username="$PGUSER" \
    --dbname="$PGDATABASE" \
    --command="
INSERT INTO users (id, username, password_hash, created_at)
VALUES (
  '${MOCK_USER_ID}',
  lower('${MOCK_USER_USERNAME}'),
  '${password_hash}',
  NOW()
)
ON CONFLICT (username) DO UPDATE SET
  password_hash = EXCLUDED.password_hash;
"
}

register_user_via_api() {
  local payload
  payload="$(printf '{"username":"%s","password":"%s"}' "$MOCK_USER_USERNAME" "$MOCK_USER_PASSWORD")"

  local status
  status="$(
    curl --silent --show-error --write-out '%{http_code}' --output /tmp/populate_test_data_register.json \
      --request POST \
      --header 'Content-Type: application/json' \
      --data "$payload" \
      "${API_BASE_URL}/api/auth/register"
  )"

  case "$status" in
    201)
      log "Registered test user via API."
      ;;
    409 | 400)
      if user_exists; then
        log "Test user already exists."
        return 0
      fi
      die "Registration failed with HTTP ${status}: $(cat /tmp/populate_test_data_register.json)"
      ;;
    *)
      die "Registration failed with HTTP ${status}: $(cat /tmp/populate_test_data_register.json)"
      ;;
  esac
}

print_credentials() {
  log "Local test user ready:"
  log "  Username: ${MOCK_USER_USERNAME}"
  log "  Password: ${MOCK_USER_PASSWORD}"
}

main() {
  wait_for_postgres

  if user_exists; then
    log "Test user already exists."
    print_credentials
    exit 0
  fi

  if backend_is_up; then
    log "Backend detected; creating test user through auth API."
    register_user_via_api
  else
    log "Backend not running; inserting test user directly into Postgres."
    create_user_via_sql
  fi

  if ! user_exists; then
    die "Test user was not created."
  fi

  print_credentials
}

main "$@"
