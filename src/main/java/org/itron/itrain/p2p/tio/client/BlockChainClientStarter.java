package org.itron.itrain.p2p.tio.client;

import lombok.extern.slf4j.Slf4j;
import org.itron.itrain.p2p.tio.common.BlockPacket;
import org.itron.itrain.p2p.tio.common.TioConstant;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.tio.client.ClientChannelContext;
import org.tio.client.ClientGroupContext;
import org.tio.client.ReconnConf;
import org.tio.client.TioClient;
import org.tio.client.intf.ClientAioHandler;
import org.tio.client.intf.ClientAioListener;
import org.tio.core.Node;
import org.tio.core.Tio;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;

/**
 * 基于 t-io 的区块链底层 P2P 网络平台的客户端
 *
 * @author Shadowalker
 */
@Slf4j
@Component
public class BlockChainClientStarter {

    // 服务端节点
    private Node serverNode;

    // handler，包括编码、解码、消息处理
    private ClientAioHandler tioClientHandler;

    // 事件监听器，可以为 null，但建议自己实现该接口，可以参考 showcase 了解接口
    private ClientAioListener aioListener = null;

    // 断连后自动连接，若不想自动连接请设为 null
    private ReconnConf reconnConf = new ReconnConf(5000L);

    // 一组连接共用的上下文对象
    private ClientGroupContext clientGroupContext;

    private TioClient tioClient = null;
    private ClientChannelContext clientChannelContext = null;

    /**
     * 启动程序入口
     * 为保证 start()在服务启动时就能加载，用 @PostConstruct 标记使其在服务器加载 bean 的时候运行，并且只会被服务器执行一次。
     * 同时，为了保证客户端后于服务端加载，用 @Order(2) 标识了 start()。
     */
    @PostConstruct
    @Order(6)
    public void start() {
        try {
            log.info("Shadow.Net 客户端即将启动……");

            // 初始化
            serverNode = new Node(TioConstant.SERVER, TioConstant.PORT);
            tioClientHandler = new BlockChainClientAioHandler();
            clientGroupContext = new ClientGroupContext(tioClientHandler, aioListener, reconnConf);
            clientGroupContext.setHeartbeatTimeout(TioConstant.TIMEOUT);
            tioClient = new TioClient(clientGroupContext);
            clientChannelContext = tioClient.connect(serverNode);

            // 连上后，发一条消息测试
            sendMessage();
            log.info("Shadow.Net 客户端启动完毕。");
        } catch (Exception e) {
            log.error("Shadow.Net 客户端启动报错！");
            e.printStackTrace();
        }
    }

    private void sendMessage() throws Exception {
        BlockPacket packet = new BlockPacket();
        packet.setBody("Hello iTrain !".getBytes(StandardCharsets.UTF_8));
        Tio.send(clientChannelContext, packet);
    }

    // 在名为 itrain 的工程根目录运行 maven 命令：mvn clean package 将工程打包成 itrain.jar
    // 随后切换到 target 目录下，执行命令 java -jar itrain.jar
    // ……
    // 1. 服务端先行启动，随后客户端启动。
    // 2. 客户端启动完毕后，通过调用 sendMessage() 方法向服务端发送消息：Hello iTrain !
    // 3. 服务器 BlockChainServerAioHandler 收到消息后，经解码交由 handler 方法处理，并打印日志。

}
