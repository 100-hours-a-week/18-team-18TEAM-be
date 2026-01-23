#!/usr/bin/env bash
set -euo pipefail

BASE="/home/ubuntu"

SERVICE_UNIT="bizkit-backend.service"
ENV_FILE="${BASE}/.env-be"

ARTIFACT_DIR="${BASE}/artifact/be"
BACKUP_DIR="${BASE}/backup/be"

CURRENT_FILE="${ARTIFACT_DIR}/.current_version"

RELEASE_ID="${1:-}"
JAR_PATH="${ARTIFACT_DIR}/bizkit-be-${RELEASE_ID}.jar"

HEALTH_URL="${HEALTH_URL:-http://127.0.0.1:8080/actuator/health}"

if [[ -z "${RELEASE_ID}" ]]; then
  echo "[deploy-be] missing RELEASE_ID. usage: deploy-be.sh <version>" >&2
  exit 2
fi

if [[ ! -f "${JAR_PATH}" ]]; then
  echo "[deploy-be] jar not found: ${JAR_PATH}" >&2
  exit 3
fi

if [[ ! -f "${ENV_FILE}" ]]; then
  echo "[deploy-be] env not found: ${ENV_FILE}" >&2
  exit 4
fi

mkdir -p "${BACKUP_DIR}"

# --- backup current (if exists) ---
PREV_ID=""
if [[ -f "${CURRENT_FILE}" ]]; then
  PREV_ID="$(cat "${CURRENT_FILE}" | tr -d '\r' | xargs || true)"
fi

if [[ -n "${PREV_ID}" && -f "${ARTIFACT_DIR}/bizkit-be-${PREV_ID}.jar" ]]; then
  cp -f "${ARTIFACT_DIR}/bizkit-be-${PREV_ID}.jar" "${BACKUP_DIR}/bizkit-be-${PREV_ID}.jar"
  cp -f "${ENV_FILE}" "${BACKUP_DIR}/bizkit-be-${PREV_ID}.env"
  echo "[deploy-be] backup prev=${PREV_ID} -> ${BACKUP_DIR}"
else
  echo "[deploy-be] no previous version to backup (first deploy?)"
fi

# --- apply systemd override to point ExecStart at this versioned jar ---
OVERRIDE_DIR="/etc/systemd/system/${SERVICE_UNIT}.d"
sudo mkdir -p "${OVERRIDE_DIR}"

sudo tee "${OVERRIDE_DIR}/override.conf" >/dev/null <<EOF
[Service]
EnvironmentFile=-${ENV_FILE}
ExecStart=
ExecStart=/usr/bin/java -XX:+ExitOnOutOfMemoryError -jar ${JAR_PATH} --server.port=8080 --server.address=0.0.0.0
EOF

sudo systemctl daemon-reload
sudo systemctl restart "${SERVICE_UNIT}"

# --- health check (up to 30s) ---
ok=0
for i in $(seq 1 30); do
  if curl -fsS --max-time 2 "${HEALTH_URL}" >/dev/null; then
    ok=1
    break
  fi
  sleep 1
done

if [[ "${ok}" -eq 1 ]]; then
  echo "${RELEASE_ID}" | sudo tee "${CURRENT_FILE}" >/dev/null
  echo "[deploy-be] SUCCESS version=${RELEASE_ID}"
  exit 0
fi

# --- auto rollback to prev (so CodeDeploy fails but service is restored) ---
echo "[deploy-be] FAILED healthcheck. try rollback to prev=${PREV_ID}" >&2

if [[ -n "${PREV_ID}" && -f "${ARTIFACT_DIR}/bizkit-be-${PREV_ID}.jar" ]]; then
  PREV_JAR="${ARTIFACT_DIR}/bizkit-be-${PREV_ID}.jar"
  sudo tee "${OVERRIDE_DIR}/override.conf" >/dev/null <<EOF
[Service]
EnvironmentFile=-${ENV_FILE}
ExecStart=
ExecStart=/usr/bin/java -XX:+ExitOnOutOfMemoryError -jar ${PREV_JAR} --server.port=8080 --server.address=0.0.0.0
EOF

  sudo systemctl daemon-reload
  sudo systemctl restart "${SERVICE_UNIT}"

  curl -fsS --max-time 2 "${HEALTH_URL}" >/dev/null && \
    echo "[deploy-be] ROLLBACK OK -> ${PREV_ID}" >&2 || \
    echo "[deploy-be] ROLLBACK ALSO FAILED" >&2
fi

exit 1
