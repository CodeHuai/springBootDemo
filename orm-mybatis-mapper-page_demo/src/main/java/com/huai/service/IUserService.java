package com.huai.service;

import com.huai.bean.PageResult;
import com.huai.entity.User;
import com.huai.query.UserQuery;

public interface IUserService {

    // 条件 + 分页查询：PageHelper 插件版
    PageResult<User> getUserPage(UserQuery query);

    // 条件 + 分页查询：手写 LIMIT 版（对照用，看清插件帮你干了什么）
    PageResult<User> getUserPageRaw(UserQuery query);
}
