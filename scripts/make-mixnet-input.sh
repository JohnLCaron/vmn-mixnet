#!/bin/bash

WORKING=$1

if [ -z "${WORKING}" ]; then
    echo "No workspace provided."
    exit 1
fi

VERIFICATUM_WORKSPACE=${WORKING}/vf
rm -rf ${VERIFICATUM_WORKSPACE}/*
mkdir -p ${VERIFICATUM_WORKSPACE}

echo "Creating mixnet input from the encrypted ballots"

CLASSPATH="build/libs/vmn-mixnet-all.jar"

java -classpath $CLASSPATH \
  org.cryptobiotic.verificabitur.vmn.RunMakeMixnetInput \
    --inputDir ${WORKING}/public \
    -out ${VERIFICATUM_WORKSPACE}/inputCiphertexts.bt

echo "[DONE] Creating mixnet input."
