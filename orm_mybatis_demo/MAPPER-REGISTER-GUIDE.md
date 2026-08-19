# MyBatis Mapper 注册方式指南

> Mapper = 接口(定义方法) + SQL(XML 或注解)。**注册**就是告诉 MyBatis"这对组合存在"——
> 接口负责让 Java 调用，注册负责把接口和 SQL 绑在一起。不注册(或注册错)，调用时就是
> MyBatis 出现率第一名的报错：`BindingException: Invalid bound statement (not found)`。

---

## 一、原生 MyBatis（mybatis-config.xml）的四种方式

注册位置：`mybatis-config.xml` 的 `<mappers>` 标签内。

### 1. resource —— 按 XML 文件路径点名（最常用）

```xml
<mappers>
    <mapper resource="mapper/UserMapper.xml"/>
    <mapper resource="mapper/OrderMapper.xml"/>
</mappers>
```

- 值是 **classpath 资源路径**：斜杠分隔、以 `.xml` 结尾，对应 `src/main/resources` 下的相对路径
- 一个标签注册**一个**文件，加一个 mapper 加一行——精确但啰嗦
- ❌ 常见错误：写成类名 `com.huai.mapper.UserMapper`（点分隔、无 .xml）——那是 `class` 属性的格式

### 2. url —— 按文件系统绝对路径点名（几乎不用）

```xml
<mapper url="file:///D:/code/mapper/UserMapper.xml"/>
```

SQL 文件不在项目里、在本机磁盘上时才用。团队协作时路径不可移植，了解即可。

### 3. class —— 按接口全限定类名注册

```xml
<mappers>
    <mapper class="com.huai.mapper.UserMapper"/>
</mappers>
```

- 值是**接口的全限定类名**（点分隔，无后缀）
- 配套两种 SQL 写法：
  - **注解版**：SQL 直接写在接口方法上

    ```java
    public interface UserMapper {
        @Select("SELECT * FROM user WHERE id = #{id}")
        User selectById(Long id);
    }
    ```
  - **XML 版**：`UserMapper.xml` 必须和接口**同包同名**地放在 classpath 里
    （即 `src/main/resources/com/huai/mapper/UserMapper.xml`，目录结构和包名一致）
- 注意：接口是接口，XML 是 XML，`class` 注册的是**接口**，XML 是顺带按约定找到的

### 4. package —— 按包批量扫描（真正意义的"扫描"）

```xml
<mappers>
    <package name="com.huai.mapper"/>
</mappers>
```

- 把 `com.huai.mapper` 包下**所有接口**一网打尽，新增 mapper 不用改配置
- 对 SQL 的要求和 `class` 一样：注解版，或 XML 与接口同包同名
- ❌ 如果 XML 放在 `resources/mapper/` 而接口在 `java/com/huai/mapper/`，包扫描找不到 XML —— 这就是为什么原生方式下常把 XML 和接口放同包

### 四种方式对比

| 方式 | 写什么 | SQL 在哪 | 适用 |
|---|---|---|---|
| resource | XML 路径 | XML 随便放，路径写对即可 | 最通用，XML 位置自由 |
| url | 文件绝对路径 | 本机磁盘 | 基本不用 |
| class | 接口类名 | 注解 / 同包同名 XML | 注解党、单个接口 |
| package | 包名 | 注解 / 同包同名 XML | 接口多、想省事 |

---

## 二、Spring Boot + mybatis-spring-boot-starter 的方式

> 前提：引入 `mybatis-spring-boot-starter` 依赖。此模式下 **mybatis-config.xml 里的 `<environments>`（数据源）会被忽略**——starter 从 Spring 的 DataSource bean 接线，数据源必须写在 application.yml。

### 1. @Mapper —— 逐个标注（小项目够用）

```java
@Mapper
public interface UserMapper {
    User selectById(Long id);
}
```

- 每个 mapper 接口标一个；不标的那个调用时报 BindingException
- 不要再叠 `@Component`——MyBatis 接口走自己的注册体系，`@Mapper` 已经够了

