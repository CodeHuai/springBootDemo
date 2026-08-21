package com.huai.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.huai.constant.Passworld;
import com.huai.entity.User;
import com.huai.mapper.UserMapper;
import com.huai.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;


@Service
public class UserServiceImpl implements IUserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public User getUserById(Long id) {
        if (ObjectUtil.isNull(id) || StrUtil.isBlank(id.toString())) {
            return null;
        }

        return userMapper.getUserById(id);
    }

    @Override
    public List<User> getUserList(User user) {
        if (ObjectUtil.isNotNull(user) || ObjectUtil.isNotEmpty(user)) {
            return userMapper.getUserList(user);
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public Boolean insertUser(User user) {
        if (ObjectUtil.isNull(user) || ObjectUtil.isEmpty(user)) {
            return false;
        }

        user.setCreateTime(new Date());
        user.setStatus(1);
        String rawPass = user.getPassword();
        String salt = IdUtil.simpleUUID();
        String pass = SecureUtil.md5(rawPass + Passworld.PASSWORLD.getValue() + salt);
        user.setPassword(pass);
        user.setSalt(salt);
        Integer i = userMapper.insertUser(user);

        return i > 0;
    }


}
