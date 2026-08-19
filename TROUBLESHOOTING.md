# 纠错记录（TROUBLESHOOTING）

> 用途：沉淀排查过的报错与 bug，定期复盘用。记录哪些条目由用户决定。
> 格式：模块 → 现象 → 复现 → 根因 → 修复 → 教训。只追加，不覆盖。

## 数据库与连接

### 1. Host 'xxx' is not allowed to connect to this MySQL server
- **模块**：orm_JDBCTemplate_demo
- **现象**：应用启动、连接 MySQL 时报 `Host 'BK-gsdn' is not allowed to connect to this MySQL server`（BK-gsdn 是本机主机名）
- **复现**：配置好 datasource 后启动 Spring Boot 应用，连接阶段即抛出
- **根因**：MySQL 的账户是「用户名@来源主机」二元组，权限表 `mysql.user` 里没有匹配当前来源主机的条目。显示主机名是因为 MySQL 对客户端 IP 做了反向 DNS
- **修复**：
  ```sql
  CREATE USER 'root'@'%' IDENTIFIED BY '密码';
  GRANT ALL PRIVILEGES ON *.* TO 'root'@'%';
  FLUSH PRIVILEGES;
  ```
- **教训**：这个报错 ≠ 密码错——它是「账户@主机组合不存在」，连验密码的资格都没有。生产环境禁止 `root@'%'`

### 2. JDBC URL 里库名写成了表名
- **模块**：orm_JDBCTemplate_demo
- **现象**：连接报 `Unknown database`，或库表撞名连上但语义混乱
- **复现**：datasource url 写成 `jdbc:mysql://127.0.0.1:3306/orm_user`（orm_user 实为表名）后启动
- **根因**：URL 该位置是库名（schema），不是表名
- **修复**：建语义独立的库，URL 指向库，DDL 脚本补 `CREATE DATABASE IF NOT EXISTS ... ; USE ...;`
- **教训**：URL 结构 `jdbc:mysql://主机:端口/库名?参数`，每个位置含义固定