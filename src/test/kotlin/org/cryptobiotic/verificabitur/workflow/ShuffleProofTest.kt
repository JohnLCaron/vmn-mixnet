package org.cryptobiotic.verificabitur.workflow

import com.github.michaelbull.result.unwrap
import com.verificatum.util.UniformExecutors
import org.cryptobiotic.eg.core.*
import org.cryptobiotic.eg.publish.Consumer
import org.cryptobiotic.eg.publish.makeConsumer
import kotlin.random.Random
import org.cryptobiotic.util.Stopwatch
import org.cryptobiotic.verificabitur.bytetree.MixnetBallot
import org.cryptobiotic.verificabitur.bytetree.publish
import org.cryptobiotic.verificabitur.bytetree.writeByteTreeToFile
import org.cryptobiotic.verificabitur.bytetree.writePublicKeyToByteFile
import org.cryptobiotic.verificabitur.vmn.Mixnet
import org.cryptobiotic.verificabitur.vmn.MixnetConfig
import org.cryptobiotic.verificabitur.vmn.RunMixnetConfig.Companion.keygen
import org.cryptobiotic.verificabitur.vmn.RunMixnetConfig.Companion.pkey
import org.cryptobiotic.verificabitur.vmn.RunMixnetConfig.Companion.rand
import org.cryptobiotic.verificabitur.vmn.RunMixnetConfig.Companion.skey
import org.cryptobiotic.verificabitur.vmn.Verifier
import java.nio.file.Path
import kotlin.test.Test

class ShuffleProofTest {
    val group = productionGroup("P-256")
    val inputDir = "src/test/data/workingEc/public"
    val workingDir = "testOut/ShuffleProofTest"
    val inputBallotFile = "$workingDir/inputCiphertexts.bt"
    val protInfo = "$workingDir/protocolInfo.xml"
    val publicKey: ElGamalPublicKey

    init {
        createDirectories(workingDir)
        val consumer : Consumer = makeConsumer(inputDir)
        val init = consumer.readElectionInitialized().unwrap()
        publicKey = init.jointPublicKey
    }

    @Test
    fun testSPVMatrix() {
        //val nthreads = listOf(1, 2, 4, 8, 12, 16, 20, 24, 32, 40, 48)
        //val nrows = listOf(100, 500, 1000, 2000, 4000)
        val nthreads = listOf(32, 40, 48)
        val nrows = listOf(500)
        val results = mutableListOf<Result>()

        for (nrow in nrows) {
            runShuffleProofVerify(nrow, 34, nthreads, results)
        }

        print("\nvmn-mixnet shuffle+proof X nrows (HP880) msecs/row\nnthreads, ")
        nrows.forEach { print("$it, ") }
        println()
        nthreads.forEach { nt ->
            print("$nt, ")
            var count = 0
            results.filter { it.nthreads == nt }.forEach {
                require( it.nrows == nrows[count])
                print("${(it.proof.toDouble()/it.nrows)}, ")
                count++
            }
            println()
        }
        println()

        print("\nvmn-mixnet verify X nrows (HP880) msecs/row\nnthreads, ")
        nrows.forEach { print("$it, ") }
        println()
        nthreads.forEach { n ->
            print("$n, ")
            var count = 0
            results.filter { it.nthreads == n }.forEach {
                require( it.nrows == nrows[count])
                print("${(it.verify.toDouble()/it.nrows)}, ")
                count++
            }
            println()
        }
        println()
    }

    fun runShuffleProofVerify(nrows: Int, width: Int, nthreads: List<Int>, results: MutableList<Result>) {
        println("=========================================")
        println("testThreads nrows=$nrows, width= $width per row, N=${nrows*width}")

        for (n in nthreads) {
            config(width)
            makeBallots(publicKey, nrows, width, inputBallotFile)
            results.add(runShuffleProofAndVerify(nrows, width, nthreads = n))
        }
    }

    fun runShuffleProofAndVerify(nrows: Int, width: Int, nthreads : Int = 10) : Result {
        UniformExecutors.clear()
        UniformExecutors.useCores = nthreads

        val stopwatch = Stopwatch()
        mix()
        val proofTime = stopwatch.elapsed()

        stopwatch.start()
        verify(width)
        val verifyTime = stopwatch.elapsed()

        return Result(nrows, nthreads, proofTime, verifyTime)
    }

    fun config(width: Int): ElGamalPublicKey {
        removeAllFiles(Path.of(workingDir))
        createDirectories(workingDir)

        val config = MixnetConfig(inputDir, workingDir)
        val publicKeyFilename = "$workingDir/publicKey.bt"
        writePublicKeyToByteFile(config.init.jointPublicKey.key, publicKeyFilename)

        val protoInfoFilename = config.makeProtoInfo(workingDir, pkey, width)
        val privInfoFilename = config.makePrivInfo(workingDir, rand, skey, keygen)

        // replace vmn -setpk
        config.setPublicKey(protoInfoFilename, privInfoFilename, publicKeyFilename)

        return config.init.jointPublicKey
    }

    fun makeBallots(key: ElGamalPublicKey, nrows: Int, width: Int, outputFile: String){
        val mixnetBallots = List(nrows) {
            val ciphertexts = List(width) {
                val vote = if (Random.nextBoolean()) 0 else  1
                vote.encrypt(key)
            }
            MixnetBallot(ciphertexts)
        }
        val tree = mixnetBallots.publish()
        writeByteTreeToFile(tree, outputFile)
    }

    fun mix() {
        val mixnet = Mixnet(privInfo = "$workingDir/privateInfo.xml", protInfo)
        mixnet.run(inputBallotFile, "mix1")
    }

    fun verify(width: Int) {
        //   org.cryptobiotic.verificabitur.vmn.RunMixnetVerifier \
        //    -protInfo ${VERIFICATUM_WORKSPACE}/protocolInfo.xml \
        //    -shuffle ${VERIFICATUM_WORKSPACE}/Party01/nizkp/mix1 \
        //    --sessionId mix1 \
        //    -width 21
        val verifier = Verifier("$workingDir/Party01/nizkp/mix1", protInfo, "mix1", width, verbose = false)
        verifier.verify()
    }

    // values are millisecs
    class Result(val nrows: Int, val nthreads: Int, val proof: Long, val verify : Long) {
        val total = (proof+verify)
        val scale = 1.0e-3

        override fun toString() =
            "${nrows}, ${nthreads}, ${proof*scale}, ${verify*scale}, ${total*scale}"
    }

}