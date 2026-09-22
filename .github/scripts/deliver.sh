#!/usr/bin/env bash
# Deliver build log (+ APK when present) to channels readable by the Arena agent.
# Channel 1 (always works): build error lines emitted as GitHub annotations.
# Channel 2: git push to build-logs branch (log + apk) via GITHUB_TOKEN.
# Channel 3: GitHub API gist (log) + contents API (apk).
# Channel 4: paste services as last resort.
set +e

# --- 1) Surface the actual build errors as annotations ---
grep -aE "^e: |error:|ERROR:|FAILURE:|What went wrong|Execution failed|Could not |Unresolved|Caused by" /tmp/build.log 2>/dev/null \
  | head -25 \
  | while IFS= read -r l; do
      echo "::error file=build.log::${l:0:300}"
    done

# --- 2) Gather files for delivery ---
D=$(mktemp -d)
cd "$D"
git init -q -b main 2>/dev/null || git init -q
git config user.email "ci@titanali.app"
git config user.name "Titanali CI"
tail -c 2000000 /tmp/build.log > build.log 2>/dev/null
APK="$GITHUB_WORKSPACE/app/build/outputs/apk/debug/app-debug.apk"
[ -f "$APK" ] && cp "$APK" titanali.apk
git add -A
git commit -q -m "build $(date +%s)"
O=/tmp/d.out
: > "$O"

case "$GITHUB_TOKEN" in
  ghs_*) tshape="ghs(len=${#GITHUB_TOKEN})" ;;
  github_pat_*) tshape="pat(len=${#GITHUB_TOKEN})" ;;
  "") tshape="EMPTY" ;;
  *) tshape="other(len=${#GITHUB_TOKEN})" ;;
esac
echo "token shape: $tshape" >> "$O"

echo "== git push ==" >> "$O"
git remote add origin "https://x-access-token:${GITHUB_TOKEN}@github.com/dmj74/Ai.git"
git push -f origin main:build-logs 2>&1 | head -8 | tee -a "$O"
echo "push exit: ${PIPESTATUS[0]}" >> "$O"

code=$(curl -s -o /tmp/p.json -w "%{http_code}" -H "Authorization: token ${GITHUB_TOKEN}" https://api.github.com/repos/dmj74/Ai 2>/dev/null)
echo "api probe: ${code:-000}" >> "$O"
head -c 200 /tmp/p.json 2>/dev/null >> "$O"
echo "" >> "$O"

url=""
if [ "$code" = "200" ]; then
  jq -n --arg d "$(cat build.log)" '{description:"titanali build log",public:true,files:{"build.log":{"content":$d}}}' > g.json 2>/dev/null
  curl -s -H "Authorization: token ${GITHUB_TOKEN}" -H "Accept: application/vnd.github+json" -d @g.json https://api.github.com/gists > g.out 2>/dev/null
  url=$(grep -oE '"html_url": *"[^"]+"' g.out 2>/dev/null | head -1 | cut -d'"' -f4)
  echo "gist: ${url:-FAILED}" >> "$O"
  if [ -f titanali.apk ]; then
    base64 -w0 titanali.apk > apk.b64
    jq -n --rawfile c apk.b64 --arg message "titanali apk" '{message:$message, content:$c, encoding:"base64"}' > a.json 2>/dev/null
    acode=$(curl -s -o a.out -w "%{http_code}" -X PUT -H "Authorization: token ${GITHUB_TOKEN}" -H "Accept: application/vnd.github+json" -d @a.json "https://api.github.com/repos/dmj74/Ai/contents/titanali.apk?ref=build-logs" 2>/dev/null)
    echo "apk contents: ${acode:-000}" >> "$O"
    head -c 200 a.out 2>/dev/null >> "$O"
    echo "" >> "$O"
  fi
fi

if [ -z "$url" ]; then
  u=$(curl -s --max-time 20 -T build.log https://paste.rs 2>/dev/null)
  case "$u" in http*) url="$u"; echo "paste.rs: $url" >> "$O";; esac
fi
if [ -z "$url" ]; then
  u=$(curl -s --max-time 20 -F 'file=@build.log' https://ix.io 2>/dev/null)
  case "$u" in http*) url="$u"; echo "ix.io: $url" >> "$O";; esac
fi
if [ -z "$url" ]; then
  u=$(curl -s --max-time 20 --data-urlencode "content@build.log" "https://dpaste.com/api/2/" 2>/dev/null)
  case "$u" in http*) url="$u"; echo "dpaste: $url" >> "$O";; esac
fi
echo "log url: ${url:-FAILED}" >> "$O"

echo "### Delivery" >> "$GITHUB_STEP_SUMMARY" 2>/dev/null
cat "$O" >> "$GITHUB_STEP_SUMMARY" 2>/dev/null
fold -w 340 "$O" 2>/dev/null | head -40 | while IFS= read -r l; do
  echo "::notice title=dl::$l"
done
echo "::notice title=log-url::${url:-FAILED}"
exit 0
