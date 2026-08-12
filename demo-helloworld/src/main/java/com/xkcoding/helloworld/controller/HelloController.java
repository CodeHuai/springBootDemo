package com.xkcoding.helloworld.controller;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 示例Controller，演示Spring MVC的基本功能
 */
@RestController
@RequestMapping("/api")
public class HelloController {

    /**
     * 简单的GET请求示例
     */
    @GetMapping("/hello")
    public String hello() {
        return "Hello, Spring MVC!";
    }

    /**
     * 路径变量示例
     */
    @GetMapping("/users/{id}")
    public Map<String, Object> getUser(@PathVariable Long id) {
        Map<String, Object> user = new HashMap<>();
        user.put("id", id);
        user.put("name", "用户" + id);
        user.put("email", "user" + id + "@example.com");
        return user;
    }

    /**
     * 请求参数示例
     */
    @GetMapping("/search")
    public Map<String, Object> search(@RequestParam String keyword,
                                      @RequestParam(defaultValue = "1") int page) {
        Map<String, Object> result = new HashMap<>();
        result.put("keyword", keyword);
        result.put("page", page);
        result.put("message", "搜索结果: " + keyword);
        return result;
    }

    /**
     * POST请求示例
     */
    @PostMapping("/users")
    public Map<String, Object> createUser(@RequestBody Map<String, Object> userData) {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "用户创建成功");
        result.put("user", userData);
        result.put("id", System.currentTimeMillis());
        return result;
    }
}
