package org.fdroid.index.v1

import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

private const val TEST_FILE_PATH = "src/commonTest/resources/verification/"
private const val validCertificate =
    "308202cf308201b7a0030201020204410b599a300d06092a864886f70d01010b" +
        "05003018311630140603550403130d546f727374656e2047726f7465301e170d" +
        "3134303631363139303332305a170d3431313130313139303332305a30183116" +
        "30140603550403130d546f727374656e2047726f746530820122300d06092a86" +
        "4886f70d01010105000382010f003082010a02820101009fee536211eb53d0b0" +
        "54b0b2cf72fe4ba66f341b5b93730f8fe4a4a68b105a35a3a5daf5b54443d744" +
        "bb19eb954456e6fb1f1fcfe9023684cddb0643be2d70a1a7a37e75badad62ba6" +
        "07e238a8d88fb1601d46030824ef5e719b65f855801ee323ac68f8da7afea30d" +
        "9366c1a132e1cab21dcf218d163a5aa8dcc5b31d876085414fcf0eed74bc5a02" +
        "c7d297beeaa756843a0acaf31eec9969322c8695ee9f2be84e58347b47dc81e4" +
        "29a6f11e5cb1415aea54b88a1911a7fc62fbd53ea7a72b1e26e7da8111510dc9" +
        "8631e939760095441ca2d0a6b316527dbe146245cf279607f3c9ff7006a1adf3" +
        "67b8fe55a7c3a9bdb66aebbe9b71711981e0b342dca8730203010001a321301f" +
        "301d0603551d0e04160414649492d14e97d5937667ee2e555926899f9a261030" +
        "0d06092a864886f70d01010b050003820101002bb228f5b31e68a9175f2a6cbb" +
        "0d727991fea7b71fbb295aaa28963963b5c697d20929b57e299c9607d20ac332" +
        "d86544300de7d1cf4602162d9929fbb7465be279a44a31cb06f778d66625077d" +
        "615affc751a300843bad116fcee9c958b88aef0f25988dc63d7f8853517d738e" +
        "fd9888e61f395597090ae7b41a5983e8d2b4bd74ee98c9a3dab91114f43b7336" +
        "cc00889385567e0f717aa76526dbdae2fa34e007375b2db3d34c423b77b37774" +
        "b93eff762c4b3b4fb05f8b26256570607a1400cddad2ebd4762bcf4efe703248" +
        "fa5b9ab455e3a5c98cb46f10adb6979aed8f96a688fd1d2a3beab380308e2ebe" +
        "0a4a880615567aafc0bfe344c5d7ef677e060f"
private const val validFingerprint =
    "28e14fb3b280bce8ff1e0f8e82726ff46923662cecff2a0689108ce19e8b347c"

internal class IndexV1VerifierTest {

    @Test
    fun testNoCertWithFingerprint() {
        val file = File("$TEST_FILE_PATH/valid-v1.jar")
        assertFailsWith<IllegalArgumentException> {
            IndexV1Verifier(file, validCertificate, validFingerprint)
        }
    }

    @Test
    fun testValid() {
        val file = File("$TEST_FILE_PATH/valid-v1.jar")

        val verifier = IndexV1Verifier(file, null, null)
        val certificate = verifier.getStreamAndVerify { inputStream ->
            assertEquals("foo\n", inputStream.readBytes().decodeToString())
        }
        assertEquals(validCertificate, certificate)
    }

    @Test
    fun testValidMatchesFingerprint() {
        val file = File("$TEST_FILE_PATH/valid-v1.jar")

        val verifier = IndexV1Verifier(file, null, validFingerprint)
        val certificate = verifier.getStreamAndVerify { inputStream ->
            assertEquals("foo\n", inputStream.readBytes().decodeToString())
        }
        assertEquals(validCertificate, certificate)
    }

    @Test
    fun testValidWrongFingerprint() {
        val file = File("$TEST_FILE_PATH/valid-v1.jar")

        val verifier = IndexV1Verifier(file, null, "foo bar")
        val e = assertFailsWith<SigningException> {
            verifier.getStreamAndVerify { inputStream ->
                assertEquals("foo\n", inputStream.readBytes().decodeToString())
            }
        }
        assertTrue(e.message!!.contains("fingerprint"))
    }

    @Test
    fun testValidWithExpectedCertificate() {
        val file = File("$TEST_FILE_PATH/valid-v1.jar")

        val verifier = IndexV1Verifier(file, validCertificate, null)
        val certificate = verifier.getStreamAndVerify { inputStream ->
            assertEquals("foo\n", inputStream.readBytes().decodeToString())
        }
        assertEquals(validCertificate, certificate)
    }

    @Test
    fun testValidWithWrongCertificate() {
        val file = File("$TEST_FILE_PATH/valid-v1.jar")

        val verifier = IndexV1Verifier(file, validFingerprint, null)
        val e = assertFailsWith<SigningException> {
            verifier.getStreamAndVerify { inputStream ->
                assertEquals("foo\n", inputStream.readBytes().decodeToString())
            }
        }
        assertTrue(e.message!!.contains("certificate"))
    }

    @Test
    fun testUnsigned() {
        val file = File("$TEST_FILE_PATH/unsigned.jar")

        val verifier = IndexV1Verifier(file, null, null)
        assertFailsWith<SigningException> {
            verifier.getStreamAndVerify { inputStream ->
                assertEquals("foo\n", inputStream.readBytes().decodeToString())
            }
        }
    }

    @Test
    fun testInvalid() {
        val file = File("$TEST_FILE_PATH/invalid-v1.jar")

        val verifier = IndexV1Verifier(file, null, null)
        assertFailsWith<SigningException> {
            verifier.getStreamAndVerify { inputStream ->
                assertEquals("foo\n", inputStream.readBytes().decodeToString())
            }
        }
    }

    @Test
    fun testWrongEntry() {
        val file = File("$TEST_FILE_PATH/invalid-wrong-entry-v1.jar")

        val verifier = IndexV1Verifier(file, null, null)
        val e = assertFailsWith<SigningException> {
            verifier.getStreamAndVerify { inputStream ->
                assertEquals("foo\n", inputStream.readBytes().decodeToString())
            }
        }
        assertTrue(e.message!!.contains("index-v1.json"))
    }

    @Test
    fun testMD5Digest() {
        val file = File("$TEST_FILE_PATH/invalid-MD5-SHA1withRSA-v1.jar")

        val verifier = IndexV1Verifier(file, null, null)
        val e = assertFailsWith<SigningException> {
            verifier.getStreamAndVerify { inputStream ->
                assertEquals("foo\n", inputStream.readBytes().decodeToString())
            }
        }
        assertTrue(e.message!!.contains("Unsupported digest"))
    }

    @Test
    fun testMD5SignatureAlgo() {
        val file = File("$TEST_FILE_PATH/invalid-MD5-MD5withRSA-v1.jar")

        val verifier = IndexV1Verifier(file, null, null)
        val e = assertFailsWith<SigningException> {
            verifier.getStreamAndVerify { inputStream ->
                assertEquals("foo\n", inputStream.readBytes().decodeToString())
            }
        }
        assertTrue(e.message!!.contains("Unsupported digest"))
    }

}
