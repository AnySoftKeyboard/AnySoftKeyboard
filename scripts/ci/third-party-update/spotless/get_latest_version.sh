#!/usr/bin/env bash
set -e

source scripts/ci/third-party-update/version_grep_regex.sh

LATEST_VERSION_PLUGIN=$(./scripts/ci/third-party-update/get_latest_maven_artifact_version.sh "https://plugins.gradle.org/m2" "com.diffplug.spotless" "spotless-plugin-gradle")
LATEST_VERSION_JAVA_FORMAT=$(./scripts/ci/third-party-update/get_latest_github_version.sh "google/google-java-format")

echo "$LATEST_VERSION_PLUGIN $LATEST_VERSION_JAVA_FORMAT"
