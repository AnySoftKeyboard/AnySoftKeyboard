#!/usr/bin/env bash

BRACNH_TO_MIGRATE=$1
TARGET_MODULE=`echo ${1} | tr '[:upper:]' '[:lower:]'`
TARGET_FOLDER="languages/${TARGET_MODULE}"
COPYBARA_BIN=$2
TEMP_DIR=${TMPDIR}/LANGUAGE_PACK_MIGRATE/${BRACNH_TO_MIGRATE}

if [[ -z ${BRACNH_TO_MIGRATE} ]]; then
    echo "supply branch to migrate as first argument!"
    exit 1
fi

if [[ ! -f ${COPYBARA_BIN} ]]; then
    echo "supply path to copybara binary as second argument!"
    echo "see https://github.com/google/copybara for details."
    exit 1
fi

rm -rf ${TEMP_DIR}
mkdir -p ${TEMP_DIR}/files/
file_contents=$(<scripts/copy.bara.sky.template)
echo "${file_contents//REPLACEMENT_BRANCH_TO_MIGRATE/$BRACNH_TO_MIGRATE}" > ${TEMP_DIR}/copy.bara.sky

${COPYBARA_BIN} --folder-dir ${TEMP_DIR}/files/ ${TEMP_DIR}/copy.bara.sky

mv ${TEMP_DIR}/files/${TARGET_MODULE} languages/

echo "include ':languages:${TARGET_MODULE}:pack', ':languages:${TARGET_MODULE}:apk'" >> settings.gradle

mv ${TARGET_FOLDER}/build.gradle ${TARGET_FOLDER}/build.gradle.old
echo "// Language pack ${BRACNH_TO_MIGRATE}" > ${TARGET_FOLDER}/build.gradle

mkdir ${TARGET_FOLDER}/apk
echo "apply plugin: 'com.android.application'" > ${TARGET_FOLDER}/apk/build.gradle
echo "ext.shouldBePublished = false" >> ${TARGET_FOLDER}/apk/build.gradle
echo "dependencies {" >> ${TARGET_FOLDER}/apk/build.gradle
echo "    implementation project(path: ':languages:${TARGET_MODULE}:pack')" >> ${TARGET_FOLDER}/apk/build.gradle
echo "}" >> ${TARGET_FOLDER}/apk/build.gradle

mkdir ${TARGET_FOLDER}/pack
echo "apply plugin: 'com.android.library'" > ${TARGET_FOLDER}/pack/build.gradle
echo "ext.status_icon_text = 'en'" >> ${TARGET_FOLDER}/pack/build.gradle
echo "ext.dictionaryInputPossibleCharacters = null//"".toCharArray()" >> ${TARGET_FOLDER}/pack/build.gradle
echo "ext.dictionaryInputAdditionalInnerCharacters = null//"".toCharArray()" >> ${TARGET_FOLDER}/pack/build.gradle
echo "dependencies {" >> ${TARGET_FOLDER}/pack/build.gradle
echo "    implementation project(path: ':languages:base')" >> ${TARGET_FOLDER}/pack/build.gradle
echo "}" >> ${TARGET_FOLDER}/pack/build.gradle
