package org.itron.itrain.p2p.websocket;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Component
public class P2pPointClient {
    // P2P 网络中的节点既是服务端，又是客户端。作为服务端运行在 7770 端口
    // 同时作为客户端通过 ws://localhost:7771 连接到服务端
    private String wsUrl = "ws://localhost:7771/";

    // 所有客户端 WebSocket 的连接池缓存
    private List<WebSocket> localSockets = new ArrayList<>();

    public List<WebSocket> getLocalSockets() {
        return localSockets;
    }

    public void setLocalSockets(List<WebSocket> localSockets) {
        this.localSockets = localSockets;
    }

    @PostConstruct
    @Order(2)
    public void connectPeer() {
        try {
            // 创建 WebSocket 的客户端
            final WebSocketClient socketClient = new WebSocketClient(new URI(wsUrl)) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    sendMessage(this, "Shadow.Net 成功创建客户端。");
                    localSockets.add(this);
                }

                @Override
                public void onMessage(String message) {
                    log.info("Shadow.Net 客户端收到服务端发送的消息：{}", message);
                }

                @Override
                public void onClose(int i, String msg, boolean b) {
                    log.info("Shadow.Net 客户端关闭。");
                    localSockets.remove(this);
                }

                @Override
                public void onError(Exception e) {
                    log.info("Shadow.Net 客户端报错！");
                    localSockets.remove(this);
                }
            };
            // 客户端开始连接服务器
            socketClient.connect();
        } catch (URISyntaxException e) {
            log.error("Shadow.Net 连接错误：{}", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 向服务端发送消息，当前 WebSocket 的远程 Socket 地址就是服务端
     *
     * @param webSocket
     * @param message
     */
    private void sendMessage(WebSocket webSocket, String message) {
        log.info("发送给" + webSocket.getRemoteSocketAddress().getPort() + "的 P2P 消息：{}", message);
        webSocket.send(message);
    }

    /**
     * 向所有连接过的服务端广播消息
     *
     * @param message 待广播的消息
     */
    private void broadcast(String message) {
        if (localSockets.isEmpty() || Strings.isNullOrEmpty(message)) {
            return;
        }
        log.info("Glad to say broadcast to servers being started !");
        for (WebSocket socket : localSockets) {
            this.sendMessage(socket, message);
        }
        log.info("Glad to say broadcast to servers has done !");
    }

    /********** 使用方法 **********/

    // 代码编写完成后，在工程目录下运行 mvn clean package 命令，将 itrain 工程打包成 itrain.jar
    // 随后切换到 target 目录，执行 java -jar itrain.jar
    // ……
    // 日志前两行是节点作为 Server 端输出，下面是节点作为客户端和服务端互相发送的消息。
}
