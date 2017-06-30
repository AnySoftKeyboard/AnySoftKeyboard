#!/bin/bash

TEMP_EXTRACT_FOLDER=/tmp/ask_crowdin/
TEMP_OUTPUT_FOLDER=/tmp/ask_crowdin_file/
TEMP_OUTPUT_FILE=all.zip

if [ -z "${CROWDIN_API}" ]; then
    echo "Could not find crowdin API environment variable at CROWDIN_API."
    exit 1
fi

rm -rf ${TEMP_EXTRACT_FOLDER} || true
rm -rf ${TEMP_OUTPUT_FOLDER} || true

if [ "$1" == "build" ]; then
    echo "Building translations..."
    wget -O export.txt https://api.crowdin.com/api/project/anysoftkeyboard/export?key=${CROWDIN_API}
    cat export.txt
    rm export.txt
else
    echo "Not exporting latest translations. Use 'build' argument to force build first."
fi

mkdir ${TEMP_EXTRACT_FOLDER}
mkdir ${TEMP_OUTPUT_FOLDER}
wget -O "${TEMP_OUTPUT_FOLDER}${TEMP_OUTPUT_FILE}" https://api.crowdin.com/api/project/anysoftkeyboard/download/all.zip?key=${CROWDIN_API}
unzip -o "${TEMP_OUTPUT_FOLDER}${TEMP_OUTPUT_FILE}" -d ${TEMP_EXTRACT_FOLDER}

pushd ${TEMP_EXTRACT_FOLDER}
find * -maxdepth 0 ! -path . -exec mv {} values-{} \;

popd
cp -R ${TEMP_EXTRACT_FOLDER} app/src/main/res

#fixing files a bit
rm -rf app/src/main/res/values-en-PT
rm -rf app/src/main/res/values-en
mv app/src/main/res/values-es-ES/strings.xml app/src/main/res/values-es/
rm -rf app/src/main/res/values-es-AR
rm -rf app/src/main/res/values-es-ES
mv app/src/main/res/values-he/strings.xml app/src/main/res/values-iw/
rm -rf app/src/main/res/values-he
mv app/src/main/res/values-hy-AM/strings.xml app/src/main/res/values-hy/
rm -rf app/src/main/res/values-hy-AM
mv app/src/main/res/values-sv-SE/strings.xml app/src/main/res/values-se/
rm -rf app/src/main/res/values-sv-SE/
mv app/src/main/res/values-pt-PT/strings.xml app/src/main/res/values-pt/
rm -rf app/src/main/res/values-pt-PT/
mv app/src/main/res/values-pt-BR/strings.xml app/src/main/res/values-pt-rBR/
rm -rf app/src/main/res/values-pt-BR/
mv app/src/main/res/values-tlh-AA/strings.xml app/src/main/res/values-tlh/
rm -rf app/src/main/res/values-tlh-AA


