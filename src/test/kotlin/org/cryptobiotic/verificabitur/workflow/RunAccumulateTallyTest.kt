package org.cryptobiotic.verificabitur.workflow

import org.cryptobiotic.eg.cli.RunAccumulateTally
import org.cryptobiotic.TestFiles
import kotlin.test.Test

class RunAccumulateTallyTest {

    @Test
    fun testAccumulateTally() {
        RunAccumulateTally.main(
            arrayOf(
                "-in", TestFiles.egDir,
                "-eballots", TestFiles.encryptedBallots,
                "-out", "testOut/testAccumulateTally"
            )
        )
    }

}

