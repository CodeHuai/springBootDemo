package com.huai.query;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 用户列表查询条件：分页参数继承自 PageQuery，搜索条件按业务加。
 * 前端请求 ?name=小&status=1&startTime=2026-08-01&pageNum=1&pageSize=10
 * Spring MVC 会按字段名自动绑定，Controller 一个对象接收，不用一排 @RequestParam
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserQuery extends PageQuery {

    /**
     * 用户名，模糊匹配（name LIKE '%xx%'）
     */
    private String name;

    /**
     * 状态，精确匹配：0 禁用 / 1 启用
     */
    private Integer status;

    /**
     * 创建时间范围-开始，格式 yyyy-MM-dd。
     * 两个注解分工：@DateTimeFormat 管 GET/form 参数绑定，@JsonFormat 管 POST JSON body 解析
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date startTime;

    /**
     * 创建时间范围-结束，格式 yyyy-MM-dd
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date endTime;
}
