package org.cryptobiotic.verificabitur.vmn

import kotlin.test.Test

class RunMixnetVerifierTest {

    @Test
    fun testRunVerifier() {
        val inputDir = "src/test/data/workingEc/vf"
        RunMixnetVerifier.main(
            arrayOf(
                "--inputDir", "$inputDir/Party01/nizkp/mix1",
                "--protInfo", "$inputDir/protocolInfo.xml",
                "--width", "21",
                "--sessionId", "mix1"
            )
        )
        RunMixnetVerifier.main(
            arrayOf(
                "--inputDir", "$inputDir/Party01/nizkp/mix2",
                "--protInfo", "$inputDir/protocolInfo.xml",
                "--width", "21",
                "--sessionId", "mix2"
            )
        )
    }
}

// RunMixnetVerifier starting
//   inputDir= working/vf/Party01/nizkp/mix1
//   protInfo = working/vf/protocolInfo.xml
//   width = 21
//   sessionId = mix1
//
//RunMixnetVerifier starting
//   inputDir= working/vf/Party01/nizkp/mix2
//   protInfo = working/vf/protocolInfo.xml
//   width = 21
//   sessionId = mix2