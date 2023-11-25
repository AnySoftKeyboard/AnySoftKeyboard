#!/usr/bin/env bash
set -e

IFS=' ' read -r -a LATEST_VERSIONS <<< "$1"

LATEST_VERSION_PLUGIN="${LATEST_VERSIONS[0]}"
LATEST_VERSION_TOOL="${LATEST_VERSIONS[1]}"

source scripts/ci/third-party-update/version_grep_regex.sh

sed "s/'net.ltgt.gradle:gradle-errorprone-plugin:${GREP_VERSION_CLASSES}'/'net.ltgt.gradle:gradle-errorprone-plugin:${LATEST_VERSION_PLUGIN}'/g" \
  build.gradle > /tmp/output.file
cp /tmp/output.file build.gradle
cat build.gradle
sed "s/'net.ltgt.gradle:gradle-errorprone-plugin:${GREP_VERSION_CLASSES}'/'net.ltgt.gradle:gradle-errorprone-plugin:${LATEST_VERSION_PLUGIN}'/g" \
  gradle/errorprone.gradle > /tmp/output.file
cp /tmp/output.file gradle/errorprone.gradle
cat gradle/errorprone.gradle

sed "s/'com.google.errorprone:error_prone_core:${GREP_VERSION_CLASSES}'/'com.google.errorprone:error_prone_core:${LATEST_VERSION_TOOL}'/g" \
  gradle/errorprone.gradle > /tmp/output.file
cp /tmp/output.file gradle/errorprone.gradle
cat gradle/errorprone.gradle

./scripts/error-prone-auto-patch.sh
