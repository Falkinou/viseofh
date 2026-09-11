#!/bin/sh
set -eu

if [ "$#" -ne 3 ]; then
  echo "Usage: $0 APK VERSION_CODE VERSION_NAME" >&2
  exit 2
fi

APK=$1
VERSION_CODE=$2
VERSION_NAME=$3
REMOTE=${VISEOFH_REMOTE:-ubuntu@152.228.238.158}
SSH_KEY=${VISEOFH_SSH_KEY:-/Users/lolo/.ssh/id_ed25519}
SSH="ssh -o BatchMode=yes -i $SSH_KEY"

test -f "$APK"
case "$VERSION_CODE" in *[!0-9]*|'') echo "VERSION_CODE invalide" >&2; exit 2;; esac
case "$VERSION_NAME" in *[!0-9.]*|'') echo "VERSION_NAME invalide" >&2; exit 2;; esac

APK_NAME="Orange-Drone-Compagnon-$VERSION_NAME.apk"
SHA256=$(shasum -a 256 "$APK" | awk '{print $1}')
SIZE=$(wc -c < "$APK" | tr -d ' ')
REMOTE_DIR="/opt/viseofh/apks/$VERSION_NAME"
REMOTE_TMP="$REMOTE_DIR/$APK_NAME.uploading"
REMOTE_APK="$REMOTE_DIR/$APK_NAME"
MANIFEST_TMP=$(mktemp)
trap 'rm -f "$MANIFEST_TMP"' EXIT HUP INT TERM

printf '{\n  "versionCode": %s,\n  "versionName": "%s",\n  "apkUrl": "https://viseofh.fr/odc/releases/%s/%s",\n  "apkSha256": "%s",\n  "apkSizeBytes": %s\n}\n' \
  "$VERSION_CODE" "$VERSION_NAME" "$VERSION_NAME" "$APK_NAME" "$SHA256" "$SIZE" > "$MANIFEST_TMP"

$SSH "$REMOTE" "install -d '$REMOTE_DIR' /opt/viseofh/www/odc"
rsync -a --partial -e "$SSH" "$APK" "$REMOTE:$REMOTE_TMP"
$SSH "$REMOTE" "test \"\$(sha256sum '$REMOTE_TMP' | awk '{print \$1}')\" = '$SHA256' && test \"\$(wc -c < '$REMOTE_TMP')\" = '$SIZE' && mv '$REMOTE_TMP' '$REMOTE_APK'"
rsync -a -e "$SSH" "$MANIFEST_TMP" "$REMOTE:/opt/viseofh/www/odc/version.json.new"
$SSH "$REMOTE" "mv /opt/viseofh/www/odc/version.json.new /opt/viseofh/www/odc/version.json"

echo "Version $VERSION_NAME publiée : $SHA256 ($SIZE octets)"
