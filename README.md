# iTrain

## 架构设计

本项目提供的联盟链底层架构如下：

### 应用层

联盟应用 1，联盟应用 2，……

### 激励层

Token，Coin

### 共识层

PBFT

### 网络层

P2P 网络

### 数据层

区块，区块“链”，Hash，Merkle 树，非对称加密数字签名，时间戳

### 存储层

日志，SQLite，LevelDB/RocksDB

## 设计分析

- 存储层主要存储交易日志和交易相关的内容。其中，交易日志基于 LogBack 实现。交易的内容由内置的 SQLite 数据库存储，读写 SQLite 可以基于 JPA 实现；交易的上链元数据信息由 RocksDB 或 LevelDB
  存储。

- 数据层由区块和链（区块的链式结构）组成。其中，区块中还会涉及交易列表在 Merkle
  树中的存储及根节点哈希值的计算。交易的内容也需要加密处理。由于在联盟链中有多个节点，为有效管理节点数据及保障数据安全，建议为不同节点分配不同的公、私钥，以便加密使用。

- 网络层主要提供共识达成及数据通信的底层支持。在区块链中，每个节点既是数据的发送方，又是数据的接收方。可以说每个节点既是客户端，又是服务端，因此需要基于长连接来实现。在本项目中，我们可以基于 WebSocket
  用原生方式建立长连接，也可以基于长连接第三方工具包实现。

- 共识层采用 PBFT（Practical Byzantine Fault Tolerance）共识算法。不同于公链的挖矿机制，联盟链中更注重各节点信息的统一，因此可以省去挖矿，直奔共识达成的目标。

- 激励层主要是币（Coin）和 Token 的颁发和流通。在公链中，激励是公链的灵魂；但在联盟链中不是必需的。本项目暂时不对联盟链中 Coin 或 Token 如何建立经济模型和高效使用，甚至是增值进行使用，仅从技术维度实现 Coin 或
  Token 的相关逻辑。

- 应用层主要是联盟链中各个产品的落地。一般联盟链的应用层都是面向行业的，解决行业内的问题。

## Java 联盟链部署架构

联盟链暂由 1
个超级节点和若干个普通节点组成，超级节点除具备普通节点的功能外，还具备在联盟中实施成员管理、权限管理、数据监控等工作。因此相较于完全去中心化的公链，联盟链是部分去中心化的，或者说联盟的“链”是去中心化的，但是联盟链的管理是中心化的。

## 相关开发

整个开发环境建议基于 Spring Boot 2.0 实现。基于 Spring Boot 开发，可以省去大量的 xml 配置文件的编写，能极大简化工程中在 POM 文件配置的复杂依赖。Spring Boot 还提供了各种
starter，可以实现自动化配置，提高开发效率。
