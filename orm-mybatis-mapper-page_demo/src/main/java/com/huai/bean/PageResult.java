package com.huai.bean;

import com.github.pagehelper.PageInfo;
import lombok.Data;

import java.util.List;

/**
 * 统一分页返回结构：接口对外只暴露前端真正需要的字段，
 * 不直接返回 PageHelper 的 PageInfo（避免第三方类结构和内部字段泄漏到接口契约里）
 */
@Data
public class PageResult<T> {

    /** 当前页数据 */
    private List<T> list;

    /** 总记录数（前端显示"共 x 条"） */
    private long total;

    /** 当前页码 */
    private int pageNum;

    /** 每页条数 */
    private int pageSize;

    /** 总页数 */
    private int pages;

    /**
     * 从 PageInfo 转换：Service 内部用 PageHelper，对外只给这个精简结构。
     * 需要更多字段（比如 hasNextPage）时按前端需求再加，加字段是兼容的，减字段是事故
     */
    public static <T> PageResult<T> of(PageInfo<T> pageInfo) {
        PageResult<T> result = new PageResult<>();
        result.setList(pageInfo.getList());
        result.setTotal(pageInfo.getTotal());
        result.setPageNum(pageInfo.getPageNum());
        result.setPageSize(pageInfo.getPageSize());
        result.setPages(pageInfo.getPages());
        return result;
    }
}
