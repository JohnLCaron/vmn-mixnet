package org.cryptobiotic.verificabitur.vmn

import org.cryptobiotic.eg.core.createDirectories
import kotlin.test.Test

class RunMixnetConfigTest {
    val inputDir = "src/test/data/workingEc/public"
    val working = "testOut/RunMixnetConfigTest/"

    init {
        createDirectories(working)
    }

    @Test
    fun testRunMixnetConfig() {
        RunMixnetConfig.main(
            arrayOf(
                "--inputDir", inputDir,
                "--workingDir", working,
            )
        )
    }
}

// RunMixnetConfig inputDir= working/public workingDir= working/vf