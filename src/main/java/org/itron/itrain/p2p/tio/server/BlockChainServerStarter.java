package org.itron.itrain.p2p.tio.server;

import lombok.extern.slf4j.Slf4j;
import org.itron.itrain.p2p.tio.common.TioConstant;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.tio.server.ServerGroupContext;
import org.tio.server.TioServer;
import org.tio.server.intf.ServerAioHandler;
import org.tio.server.intf.ServerAioListener;

import javax.annotation.PostConstruct;

/**
 * 基于 t-io 的区块链底层 P2P 网络平台的服务端
 *
 * @author Shadowalker
 */
@Slf4j
@Component
public class BlockChainServerStarter {

    // handler，包括编码、解码、消息处理
    public static ServerAioHandler aioHandler = new BlockChainServerAioHandler();

    // TODO 事件监听器，可以为 null，但建议自己实现该接口，可以参考 showcase 了解接口
    public static ServerAioListener aioListener = null;

    // 一组连接共用的上下文对象
    public static ServerGroupContext serverGroupContext = new ServerGroupContext("hello-tio-server", aioHandler, aioListener);

    // tioServer 对象
    public static TioServer tioServer = new TioServer(serverGroupContext);

    // 有时候需要绑定 IP，不需要则为 null
    public static String serveIp = null; // TioConstant.SERVER

    // 监听的端口
    public static int serverPort = TioConstant.PORT;

    @PostConstruct
    @Order(1)
    public void start() {
        try {
            log.info("Shadow.Net 服务端即将启动……");
            serverGroupContext.setHeartbeatTimeout(TioConstant.TIMEOUT);
            tioServer.start(serveIp, serverPort);
            log.info("Shadow.Net 服务端启动完毕。");
        } catch (Exception e) {
            log.error("Shadow.Net 服务端启动报错。");
        }
    }
}
