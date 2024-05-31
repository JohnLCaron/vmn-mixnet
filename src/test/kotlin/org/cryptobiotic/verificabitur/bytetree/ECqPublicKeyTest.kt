package org.cryptobiotic.verificabitur.bytetree

import org.cryptobiotic.eg.core.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** Compare ElectionGuard and Verificatum group definitions */
class ECqPublicKeyTest {
    val group = productionGroup("P-256")
    val ecpkFilename = "src/test/data/demo/Party01/publicKey"

    @Test
    fun testDemoPublicKeyFile() {
        testReadRavePublicKeyFile(ecpkFilename)
    }

    @Test
    fun testEcPublicKeyFile() {
        testReadRavePublicKeyFile("working/vf/publicKey.bt")
    }

    fun testReadRavePublicKeyFile(filename: String): String? {
        println()
        println("readByteTreeFromFile filename = ${filename}")
        val tree = readByteTreeFromFile(filename)
        println(tree.show(10))

        val node = findNodeByName(tree.root, "root-1-1")
        if (node == null) return null
        assertTrue(node.isLeaf)
        assertNotNull(node.content)
        println("root-1-1 content as String = '${String(node.content!!)}'\n")
        return String(node.content!!)
    }

    @Test
    fun testReadAsByteTree() {
        val filename = ecpkFilename
        println("readByteTreeFromFile filename = ${filename}")
        val tree = readByteTreeFromFile(filename)
        println(tree.show(10))

        val mixnetPublicKey = tree.root.importECqPublicKey(group)
        val node: ByteTreeNode = mixnetPublicKey.publish()
        println(node.show())

        tree.root.compareContents(node)

        val a1 = tree.root.array()
        val a2 = node.array()
        assertEquals(tree.root.array().size, node.array().size)
        repeat(a1.size) {
            if (a1[it] != a2[it]) println("$it ${a1[it]} != ${a2[it]}")
        }
        //assertEquals(a1.toHex(), a2.toHex())
        //assertTrue(tree.root.array().contentEquals(node.array()))
    }

    @Test
    fun testPublishImportRoundtrip() {
        val filename = ecpkFilename
        println("readPublicKeyFile filename = ${filename}")
        val mpk = ECqPublicKey(readPublicKeyFromByteFile(filename, group))
        println( "MixnetPublicKey = \n${mpk}")

        val root = mpk.publish()
        println(root.show())

        val mixnetPublicKey = root.importECqPublicKey(group)
        val node: ByteTreeNode = mixnetPublicKey.publish()
        println("\npublish\n${node.show()}")

        assertTrue(root.array().contentEquals(node.array()))
    }

    @Test
    fun testCreatePublishImportRoundtrip() {
        val pk = elGamalKeyPairFromRandom(group).publicKey.key
        val ecpk = ECqPublicKey(pk)
        val btnode = ecpk.publish()
        println(btnode.show())

        val roundtripPk: ECqPublicKey = btnode.importECqPublicKey(group)
        assertEquals(ecpk, roundtripPk)
    }

}