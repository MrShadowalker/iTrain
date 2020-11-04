package org.itron.itrain.p2p.websocket;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Component
public class P2pPointServer {
    // 本机 Server 的 WebSocket 端口
    // 多机测试时可改变该值
    private int port = 7770;

    // 所有连接到服务端的 WebSocket 缓存器
    private List<WebSocket> localSockets = new ArrayList<>();

    public List<WebSocket> getLocalSockets() {
        return localSockets;
    }

    public void setLocalSockets(List<WebSocket> localSockets) {
        this.localSockets = localSockets;
    }

    /**
     * 初始化 P2P Server 端
     */
    @PostConstruct
    @Order(1)
    public void initServer() {
        /**
         * 初始化 WebSocket 的服务器定义内部类对象 socketServer，源于 WebSocketServer
         * new InetSocketAddress(port) 是 WebSocketServer 构造器的参数
         * InetSocketAddress 是（IP 地址 + 端口号）类型，即端口地址类型
         */
        final WebSocketServer socketServer = new WebSocketServer(new InetSocketAddress(port)) {

            /***** 重写 5 个事件方法，事件发生时触发对应的方法 *****/

            /**
             * 创建连接成功时触发
             * @param webSocket
             * @param clientHandshake
             */
            @Override
            public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
                sendMessage(webSocket, "Shadow.Net 服务端成功创建连接");
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
                log.info(webSocket.getRemoteSocketAddress() + " 客户端与服务器断开连接！");
                // 当客户端断开连接时，WebSocket 连接池删除该连接
                localSockets.remove(webSocket);
            }

            /**
             * 收到客户端发来的消息时触发
             * @param webSocket
             * @param msg
             */
            @Override
            public void onMessage(WebSocket webSocket, String msg) {
                log.info("Shadow.Net 服务端接收到客户端消息：{}", msg);
                sendMessage(webSocket, "收到消息");
            }

            /**
             * 连接发生错误时调用，紧接着触发 onClose 方法
             * @param webSocket
             * @param e
             */
            @Override
            public void onError(WebSocket webSocket, Exception e) {
                log.info(webSocket.getRemoteSocketAddress() + " 客户端连接错误！");
                localSockets.remove(webSocket);
            }

            /**
             * 服务端启动
             */
            @Override
            public void onStart() {
                log.info("Shadow.Net 的 WebSocket Server 端启动……");
            }
        };

        socketServer.start();
        log.info("Shadow.Net 服务端监听 socketServer 端口：{}", port);
    }

    /**
     * 向连接到本机的某客户端发送消息
     *
     * @param ws
     * @param message
     */
    private void sendMessage(WebSocket ws, String message) {
        log.info("发送给" + ws.getRemoteSocketAddress().getPort() + "的 P2P 消息是：" + message);
        ws.send(message);
    }

    /**
     * 向所有连接到本机的客户端广播消息
     *
     * @param message
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
