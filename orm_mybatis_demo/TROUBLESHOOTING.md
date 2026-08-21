# 纠错记录（TROUBLESHOOTING）—— orm_mybatis_demo

> 用途：沉淀 orm_mybatis_demo 模块（Spring Boot 整合 MyBatis）排查过的报错与 bug，复盘用。
> 记录哪些条目由我决定，只追加不覆盖。
> 格式：现象 → 复现 → 根因 → 修复 → 教训。

## 整合配置与 Mapper 注册

### 1. 启动即报 Failed to configure a DataSource
- **现象**：运行 `MybatisSpringBootApplication` 直接启动失败：
  ```
  Failed to configure a DataSource: 'url' attribute is not specified
  and no embedded datasource could be configured.
  ```
- **复现**：application.yml 里只写了 `server.port` / `context-path`，直接启动
- **根因**：引入 `mybatis-spring-boot-starter`（连带 starter-jdbc）后，自动配置必须要一个 DataSource；starter 模式下数据源只从 application.yml 的 `spring.datasource` 读取，mybatis-config.xml 里的 `<environments>` 不参与。yml 没配 → 启动直接挂
- **修复**：application.yml 补四件套 + mapper 注册：
  ```yaml
  spring:
    datasource:
      url: jdbc:mysql://127.0.0.1:3306/furns_ssm?serverTimezone=Asia/Shanghai&useSSL=false
      username: root
      password: 123456
      driver-class-name: com.mysql.cj.jdbc.Driver

  mybatis:
    mapper-locations: classpath:mapper/*.xml
  ```
- **教训**：starter 模式数据源只认 application.yml；`<environments>` 是原生模式的产物，在这里是摆设

### 2. BindingException: Invalid bound statement (not found)
- **现象**：mapper 能正常注入，调用 `userMapper.getUserById(1L)` 时抛：
  ```
  org.apache.ibatis.binding.BindingException: Invalid bound statement (not found):
  com.huai.mapper.UserMapper.getUserById
  ```
- **复现**：数据源配好后启动成功，一调 mapper 方法就抛
- **根因**：userMapper.xml 的 namespace 写的是占位符 `namespace`。MyBatis 按 **namespace + SQL id** 绑定接口方法：namespace 必须等于接口全限定名、id 必须等于方法名，对不上就按"没有这条 SQL"处理
- **修复**：
  ```xml
  <mapper namespace="com.huai.mapper.UserMapper">
  ```
- **教训**：绑定三要素——namespace=接口全限定名、id=方法名、#{}=入参，一个都不能歪（详见 MAPPER-REGISTER-GUIDE.md 第三节）

### 3. mybatis-config.xml 整个文件不生效
- **现象**：在 mybatis-config.xml 里改数据源、注册 mapper，重启后完全没反应——文件根本没被加载
- **复现**：改动该文件里的任意配置（比如换数据库），观察启动行为不变
- **根因**：两层问题——
  1. starter 模式下，mybatis-config.xml 只有显式配置 `mybatis.config-location` 才会被加载，yml 里没配 → 死配置；就算引入了，`<environments>`（数据源）和 `<mappers>`（mapper 注册）在 starter 下也不生效
  2. `<mapper resource="mapper/userMapper"/>` 少了 `.xml` 后缀，真按原生方式加载会报 `Could not find resource mapper/userMapper`（实际文件是 `mapper/userMapper.xml`）
- **修复**：数据源、mapper 注册全部走 yml；mybatis-config.xml 删除，将来要复用时只放 settings / typeAliases / plugins 这类配置，再用 `config-location` 引入
- **教训**：原生 config 和 starter 是两套接线方式，混着写 = 大半不生效还添乱；resource 路径必须带 .xml、和 resources 下的相对路径逐字符一致

### 4. @Mapper 和 @Component 叠加（不报错但多余）
- **现象**：UserMapper 同时标了 `@Mapper` 和 `@Component`，运行无异常
- **复现**：看代码即可，无运行时症状
- **根因**：两套注册体系——`@Mapper` 是 mybatis-spring-boot-starter 的扫描标记，扫到后由 MyBatis 生成代理 bean；`@Component` 走 Spring 组件扫描，且 Spring 扫描默认跳过接口（非具体类），标了也白标
- **修复**：只留 `@Mapper`；或改用启动类 `@MapperScan("com.huai.mapper")` 批量扫，连 `@Mapper` 都不用
- **教训**：mapper 接口归 MyBatis 自己的注册体系管，别拿 Spring 的组件注解去叠

## 依赖注入

### 5. 请求接口报 NullPointerException: null
- **现象**：启动完全正常，请求 `/demo/api/getUserById/1` 返回 500，日志：
  ```
  java.lang.NullPointerException: null
  ```
  堆栈第一行指向 `UserServiceImpl.getUserbyId(UserServiceImpl.java:15)`——即 `userMapper.getUserById(id)` 这一行
- **复现**：service 里的 mapper 字段不加任何注入注解，启动后调任意走该字段的接口
- **根因**：`UserServiceImpl` 里 `private UserMapper userMapper;` 既没 `@Autowired` 也没构造器注入。Spring 用无参构造器创建 bean，字段保持默认值 null；DI 不是按类型自动塞，是**标注了哪里才注入哪里**。"字段为 null"本身合法，启动不报错，拖到第一次调用解引用才炸
- **修复**（二选一）：
  ```java
  // 方式一：字段注入
  @Autowired
  private UserMapper userMapper;

  // 方式二：构造器注入（推荐）
  private final UserMapper userMapper;
  public UserServiceImpl(UserMapper userMapper) {
      this.userMapper = userMapper;
  }
  ```
  构造器注入字段可 final，Spring 4.3+ 单构造器免 @Autowired，漏依赖在创建 bean 时就报错
