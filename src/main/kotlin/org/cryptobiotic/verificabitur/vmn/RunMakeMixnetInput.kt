package org.cryptobiotic.verificabitur.vmn

import com.github.michaelbull.result.unwrap
import org.cryptobiotic.eg.core.*
import org.cryptobiotic.eg.publish.makeConsumer
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.required
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import org.cryptobiotic.eg.election.EncryptedBallot
import org.cryptobiotic.verificabitur.bytetree.MixnetBallot
import org.cryptobiotic.verificabitur.bytetree.publish
import org.cryptobiotic.verificabitur.bytetree.writeByteTreeToFile
import org.cryptobiotic.verificabitur.reader.publishJson
import java.io.FileOutputStream

/** Read the EG encrypted ballots and create input file of ciphertexts for the mixnet to mix. */
class RunMakeMixnetInput {
    val group = productionGroup()

    companion object {
        val jsonReader = Json { explicitNulls = false; ignoreUnknownKeys = true; prettyPrint = true }

        @JvmStatic
        fun main(args: Array<String>) {
            val parser = ArgParser("RunMakeMixnetInput")
            val inputDir by parser.option(
                ArgType.String,
                shortName = "publicDir",
                description = "Directory containing input encrypted ballots directory"
            ).required()
            val outputFile by parser.option(
                ArgType.String,
                shortName = "out",
                description = "Write to this filename"
            ).required()
            val isJson by parser.option(
                ArgType.Boolean,
                shortName = "json",
                description = "Encrypted ballots are JSON (default is ByteTree)"
            ).default(false)
            parser.parse(args)

            val info = buildString {
                append("RunMakeMixnetInput")
                append( "\n   inputDir= $inputDir,")
                append( "\n   outputFile= $outputFile,")
                append( "\n   isJson= $isJson,")
            }
            println(info)

            // create output directory if needed
            val outputDir = outputFile.substringBeforeLast("/")
            createDirectories(outputDir)

            val makeMixnetInput = RunMakeMixnetInput()
            val mixnetBallots = makeMixnetInput.makeMixnetBallots(inputDir)

            if (isJson) makeMixnetInput.writeJson(mixnetBallots, outputFile)
            else makeMixnetInput.writeByteTree(mixnetBallots, outputFile)

            println("wrote ${mixnetBallots.size} ballots")
        }
    }

    fun makeMixnetBallots(encryptedBallotsDir: String): List<MixnetBallot> {
        val consumer = makeConsumer(encryptedBallotsDir, group)
        val init = consumer.readElectionInitialized().unwrap()
        val publicKey = init.jointPublicKey

        // must be in some definite order
        val ballots = consumer.iterateAllCastBallots().toList().sortedBy { it.ballotId }

        val width = ballots.map { widthOfBallot(it) }.max()
        println(" width = ${width+2}")
        var ncount = 0

        val mixnetBallots = mutableListOf<MixnetBallot>()
        val seed = group.randomElementModQ()
        val nonces = Nonces(seed, "mixName") // used for the extra ciphertexts to make even rows

        consumer.iterateAllCastBallots().forEach { eballot ->
            val ciphertexts = mutableListOf<ElGamalCiphertext>()
            ciphertexts.add(eballot.encryptedSn!!) // always the first one
            ciphertexts.add(eballot.encryptedSn!!) // fake "encryptedStyleIndex" always the second one

            var count = 0
            eballot.contests.forEach { contest ->
                contest.selections.forEach { selection ->
                    ciphertexts.add(selection.encryptedVote)
                    count++
                }
            }
            // fill the remaining with encrypted zeroes. nonce must be deterministic.
            repeat(width - count) {
                ciphertexts.add(0.encrypt(publicKey, nonces.get(ncount++)))
            }
            require(ciphertexts.size == width + 2)
            mixnetBallots.add(MixnetBallot(ciphertexts))
        }
        return mixnetBallots
    }

    fun widthOfBallot(eballot: EncryptedBallot): Int {
        var count = 0
        eballot.contests.forEach { contest ->
            count += contest.selections.size
        }
        return count
    }

    fun writeJson(mixnetBallots: List<MixnetBallot>, outputFile: String) {
        val json = mixnetBallots.publishJson()
        FileOutputStream(outputFile).use { out ->
            jsonReader.encodeToStream(json, out)
        }
        println("*** Write mixnetBallots to Json $outputFile")
    }

    fun writeByteTree(mixnetBallots: List<MixnetBallot>, outputFile: String) {
        val tree = mixnetBallots.publish()
        writeByteTreeToFile(tree, outputFile)
        println("*** Write mixnetBallots to byteTree $outputFile")
    }

}