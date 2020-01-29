#!/usr/bin/env bash
shopt -s dotglob

ASSEMBLING_FOLDER_PREFIX="assembling_"
function clone_locally() {
    local REPO_NAME=$1
    local REPO_URL=$2
    local START_COMMIT=$3

    echo "Fetching remote ${REPO_NAME} from ${REPO_URL}"

    git clone ${REPO_URL}

    pushd ${REPO_NAME} &> /dev/null

    echo "Switching to latest good commit ${START_COMMIT}"
    git reset --hard ${START_COMMIT}

    popd &> /dev/null
}

function ensure_valid() {
    local REPO_NAME=$1
    
    pushd ${REPO_NAME} &> /dev/null
    echo "Checking for illegal objects"
    local FS_LOG=$(git fsck --full 2>&1 | grep "zeroPaddedFilemode")
    echo ${FS_LOG}
    if [ "${FS_LOG}" != "" ]; then
        echo "Fixing ${REPO_NAME}"
        rm -rf ../temp
        mkdir ../temp
        pushd ../temp &> /dev/null
        git init
        popd &> /dev/null

        git fast-export --all --signed-tags=strip --progress=1319 | (cd ../temp/ && git fast-import)

        echo "Finishing up fixes"
        pushd  .. &> /dev/null
        rm -rf ${REPO_NAME}
        mv temp ${REPO_NAME}
        popd &> /dev/null
    fi

    popd &> /dev/null
}

function move_to_sub_folder() {
    local REPO_NAME=$1
    local ASSEMBLING_AREA_FOLDER="${ASSEMBLING_FOLDER_PREFIX}${REPO_NAME}"
    
    pushd ${REPO_NAME} &> /dev/null
    echo "Moving files to ${ASSEMBLING_AREA_FOLDER}"
    mkdir ${ASSEMBLING_AREA_FOLDER}
    for f in *; do
        if [ "${f}" != "${ASSEMBLING_AREA_FOLDER}" ] && [ "${f}" != ".git" ]; then
            git mv ${f} ${ASSEMBLING_AREA_FOLDER}/
        fi
    done
    
    git add .

    git commit --no-status -q -m "Temporarily holding ${REPO_NAME} files at ${ASSEMBLING_AREA_FOLDER}"

    popd &> /dev/null
}

function ready_clone() {
    local REPO_NAME=$1
    local REPO_URL=$2
    local START_COMMIT=$3

    clone_locally ${REPO_NAME} ${REPO_URL} ${START_COMMIT}

    ensure_valid ${REPO_NAME}
    move_to_sub_folder ${REPO_NAME}
}

function assemble_into_main() {
    local REPO_NAME=$1
    local ASSEMBLING_AREA_FOLDER="${ASSEMBLING_FOLDER_PREFIX}${REPO_NAME}"
    local TARGET_FOLDER=$2

    echo "Merging history from ${REPO_NAME} as file://${PWD}/../${REPO_NAME}"

    git remote add ${REPO_NAME} file://${PWD}/../${REPO_NAME}
    git fetch ${REPO_NAME} master

    git merge -q ${REPO_NAME}/master --allow-unrelated-histories -m "Initial ${REPO_NAME} history merge"

    git remote remove ${REPO_NAME}

    mkdir -p ${TARGET_FOLDER}

    for f in ${ASSEMBLING_AREA_FOLDER}/*; do
        if [ "${f}" != ".git" ]; then
            git mv ${f} ${TARGET_FOLDER}
        fi
    done

    git commit --no-status -q -m "Moving ${ASSEMBLING_AREA_FOLDER} files to ${TARGET_FOLDER}"

    rmdir ${ASSEMBLING_AREA_FOLDER}
}

pushd .. &> /dev/null

ready_clone "LanguagePack" "git@github.com:AnySoftKeyboard/LanguagePack.git" "HEAD"

popd &> /dev/null

assemble_into_main "LanguagePack" "addons"