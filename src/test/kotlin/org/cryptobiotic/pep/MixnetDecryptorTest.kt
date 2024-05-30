package org.cryptobiotic.pep

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.unwrap
import org.cryptobiotic.eg.core.ElGamalCiphertext
import org.cryptobiotic.eg.core.ElementModP
import org.cryptobiotic.eg.core.productionGroup
import org.cryptobiotic.eg.publish.makeConsumer
import org.cryptobiotic.egk.CiphertextDecryptor
import org.cryptobiotic.verificabitur.bytetree.ByteTreeNode
import org.cryptobiotic.verificabitur.bytetree.readByteTreeFromFile
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.fail

// check that the mixnet output decrypts properly
class MixnetDecryptorTest {
    val inputDir = "src/test/data/working"
    val egDir = "$inputDir/eg"
    val vfDir = "$inputDir/vf"
    val bbDir = "$inputDir/bb"
    val group = productionGroup()

    @Test
    fun testMixnetInput() {
        val root = readByteTreeFromFile("$vfDir/inputCiphertexts.bt")
        val ptree = convertByteTree(root.root)

        val consumer = makeConsumer("$egDir")
        val result = consumer.readEncryptedBallot(
            "$inputDir/bb/encryptedBallots",
            "id-1066432929"
        )
        if (result is Err) {
            println(result.error)
            fail()
        } else {
            val eballot = result.unwrap()
            var count = 1
            eballot.contests.forEach {
                it.selections.forEach {
                    val ciphertext = it.encryptedVote
                    val where = ptree.findCiphertext(ciphertext.pad)
                    if (where != null) {
                        println("$count found ${it.selectionId} in $where")
                    } else {
                        println("$count not found ${it.selectionId}")
                        fail()
                    }
                    count++
                }
            }
        }
    }

    @Test
    fun testCiphertextDecryptor() {
        val decryptor = CiphertextDecryptor(
            group,
            "$egDir",
            "$egDir/trustees",
        )

        val consumer = makeConsumer("$egDir")
        val result = consumer.readEncryptedBallot(
            "$inputDir/bb/encryptedBallots",
            "id711984157"
        )
        if (result is Err) {
            println(result.error)
        } else {
            val eballot = result.unwrap()
            var count = 1
            eballot.contests.forEach {
                it.selections.forEach {
                    val ciphertext = it.encryptedVote
                    val vote = decryptor.decrypt(ciphertext)
                    println("$count ${it.selectionId} vote $vote")
                    assertNotNull(vote)
                    count++
                }
            }
        }
    }

    @Test
    fun testDecryptMixnetOutput() {
        val root = readByteTreeFromFile("$bbDir/vf/mix2/ShuffledCiphertexts.bt")
        val ptree = convertByteTree(root.root)
        println(ptree)
        val ctree = convertPTree(ptree)
        println(ctree)

        val decryptor = CiphertextDecryptor(
            group,
            "$egDir",
            "$egDir/trustees",
        )

        decryptor.checkCipherTextDecrypts(ctree)
    }

    fun CiphertextDecryptor.checkCipherTextDecrypts(ctree : CTree) {
        if (ctree.ciphertext != null) {
            val vote = this.decrypt(ctree.ciphertext)
            println("${ctree.name} vote $vote")
            assertNotNull(vote)
        } else {
            ctree.children.forEach { this.checkCipherTextDecrypts(it) }
        }
    }

    data class PTree(val name: String, val modp: ElementModP?) {
        val children = mutableListOf<PTree>()

        fun findCiphertext(wantp: ElementModP) : PTree? {
            if (modp != null && modp == wantp) return this
            children.forEach {
                val found = it.findCiphertext(wantp)
                if (found != null) return found
            }
            return null
        }
    }

    fun convertByteTree(node: ByteTreeNode): PTree {
        val ptree = PTree(node.name, convertContent(node.content))
        node.child.forEach { child ->
            ptree.children.add(convertByteTree(child))
        }
        return ptree
    }

    fun convertContent(content: ByteArray?): ElementModP? {
        if (content == null) return null
        // remove first byte
        val n = content.size
        val ba = ByteArray(n - 1) { it -> content[it + 1] }
        val p = group.binaryToElementModP(ba)
        return p
    }

    data class CTree(val name: String, val ciphertext: ElGamalCiphertext?) {
        val children = mutableListOf<CTree>()

        fun add(pad: PTree, data: PTree) {
            if (pad.modp != null && data.modp != null) {
                children.add(CTree(pad.name, ElGamalCiphertext(pad.modp, data.modp)))
            } else {
                require(pad.children.size == data.children.size)
                val result = CTree(pad.name, null)
                pad.children.zip(data.children).forEach { (padc, datac) ->
                    result.add(padc, datac)
                }
                children.add(result)
            }
        }
    }

    fun convertPTree(root: PTree): CTree {
        require(root.children.size == 2)
        val pad = root.children[0]
        val data = root.children[1]
        val ctree = CTree(root.name, null)
        ctree.add(pad, data)
        return ctree
    }
}