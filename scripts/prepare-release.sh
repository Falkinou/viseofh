#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

VERSION_NAME="$(grep -E '^val orangeDroneCompagnonVersionName' app/build.gradle.kts | sed -E 's/.*= "([^"]+)".*/\1/')"
VERSION_CODE="$(grep -E '^val orangeDroneCompagnonVersionCode' app/build.gradle.kts | sed -E 's/.*= ([0-9]+).*/\1/')"
PACKAGE_DIR="$ROOT_DIR/release-packages/odc-$VERSION_NAME"

./gradlew --no-daemon :app:packageOrangeDroneCompagnonApk :app:packageOrangeDroneCompagnonLatestApk

APK_PATH="$ROOT_DIR/dist/Orange-Drone-Compagnon.apk"
if command -v shasum >/dev/null 2>&1; then
  APK_SHA256="$(shasum -a 256 "$APK_PATH" | awk '{print $1}')"
else
  APK_SHA256="$(sha256sum "$APK_PATH" | awk '{print $1}')"
fi
APK_SIZE_BYTES="$(wc -c < "$APK_PATH" | tr -d '[:space:]')"
cat > "$ROOT_DIR/github-pages-upload/odc/version.json" <<JSON
{
  "versionCode": $VERSION_CODE,
  "versionName": "$VERSION_NAME",
  "apkUrl": "https://github.com/Falkinou/viseofh/releases/latest/download/Orange-Drone-Compagnon.apk",
  "apkSha256": "$APK_SHA256",
  "apkSizeBytes": $APK_SIZE_BYTES
}
JSON
cp "$ROOT_DIR/github-pages-upload/odc/version.json" "$ROOT_DIR/dist/odc/version.json"

rm -rf "$PACKAGE_DIR"
mkdir -p "$PACKAGE_DIR/odc" "$PACKAGE_DIR/apk" "$PACKAGE_DIR/docs"

cp "$ROOT_DIR/dist/Orange-Drone-Compagnon-$VERSION_NAME.apk" "$PACKAGE_DIR/apk/"
cp "$ROOT_DIR/dist/Orange-Drone-Compagnon.apk" "$PACKAGE_DIR/apk/"
cp -R "$ROOT_DIR/github-pages-upload/odc/." "$PACKAGE_DIR/odc/"
cp "$ROOT_DIR/docs/"*.md "$PACKAGE_DIR/docs/"

RELEASE_DIRS=()
while IFS= read -r dir; do
  RELEASE_DIRS+=("$dir")
done < <(find "$ROOT_DIR/release-packages" -maxdepth 1 -type d -name 'odc-*' | sort -V)
if (( ${#RELEASE_DIRS[@]} > 2 )); then
  for (( i=0; i<${#RELEASE_DIRS[@]}-2; i++ )); do
    rm -rf "${RELEASE_DIRS[$i]}"
  done
fi

echo "Release package prepared:"
echo "$PACKAGE_DIR"
