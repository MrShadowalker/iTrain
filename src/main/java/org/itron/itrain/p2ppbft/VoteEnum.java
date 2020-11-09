package org.itron.itrain.p2ppbft;

/**
 * PBFT 基于 WebSocket 的实现
 * 在 PBFT 中，节点是有状态的，如 Pre-prepare 状态、Prepare 状态、Commit 状态等。对应到 Java 代码中，我们设定投票节点的 3 个状态 Enum。
 *
 * @author Shadowalker
 */
public enum VoteEnum {

    PREPREPARE("节点将自己生成 Block", 100),
    PREPARE("节点收到请求生成 Block 的消息，进入准备状态，并对外广播该状态", 200),
    COMMIT("每个节点收到超过 2f+1 个不同节点的 Commit 消息后，则认为该区块已经达成一致，即进入 Commit 状态，并将其持久化到区块链数据库中", 400);

    // 投票情况描述
    private String msg;
    // 投票情况状态码
    private int code;

    VoteEnum(String msg, int code) {
        this.msg = msg;
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    void setCode(int code) {
        this.code = code;
    }

    // 根据状态码返回对应的 Enum
    public static VoteEnum find(int code) {
        for (VoteEnum ve : VoteEnum.values()) {
            if (ve.code == code) {
                return ve;
            }
        }
        return null;
    }
}
