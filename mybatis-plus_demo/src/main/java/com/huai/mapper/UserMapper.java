package com.huai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huai.domain.OrderVO;
import com.huai.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    List<OrderVO> selectOrdersVo();
}
