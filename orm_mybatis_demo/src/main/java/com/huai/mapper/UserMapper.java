package com.huai.mapper;

import com.huai.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {
    User getUserById(Long id);

    List<User> getUserList(User user);

    Integer insertUser(User user);

    Integer updateUserInfo(User user);

    Integer removeUserById(Long id);
}
