#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

PGHOST="${PGHOST:-localhost}"
PGPORT="${PGPORT:-5433}"
PGUSER="${PGUSER:-shogunate}"
PGPASSWORD="${PGPASSWORD:-shogunate}"
PGDATABASE="${PGDATABASE:-shogunate}"
API_BASE_URL="${API_BASE_URL:-http://localhost:8080}"

ANALYTICS_USER_USERNAME="${ANALYTICS_USER_USERNAME:-analytics_tester}"
ANALYTICS_USER_PASSWORD="${ANALYTICS_USER_PASSWORD:-TestPassword123!}"
ANALYTICS_USER_ID="${ANALYTICS_USER_ID:-b2222222-2222-4222-8222-222222222222}"

DEFAULT_MOCK_USER_PASSWORD_HASH='$2b$10$NWTY50j5qVYT0Bgr0GBV6O3qwPk4X57BpI1jm5IY0SxUUeGvnWgJ.'

export PGPASSWORD

log() {
  echo "[populate_analytics_test_data] $*"
}

die() {
  echo "[populate_analytics_test_data] ERROR: $*" >&2
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
  if [[ "$ANALYTICS_USER_PASSWORD" == "TestPassword123!" ]]; then
    printf '%s' "$DEFAULT_MOCK_USER_PASSWORD_HASH"
    return 0
  fi

  if command -v python3 >/dev/null 2>&1; then
    local generated_hash
    generated_hash="$(
      ANALYTICS_USER_PASSWORD="$ANALYTICS_USER_PASSWORD" python3 - <<'PY'
import os
import sys

password = os.environ["ANALYTICS_USER_PASSWORD"]

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

  die "Custom ANALYTICS_USER_PASSWORD requires python3 with the bcrypt package, or use the default password."
}

ensure_analytics_user() {
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
  '${ANALYTICS_USER_ID}',
  lower('${ANALYTICS_USER_USERNAME}'),
  '${password_hash}',
  NOW()
)
ON CONFLICT (username) DO UPDATE SET
  password_hash = EXCLUDED.password_hash;
"
}

