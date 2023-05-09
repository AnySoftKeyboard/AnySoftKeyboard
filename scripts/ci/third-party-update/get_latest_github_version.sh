#!/usr/bin/env bash
set -e

GITHUB_REPO="$1"

source scripts/ci/third-party-update/version_grep_regex.sh

RELEASE_TAG_NAME=$(curl --silent -u "${BOT_USERNAME}:${BOT_TOKEN}" "https://api.github.com/repos/$GITHUB_REPO/releases" \
            | jq -c -r '.[] | select(.prerelease == false) | .tag_name' \
            | grep -o "${GREP_VERSION_CLASSES}" \
            | uniq \
            | head -n 1)

echo "$RELEASE_TAG_NAME"
