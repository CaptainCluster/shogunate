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

MOCK_USER_FIRST_NAME="${MOCK_USER_FIRST_NAME:-Firstmock}"
MOCK_USER_LAST_NAME="${MOCK_USER_LAST_NAME:-Lastmock}"
MOCK_USER_EMAIL="${MOCK_USER_EMAIL:-firstmock.lastmock@local.test}"
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

  die "Custom MOCK_USER_PASSWORD requires the backend API or python3 with the bcrypt package. Use the default password or start the backend."
}

user_verification_status() {
  psql_exec --command="SELECT email_verified FROM users WHERE lower(email) = lower('${MOCK_USER_EMAIL}');"
}

create_verified_user_via_sql() {
  local password_hash
  password_hash="$(resolve_password_hash)"

  psql \
    --host="$PGHOST" \
    --port="$PGPORT" \
    --username="$PGUSER" \
    --dbname="$PGDATABASE" \
    --command="
INSERT INTO users (id, email, password_hash, email_verified, created_at)
VALUES (
  '${MOCK_USER_ID}',
  '${MOCK_USER_EMAIL}',
  '${password_hash}',
  true,
  NOW()
)
ON CONFLICT (email) DO UPDATE SET
  email_verified = true,
  password_hash = EXCLUDED.password_hash;
"
}

register_user_via_api() {
  local payload
  payload="$(printf '{"email":"%s","password":"%s"}' "$MOCK_USER_EMAIL" "$MOCK_USER_PASSWORD")"

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
      if [[ "$(user_verification_status)" == "t" ]]; then
        log "Verified test user already exists."
        return 0
      fi
      log "Test user already registered; verifying existing account."
      ;;
    *)
      die "Registration failed with HTTP ${status}: $(cat /tmp/populate_test_data_register.json)"
      ;;
  esac
}

verify_user_via_api() {
  local token
  token="$(
    psql_exec --command="
SELECT token
FROM email_verification_tokens evt
JOIN users u ON u.id = evt.user_id
WHERE lower(u.email) = lower('${MOCK_USER_EMAIL}')
  AND evt.used_at IS NULL
  AND evt.expires_at > NOW()
ORDER BY evt.created_at DESC
LIMIT 1;
"
  )"

  token="$(printf '%s' "$token" | tr -d '[:space:]')"
  if [[ -z "$token" ]]; then
    if [[ "$(user_verification_status)" == "t" ]]; then
      return 0
    fi
    die "No active verification token found for ${MOCK_USER_EMAIL}."
  fi

  local payload
  payload="$(printf '{"token":"%s"}' "$token")"

  local status
  status="$(
    curl --silent --show-error --write-out '%{http_code}' --output /tmp/populate_test_data_verify.json \
      --request POST \
      --header 'Content-Type: application/json' \
      --data "$payload" \
      "${API_BASE_URL}/api/auth/verify-email"
  )"

  if [[ "$status" != "200" ]]; then
    die "Email verification failed with HTTP ${status}: $(cat /tmp/populate_test_data_verify.json)"
  fi

  log "Verified test user via API."
}

create_verified_user_via_api() {
  register_user_via_api
  if [[ "$(user_verification_status)" != "t" ]]; then
    verify_user_via_api
  fi
}

print_credentials() {
  log "Local test user ready:"
  log "  Name:     ${MOCK_USER_FIRST_NAME} ${MOCK_USER_LAST_NAME}"
  log "  Email:    ${MOCK_USER_EMAIL}"
  log "  Password: ${MOCK_USER_PASSWORD}"
}

main() {
  wait_for_postgres

  if [[ "$(user_verification_status)" == "t" ]]; then
    log "Verified test user already exists."
    print_credentials
    exit 0
  fi

  if backend_is_up; then
    log "Backend detected; creating test user through auth API."
    create_verified_user_via_api
  else
    log "Backend not running; inserting verified test user directly into Postgres."
    create_verified_user_via_sql
  fi

  if [[ "$(user_verification_status)" != "t" ]]; then
    die "Test user exists but is still unverified."
  fi

  print_credentials
}

main "$@"
