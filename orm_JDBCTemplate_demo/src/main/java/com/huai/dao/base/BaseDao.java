package com.huai.dao.base;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.huai.annitation.Column;
import com.huai.annitation.Ignore;
import com.huai.annitation.PK;
import com.huai.annitation.Table;
import com.huai.constant.Const;
import org.springframework.jdbc.core.JdbcTemplate;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BaseDao<T, P> {
    private JdbcTemplate jdbcTemplate;
    private Class<T> clazz;

    public BaseDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        // 这里最终实际获得的就是T对应的类型
        clazz = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }


    /**
     * 获取表名称
     *
     * @param t 对象
     * @return 表名
     */
    private String getTableName(T t) {
        // 通过 类对象 获取 Table.class 的对应注解
        Table annotation = t.getClass().getAnnotation(Table.class);
        // 如果当前Table注解存在，返回这个Table注解中的name对应的值，也就是表名
        if (ObjectUtil.isNotEmpty(annotation)) {
            return StrUtil.format("`{}`", annotation.name());
        } else {
            // 如果不存在，拿到的就是 全限定名（带包）
            return StrUtil.format("`{}`", t.getClass().getSimpleName().toLowerCase());
        }
    }

    /**
     * 通用插入，自增列需要添加 {@link PK} 注解
     *
     * @param t          对象
     * @param ignoreNull 是否忽略 null 值
     * @return 操作的行数
     */
    private Integer insert(T t, Boolean ignoreNull) {
        // 通过实例获取对应的表名
        String table = this.getTableName(t);

        // 获取这个t对应类下的所有属性，包括私有和其他模式的修饰符字段，继承过来的字段也可以拿到
        List<Field> filterField = this.getField(t, ignoreNull);

        // 根据类的字段，通过注解Column，获取对应的数据库字段，没有标记这个Column注解的，默认就是数据库字段
        List<String> columnList = this.getColumns(filterField);

        // 将对应的数据库列名通过逗号拼接
        String columns = StrUtil.join(Const.SEPARATOR_COMMA, columnList);

        // 占位符 ?,?,?,...
        String params = StrUtil.repeatAndJoin("?", columnList.size(), Const.SEPARATOR_COMMA);
        // 获取实例t上，对应field字段的值，放入数组中
        Object[] values = filterField.stream().map(field -> ReflectUtil.getFieldValue(t, field)).toArray();

        String sql = StrUtil.format("INSERT INTO {table} ({columns}) VALUES ({params})", Dict.create().set("table", table).set("columns", columns).set("params", params));

        return jdbcTemplate.update(sql, values);
    }

    /**
     * 通用根据主键删除（适合主键只有一个字段，而不是关联字段组成的主键）
     *
     * @param id 主键
     * @return 影响行数
     */
    private Integer deleteById(P id) {
        String tableName = this.getTableName();

        Field[] fields = ReflectUtil.getFields(this.clazz);
        List<Field> collect = CollUtil.toList(fields).stream().filter(field -> ObjectUtil.isNotNull(field.getAnnotation(PK.class))).collect(Collectors.toList());

        int size = collect.size();
        if (size > 0) {
            Field field = collect.get(0);
            // 这里需要获取字段对应的列名
            String targetColumnByField = this.getTargetColumnByField(field);
            String sql = StrUtil.format("DELETE FROM {table} where {column} = ?", Dict.create().set("table", tableName).set("column", targetColumnByField));
            return jdbcTemplate.update(sql, id);
        } else {
            return 0;
        }
    }


    /**
     * 根据字段获取列名
     *
     * @param field 字段对象
     * @return 对应列名
     */
    private String getTargetColumnByField(Field field) {
        Column columnAnnotation = field.getAnnotation(Column.class);
        String columnName;

        if (ObjectUtil.isNotNull((columnAnnotation))) {
            columnName = columnAnnotation.name();
        } else {
            columnName = field.getName();
        }
        return columnName;
    }

    /**
     * 获取表名
     *
     * @return 表名
     */
    private String getTableName() {
        Table annotation = clazz.getAnnotation(Table.class);
        if (ObjectUtil.isNotEmpty(annotation)) {
            return StrUtil.format("`{}`", annotation.name());
        } else {
            return StrUtil.format("`{}`", clazz.getSimpleName().toLowerCase());
        }
    }

    /**
     * 获取列
     *
     * @param fieldList 字段列表
     * @return 列信息列表
     */
    private List<String> getColumns(List<Field> fieldList) {
        ArrayList<String> columnList = CollUtil.newArrayList();
        for (Field field : fieldList) {
            String columnName = this.getTargetColumnByField(field);
            columnList.add(StrUtil.format("`{}`", columnName));
        }
        return columnList;
    }

    /**
     * 获取字段列表 {@code 过滤数据库中不存在的字段，以及自增列}
     *
     * @param t          对象
     * @param ignoreNull 是否忽略空值
     * @return 字段列表
     */
    private List<Field> getField(T t, Boolean ignoreNull) {
        // 获取所有字段，包含父类中继承过来的字段，私有的字段也存在，并且继承的字段也能拿到
        Field[] fields = ReflectUtil.getFields(t.getClass());

        List<Field> filterField;
        // 只获取没有加 Ignore注解和PK注解的字段
        Stream<Field> fieldStream = CollUtil.toList(fields).stream().filter(field -> ObjectUtil.isNull(field.getAnnotation(Ignore.class)) && ObjectUtil.isNull(field.getAnnotation(PK.class)));

        // 是否过滤字段值为null的字段
        if (ignoreNull) {
            filterField = fieldStream.filter(field -> ObjectUtil.isNotNull(ReflectUtil.getFieldValue(t, field))).collect(Collectors.toList());
        } else {
            filterField = fieldStream.collect(Collectors.toList());
        }

        return filterField;
    }
}
