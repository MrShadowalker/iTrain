在联盟链中，联盟各个节点往往来自同一行业，有着共同的行业困扰和痛点，因此联盟链往往注重对实际问题的高效解决。
而 PoW 算法相对低效且费时费力，因此在联盟链中并不适用。
相反，在公链中很小适用的 PBFT 算法在联盟链中却有用武之地。

PBFT 算法及变种算法应用较为广泛，目前此项目基于 Hyperledger 项目，其正在开发基于 PBFT 实现的共识算法。
因此，本项目采用 PBFT 实现共识算法，并基于 WebSocket 和 t-io 将其实现。

# PBFT 概述
Practical Byzantine Fault Tolerance： PBFT，是联盟币的共识算法的基础。
实现了在有限个节点的情况下的拜占庭问题，有 3f+1 的容错性，并同时保证一定的性能。

## 容错率
raft 算法的的容错只支持容错故障节点，不支持容错作恶节点，所以容错率高，过半节点正常即可。
PBFT 算法可以容忍小于1/3个无效或者恶意节点。
作恶节点：除了可以故意对集群的其它节点的请求无响应之外，还可以故意发送错误的数据，或者给不同的其它节点发送不同的数据，使整个集群的节点最终无法达成共识，这种节点就是作恶节点。

## 角色
Primary节点和普通节点，PBFT系统的Primary是轮流当选的，这和zab、raft不一样

- 主节点 p = v mod |R|
- p：主节点编号
- v：视图编号
- |R|节点个数

### Primary角色分析

Primary节点的作用：

1. 正常工作时，接收客户端的事务请求，验证request身份后，为该请求设置编号，广播pre-prepare消息
2. 新Primary当选时，根据自己收集的View-Change消息，发送View-New信息，让其它节点同步数据
3. Primary与所有的其它节点维系心跳

Primary节点地位和follower节点一样，并没有什么特权

1. 如果Primary宕机，会因为心跳超时，而触发重新选举，保证系统运行稳定
2. 如果Primary恶意发送错误编号的消息，那么会在后续的操作中，被follower察觉，因为 prepare和commit阶段都是会进行广播的，一旦不一致，view-change
3. 如果Primary不发送接收到的request，client在超时未回复时，会重发request到所有的replica，小弟们发现primary竟然私藏消息，view-change
4. 如果Primary节点篡改消息，因为有Request里面有data和client的签名，所以primary无法篡改消息，其它replica会先验证消息的合法性，否则丢弃，view-change

综上所述，限制了权限的Primary节点，如果宕机、或者不发生消息、或者发送错误编号的消息、或者篡改消息，都会被其它节点感知，并触发view-change。

# PBFT工作正常的详细流程

客户端发起请求 --> 转发请求到 primary --> primary 生成 proposal --> primary 广播 proposal --> 所有节点复制 proposal 并广播 
--> 复制过半节点完成 --> 广播 commit 节点 --> commit 过半节点完成 --> 应用状态机 --> 反馈客户端 --> 客户端统计 f+1 个反馈消息 --> 交易完成

1. 系统根据机器编号顺序轮流选举出一个primary，primary初始化时发出View-new消息，同步所有节点的数据
2. Client发起请求转发给primary，primary验证通过后，广播这个请求，发起pre-prepare消息给所有的follower节点，并且自己也保存这个request
3. 所有的follower收到pre-prepare消息后，第一步是进行校验，包括数据的顺序是否正确，操作的先后有序性，以及交易是否有效比如签名。（防止客户端造假或者primary节点篡改造假）
4. follower验证正确之后，写到自己的磁盘里，然后广播Prepare消息，并且自己也进入Prepare阶段
5. 所有节点统计针对某个Request的Prepare消息，当统计结果超过2f节点时，表明大部分节点已经完成了持久化，则自己进入commit阶段
6. 广播 commit 消息，并且统计收到的commit 消息的数量，当超过2f节点都发出commit的消息时
7. 该节点完成commit阶段，写入数据（该操作已经完成 2/3 共识了），运用自己的状态机，更新 stable checkpoint，缓存该客户端最后一次的请求，并且反馈给客户端
8. 当客户端统计反馈的节点超过f个时，表示交易已经被大部分节点确认了，交易成功。如果超时还不成功，则向所有的replica广播这个request

