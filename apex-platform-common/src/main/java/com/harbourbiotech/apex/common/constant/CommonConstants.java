package com.harbourbiotech.apex.common.constant;

/**
 * 通用常量定义
 * <p>
 * 定义系统中使用的常量值
 *
 * @author Harbour BioMed
 * @version 1.0.0
 */
public class CommonConstants {

    /**
     * JWT Token 请求头名称
     */
    public static final String JWT_TOKEN_HEADER = "Authorization";

    /**
     * JWT Token 前缀
     */
    public static final String JWT_TOKEN_PREFIX = "Bearer ";

    /**
     * 默认分页大小
     */
    public static final Integer DEFAULT_PAGE_SIZE = 10;

    /**
     * 最大分页大小
     */
    public static final Integer MAX_PAGE_SIZE = 100;

    /**
     * UTF-8 编码
     */
    public static final String UTF8 = "UTF-8";

    /**
     * 默认成功码
     */
    public static final Integer SUCCESS_CODE = 0;

    /**
     * 默认成功消息
     */
    public static final String SUCCESS_MESSAGE = "success";

    /**
     * 时间日期格式
     */
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 日期格式
     */
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * 私有构造方法，防止实例化
     */
    private CommonConstants() {
        throw new UnsupportedOperationException("常量类不允许实例化");
    }
}