### 2. @MapperScan —— 按包批量扫描（主流做法）

```java
@SpringBootApplication
@MapperScan("com.huai.mapper")          // 也可写 {"包1", "包2"}
public class MybatisSpringBootApplication {
    public static void main(String[] args) {
        SpringApplication.run(MybatisSpringBootApplication.class, args);
    }
}
```

- 包下所有接口自动注册，**接口上连 @Mapper 都不用标**
- 二选一即可：@MapperScan 已覆盖的包里再标 @Mapper 不会冲突，但没必要混用

### 3. XML 的位置：application.yml 的 mapper-locations

Boot 模式下 XML 不再在 config 里注册，而是 yml 一行通配：

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/你的库名?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false
    username: root
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver

mybatis:
  mapper-locations: classpath:mapper/*.xml     # XML 统一放 resources/mapper/ 下
  # config-location: classpath:mybatis-config.xml   # 可选：想复用原生 config 时才配
  type-aliases-package: com.huai.entity        # 可选：实体包别名，XML 里不用写全限定名
```

通配符说明：
- `classpath:mapper/*.xml` —— 本模块 resources/mapper/ 下所有 XML
- `classpath*:mapper/**/*.xml` —— `classpath*` 会连依赖 jar 里的也扫进来，`**` 跨多级目录

### 4. 想复用原生 mybatis-config.xml？

只能放 `settings`、`typeAliases`、`plugins` 这类配置，用 `mybatis.config-location` 引入；
`<environments>` 和里面的 `<mappers>` 在 starter 模式下**不生效**，mapper 注册以
@Mapper/@MapperScan + mapper-locations 为准。

---

## 三、接口和 XML 是怎么绑上的（原理一分钟）

一对最小可用组合：

**接口** `src/main/java/com/huai/mapper/UserMapper.java`
```java
@Mapper
public interface UserMapper {
    User selectById(Long id);
}
```

**XML** `src/main/resources/mapper/UserMapper.xml`
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.huai.mapper.UserMapper">          <!-- ① 必须等于接口全限定名 -->

    <select id="selectById" resultType="com.huai.entity.User">   <!-- ② 必须等于方法名 -->
        SELECT * FROM user WHERE id = #{id}               <!-- ③ #{参数名} 对应方法入参 -->
    </select>

</mapper>
```

绑定三要素：**namespace = 接口全限定名、SQL id = 方法名、#{} = 方法参数**。
调用 `userMapper.selectById(1L)` 时，MyBatis 按 `namespace + id` 找到 SQL，填参、执行、
按 `resultType` 把每行装进对象（干的就是手搓 ORM 里 buildRowMapper 干的活）。
任何一环对不上 → BindingException。

---

## 四、报错速查

| 报错 | 病因 |
|---|---|
| `BindingException: Invalid bound statement (not found)` | mapper 没注册 / mapper-locations 没配或路径写错 / namespace ≠ 接口全限定名 / SQL id ≠ 方法名 |
| `IllegalArgumentException: Mapped Statements collection does not contain value ...` | 同上家族，按 id 找不到 SQL |
| `Could not find resource xxx.xml` | resource 路径写错（斜杠、大小写、.xml 后缀） |
| 启动报 `Invalid bound statement` 但 SQL 明明在 | XML 编译后不在 classpath（放错目录 / maven 资源过滤没带上） |

---

## 五、选型结论（对应本模块）

本模块依赖是 `mybatis-spring-boot-starter`，推荐配置：

1. 接口：`com.huai.mapper` 下建，**不标 @Mapper**，启动类加 **@MapperScan("com.huai.mapper")**
2. XML：统一放 `src/main/resources/mapper/`，命名与接口同名
3. yml：`mybatis.mapper-locations: classpath:mapper/*.xml` + `spring.datasource` 四件套
4. mybatis-config.xml 里的 `<environments>` 删除（不生效还添乱）；整个文件暂时可以不要

> 一句话记忆：**Boot 模式下，"扫描"分两半——接口归 @MapperScan，XML 归 mapper-locations；
> 原生模式下才有 resource/class/package 那一套。**