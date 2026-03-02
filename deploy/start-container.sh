#!/usr/bin/env bash
set -euo pipefail

HOOK_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "[start-be-container] HOOK_DIR=${HOOK_DIR}"

if [[ -f "${HOOK_DIR}/env.sh" ]]; then
  # shellcheck disable=SC1091
  source "${HOOK_DIR}/env.sh"
else
  echo "[start-be-container] env.sh not found in ${HOOK_DIR}" >&2
  exit 11
fi

: "${RELEASE_ID:?RELEASE_ID is required}"
: "${APP_STAGE:?APP_STAGE is required}"
: "${IMAGE_URI:?IMAGE_URI is required}"

export RELEASE_ID
export APP_STAGE
export IMAGE_URI
export HEALTH_CHECK_URL="${HEALTH_CHECK_URL:-http://127.0.0.1:8080/actuator/health}"
export URL="${URL:-${HEALTH_CHECK_URL}}"
export ENV_FILE="${ENV_FILE:-/home/ubuntu/.env-be}"
export CONTAINER_NAME="${CONTAINER_NAME:-bizkit-be}"
export HOST_PORT="${HOST_PORT:-8080}"
export CONTAINER_PORT="${CONTAINER_PORT:-8080}"
export LEGACY_SERVICES="${LEGACY_SERVICES:-bizkit-be.service,bizkit-backend.service}"

chmod +x "${HOOK_DIR}/deploy-be-container.sh"
exec "${HOOK_DIR}/deploy-be-container.sh"
