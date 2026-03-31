package com.harbourbiotech.apex.common.handler;

import com.harbourbiotech.apex.common.exception.BusinessException;
import com.harbourbiotech.apex.common.model.ErrorCode;
import com.harbourbiotech.apex.common.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.security.auth.message.AuthException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * <p>
 * 统一处理各种异常，返回规范的错误响应格式
 *
 * @author Harbour BioMed
 * @version 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务业务异常
     * <p>
     * 返回自定义错误码和错误信息
     *
     * @param e 业务异常
     * @return 错误响应
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.error("业务异常: code={}, message={}", e.getCode(), e.getMessage(), e);
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常
     * <p>
     * 返回参数格式错误码和详细的校验失败信息
     *
     * @param e 参数校验异常
     * @return 错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleValidationException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        log.error("参数校验失败: {}", errorMessage);
        return Result.error(ErrorCode.PARAM_FORMAT_ERROR, errorMessage);
    }

    /**
     * 处理认证异常
     * <p>
     * 返回 401 未授权状态
     *
     * @param e 认证异常
     * @return 错误响应
     */
    @ExceptionHandler({AuthException.class, org.springframework.security.core.AuthenticationException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<?> handleAuthException(Exception e) {
        log.error("认证失败: {}", e.getMessage());
        return Result.error(ErrorCode.AUTH_FAILED, "认证失败");
    }

    /**
     * 处理访问拒绝异常
     * <p>
     * 返回 403 禁止访问状态
     *
     * @param e 访问拒绝异常
     * @return 错误响应
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<?> handleAccessDeniedException(AccessDeniedException e) {
        log.error("访问拒绝: {}", e.getMessage());
        return Result.error(ErrorCode.ACCESS_DENIED, "无访问权限");
    }

    /**
     * 处理未预期的异常
     * <p>
     * 返回 500 系统错误状态，避免暴露系统内部信息
     *
     * @param e 异常
     * @return 错误响应
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(ErrorCode.SYSTEM_ERROR, "系统异常，请稍后重试");
    }

    /**
     * 处理非法参数异常
     * <p>
     * 返回 400 错误状态
     *
     * @param e 非法参数异常
     * @return 错误响应
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("非法参数: {}", e.getMessage());
        return Result.error(ErrorCode.PARAM_VALUE_INVALID, e.getMessage());
    }

    /**
     * 处理空指针异常
     * <p>
     * 返回 500 系统错误状态
     *
     * @param e 空指针异常
     * @return 错误响应
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleNullPointerException(NullPointerException e) {
        log.error("空指针异常", e);
        return Result.error(ErrorCode.SYSTEM_ERROR, "系统异常，请稍后重试");
    }
}
