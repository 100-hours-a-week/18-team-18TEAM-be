#!/usr/bin/env bash
set -euo pipefail

REV_DIR="$(pwd)"

if [[ -f "${REV_DIR}/codedeploy/be/env.sh" ]]; then
  # shellcheck disable=SC1091
  source "${REV_DIR}/codedeploy/be/env.sh"
fi

: "${RELEASE_ID:?RELEASE_ID is required}"

chmod +x "${REV_DIR}/deploy-be.sh"
"${REV_DIR}/deploy-be.sh" "${RELEASE_ID}"

