#!/usr/bin/env bash
set -euo pipefail

BASE="/home/ubuntu"
ARTIFACT_DIR="${BASE}/artifact/be"

# GitHub Actions가 번들에 심어주는 값
source "${BASE}/codedeploy/be/env.sh"

mkdir -p "${ARTIFACT_DIR}"

echo "[codedeploy][be] download jar -> caro-be-${RELEASE_ID}.jar"
curl -fL "${JAR_URL}" -o "${ARTIFACT_DIR}/caro-be-${RELEASE_ID}.jar"

bash "${BASE}/deploy-be.sh" "${RELEASE_ID}"
