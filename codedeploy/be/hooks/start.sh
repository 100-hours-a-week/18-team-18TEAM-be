#!/usr/bin/env bash
set -euo pipefail

# start.sh가 있는 위치: .../deployment-archive/codedeploy/be/hooks
HOOK_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# 번들 루트(= deployment-archive)
REV_DIR="$(cd "${HOOK_DIR}/../../.." && pwd)"

echo "[start.sh] HOOK_DIR=${HOOK_DIR}"
echo "[start.sh] REV_DIR=${REV_DIR}"
ls -al "${REV_DIR}/codedeploy/be" || true

# env 주입 파일 로드
if [[ -f "${REV_DIR}/codedeploy/be/env.sh" ]]; then
  # shellcheck disable=SC1091
  source "${REV_DIR}/codedeploy/be/env.sh"
fi

: "${RELEASE_ID:?RELEASE_ID is required}"

chmod +x "${REV_DIR}/deploy-be.sh"
exec "${REV_DIR}/deploy-be.sh" "${RELEASE_ID}"

