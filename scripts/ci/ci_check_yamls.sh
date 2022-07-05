#!/usr/bin/env bash
set -e

if ! command -v yamllint; then
  sudo apt-get install -y --allow-remove-essential --allow-change-held-packages yamllint
fi
yamllint -c config/yamllint.yml -f github . | tee build/yamllint.log

if command -v go; then
  go install github.com/rhysd/actionlint/cmd/actionlint@latest
  actionlint -no-color -ignore SC2215| tee build/actionlint.log
fi
