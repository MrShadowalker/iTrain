package org.itron.itrain.p2ppbft.tio.common;

/**
 * PBFT 投票 Enum 类
 *
 * @author Shadowalker
 */
public enum VoteEnum {
    PREPREPARE("节点将自己生成 Block", 100),
    PREPARE("节点收到请求生成 Block 的消息，进入准备状态，并对外广播该状态", 200),
    COMMIT("每个节点收到超过 2f + 1 个不同节点的 commit 消息后，则认为该区块已经达成一致，即进入 Commit 状态，并将其持久化到区块链数据库中", 400);

    // 投票情况描述
    private String message;
    // 投票情况状态码
    private int code;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    VoteEnum(String message, int code) {
        this.message = message;
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
