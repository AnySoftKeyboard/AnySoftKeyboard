#!/usr/bin/env bash
set -e
set -x

REF="${1}"
LOCAL_REF="$(echo "${REF}" | cut -d'/' -f 3)"
git config --global --add safe.directory "${PWD}"

echo "setting git user details"
echo "email"
git config --global user.email "ask@evendanan.net"
echo "name"
git config --global user.name "Polyglot"
  
echo "Fetching from ${REF}, as local ${LOCAL_REF}:"

if [[ $(git ls-tree -r "${REF}" --name-only deployment/halt_deployment_marker) ]]; then
  echo "halt_deployment_marker file was found. This branch can not be merged into main branch."
  exit 0
fi

git fetch origin "${REF}"
echo "Merging:"
git merge "origin/${LOCAL_REF}"
