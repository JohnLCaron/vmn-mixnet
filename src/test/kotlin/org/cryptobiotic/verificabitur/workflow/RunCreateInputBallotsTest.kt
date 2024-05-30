package org.cryptobiotic.verificabitur.workflow

import org.cryptobiotic.eg.cli.RunCreateInputBallots
import org.cryptobiotic.TestFiles
import kotlin.test.Test

class RunCreateInputBallotsTest {

    @Test
    fun testCreateInputBallots() {
        RunCreateInputBallots.main(
            arrayOf(
                "-manifest", TestFiles.egDir,
                "-out", "testOut/testCreateInputBallots",
                "-n", "22"
            )
        )
    }

}

