#!/bin/bash

# Generate a diff report of all localization changes made by Crowdin
echo "Generating localization changes report..."

# Create a diff of all localization files that were modified
git diff --cached --name-only HEAD~1 -- "**/values-*/strings.xml" > /tmp/changed_localization_files.txt || true

if [ -s /tmp/changed_localization_files.txt ]; then
  echo "Found changes in localization files:"
  cat /tmp/changed_localization_files.txt
  
  git diff --cached HEAD~1 -- "**/values-*/strings.xml" > /tmp/localization_diff.patch
  
  bazel run //js/localization_tools -- diffReport -i /tmp/localization_diff.patch -o /tmp/localization_changes.xml
  
  echo "=== LOCALIZATION CHANGES REPORT ==="
  cat /tmp/localization_changes.xml
  echo "=== END REPORT ==="
else
  echo "No localization files were changed in this workflow run"
fi 