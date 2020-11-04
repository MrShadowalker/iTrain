# 基于 t-io 构建 P2P 网络

## t-io 介绍

t-io/tio 是一个网络编程框架，或称为 TCP 长连接框架，其官方网站中宣称，t-io/tio 不仅仅是百万级 TCP 长连接框架。 基于 t-io 开发 IM、TCP
私有协议、RPC、游戏服务器端、推送服务、实时监控、物联网、UDP、Socket 等将会变得空前的简单。

t-io 一般是指 tio-core，它是基于 Java AIO 的网络编程框架，和 Netty 属于同类。 t-io 家族除 tio-core 外，还有
tio-websocket-server、tio-http-server、tio-webpack-core、tio-flash-policy-server 等，后面所列都是基于tio-core 开发的应用层组件。 下面分别介绍 t-io
家族的各个成员，👇

- tio-core：基于 Java AIO 网络编程框架。
- tio-websocket-server：基于 tio-core 开发的 WebSocket 服务器。
- tio-http-server：基于 tio-core 开发的 HTTP 服务器。
- tio-webpack-core：基于 tio-core 开发的 JS/CSS/HTML 编译压缩工具。
- tio-flash-policy-server：基于 tio-core 开发的 flash-policy-server。

## t-io 的主要用法

下面介绍 t-io 常用类。t-io 常用类主要有 ChannelContext、GroupContext、AioHandler、AioListener、Packet、AioServer 和 AioClient。
其中，ChannelContext 是通道上下文相关的类，GroupContext 用于服务配置与维护，AioHandler 是消息处理接口，AioListener 是通道监听类，Packet 是应用层的数据包，AioServer 是
t-io 服务端的入口类，AioClient 是 t-io 客户端的入口类，具体介绍如下。

### ChannelContext（通道上下文）

每一个 TCP 连接的建立都会产生一个 ChannelContext 对象，这是一个抽象类。 如果用 t-io 作为 TCP 客户端，那么就是 ClientChannelContext；如果用 t-io 作为 TCP 服务端，那么就是
ServerChannelContext。 客户端和服务端常用的 ChannelContext 如下：👇

#### ServerChannelContext

ChannelContext 的子类，当 t-io 作为 TCP 服务端时，业务层接触的是这个类的实例。

#### ClientChannelContext

ChannelContext 的子类，当 t-io 作为 TCP 客户端时，业务层接触的是这个类的实例。

### GroupContext（服务配置与维护）

GroupContext 用来配置线程池，确定监听端口，维护客户端的各种数据等。GroupContext 是个抽象类。 如果 t-io 作为 TCP 客户端，那么需要创建 ClientGroupContext；如果 t-io 作为 TCP
服务端，那么需要创建 ServerGroupContext。

### AioHandler（消息处理接口）

AioHandler 是处理消息的核心接口，它有两个子接口：ClientAioHandler 和 ServerAioHandler。 当 t-io 作为 TCP 客户端时，需要实现 ClientAioHandler；当 t-io 作为
TCP 服务端时，需要实现 ServerAioHandler。 AioHandler 主要定义了 3 个方法，decode()、encode() 和 handler()，分别处理编码、解码和消息包。

### AioListener（通道监听者）

AioListener 是处理消息的核心接口，它有两个子接口，ClientAioListener 和 ServerAioListener。 当 t-io 作为 TCP 客户端时，需要实现 ClientAioListener；当 t-io
作为 TCP 服务端时，需要实现 ServerAioListener。 AioListener 主要定义了如下方法：👇 onAfterClose()、onAfterConnected()、onAfterSend()
、onAfterReceived() 和 onBeforeClosed()。 其中，onAfterClose() 在连接关闭后触发，onAfterReceived() 在连接建立后触发，onAfterSend()
在消息包发送之后触发，onAfterReceived() 在收到消息并解码成功后触发，onBeforeClosed() 在连接关闭前触发。

### Packet（应用层数据包）

TCP 层过来的数据，都会按 t-io 要求解码成 Packet 对象，应用都需要继承这个类，从而实现自己的业务数据包。

### AioServer（tio 服务端入口类）

AioServer 是 t-io 服务端入口类，在 start 方法中启动制定 IP 和端口的服务。

### AioClient（tio 客户端入口类）

AioClient 是 t-io 客户端入口类，提供 connect()、asynconnect()方法族用于和服务端的同步和异步信息传输。