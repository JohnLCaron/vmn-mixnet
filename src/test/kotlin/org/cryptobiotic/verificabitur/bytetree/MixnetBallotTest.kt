package org.cryptobiotic.verificabitur.bytetree

import org.cryptobiotic.egk.CiphertextDecryptor
import org.cryptobiotic.eg.core.*
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class MixnetBallotTest {
    val inputDir = "src/test/data/working/vf"
    val bbDir = "src/test/data/working/bb/vf"
    val nizkpDir = "$inputDir/Party01/nizkp"
    val proofsDir = "$inputDir/Party01/nizkp/mix2/proofs"

    val egDir = "src/test/data/working/eg"
    val group = productionGroup("Integer4096")

    @Test
    fun testMixnetInpute() {
        readMixnetBallot("$inputDir/inputCiphertexts.bt")
    }

    @Test
    fun testMixnetRawBallots() {
        readMixnetBallot("$nizkpDir/mix1/Ciphertexts.bt")
        readMixnetBallot("$nizkpDir/mix2/Ciphertexts.bt")
        readMixnetBallot("$nizkpDir/mix1/ShuffledCiphertexts.bt")
        readMixnetBallot("$nizkpDir/mix2/ShuffledCiphertexts.bt")
    }

    @Test
    fun showFiles() {
        showFileHash("$inputDir/inputCiphertexts.bt")
        println()
        showFileHash("$nizkpDir/mix1/Ciphertexts.bt")
        showFileHash("$nizkpDir/mix2/Ciphertexts.bt")
        showFileHash("$nizkpDir/mix1/ShuffledCiphertexts.bt")
        showFileHash("$nizkpDir/mix2/ShuffledCiphertexts.bt")
        println()

        showFileHash("$bbDir/mix1/Ciphertexts.bt")
        showFileHash("$bbDir/mix2/Ciphertexts.bt")
        showFileHash("$bbDir/mix1/ShuffledCiphertexts.bt")
        showFileHash("$bbDir/mix2/ShuffledCiphertexts.bt")
    }

    fun readMixnetBallot(inputFilename: String) {
        val ballots = readMixnetBallotFromFile(group, inputFilename)
        assertEquals(13, ballots.size)
        ballots.forEach() {
            assertEquals(34, it.ciphertexts.size)
        }
        println(" width = ${ballots[0].ciphertexts.size}")

        // the real test is if we can decrypt them
        val decryptor = CiphertextDecryptor(
            group,
            egDir,
            "$egDir/trustees",
        )
        ballots.forEachIndexed() { idx, it ->
            decryptor.decryptPep(it.encryptedSn())
            print("Ballot $idx decrypted to K^sn")
            it.removeFirst().ciphertexts.forEach { text ->
                decryptor.decrypt(text)
            }
            println(": all ciphertexts decrypted")
        }
    }

    fun showFileHash(file1 : String) {
        val ba1 = File(file1).readBytes()
        println("$file1 (${ba1.contentHashCode()})")
    }
}

fun compareFiles(file1 : String, file2 : String) {
    val ba1 = File(file1).readBytes()
    val ba2 = File(file2).readBytes()
    val same = ba1.contentEquals(ba2)
    println("$file1 (${ba1.contentHashCode()}) \n$file2 (${ba2.contentHashCode()}) \n same = $same \n")
}

/*
// input
working/vf/input-ciphertexts.raw (214181973)
working/vf/dir/nizkp/1701230437/Ciphertexts.bt (214181973)

// output of shuffle1
working/vf/dir/nizkp/1701230437/ShuffledCiphertexts.bt (1351194689)
working/vf/dir/nizkp/1701230437/proofs/Ciphertexts01.bt (1351194689)
working/vf/after-mix-1-ciphertexts.raw (1351194689)
working/vf/dir/nizkp/1701230458/Ciphertexts.bt (1351194689)

// output of shuffle2
working/vf/dir/nizkp/1701230458/ShuffledCiphertexts.bt (1587506439)
working/vf/dir/nizkp/1701230458/proofs/Ciphertexts01.bt (1587506439)
working/vf/after-mix-2-ciphertexts.raw (1587506439)
 */