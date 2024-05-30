package org.cryptobiotic.verificabitur.bytetree

import org.cryptobiotic.eg.core.GroupContext
import org.cryptobiotic.eg.core.normalize
import java.math.BigInteger

private const val BYTETREE_GROUP_NAME : String = "com.verificatum.arithm.ECqPGroup"


data class ECqPGroupBt(val name: String) {
    override fun toString(): String {
        return  "      name = ${this.name}\n"
    }
}

fun ByteTreeNode.importECqPGroup() : ECqPGroupBt {
    val className = String(this.child[0].content!!)
    require(className == BYTETREE_GROUP_NAME)
    val name = String(this.child[1].content!!)
    return ECqPGroupBt(name)
}

// root
//    root n=2 nbytes=52
//      root-1 n=32 nbytes=37 content='636f6d2e766572696669636174756d2e61726974686d2e4543715047726f7570'
//      root-2 n=5 nbytes=10 content='502d323536'

fun ECqPGroupBt.publish() : ByteTreeNode {
    return makeNode(name,
        listOf(
            makeLeaf("pname", BYTETREE_GROUP_NAME.toByteArray()),
            makeLeaf("name", name.toByteArray()),
        ))
}

fun GroupContext.makeECqPGroupBt() : ECqPGroupBt {
    return ECqPGroupBt("P-256")
}

fun readECqPGroupFromFile(filename : String) : ECqPGroupBt {
    val tree = readByteTreeFromFile(filename)
    require(tree.className == BYTETREE_GROUP_NAME)
    return tree.root.importECqPGroup()
}

// public abstract class ProtocolElGamalInterface {
//
//    /**
//     * Returns a byte tree representing a full public key.
//     *
//     * @param fullPublicKey Full public key.
//     */
//    protected ByteTreeBasic publicKeyToByteTree(final PGroupElement fullPublicKey) {
//
//        final PGroup pGroup = ((PPGroupElement) fullPublicKey).project(0).getPGroup();
//        final ByteTreeBasic gbt = Marshalizer.marshal(pGroup);
//        final ByteTreeBasic kbt = fullPublicKey.toByteTree();
//
//        return new ByteTreeContainer(gbt, kbt);
//    }

// public final class ECqPGroupElement extends BPGroupElement {
//    LargeInteger x;
//    LargeInteger y;
// ...
//     public ByteTree toByteTree() {
//        PField primeOrderField = ((ECqPGroup)this.pGroup).primeOrderField;
//        int byteLength = primeOrderField.getByteLength();
//        return new ByteTree(new ByteTree[]{new ByteTree(this.innerToByteArray(byteLength, this.x)), new ByteTree(this.innerToByteArray(byteLength, this.y))});
//    }
//
//     protected byte[] innerToByteArray(int len, LargeInteger x) {
//        byte[] res = new byte[len];
//        if (x.equals(MINUS_ONE)) {
//            Arrays.fill(res, (byte)-1);
//        } else {
//            byte[] tmp = x.toByteArray();  // BigIntger.toByteArray()
//            System.arraycopy(tmp, 0, res, res.length - tmp.length, tmp.length);
//        }
//
//        return res;
//    }

// public final class ModPGroupElement extends BPGroupElement {
//    LargeInteger value;
//
//     public ByteTreeBasic toByteTree() {
//        final byte[] temp = value.toByteArray();
//        final byte[] result = new byte[((ModPGroup) pGroup).modulusByteLength];
//
//        // We know that temp.length <= modulusByteLength
//        // normalize with leading zeroes
//        Arrays.fill(result, 0, ((ModPGroup) pGroup).modulusByteLength - temp.length, (byte) 0);
//
//        System.arraycopy(temp,
//                         0,
//                         result,
//                         ((ModPGroup) pGroup).modulusByteLength - temp.length,
//                         temp.length);
//
//        return new ByteTree(result);
//    }
