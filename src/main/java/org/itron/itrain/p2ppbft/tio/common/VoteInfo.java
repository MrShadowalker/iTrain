package org.itron.itrain.p2ppbft.tio.common;

import lombok.Data;

import java.util.List;

/**
 * 投票信息类
 *
 * @author Shadowalker
 */
@Data
public class VoteInfo {
    // 投票状态码
    private int code;
    // 待写入区块的内容
    private List<String> contents;
    // 待写入区块的内容的 Merkle 树根节点的 Hash 值
    private String hash;
}
