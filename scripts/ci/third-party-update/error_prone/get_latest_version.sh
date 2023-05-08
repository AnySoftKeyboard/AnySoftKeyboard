#!/usr/bin/env bash
set -e

source scripts/ci/third-party-update/version_grep_regex.sh

LATEST_VERSION_PLUGIN=$(./scripts/ci/third-party-update/get_latest_github_version.sh "tbroyer/gradle-errorprone-plugin")
LATEST_VERSION_TOOL=$(./scripts/ci/third-party-update/get_latest_github_version.sh "google/error-prone")

echo "$LATEST_VERSION_PLUGIN $LATEST_VERSION_TOOL"
