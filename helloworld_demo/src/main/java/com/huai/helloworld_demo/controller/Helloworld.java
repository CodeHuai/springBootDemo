package com.huai.helloworld_demo.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class Helloworld {

    @GetMapping("/hello")
    public String hello(@RequestParam(required = false, name = "who") String who) {
        return "Hello World!";
    }
}
