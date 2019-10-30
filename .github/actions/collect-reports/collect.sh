
OUTPUT=${1}
PATTERN=${2}
rm -rf ${OUTPUT} || true
mkdir ${OUTPUT} || true
find . -path ./${OUTPUT} -prune -o -name "${PATTERN}" -exec cp {} ${OUTPUT}/ \;
