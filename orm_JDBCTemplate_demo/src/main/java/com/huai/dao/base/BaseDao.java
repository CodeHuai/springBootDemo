package com.huai.dao.base;

import org.springframework.jdbc.core.JdbcTemplate;

import java.lang.reflect.ParameterizedType;

public class BaseDao<T, P> {
    private JdbcTemplate jdbcTemplate;
    private Class<T> clazz;

    public BaseDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        // 这里最终实际获得的就是T对应的类型
        clazz = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    protected Integer insert(T t, Boolean ignorNull) {

    }
}
