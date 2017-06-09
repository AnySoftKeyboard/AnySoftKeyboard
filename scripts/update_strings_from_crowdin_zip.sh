#!/bin/bash

CROWDIN_ZIP_FILE=$1
TEMP_FOLDER=/tmp/ask_crowdin/

if [ ! -f "${CROWDIN_ZIP_FILE}" ]; then
    echo "Could not find crowdin localization file ${CROWDIN_ZIP_FILE}."
    echo "First argument should be the path to crowdin zip file."
    exit 1
fi

if [ -d "${TEMP_FOLDER}" ]; then
    rm -rf ${TEMP_FOLDER}
fi

mkdir ${TEMP_FOLDER}
unzip -o "${CROWDIN_ZIP_FILE}" -d ${TEMP_FOLDER}

pushd ${TEMP_FOLDER}
find * -maxdepth 0 ! -path . -exec mv {} values-{} \;

popd
cp -R ${TEMP_FOLDER} app/src/main/res

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


