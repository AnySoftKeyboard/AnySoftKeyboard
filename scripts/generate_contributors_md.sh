#!/bin/bash

TEMP_CONT_MD_FILE="$(mktemp)"

echo "# Contributors" > "$TEMP_CONT_MD_FILE"
echo "" >> "$TEMP_CONT_MD_FILE"
echo "Thank you for the fine contributors:" >> "$TEMP_CONT_MD_FILE"
echo "" >> "$TEMP_CONT_MD_FILE"
curl https://api.github.com/repos/AnySoftKeyboard/AnySoftKeyboard/contributors\?per_page\=200\&anon\=0 \
  | jq -r '.[] | "1. [\(.login)](https://github.com/\(.login)) (\(.contributions))"' \
  | grep -v anysoftkeyboard-bot \
  >> "$TEMP_CONT_MD_FILE"

cp "$TEMP_CONT_MD_FILE" CONTRIBUTORS.md