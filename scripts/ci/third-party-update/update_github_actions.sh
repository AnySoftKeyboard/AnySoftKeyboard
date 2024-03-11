#!/usr/bin/env bash
set -e

GREP_VERSION_CLASSES="[[:digit:]]\\{1,\\}[.][[:digit:]]\\{1,\\}[[:punct:][:alnum:]]*"

if [[ -z "$(git config --list | grep user.name)" ]]; then
  git config user.email "ask@evendanan.net"
  git config user.name "Polyglot"
fi

function do_update() {
  local MAVEN_URL="$1"
  local GROUP_ID="$2"
  local ARTIFACT_ID="$3"

  local LATEST_VERSION
  LATEST_VERSION=$(./scripts/ci/third-party-update/get_latest_maven_artifact_version.sh "${MAVEN_URL}" "${GROUP_ID}" "${ARTIFACT_ID}")
  if [[ -z "$LATEST_VERSION" ]]; then
    echo "Failed to load versions metadata for '${GROUP_ID}:${ARTIFACT_ID}'."
  else
    echo -n "Found version '${LATEST_VERSION}' for '${GROUP_ID}:${ARTIFACT_ID}'. Checking if bump is needed..."
    for f in $(find . -name 'build.gradle'); do
      sed "s/${GROUP_ID}:${ARTIFACT_ID}:${GREP_VERSION_CLASSES}'/${GROUP_ID}:${ARTIFACT_ID}:${LATEST_VERSION}'/g" "$f" > /tmp/output.file
      cp /tmp/output.file "$f"
    done

    
  fi
}

declare -a used_actions

mapfile -t used_actions < <(find .github/workflows/ -name "*.yml" -type f -exec grep -oP '\s+uses: \K[\w\-]+/[\w\-]+(?=\@)' {} \; | sort | uniq)

echo "found ${#used_actions[@]} used github actions:"
for action in "${used_actions[@]}"
do
  echo " - $action"
  latest_version="$(./scripts/ci/third-party-update/get_latest_github_version.sh $action)"
  echo "   latest version $latest_version"
  for f in $(find .github/workflows/ -name "*.yml" -type f); do
    python3 scripts/file_text_replace_in_place.py "${f}" "${action}@.*" "${action}@v${latest_version}"
  done

  if [[ -n $(git status -s) ]]; then
    echo "   $action Bumped."
    git add .
    git commit -m "Bumping $action to version ${latest_version}."
  else
    echo " Already on latest."
  fi

done
