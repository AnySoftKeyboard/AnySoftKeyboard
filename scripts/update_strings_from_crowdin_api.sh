#!/bin/bash
set -e

TEMP_EXTRACT_FOLDER="${TMPDIR:-/tmp}/ask_crowdin/"
TEMP_OUTPUT_FOLDER="${TMPDIR:-/tmp}/ask_crowdin_file/"
TEMP_OUTPUT_FILE=all.zip

if [ -z "${CROWDIN_API}" ]; then
    echo "Could not find crowdin API environment variable at CROWDIN_API."
    exit 1
fi

rm -rf "${TEMP_EXTRACT_FOLDER}" || true
rm -rf "${TEMP_OUTPUT_FOLDER}" || true

if [ "$1" == "build" ]; then
    echo "Building translations..."
    wget --tries=5 --waitretry=5 -O export.txt "https://api.crowdin.com/api/project/anysoftkeyboard/export?key=${CROWDIN_API}"
    cat export.txt
    rm export.txt
else
    echo "Not exporting latest translations. Use 'build' argument to force build first."
fi

mkdir "${TEMP_EXTRACT_FOLDER}"
mkdir "${TEMP_OUTPUT_FOLDER}"
wget --tries=5 --waitretry=5 -O "${TEMP_OUTPUT_FOLDER}${TEMP_OUTPUT_FILE}" "https://api.crowdin.com/api/project/anysoftkeyboard/download/all.zip?key=${CROWDIN_API}"
unzip -o "${TEMP_OUTPUT_FOLDER}${TEMP_OUTPUT_FILE}" -d "${TEMP_EXTRACT_FOLDER}"

pushd "${TEMP_EXTRACT_FOLDER}" || exit 1
for f in *; do mv "$f" "values-$f"; done
popd || exit 1

APP_RES_FOLDER=ime/app/src/main/res
echo "will copy from ${TEMP_EXTRACT_FOLDER} to ${APP_RES_FOLDER}"
for f in "${TEMP_EXTRACT_FOLDER}"*; do cp -R "$f" "${APP_RES_FOLDER}"; done

echo "fixing files a bit..."
rm -rf "${APP_RES_FOLDER}/values-en-PT"
mv "${APP_RES_FOLDER}/values-es-ES/strings.xml" "${APP_RES_FOLDER}/values-es/"
rm -rf "${APP_RES_FOLDER}/values-es-AR" || true
rm -rf "${APP_RES_FOLDER}/values-es-ES" || true
mv "${APP_RES_FOLDER}/values-he/strings.xml" "${APP_RES_FOLDER}/values-iw/"
rm -rf "${APP_RES_FOLDER}/values-he" || true
mv "${APP_RES_FOLDER}/values-yi/strings.xml" "${APP_RES_FOLDER}/values-ji/"
rm -rf "${APP_RES_FOLDER}/values-yi" || true
mv "${APP_RES_FOLDER}/values-hy-AM/strings.xml" "${APP_RES_FOLDER}/values-hy/"
rm -rf "${APP_RES_FOLDER}/values-hy-AM" || true
mv "${APP_RES_FOLDER}/values-sv-SE/strings.xml" "${APP_RES_FOLDER}/values-se/"
rm -rf "${APP_RES_FOLDER}/values-sv-SE/" || true
mv "${APP_RES_FOLDER}/values-pt-PT/strings.xml" "${APP_RES_FOLDER}/values-pt/"
rm -rf "${APP_RES_FOLDER}/values-pt-PT/" || true
mv "${APP_RES_FOLDER}/values-pt-BR/strings.xml" "${APP_RES_FOLDER}/values-pt-rBR/"
rm -rf "${APP_RES_FOLDER}/values-pt-BR/" || true
mv "${APP_RES_FOLDER}/values-zh-CN/strings.xml" "${APP_RES_FOLDER}/values-zh-rCN/"
rm -rf "${APP_RES_FOLDER}/values-zh-CN/" || true
mv "${APP_RES_FOLDER}/values-tlh-AA/strings.xml" "${APP_RES_FOLDER}/values-tlh/"
rm -rf "${APP_RES_FOLDER}/values-tlh-AA" || true
mv "${APP_RES_FOLDER}/values-es-MX/strings.xml" "${APP_RES_FOLDER}/values-es-rMX/"
rm -rf "${APP_RES_FOLDER}/values-es-MX/" || true
mv "${APP_RES_FOLDER}/values-ml-IN/strings.xml" "${APP_RES_FOLDER}/values-ml-rIN/"
rm -rf "${APP_RES_FOLDER}/values-ml-IN/" || true
mv "${APP_RES_FOLDER}/values-bn-IN/strings.xml" "${APP_RES_FOLDER}/values-bn-rIN/"
rm -rf "${APP_RES_FOLDER}/values-bn-IN/" || true
mv "${APP_RES_FOLDER}/values-si-LK/strings.xml" "${APP_RES_FOLDER}/values-si-rLK/"
rm -rf "${APP_RES_FOLDER}/values-si-LK/" || true
#copying generic strings to en
cp "${APP_RES_FOLDER}/values/strings.xml" "${APP_RES_FOLDER}/values-en/strings.xml"

echo "done"
