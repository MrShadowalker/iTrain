package org.itron.itrain.blockchain.block;

import com.xiaoleilu.hutool.crypto.digest.DigestUtil;
import lombok.Data;

/**
 * 区块
 * 如果对 HTTP 比较清楚，就会发现区块的结构设计和 HTTP Request/HTTP Response 结构设计类似
 * 如 HTTP Request 也是由三部分组成的，分别是 Request Line、Request Header 和 Request Body；
 * HTTP Response 也是由三部分组成的，分别是 Response Line、Response Header 和 Response Body。
 * <p>
 * Java 版区块 Block 由 BlockHeader、BlockBody 和 blockHash 组成。
 * blockHash 由区块所有内容，即 BlockHeader 和 BlockBody 根据 SHA256 计算得到。
 *
 * @author Shadowalker
 */
@Data
public class Block {

    // 区块头
    private BlockHeader blockHeader;

    // 区块 body
    private BlockBody blockBody;

    // 哈希
    private String blockHash;

    // 根据该区块所有属性计算 SHA256
    private String getBlockHash() {
        return DigestUtil.sha256Hex(blockHeader.toString() + blockBody.toString());
    }
}
