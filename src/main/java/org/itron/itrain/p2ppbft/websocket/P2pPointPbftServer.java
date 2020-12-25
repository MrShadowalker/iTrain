package org.itron.itrain.p2ppbft.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.itron.itrain.utils.merkle.SimpleMerkleTree;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.testng.util.Strings;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于 Spring Boot 2.0 的 WebSocket 服务端
 *
 * @author Shadowalker
 */
@Data
@Slf4j
@Component
public class P2pPointPbftServer {

    // 本机 Server 的 WebSocket 端口
    // 多机测试时可改变该值
    private int port = 7001;

    // 所有连接到服务端的 WebSocket 缓存器
    private List<WebSocket> localSockets = new ArrayList<>();

    /**
     * 初始化 P2P Server 端
     */
    @PostConstruct
    @Order(3)
    public void initServer() {
        /**
         * 初始化 WebSocket 的服务端，定义内部类对象 socketServer，源于 WebSocket
         * new InetSocketAddress(port) 是 WebSocketServer 构造器的参数
         * InetSocketAddress 是 IP 地址 + 端口号类型，即端口地址类型
         */
        final WebSocketServer socketServer = new WebSocketServer(new InetSocketAddress(port)) {

            // 重写五个事件方法，事件发生时触发对应的方法

            /**
             * 创建连接成功时触发
             *
             * @param webSocket
             * @param clientHandshake
             */
            @Override
            public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
                sendMessage(webSocket, "PBFT 服务端成功创建连接。");
                // 当成功创建一个 WebSocket 连接时，将该连接加入连接池
                localSockets.add(webSocket);
            }

            /**
             * 断开连接时触发
             * @param webSocket
             * @param i
             * @param s
             * @param b
             */
            @Override
            public void onClose(WebSocket webSocket, int i, String s, boolean b) {
                log.info(webSocket.getRemoteSocketAddress() + "客户端与服务器断开连接！");
                // 当客户端断开连接时，webSocket 连接池删除该连接
                localSockets.remove(webSocket);
            }

            /**
             * 收到客户端发来的消息时触发
             *
             * 1. 当服务端收到“客户端开始区块入库”的消息时，服务端认为 PBFT 共识已经达成，因此不再对消息进行处理。
             * 2. 当服务端收到的消息内容不是 JSON 化数据时，说明扔在双方建立连接的过程中。目前连接刚刚建立完毕，于是发起投票，投票的数据状态置为 Pre-prepare 状态。
             * 3. 当服务端收到的消息内容是 JSON 化数据，则说明目前已经进入到了 PBFT 投票阶段。服务端对数据信息进行成功校验后，讲投票状态置为 Commit 状态。
             *
             * @param webSocket
             * @param message
             */
            @Override
            public void onMessage(WebSocket webSocket, String message) {
                log.info("PBFT 服务端接收到客户端消息：" + message);
                // 收到入库的消息则不再发送
                if ("客户端开始区块入库".equals(message)) {
                    return;
                }

                // 如果收到的不是 JSON 化数据，则说明仍处在双方建立连接的过程中。
                // 目前连接已经建立完毕，发起投票。
                if (!message.startsWith("{")) {
                    VoteInfo vi = createVoteInfo(VoteEnum.PREPREPARE);
                    sendMessage(webSocket, JSON.toJSONString(vi));
                    log.info("服务端发送到客户端的 PBFT 消息：" + JSON.toJSONString(vi));
                    return;
                }

                // 如果是 JSON 化数据，则表明已经进入到了 PBFT 投票阶段
                JSONObject json = JSON.parseObject(message);
                if (!json.containsKey("code")) {
                    log.info("服务端收到非 JSON 化数据！");
                }

                int code = json.getIntValue("code");
                if (code == VoteEnum.PREPARE.getCode()) {
                    // 校验 Hash
                    VoteInfo voteInfo = JSON.parseObject(message, VoteInfo.class);
                    if (!voteInfo.getHash().equals(SimpleMerkleTree.getTreeNodeHash(voteInfo.getContents()))) {
                        log.info("服务端接收到错误的 JSON 化数据！");
                        return;
                    }

                    // 校验成功，发送下一个状态的数据
                    VoteInfo vi = createVoteInfo(VoteEnum.COMMIT);
                    sendMessage(webSocket, JSON.toJSONString(vi));
                    log.info("服务端发送到客户端 PBFT 消息：" + JSON.toJSONString(vi));
                }
            }

            /**
             * 连接发生错误时调用，并触发onClose 方法
             * @param webSocket
             * @param e
             */
            @Override
            public void onError(WebSocket webSocket, Exception e) {
                log.info(webSocket.getRemoteSocketAddress() + "客户端连接错误！");
                localSockets.remove(webSocket);
            }

            /**
             * 服务端启动
             */
            @Override
            public void onStart() {
                log.info("WebSocket Server 端启动……");
            }
        };
        socketServer.start();
        log.info("服务端监听 socketServer 端口：" + port);
    }

    /**
     * 根据 VoteEnum 构建对应状态的 VoteInfo
     *
     * @param ve
     * @return
     */
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
     * 向连接到本机的某客户端发送消息
     *
     * @param webSocket
     * @param message
     */
    private void sendMessage(WebSocket webSocket, String message) {
        log.info("发送给" + webSocket.getRemoteSocketAddress().getPort() + "的 P2P 消息是：" + message);
        webSocket.send(message);
    }

    /**
     * 向所有连接到本机的客户端广播消息
     *
     * @param message 待广播内容
     */
    public void broadcast(String message) {
        if (localSockets.isEmpty() || Strings.isNullOrEmpty(message)) {
            return;
        }
        log.info("Glad to say broadcast to clients being started !");
        for (WebSocket socket : localSockets) {
            this.sendMessage(socket, message);
        }
        log.info("Glad to say broadcast to clients has done !");
    }
}
