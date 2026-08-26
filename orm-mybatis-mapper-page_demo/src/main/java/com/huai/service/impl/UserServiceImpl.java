package com.huai.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.huai.bean.PageResult;
import com.huai.entity.User;
import com.huai.mapper.UserMapper;
import com.huai.query.UserQuery;
import com.huai.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements IUserService {

    // 页大小上限：防止前端传 pageSize=100000 这类超大分页把数据库和内存拖垮
    private static final int MAX_PAGE_SIZE = 100;

    @Autowired
    private UserMapper userMapper;

    @Override
    public PageResult<User> getUserPage(UserQuery query) {
        normalize(query);

        // 方式一：分页逻辑全部交给插件。
        // PageHelper 基于 ThreadLocal：startPage 之后【紧跟的第一条】查询会被改写成
        // "SELECT count(0) ... where 条件" + "... where 条件 LIMIT ?,?" 两条 SQL，
        // 所以两行必须紧挨着，中间不能插入其它数据库操作，否则分页会错打到别的查询上
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        List<User> users = userMapper.selectUserList(query);

        // PageInfo 在 Service 内部用完就转成 PageResult，不对外泄漏第三方类结构
        return PageResult.of(PageInfo.of(users));
    }

    @Override
    public PageResult<User> getUserPageRaw(UserQuery query) {
        normalize(query);

        // 方式二：手写分页，插件干的事这里全部自己干一遍——
        // 1. 查符合条件的总记录数（count 和列表必须用同一份条件）
        long total = userMapper.countUser(query);
        // 2. 查当前页数据：LIMIT 跳过条数, 取条数（offset 由 PageQuery.getOffset() 算好）
        List<User> users = userMapper.selectUserPage(query);

        // 3. 组装分页结果
        PageResult<User> result = new PageResult<>();
        result.setList(users);
        result.setTotal(total);
        result.setPageNum(query.getPageNum());
        result.setPageSize(query.getPageSize());
        // 总页数 = 总数/页大小 向上取整：23 条、每页 10 条 => 3 页
        result.setPages((int) ((total + query.getPageSize() - 1) / query.getPageSize()));
        return result;
    }

    // 参数兜底：页码、页大小缺省或非法时给默认值，页大小强制收口到上限
    private void normalize(UserQuery query) {
        if (query.getPageNum() == null || query.getPageNum() < 1) {
            query.setPageNum(1);
        }
        if (query.getPageSize() == null || query.getPageSize() < 1) {
            query.setPageSize(10);
        } else {
            query.setPageSize(Math.min(query.getPageSize(), MAX_PAGE_SIZE));
        }
    }
}
