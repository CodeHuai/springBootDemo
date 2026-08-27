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

## 依赖与版本兼容

### 5. 启动报 documentationPluginsBootstrapper NPE：Springfox 3.0.0 不兼容 Spring Boot 2.6+
- **模块**：orm-mybatis-plus_demo
- **现象**：跑 `@SpringBootTest` 测试（或启动应用）时容器起不来，报 `Failed to start bean 'documentationPluginsBootstrapper'; nested exception is java.lang.NullPointerException`
- **复现**：pom 同时存在 `springfox-boot-starter 3.0.0`（模块）+ Spring Boot `2.6.13`（父 pom），任何触发全量上下文启动的操作即抛
- **根因**：Spring Boot 2.6 起 Spring MVC 默认路径匹配策略从 `AntPathMatcher` 换成 `PathPatternParser`；Springfox 2020 年发完 3.0.0 即停更、没适配这次变更，启动时扫描 Controller 路径映射按老 API 取值拿到 null → NPE。`documentationPluginsBootstrapper` 不是自己代码里的 Bean，是 springfox starter 自动装配出来的（Swagger 文档启动器）
- **修复**（两选一）：
  **方案① 退回老匹配策略**（想继续用 Swagger）——application.yml 加：
  ```yaml
  spring:
    mvc:
      pathmatch:
        matching-strategy: ant_path_matcher
  ```
  **方案② 删掉 springfox-boot-starter 依赖**——本模块练 ORM、测试全走 JUnit，根本用不到接口文档，少一个依赖少一片雷区（需要接口文档时再引，且优先选仍在维护的 springdoc-openapi）
- **教训**：停更的开源库 × 升级中的框架 = 版本雷区，跟教程抄依赖前先看它的发布年代；报错里"自己代码搜不到的 Bean 名"直接整段拿去搜索，就是最快的定位线索；`@SpringBootTest` 是全量装配——pom 里每个 starter 都会被拉起，用不到的别引

### 6. PaginationInterceptor / PerformanceInterceptor 爆红：老教程的类在 MP 3.5.x 里已删除
- **模块**：orm-mybatis-plus_demo
- **现象**：配置类里 `new PaginationInterceptor()`、`new PerformanceInterceptor()` 两处 `Cannot resolve symbol` 爆红
- **复现**：照 3.1~3.3 年代的教程写拦截器配置，而 pom 里是 `mybatis-plus-boot-starter 3.5.17`
- **根因**：MP 3.4.0 重构插件体系（统一收口到 `MybatisPlusInterceptor`），老插件类 3.5.0 起直接从 jar 中删除；`PerformanceInterceptor` 官方推荐用 p6spy 替代，没有对应新类
- **修复**：分页改新写法：
  ```java
  @Bean
  public MybatisPlusInterceptor mybatisPlusInterceptor() {
      MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
      interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
      return interceptor;
  }
  ```
  注意 3.5.9 起 `PaginationInnerInterceptor` 被拆到独立依赖，且 **Java 8 项目要引 `mybatis-plus-jsqlparser-4.9`**（不带后缀的绑定 jsqlparser 5.x，要求 Java 17+），版本跟 boot-starter 保持一致；性能插件直接删除，开发期看 SQL 用 `mybatis-plus.configuration.log-impl=StdOutImpl`
- **教训**：依赖库的类"搜不到"≠ 单词拼错，先查版本演进（`@Deprecated` → 删除是常见节奏）；跟教程学习先对齐依赖版本，对不齐就以官方文档为准

## Spring 与配置

### 7. yml 双层 datasource 嵌套：启动报 url attribute is not specified
- **模块**：orm-mybatis-plus_demo
- **现象**：启动报 `Failed to configure a DataSource: 'url' attribute is not specified and no embedded datasource could be configured`
- **复现**：application.yml 手滑写成 `spring.datasource.datasource.url`（多嵌套一层），url/username/password 实际都写在 `spring.datasource.datasource.*` 下
- **根因**：yml 的层级结构就是 Spring 的属性绑定协议，多一层 = 属性全挂在错误路径下；Spring Boot 对不认识的属性**静默忽略**，不报"写错了"只报"没找到 url"
- **修复**：删掉多余的 `datasource:` 一层，属性左移
- **教训**：报"url 没配置"而明明写了时，先检查缩进层级；IDEA 的 yml 补全提示能提前暴露这种结构错误

### 8. No qualifying bean of type 'UserService'：实现类漏 @Service
- **模块**：orm-mybatis-plus_demo
- **现象**：`@SpringBootTest` 启动报 `No qualifying bean of type 'com.huai.service.UserService' available`；此前 IDEA 已有黄色警告 `Could not autowire. No beans of 'UserService' type found`
- **复现**：`UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService` 头上没有任何 Spring 注解，测试里 `@Autowired UserService`
- **根因**：Spring 只实例化"带注解的类"。Mapper 有 `@MapperScan` 按包批量登记所以不需要注解，Service 没有这种批量机制——漏 `@Service` 容器就不认识它，注入自然落空
- **修复**：给**实现类**加 `@Service`（`org.springframework.stereotype.Service`），注入时用接口类型即可匹配到实现
- **教训**：Mapper 和 Service 是两套"上户口"机制，别因为 Mapper 不用注解就以为 Service 也不用；IDEA 的 `Could not autowire` 警告很多时候是真的，别无视