#!/usr/bin/env bash
set -e

source scripts/ci/third-party-update/version_grep_regex.sh

LATEST_VERSION=$(./scripts/ci/third-party-update/get_latest_github_version.sh "robolectric/robolectric")

echo "$LATEST_VERSION"
