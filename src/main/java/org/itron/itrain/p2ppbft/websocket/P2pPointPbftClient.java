package org.itron.itrain.p2ppbft.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.itron.itrain.utils.merkle.SimpleMerkleTree;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.testng.util.Strings;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于 Spring Boot 2.0 的 WebSocket 客户端
 *
 * @author Shadowalker
 */
@Data
@Slf4j
@Component
public class P2pPointPbftClient {
    // P2P 网络中的节点既是服务端，又是客户端。
    // 作为服务端运行在 7001 端口，同时作为客户端通过 ws://localhost:7001 连接到服务端
    private String wsUrl = "ws://localhost:7001/";

    // 所有客户端 WebSocket 的连接池缓存
    private List<WebSocket> localSockets = new ArrayList<>();

    /**
     * 连接到服务端
     */
    @PostConstruct
    @Order(2)
    public void connectPeer() {
        try {
            // 创建 WebSocket 的客户端
            final WebSocketClient socketClient = new WebSocketClient(new URI(wsUrl)) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    sendMessage(this, "PBFT 客户端成功创建客户端。");
                    localSockets.add(this);
                }

                /**
                 * 实现了客户端的 PBFT 算法
                 * 1. 当客户端收到的是非 JSON 化数据时，说明当前不是 PBFT 阶段。
                 * 2. 当客户端收到的消息内容是 JSON 化数据时，说明目前已经进入到了 PBFT 投票阶段。客户端对数据信息进行成功校验后，将投票状态置为 Prepare 状态。
                 * 3. 当客户端校验消息成功后，且当前校验节点确认个数有效，则开始向服务端发送“客户端开始区块入库”消息。
                 *
                 * @param message
                 */
                @Override
                public void onMessage(String message) {
                    log.info("客户端收到服务端发送的消息：" + message);
                    // 如果收到的不是 JSON 化数据，则说明不是 PBFT 阶段
                    if (!message.startsWith("{")) {
                        return;
                    }

                    // 如果收到的是 JSON 化数据，则说明是 PBFT 阶段
                    // 如果是 JSON 化数据，则进入到了 PBFT 投票阶段
                    JSONObject json = JSON.parseObject(message);
                    if (!json.containsKey("code")) {
                        log.info("客户端收到非 JSON 化数据！");
                    }
                    int code = json.getIntValue("code");
                    if (code == VoteEnum.PREPREPARE.getCode()) {
                        // 校验 Hash
                        VoteInfo voteInfo = JSON.parseObject(message, VoteInfo.class);
                        if (!voteInfo.getHash().equals(SimpleMerkleTree.getTreeNodeHash(voteInfo.getContents()))) {
                            log.info("客户端收到服务端错误的 JSON 化数据。");
                            return;
                        }
                        // 校验成功，发送下一个状态的数据
                        VoteInfo vi = createVoteInfo(VoteEnum.PREPARE);
                        sendMessage(this, JSON.toJSONString(vi));
                        log.info("客户端发送到服务端 PBFT 消息：" + JSON.toJSONString(vi));
                        return;
                    }
                    if (code == VoteEnum.COMMIT.getCode()) {
                        // 校验 Hash
                        VoteInfo voteInfo = JSON.parseObject(message, VoteInfo.class);
                        if (!voteInfo.getHash().equals(SimpleMerkleTree.getTreeNodeHash(voteInfo.getContents()))) {
                            log.info("客户端收到服务端错误的 JSON 化数据。");
                            return;
                        }
                        // 校验成功，校验节点确认个数是否有效
                        if (getConnectedNodeCount() >= getLeastNodeCount()) {
                            sendMessage(this, "客户端开始区块入库");
                            log.info("客户端开始区块入库");
                        }
                    }
                }

                @Override
                public void onClose(int i, String message, boolean b) {
                    log.info("客户端关闭");
                    localSockets.remove(this);
                }

                @Override
                public void onError(Exception e) {
                    log.info("客户端报错");
                    localSockets.remove(this);
                }
            };

            // 客户端开始连接服务器
            socketClient.connect();
        } catch (URISyntaxException e) {
            log.info("PBFT 连接错误：" + e.getMessage());
        }
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

    /**
     * 向服务端发送消息。
     * 当前 WebSocket 的远程 Socket 地址就是服务端。
     *
     * @param socket
     * @param message
     */
    private void sendMessage(WebSocket socket, String message) {
        log.info("发送给：" + socket.getRemoteSocketAddress().getPort() + "的 P2P 消息：" + message);
        socket.send(message);
    }

    public void broadcast(String message) {
        if (localSockets.isEmpty() || Strings.isNullOrEmpty(message)) {
            return;
        }
        log.info("Glad to say broadcast to servers being started !");
        for (WebSocket socket : localSockets) {
            this.sendMessage(socket, message);
        }
        log.info("Glad to say broadcast to servers has done !");
    }

    /********** 后续操作 **********/

    // 代码编写完成后，我们在名为 itrain 的工程目录下运行 mvn clean package 命令，将 itrain 工程打包成 itrain.jar
    // 随后切换到 target 目录，执行 java -jar itrain.jar

}
