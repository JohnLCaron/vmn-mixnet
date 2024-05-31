package org.cryptobiotic.verificabitur.vmn

import org.cryptobiotic.verificabitur.bytetree.readByteTreeFromFile
import org.cryptobiotic.verificabitur.vmn.RunMakeMixnetInput
import kotlin.test.Test
import kotlin.test.assertEquals

class RunMakeMixnetInputTest {
    val egDir = "src/test/data/working/eg"
    val bbDir = "src/test/data/working/bb"

    @Test
    fun testMakeMixnetInputJson() {
        RunMakeMixnetInput.main(
            arrayOf(
                "-publicDir", "$bbDir/encryptedBallots",
                "--outputFile", "testOut/inputCiphertexts.json",
                "-json"
            )
        )
    }


    //    encryptedBallotsDir= working/public/encrypted_ballots,
    //   outputFile= working/vf/inputCiphertexts.bt,

    @Test
    fun testMakeMixnetInput() {
        val outputFile = "working/vf/inputCiphertexts.bt"
        RunMakeMixnetInput.main(
            arrayOf(
                "--inputDir", "working/public",
                "--outputFile", outputFile,
            )
        )
        /* make sure its correctly formed
        val root = readByteTreeFromFile(outputFile).root
        assertEquals(2, root.n)
        assertEquals(34, root.child[0].n)
        assertEquals(13, root.child[0].child[0].n)
        assertEquals(513, root.child[0].child[0].child[0].n)

         */
    }

}

