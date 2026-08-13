package com.huai.helloworld_demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Hello World 示例接口
 */
@RestController
@RequestMapping("/api")
public class Helloworld {

    /**
     * 返回 Hello, {who}!
     * <p>
     * who 为空（不传或传空串）时默认 World。
     *
     * @param who 名字，非必须；前端通过查询参数 {@code who} 传值
     */
    @GetMapping("/hello")
    public String hello(@RequestParam(required = false, name = "who") String who) {
        if (who == null || who.trim().isEmpty()) {
            who = "World";
        }
        return "Hello, " + who + "!";
    }

    /**
     * 多参数示例：用多个 {@code @RequestParam} 分别接收查询参数
     * <p>
     * 请求示例：/demo/api/search?keyword=java&page=2&sort=desc
     *
     * @param keyword 关键词（必填）
     * @param page    页码，默认 1
     * @param sort    排序方式，非必须
     */
    @GetMapping("/search")
    public String search(@RequestParam String keyword,
                         @RequestParam(defaultValue = "1") int page,
                         @RequestParam(required = false) String sort) {
        return String.format("搜索「%s」，第 %d 页，排序：%s", keyword, page, sort);
    }
}