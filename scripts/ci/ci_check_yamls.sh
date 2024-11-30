#!/usr/bin/env bash
set -e

yamllint -c config/yamllint.yml -f github .

if command -v go; then
  go install github.com/rhysd/actionlint/cmd/actionlint@latest
  actionlint -no-color -ignore SC2215
fi
