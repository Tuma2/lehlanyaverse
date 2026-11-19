#!/usr/bin/env bash
# Replace the CV on lehlanyaverse.fyi in one command.
#
#   ./scripts/update-cv.sh ~/Downloads/CV.pdf            # copy, date, commit, (ask, then) push to main
#   ./scripts/update-cv.sh ~/Downloads/CV.pdf --no-push  # copy, date, commit only
#
# The CV is public: upload the web version, without your phone number or references.
# Works in Git Bash on Windows, Linux and macOS (no GNU-only flags).
set -euo pipefail

CV_DEST="site/src/jsMain/resources/public/cv/Sebabatso_Moleele_CV.pdf"
INTEL="site/src/jsMain/kotlin/fyi/lehlanyaverse/data/Intel.kt"
MAX_BYTES=$((2 * 1024 * 1024))
REPO_HTTPS="https://github.com/Tuma2/lehlanyaverse.git"

die() { printf '✗ %s\n' "$*" >&2; exit 1; }

usage() { printf 'Usage: %s <path/to/cv.pdf> [--no-push]\n' "$0" >&2; exit 2; }

SRC=""
PUSH=1
for arg in "$@"; do
  case "$arg" in
    --no-push) PUSH=0 ;;
    -h|--help) usage ;;
    -*) die "Unknown option: $arg" ;;
    *) [ -z "$SRC" ] || usage; SRC="$arg" ;;
  esac
done
[ -n "$SRC" ] || usage

# Resolve the source before leaving the caller's directory
[ -f "$SRC" ] || die "File not found: $SRC"
SRC="$(cd "$(dirname "$SRC")" && pwd)/$(basename "$SRC")"

ROOT="$(git rev-parse --show-toplevel 2>/dev/null)" || die "Run this inside the lehlanyaverse repository"
cd "$ROOT"
[ -f "$INTEL" ] || die "Can't find $INTEL: is this the lehlanyaverse repo?"

# 1. A real PDF, under 2 MB
[ "$(head -c 5 "$SRC")" = "%PDF-" ] || die "$SRC is not a PDF (it doesn't start with %PDF-). Export the CV as PDF and try again."
BYTES=$(wc -c < "$SRC" | tr -d ' ')
[ "$BYTES" -gt 0 ] || die "$SRC is empty"
[ "$BYTES" -lt "$MAX_BYTES" ] || die "$SRC is $((BYTES / 1024)) KB: keep the CV under 2 MB (compress or export at lower image quality)"

# 2. Copy into place (skip if the caller passed the destination itself)
if [ "$SRC" != "$ROOT/$CV_DEST" ]; then
  cp "$SRC" "$CV_DEST"
fi

# 3. cv.updated = today (only inside the CvFile block)
TODAY="$(date +%Y-%m-%d)"
TMP="$(mktemp)"
awk -v today="$TODAY" '
  /CvFile\(/ { in_cv = 1 }
  in_cv && /updated = "/ { sub(/updated = "[^"]*"/, "updated = \"" today "\""); done = 1 }
  in_cv && /^[[:space:]]*\)/ { in_cv = 0 }
  { print }
  END { if (!done) exit 3 }
' "$INTEL" > "$TMP" || { rm -f "$TMP"; die "Couldn't find cv.updated in $INTEL"; }
cat "$TMP" > "$INTEL"   # cat, not mv: keeps the file's mode and line endings
rm -f "$TMP"
grep -q "updated = \"$TODAY\"" "$INTEL" || die "cv.updated was not set to $TODAY"

# 4. Commit just these two files
git add -- "$CV_DEST" "$INTEL"
if git diff --cached --quiet -- "$CV_DEST" "$INTEL"; then
  echo "Nothing changed: that CV is already live with today's date."
  exit 0
fi
git commit --quiet -m "chore(cv): update CV $TODAY" -- "$CV_DEST" "$INTEL"
echo "✓ Committed: chore(cv): update CV $TODAY ($((BYTES / 1024)) KB)"

# 5. Push to main (deploys). Only from main, and only after a yes.
BRANCH="$(git rev-parse --abbrev-ref HEAD)"
if [ "$PUSH" -eq 0 ]; then
  echo "--no-push: not pushed. Push later with: git push origin $BRANCH"
elif [ "$BRANCH" != "main" ]; then
  echo "Not on main ($BRANCH): skipped push. Merge into main to deploy."
else
  printf 'Push to main and deploy? [y/N] '
  read -r answer || answer=""
  case "$answer" in
    y|Y|yes|YES)
      if ! git push origin main 2>/dev/null; then
        echo "SSH push failed; trying HTTPS via the GitHub CLI…"
        git -c credential.helper='!gh auth git-credential' push "$REPO_HTTPS" main \
          || die "Push failed. The commit is saved locally; push it with: git push origin main"
        git fetch --quiet origin main 2>/dev/null || true
      fi
      echo "✓ Pushed to main"
      ;;
    *) echo "Not pushed. Push later with: git push origin main" ;;
  esac
fi

cat <<EOF

Share these links (they never change):
  https://lehlanyaverse.fyi/cv       (redirects to the PDF: use this in emails and applications)
  https://lehlanyaverse.fyi/cv.pdf   (the PDF itself)

Once pushed, the deploy takes a few minutes (GitHub → Actions → Deploy).
EOF
