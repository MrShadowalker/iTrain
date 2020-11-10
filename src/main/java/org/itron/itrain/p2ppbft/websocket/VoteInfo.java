package org.itron.itrain.p2ppbft.websocket;

import lombok.Data;

import java.util.List;

/**
 * PBFT 在投票过程中的投票实体需要包含 VoteEnum 中的投票状态码，还可以包含待写入区块的内容及对应的 Merkle 树根节点的 Hash 值。
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
    // 待写入区块的内容的 Merkle 树根节点 Hash 值
    private String hash;
}
