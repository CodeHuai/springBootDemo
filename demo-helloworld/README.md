# demo-helloworld

对应学习项目 `xkcoding/spring-boot-demo/demo-helloworld`，演示如何用 Spring Boot 写一个 Hello World。

## 运行

```sh
$ mvn spring-boot:run
```

启动后访问 http://localhost:8080/demo/hello

## 接口清单

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/demo/hello?who=xxx` | 启动类内达示例，返回 `Hello, {who}!`（who 为空时默认 World） |
| GET | `/demo/api/hello` | 返回字符串 |
| GET | `/demo/api/users/{id}` | 路径变量示例 |
| GET | `/demo/api/search?keyword=xx&page=1` | 请求参数示例 |
| POST | `/demo/api/users` | 请求体（@RequestBody）示例 |

## 关键文件

- `pom.xml`
- `src/main/java/com/xkcoding/helloworld/SpringBootDemoHelloworldApplication.java` —— 启动类（内嵌 `/hello`）
- `src/main/java/com/xkcoding/helloworld/controller/HelloController.java` —— MVC 示例接口
- `src/main/resources/application.yml`
