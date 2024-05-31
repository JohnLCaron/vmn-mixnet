package org.cryptobiotic.verificabitur.bytetree

import com.github.michaelbull.result.unwrap
import org.cryptobiotic.eg.core.*
import org.cryptobiotic.eg.core.Base16.toHex
import org.cryptobiotic.eg.publish.Consumer
import org.cryptobiotic.eg.publish.makeConsumer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** Compare ElectionGuard and Verificatum group definitions */
class ModPublicKeyTest {
    val group = productionGroup("Integer4096")
    val inputDir = "src/test/data/working"

    @Test
    fun testReadRavePublicKeyFile() {
        val filename = "$inputDir/vf/publicKey.bt"
        val className = testReadRavePublicKeyFile(filename)
        assertEquals("com.verificatum.arithm.ModPGroup", className)
    }

    @Test
    fun testOtherPublicKeyFile() {
        val otherDir = "src/test/data/publicKeys"
        testReadRavePublicKeyFile("$otherDir/publickey.raw")
        testReadRavePublicKeyFile("$otherDir/FullPublicKey.bt")
    }

    @Test
    fun testDemoPublicKeyFile() {
        val demoDir = "src/test/data/demo"
        testReadRavePublicKeyFile("$demoDir/Party01/publicKey")
    }

    @Test
    fun testEcPublicKeyFile() {
        testReadRavePublicKeyFile("/home/stormy/dev/github/egk-rave/working/vf/publicKey.bt")
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
        val filename = "$inputDir/vf/publicKey.bt"
        println("readByteTreeFromFile filename = ${filename}")
        val tree = readByteTreeFromFile(filename)
        println(tree.show(10))

        val mixnetPublicKey = tree.root.importModPublicKey(group)
        val node: ByteTreeNode = mixnetPublicKey.publish()
        println(node.show())

        tree.root.compareContents(node)

        val a1 = tree.root.array()
        val a2 = node.array()
        assertEquals(tree.root.array().size, node.array().size)
        repeat(a1.size) {
            if (a1[it] != a2[it]) println("$it ${a1[it]} != ${a2[it]}")
        }
        assertEquals(a1.toHex(), a2.toHex())
        assertTrue(tree.root.array().contentEquals(node.array()))
    }

    @Test
    fun testRoundtrip() {
        val filename = "$inputDir/vf/publicKey.bt"
        println("readPublicKeyFile filename = ${filename}")
        val mpk = ModPublicKey(readPublicKeyFromByteFile(filename, group))
        println( "MixnetPublicKey = \n${mpk}")

        val root = mpk.publish()
        println(root.show())

        val mixnetPublicKey = root.importModPublicKey(group)
        val node: ByteTreeNode = mixnetPublicKey.publish()
        println("\npublish\n${node.show()}")

        assertTrue(root.array().contentEquals(node.array()))
    }

    // @Test
    fun testComparePublicKey() {
        val egdir = "$inputDir/eg"
        val consumer : Consumer = makeConsumer(egdir)
        val init = consumer.readElectionInitialized().unwrap()

        val filename = "$inputDir/vf/publicKey.bt"
        println("readPublicKeyFile filename = ${filename}")
        val mpk = readPublicKeyFromByteFile(filename, consumer.group)
        println( "MixnetPublicKey = \n${mpk}")

        assertEquals(init.jointPublicKey.key, mpk)
    }

}