package com.huai.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.huai.domain.OrderVO;
import com.huai.entity.User;
import com.huai.mapper.UserMapper;
import com.huai.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public List<OrderVO> selectOrdersVo() {
        List<OrderVO> orderVoList = userMapper.selectOrdersVo();
        return orderVoList;
    }
}
