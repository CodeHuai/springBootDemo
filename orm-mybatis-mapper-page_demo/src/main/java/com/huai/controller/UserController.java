package com.huai.controller;

import com.huai.bean.ApiResponse;
import com.huai.bean.PageResult;
import com.huai.entity.User;
import com.huai.query.UserQuery;
import com.huai.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private IUserService iUserService;

    // 方式一：PageHelper 分页 + 搜索条件。
    // 条件放 JSON body 里：不进 URL、不进浏览器历史、不进网关 access log，
    // 而且条件再复杂（数组、嵌套对象）也装得下。前端请求体示例：
    // POST /demo/api/user/page   {"name":"小","status":1,"pageNum":1,"pageSize":10}
    @PostMapping("/user/page")
    public ApiResponse<PageResult<User>> getUserPage(@RequestBody UserQuery query) {
        return ApiResponse.success(iUserService.getUserPage(query));
    }

    // 方式二：手写 LIMIT 分页 + 搜索条件（对照用）
    // POST /demo/api/user/pageRaw   {"name":"小","pageNum":2,"pageSize":1}
    @PostMapping("/user/pageRaw")
    public ApiResponse<PageResult<User>> getUserPageRaw(@RequestBody UserQuery query) {
        return ApiResponse.success(iUserService.getUserPageRaw(query));
    }
}