- **教训**："启动正常、一调就 NPE" → 条件反射先查注入注解；看堆栈第一行定位解引用处

### 6. 注入 service 时声明了实现类而不是接口（不报错但反模式）
- **现象**：controller 里 `@Autowired private UserServiceImpl userServiceImpl;`，能正常跑
- **复现**：看代码可现，无运行时症状
- **根因**：把"注入"理解成了"实例化哪个类"——以为声明谁就是 new 谁。实际上容器里的 bean 自始至终是 `UserServiceImpl` 的实例（接口不可能被实例化），声明类型只决定变量怎么声明、`@Autowired` 按什么类型找 bean。声明成实现类 = 调用方绑死具体实现，抽的接口形同虚设
- **修复**：
  ```java
  @Autowired
  private IUserService userService;   // 声明接口，注入进来的仍是 UserServiceImpl 实例
  ```
- **教训**：**实例永远是实现类的，声明永远用接口类型**——面向接口编程，将来换实现类，调用方一行不用改

## Mapper XML 编写

### 7. 启动报 Error parsing Mapper XML（占位符括号笔误）
- **现象**：启动直接失败，满屏 `UnsatisfiedDependencyException` → `BeanCreationException` 层层套娃，最内层 Caused by：
  ```
  BuilderException: Error parsing Mapper XML. The XML location is '...target\classes\mapper\userMapper.xml'.
  Cause: Parsing error was found in mapping #{status), #{createTime}.
  Check syntax #{property|(expression), var1=value1, ...}
  Caused by: java.lang.StringIndexOutOfBoundsException
  ```
- **复现**：insert 语句里把 `#{status}` 手滑写成 `#{status)`（`}` 成了 `)`），重启
- **根因**：MyBatis 扫描占位符只认 `#{` 开、`}` 闭。`#{status)` 等不到自己的 `}`，一路吞过 `)` 和下一个 `#{`，直到 `#{createTime}` 的 `}` 才闭合——**两个占位符被并成一个畸形 token**（报错里俩字段"连体"就是这类笔误的签名特征）。XML 解析发生在启动建 SqlSessionFactory 时，所以语法错一律启动即炸；外层那串 bean 失败（sqlSessionFactory → userMapper → userServiceImpl → userController）全是连环断供的连带伤害
- **修复**：`#{status)` → `#{status}`。行尾分号实测 MySQL 单条语句能容忍（后续踩 insert 时验证过），但按规范建议删掉——mapper XML 里的 SQL 语句不写分号
- **教训**：Spring 套娃堆栈从**最内层 Caused by** 读起；两个占位符在报错里"连体" = 中间有个没闭合的 `#{`；这串报错看着吓人，真凶只有一个

### 8. insert 后拿不到自增 id（前端要新用户信息怎么办）
- **现象**：新增成功，但返回的 user 里 id 是 null；想再查一次拿完整数据，按 name 查又会撞同名
- **复现**：`<insert>` 不配回填属性，插入后读 `user.getId()`
- **根因**：mapper 的 insert 方法**返回值是影响行数**（1 = 插了一行），id 从来不走返回值；主键是数据库在 insert 那一刻分配的，MyBatis 默认插完扭头就走，不会去要
- **修复**：`<insert>` 加属性对，id 自动回填进参数对象：
  ```xml
  <insert id="insertUser" useGeneratedKeys="true" keyProperty="id">
  ```
  - `useGeneratedKeys="true"`：**开关**——insert 后向数据库回取自增主键（底层 JDBC 的 RETURN_GENERATED_KEYS + getGeneratedKeys()）
  - `keyProperty="id"`：**去向**——拿到后反射 `setId()` 塞回参数对象；填的是 Java 属性名，不是列名
  - `keyColumn`：表列名 ≠ 属性名时才必须写（列叫 user_id、属性叫 id 这类），MySQL 常可省略
  - `<selectKey>`：不靠数据库自增时（Oracle 序列 / 雪花 id）"先取号再插"的替代方案，MySQL 用不上，认脸即可

  时序：SQL 执行 → 库分配 id → MyBatis 要回 → setId 回填 → 方法返回。之后 controller 直接 `ApiResponse.success(user)`，无需二次查询
- **教训**：**insert 返回行数，id 走回填**；确要回读完整行（DB 默认值/触发器加工过的字段）也是拿回填的 id 去 selectById（按主键查），别按业务字段猜——name 会重名，主键不会

## Web 层参数绑定

### 9. JSON 传参，controller 形参对象字段全为 null
- **现象**：POST 发 JSON body（`{"name":"小花","email":"10087@qq.com",...}`），controller 里 `User user` 对象不是 null，但**所有字段都是 null**，insert 进库一行全空数据
- **复现**：`insertUser(User user)` 参数上不加 `@RequestBody`，Postman 以 raw JSON 调用
- **根因**：无注解的复杂类型参数按 @ModelAttribute 处理——Spring 先 new 一个空对象，再去**查询串/表单参数**里按字段名逐个绑定；而 JSON 数据在**请求体**里。体和串是两条互不相通的通道，一个字段都绑不上。`@RequestBody` 的作用就是切换通道：让 Jackson 反序列化请求体
- **修复**：参数加 `@RequestBody`，请求头 `Content-Type: application/json`。注意 `getUserList(User user)` 这类无注解绑定只认 `?name=xx` / form 格式——不是 bug，是通道不同，调用姿势要匹配
- **教训**：**体里的 JSON 必须 @RequestBody 接，串里的参数不用它接**；"对象不是 null 但字段全空" = 绑定通道错了，第一时间查参数注解

