package org.cryptobiotic.verificabitur.bytetree

import org.cryptobiotic.eg.core.productionGroup
import kotlin.test.Test

class ByteTreeReaderTest {
    val demoDir = "/home/stormy/dev/verificatum-vmn-3.1.0-full/verificatum-vmn-3.1.0/demo/mixnet/mydemodir/"
    val inputDir = "src/test/data/working/vf"
    val protInfo = "$inputDir/protocolInfo.xml"
    val nizkpDir = "$inputDir/Party01/nizkp/mix2/proofs"
    val group = productionGroup("Integer4096")

    @Test
    fun testReadRaveInput() {
        readByteTreeFromFile("$inputDir/inputCiphertexts.bt", 2)
    }

    @Test
    fun testReadRaveInput2() {
        readByteTreeFromFile("working/vf/inputCiphertexts.bt", 2)
    }

    @Test
    fun testReadRaveOutput() {
        readByteTreeFromFile("$inputDir/Party01/nizkp/mix1/ShuffledCiphertexts.bt", 2)
        readByteTreeFromFile("$inputDir/Party01/nizkp/mix2/ShuffledCiphertexts.bt", 2)
    }

    @Test
    fun testReadRavePublicKeyFile() {
        val filename = "src/test/data/workingEc/vf/Party01/nizkp/mix1/FullPublicKey.bt"
        println("readPublicKeyFile filename = ${filename}")
        val root = readByteTreeFromFile(filename)
        println(root.show(10))
    }

    // MixnetPublicKey =
    //   key1 = 36036FED214F3B50DC566D3A312FE4131FEE1C2BCE6D02EA39B477AC05F7F885F38CFE77A7E45ACF4029114C4D7A9BFE058BF2F995D2479D3DDA618FFD910D3C4236AB2CFDD783A5016F7465CF59BBF45D24A22F130F2D04FE93B2D58BB9C1D1D27FC9A17D2AF49A779F3FFBDCA22900C14202EE6C99616034BE35CBCDD3E7BB7996ADFE534B63CCA41E21FF5DC778EBB1B86C53BFBE99987D7AEA0756237FB40922139F90A62F2AA8D9AD34DFF799E33C857A6468D001ACF3B681DB87DC4242755E2AC5A5027DB81984F033C4D178371F273DBB4FCEA1E628C23E52759BC7765728035CEA26B44C49A65666889820A45C33DD37EA4A1D00CB62305CD541BE1E8A92685A07012B1A20A746C3591A2DB3815000D2AACCFE43DC49E828C1ED7387466AFD8E4BF1935593B2A442EEC271C50AD39F733797A1EA11802A2557916534662A6B7E9A9E449A24C8CFF809E79A4D806EB681119330E6C57985E39B200B4893639FDFDEA49F76AD1ACD997EBA13657541E79EC57437E504EDA9DD011061516C643FB30D6D58AFCCD28B73FEDA29EC12B01A5EB86399A593A9D5F450DE39CB92962C5EC6925348DB54D128FD99C14B457F883EC20112A75A6A0581D3D80A3B4EF09EC86F9552FFDA1653F133AA2534983A6F31B0EE4697935A6B1EA2F75B85E7EBA151BA486094D68722B054633FEC51CA3F29B31E77E317B178B6B9D8AE0F
    //   key2 = F670BAC355C05A2E3C2C67C5F4952AB7C086CAE24DF857984892866B7524E538F7B6BA2217AFF9FFEEAC56E7029BE8005D6E8C0FFBE84EDA5B7A3F8051BC7511DE4182A6FF1BCE306B5D5164441FA00C8FF2A77ECBEA4DDDA59816765DD13504624E12F95C5DD7DC31BDA23C573181083A37409380DAAEFC693E6A17049777140124C39AA842E5762244E68EC06EDF8AF90AADF18BCB3F2816B50802C2CFCEFD71514DDF19A785659F74B02D361E8B29B51870B66AFC10173BAABC4E385699590D57239AB9E57D3D5E7C81C65F4C3332C5085C3D170AA39B98FE4FB8072FF2D6F0981EC594C2ABEC25229015EA58AC3DBE7EC77EB528262E89147CD5A270E90E8ED45F4C4E6D14B430888D9CDE7448887E6CBDAC83DCF8341846382DBF976ACF3DE02E285685F0C6D38767524AD1075D830AA573D8F6DE95C504D5313FEF173DEF32A3430AA709FF78401F3C8AF8003337CE3AC2DF7F255D816110F76BBF61C0A7D48C8F5304031EDCD414B8A54187C2AE8F3180947F6005C12CA37B82BDF96240AC4CFEA9B07CFED696F8EC09A90784A55D02982313F507494628F93D6F6C8D011EF9207EF88041E79CE1A29499B90E5992B2CCE58424DE32496E838209AFF5D27EF227777C5D298852988081AC304E36B2C9A793F7CEF8C3747A75043C270C2B5C2013185A1E0EB6A0CBAB7E216A3F070C80B0667135D51514BF4F424535F7
    //         "F670BAC355C05A2E3C2C67C5F4952AB7C086CAE24DF857984892866B7524E538F7B6BA2217AFF9FFEEAC56E7029BE8005D6E8C0FFBE84EDA5B7A3F8051BC7511DE4182A6FF1BCE306B5D5164441FA00C8FF2A77ECBEA4DDDA59816765DD13504624E12F95C5DD7DC31BDA23C573181083A37409380DAAEFC693E6A17049777140124C39AA842E5762244E68EC06EDF8AF90AADF18BCB3F2816B50802C2CFCEFD71514DDF19A785659F74B02D361E8B29B51870B66AFC10173BAABC4E385699590D57239AB9E57D3D5E7C81C65F4C3332C5085C3D170AA39B98FE4FB8072FF2D6F0981EC594C2ABEC25229015EA58AC3DBE7EC77EB528262E89147CD5A270E90E8ED45F4C4E6D14B430888D9CDE7448887E6CBDAC83DCF8341846382DBF976ACF3DE02E285685F0C6D38767524AD1075D830AA573D8F6DE95C504D5313FEF173DEF32A3430AA709FF78401F3C8AF8003337CE3AC2DF7F255D816110F76BBF61C0A7D48C8F5304031EDCD414B8A54187C2AE8F3180947F6005C12CA37B82BDF96240AC4CFEA9B07CFED696F8EC09A90784A55D02982313F507494628F93D6F6C8D011EF9207EF88041E79CE1A29499B90E5992B2CCE58424DE32496E838209AFF5D27EF227777C5D298852988081AC304E36B2C9A793F7CEF8C3747A75043C270C2B5C2013185A1E0EB6A0CBAB7E216A3F070C80B0667135D51514BF4F424535F7",
    @Test
    fun testReadPublicKeyFile() {
        val filename = "$inputDir/publicKey.bt"
        println("readPublicKeyFile filename = ${filename}")
        val pk = readPublicKeyFromByteFile(filename, group)
        println( "MixnetPublicKey = \n${pk}")
    }

