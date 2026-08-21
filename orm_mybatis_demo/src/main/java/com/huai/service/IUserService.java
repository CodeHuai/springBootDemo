package com.huai.service;

import com.huai.entity.User;

import java.util.List;


public interface IUserService {
    User getUserById(Long id);

    List<User> getUserList(User user);

    Boolean insertUser(User user);
}
