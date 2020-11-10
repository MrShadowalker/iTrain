package org.itron.itrain.p2ppbft.tio.server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.itron.itrain.p2ppbft.tio.common.BlockPacket;
import org.itron.itrain.p2ppbft.tio.common.VoteEnum;
import org.itron.itrain.p2ppbft.tio.common.VoteInfo;
import org.itron.itrain.utils.merkle.SimpleMerkleTree;
import org.tio.core.ChannelContext;
import org.tio.core.GroupContext;
import org.tio.core.Tio;
import org.tio.core.exception.AioDecodeException;
import org.tio.core.intf.Packet;
import org.tio.server.intf.ServerAioHandler;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于 t-io 的区块链底层 P2P 网络平台的服务端 Handler
 *
 * @author Shadowalker
 */
@Slf4j
public class BlockChainPbftServerAioHandler implements ServerAioHandler {
    /**
     * 解码：把接收到的 ByteBuffer 解码成应用可以识别的业务消息包
     * 总的消息结构：消息头 + 消息体
     * 消息头结构：4 个字节，存储消息体的长度
     * 消息体结构：对象的 JSON 串的byte[]
     *
     * @param buffer
     * @param limit
     * @param position
     * @param readableLength
     * @param channelContext
     * @return
     * @throws AioDecodeException
     */
    @Override
    public Packet decode(ByteBuffer buffer, int limit, int position, int readableLength, ChannelContext channelContext) throws AioDecodeException {
        // 提醒：buffer 的开始位置并不一定是 0， 应用需要从 buffer。position() 开始读
        // 取数据
        // 若收到的数据无法组成业务包，则返回 null，告诉框架数据不够
        if (readableLength < BlockPacket.HEADER_LENGTH) {
            return null;
        }

        // 读取消息体的长度
        int bodyLength = buffer.getInt();

        // 数据不正确，则抛出 AioDecodeException 异常
        if (bodyLength < 0) {
            throw new AioDecodeException("bodyLength [" + bodyLength + "] is not right, remote:" + channelContext.getClientNode());
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
     * 编码：把业务消息包编码为可以发送的 ByteBuffer
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
        BlockPacket helloPacket = (BlockPacket) packet;
        byte[] body = helloPacket.getBody();
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
     * <p>
     * 1. 当服务端收到来自客户端的非 JSON 化数据时，则说明当前不是 PBFT 阶段，这里仅将收到的消息回传给客户端。
     * 2. 当服务端收到的是 JSON 化数据时，则说明目前是 PBFT 阶段。服务端先校验消息的有效性，校验通过后再根据消息的状态码进行相应的 PBFT 逻辑处理。
     * 2.1 若服务端收到的是 Pre-prepare 消息，则将消息的状态置为 Prepare，并将消息发送给客户端。
     * 2.2 若服务端收到的是 Commit 消息，则检验节点确认个数是否有效，若有效则说明 PBFT 一致性达成，返回客户端“服务端开始区块入库”的消息。
     *
     * @param packet
     * @param channelContext
     * @throws Exception
     */
    @Override
    public void handler(Packet packet, ChannelContext channelContext) throws Exception {
        BlockPacket helloPacket = (BlockPacket) packet;
        byte[] body = helloPacket.getBody();
        if (body != null) {
            String str = new String(body, BlockPacket.CHARSET);
            log.info("服务端收到消息：" + str);

            // 如果收到的不是 JSON 化数据，则说明不是 PBFT 阶段
            if (!str.startsWith("{")) {
                BlockPacket respacket = new BlockPacket();
                respacket.setBody(("服务端收到了客户端的消息，客户端的消息是：" + str).getBytes(BlockPacket.CHARSET));
                Tio.send(channelContext, respacket);
            }

            // 如果收到的是 JSON 数据，则说明是 PBFT 阶段
            // 如果是 JSON 化数据，则进入到了 PBFT 投票阶段
            JSONObject json = JSON.parseObject(str);
            if (!json.containsKey("code")) {
                log.info("服务端收到 JSON 化数据。");
            }
            int code = json.getIntValue("code");
            if (code == VoteEnum.PREPREPARE.getCode()) {
                // 校验 Hash
                VoteInfo voteInfo = JSON.parseObject(str, VoteInfo.class);
                if (!voteInfo.getHash().equals(SimpleMerkleTree.getTreeNodeHash(voteInfo.getContents()))) {
                    log.info("服务端收到客户端错误的 JSON 化数据！");
                    return;
                }
                // 校验成功，发送下一个状态的数据
                VoteInfo vi = createVoteInfo(VoteEnum.PREPARE);
                BlockPacket respacket = new BlockPacket();
                respacket.setBody(JSON.toJSONString(vi).getBytes(BlockPacket.CHARSET));
                Tio.send(channelContext, respacket);
                log.info("服务端发送到客户端 PBFT 消息：" + JSON.toJSONString(vi));
                return;
            }

            if (code == VoteEnum.COMMIT.getCode()) {
                // 校验 Hash
                VoteInfo voteInfo = JSON.parseObject(str, VoteInfo.class);
                if (!voteInfo.getHash().equals(SimpleMerkleTree.getTreeNodeHash(voteInfo.getContents()))) {
                    log.info("服务端收到客户端错误的 JSON 化数据！");
                    return;
                }
                // 校验成功，检查节点确认个数是否有效
                if (getConnectedNodeCount() >= getLeastNodeCount()) {
                    BlockPacket respacket = new BlockPacket();
                    respacket.setBody("服务端开始区块入库".getBytes(BlockPacket.CHARSET));
                }
            }
        }
    }

    // 根据 VoteEnum 构建对应状态的 VoteInfo
    private VoteInfo createVoteInfo(VoteEnum ve) {
        VoteInfo vi = new VoteInfo();
        vi.setCode(ve.getCode());
        List<String> list = new ArrayList<>();
        list.add("AI");
        list.add("BlockChain");
        vi.setContents(list);
        vi.setHash(SimpleMerkleTree.getTreeNodeHash(list));
        return vi;
    }

    // 已经在连接的节点的个数
    private int getConnectedNodeCount() {
        // 本机测试时，写死为 1。
        // 实际开发部署多个节点时，按实际情况返回。
        return 1;
    }

    // PBFT 消息节点最少确认个数计算
    private int getLeastNodeCount() {
        // 本机测试时，写死为 1。
        // 实际开发部署多个节点时，PBFT 算法中拜占庭节点数量 f。
        // 总节点数 3f + 1
        return 1;
    }
}
