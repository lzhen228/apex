package com.harbourbiotech.apex.common.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 分页查询参数类
 * <p>
 * 用于接收分页查询请求参数
 *
 * @author Harbour BioMed
 * @version 1.0.0
 */
@Data
public class PageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 页码（从1开始）
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 获取偏移量（用于SQL的OFFSET）
     *
     * @return 偏移量
     */
    public long getOffset() {
        return (pageNum - 1L) * pageSize;
    }

    /**
     * 校验分页参数
     */
    public void validate() {
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        if (pageSize > 100) {
            pageSize = 100; // 限制最大每页100条
        }
    }
}
