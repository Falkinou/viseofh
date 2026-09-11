#!/bin/sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd)
REMOTE=${VISEOFH_REMOTE:-ubuntu@152.228.238.158}
SSH_KEY=${VISEOFH_SSH_KEY:-/Users/lolo/.ssh/id_ed25519}
SSH="ssh -o BatchMode=yes -i $SSH_KEY"

test -f "$ROOT_DIR/index.html"
test -f "$ROOT_DIR/odc/index.html"
test -f "$ROOT_DIR/odc/version.json"

$SSH "$REMOTE" "sudo install -d -o ubuntu -g ubuntu /opt/viseofh /opt/viseofh/www /opt/viseofh/www/odc /opt/viseofh/apks && sudo chown ubuntu:ubuntu /opt/viseofh /opt/viseofh/www /opt/viseofh/www/odc /opt/viseofh/apks"
rsync -a --delete -e "$SSH" "$ROOT_DIR/index.html" "$REMOTE:/opt/viseofh/www/"
rsync -a --delete -e "$SSH" \
  --exclude 'releases/' \
  "$ROOT_DIR/odc/" "$REMOTE:/opt/viseofh/www/odc/"
$SSH "$REMOTE" "install -d /opt/viseofh/www/odc/releases"
rsync -a -e "$SSH" \
  "$ROOT_DIR/deploy/viseofh/docker-compose.yml" \
  "$ROOT_DIR/deploy/viseofh/nginx.conf" \
  "$REMOTE:/opt/viseofh/"

$SSH "$REMOTE" "cd /opt/viseofh && docker compose config -q && docker compose up -d && docker compose ps"
