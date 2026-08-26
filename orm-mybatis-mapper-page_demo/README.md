# orm-mybatis-mapper-page_demo — 通用 Mapper + PageHelper 分页查询

> 本模块演示 Spring Boot 集成 **PageHelper** 实现企业级分页查询（含搜索条件），并对照**手写 LIMIT 分页**，
> 看清"分页插件帮你干了什么"。分页 + 条件搜索是后台管理系统最高频的接口形态，没有之一。
>
> - 前置模块：`orm_mybatis_demo`（原生 MyBatis、XML mapper 写法）
> - 难度：⭐⭐
> - ⚠️ 本模块代码由 AI 生成、经运行验证，**建议按文末"动手作业"自己重写一遍**，看懂 ≠ 会写

---

## 🎯 学完这个模块，你应该能回答（先自测，再看下文）

1. 一个分页接口为什么需要**两条 SQL**？分别是什么？
2. `LIMIT` 的偏移量怎么算？查第 3 页、每页 10 条，LIMIT 长什么样？
3. `PageHelper.startPage()` 为什么必须**紧跟**目标查询？它靠什么机制生效？
4. count 查询为什么要和列表查询用**同一份** WHERE 条件？条件不一致会是什么现象？
5. 搜索条件为什么要封装成 Query 对象，而不是一个一个加参数？
6. XML 里 `<if>`、`<where>`、`<sql>` + `<include>` 各解决什么问题？
7. 搜索条件用 GET 还是 POST 传？各自的适用场景？
8. 为什么对外返回自定义 `PageResult` 而不是 PageHelper 的 `PageInfo`？

---

## 🚀 快速启动

```sh
# 前置：本地 MySQL 里有 furns_ssm 库和 orm_user 表（建表语句见文末附录）
# 改数据库账号密码：src/main/resources/application.yml

$ cd orm-mybatis-mapper-page_demo
$ mvn spring-boot:run
# 或 IDEA 直接运行 SpringBootDemoOrmMybatisApplication

# 验证（端口 10086，context-path /demo）
$ curl -X POST -H "Content-Type: application/json" \
    -d '{"name":"小","pageNum":1,"pageSize":2}' \
    http://localhost:10086/demo/api/user/page
```

`application.yml` 已开启 `logging.level.com.huai.mapper: debug`，
控制台会打印每条 SQL 原文和参数——**学习时重点看这里**，PageHelper 改写后的 SQL 一目了然。

---

## 📁 代码结构（建议阅读顺序）

| 文件 | 干什么 | 学习要点 |
|------|--------|----------|
| `pom.xml` | 依赖 | pagehelper-spring-boot-starter **1.4.7**（版本原因见"踩坑记录"） |
| `application.yml` | 配置 | pagehelper 的 `helper-dialect` / `reasonable` / `params` 各项含义 |
| `SpringBootDemoOrmMybatisApplication.java` | 启动类 | `@MapperScan`（tk.mybatis 的，见踩坑记录） |
| `controller/UserController.java` | 接口层 | 只做三件事：收参数 → 调 Service → 包统一响应 |
| `service/IUserService.java` + `impl/UserServiceImpl.java` | 业务层 | **分页逻辑核心在这里**，两种实现并排对照 |
| `mapper/UserMapper.java` | 数据层 | 接口方法 ↔ XML SQL 的绑定关系；`@Param` 的用途 |
| `resources/mapper/UserMapper.xml` | SQL | 动态 SQL 三件套 `<where>`/`<if>`/`<sql>`，LIMIT 写法 |
| `query/PageQuery.java` | 分页参数基类 | `pageNum`/`pageSize` + `getOffset()` 算偏移量 |
| `query/UserQuery.java` | 搜索条件 | 继承 PageQuery，条件按业务加 |
| `bean/PageResult.java` | 分页返回结构 | 对外契约，只暴露前端需要的 5 个字段 |
| `bean/ApiResponse.java` + `constant/Status.java` | 统一响应 | 和 orm_mybatis_demo 同款 |
| `entity/User.java` | 表映射 | 注意本模块 SQL 故意不查 password/salt 两列 |

---

## 📮 接口文档

### 1. PageHelper 分页（主推方式）

`POST /demo/api/user/page`，请求体 JSON：

```json
{
  "name": "小",
  "status": 1,
  "startTime": "2026-08-20",
  "endTime": "2026-08-31",
  "pageNum": 1,
  "pageSize": 10
}
```

字段全部可选，不传就是该维度不过滤：

| 字段 | 类型 | 匹配方式 | 说明 |
|------|------|----------|------|
| `name` | String | 模糊 `LIKE '%x%'` | 用户名 |
| `status` | Integer | 精确 `=` | 0 禁用 / 1 启用 |
| `startTime` / `endTime` | String(yyyy-MM-dd) | 范围 `>=` / `<` | 按 create_time 过滤 |
| `pageNum` | Integer | — | 页码，默认 1 |
| `pageSize` | Integer | — | 每页条数，默认 10，上限 100（Service 强制收口） |

