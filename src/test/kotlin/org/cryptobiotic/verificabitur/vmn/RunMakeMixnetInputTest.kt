package org.cryptobiotic.verificabitur.vmn

import org.cryptobiotic.verificabitur.bytetree.readByteTreeFromFile
import kotlin.test.Test
import kotlin.test.assertEquals

class RunMakeMixnetInputTest {
    val inputDir = "src/test/data/workingEc/public"
    val working = "testOut/RunMakeMixnetInputTest/"

    @Test
    fun testMakeMixnetInputJson() {
        RunMakeMixnetInput.main(
            arrayOf(
                "-publicDir", inputDir,
                "--outputFile", "$working/inputCiphertexts.bt",
            )
        )

        // make sure its correctly formed
        val root = readByteTreeFromFile("$working/inputCiphertexts.bt").root
        assertEquals(2, root.n)
        assertEquals(21, root.child[0].n)
        assertEquals(100, root.child[0].child[0].n)
        assertEquals(2, root.child[0].child[0].child[0].n)
        assertEquals(33, root.child[0].child[0].child[0].child[0].n)
    }

}

