#!/usr/bin/env bash
# Pushes the build log tail (and the APK when present) to the build-logs branch.
# Runs in GitHub Actions after the Gradle build step (if: always).
set -u

WORKSPACE="${GITHUB_WORKSPACE:-.}"
LOG="${BUILD_LOG:-/tmp/build.log}"
APK="$WORKSPACE/app/build/outputs/apk/debug/app-debug.apk"
REPO="https://x-access-token:${GITHUB_TOKEN}@github.com/dmj74/Ai.git"

echo "== ci-log-push start =="

rm -rf /tmp/logrepo
mkdir -p /tmp/logrepo
cd /tmp/logrepo
git init -q -b main 2>/dev/null || git init -q
git config user.email "ci@titanali"
git config user.name "titanali-ci"

if [ -f "$LOG" ]; then
  tail -c 2000000 "$LOG" > build.log
  echo "log captured: $(du -h build.log | cut -f1)"
else
  echo "WARNING: build log not found at $LOG"
fi

if [ -f "$APK" ]; then
  cp "$APK" titanali.apk
  echo "APK captured: $(du -h titanali.apk | cut -f1)"
else
  echo "no APK (build failed?)"
fi

git add -A
if git diff --cached --quiet; then
  echo "nothing to commit"
else
  git commit -q -m "build-$(date +%s)"
fi

git remote add origin "$REPO" 2>/dev/null || git remote set-url origin "$REPO"
git push -f origin main:build-logs
rc=$?
echo "== push exit code: $rc =="
exit 0
