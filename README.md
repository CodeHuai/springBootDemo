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
| 已建模块 | `helloworld_demo`（逐步增加中） | 55+ |

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
└── helloworld_demo/            ← 子模块：每个知识点一个
    ├── pom.xml
    └── src/main/...
```

## 如何运行一个模块

```sh
# 在对应模块目录下
$ cd helloworld_demo
$ mvn spring-boot:run
```

启动后访问 http://localhost:10086/demo/api/hello

或在 IDEA 中找到各模块的启动类（如 `HelloworldDemoApplication`）直接运行。

## 如何新增一个模块（学习新知识点时）

1. 打开 [新手阅读顺序.md](./新手阅读顺序.md)，按阶段顺序学习。
2. 每学一个知识点，参照原项目 `xkcoding/spring-boot-demo` 的对应模块源码（GitHub：https://github.com/xkcoding/spring-boot-demo ，或本地 `D:\PersonCodeDir\code\java\spring-boot-demo\<模块名>`），在本工程新建模块：
   - 复制 `helloworld_demo` 作为模板，改 `artifactId`、包名（`com.huai.<主题>`）、启动类名
   - 在根 `pom.xml` 的 `<modules>` 中加上新模块
   - IDEA 中 Maven Reload 即可
3. 阅读每个模块的通用套路：`pom.xml → application.yml → 启动类 → config → controller → service → dao → model → README`

## 已建模块

| 模块 | 简介 |
| --- | --- |
| [helloworld_demo](./helloworld_demo) ✅ | spring-boot 的一个 helloworld，入门 |

## 参考项目

- GitHub：https://github.com/xkcoding/spring-boot-demo
- 本地路径：`D:\PersonCodeDir\code\java\spring-boot-demo`（如有）