package org.cryptobiotic.egk

import com.github.michaelbull.result.*
import org.cryptobiotic.eg.cli.RunTrustedTallyDecryption.Companion.readDecryptingTrustees
import org.cryptobiotic.eg.election.ElectionInitialized
import org.cryptobiotic.eg.core.*
import org.cryptobiotic.eg.decrypt.*
import org.cryptobiotic.eg.publish.makeConsumer

/**
 * Shortcut to decryption when you're willing to compute the secret key, eg for testing.
 * Should be replaced by a distributed algorithm where s is never computed.
 * Uses ElectionInitialized.guardians to read DecryptingTrustee's from trusteeDir
 */
class CiphertextDecryptor(
    val group: GroupContext,
    inputDir: String,
    trusteeDir: String,
    missing: String? = null
) {
    val trustees : List<DecryptingTrusteeIF>
    val lagrangeCoeff : List<ElementModQ>
    val secretKey : ElementModQ
    val keyPair : ElGamalKeypair
    val init : ElectionInitialized

    init {
        val consumerIn = makeConsumer(inputDir, group)
        val initResult = consumerIn.readElectionInitialized()
        if (initResult is Err) {
            throw RuntimeException(initResult.error.toString())
        }
        init = initResult.unwrap()
        trustees = readDecryptingTrustees(inputDir, trusteeDir, missing)

        /*
        val trusteeSource: Consumer = makeTrusteeSource(trusteeDir, group, consumerIn.isJson())
        val readTrusteeResults: List<Result<DecryptingTrusteeIF, ErrorMessages>> =
            init.guardians.map { trusteeSource.readTrustee(trusteeDir, it.guardianId) }
        val (allTrustees, allErrors) = readTrusteeResults.partition()
        if (allErrors.isNotEmpty()) {
            throw RuntimeException(mergeErrorMessages("readDecryptingTrustees", allErrors).toString())
        }
        trustees = if (missing.isNullOrEmpty()) {
            allTrustees
        } else {
            // remove missing guardians
            val missingX = missing.split(",").map { it.toInt() }
            allTrustees.filter { !missingX.contains(it.xCoordinate()) }
        }

         */

        // build the lagrangeCoordinates once and for all
        val coeffs = mutableListOf<ElementModQ>()
        for (trustee in trustees) {
            // available trustees minus me
            val present: List<Int> = trustees.filter { it.id() != trustee.id() }.map { it.xCoordinate() }
            coeffs.add( computeLagrangeCoefficient(group, trustee.xCoordinate(), present))
        }
        this.lagrangeCoeff = coeffs

        // The decryption M = A^s mod p can be computed as shown in Equation (68) because
        // s = Sum( wi * P(i)) mod q,  i∈U
        secretKey = group.addQ(
            trustees.mapIndexed { idx, it -> (it as DecryptingTrustee).keyShare * lagrangeCoeff[idx] }
        )
        require(init.jointPublicKey.key == group.gPowP(secretKey))
        keyPair = ElGamalKeypair(ElGamalSecretKey(secretKey), init.jointPublicKey)
    }


    fun makeDecryptorDoerre() : TallyDecryptor {
        val guardians = Guardians(group, init.guardians)
        return TallyDecryptor(group, init.extendedBaseHash, init.jointPublicKey, guardians, trustees)
    }

    fun decrypt(ciphertext : ElGamalCiphertext) : Int? {
        return ciphertext.decrypt(keyPair)
    }

    // T = B · M−1 mod p; spec 2.0.0, eq 64
    // PEP means "dont take the log"
    fun decryptPep(ciphertext : ElGamalCiphertext): ElementModP {
        val blind = ciphertext.pad powP keyPair.secretKey.negativeKey // M-1 = A ^ -s
        return ciphertext.data * blind
    }

    /*
    // T = B · M−1 mod p; spec 2.0.0, eq 64
    fun decryptPepWithProof(ciphertext : ElGamalCiphertext): PepWithProof {
        val M = ciphertext.pad powP keyPair.secretKey.key // M = A ^ s, spec 2.0.0, eq 66
        val bOverM = ciphertext.data / M
        val u = group.randomElementModQ() // random value u in Zq
        val a = group.gPowP(u)  // g ^ u
        val b = ciphertext.pad powP u // A ^ u

        // "collective challenge" c = H(HE ; 0x30, K, A, B, a, b, M) ; spec 2.0.0 eq 71
        val challenge = hashFunction(
            init.extendedBaseHash.bytes,
            0x30.toByte(),
            keyPair.publicKey.key,
            ciphertext.pad,
            ciphertext.data,
            a, b, M).toElementModQ(group)

        val v = u - challenge * keyPair.secretKey.key
        return PepWithProof(bOverM, ChaumPedersenProof(challenge, v))
    }

     */
}