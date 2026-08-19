package com.huai.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.huai.constant.Const;
import com.huai.dao.UserDao;
import com.huai.entity.User;
import com.huai.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements IUserService {
    @Autowired
    private UserDao userDao;

    @Override
    public Boolean save(User user) {
        // 处理密码的加密
        String rawPass = user.getPassword();
        String salt = IdUtil.simpleUUID();
        String pass = SecureUtil.md5(rawPass + Const.SALT_PREFIX + salt);
        user.setPassword(pass);
        user.setSalt(salt);
        return userDao.insert(user) > 0;
    }

    @Override
    public Boolean delete(Long id) {
        return userDao.delete(id) > 0;
    }

    @Override
    public Boolean update(User user, Long id) {
        User exist = getUserById(id);
        if (StrUtil.isNotBlank(user.getPassword())) {
            String rawPass = user.getPassword();
            String salt = IdUtil.simpleUUID();
            String pass = SecureUtil.md5(rawPass + Const.SALT_PREFIX + salt);
            user.setPassword(pass);
            user.setSalt(salt);
        }
        BeanUtil.copyProperties(user, exist, CopyOptions.create().setIgnoreNullValue(true));
        exist.setLastUpdateTime(new DateTime());
        return userDao.update(exist, id) > 0;
    }

    @Override
    public User getUserById(Long id) {
        return userDao.selectById(id);
    }

    @Override
    public List<User> getUser(User user) {
        return userDao.getList(user);
    }
}
