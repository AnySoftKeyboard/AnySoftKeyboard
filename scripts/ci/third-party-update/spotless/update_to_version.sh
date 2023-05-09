#!/usr/bin/env bash
set -e

IFS=' ' read -r -a LATEST_VERSIONS <<< "$1"

LATEST_VERSION_PLUGIN="${LATEST_VERSIONS[0]}"
LATEST_VERSION_JAVA_FORMAT="${LATEST_VERSIONS[1]}"

source scripts/ci/third-party-update/version_grep_regex.sh

sed "s/'com.diffplug.spotless:spotless-plugin-gradle:${GREP_VERSION_CLASSES}'/'com.diffplug.spotless:spotless-plugin-gradle:${LATEST_VERSION_PLUGIN}'/g" \
  build.gradle > /tmp/output.file
cp /tmp/output.file build.gradle
cat build.gradle

sed "s/googleJavaFormat('${GREP_VERSION_CLASSES}')/googleJavaFormat('${LATEST_VERSION_JAVA_FORMAT}')/g" \
  gradle/spotless.gradle > /tmp/output.file
cp /tmp/output.file gradle/spotless.gradle
cat gradle/spotless.gradle

./gradlew spotlessApply
