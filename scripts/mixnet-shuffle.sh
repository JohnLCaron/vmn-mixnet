#!/bin/bash

WORKSPACE_DIR=$1

if [ -z "${WORKSPACE_DIR}" ]; then
    rave_print "No workspace provided."
    exit 1
fi

echo "Shuffling encrypted ballots..."

EG_WORKSPACE="${WORKSPACE_DIR}/public"
CONSTANTS="${EG_WORKSPACE}/constants.json"
ELECTION_PARAMS="${EG_WORKSPACE}/election_initialized.json"

VERIFICATUM_WORKSPACE="${WORKSPACE_DIR}/vf"

CLASSPATH="build/libs/vmn-mixnet-all.jar"

# shuffle once
echo "... now shuffling once ..."

java -classpath $CLASSPATH \
  org.cryptobiotic.verificabitur.vmn.RunMixnet \
    -in ${VERIFICATUM_WORKSPACE}/inputCiphertexts.bt \
    -privInfo ${VERIFICATUM_WORKSPACE}/privateInfo.xml \
    -protInfo ${VERIFICATUM_WORKSPACE}/protocolInfo.xml \
    -sessionId mix1

# shuffle twice
echo "... now shuffling twice ..."

java -classpath $CLASSPATH \
  org.cryptobiotic.verificabitur.vmn.RunMixnet \
    -in ${VERIFICATUM_WORKSPACE}/Party01/nizkp/mix1/ShuffledCiphertexts.bt \
    -privInfo ${VERIFICATUM_WORKSPACE}/privateInfo.xml \
    -protInfo ${VERIFICATUM_WORKSPACE}/protocolInfo.xml \
    -sessionId mix2

echo "[DONE] Shuffling encrypted ballots"