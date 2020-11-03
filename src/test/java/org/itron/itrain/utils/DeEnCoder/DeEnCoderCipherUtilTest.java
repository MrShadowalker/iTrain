package org.itron.itrain.utils.DeEnCoder;

import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * DeEnCoderCipherUtil 的单元测试类
 *
 * @author Shadowalker
 */
@Slf4j
public class DeEnCoderCipherUtilTest {
    private static String ciphertextGlobal;

    @Test
    public void testEncrypt() {
        // case1: originalContent = null; key = null;
        String originalContent = null;
        String key = null;
        Assert.assertEquals(DeEnCoderCipherUtil.encrypt(originalContent, key), null);

        // case2: originalContent != null; key = null;
        originalContent = "Shadowalker";
        Assert.assertEquals(DeEnCoderCipherUtil.encrypt(originalContent, key), null);

        // case3: originalContent = null; key != null;
        originalContent = null;
        key = "Shadowalker";
        Assert.assertEquals(DeEnCoderCipherUtil.encrypt(originalContent, key), null);

        // case4: originalContent != null; key != null;
        originalContent = "Shadowalker";
        key = "Shadowalker";
        String encodeString = DeEnCoderCipherUtil.encrypt(originalContent, key);
        log.info("encodeString : {}", encodeString);
        Assert.assertEquals(encodeString, "5BlES3XGCWx1a3BH1fpomQ==");

    }
}
