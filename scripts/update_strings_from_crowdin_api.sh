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
  mv "${TARGET_FOLDER}/values-es-ES/strings.xml" "${TARGET_FOLDER}/values-es/" || true
  rm -rf "${TARGET_FOLDER}/values-es-AR" || true
  rm -rf "${TARGET_FOLDER}/values-es-ES" || true
  mv "${TARGET_FOLDER}/values-he/strings.xml" "${TARGET_FOLDER}/values-iw/" || true
  rm -rf "${TARGET_FOLDER}/values-he" || true
  mv "${TARGET_FOLDER}/values-yi/strings.xml" "${TARGET_FOLDER}/values-ji/" || true
  rm -rf "${TARGET_FOLDER}/values-yi" || true
  mv "${TARGET_FOLDER}/values-hy-AM/strings.xml" "${TARGET_FOLDER}/values-hy/" || true
  rm -rf "${TARGET_FOLDER}/values-hy-AM" || true
  mv "${TARGET_FOLDER}/values-sv-SE/strings.xml" "${TARGET_FOLDER}/values-se/" || true
  rm -rf "${TARGET_FOLDER}/values-sv-SE/" || true
  mv "${TARGET_FOLDER}/values-pt-PT/strings.xml" "${TARGET_FOLDER}/values-pt/" || true
  rm -rf "${TARGET_FOLDER}/values-pt-PT/" || true
  mv "${TARGET_FOLDER}/values-pt-BR/strings.xml" "${TARGET_FOLDER}/values-pt-rBR/" || true
  rm -rf "${TARGET_FOLDER}/values-pt-BR/" || true
  mv "${TARGET_FOLDER}/values-zh-CN/strings.xml" "${TARGET_FOLDER}/values-zh-rCN/" || true
  rm -rf "${TARGET_FOLDER}/values-zh-CN/" || true
  mv "${TARGET_FOLDER}/values-tlh-AA/strings.xml" "${TARGET_FOLDER}/values-tlh/" || true
  rm -rf "${TARGET_FOLDER}/values-tlh-AA" || true
  mv "${TARGET_FOLDER}/values-es-MX/strings.xml" "${TARGET_FOLDER}/values-es-rMX/" || true
  rm -rf "${TARGET_FOLDER}/values-es-MX/" || true
  mv "${TARGET_FOLDER}/values-ml-IN/strings.xml" "${TARGET_FOLDER}/values-ml-rIN/" || true
  rm -rf "${TARGET_FOLDER}/values-ml-IN/" || true
  mv "${TARGET_FOLDER}/values-bn-IN/strings.xml" "${TARGET_FOLDER}/values-bn-rIN/" || true
  rm -rf "${TARGET_FOLDER}/values-bn-IN" || true
  mv "${TARGET_FOLDER}/values-si-LK/strings.xml" "${TARGET_FOLDER}/values-si-rLK/" || true
  rm -rf "${TARGET_FOLDER}/values-si-LK/" || true
  #copying generic strings to en
  cp "${TARGET_FOLDER}/values/strings.xml" "${TARGET_FOLDER}/values-en/strings.xml"

  echo "fixing ellipsis character..."
  find "${TARGET_FOLDER}" -type f -name "strings.xml" -exec sed -i 's/\.\.\./…/g' {} \;
}

pushd "${TEMP_EXTRACT_FOLDER}"

copy_translations "${REPO_ROOT}/ime/app/src/main/res" strings.xml
copy_translations "${REPO_ROOT}/ime/remote/src/main/res" remote_strings.xml
copy_translations "${REPO_ROOT}/ime/addons/src/main/res" addons_strings.xml

popd
echo "done"
