package com.huai.dao.base;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.huai.annitation.Column;
import com.huai.annitation.Ignore;
import com.huai.annitation.PK;
import com.huai.annitation.Table;
import com.huai.constant.Const;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

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
     * 获取表的主键
     *
     * @return 表的主键列名
     */
    private String getPrimaryKeyColumn() {
        Field[] fields = ReflectUtil.getFields(this.clazz);
        List<Field> collect = CollUtil.toList(fields).stream().filter(field -> ObjectUtil.isNotNull(field.getAnnotation(PK.class))).collect(Collectors.toList());

        if (collect.size() > 0) {
            Field field = collect.get(0);
            String targetColumnByField = this.getTargetColumnByField(field);

            return targetColumnByField;
        } else {
            return null;
        }
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

    /**
     * 构造本 ORM 专用的行映射器：按 @Column 字典（与写路径同一本）把一行数据装进 T 对象。
     * BeanPropertyRowMapper / fillBeanWithMap 按「列名↔属性名」匹配，字段名与列名不一致时会静默丢数据
     *
     * @return 行映射器
     */
    private RowMapper<T> buildRowMapper() {
        return (rs, rowNum) -> {
            T instance = ReflectUtil.newInstance(clazz);
            for (Field field : ReflectUtil.getFields(clazz)) {
                // @Ignore 字段在数据库中不存在，跳过，否则 rs.getObject 会抛「列不存在」异常
                if (ObjectUtil.isNotNull(field.getAnnotation(Ignore.class))) {
                    continue;
                }
                // 列名走 @Column 字典；Convert 把 JDBC 返回类型对齐成字段类型（如 Integer → Long）
                Object value = Convert.convert(field.getType(), rs.getObject(this.getTargetColumnByField(field)));
                ReflectUtil.setFieldValue(instance, field, value);
            }
            return instance;
        };
    }

    /**
     * 通用插入，自增列需要添加 {@link PK} 注解
     *
     * @param t          对象
     * @param ignoreNull 是否忽略 null 值
     * @return 操作的行数
     */
    protected Integer insert(T t, Boolean ignoreNull) {
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
    protected Integer deleteById(P id) {
        String tableName = this.getTableName();

        String primaryKey = this.getPrimaryKeyColumn();
        if (ObjectUtil.isNotNull(primaryKey)) {
            String sql = StrUtil.format("DELETE FROM {table} WHERE {column} = ?", Dict.create().set("table", tableName).set("column", StrUtil.format("`{}`", primaryKey)));
            return jdbcTemplate.update(sql, id);
        } else {
            return 0;
        }
    }

    /**
     * 通用根据主键更新，自增列需要添加 {@link PK} 注解
     *
     * @param t          对象
     * @param id         主键
     * @param ignoreNull 是否忽略 null 值
     * @return 操作的行数
     */
    protected Integer updateById(T t, P id, Boolean ignoreNull) {
        // update table set xx where id = id
        String table = this.getTableName(t);

        List<Field> filterField = this.getField(t, ignoreNull);

        List<String> columnList = this.getColumns(filterField);

        String primaryKey = this.getPrimaryKeyColumn();

        if (ObjectUtil.isNotNull(primaryKey)) {
            // 处理 set 后的值
            List<String> columns = columnList.stream().map(cloumn -> StrUtil.appendIfMissing(cloumn, "= ?")).collect(Collectors.toList());
            String params = StrUtil.join(Const.SEPARATOR_COMMA, columns);

            String sql = StrUtil.format("UPDATE {table} SET {params} WHERE {column} = ?", Dict.create().set("table", table).set("params", params).set("column", StrUtil.format("`{}`", primaryKey)));

            // 获取对应的值
            List<Object> collect1 = filterField.stream().map(s -> ReflectUtil.getFieldValue(t, s)).collect(Collectors.toList());
            collect1.add(id);

            Object[] values = ArrayUtil.toArray(collect1, Object.class);

            // 执行sql
            return jdbcTemplate.update(sql, values);
        } else {
            return 0;
        }
    }

    /**
     * 通用根据主键查询单条记录
     *
     * @param id 主键
     * @return 单条记录，查不到或实体未声明 @PK 时返回 null
     */
    protected T getDetailById(P id) {
        String primaryKey = this.getPrimaryKeyColumn();
        if (ObjectUtil.isNull(primaryKey)) {
            return null;
        }

        String sql = StrUtil.format("SELECT * FROM {table} WHERE {column} = ?",
                Dict.create().set("table", this.getTableName()).set("column", StrUtil.format("`{}`", primaryKey)));

        try {
            return jdbcTemplate.queryForObject(sql, this.buildRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            // 主键不存在时安静返回 null，不把异常抛给调用方
            return null;
        }
    }

    /**
     * 通用条件查询：取实体的非空字段作为等值条件（and 连接）查询。
     * <p>条件为空时查询全表（列表刷新/首次加载场景）；数据量大时务必配合分页，避免一次性拉全表
     *
     * @param t 条件实体（非空字段 = 等值查询条件，全空 = 查询全部）
     * @return 符合条件的记录列表
     */
    protected List<T> getListByParams(T t) {
        // 只取非空字段作条件：null 字段若混入会生成 "= NULL"，永不匹配，静默查出空列表
        List<Field> fieldList = this.getField(t, true);

        // 每个条件拼 "`列` = ?"，条件间用 and 连接
        String where = fieldList.stream()
                .map(field -> StrUtil.format("`{}` = ?", this.getTargetColumnByField(field)))
                .collect(Collectors.joining(" and "));

        // 空条件时不拼 WHERE 子句（"WHERE" 单独出现是语法错误；条件拼装比垫 "1=1" 干净，SQL 里不留垃圾谓词）
        String whereClause = StrUtil.isEmpty(where) ? "" : " WHERE " + where;

        // 条件列与值派生自同一个 fieldList，顺序天然对齐；空条件时 values 为空数组，正好对应零个 ?
        Object[] values = fieldList.stream().map(field -> ReflectUtil.getFieldValue(t, field)).toArray();

        String sql = StrUtil.format("SELECT * FROM {table}{whereClause}",
                Dict.create().set("table", this.getTableName()).set("whereClause", whereClause));

        // 行 → T 一步到位，不再经过 List<Map> 中转
        return jdbcTemplate.query(sql, this.buildRowMapper(), values);
    }
}