响应：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "list": [ { "id": 4, "name": "小花", "email": "10087@qq.com", "...": "..." } ],
    "total": 2,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 1
  }
}
```

### 2. 手写 LIMIT 分页（对照用，看清插件做了什么）

`POST /demo/api/user/pageRaw`，请求体同上。功能一致，实现不同（见下节对照表）。

---

## 🧠 核心知识点

### 1. 分页的本质：两条 SQL

前端渲染一个分页条需要"当前页数据 + 总条数"，所以**任何分页方案最终都是两条 SQL**：

```sql
-- ① 查总数（算总页数用：total=23、pageSize=10 → pages=3，向上取整）
SELECT COUNT(*) FROM orm_user WHERE <搜索条件>;
-- ② 查当前页（跳过前 offset 条，取 pageSize 条）
SELECT ... FROM orm_user WHERE <搜索条件> ORDER BY create_time DESC LIMIT <offset>, <pageSize>;
```

偏移量公式：**`offset = (pageNum - 1) × pageSize`**（第 1 页跳 0 条，第 3 页每页 10 条 → LIMIT 20, 10）。

分页必须带 `ORDER BY`，否则每页顺序不稳定，翻页可能重复/漏数据。

### 2. 两种实现对照

| | 方式一：PageHelper | 方式二：手写 LIMIT |
|---|---|---|
| SQL | 不写任何分页语法 | 自己写 `LIMIT #{offset}, #{pageSize}` + count |
| Java | `PageHelper.startPage(page, size);` 紧跟查询 | 自己算 offset、自己查 count、自己组装 |
| count 忘带条件 | 不可能（自动从原 SQL 生成） | 很容易犯，total 和列表对不上 |
| 生产采用 | 主流 | 了解原理用；或无插件环境 |

### 3. PageHelper 的原理与最大的坑

`startPage` 把分页参数放进 **ThreadLocal**，对**紧随其后第一条** MyBatis 查询生效：
自动改写成 `... LIMIT ?,?` 并追加 `SELECT count(0) ...`（条件自动带上）。

**坑**：两行必须紧挨着。中间插入其它 SQL → 分页错打到那条 SQL 上；
startPage 之后没执行查询就 return → ThreadLocal 残留，同一线程后续不相关查询被莫名分页。
（Tomcat 线程复用，这是 PageHelper 最经典的生产事故来源。）

`reasonable: true` 的效果：页码超过最大页时回退查最后一页，而不是返回空。

### 4. Mapper 接口与 XML 的绑定

- `namespace` = 接口全限定名，`id` = 方法名 → MyBatis 靠这个把方法调用绑定到 SQL
- 方法找不到对应 SQL → `Invalid bound statement (not found)`（本模块踩过，见下）
- `#{}` 是**预编译占位符**（防 SQL 注入，值会转义）；`${}` 是字符串拼接，仅限表名/排序字段等白名单场景
- 多参数用 `@Param("offset")` 起名字，XML 里 `#{offset}` 引用；单对象参数不用注解，直接 `#{属性名}`
- **`#{offset}` 取的是 `PageQuery.getOffset()`**——getter 算出来的值也能引用，Service 不用单独传 offset

### 5. 搜索条件：Query 对象 + 动态 SQL

- 参数超过三四个就封装成对象：`PageQuery`（分页基类，全模块复用）← `UserQuery`（业务条件）
- GET 参数 / JSON body 都能按字段名自动绑定到对象上，条件从 3 个涨到 10 个不用改接口签名
- XML 动态 SQL 三件套：
  - `<if test="name != null and name != ''">`：参数传了才拼这段 SQL
  - `<where>`：自动处理开头多余的 AND
  - `<sql id="queryCondition">` + `<include refid>`：条件片段复用，**列表查询和 count 引用同一份**，杜绝 total 对不上
- 日期字段两个注解各管一边：`@DateTimeFormat`（GET/form 绑定）、`@JsonFormat`（POST JSON 解析）

### 6. GET 还是 POST 传搜索条件？

- "POST 就藏起来了"是误解：body 在 F12/抓包里一样看得见，POST ≠ 加密
- GET 的真实优势：可收藏/分享链接、浏览器后退友好、可缓存、符合查询语义
- POST(JSON) 的真实优势：条件不进 URL/浏览器历史/网关 access log（含姓名手机号等个人信息时重要）；
  支持数组、嵌套等复杂条件（URL 有长度上限）
- 结论：查询语义两种都行，**团队统一 + 视条件敏感度/复杂度选择**

### 7. 对外契约：PageResult 与字段最小暴露

