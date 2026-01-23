#!/usr/bin/env bash
set -euo pipefail

systemctl stop caro-backend.service || true
