#!/bin/bash

# Generate a diff report of all localization changes made by Crowdin
echo "Generating localization changes report..."

LLM_API_KEY="$1"
OUTPUT_FILE="$2"

if [ -z "$LLM_API_KEY" ]; then
  echo "Error: LLM_API_KEY is not set or empty. Please provide your Gemini API key as the first argument."
  echo "Usage: $0 <gemini_api_key> [output_file]"
  exit 1
fi

# Create a diff of all localization files that were modified
git diff --cached --name-only -- "**/values-*/strings.xml" > /tmp/changed_localization_files.txt || true

if [ -s /tmp/changed_localization_files.txt ]; then
  echo "Found changes in localization files:"
  cat /tmp/changed_localization_files.txt
  
  git diff --cached -- "**/values-*/strings.xml" > /tmp/localization_diff.patch
  
  echo "Cleaning up invalid empty translations..."
  bazel run //js/localization_tools -- cleanEmptyTranslations --diff-file /tmp/localization_diff.patch
  
  # Update index with potential changes
  git add .
  
  # Regenerate diff for the report
  git diff --cached -- "**/values-*/strings.xml" > /tmp/localization_diff.patch

  bazel run //js/localization_tools -- diffReport -i /tmp/localization_diff.patch -o /tmp/localization_changes.xml
  
  echo "=== LOCALIZATION CHANGES REPORT ==="
  cat /tmp/localization_changes.xml
  echo "=== END REPORT ==="

  echo "Verifying translations with AI:"
  if [ -n "$OUTPUT_FILE" ]; then
    echo "Output will be written to: $OUTPUT_FILE"
    bazel run //js/ai -- translationsVerification --gemini-api-key "$LLM_API_KEY" --diff-file /tmp/localization_changes.xml --output-file "$OUTPUT_FILE"
  else
    bazel run //js/ai -- translationsVerification --gemini-api-key "$LLM_API_KEY" --diff-file /tmp/localization_changes.xml
  fi
else
  echo "No localization files were changed in this workflow run"
fi 