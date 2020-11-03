package org.itron.itrain.utils.DeEnCoder;

import com.google.crypto.tink.*;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.AeadFactory;
import com.google.crypto.tink.aead.AeadKeyTemplates;
import com.google.crypto.tink.config.TinkConfig;
import com.google.crypto.tink.integration.awskms.AwsKmsClient;
import com.google.crypto.tink.integration.gcpkms.GcpKmsClient;
import com.google.crypto.tink.proto.KeyTemplate;
import com.google.crypto.tink.proto.Keyset;
import com.google.crypto.tink.signature.PublicKeySignFactory;
import com.google.crypto.tink.signature.PublicKeyVerifyFactory;
import com.google.crypto.tink.signature.SignatureKeyTemplates;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;


/**
 * 基于 Tink 的加密解密工具类
 * Tink 是由 Google 的一群密码学家和安全工程师编写的密码库。
 * Tink 通过原语执行加密任务，每个原语都是通过指定原语功能的相应接口定义的。
 * @author Shadowalker
 */
public class DeEnCoderTinkUtil {


    /********** Tink 的初始化 **********/

    /**
     * 使用 Tink 中所有原语来初始化
     */
    public void registerByTink() {
        try {
            TinkConfig.register();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 仅使用 AEAD 原语实现
     */
    public void registerByAead() {
        try {
            AeadConfig.register();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Tink 也支持用户自定义初始化，即直接通过注册表类进行注册
     */
    public void registerByCustomize() throws GeneralSecurityException {
        // TODO Register a custom implementation of AEAD.
        // 其中 MyAeadKeyManager 为自定义的 KeyManager。
        Registry.registerKeyManager(new MyAeadKeyManager());
    }

    // 注册原语实现后，Tink 的基本使用分三步进行：
    // 1. 加载或生成加密密钥（Tink 术语中的密钥集）
    // 2. 使用 key 获取所选原语的实例
    // 3. 使用原语完成加密任务

    /**
     * 使用 Java 中的 AEAD 原语加密解密
     */
    public void aead(byte[] plaintext, byte[] aad) {
        try {
            // 1. 配置生成密钥集
            KeysetHandle keysetHandle = KeysetHandle.generateNew(AeadKeyTemplates.AES128_GCM);
            // 2. 使用 key 获取所选原语的实例
            Aead aead = AeadFactory.getPrimitive(keysetHandle);
            // 3. 使用原语完成加密任务
            byte[] ciphertext = aead.encrypt(plaintext, aad);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    /********** 生成新密钥（组） **********/

    // 每个密钥管理器 KeyManager 的实现都提供了新密钥的生成接口 newKey()，
    // 该接口根据用户设置的密钥类型生成新密钥。
    // 然而，为避免敏感密钥信息的意外泄露，开发者在代码中应小心将密钥（集合）生成与密钥（集合）使用混合。
    // 为了支持这些工作之间的分离，Tink 包提供了一个名为 Tinkey 的命令行工具，可用于公共密钥的管理。

    /**
     * 如果用户需要在 Java 代码中直接用新的密钥生成 KeysetHandle，则用户可以使用 keysteadle 工具类。
     * 例如，用户可以生成包含随机生成的 AES 128-GCM 密钥的密钥集，如下
     */
    public void createAes128GcmKeySet() {
        try {
            KeyTemplate keyTemplate = AeadKeyTemplates.AES128_GCM;
            KeysetHandle keysetHandle = KeysetHandle.generateNew(keyTemplate);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    /********** 存储密钥 **********/

    /**
     * 生成密钥后，用户可以直接将其保存到存储系统中，写入文件
     */
    public void save2File() {
        try {
            // 创建 AES 对应的 keysetHandle
            KeysetHandle keysetHandle = KeysetHandle.generateNew(AeadKeyTemplates.AES128_GCM);
            // 写入 json 文件
            String keysetFilename = "my_keyset.json";
            CleartextKeysetHandle.write(keysetHandle, JsonKeysetWriter.withFile(new File(keysetFilename)));
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 用户还可以使用 Google Cloud KMS key 来对 key 进行加密
     * 其中 Google Cloud KMS key 位于 gcp-kms:/projects/tink-examples/locations/global/keyRings/foo/cryptoKeys/bar as follows
     * 保存在文件中的代码示例如下：
     */
    public void save2FileBaseKMS() {
        try {
            // 创建 AES 对应的 keysetHandle
            KeysetHandle keysetHandle = KeysetHandle.generateNew(AeadKeyTemplates.AES128_GCM);
            // 写入 json 文件
            String keysetFilename = "my_keyset.json";
            // 使用 gcp-kms 方式对密钥加密
            String masterKeyUri = "gcp-kms://projects/tink-examples/locations/global/keyRings/foo/cryptoKeys/bar";
            keysetHandle.write(JsonKeysetWriter.withFile(new File(keysetFilename)),
                    new GcpKmsClient().getAead(masterKeyUri));
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    /********** 加载密钥 **********/

    /**
     * 使用 KeysetHandler 加载加密的密钥集
     */
    public void loadKeySet() {
        try {
            String keysetFilename = "my_keyset.json";
            // 使用 aws-kms 方式对密钥加密
            String masterKeyUri = "aws-kms:arn:aws:kms:us-east-1:007084425826:key/84a65985-f868-4bfc-83c2-366618acf147";
            KeysetHandle keysetHandle = KeysetHandle.read(JsonKeysetReader.withFile(new File(keysetFilename)),
                    new AwsKmsClient().getAead(masterKeyUri));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 若加载明文的密钥集，则需要使用 CleartextKeysetHandle 类
     */
    public void loadCleartextKeySet() {
        try {
            String keysetFilename = "my_keyset.json";
            KeysetHandle keysetHandle = CleartextKeysetHandle.read(JsonKeysetReader.withFile(new File(keysetFilename)));
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    /********** 原语的使用和获取 **********/

    // 原语在 Tink 中指的是加密操作，因此它们构成了 Tink API 的核心。
    // 原语是一个接口，它指定了原语能提供的基本操作。一个原语可以有多个实现，用户可以通过使用某种类型的键来设定想要的实现。
    // 以下整理当前可用或计划中的原语的 Java 实现：
    // 原语 | Java 实现
    // AEAD | AES-EAX, AES-GCM, AES-CTR-HMAC, KMS Envelope
    // Streaming AEAD | AES-GCM-HKDF-STREAMING, AES-CTR-HMAC-STREAMING
    // Deterministic AEAD |  AES=SIV
    // Mac | HMAC-SHA2
    // Digital Signatures | ECDSA over NIST curves, ED25519
    // Hybrid Encryption | ECIES with AEAD and HKDF

    /********** 对称密钥加密 **********/

    /**
     * 获得和使用 AEAD（通过认证的加密，以及加密或解散数据）
     */
    public void aeadAES(byte[] plaintext, byte[] aad) {
        try {
            // 创建 AES 对应的 keysetHandler
            KeysetHandle keysetHandle = KeysetHandle.generateNew(AeadKeyTemplates.AES128_GCM);
            // 获取私钥
            Aead aead = AeadFactory.getPrimitive(keysetHandle);
            // 用私钥加密明文
            byte[] ciphertext = aead.encrypt(plaintext, aad);
            // 解密密文
            byte[] decrypted = aead.decrypt(ciphertext, aad);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    /********** 数字签名 **********/

    /**
     * 数字签名的签名或验证
     */
    public void signatures(byte[] data) {
        try {
            /***** 签名 *****/
            // 创建 ESCSA 对应的 KeysetHandle 对象
            KeysetHandle privateKeysetHandler = KeysetHandle.generateNew(SignatureKeyTemplates.ECDSA_P256);
            // 获取私钥
            PublicKeySign signer = PublicKeySignFactory.getPrimitive(privateKeysetHandler);
            // 用私钥签名
            byte[] signature = signer.sign(data);

            /***** 签名验证 *****/
            // 获取公钥对应的 KeysetHandle 对象
            KeysetHandle publicKeysetHandler = privateKeysetHandler.getPublicKeysetHandle();
            // 获取私钥
            PublicKeyVerify verifier = PublicKeyVerifyFactory.getPrimitive(publicKeysetHandler);
            // 使用私钥校验签名
            verifier.verify(signature, data);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    // 使用 Tink 的注意事项如下
    // 不要使用标有 @Alpha 注释的字段和方法的 API。这些 API 可以以任意方式修改，甚至可以随时删除。它们仅用于测试，不是官方生产发布的。
    // 不要在 com.google.crypto.tink.subtle 上使用 API。虽然这些 API 通常使用起来是安全的，但并不适合公众消费，因为可以随时以任何方式修改甚至删除他们。
}
