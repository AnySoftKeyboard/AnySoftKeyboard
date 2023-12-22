#!/bin/bash
set -e

TEMP_EXTRACT_FOLDER="${TMPDIR:-/tmp}/ask_crowdin/"
TEMP_OUTPUT_FOLDER="${TMPDIR:-/tmp}/ask_crowdin_file/"
TEMP_OUTPUT_FILE=all.zip
REPO_ROOT="$PWD"

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

echo "output folder: ${TEMP_EXTRACT_FOLDER}"

pushd "${TEMP_EXTRACT_FOLDER}" || exit 1
for f in *; do mv "$f" "values-$f"; done
popd || exit 1

function move_translation_to_dir() {
    local res_folder="$1"
    local source_file="$2"
    local target_folder="$3"

    mkdir -p "${res_folder}/${target_folder}" || true
    mv "${res_folder}/${source_file}" "${res_folder}/${target_folder}" || true
    rmdir --ignore-fail-on-non-empty "$(dirname ${res_folder}/${source_file})"
}

copy_script="${PWD}/scripts/copy_translations_from_crowdin_to_module.sh"
function copy_translations() {
  local TARGET_FOLDER="$1"
  local SOURCE_FILE="$2"

  echo "will copy ${SOURCE_FILE} from ${TEMP_EXTRACT_FOLDER} to ${TARGET_FOLDER}"
  for f in **/"${SOURCE_FILE}"; do
    echo "file $f"
    local TARGET_RES_FOLDER="${TARGET_FOLDER}/$(dirname $f)"
    echo "target folder $TARGET_RES_FOLDER"
    mkdir -p "${TARGET_RES_FOLDER}"
    cp "$f" "${TARGET_RES_FOLDER}/strings.xml"
  done

  echo "fixing file locations a bit..."
  rm -rf "${TARGET_FOLDER}/values-en-PT" || true
  rm -rf "${TARGET_FOLDER}/values-es-AR" || true
  move_translation_to_dir "${TARGET_FOLDER}" "values-es-ES/strings.xml" "values-es/"
  move_translation_to_dir "${TARGET_FOLDER}" "values-he/strings.xml" "values-iw/"
  move_translation_to_dir "${TARGET_FOLDER}" "values-yi/strings.xml" "values-ji/"
  move_translation_to_dir "${TARGET_FOLDER}" "values-hy-AM/strings.xml" "values-hy/"
  move_translation_to_dir "${TARGET_FOLDER}" "values-sv-SE/strings.xml" "values-se/"
  move_translation_to_dir "${TARGET_FOLDER}" "values-pt-PT/strings.xml" "values-pt/"
  move_translation_to_dir "${TARGET_FOLDER}" "values-pt-BR/strings.xml" "values-pt-rBR/"
  move_translation_to_dir "${TARGET_FOLDER}" "values-zh-CN/strings.xml" "values-zh-rCN/"
  move_translation_to_dir "${TARGET_FOLDER}" "values-tlh-AA/strings.xml" "values-tlh/"
  move_translation_to_dir "${TARGET_FOLDER}" "values-es-MX/strings.xml" "values-es-rMX/"
  move_translation_to_dir "${TARGET_FOLDER}" "values-ml-IN/strings.xml" "values-ml-rIN/"
  move_translation_to_dir "${TARGET_FOLDER}" "values-bn-IN/strings.xml" "values-bn-rIN/"
  move_translation_to_dir "${TARGET_FOLDER}" "values-si-LK/strings.xml" "values-si-rLK/"
  #copying generic strings to en
  cp "${TARGET_FOLDER}/values/strings.xml" "${TARGET_FOLDER}/values-en/strings.xml"

  echo "fixing ellipsis character..."
  find "${TARGET_FOLDER}" -type f -name "strings.xml" -exec sed -i 's/\.\.\./…/g' {} \;
}

pushd "${TEMP_EXTRACT_FOLDER}"

copy_translations "${REPO_ROOT}/ime/app/src/main/res" strings.xml
copy_translations "${REPO_ROOT}/ime/remote/src/main/res" remote_strings.xml
copy_translations "${REPO_ROOT}/ime/addons/src/main/res" addons_strings.xml
copy_translations "${REPO_ROOT}/ime/chewbacca/src/main/res" chewbacca_strings.xml
copy_translations "${REPO_ROOT}/ime/releaseinfo/src/main/res" release_info_strings.xml
copy_translations "${REPO_ROOT}/ime/pixel/src/main/res" pixel_strings.xml

popd
echo "done"
