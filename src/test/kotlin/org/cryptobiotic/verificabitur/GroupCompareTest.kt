package org.cryptobiotic.verificabitur

import com.verificatum.arithm.*
import com.verificatum.arithm.ModPGroup.SAFEPRIME_ENCODING
import com.verificatum.crypto.RandomDevice
import org.cryptobiotic.eg.core.*
import org.cryptobiotic.eg.core.Base16.toHex
import org.cryptobiotic.eg.core.intgroup.IntGroupContext
import java.math.BigInteger
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

/** Compare ElectionGuard and Verificatum group definitions */
class GroupCompareTest {

    @Test
    fun testEgkGroup() {
        val group = productionGroup()
        println("group constants = ${group.constants}")
    }

    // This is to get the string representation of the ModPGroup equivilent to EG group.
    @Test
    fun testModPGroupGen() {
        val group = productionGroup("Integer4096") as IntGroupContext
        val egkConstants = group.groupConstants

        val modulus = convert(egkConstants.largePrime.toByteArray())
        val order = convert(egkConstants.smallPrime.toByteArray())
        val gli = convert(egkConstants.generator.toByteArray())

        //     public ModPGroup(final LargeInteger modulus,
        //                     final LargeInteger order,
        //                     final LargeInteger gli,
        //                     final int encoding,
        //                     final RandomSource rs,
        //                     final int certainty)
        val vcrGroup = ModPGroup(modulus, order, gli, SAFEPRIME_ENCODING, RandomDevice(), 50)
        println("vcrGroup = '$vcrGroup'")

        // val help = ModPGroupGen().gen(RandomDevice(), arrayOf("-h"))
        // println("help: ${help}")

        // RandomSource randomSource, final String[] args
        val genGroupDesc = ModPGroupGen().gen(
            RandomDevice(), arrayOf(
                "-explic",
                normalize(modulus),
                normalize(gli),
                normalize(order),
                //       "-v",
            )
        )
        println("genGroupDesc = '$genGroupDesc'")
    }

    @Test
    fun testVcrPGroup() {
        val group = productionGroup("Integer4096") as IntGroupContext
        val egkConstants = group.groupConstants

        val modulus = convert(egkConstants.largePrime.toByteArray())
        val order = convert(egkConstants.smallPrime.toByteArray())
        val gli = convert(egkConstants.generator.toByteArray())

        //     public ModPGroup(final LargeInteger modulus,
        //                     final LargeInteger order,
        //                     final LargeInteger gli,
        //                     final int encoding,
        //                     final RandomSource rs,
        //                     final int certainty)
        val vcrGroup = ModPGroup(modulus, order, gli, SAFEPRIME_ENCODING, RandomDevice(), 50)
        val g = (vcrGroup.getg() as ModPGroupElement).toLargeInteger()

        testByteArrayEquals(egkConstants.generator.toByteArray(), g.toByteArray())
        testByteArrayEquals(egkConstants.smallPrime.toByteArray(), vcrGroup.elementOrder.toByteArray())
        testByteArrayEquals(egkConstants.largePrime.toByteArray(), vcrGroup.modulus.toByteArray())
        // testEquals(egkConstants.cofactor.toHex(), vcrGroup.coOrder)
        println("\nvcrGroup = $vcrGroup")

        val ntrials = 1000

        // test g^q
        repeat(ntrials) {
            val randomQ = group.randomElementModQ()
            val gp: ElementModP = group.gPowP(randomQ)
            val vgp: LargeInteger = gli.modPow(convert(randomQ), modulus)
            testByteArrayEquals(gp.byteArray(), vgp.toByteArray())
            // println(" $randomQ ok")
        }

        val starting = getSystemTimeInMillis()
        var summ: ElementModP = group.ONE_MOD_P
        repeat(ntrials) {
            val randomQ = group.randomElementModQ()
            val wtf = convert(randomQ)
            val gp: ElementModP = group.gPowP(randomQ)
            summ = summ.times(gp)
        }
        val egk = getSystemTimeInMillis() - starting
        println("egk.Group took ${getSystemTimeInMillis() - starting}")

        val starting2 = getSystemTimeInMillis()
        var summ2: LargeInteger = LargeInteger.ONE
        repeat(ntrials) {
            val randomQ = group.randomElementModQ()
            val vgp: LargeInteger = gli.modPow(convert(randomQ), modulus)
            summ2 = LargeInteger.modProd(arrayOf(summ2, vgp), modulus)
        }
        val vcr = getSystemTimeInMillis() - starting2
        println("vcr.ModPGroup took ${getSystemTimeInMillis() - starting2}")

        println("speedup old/new = ${vcr.toDouble() / egk}")

        // fails
        // testEquals(summ, normalize(summ2))
    }
}

fun testEquals(egk: Any, vcr: Any) {
    assertEquals(egk.toString().lowercase(), vcr.toString())
}

fun testByteArrayEquals(egk: ByteArray, vcr: ByteArray) {
    val nbytes = egk.size
    val vcrn = vcr.normalize(nbytes)
    assertContentEquals(egk, vcrn)
}

fun convert(ba : ByteArray) : LargeInteger {
    return LargeInteger(BigInteger(1, ba))
}

fun convert(elem : Element) : LargeInteger {
    return LargeInteger(BigInteger(1, elem.byteArray()))
}

fun normalize(li : LargeInteger) : String {
    val ba = li.toBigInteger().toByteArray()
    val nba = ba.normalize(512)
    return nba.toHex().lowercase()
}

fun normalize(bi : BigInteger, nbytes : Int = 512) : String {
    val ba = bi.toByteArray()
    val nba = ba.normalize(nbytes)
    return nba.toHex().lowercase()
}