**解释：**

1. 为什么客户端收到f+1个确认时，交易就成功了？
因为默认问题节点为f，那么f+1个确认节点中，肯定有1个是诚实的节点，只要有 1 个诚实的确认消息，则交易成功，因为1个诚实的消息必须是2f+1个节点都commit操作成功了，才可能有这个1个最终确认消息的。所以为了提升交易处理的速度，只要有f+1个确认反馈，就可以表示交易成功。

## 流程描述

PBFT 分五个阶段实施，执行前需要先在全网节点选举出一个主节点，新区块的生成由主节点负责。
选举出主节点后，开始落实 PBFT。
PBFT 的五个阶段分别是：Request 阶段、Pre-prepare 阶段、Prepare 阶段、Commit 阶段、执行并 Reply。
下面分别阐述各个阶段的处理逻辑。
假设此时有主 M0 节点、从 S1 节点、从 S2 节点和从 S3 节点共 4 个节点。

### Request 阶段
客户端发起请求。
### Pre-prepare 阶段
M0 收到客户端请求后给请求进行编号
### Prepare 阶段
S1 同意 M0 请求的编号，并发送 prepare 类型消息给 M0 和其他从节点。如果 S1 不同意请求的编号，则不进行处理。
S1 如果收到另外两个从节点都发出的同意 M0 分配的编号的 prepare 类型消息，则表示 S1 节点的状态置为 Prepared，进入 Commit 阶段。
### Commit 阶段
S1 进入 Prepared 状态后，将发送一条 commit 类型信息给其他所有节点，告诉其他节点当前 S1 已经进入 Prepared 状态了。
如果 S1 收到 2f+1 条 commit 信息，则证明 S1 已经进入 Commit 状态。
当一个请求在某个节点中到达 Commit 状态后，该请求就会被该节点执行。
### 执行及 Reply 阶段
执行区块生成并 Reply 生成结果。

## 客户端Client发起请求

1. 客户端 c 通过向副本多播一条<REQUEST，o，t，c>到系统中
2. 副本对Request进行身份验证
3. 验证成功，则接受请求并将其添加到它们的日志中，请求执行使用request中的时间戳进行排序执行
4. 副本直接将请求的回复发送给客户端
5. 客户端在接受结果 r 之前，等待一个来自不同副本的有 f + 1 个带有有效 MACs 的以及相 同的 t 和 r 的 weak certificate
6. 如果客户端没有足够迅速的收到一个 reply certificate，则会重新发送请求。如果请求已被处理，则副本只是重新发送回复;副本记住他们发送给每个客户端的最后一个回复消息以 启用此重传

**客户端请求消息：**

客户端直接向Primary节点发起一个请求：消息的格式<REQUEST, o, t, c>

- o: 请求的具体操作，operation
- t: 请求时客户端追加的时间戳，time，这里面追加的是client的时间戳，会在后面的时候，客户端的请求做时间戳排序，结合请求编号一起，保证消息的有序性（不仅仅是写操作，还有读写操作）
- c：客户端标识，clientID，其中 c + t 就是requestID
- REQUEST: 包含消息内容m，以及消息摘要d(m)。客户端对请求进行签名。

服务端在执行request时会进行签名验证，因为PBFT应用的是联盟链，而不是私链，所以要对操作者的身份进行校验，比如A发起一笔转账，则服务端需要check是不是A发起的转账，防止盗刷

**服务端回复消息：**

<REPLY, v, t, c, i, r>

- REPLY，消息类型为回复客户端
- v，当前view
- t，c 哪个client的时间戳为t的回复（而不是通过 zxid，是通过时间戳，相当于requestID）
- i 当前node编号
- r 操作结果，还必须有server i的签名，客户端要验证的，防止网络拦截和欺诈

## 消息

消息的类型（pre-prepare、prepare、commit）

