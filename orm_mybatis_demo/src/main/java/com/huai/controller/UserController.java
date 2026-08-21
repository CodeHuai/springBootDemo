package com.huai.controller;

import com.huai.entity.User;
import com.huai.service.IUserService;
import com.huai.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {
    @Autowired
    private IUserService iUserService;


    @GetMapping("/getUserById/{id}")
    public User getUserById(@PathVariable(value = "id") Long id) {
        return iUserService.getUserById(id);
    }
}
