#!/bin/bash

# Generate a diff report of all localization changes made by Crowdin
echo "Generating localization changes report..."

LLM_API_KEY="$1"
if [ -z "$LLM_API_KEY" ]; then
  echo "Error: LLM_API_KEY is not set or empty. Please provide your Gemini API key as the first argument."
  exit 1
fi

# Create a diff of all localization files that were modified
git diff --cached --name-only -- "**/values-*/strings.xml" > /tmp/changed_localization_files.txt || true

if [ -s /tmp/changed_localization_files.txt ]; then
  echo "Found changes in localization files:"
  cat /tmp/changed_localization_files.txt
  
  git diff --cached -- "**/values-*/strings.xml" > /tmp/localization_diff.patch
  
  bazel run //js/localization_tools -- diffReport -i /tmp/localization_diff.patch -o /tmp/localization_changes.xml
  
  echo "=== LOCALIZATION CHANGES REPORT ==="
  cat /tmp/localization_changes.xml
  echo "=== END REPORT ==="

  echo "Verifying translations with AI:"
  bazel run //js/ai -- translationsVerification --gemini-api-key "$1" --diff-file /tmp/localization_changes.xml
else
  echo "No localization files were changed in this workflow run"
fi 