- View（类似于term）
- n（类似于index)
- d（交易的详细信息)
- m(交易的签名）
- i（节点的编号）
- checkpoint ：节点参数，该节点最新的proposal编号
- stable checkpoint：系统参数，该系统中，最新的超过 2f 节点 commit 过了的 proposal 的编号。可以减少内存的占用，已经2f+1确认过的操作，就最终确认了，后续不需要操作了，可以从内存中移除了。

## 重新选举 viewChange
当普通节点感知到 primary 异常的时候，触发 viewChange，重新选举必须要有 2f+1 个节点都 confirm（VIEW-CHANGE）了，发起重选才生效，一旦超过 2f 节点都发起 VIEW-CHANGE 消息，则选举结束，p = v+1 mod |R|节点当选为 new Primary。
并且 new primary 会根据自己统计的 VIEW-CHANGE 的内容，生成并广播NEW-VIEW消息，其它节点验证之后，开始新的view

<VIEW-CHANGE, v+1, n, C, P, i>消息

- v+1：新的view编号
- n：是最新的stable checkpoint的编号
- C：是2f+1验证过的CheckPoint消息集合
- P：是当前副本节点未完成的请求的PRE-PREPARE和PREPARE消息集合

新的主节点就是 newPrimary = v + 1 mod |R|。当newPrimary收到2f个有效的VIEW-CHANGE消息后，向其他节点广播NEW-VIEW消息

<NEW-VIEW, v+1, V, O>

- V：是有效的VIEW-CHANGE消息集合
- O：是主节点重新发起的未经完成的PRE-PREPARE消息集合

未完成的PRE-PREPARE消息集合的生成逻辑：

- 选取V中最小的stable checkpoint编号min-s，选取V中prepare消息的最大编号max-s。
- 在min-s和max-s之间，如果存在P消息集合，则创建<<PRE-PREPARE, v+1, n, d>, m>消息。否则创建一个空的PRE-PREPARE消息，即：<<PRE-PREPARE, v+1, n, d(null)>, m(null)>, m(null)空消息，d(null)空消息摘要。

副本节点收到主节点的NEW-VIEW消息，验证有效性（各个replica都统计view-change的个数），有效的话，进入v+1状态，并且开始O中的PRE-PREPARE消息处理流程。

特殊情况：
那么如果一半的节点和primary网络分区了，那也无法发起重选。
同时primary也执行不了新的操作，因为新的消息有一半节点收不到，整个集群陷入瘫痪状态。所以primary也应该和zab一样，具备自我检测超时，超过一定个数的ack缺失时，触发重新选举。



# PBFT 现状

## PBFT的特点

1. 客户端事务请求的严格有序性
   request里面包含了时间戳，request在服务端执行的时候，按照时间戳进行排序执行。而zab协议、raft协议都是按照先到先执行的有序性（服务端），但是PBFT却能按照Client的有序性。即使网络问题，先发起的请求晚于后发起的请求抵达服务端，服务端也不会打乱执行的顺序，PBFT是更严格的操作有序性。这也提高了系统的复杂度。

2. 性能尚可
   PBFT 算法通信复杂度 o（n^2），因为系统在尝试达成状态共识时，涉及到N个几点都需要广播N-1个其它节点。而在没有作恶节点的zab、raft系统中，通信复杂度 O(N)

## Raft Vs PBFT

1. Raft系统中leader拥有最高权限，follower如果和leader数据不一致，那么必须删除自己的数据，保持和leader一致
2. PBFT中，Primary向我发送命令时，当我认为老大的命令是有问题时，我会拒绝执行。并且很有可能会触发view-change。就算我认为老大的命令是对的，我还会问下团队的其它成员老大的命令是否是对的，只有大多数人 （2f+1） 都认为老大的命令是对的时候，我才会去执行命令

目前，Hyperledger Fabric 中已将 PBFT 纳入其候选共识算法集。不过，PBFT 的缺点也很明显，由于先选举主节点，因此当主节点宕机不得不重新选举主节点时，PBFT 将无法正常达成共识。
此外，虽然联盟链中各个节点都是由同行业内知根知底的机构组成的，但安全性同样不可小觑。一旦主节点被攻击甚至是作恶，其他节点并不能及时发现。这也是联盟链中不可避免要处理的问题。
