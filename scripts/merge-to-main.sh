#!/usr/bin/env bash
# scripts/merge-to-main.sh
# Promotes dev -> main. Pushing main is what triggers Vercel + Render deploys.
#
# Usage:
#   ./scripts/merge-to-main.sh           # waits for CI on dev, then merges
#   ./scripts/merge-to-main.sh --local   # also runs tests locally first
#
# Requires: git, and the GitHub CLI (gh) for the CI check.

set -euo pipefail

RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; NC='\033[0m'

fail() { echo -e "${RED}✗ $1${NC}"; exit 1; }
ok()   { echo -e "${GREEN}✓ $1${NC}"; }
info() { echo -e "${YELLOW}→ $1${NC}"; }

# --- 1. Preconditions -------------------------------------------------------
[ -d .git ] || fail "Run this from the repo root."
[ -z "$(git status --porcelain)" ] || fail "Working tree is dirty. Commit or stash first."

info "Fetching latest from origin..."
git fetch origin

# --- 2. Optional local test run --------------------------------------------
if [ "${1:-}" = "--local" ]; then
  info "Running backend tests locally..."
  (cd backend && ./mvnw -B -q test) || fail "Backend tests failed."
  ok "Backend tests passed"

  info "Running frontend tests locally..."
  (cd frontend && npx ng test --watch=false --browsers=ChromeHeadless) \
    || fail "Frontend tests failed."
  ok "Frontend tests passed"
fi

# --- 3. Make sure CI is green on dev ----------------------------------------
if command -v gh >/dev/null 2>&1; then
  info "Checking CI status on origin/dev..."
  # Waits for in-progress runs; fails the script if any check fails.
  gh run watch --exit-status "$(gh run list --branch dev --limit 1 --json databaseId --jq '.[0].databaseId')" \
    || fail "CI is not green on dev. Fix it before promoting."
  ok "CI is green on dev"
else
  info "GitHub CLI not found — skipping remote CI check (install 'gh' to enable)."
fi

# --- 4. Merge dev into main --------------------------------------------------
info "Updating local branches..."
git checkout dev  && git pull origin dev
git checkout main && git pull origin main

info "Merging dev into main..."
git merge --no-ff dev -m "Release: merge dev into main $(date +%Y-%m-%d)" \
  || { git merge --abort; git checkout dev; fail "Merge conflict — resolve manually."; }

# --- 5. Push: this is the deploy trigger -------------------------------------
info "Pushing main (this triggers Vercel + Render deploys)..."
git push origin main
ok "main pushed — deploys are rolling"

git checkout dev
ok "Back on dev. Watch deploys at vercel.com and dashboard.render.com"