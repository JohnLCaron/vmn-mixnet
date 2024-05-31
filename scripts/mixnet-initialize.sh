#!/bin/bash

WORKING=$1

if [ -z "${WORKING}" ]; then
    echo "No workspace provided."
    exit 1
fi

VERIFICATUM_WORKSPACE=${WORKING}/vf
EG_WORKSPACE="${WORKING}/public"

CLASSPATH="build/libs/vmn-mixnet-all.jar"
java -classpath $CLASSPATH \
  org.cryptobiotic.verificabitur.vmn.RunMixnetConfig \
    -input ${EG_WORKSPACE} \
    -working ${VERIFICATUM_WORKSPACE}

echo "[DONE] Initialize verificatum mixnet in directory ${VERIFICATUM_WORKSPACE}"
