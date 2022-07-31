package com.gee.mq.config;

import com.gee.mq.bean.Result;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Component
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GeeException.class)
    public Result handlerRuntimeException(GeeException e) {
        return Result.error(e.getMsg());
    }
}
