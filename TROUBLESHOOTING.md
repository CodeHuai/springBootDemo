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

## MyBatis

### 3. update 接口返回 id 为 null：Controller 把请求体对象原样弹回
- **模块**：orm_mybatis_demo
- **现象**：`POST /api/updateUserInfo/5` 更新成功，但返回的 data 里 `id=null`，`password`/`salt`/`createTime` 等前端没传的字段也全是 null——恰好"前端传了什么，什么才有值"
- **复现**：Controller 写成 `iUserService.updateUserInfo(id, user); return ApiResponse.success(user);`——丢弃 service 返回值，把 `@RequestBody` 反序列化出来的对象直接返回
- **根因**：返回的是"请求回声"而不是数据库状态。id 走 URL 路径不在 body 里，Jackson 只填 body 中出现的字段，id 和没传的字段保持 null
- **修复**：Controller 改为 `return ApiResponse.success(iUserService.updateUserInfo(id, user));`；Service 里调 mapper 前先 `user.setId(id)` 把路径参数合并进对象（否则 `where id = null` 匹配不到任何行，行数为 0）。附带教训：别把库里查出来的 `targetUser` 当更新参数传给 mapper——会把旧值原样写回，行数为 1 但数据没变
- **教训**：写操作的响应数据要来自"回查数据库"，不是把请求对象加工后弹回去——回声数据会骗人

### 4. 查询返回的下划线字段全是 null：驼峰映射没开
- **模块**：orm_mybatis_demo
- **现象**：`getUserById` / update 后回查，返回对象里 `phoneNumber`/`createTime`/`lastUpdateTime` 全 null，但库里这些列 `NOT NULL` 且有值；单单词字段（id/name/password/salt/email/status）全部正常
- **复现**：`resultType="com.huai.entity.User"` + `select *`，application.yml 未配置驼峰映射
- **根因**：MyBatis 结果自动映射默认要求列名与属性名精确匹配，`phone_number` ≠ `phoneNumber`，对不上的列**静默丢弃**（不报错不打警告）；yml 缺 `map-underscore-to-camel-case`
- **修复**（三选一即可，解决的是同一件事，别叠加）：
  **方案① SQL 起别名**——让结果集列名与属性名同名即可被 resultType 自动映射（MyBatis 匹配的是查询**结果集**的列名，`as` 改的正是它）：
  ```sql
  select phone_number as phoneNumber,
         create_time  as createTime,
         last_login_time   as lastLoginTime,
         last_update_time  as lastUpdateTime
  from orm_user where id = #{id}
  ```
  代价：不能再用 `select *`，且每条查询都要重写一遍别名
  **方案② resultMap 对照表**——XML 里写一次，多条查询共用，`select *` 照用：
  ```xml
  <resultMap id="userMap" type="com.huai.entity.User">
      <id     column="id"               property="id"/>
      <result column="phone_number"     property="phoneNumber"/>
      <result column="create_time"      property="createTime"/>
      <result column="last_login_time"  property="lastLoginTime"/>
      <result column="last_update_time" property="lastUpdateTime"/>
  </resultMap>
  <!-- select 标签 resultType 换成 resultMap="userMap"（值是 resultMap 的 id，不是类名）；
       名字相同的列可以省略；主键行用 <id>，其余用 <result>；resultType 与 resultMap 二选一 -->
  ```
  **方案③ yml 全局开关**（改完需重启）：
  ```yaml
  mybatis:
    configuration:
      map-underscore-to-camel-case: true
  ```
- **教训**："一半字段有值一半 null"先找 null 字段的共同点——带下划线的列全 null 就是驼峰映射问题；另注意 `lastLoginTime` 在库里真实为 null（用户从未登录），修完它仍为 null 属正常，别再排查一轮