package com.huai.query;

import lombok.Data;

/**
 * 分页参数基类：各业务的查询对象继承它，分页参数不用每个类重复写。
 * GET 参数由 Spring MVC 自动绑定到字段上：?pageNum=1&pageSize=10
 */
@Data
public class PageQuery {

    /**
     * 页码，从 1 开始
     */
    private Integer pageNum = 1;

    /**
     * 每页条数
     */
    private Integer pageSize = 10;

    /**
     * LIMIT 偏移量：第 n 页跳过 (n-1)*pageSize 条。
     * XML 里的 #{offset} 会通过这个 getter 取值，Service 不用单独传 offset 了
     */
    public int getOffset() {
        return (pageNum - 1) * pageSize;
    }
}
