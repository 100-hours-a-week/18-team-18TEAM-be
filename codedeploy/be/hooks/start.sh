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
: "${JAR_URL:?JAR_URL is required}"

# ---- download jar to the path deploy-be.sh expects ----
ARTIFACT_DIR="/home/ubuntu/artifact/be"
JAR_PATH="${ARTIFACT_DIR}/bizkit-be-${RELEASE_ID}.jar"

sudo mkdir -p "${ARTIFACT_DIR}"

echo "[start.sh] download jar -> ${JAR_PATH}"
# curl이 없을 수도 있으니 둘 중 가능한 걸로
if command -v curl >/dev/null 2>&1; then
  curl -fsSL "${JAR_URL}" -o "/tmp/bizkit-be-${RELEASE_ID}.jar"
elif command -v wget >/dev/null 2>&1; then
  wget -qO "/tmp/bizkit-be-${RELEASE_ID}.jar" "${JAR_URL}"
else
  echo "[start.sh] neither curl nor wget is installed" >&2
  exit 10
fi

sudo mv "/tmp/bizkit-be-${RELEASE_ID}.jar" "${JAR_PATH}"
sudo chmod 0644 "${JAR_PATH}"
ls -al "${JAR_PATH}" || true

chmod +x "${REV_DIR}/deploy-be.sh"
exec "${REV_DIR}/deploy-be.sh" "${RELEASE_ID}"

