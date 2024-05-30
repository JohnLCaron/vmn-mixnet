package org.cryptobiotic.verificabitur.bytetree

import org.cryptobiotic.eg.core.ElementModP
import org.cryptobiotic.eg.core.GroupContext
import org.cryptobiotic.eg.core.ecgroup.EcElementModP
import org.cryptobiotic.eg.core.ecgroup.EcGroupContext
import org.cryptobiotic.eg.core.ecgroup.VecElementP
import org.cryptobiotic.eg.core.normalize
import org.cryptobiotic.eg.election.GroupType
import java.math.BigInteger

data class ElementBt(val elem: ElementModP) {
    val isEC = elem.group.constants.type == GroupType.EllipticCurve

    fun publish() : ByteTreeNode {
        return if (isEC) publishEC() else publishMod()
    }

    fun publishMod() : ByteTreeNode {
        return makeLeaf("ElementModP", this.elem.byteArray().normalize(513))
    }

    fun publishEC() : ByteTreeNode {
        val vec: VecElementP = (elem as EcElementModP).ec
        return makeNode("ElementModP",
            listOf(
                makeLeaf("x", vec.x.toByteArray().normalize(33)),
                makeLeaf("y", vec.y.toByteArray().normalize(33)),
            )
        )
    }
}

fun ByteTreeNode.importElementBt(group: GroupContext) : ElementModP {
    return if (group.constants.type == GroupType.EllipticCurve) {
        val ecgroup = group as EcGroupContext
        val x = BigInteger(1, child[0].content!!)
        val y = BigInteger(1, child[1].content!!)
        val vec = VecElementP(ecgroup.vecGroup, x, y)
        EcElementModP(ecgroup, vec)
    } else {
        group.binaryToElementModP(child[0].content!!)!!
    }
}