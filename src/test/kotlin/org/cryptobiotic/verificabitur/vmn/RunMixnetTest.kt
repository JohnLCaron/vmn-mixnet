package org.cryptobiotic.verificabitur.vmn

import org.cryptobiotic.eg.core.createDirectories
import org.cryptobiotic.eg.core.removeAllFiles
import java.io.File
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertTrue

class RunMixnetTest {
    val inputDir = "src/test/data/workingEc/"
    val working = "testOut/RunMixnetTest"  // TODO

    init {
        removeAllFiles(Path.of(working))
        createDirectories(working)
        File("$inputDir/vf/inputCiphertexts.bt").copyTo(File("$working/inputCiphertexts.bt"))
        RunMixnetConfig.main(
            arrayOf(
                "--inputDir", "$inputDir/public",
                "--workingDir", working,
            )
        )
    }

    // need to set up a clean directory
    @Test
    fun testRunMixnet() {
        RunMixnet.main(
            arrayOf(
                "-in", "$working/inputCiphertexts.bt",
                "-privInfo", "$working/privateInfo.xml",
                "-protInfo", "$working/protocolInfo.xml",
                "-sessionId", "mix1",
            )
        )

        RunMixnet.main(
            arrayOf(
                "-in", "$working/Party01/nizkp/mix1/ShuffledCiphertexts.bt",
                "-privInfo", "$working/privateInfo.xml",
                "-protInfo", "$working/protocolInfo.xml",
                "-sessionId", "mix2",
            )
        )
    }

}

// ... now shuffling once ...
//RunMixnet starting
//   input= working/vf/inputCiphertexts.bt
//   privInfo = working/vf/privateInfo.xml
//   protInfo = working/vf/protocolInfo.xml
//   auxsid = mix1
//
// elGamalRawInterface = com.verificatum.protocol.elgamal.ProtocolElGamalInterfaceRaw
//read 340425 bytes from working/vf/inputCiphertexts.bt
//width = 21
//Run with 48 cores
//RunMixnet elapsed time = 10065 msecs ( 0h  0m 10s)
//sessionId mix1 complete successfully
//
//... now shuffling twice ...
//RunMixnet starting
//   input= working/vf/Party01/nizkp/mix1/ShuffledCiphertexts.bt
//   privInfo = working/vf/privateInfo.xml
//   protInfo = working/vf/protocolInfo.xml
//   auxsid = mix2