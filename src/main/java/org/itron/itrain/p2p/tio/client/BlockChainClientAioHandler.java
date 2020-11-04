package org.itron.itrain.p2p.tio.client;

import lombok.extern.slf4j.Slf4j;
import org.itron.itrain.p2p.tio.common.BlockPacket;
import org.tio.client.intf.ClientAioHandler;
import org.tio.core.ChannelContext;
import org.tio.core.GroupContext;
import org.tio.core.exception.AioDecodeException;
import org.tio.core.intf.Packet;

import java.nio.ByteBuffer;

/**
 * 基于 t-io 的区块链底层 P2P 网络平台的客户端 Handler
 *
 * @author Shadowalker
 */
@Slf4j
public class BlockChainClientAioHandler implements ClientAioHandler {

    private static BlockPacket heartbeatPacket = new BlockPacket();

    /**
     * 此方法如果返回 null，则框架层面不会发心跳
     * 如果返回非 null，则框架层面会定时发本方法返回的消息包
     *
     * @return
     */
    @Override
    public Packet heartbeatPacket() {
        return heartbeatPacket;
    }

    /**
     * 解码：把接收到的 ByteBuffer 解码成应用可以识别的业务消息包。
     * 总的消息结构：消息头 + 消息体
     * 消息头结构：4 个字节，存储消息体的长度
     * 消息体结构：对象的 JSON 串的 byte[]
     *
     * @param byteBuffer
     * @param i
     * @param i1
     * @param i2
     * @param channelContext
     * @return
     * @throws AioDecodeException
     */
    @Override
    public Packet decode(ByteBuffer buffer, int limit, int position, int readableLength, ChannelContext channelContext) throws AioDecodeException {
        // 若收到的数据若无组成业务包，则返回 null，以表示数据长度不够
        if (readableLength < BlockPacket.HEADER_LENGTH) {
            return null;
        }
        // 读取消息体的长度
        int bodyLength = buffer.getInt();
        // 数据不正确，则抛出 AioDecodeException 异常
        if (bodyLength < 0) {
            throw new AioDecodeException("bodyLength [ " + bodyLength + "  ] is not right, remote: " + channelContext.getClientNode());
        }
        // 计算本次需要的数据长度
        int neededLength = BlockPacket.HEADER_LENGTH + bodyLength;
        // 收到的数据是否足够组包
        int isDataEnough = readableLength - neededLength;
        // 不够消息体长度（剩下的 buffer 组不了消息体）
        if (isDataEnough < 0) {
            return null;
        } else { // 组包成功
            BlockPacket imPacket = new BlockPacket();
            if (bodyLength > 0) {
                byte[] dst = new byte[bodyLength];
                buffer.get(dst);
                imPacket.setBody(dst);
            }
            return imPacket;
        }
    }

    /**
     * 编码：把业务消息包编码为可以发送的 ByteBuffer。
     * 总的消息结构：消息头 + 消息体
     * 消息头结构：4 个字节，存储消息体的长度
     * 消息体结构：对象的 JSON 串的 byte[]
     *
     * @param packet
     * @param groupContext
     * @param channelContext
     * @return
     */
    @Override
    public ByteBuffer encode(Packet packet, GroupContext groupContext, ChannelContext channelContext) {
        BlockPacket blockPacket = (BlockPacket) packet;
        byte[] body = blockPacket.getBody();
        int bodyLen = 0;
        if (body != null) {
            bodyLen = body.length;
        }
        // ByteBuffer 的总长度 = 消息头的长度 + 消息体的长度
        int allLen = BlockPacket.HEADER_LENGTH + bodyLen;
        // 创建一个新的 ByteBuffer
        ByteBuffer buffer = ByteBuffer.allocate(allLen);
        // 设置字节序
        buffer.order(groupContext.getByteOrder());

        // 写入消息头，消息头的内容就是消息体的长度
        buffer.putInt(bodyLen);

        // 写入消息体
        if (body != null) {
            buffer.put(body);
        }
        return buffer;
    }

    /**
     * 处理消息
     *
     * @param packet
     * @param channelContext
     * @throws Exception
     */
    @Override
    public void handler(Packet packet, ChannelContext channelContext) throws Exception {
        BlockPacket blockPacket = (BlockPacket) packet;
        byte[] body = blockPacket.getBody();
        if (body != null) {
            String str = new String(body, BlockPacket.CHARSET);
            log.info("Shadow.Net 客户端收到消息：{}", str);
        }
    }

}
