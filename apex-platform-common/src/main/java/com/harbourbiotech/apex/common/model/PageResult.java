package com.harbourbiotech.apex.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页响应数据类
 * <p>
 * 封装分页查询的响应结果
 *
 * @param <T> 记录类型
 * @author Harbour BioMed
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据记录列表
     */
    private List<T> records;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 每页大小
     */
    private Integer pageSize;

    /**
     * 总页数
     */
    private Integer totalPages;

    /**
     * 是否有下一页
     */
    private Boolean hasNext;

    /**
     * 创建空的分页结果
     *
     * @param <T> 记录类型
     * @return 空的分页结果
     */
    public static <T> PageResult<T> empty() {
        return PageResult.<T>builder()
                .records(Collections.emptyList())
                .total(0L)
                .pageNum(1)
                .pageSize(10)
                .totalPages(0)
                .hasNext(false)
                .build();
    }

    /**
     * 创建分页结果（自动计算总页数和是否有下一页）
     *
     * @param records  数据记录列表
     * @param total    总记录数
     * @param pageNum  当前页码
     * @param pageSize 每页大小
     * @param <T>      记录类型
     * @return 分页结果
     */
    public static <T> PageResult<T> of(List<T> records, Long total, Integer pageNum, Integer pageSize) {
        int totalPages = (int) Math.ceil((double) total / pageSize);
        boolean hasNext = pageNum < totalPages;

        return PageResult.<T>builder()
                .records(records)
                .total(total)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .totalPages(totalPages)
                .hasNext(hasNext)
                .build();
    }

    /**
     * 计算总页数
     *
     * @return 总页数
     */
    public int calculateTotalPages() {
        if (total == null || total == 0) {
            return 0;
        }
        if (pageSize == null || pageSize == 0) {
            return 0;
        }
        return (int) Math.ceil((double) total / pageSize);
    }

    /**
     * 判断是否有下一页
     *
     * @return 是否有下一页
     */
    public boolean isHasNext() {
        if (pageNum == null || totalPages == null) {
            return false;
        }
        return pageNum < totalPages;
    }
}
