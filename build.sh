#!/usr/bin/env bash
# Build CallCard Dialer without Gradle.
# Needs: JDK 17, Android SDK with build-tools 34.0.0 and platform android-34.
# Usage: ./build.sh   ->  produces CallCardDialer.apk
set -e

SDK="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-$HOME/Android/Sdk}}"
BT="$SDK/build-tools/34.0.0"
PLAT="$SDK/platforms/android-34/android.jar"
OUT="build"

rm -rf "$OUT"
mkdir -p "$OUT/gen" "$OUT/obj" "$OUT/dex"

echo "==> resources"
"$BT/aapt2" compile --dir res -o "$OUT/res.zip"
"$BT/aapt2" link -o "$OUT/base.apk" -I "$PLAT" \
  --manifest AndroidManifest.xml -R "$OUT/res.zip" --java "$OUT/gen" --auto-add-overlay

echo "==> java"
find src "$OUT/gen" -name "*.java" > "$OUT/sources.txt"
javac -source 11 -target 11 -encoding UTF-8 -cp "$PLAT" -d "$OUT/obj" @"$OUT/sources.txt"

echo "==> dex"
find "$OUT/obj" -name "*.class" > "$OUT/classes.txt"
"$BT/d8" --min-api 24 --lib "$PLAT" --output "$OUT/dex" @"$OUT/classes.txt"

echo "==> package"
cp "$OUT/base.apk" "$OUT/app.apk"
( cd "$OUT/dex" && zip -q "../app.apk" classes.dex )

echo "==> align + sign"
"$BT/zipalign" -p -f 4 "$OUT/app.apk" "$OUT/app-aligned.apk"
if [ ! -f debug.keystore ]; then
  keytool -genkeypair -v -keystore debug.keystore -storepass android -keypass android \
    -alias androiddebugkey -keyalg RSA -keysize 2048 -validity 10000 \
    -dname "CN=CallCard Dialer, OU=Android, O=CallCard, C=IN"
fi
"$BT/apksigner" sign --ks debug.keystore --ks-pass pass:android --key-pass pass:android \
  --out CallCardDialer.apk "$OUT/app-aligned.apk"

echo "==> done: $(pwd)/CallCardDialer.apk"
