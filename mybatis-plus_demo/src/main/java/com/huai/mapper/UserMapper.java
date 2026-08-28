package com.huai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huai.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