    @Test
    fun testReadZkpProofs() {
        readByteTreeFromFile("$nizkpDir/PermutationCommitment01.bt", 1)
        readByteTreeFromFile("$nizkpDir/PoSCommitment01.bt", 1)
        readByteTreeFromFile("$nizkpDir/PoSReply01.bt", 1)
    }

    @Test
    fun testReadPermutationCommitment1() {
        readByteTreeFromFile("$nizkpDir/PermutationCommitment01.bt", 10)
    }

    @Test
    fun testReadPosCommitment2() {
        readByteTreeFromFile("$nizkpDir/PoSCommitment01.bt", 10)
    }

    @Test
    fun testReadPoSReply() {
        readByteTreeFromFile("$nizkpDir/PoSReply01.bt", 10)
    }

    @Test
    fun testReadZkpProofCiphertexts() {
        readByteTreeFromFile("$nizkpDir/Ciphertexts01.bt", 2)
    }

    /////////////////////////////////////////////


    @Test
    fun testReadPermutationCommitment3() {
        readByteTreeFromFile(demoDir + "Party01/export/default/proofs/PermutationCommitment01.bt", 1)
    }

    @Test
    fun testReadDemoProofs() {
        readByteTreeFromFile(
            demoDir + "Party01/export/default/proofs/Ciphertexts01.bt",
            1
        )
        readByteTreeFromFile(
            demoDir + "Party01/export/default/proofs/PermutationCommitment01.bt",
            1
        )
        readByteTreeFromFile(
            demoDir + "Party01/export/default/proofs/PoSCommitment01.bt",
            1
        )
    }

    @Test
    fun testReadPosCommitment3() {
        readByteTreeFromFile(
            demoDir + "Party01/export/default/proofs/PoSCommitment01.bt",
            10
        )
    }

    @Test
    fun testReadDemoPublicKeyFile() {
        val filename = demoDir + "Party01/publicKey"
        println("readPublicKeyFile filename = ${filename}")
        val root = readByteTreeFromFile(filename)
        println(root.show(10))
    }
}

private fun readByteTreeFromFile(filename : String, maxDepth: Int) : ByteTree {
    println("readByteTreeFromFile = ${filename}")
    val tree = readByteTreeFromFile(filename)
    return tree
}
