# SQLite 介绍

SQLite 是目前世界上使用最多的数据库引擎。 不同于常用的 MySQL 或 Oracle 数据库，SQLite 是一个进程内的库，实现了自给自足的、无服务器的、零配置的、事务性的 SQL 数据库引擎。 数据库零配置是 SQLite
区别于其他数据库的主要特点。

与 MySQL 和 Oracle 数据库类似之处在于，SQLite 引擎不是一个独立的进程，可以按应用程序需求进行静态或动态连接，SQLite 可直接访问其存储文件。

什么场景适用于 SQLite 呢？比如不需要一个单独的服务器进程或操作的系统（无服务器）的场景。因为 SQLite 不需要配置，所以使用 SQLite 并不需要像 MySQL 那样进行安装与管理。

此外，SQLite 非常小，是轻量级的，完全配置时小于 400 KB ，省略可选功能配置时小于 250 KB。SQLite 无需任何外部的依赖，能实现自给自足。

与 MySQL 和 Oracle 一样，SQLite 事务是完全兼容 ACID 的，允许从多个进程或线程安全访问。SQLite 支持 SQL92（SQL2）标准的大多数查询语言的功能，为了方便用户使用，SQLite 提供了简单和易用的
API。

SQLite 的系统适配性良好，不仅可以再 UNIX、LINUX、Windows、Mac OS-X 系统中运行，还能在 Android、iOS 系统中运行。