#!/bin/bash

source $(dirname "$0")/functions.sh

WORKSPACE_DIR=$1

if [ -z "${WORKSPACE_DIR}" ]; then
    rave_print "No workspace provided."
    exit 1
fi

rave_print "Shuffling encrypted ballots..."

EG_WORKSPACE="${WORKSPACE_DIR}/eg/"
CONSTANTS="${EG_WORKSPACE}/constants.json"
ELECTION_PARAMS="${EG_WORKSPACE}/election_initialized.json"

rave_print "remake VERIFICATUM_WORKSPACE"

VERIFICATUM_WORKSPACE="${WORKSPACE_DIR}/vf"
rm -rf ${VERIFICATUM_WORKSPACE}/*
mkdir -p ${VERIFICATUM_WORKSPACE}

rave_print "extract pqg"

# extract p q g
P=`cat ${CONSTANTS} | jq -r '.large_prime' | tr '[:upper:]' '[:lower:]'`
Q=`cat ${CONSTANTS} | jq -r '.small_prime' | tr '[:upper:]' '[:lower:]'`
G=`cat ${CONSTANTS} | jq -r '.generator' | tr '[:upper:]' '[:lower:]'`

rave_print "Call vog -gen ECqPGroup -name P-256"

# generate group description for Verificatum
GROUP=$(vog -gen ECqPGroup -name "P-256" | sed "s/[^:]*:://g")

rave_print "vog extracted group= ${GROUP}"

# convert it to JSON
echo "${GROUP}" | sed "s/[^:]*:://g" > ./_tmp_group_description
GROUP_JSON=`vbt -hex ./_tmp_group_description`
rm ./_tmp_group_description

# generate verificatum configuration
MIXER_NAME="MergeMixer"

rave_print "Call vmni -prot..."

vmni -prot -sid "FOO" -name ${MIXER_NAME} -nopart 1 -thres 1 \
     -pgroup "${GROUP}" -keywidth "1" ${VERIFICATUM_WORKSPACE}/localProtInfo.xml

rave_print "Call vmni -party..."

# generate mixer info, including private key for signing
vmni -party -name "${MIXER_NAME}" \
     -http http://localhost:8041 \
     -hint localhost:4041 \
     ${VERIFICATUM_WORKSPACE}/localProtInfo.xml ${VERIFICATUM_WORKSPACE}/privInfo.xml ${VERIFICATUM_WORKSPACE}/protInfo.xml

# extract public key from ElectionGuard
Y=`cat ${ELECTION_PARAMS} | jq -r '.joint_public_key' | tr '[:upper:]' '[:lower:]'`

rave_print "ElectionGuard ELECTION_PARAMS= 'cat ${ELECTION_PARAMS}'"
YY = 'cat ${ELECTION_PARAMS}'
rave_print "contents= $YY"
rave_print "extract public key= ${Y}"

# convert pk to Verificatum JSON
echo ${GROUP_JSON} | jq --arg g "00$G" --arg y "00$Y" '[., [$g, $y]]' > ${VERIFICATUM_WORKSPACE}/publickey.json

rave_print "Call vmnc -pkey..."

# convert to Verificatum RAW
vmnc -e -pkey ${VERIFICATUM_WORKSPACE}/protInfo.xml -ini seqjson -outi raw ${VERIFICATUM_WORKSPACE}/publickey.json ${VERIFICATUM_WORKSPACE}/publickey.raw

rave_print "Call vmn -setpk..."

# import it
vmn -setpk ${VERIFICATUM_WORKSPACE}/privInfo.xml ${VERIFICATUM_WORKSPACE}/protInfo.xml ${VERIFICATUM_WORKSPACE}/publickey.raw

# extract ciphertext components to V JSON import format
echo "[" > ${VERIFICATUM_WORKSPACE}/input-ciphertexts.json
LOOP_NUM=0

rave_print "... Set up the mixnet, now loading encrypted ballots ..."

for file in ${WORKSPACE_DIR}/bb/EB/eballot-*.json; do
    # Check if file exists (this avoids the loop body executing if no files match the pattern)
    if [[ -f "$file" ]]; then
        echo "Loading ballot: $file"

	# pull all the ciphertexts for one ballot into a multi-ciphertext wide input for Verificatum
	NEW_BALLOT=`cat $file | jq '[.contests[] | .selections[] | .encrypted_vote]' | jq '[[.[] | .pad | "00" + .], [.[] | .data | "00" + .]]' | tr '[:upper:]' '[:lower:]'`

	# compute the width (yes this will happen every time and overwrite the previous value, that's fine)
	WIDTH=`echo "$NEW_BALLOT" | jq '.[0] | length'`

	# append ballot to the array of ballots
	if [ $LOOP_NUM -gt 0 ]; then
	    echo "," >> ${VERIFICATUM_WORKSPACE}/input-ciphertexts.json
	fi
	echo "$NEW_BALLOT" >> ${VERIFICATUM_WORKSPACE}/input-ciphertexts.json
    fi
    LOOP_NUM=1
done

echo "]" >> ${VERIFICATUM_WORKSPACE}/input-ciphertexts.json

rave_print "Call vmnc -ciphs -ini seqjson -outi raw "

# convert ciphertexts to V raw format
vmnc -e -ciphs -width "${WIDTH}"  -ini seqjson -outi raw \
     ${VERIFICATUM_WORKSPACE}/protInfo.xml ${VERIFICATUM_WORKSPACE}/input-ciphertexts.json ${VERIFICATUM_WORKSPACE}/input-ciphertexts.raw


rave_print "... now shuffling once ..."

AUXSID=`date "+%s" | sed "s/ /_/g"`

# shuffle once
vmn -shuffle -width "${WIDTH}" -auxsid "${AUXSID}" \
    ${VERIFICATUM_WORKSPACE}/privInfo.xml \
    ${VERIFICATUM_WORKSPACE}/protInfo.xml \
    ${VERIFICATUM_WORKSPACE}/input-ciphertexts.raw ${VERIFICATUM_WORKSPACE}/after-mix-1-ciphertexts.raw

rave_print "Call vmnc -ciphs -ini raw -outi seqjson"

# convert output ciphertexts to JSON format
vmnc -ciphs -width "${WIDTH}" -ini raw -outi seqjson \
     ${VERIFICATUM_WORKSPACE}/protInfo.xml ${VERIFICATUM_WORKSPACE}/after-mix-2-ciphertexts.raw ${VERIFICATUM_WORKSPACE}/after-mix-2-ciphertexts.json

rave_print "done shuffling encrypted ballots are in ${VERIFICATUM_WORKSPACE}/after-mix-2-ciphertexts.json"

rave_print "proto file = ${WORKSPACE_DIR}/vf/protInfo.xml"
rave_print "nizkp file = dir/nizkp/${AUXSID}/"

vmnv -shuffle -width "${WIDTH}" -auxsid "${AUXSID}" -v \
    ${VERIFICATUM_WORKSPACE}/protInfo.xml \
    dir/nizkp/${AUXSID}

rave_print "[DONE] verify"