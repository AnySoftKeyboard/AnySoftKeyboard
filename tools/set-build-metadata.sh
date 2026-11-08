#!/usr/bin/env bash
set -eu
printf 'STABLE_BRANCH %s\n' "$(git rev-parse --abbrev-ref HEAD)"
printf 'COMMIT_SHA %s\n' "$(git rev-parse HEAD)"
printf 'STABLE_REPO %s\n' "$(git config --get remote.origin.url || printf local)"
case "${CI:-}" in
  true|TRUE|1)
    ROLE="CI"
    ;;
  *)
    ROLE="developer"
    ;;
esac
printf 'ROLE %s\n' "$ROLE"
