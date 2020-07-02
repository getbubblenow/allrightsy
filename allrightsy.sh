#!/bin/bash
#
# Generate third-party software license information
#
# Usage:
#
#     allrightsy.sh <packs> <outfile.json>
#
#  packs        : a comma-separated list of inputs to allrightsy. these inputs are supported:
#                   /path/to/pom.xml       -- a maven pom.xml file
#                   /path/to/node_modules  -- an npm node_modules directory
#                   /path/to/licenses.json -- a verbatim set of license JSON entries to include
#
#  outfile.json : the output file to write. it will be an array of JSON objects, each representing a piece of
#                 third-party software and its associated license.
#
function die {
  echo 1>&2 "${1}"
  exit 1
}

BASE_DIR="$(cd "$(dirname "${0}")" && pwd)"
PACKS="${1:?no packs provided}"
shift
OUTFILE="${1:?no outfile provided}"
shift

AR_JAR="$(find "${BASE_DIR}/target" -type f -name "allrightsy.jar" | head -1)"
if [[ -z "${AR_JAR}" ]] ; then
  die "Error finding jar file"
fi

java -cp "${AR_JAR}" allrightsy.main.AllRightsyMain  --packs "${PACKS}" --outfile "${OUTFILE}" "${@}"
