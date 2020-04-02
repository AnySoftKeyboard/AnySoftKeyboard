#!/usr/bin/env bash
set -e
set -x

REF="${1}"
LOCAL_REF="$(echo refs/heads/release-branch-addons-v4.0-r1 | cut -d'/' -f 3)"

echo "setting git user details"
echo "email"
git config --global user.email "ask@evendanan.net"
echo "name"
git config --global user.name "Polyglot"
  
echo "Fetching from ${REF}, as local ${LOCAL_REF}:"
git fetch origin "${REF}"
echo "Merging:"
git merge "origin/${LOCAL_REF}"
