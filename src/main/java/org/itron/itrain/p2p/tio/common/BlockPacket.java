package org.itron.itrain.p2p.tio.common;

import lombok.Data;
import org.tio.core.intf.Packet;

import java.nio.charset.StandardCharsets;

/**
 * 区块链底层定制的 Packet
 *
 * @author Shadowalker
 */
@Data
public class BlockPacket extends Packet {

    // 网络传输需要序列化，这里采用 Java 自带序列化方式
    private static final long serialVersionUID = 5626585902612872939L;
    // 消息头的长度
    public static final int HEADER_LENGTH = 4;
    // 字符编码类型
    public static final String CHARSET = StandardCharsets.UTF_8.name();
    // 传输内容的字节
    private byte[] body;
}
