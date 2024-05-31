package org.cryptobiotic.verificabitur.bytetree

import org.cryptobiotic.eg.core.*
import org.cryptobiotic.eg.core.ecgroup.EcGroupContext
import org.cryptobiotic.eg.core.intgroup.IntGroupContext
import org.cryptobiotic.eg.election.GroupType

// public key y = g^x
data class ECqPublicKey(val publicKey : ElementModP) {
    override fun toString(): String {
        return  "    group = ${this.publicKey.group.constants.name}\n" +
                "publicKey = ${this.publicKey}\n"
    }

    fun publish() : ByteTreeNode {
        val ecgroup = (publicKey.group as EcGroupContext)
        val groupBt = ecgroup.makeECqPGroupBt()
        return makeNode("ECqPublicKey",
            listOf(
                groupBt.publish(),
                makeNode("wtf",
                    listOf(
                        ElementBt(ecgroup.G_MOD_P).publish(), // wtf?
                        ElementBt(this.publicKey).publish()
                    )
                )
            )
        )
    }
}

/* EC
readByteTreeFromFile filename = src/test/data/demo/Party01/publicKey
read 224 bytes from src/test/data/demo/Party01/publicKey
root n=2 nbytes=224
  root-1 n=2 nbytes=52
    root-1-1 n=32 nbytes=37 content='636f6d2e766572696669636174756d2e61726974686d2e4543715047726f7570'
    root-1-2 n=5 nbytes=10 content='502d323536'
  root-2 n=2 nbytes=167
    root-2-1 n=2 nbytes=81
      root-2-1-1 n=33 nbytes=38 content='006b17d1f2e12c4247f8bce6e563a440f277037d812deb33a0f4a13945d898c296'
      root-2-1-2 n=33 nbytes=38 content='004fe342e2fe1a7f9b8ee7eb4a7c0f9e162bce33576b315ececbb6406837bf51f5'
    root-2-2 n=2 nbytes=81
      root-2-2-1 n=33 nbytes=38 content='0098e69fa341b64d1aa57506f5e8290930adb77dfc445525abd00a857358e16375'
      root-2-2-2 n=33 nbytes=38 content='0069c2ffe1ebde687d45ee1d22c6c93e2d506850c689de0dc079ef771244bb5680'

root-1-1 content as String = 'com.verificatum.arithm.ECqPGroup'
 */
fun ByteTreeNode.importECqPublicKey(group: GroupContext) : ECqPublicKey {
    val pknode = this.child[1].child[1]
    val pk = pknode.importElementBt(group)
    return ECqPublicKey(pk)
}
