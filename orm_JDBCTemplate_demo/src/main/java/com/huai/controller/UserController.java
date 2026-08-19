package com.huai.controller;

import cn.hutool.core.lang.Dict;
import com.huai.entity.User;
import com.huai.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {
    @Autowired
    private IUserService userService;

    // 新增
    @PostMapping("/saveUser")
    public Dict save(User user) {
        Boolean save = userService.save(user);
        return Dict.create().set("code", save ? 200 : 500).set("data", save ? save : null).set("msg", save ? "成功" : "失败");
    }
}
