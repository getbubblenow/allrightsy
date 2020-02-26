#!/bin/bash

function die {
  echo 1>&2 "${1}"
  exit 1
}

BASE_DIR="$(cd "$(dirname "${0}")" && pwd)"
PACKS="${1:?no packs provided}"
shift
OUTFILE="${1:?no outfile provided}"
shift

AR_JAR="$(find ${BASE_DIR}/target -type f -name "allrightsy-*.jar" | head -1)"
if [[ -z "${AR_JAR}" ]] ; then
  die "Error finding jar file"
fi

java -cp "${AR_JAR}" allrightsy.main.AllRightsyMain  --packs "${PACKS}" --outfile "${OUTFILE}" "${@}"
