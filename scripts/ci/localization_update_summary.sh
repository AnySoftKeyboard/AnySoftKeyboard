#!/bin/bash

# Generate a diff report of all localization changes made by Crowdin
echo "Generating localization changes report..."

OUTPUT_FILE="$1"

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

  if [ -n "$OUTPUT_FILE" ]; then
    echo "Summary written to: $OUTPUT_FILE"
    echo "Localization changes were found. PR will be reviewed by AI." > "$OUTPUT_FILE"
  fi
else
  echo "No localization files were changed in this workflow run"
  if [ -n "$OUTPUT_FILE" ]; then
    echo "No localization files were changed in this workflow run" > "$OUTPUT_FILE"
  fi
fi 
