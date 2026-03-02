#!/usr/bin/env bash
set -euo pipefail

UBUNTU_USER="${UBUNTU_USER:-ubuntu}"
UBUNTU_HOME="/home/${UBUNTU_USER}"
UBUNTU_UID="$(id -u "${UBUNTU_USER}")"
USER_RUNTIME_DIR="/run/user/${UBUNTU_UID}"

CONTAINER_NAME="${CONTAINER_NAME:-bizkit-be}"
UNIT_NAME="container-${CONTAINER_NAME}.service"
LEGACY_SERVICES="${LEGACY_SERVICES:-bizkit-be.service,bizkit-backend.service}"

echo "[stop-be-container] stopping ${CONTAINER_NAME}"

for service in ${LEGACY_SERVICES//,/ }; do
  systemctl stop "${service}" || true
done

for cmd in podman runuser systemctl; do
  if ! command -v "${cmd}" >/dev/null 2>&1; then
    echo "[stop-be-container] missing command: ${cmd}. skip stop." >&2
    exit 0
  fi
done

if [[ ! -d "${USER_RUNTIME_DIR}" ]]; then
  echo "[stop-be-container] user runtime dir not found: ${USER_RUNTIME_DIR}. skip stop." >&2
  exit 0
fi

as_ubuntu() {
  runuser -u "${UBUNTU_USER}" -- env \
    HOME="${UBUNTU_HOME}" \
    XDG_RUNTIME_DIR="${USER_RUNTIME_DIR}" \
    DBUS_SESSION_BUS_ADDRESS="unix:path=${USER_RUNTIME_DIR}/bus" \
    "$@"
}

as_ubuntu systemctl --user disable --now "${UNIT_NAME}" >/dev/null 2>&1 || true

if as_ubuntu podman ps -a --format "{{.Names}}" | grep -Fxq "${CONTAINER_NAME}"; then
  as_ubuntu podman rm -f "${CONTAINER_NAME}" || true
  echo "[stop-be-container] container removed: ${CONTAINER_NAME}"
else
  echo "[stop-be-container] container not found: ${CONTAINER_NAME}"
fi
