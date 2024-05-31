#!/bin/bash

WORKSPACE_DIR=$1

if [ -z "${WORKSPACE_DIR}" ]; then
    rave_print "No workspace provided."
    exit 1
fi

echo "Verifying shuffled ballots..."

EG_WORKSPACE="${WORKSPACE_DIR}/public"
VERIFICATUM_WORKSPACE="${WORKSPACE_DIR}/vf"

CLASSPATH="build/libs/vmn-mixnet-all.jar"

echo "... verify mix1 shuffle ..."

java -classpath $CLASSPATH \
  org.cryptobiotic.verificabitur.vmn.RunMixnetVerifier \
    -protInfo ${VERIFICATUM_WORKSPACE}/protocolInfo.xml \
    -shuffle ${VERIFICATUM_WORKSPACE}/Party01/nizkp/mix1 \
    --sessionId mix1 \
    -width 21

echo "\n... verify mix2 shuffle ..."

java -classpath $CLASSPATH \
  org.cryptobiotic.verificabitur.vmn.RunMixnetVerifier \
    -protInfo ${VERIFICATUM_WORKSPACE}/protocolInfo.xml \
    -shuffle ${VERIFICATUM_WORKSPACE}/Party01/nizkp/mix2 \
    --sessionId mix2 \
    -width 21

echo "[DONE] Verifying shuffled ballots"
