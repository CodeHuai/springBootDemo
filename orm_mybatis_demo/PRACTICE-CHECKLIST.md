# 练习清单（PRACTICE CHECKLIST）

> 目标：把 orm_mybatis_demo 从"只会 getUserById"补成 完整 CRUD + 动态 SQL + 事务 + 统一响应/全局异常。
> 用法：每完成一项打勾；踩到的坑按 TROUBLESHOOTING.md 的格式记录（记不记自己定）。
> 规则：代码自己写，写完找 review；提示只指路不剧透，卡住先自查 15 分钟再问。

## 阶段〇：开工收尾（上轮遗留）

- [ ] controller 注入从 `UserServiceImpl` 改为 `IUserService`（原因见 TROUBLESHOOTING 条目 6）
- [ ] `getUserbyId` → `getUserById`（接口、实现、调用三处一起改）

## 阶段一：MyBatis 补课

### 1. 摸底：数据库列名风格
- [ ] 执行 `DESC furns_ssm.orm_user;`，记下列名是驼峰还是下划线

> 这一步决定第 6 步会不会踩"查出来字段是 null"的坑。如果是下划线风格——
> 现在 getUserById 返回的 JSON 里 `phoneNumber`、`createTime` 等字段恐怕已经是 null 了，先请求一次验证。

### 2. insert：新增用户
- [ ] UserMapper 加方法 + XML 写 insert（`#{}` 直接取对象属性，如 `#{name}`）
- [ ] 新增后把数据库生成的自增 id 回填到 user 对象

- 验收：POST 新增，返回体里 id 有值
- 坑预告：光写 insert，id 回不来——得告诉 MyBatis"把主键塞回来"

### 3. updateById：按 id 更新
- [ ] 更新指定字段（如 email、lastLoginTime）

- 验收：更新后重新查询，字段已变化

### 4. deleteById：逻辑删除
- [ ] 不许真 `DELETE`，改成 `UPDATE status = -1`（含义见 User.status 注释）

- 验收：删除后 getUserById 仍能查到，且 status = -1
- 思考：逻辑删除的数据，第 6 步的列表查询要不要排除？怎么排除？

### 5. 多参数查询
- [ ] 写 `getUserByNameAndEmail(String name, String email)`

- 坑预告：两个及以上参数有个几乎必踩的报错，和"参数名"有关，报错时逐字读

### 6. 查列表 + 条件查询（动态 SQL）
- [ ] `selectAll` 返回 `List<User>`
- [ ] 条件查询：name / email 可能传也可能不传 → `<where>` + `<if>`

- 暗雷：若第 1 步查到列名是下划线风格，列表里部分字段会一直是 null——
  去学 `mapUnderscoreToCamelCase` 或 `resultMap`，两个方案都要知道区别

### 7. 批量删除
- [ ] `deleteByIds(List<Long> ids)`，`<foreach>` 拼 `IN (...)`（也是逻辑删除）

- 坑预告：括号、逗号、separator 三者怎么配合不炸

### 8. 事务
- [ ] 写一个"双写"方法：先 insert 一条，再故意制造异常（如 `1/0`）
- [ ] 第一次**不加** `@Transactional` 跑：亲眼看"写了一半"的脏数据
- [ ] 加上 `@Transactional` 再跑：验证回滚

- 思考：`@Transactional` 加在 controller 还是 service？为什么？

## 阶段二：移植 exceptionHandle_demo 的逻辑

原模块资产四件：`Status` 枚举、`BaseException` + 子类、`ApiResponse` 统一返回体、
`@ControllerAdvice` 全局处理器。逐个搬进本模块，边搬边问自己每行为什么存在。

- [ ] `Status`：搬过来并扩充业务码，如 `USER_NOT_FOUND(1001, "用户不存在")`
- [ ] `BaseException` + 子类：子类别再叫 `JsonException`，按业务语义命名，如 `UserNotFoundException`
- [ ] `ApiResponse`：code / message / data 三件套 + 静态工厂方法
- [ ] 全局处理器：`@ControllerAdvice` + `@ExceptionHandler`；
      建议比原模块多加一个 `@ExceptionHandler(Exception.class)` 兜底——日志记全，对外只吐 ApiResponse
- [ ] controller 全部接口改为返回 `ApiResponse`
- [ ] getUserById 查不到 → service 抛 `UserNotFoundException` → 全局处理器兜住

- 验收：请求一个不存在的 id，返回 `{"code":1001,"message":"用户不存在","data":null}`，
  而不是 null、也不是 500 堆栈页
- 思考：为什么用 `@ControllerAdvice` 集中处理，而不是每个方法里 try-catch？

## 阶段三：可观测性（日志三件套）

需求场景：前端调一次接口，日志里要能看清——调了哪个接口、参数是什么、执行了什么 SQL、报错炸在哪。

> 前置认知：logback_demo 解决的是"日志往哪打、什么格式"（管道）；
> 本阶段解决"谁在哪个环节打什么"（水源）。管道第 3 步从 logback_demo 搬。

### 1. 零代码层：yml 开两路日志
- [ ] `logging.level.com.huai.mapper: debug` → SQL 三连：`==> Preparing` / `==> Parameters` / `<== Total`
- [ ] 注册 `CommonsRequestLoggingFilter`（@Bean + 开它的 debug 级别）→ 每个请求打出方法 + URI + 参数

- 验收：请求一次 getUserById，控制台同时出现请求行和 SQL 三行

### 2. 接口日志：自定义 HandlerInterceptor
- [ ] 写 WebLogInterceptor：`preHandle` 记开始时间、`MDC.put("traceId", 短随机串)`
- [ ] `afterCompletion` 打 URI、耗时 ms、状态码，并 `MDC.remove("traceId")`
- [ ] 实现 `WebMvcConfigurer` 注册拦截器
- [ ] logback pattern 加 `%X{traceId}`，观察同一次请求的接口日志和 SQL 日志带同一个 id

- 思考：为什么 `afterCompletion` 里必须 remove？不删的话，线程池复用线程会发生什么？

### 3. 搬管道：logback-spring.xml
- [ ] 把 logback_demo 的 `logback-spring.xml` 搬进本模块 resources，按需裁剪（error/info 分文件保留）
- [ ] pattern 追加 `%X{traceId}`

### 4. 报错可见（与阶段二联动）
- [ ] 全局处理器里 `log.error("接口异常", e)`——第二个参数传**异常对象本身**，
      别写 `log.error(e.getMessage())`（那样堆栈全丢，等于白记）

- 验收：人为制造一个异常 → 日志文件 error.log 里有完整堆栈 + traceId，
  控制台同 traceId 的请求行/SQL 行都在，前端只收到统一错误 JSON

## 完成标准

全部勾完时，这个模块 = 完整 CRUD + 动态 SQL + 事务 + 统一响应 + 全局异常 + 可观测日志——
一个标准三层架构后端骨架成型，可以着手下一个模块了。
