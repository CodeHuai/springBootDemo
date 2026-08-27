# spring-boot-demo（个人学习版）

> 本工程用于个人学习 Spring Boot，采用 **多模块范式**：一个父 pom 做聚合与依赖版本管理，**每个知识点一个可独立运行的子模块**，按需分层、互不依赖。
>
> 学习路线与模块组织参考开源项目 [xkcoding/spring-boot-demo](https://github.com/xkcoding/spring-boot-demo)，但使用个人命名空间 `com.huai`，不逐文件对照、不借用其包名。

## 与参考项目的差异

| 项 | 本工程 | xkcoding/spring-boot-demo |
| --- | --- | --- |
| Spring Boot | 2.6.13 | 2.1.0.RELEASE |
| groupId / 包名 | `com.huai` | `com.xkcoding` |
| 模块命名 | `xxx_demo`（如 `helloworld_demo`） | `demo-xxx`（如 `demo-helloworld`） |
| 已建模块 | 8 个（逐步增加中） | 55+ |

> 版本说明：保留较新的 2.6.13 以规避老版本安全问题；命名空间与模块命名按个人习惯调整，知识点本身仍参照原项目学习。

## 开发环境

- **JDK 1.8+**
- **Maven 3.5+**
- **IntelliJ IDEA**（务必安装 Lombok 插件）
- MySQL 5.7+、Redis（学到对应模块时再装）

## 目录结构

```
springBootDemo/                 ← 父工程（packaging=pom，不含代码）
├── pom.xml                     ← 聚合 + dependencyManagement 统一版本
├── README.md                   ← 本文件
├── 新手阅读顺序.md               ← 学习路线（必看）
├── TROUBLESHOOTING.md          ← 踩坑记录：排查过的报错按「模块→现象→根因→修复」沉淀
├── helloworld_demo/            ← 以下每个目录一个知识点，可独立运行
├── properties_demo/
├── logback_demo/
├── exceptionHandle_demo/
├── orm_JDBCTemplate_demo/
├── orm_mybatis_demo/
├── orm-mybatis-mapper-page_demo/
└── orm-mybatis-plus_demo/
```

## 如何运行一个模块

```sh
# 在对应模块目录下
$ cd helloworld_demo
$ mvn spring-boot:run
```

启动后访问 http://localhost:10086/demo/api/hello

或在 IDEA 中找到各模块的启动类（如 `HelloworldDemoApplication`）直接运行。

> ORM 系列模块（orm_*）的验证方式以单元测试为主：在模块目录下 `mvn test`，或在 IDEA 中右键运行测试类（如 `orm-mybatis-plus_demo` 的 `UserTest`）。运行前确认各模块 `application.yml` 里的数据库连接指向本机。

## 如何新增一个模块（学习新知识点时）

1. 打开 [新手阅读顺序.md](./新手阅读顺序.md)，按阶段顺序学习。
2. 每学一个知识点，参照原项目 `xkcoding/spring-boot-demo` 的对应模块源码（GitHub：https://github.com/xkcoding/spring-boot-demo ，或本地 `D:\PersonCodeDir\code\java\spring-boot-demo\<模块名>`），在本工程新建模块：
   - 复制 `helloworld_demo` 作为模板，改 `artifactId`、包名（`com.huai.<主题>`）、启动类名
   - 在根 `pom.xml` 的 `<modules>` 中加上新模块
   - IDEA 中 Maven Reload 即可
3. 阅读每个模块的通用套路：`pom.xml → application.yml → 启动类 → config → controller → service → dao → model → README`

## 已建模块

### 基础入门

| 模块 | 简介 |
| --- | --- |
| [helloworld_demo](./helloworld_demo) ✅ | 第一个 Spring Boot 应用：启动类 + REST 接口返回 JSON |
| [properties_demo](./properties_demo) ✅ | 配置文件读取：`@Value` 与 `@ConfigurationProperties`（ApplicationProperty / DeveloperProperty 两组配置的绑定） |

### 日志与异常

| 模块 | 简介 |
| --- | --- |
| [logback_demo](./logback_demo) ✅ | logback 日志：控制台 + 文件双路输出，文件按日期与大小滚动拆分 |
| [exceptionHandle_demo](./exceptionHandle_demo) ✅ | 统一异常处理：API 接口异常封装统一返回格式（ApiResponse），页面请求异常统一跳错误页（`@ControllerAdvice`） |

### ORM 演进（同一张 `orm_user` 表的四代实现，建议按序对照学习）

| 模块 | 简介 |
| --- | --- |
| [orm_JDBCTemplate_demo](./orm_JDBCTemplate_demo) ✅ | 第一代：JdbcTemplate 手写 SQL，自制 `@Table`/`@Column`/`@PK` 注解封装通用 Dao 层 |
| [orm_mybatis_demo](./orm_mybatis_demo) ✅ | 第二代：原生 MyBatis（mybatis-spring-boot-starter），Mapper 接口 + XML 完整 CRUD |
| [orm-mybatis-mapper-page_demo](./orm-mybatis-mapper-page_demo) ✅ | 第三代：MyBatis + 通用 Mapper（免写单表 CRUD）+ PageHelper 物理分页 |
| [orm-mybatis-plus_demo](./orm-mybatis-plus_demo) 🚧 | 第四代：MyBatis-Plus——BaseMapper/IService 免 SQL CRUD、`MetaObjectHandler` 公共字段自动填充、分页插件；学习进行中 |

## 参考项目

- GitHub：https://github.com/xkcoding/spring-boot-demo
- 本地路径：`D:\PersonCodeDir\code\java\spring-boot-demo`（如有）