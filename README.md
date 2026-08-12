# spring-boot-demo（个人学习版）

> 本工程用于个人学习 Spring Boot，**架构对齐开源项目 [xkcoding/spring-boot-demo](https://github.com/xkcoding/spring-boot-demo)**。
>
> 采用与之相同的多模块范式：一个父 pom 做聚合与依赖版本管理，**每个知识点一个可独立运行的子模块**，按需分层、互不依赖。这样每学一个知识点，就在本工程里建一个同构模块，与原项目逐文件对照学习。

## 与原项目的差异

| 项 | 本工程 | 原项目 xkcoding |
| --- | --- | --- |
| Spring Boot | 2.6.13 | 2.1.0.RELEASE |
| groupId / 包名 | com.xkcoding（保持一致，便于逐文件对照） | com.xkcoding |
| 已建模块 | demo-helloworld（逐步增加中） | 55+ |

> 版本说明：保留较新的 2.6.13 以规避老版本安全问题；架构与原项目一致，个别 API（如 Spring Security）在 2.6 下可能略有差异，学到具体模块时再处理。

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
└── demo-helloworld/            ← 子模块：每个知识点一个
    ├── pom.xml
    ├── README.md
    └── src/main/...
```

## 如何运行一个模块

```sh
# 在对应模块目录下
$ cd demo-helloworld
$ mvn spring-boot:run
```

或在 IDEA 中找到各模块的 `SpringBootDemoXxxApplication` 启动类直接运行。

## 如何新增一个模块（学习新知识点时）

1. 打开 [新手阅读顺序.md](./新手阅读顺序.md)，按阶段顺序学习。
2. 每学一个知识点，参照原项目 `D:\PersonCodeDir\code\java\spring-boot-demo\demo-xxx`，在本工程新建同名模块：
   - 复制 `demo-helloworld` 作为模板，改 `artifactId`、包名（`com.xkcoding.<主题>`，模块名横线转点）、启动类名（`SpringBootDemoXxxApplication`）
   - 在根 `pom.xml` 的 `<modules>` 中加上新模块
   - IDEA 中 Maven Reload 即可
3. 阅读每个模块的通用套路：`pom.xml → application.yml → 启动类 → config → controller → service → dao → model → README`

## 已建模块

| 模块 | 简介 |
| --- | --- |
| [demo-helloworld](./demo-helloworld) ✅ | spring-boot 的一个 helloworld，入门 |

## 原项目参考

- 学习项目本地路径：`D:\PersonCodeDir\code\java\spring-boot-demo`
- GitHub：https://github.com/xkcoding/spring-boot-demo
