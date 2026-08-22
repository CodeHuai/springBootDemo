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

    @Override
    public User updateUserInfo(Long id, User user) {
        if (ObjectUtil.isNotNull(id) || StrUtil.isNotBlank(id.toString())) {
            User targetUser = this.getUserById(id);

            if (ObjectUtil.isNotNull(targetUser) && ObjectUtil.isNotNull(user)) {
                user.setId(id);
                Integer index = userMapper.updateUserInfo(user);
                if (index > 0) {
                    User afterUserInfo = userMapper.getUserById(id);
                    return afterUserInfo;
                }
            }
        }
        return null;
    }

    @Override
    public Boolean removeUserById(Long id) {
        if (StrUtil.isNotBlank(id.toString())) {
            Integer index = userMapper.removeUserById(id);
            return index > 0;
        }
        return false;
    }


}
