package com.openx3.framework.web;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.openx3.common.common.R;
import com.openx3.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一处理所有异常，返回标准 JSON 响应
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public R<?> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return R.error(e.getCode(), e.getMessage());
    }

    /**
     * 未登录/登录过期
     */
    @ExceptionHandler(NotLoginException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public R<?> handleNotLoginException(NotLoginException e) {
        log.warn("未登录: {}", e.getMessage());
        return R.error(401, "未登录或登录已过期");
    }

    /**
     * 无权限访问
     */
    @ExceptionHandler({NotPermissionException.class, NotRoleException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public R<?> handleNoPermissionException(Exception e) {
        log.warn("无权限: {}", e.getMessage());
        return R.error(403, "无权限访问");
    }
    
    /**
     * 处理参数校验异常
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<?> handleValidationException(Exception e) {
        String message = "参数校验失败";
        
        if (e instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException ex = (MethodArgumentNotValidException) e;
            message = ex.getBindingResult().getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
        } else if (e instanceof BindException) {
            BindException ex = (BindException) e;
            message = ex.getBindingResult().getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
        }
        
        log.warn("参数校验异常: {}", message);
        return R.error(400, message);
    }
    
    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<?> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常", e);
        return R.error(500, e.getMessage());
    }
    
    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<?> handleException(Exception e) {
        log.error("系统异常", e);
        return R.error(500, "系统内部错误");
    }
}
