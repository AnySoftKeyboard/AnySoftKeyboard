#!/usr/bin/env bash
set -e

LATEST_VERSION="$1"

source scripts/ci/third-party-update/version_grep_regex.sh

sed "s/androidBuildTools[[:space:]]=[[:space:]]'${GREP_VERSION_CLASSES}'/androidBuildTools = '${LATEST_VERSION}'/g" gradle/root_all_projects_ext.gradle > /tmp/output.file
cp /tmp/output.file gradle/root_all_projects_ext.gradle
cat gradle/root_all_projects_ext.gradle
yes | "${ANDROID_HOME}/cmdline-tools/latest/bin/sdkmanager" "build-tools;${LATEST_VERSION}"
