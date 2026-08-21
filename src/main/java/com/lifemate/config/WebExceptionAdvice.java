package com.lifemate.config;

import com.lifemate.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** 全局异常处理：RuntimeException 统一记日志并返回 Result.fail("服务器异常")。 */
@Slf4j
@RestControllerAdvice
public class WebExceptionAdvice {
    @ExceptionHandler(RuntimeException.class)
    public Result handleRuntimeException(RuntimeException e) {
        log.error("unhandled runtime exception", e);
        return Result.fail("服务器异常");
    }
}
