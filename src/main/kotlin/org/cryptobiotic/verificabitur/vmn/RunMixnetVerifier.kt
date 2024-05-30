package org.cryptobiotic.verificabitur.vmn

import com.verificatum.crypto.RandomDevice
import com.verificatum.eio.ExtIO
import com.verificatum.protocol.Protocol
import com.verificatum.protocol.ProtocolError
import com.verificatum.protocol.ProtocolFormatException
import com.verificatum.protocol.elgamal.ProtocolElGamalInterface
import com.verificatum.protocol.elgamal.ProtocolElGamalInterfaceFactory
import com.verificatum.protocol.mixnet.MixNetElGamalInterfaceFactory
import com.verificatum.protocol.mixnet.MixNetElGamalVerifyFiatShamir
import com.verificatum.util.SimpleTimer
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.required
import java.io.File
import java.io.IOException

class RunMixnetVerifier {

    // vmnv -shuffle -width "${WIDTH}" -auxsid "${AUXSID}" \
    //   ${VERIFICATUM_WORKSPACE}/protInfo.xml \
    //   ./dir/nizkp/${AUXSID} -v

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            val parser = ArgParser("RunMixnetVerifier")
            val inputDir by parser.option(
                ArgType.String,
                shortName = "shuffle",
                description = "Directory containing public shuffle info"
            ).required()
            val protInfo by parser.option(
                ArgType.String,
                shortName = "protInfo",
                description = "Protocol info file"
            ).default("protInfo.xml")
            val sessionId by parser.option(
                ArgType.String,
                shortName = "auxsid",
                description = "Auxiliary session identifier used to distinguish different sessions of the mix-net"
            ).required()
            val width by parser.option(
                ArgType.Int,
                shortName = "width",
                description = "Number of ciphertexts per row"
            ).required() // TODO get rid of

            parser.parse(args)

            println(
                "RunMixnetVerifier starting\n" +
                        "   inputDir= $inputDir\n" +
                        "   protInfo = $protInfo\n" +
                        "   width = $width\n" +
                        "   sessionId = $sessionId\n"
            )

            val timer = SimpleTimer();
            val verifier = Verifier(inputDir, protInfo, sessionId, width, true)
            verifier.verify()
            println("sessionId $sessionId complete successfully")
            println("RunMixnetVerifier elapsed time = ${timer.elapsed()} msecs ($timer)")
        }
    }
}


class Verifier(shuffleDir: String, protInfo: String, val auxsid: String, val width: Int, val verbose: Boolean) {
    val elGamalRawInterface: ProtocolElGamalInterface
    val verifier: MixNetElGamalVerifyFiatShamir
    val shuffleDirFile = File(shuffleDir)

    init {
        val factory: ProtocolElGamalInterfaceFactory = MixNetElGamalInterfaceFactory()

        try {
            elGamalRawInterface = factory.getInterface("raw")
        } catch (pfe: ProtocolFormatException) {
            throw ProtocolError("Unable to get raw interface!", pfe)
        }

        val protocolInfoFile = File(protInfo)

        val generator = factory.getGenerator(protocolInfoFile)
        val protocolInfo = Protocol.getProtocolInfo(generator, protocolInfoFile)
        println("Using Generator class = ${generator.javaClass.name}")
        // println("Using Protocol = ${protocolInfo.toXML()}")

        verifier = MixNetElGamalVerifyFiatShamir(
            protocolInfo,
            RandomDevice(),
            System.out,
            verbose,
            emptySet(),
            true,
        )
    }

    fun verify() {
        var timer = SimpleTimer()

        verifier.verify(shuffleDirFile, auxsid, width)

        if (verbose) {
            val nizkpSize: Long
            try {
                nizkpSize = ExtIO.fileSize(shuffleDirFile)
            } catch (ioe: IOException) {
                val e = "Unable to determine communicated bytes!"
                throw ProtocolError(e, ioe)
            }

            println("Proof size is ${ExtIO.bytesToHuman(nizkpSize)}  ($nizkpSize bytes)")
            println("Completed verification after $timer  (${timer.elapsed()} ms")
            println()
        }
    }

}