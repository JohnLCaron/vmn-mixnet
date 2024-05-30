package org.cryptobiotic.verificabitur.bytetree

import com.verificatum.arithm.ECqPGroup
import com.verificatum.arithm.ECqPGroupParams
import org.cryptobiotic.eg.core.*
import org.cryptobiotic.eg.core.Base16.toHex
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private val expect = "00000000020100000020636f6d2e766572696669636174756d2e61726974686d2e4543715047726f75700100000005502d323536"

class ECqPGroupBtTest {
    val group = productionGroup()

    @Test
    fun testRoundtrip() {
        val tree = readByteTree(expect)
        val ecGroup = tree.root.importECqPGroup()
        println("\nimportECqPGroup\n$ecGroup")
        println("\nroot\n${tree.root.show()}")

        val node: ByteTreeNode = ecGroup.publish()
        println("\npublish\n${node.show()}")
        assertTrue(tree.root.array().contentEquals(node.array()))
    }

    @Test
    fun testMakeGroupBt() {
        val ecGroup = group.makeECqPGroupBt()
        println("\nreadModPGroup\n$ecGroup")
        val btree = ecGroup.publish()
        println("\nbt\n${btree.show()}")
        val hex : String = btree.hex()
        assertEquals(expect, hex)
    }

    @Test
    fun testVmn() {
        val ecgroup: ECqPGroup = ECqPGroupParams.getECqPGroup("P-256")
        val btree = ecgroup.toByteTree()
        val bas = ByteArrayOutputStream()
        val das = DataOutputStream(bas)
        btree.writeTo(das)
        val ba = bas.toByteArray()
        println("hex = ${ba.toHex()}")
    }
}

