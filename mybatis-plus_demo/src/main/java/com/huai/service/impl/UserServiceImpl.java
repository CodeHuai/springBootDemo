package com.huai.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.huai.entity.User;
import com.huai.mapper.UserMapper;
import com.huai.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
