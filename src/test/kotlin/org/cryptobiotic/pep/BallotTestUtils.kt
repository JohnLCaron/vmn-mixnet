package org.cryptobiotic.pep

import com.github.michaelbull.result.*
import org.cryptobiotic.eg.election.EncryptedTally
import org.cryptobiotic.eg.election.Guardian
import org.cryptobiotic.eg.core.ElGamalCiphertext
import org.cryptobiotic.eg.core.ElGamalPublicKey
import org.cryptobiotic.eg.core.UInt256
import org.cryptobiotic.eg.decrypt.DecryptingTrustee
import org.cryptobiotic.eg.keyceremony.KeyCeremonyTrustee

fun makeDoerreTrustee(ktrustee: KeyCeremonyTrustee): DecryptingTrustee {
    return DecryptingTrustee(
        ktrustee.id,
        ktrustee.xCoordinate,
        ElGamalPublicKey(ktrustee.guardianPublicKey()),
        ktrustee.computeSecretKeyShare(),
    )
}

fun makeGuardian(trustee: KeyCeremonyTrustee): Guardian {
    val publicKeys = trustee.publicKeys().unwrap()
    return Guardian(
        trustee.id,
        trustee.xCoordinate,
        publicKeys.coefficientProofs,
    )
}

fun makeTallyForSingleCiphertext(ciphertext : ElGamalCiphertext, extendedBaseHash : UInt256) : EncryptedTally {
    val selection = EncryptedTally.Selection("Selection1", 1, ciphertext)
    val contest = EncryptedTally.Contest("Contest1", 1, listOf(selection))
    return EncryptedTally("tallyId", listOf(contest), emptyList(), extendedBaseHash)
}
