# helloworld_demo

Spring Boot 入门模块，演示最简启动类与一个 Hello 接口。

## 运行

```sh
$ cd helloworld_demo
$ mvn spring-boot:run
```

启动后访问 http://localhost:10086/demo/api/hello

## 接口清单

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/demo/api/hello` | 返回 `Hello, World!`（不传 `who` 时默认 World） |
| GET | `/demo/api/hello?who=张三` | 返回 `Hello, 张三!` |
| GET | `/demo/api/hello?who=` | 传空串，同样回退为 `Hello, World!` |
| GET | `/demo/api/search?keyword=java&page=2&sort=desc` | 多参数示例，返回 `搜索「java」，第 2 页，排序：desc` |

## 关键文件

- `pom.xml`
- `src/main/java/com/huai/helloworld_demo/HelloworldDemoApplication.java` —— 启动类
- `src/main/java/com/huai/helloworld_demo/controller/Helloworld.java` —— Hello 接口
- `src/main/resources/application.yml` —— 端口 `10086`、context-path `/demo`