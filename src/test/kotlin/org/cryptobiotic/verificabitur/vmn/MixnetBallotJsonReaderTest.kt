package org.cryptobiotic.verificabitur.vmn

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.unwrap
import org.cryptobiotic.eg.core.*
import org.cryptobiotic.eg.publish.makeConsumer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.cryptobiotic.egk.CiphertextDecryptor
import org.cryptobiotic.verificabitur.bytetree.MixnetBallot
import java.nio.file.FileSystems
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MixnetBallotJsonReaderTest {
    val workingDir = "src/test/data/working/"
    val bbDir = "src/test/data/working/bb/vf"

    val jsonFile = "src/test/data/mixnetInput/inputCiphertexts.json"

    var fileSystem = FileSystems.getDefault()
    var fileSystemProvider = fileSystem.provider()
    val group = productionGroup()
    val jsonReader = Json { explicitNulls = false; ignoreUnknownKeys = true }


    @Test
    fun testMixnetInput() {
        val result = readMixnetBallotJson(jsonFile)
        assertTrue(result is Ok)
        val mixnetInput: MixnetBallotJson = result.unwrap() // SO?

        val decryptor = CiphertextDecryptor(
            group,
            "$workingDir/eg",
            "$workingDir/eg/trustees",
        )

        val mixnetBallots = mutableListOf<MixnetBallot>()
        val consumer = makeConsumer("$workingDir/eg")
        consumer.iterateAllCastBallots().forEach { encryptedBallot ->
            val ciphertexts = mutableListOf<ElGamalCiphertext>()
            ciphertexts.add(encryptedBallot.encryptedSn!!) // always the first one
            encryptedBallot.contests.forEach { contest ->
                contest.selections.forEach { selection ->
                    ciphertexts.add(selection.encryptedVote)
                }
            }
            mixnetBallots.add(MixnetBallot(ciphertexts))
        }

        mixnetBallots.forEachIndexed { idx, it ->
            it.ciphertexts.forEach { ciphertext ->
                val vote = decryptor.decrypt(ciphertext)
                // print("$vote,")
                assertNotNull(vote)
            }
            println("\nballot ${idx + 1} OK")
        }
    }


    private fun readMixnetBallotJson(filename: String): Result<MixnetBallotJson, String> =
        try {
            val path = Path.of(filename)
            val mixnetBallotJson : MixnetBallotJson
            fileSystemProvider.newInputStream(path).use { inp ->
                val lists = jsonReader.decodeFromStream<List<List<List<String>>>>(inp)
                mixnetBallotJson = MixnetBallotJson(lists)
            }
            Ok(mixnetBallotJson)
        } catch (e: Exception) {
            e.printStackTrace()
            Err(e.message ?: "readMixnetInput on $filename error")
        }
}