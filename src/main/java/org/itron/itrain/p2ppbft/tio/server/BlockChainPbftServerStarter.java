package org.itron.itrain.p2ppbft.tio.server;

import lombok.extern.slf4j.Slf4j;
import org.itron.itrain.p2ppbft.tio.common.Const;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.tio.server.ServerGroupContext;
import org.tio.server.TioServer;
import org.tio.server.intf.ServerAioHandler;
import org.tio.server.intf.ServerAioListener;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * 基于 t-io 的区块链底层 P2P 网络平台的服务端
 *
 * @author Shadowalker
 */
@Slf4j
@Component
public class BlockChainPbftServerStarter {

    // handler，包括编码、解码、消息处理
    public static ServerAioHandler aioHandler = new BlockChainPbftServerAioHandler();

    // 事件监听器，可以为 null，但建议自己实现该接口，可以参考 showcase 了解接口
    public static ServerAioListener aioListener = null;

    // 一组连接共用的上下文对象
    public static ServerGroupContext serverGroupContext = new ServerGroupContext("hello-tio-server", aioHandler, aioListener);

    // tioServer 对象
    public static TioServer tioServer = new TioServer(serverGroupContext);

    // 有时候需要绑定 IP，不需要则为 null
    public static String serverIp = null; // Const.SERVER

    // 监听的端口
    public static int serverPort = Const.PORT;

    /**
     * 服务端启动
     */
    @PostConstruct
    @Order(1)
    public void start() {
        try {
            log.info("服务端即将启动……");
            serverGroupContext.setHeartbeatTimeout(Const.TIMEOUT);
            tioServer.start(serverIp, serverPort);
            log.info("服务端启动完毕。");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
