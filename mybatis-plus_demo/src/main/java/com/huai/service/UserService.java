package com.huai.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.huai.domain.OrderVO;
import com.huai.entity.User;

import java.util.List;

public interface UserService extends IService<User> {
    List<OrderVO> selectOrdersVo();
}