- 不直接返回 `PageInfo`（第三方类，插件升级会牵动接口结构），转成自定义 `PageResult`，只留 5 个字段
- Entity 是表的形状，接口是契约的形状，两者分开；本模块 SQL 层就不查 password/salt，
  更彻底的做法是返回 `UserVO`（进阶练习）

---

## ⚠️ 踩坑记录（本模块开发过程实录，每条都实际撞过）

1. **pagehelper-spring-boot-starter 1.2.9 + Spring Boot 2.6+ 启动失败**
   `PageHelperAutoConfiguration` 循环引用（Boot 2.6 起默认禁止循环依赖）→ 升级 **1.4.7** 解决
2. **`Invalid bound statement (not found): com.huai.mapper.UserMapper.selectAll`**
   接口方法没绑定到 SQL。本模块根因是依赖版本冲突（mybatis 3.4.6 vs tk.mybatis 需要的 3.5.x），
   后改为手写 XML + `@Mapper` 绕开。排查思路：先确认 XML 的 namespace/id 和接口对不对，再查依赖树
3. **父 pom `dependencyManagement` 钉了 mybatis-spring-boot-starter 1.3.2**
   会把 pagehelper 传递进来的版本也拖到 1.3.2 → `mvn dependency:tree` 是看依赖冲突的第一工具
4. **Windows 终端 GBK 编码，curl 发中文参数变乱码**
   日志 `Parameters: ??(String)`、搜索结果 0 条，但代码没问题。Postman/前端 UTF-8 不受影响；
   curl 测中文用 `\uXXXX` 转义
5. **JUnit 3 风格的 AppTest 编译失败**
   Boot 2.6 的 starter-test 只带 JUnit 5，Archetype 生成的 junit.framework 风格测试需要显式引入 junit 4

---

## 📎 附录：orm_user 建表语句

> 本地表已存在（数据来自 xkcoding 教程），以下按 Entity 字段重建，供从零重写时使用：

```sql
CREATE TABLE `orm_user` (
  `id`              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`            VARCHAR(32)  NOT NULL COMMENT '用户名',
  `password`        VARCHAR(64)  NOT NULL COMMENT '加密后的密码',
  `salt`            VARCHAR(64)           DEFAULT NULL COMMENT '加密使用的盐',
  `email`           VARCHAR(64)           DEFAULT NULL COMMENT '邮箱',
  `phone_number`    VARCHAR(32)           DEFAULT NULL COMMENT '手机号码',
  `status`          TINYINT      NOT NULL DEFAULT 1 COMMENT '-1：逻辑删除，0：禁用，1：启用',
  `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_login_time` DATETIME              DEFAULT NULL COMMENT '上次登录时间',
  `last_update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '上次更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 造点数据方便翻页验证
INSERT INTO orm_user (name, password, email, phone_number, status, create_time) VALUES
('user_1', 'x', 'user1@xkcoding.com', '17300000001', 1, '2026-08-17 06:04:24'),
('user_2', 'x', 'user2@xkcoding.com', '17300000002', 1, '2026-08-17 06:04:24'),
('小明',   'x', '10086@qq.com',       '10086',        1, '2026-08-19 05:44:26'),
('小花',   'x', '10087@qq.com',       '10087',        1, '2026-08-21 08:27:36');
```

---

## 🎯 动手作业：自己重写一遍（把"看懂"变成"会写"）

新建一个空模块（如 `orm-mybatis-page_homework`），**不看不抄本模块代码**，按下面清单实现：

1. 建 Module + pom：spring-boot-starter-web、pagehelper-spring-boot-starter、mysql、lombok
2. application.yml：数据源、`mybatis.mapper-locations`、`map-underscore-to-camel-case`、pagehelper 三项配置、SQL 日志
3. 建 `orm_user` 表（用上面 DDL）+ User 实体
4. 只用**手写 LIMIT** 方式实现 `POST /api/user/page`：XML 里 count + LIMIT 两条 SQL，自己算 offset
5. 用 curl 验证：无条件查全部 → 加 name 条件 → 翻第 2 页 → 观察 SQL 日志里 LIMIT 的参数变化
6. 改造成 **PageHelper** 方式：删掉手写的 LIMIT 和 count，`startPage` + 查询两行搞定
7. 加搜索条件：Query 对象 + `<where>/<if>/<sql>` 动态 SQL，日期范围也支持
8. 最后加 `PageResult` 统一返回结构

**验收标准**（全过才算会）：

- [ ] 能不看资料说出 offset 公式和两条 SQL 各自的作用
- [ ] `{"name":"小","pageNum":1,"pageSize":1}` 返回 `total=2, pages=2`，且第一页只有 1 条
- [ ] SQL 日志里能指出哪条 count 是 PageHelper 自动生成的
- [ ] 把 startPage 和查询中间插一行别的查询，复现"分页错打"现象，再改回来（踩一次坑胜过看十篇文章）

卡住了再来问——问的时候先说"我预期 X，实际 Y，我试过 Z"。
