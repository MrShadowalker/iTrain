package org.itron.itrain.utils.DeEnCoder;

import com.xiaoleilu.hutool.crypto.SecureUtil;
import com.xiaoleilu.hutool.crypto.asymmetric.KeyType;
import com.xiaoleilu.hutool.crypto.asymmetric.RSA;
import com.xiaoleilu.hutool.crypto.symmetric.DES;
import lombok.extern.slf4j.Slf4j;
import org.testng.util.Strings;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * 基于 Hutool 工具类的加解密方法类
 *
 * @author Shadowalker
 */
@Slf4j
public class DeEnCoderHutoolUtil {
    // 构建 RSA 对象
    private static RSA rsa = new RSA();

    // 获得私钥
    private static PrivateKey privateKey = rsa.getPrivateKey();

    // 获得公钥
    private static PublicKey publicKey = rsa.getPublicKey();

    /**
     * RSA 加密通用方法：对称加密解密
     *
     * @param originalContent 明文
     * @return 密文
     */
    public static String rsaEncrypt(String originalContent) {
        // 明文或加密密钥为空时
        if (Strings.isNullOrEmpty(originalContent)) {
            return null;
        }
        // 公钥加密，之后私钥解密
        // TODO rsa.encryptBase64()??
        String encryptContent = rsa.encryptStr(originalContent, KeyType.PublicKey);
        log.info("密文为: {}", encryptContent);
        return encryptContent;
    }

    /**
     * RSA 解密通用方法：对称加密解密
     *
     * @param ciphertext 密文
     * @return 明文
     */
    public static String rsaDecrypt(String ciphertext) {
        // 密文或加密密钥为空时
        if (Strings.isNullOrEmpty(ciphertext)) {
            return null;
        }
        String originalContent = rsa.decryptStr(ciphertext, KeyType.PrivateKey);
        log.info("明文为: {}", originalContent);
        return originalContent;
    }

    /**
     * DES 加密通用方法：对称加密解密
     *
     * @param originalContent 明文
     * @param key             加密密钥
     * @return 密文
     */
    public static String desEncrypt(String originalContent, String key) {
        // 明文或加密密钥为空时
        if (Strings.isNullOrEmpty(originalContent) || Strings.isNullOrEmpty(key)) {
            return null;
        }
        // TODO 也可以随机生成密钥
        // byte[] key = SecureUtil.generateKey(SymmetricAlgorithm.DES.getValue()).getEncoded();

        // 构建
        DES des = SecureUtil.des(key.getBytes());

        // 加密
        return des.encryptHex(originalContent);
    }

    public static String desDecrypt(String ciphertext, String key) {
        // 密文或加密密钥为空时
        if (Strings.isNullOrEmpty(ciphertext) || Strings.isNullOrEmpty(key)) {
            return null;
        }
        // TODO 也可以随机生成密钥
        // byte[] key = SecureUtil.generateKey(SymmetricAlgorithm.DES.getValue()).getEncoded();

        // 构建
        DES des = SecureUtil.des(key.getBytes());

        // 解密
        return des.decryptStr(ciphertext);
    }

}