seed_analytics_data() {
  ANALYTICS_USER_ID="$ANALYTICS_USER_ID" python3 - <<'PY' | psql \
    --host="$PGHOST" \
    --port="$PGPORT" \
    --username="$PGUSER" \
    --dbname="$PGDATABASE" \
    --set ON_ERROR_STOP=1

import os
import uuid
from datetime import date, datetime, timedelta, timezone

user_id = os.environ["ANALYTICS_USER_ID"]

# Fixed catalog IDs (tvmaze_id 99101-99104 reserved for analytics fixtures)
shows = {
    "long_binge": {
        "id": "a1000001-0001-4001-8001-000000000001",
        "tvmaze_id": 99101,
        "title": "Analytics Long Binge",
        "library_status": "WATCHED",
        "episodes": 4,
    },
    "half_done": {
        "id": "a1000001-0001-4001-8001-000000000002",
        "tvmaze_id": 99102,
        "title": "Analytics Half Done",
        "library_status": "NONE",
        "episodes": 2,
    },
    "plan_later": {
        "id": "a1000001-0001-4001-8001-000000000003",
        "tvmaze_id": 99103,
        "title": "Analytics Plan Later",
        "library_status": "PLAN_TO_WATCH",
        "episodes": 2,
    },
    "quick_favorite": {
        "id": "a1000001-0001-4001-8001-000000000004",
        "tvmaze_id": 99104,
        "title": "Analytics Quick Favorite",
        "library_status": "WATCHED",
        "episodes": 2,
        "favorite": True,
    },
}

season_ids = {}
episode_ids = {}

for key, show in shows.items():
    season_ids[key] = str(uuid.uuid5(uuid.NAMESPACE_DNS, f"analytics-season-{key}"))
    episode_ids[key] = [
        str(uuid.uuid5(uuid.NAMESPACE_DNS, f"analytics-episode-{key}-{n}"))
        for n in range(1, show["episodes"] + 1)
    ]

today = datetime.now(timezone.utc).date()
month_start = today.replace(day=1)
prev_month_end = month_start - timedelta(days=1)
prev_month_anchor = prev_month_end.replace(day=15)

longest_streak_start = today - timedelta(days=24)
current_streak_start = today - timedelta(days=1)

long_binge_dates = [
    today - timedelta(days=40),
    today - timedelta(days=37),
    today - timedelta(days=34),
    today - timedelta(days=31),
]

quick_finish_base = datetime.combine(today, datetime.min.time(), tzinfo=timezone.utc) + timedelta(hours=12)

def ts(day_or_dt):
    if isinstance(day_or_dt, date) and not isinstance(day_or_dt, datetime):
        return datetime.combine(day_or_dt, datetime.min.time(), tzinfo=timezone.utc).isoformat()
    return day_or_dt.isoformat()

print("BEGIN;")

print(f"""
DELETE FROM watch_events WHERE user_id = '{user_id}';
DELETE FROM user_watch_state WHERE user_id = '{user_id}';
DELETE FROM favorites WHERE user_id = '{user_id}';
DELETE FROM reviews WHERE user_id = '{user_id}';
DELETE FROM user_library WHERE user_id = '{user_id}';
DELETE FROM shows WHERE tvmaze_id BETWEEN 99101 AND 99104;
""")

for key, show in shows.items():
    print(f"""
INSERT INTO shows (id, tvmaze_id, title, overview, poster_url, tvmaze_url, first_air_date, created_at)
VALUES (
  '{show["id"]}',
  {show["tvmaze_id"]},
  '{show["title"]}',
  'Local analytics fixture',
  NULL,
  'https://www.tvmaze.com/shows/{show["tvmaze_id"]}',
  '2020-01-01',
  NOW()
);
INSERT INTO seasons (id, show_id, season_number, name)
VALUES ('{season_ids[key]}', '{show["id"]}', 1, 'Season 1');
""")

    for index, episode_id in enumerate(episode_ids[key], start=1):
        print(f"""
INSERT INTO episodes (id, season_id, episode_number, title, air_date)
VALUES ('{episode_id}', '{season_ids[key]}', {index}, 'Episode {index}', '2020-01-{index:02d}');
""")

    print(f"""
INSERT INTO user_library (id, user_id, show_id, library_status, added_at)
VALUES (
  '{uuid.uuid5(uuid.NAMESPACE_DNS, f"analytics-library-{key}")}',
  '{user_id}',
  '{show["id"]}',
  '{show["library_status"]}',
  NOW()
);
""")

    if show.get("favorite"):
        print(f"""
INSERT INTO favorites (id, user_id, show_id, created_at)
VALUES (
  '{uuid.uuid5(uuid.NAMESPACE_DNS, f"analytics-favorite-{key}")}',
  '{user_id}',
  '{show["id"]}',
  NOW()
);
""")

watch_specs = []

for index, episode_id in enumerate(episode_ids["long_binge"]):
    watch_specs.append((episode_id, long_binge_dates[index], True))

watch_specs.append((episode_ids["half_done"][0], month_start + timedelta(days=2), True))
watch_specs.append((episode_ids["plan_later"][0], prev_month_anchor, False))

watch_specs.append((episode_ids["quick_favorite"][0], quick_finish_base, True))
watch_specs.append((episode_ids["quick_favorite"][1], quick_finish_base + timedelta(hours=2), True))

for offset in range(5):
    watch_specs.append(
        (episode_ids["long_binge"][0], longest_streak_start + timedelta(days=offset), False)
    )

watch_specs.append((episode_ids["half_done"][0], current_streak_start, False))
watch_specs.append((episode_ids["quick_favorite"][0], today, False))

for episode_id, when, update_state in watch_specs:
    event_id = str(uuid.uuid4())
    print(f"""
INSERT INTO watch_events (
  id, user_id, target_type, target_id, action, occurred_at, triggered_by_cascade, cascade_source_id
) VALUES (
  '{event_id}',
  '{user_id}',
  'EPISODE',
  '{episode_id}',
  'WATCHED',
  '{ts(when)}',
  FALSE,
  NULL
);
""")

    if update_state:
        print(f"""
INSERT INTO user_watch_state (user_id, target_type, target_id, watched, watched_at)
VALUES ('{user_id}', 'EPISODE', '{episode_id}', TRUE, '{ts(when)}')
ON CONFLICT (user_id, target_type, target_id) DO UPDATE SET
  watched = EXCLUDED.watched,
  watched_at = EXCLUDED.watched_at;
""")

print("COMMIT;")

PY
}

login_and_print_curls() {
  local payload token month_from year_from prev_month_from

  payload="$(printf '{"username":"%s","password":"%s"}' "$ANALYTICS_USER_USERNAME" "$ANALYTICS_USER_PASSWORD")"

  token="$(
    curl --silent --show-error --fail \
      --request POST \
      --header 'Content-Type: application/json' \
      --data "$payload" \
      "${API_BASE_URL}/api/auth/login" | python3 -c 'import json,sys; print(json.load(sys.stdin)["token"])'
  )"

  month_from="$(python3 - <<'PY'
