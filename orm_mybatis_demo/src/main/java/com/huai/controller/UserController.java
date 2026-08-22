package com.huai.controller;

import com.huai.bean.ApiResponse;
import com.huai.entity.User;
import com.huai.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class UserController {
    @Autowired
    private IUserService iUserService;

    // 根据id查询对应的user
    @GetMapping("/getUserById/{id}")
    public ApiResponse getUserById(@PathVariable(value = "id") Long id) {
        User user = iUserService.getUserById(id);
        return ApiResponse.success(user);
    }

    // 查询所有的user信息，并且支持搜索
    @PostMapping("/getUserList")
    public ApiResponse getUserList(User user) {
        List<User> list = iUserService.getUserList(user);
        return ApiResponse.success(list);
    }

    // 新增用户
    @PostMapping("/insertUser")
    public ApiResponse insertUser(@RequestBody User user) {
        iUserService.insertUser(user);
        return ApiResponse.success(user);
    }

    // 更新用户信息
    @PostMapping("/updateUserInfo/{id}")
    public ApiResponse updateUserInfo(@PathVariable("id") long id, @RequestBody User user) {
        return ApiResponse.success(iUserService.updateUserInfo(id, user));
    }

    // 删除用户
    @GetMapping("/removeUserById/{id}")
    public ApiResponse removeUserById(@PathVariable("id") Long id) {
        Boolean flag = iUserService.removeUserById(id);
        return flag ? ApiResponse.success() : ApiResponse.error();
    }
}
