#!/usr/bin/env bash

REF="${1}"
LOCAL_REF="$(echo refs/heads/release-branch-addons-v4.0-r1 | cut -d'/' -f 3)"

echo "Creating merge commit from ${REF}, as local ${LOCAL_REF}"
git fetch origin "${REF}"
git merge "${LOCAL_REF}"
