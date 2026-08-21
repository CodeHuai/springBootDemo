package com.huai.mapper;

import com.huai.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    User getUserById(Long id);
}
