package org.itron.itrain.p2ppbft.tio.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.itron.itrain.p2ppbft.tio.common.BlockPacket;
import org.itron.itrain.p2ppbft.tio.common.VoteEnum;
import org.itron.itrain.p2ppbft.tio.common.VoteInfo;
import org.itron.itrain.utils.merkle.SimpleMerkleTree;
import org.tio.client.intf.ClientAioHandler;
import org.tio.core.ChannelContext;
import org.tio.core.GroupContext;
import org.tio.core.Tio;
import org.tio.core.exception.AioDecodeException;
import org.tio.core.intf.Packet;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于 t-io 的区块链底层 P2P 网络平台的客户端 Handler
 *
 * @author Shadowalker
 */
@Slf4j
public class BlockChainPbftClientAioHandler implements ClientAioHandler {

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
     * <p>
     * 1. 当服务端收到了来自客户端的非 JSON 化数据，且数据为“服务端开始区块入库”，则说明 PBFT 阶段一致性已经达成，此时不处理该消息。否则，进行下一步处理。
     * 2. 当服务端收到来自客户端的非 JSON 化数据，且数据不为“服务端开始区块入库”，则说明当前不是 PBFT 阶段，这里j仅将收到的消息回传给服务端。
     * 3. 当服务端收到的是 JSON 化数据，则说明目前是 PBFT 阶段。服务端先校验消息的有效性，校验通过后再根据消息的状态码进行相应的 PBFT 逻辑处理。
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

            // 收到入库的消息则不再发送
            if ("服务端开始区块入库".equals(str)) {
                return;
            }

            // 发送 PBFT 投票信息
            // 如果收到的不是 JSON 化数据，说明仍在双方建立连接的过程中。
            // 目前连接已经建立完毕，发起投票。
            if (!str.startsWith("{")) {
                VoteInfo vi = createVoteInfo(VoteEnum.PREPREPARE);
                BlockPacket bp = new BlockPacket();
                bp.setBody(JSON.toJSONString(vi).getBytes(BlockPacket.CHARSET));
                Tio.send(channelContext, bp);
                log.info("客户端发送到服务端的 PBFT 消息：" + JSON.toJSONString(vi));
                return;
            }

            // 如果是 JSON 化数据，则表明进入了 PBFT 投票阶段
            JSONObject json = JSON.parseObject(str);
            if (!json.containsKey("code")) {
                log.info("客户端收到非 JSON 化数据！");
            }
            int code = json.getIntValue("code");
            if (code == VoteEnum.PREPARE.getCode()) {
                // 校验 Hash
                VoteInfo voteInfo = JSON.parseObject(str, VoteInfo.class);
                if (!voteInfo.getHash().equals(SimpleMerkleTree.getTreeNodeHash(voteInfo.getContents()))) {
                    log.info("客户端收到错误的 JSON 化数据！");
                    return;
                }

                // 校验成功，发送下一个状态的数据
                VoteInfo vi = createVoteInfo(VoteEnum.COMMIT);
                BlockPacket bp = new BlockPacket();
                bp.setBody(JSON.toJSONString(vi).getBytes(BlockPacket.CHARSET));
                Tio.send(channelContext, bp);
                log.info("客户端发送到服务端的 PBFT 消息：" + JSON.toJSONString(vi));
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

    /********** 后续操作 **********/
    // 在名为 itrain 的工程根目录运行 maven 命令 mvn clean package，将 itrain 工程打包成 itrain.jar
    // 随后切换到 target 目录下，执行命令 java -jar itrain.jar

}
