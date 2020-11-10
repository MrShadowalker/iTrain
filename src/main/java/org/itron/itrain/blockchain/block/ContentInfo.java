package org.itron.itrain.blockchain.block;

import lombok.Data;

/**
 * 区块 body 内的一条内容实体类
 *
 * @author Shadowalker
 */
@Data
public class ContentInfo {
    // 新的 JSON 内容
    private String jsonContent;

    // 时间戳
    private Long timeStamp;

    // 公钥
    private String publicKey;

    // 签名
    private String sign;

    // 该操作的哈希
    private String hash;
}
