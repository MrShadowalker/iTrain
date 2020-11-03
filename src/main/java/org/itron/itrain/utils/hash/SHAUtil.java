package org.itron.itrain.utils.hash;

import com.xiaoleilu.hutool.crypto.digest.DigestUtil;
import org.apache.commons.codec.binary.Hex;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 实现 SHA-256 加密
 * @author Shadowalker
 */
public class SHAUtil {

    /**
     * 利用 Apache commons-codec 工具类实现 SHA-256 加密
     * @param originalStr 加密前的报文
     * @return 加密后的报文
     */
    public static String getSHA256BasedMD(String originalStr) {
        MessageDigest messageDigest;
        String encodeStr = "";
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hash = messageDigest.digest(originalStr.getBytes(StandardCharsets.UTF_8));
            encodeStr = Hex.encodeHexString(hash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return encodeStr;
    }

    /**
     * 利用 Hutool 工具类实现 SHA-256 加密
     * @param originalStr
     * @return
     */
    public static String sha256BaseHutool(String originalStr) {
        return DigestUtil.sha256Hex(originalStr);
    }
}