from datetime import datetime, timezone
print(datetime.now(timezone.utc).date().replace(day=1).isoformat())
PY
)"

  year_from="$(python3 - <<'PY'
from datetime import datetime, timezone
print(datetime.now(timezone.utc).date().replace(month=1, day=1).isoformat())
PY
)"

  prev_month_from="$(python3 - <<'PY'
from datetime import datetime, timezone, timedelta
today = datetime.now(timezone.utc).date()
first = today.replace(day=1)
prev_last = first - timedelta(days=1)
print(prev_last.replace(day=1).isoformat())
PY
)"

  log "Backend is up. Example analytics requests:"
  cat <<EOF

export TOKEN='${token}'

# plan-to-watch-count -> expect count: 1
curl -s -H "Authorization: Bearer \$TOKEN" \\
  "${API_BASE_URL}/api/analytics/plan-to-watch-count" | python3 -m json.tool

# library-completion -> expect overall ~70% (7/10 episodes)
curl -s -H "Authorization: Bearer \$TOKEN" \\
  "${API_BASE_URL}/api/analytics/library-completion" | python3 -m json.tool

# favorites -> expect 1 show (Analytics Quick Favorite)
curl -s -H "Authorization: Bearer \$TOKEN" \\
  "${API_BASE_URL}/api/analytics/favorites" | python3 -m json.tool

# watch-streaks -> expect currentStreakDays: 2, longestStreakDays: 5
curl -s -H "Authorization: Bearer \$TOKEN" \\
  "${API_BASE_URL}/api/analytics/watch-streaks" | python3 -m json.tool

# totals -> expect episodes >= 13 (includes streak + completion fixture events)
curl -s -H "Authorization: Bearer \$TOKEN" \\
  "${API_BASE_URL}/api/analytics/totals" | python3 -m json.tool

# longest-to-watch -> Long Binge first, Half Done second, Quick Favorite last (shortest span)
curl -s -H "Authorization: Bearer \$TOKEN" \\
  "${API_BASE_URL}/api/analytics/longest-to-watch" | python3 -m json.tool

# watch-counts current month -> includes half_done ep1 + quick favorite episodes + streak day events in month
curl -s -H "Authorization: Bearer \$TOKEN" \\
  "${API_BASE_URL}/api/analytics/watch-counts?period=MONTH&from=${month_from}" | python3 -m json.tool

# watch-counts previous month -> includes long-binge completion dates + plan_later padding event
curl -s -H "Authorization: Bearer \$TOKEN" \\
  "${API_BASE_URL}/api/analytics/watch-counts?period=MONTH&from=${prev_month_from}" | python3 -m json.tool

# watch-counts current year
curl -s -H "Authorization: Bearer \$TOKEN" \\
  "${API_BASE_URL}/api/analytics/watch-counts?period=YEAR&from=${year_from}" | python3 -m json.tool

EOF
}

print_fixture_summary() {
  log "Analytics fixture seeded (idempotent; re-run safe)."
  log "Credentials:"
  log "  Username: ${ANALYTICS_USER_USERNAME}"
  log "  Password: ${ANALYTICS_USER_PASSWORD}"
  cat <<'EOF'
Expected highlights:
  plan-to-watch-count     -> 1 ("Analytics Plan Later")
  library-completion      -> ~70% overall (7/10 episodes watched)
                            Half Done 50%, Long Binge & Quick Favorite 100%, Plan Later 0%
  favorites               -> 1 explicit favorite ("Analytics Quick Favorite")
  watch-streaks             -> currentStreakDays: 2, longestStreakDays: 5
  longest-to-watch          -> "Analytics Long Binge" first (~20 days), "Analytics Half Done" second, "Analytics Quick Favorite" last (~same day)
  totals                    -> multiple EPISODE WATCHED rows (fixture logs overlap by design)
  watch-counts (MONTH)      -> current-month activity only; compare with previous-month anchor date

Shows (tvmaze_id 99101-99104) are removed and recreated on each run for this user.
Start backend with ./gradlew bootRun, then use the curl block above when available.
EOF
}

main() {
  command -v python3 >/dev/null 2>&1 || die "python3 is required."

  wait_for_postgres
  log "Ensuring analytics test user exists."
  ensure_analytics_user
  log "Seeding analytics catalog, library, watch history, and favorites."
  seed_analytics_data
  print_fixture_summary

  if backend_is_up; then
    login_and_print_curls
  else
    log "Backend not running at ${API_BASE_URL}; seed complete without login curls."
    log "Start the backend, then re-run this script to print ready-to-run curl examples."
  fi
}

main "$@"
