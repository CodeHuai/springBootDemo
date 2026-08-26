package com.huai.mapper;

import com.huai.entity.User;
import com.huai.query.UserQuery;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {

    // 条件 + 列表查询：SQL 不写 LIMIT，配合 PageHelper.startPage 分页。
    // 单个对象参数不需要 @Param，XML 里直接引用属性 #{name}、#{status}
    List<User> selectUserList(UserQuery query);

    // 手写分页：条件 + LIMIT #{offset}, #{pageSize}，offset 由 PageQuery.getOffset() 算好
    List<User> selectUserPage(UserQuery query);

    // 手写分页配套：总数查询，必须带【同样的】搜索条件，否则 total 和列表对不上
    Long countUser(UserQuery query);
}
