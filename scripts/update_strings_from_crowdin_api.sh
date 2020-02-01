#!/bin/bash

TEMP_EXTRACT_FOLDER="${TMPDIR}/ask_crowdin/"
TEMP_OUTPUT_FOLDER="${TMPDIR}/ask_crowdin_file/"
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
find * -maxdepth 0 ! -path . -exec mv {} values-{} \;

popd || exit 1
APP_RES_FOLDER=ime/app/src/main/res
cp -R "${TEMP_EXTRACT_FOLDER}" "${APP_RES_FOLDER}"

#fixing files a bit
rm -rf "${APP_RES_FOLDER}/values-en-PT"
mv "${APP_RES_FOLDER}/values-es-ES/strings.xml" "${APP_RES_FOLDER}/values-es/"
rm -rf "${APP_RES_FOLDER}/values-es-AR"
rm -rf "${APP_RES_FOLDER}/values-es-ES"
mv "${APP_RES_FOLDER}/values-he/strings.xml" "${APP_RES_FOLDER}/values-iw/"
rm -rf "${APP_RES_FOLDER}/values-he"
mv "${APP_RES_FOLDER}/values-yi/strings.xml" "${APP_RES_FOLDER}/values-ji/"
rm -rf "${APP_RES_FOLDER}/values-yi"
mv "${APP_RES_FOLDER}/values-hy-AM/strings.xml" "${APP_RES_FOLDER}/values-hy/"
rm -rf "${APP_RES_FOLDER}/values-hy-AM"
mv "${APP_RES_FOLDER}/values-sv-SE/strings.xml" "${APP_RES_FOLDER}/values-se/"
rm -rf "${APP_RES_FOLDER}/values-sv-SE/"
mv "${APP_RES_FOLDER}/values-pt-PT/strings.xml" "${APP_RES_FOLDER}/values-pt/"
rm -rf "${APP_RES_FOLDER}/values-pt-PT/"
mv "${APP_RES_FOLDER}/values-pt-BR/strings.xml" "${APP_RES_FOLDER}/values-pt-rBR/"
rm -rf "${APP_RES_FOLDER}/values-pt-BR/"
mv "${APP_RES_FOLDER}/values-tlh-AA/strings.xml" "${APP_RES_FOLDER}/values-tlh/"
rm -rf "${APP_RES_FOLDER}/values-tlh-AA"
mv "${APP_RES_FOLDER}/values-es-MX/strings.xml" "${APP_RES_FOLDER}/values-es-rMX/"
rm -rf "${APP_RES_FOLDER}/values-es-MX/"
#copying generic strings to en
cp "${APP_RES_FOLDER}/values/strings.xml" "${APP_RES_FOLDER}/values-en/strings.xml"
