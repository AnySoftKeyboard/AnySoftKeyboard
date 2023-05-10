#!/usr/bin/env bash
#https://dl.google.com/dl/android/maven2
MAVEN_URL="$1"
#com.android.tools.build
GROUP_ID="$2"
#gradle
ARTIFACT_ID="$3"

VERSIONS_URL="${MAVEN_URL}/${GROUP_ID//[.]/\/}/${ARTIFACT_ID}/maven-metadata.xml"

VERSIONS="$(curl --silent "$VERSIONS_URL" | xmllint --format - | xmllint --xpath "string(/metadata/versioning/versions)" - | xargs)"
IFS=' ' read -r -a VERSIONS_ARRAY <<< "$VERSIONS"

readarray -t SORTED_VERSIONS_ARRAY < <(for v in "${VERSIONS_ARRAY[@]}"; do echo "$v"; done | sort -r -V)
for index in "${!SORTED_VERSIONS_ARRAY[@]}" ; do [[ ${SORTED_VERSIONS_ARRAY[$index]} =~ alpha ]] && unset -v 'SORTED_VERSIONS_ARRAY[$index]' ; done
for index in "${!SORTED_VERSIONS_ARRAY[@]}" ; do [[ ${SORTED_VERSIONS_ARRAY[$index]} =~ beta ]] && unset -v 'SORTED_VERSIONS_ARRAY[$index]' ; done
for index in "${!SORTED_VERSIONS_ARRAY[@]}" ; do [[ ${SORTED_VERSIONS_ARRAY[$index]} =~ -rc ]] && unset -v 'SORTED_VERSIONS_ARRAY[$index]' ; done
SORTED_VERSIONS_ARRAY=("${SORTED_VERSIONS_ARRAY[@]}")
echo "${SORTED_VERSIONS_ARRAY[0]}"
