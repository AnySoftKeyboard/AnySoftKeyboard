#!/usr/bin/env bash
set -e

source scripts/ci/third-party-update/version_grep_regex.sh

LATEST_VERSION=$("${ANDROID_HOME}/cmdline-tools/latest/bin/sdkmanager" --list | grep -o "build-tools;${GREP_VERSION_CLASSES}" | grep -o "${GREP_VERSION_CLASSES}" | uniq | tail -n 1)

echo "$LATEST_VERSION"
