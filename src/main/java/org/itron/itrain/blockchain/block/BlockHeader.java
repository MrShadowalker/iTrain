package org.itron.itrain.blockchain.block;

import lombok.Data;

import java.util.List;

/**
 * 区块头
 * 包含当前区块运行的版本、上一区块的哈希值、Merkle 树根节点哈希值、区块的序号、时间戳、32 位随机数 Nonce、交系信息的哈希集合等。
 *
 * @author Shadowalker
 */
@Data
public class BlockHeader {

    // 版本号
    private int version;

    // 上一区块的哈希值
    private String hashPreviousBlock;

    // Merkle 树根节点哈希值
    private String hashMerkleRoot;

    // 生成该区块的公钥
    private String publicKeyey;

    // 区块的序号
    private int number;

    // 时间戳
    private long timeStamp;

    // 32 位随机数
    private long nonce;

    // 该区块里每条交易信息的哈希集合，按顺序来的，通过该哈希集合能算出根节点哈希值
    private List<String> hashList;
}
