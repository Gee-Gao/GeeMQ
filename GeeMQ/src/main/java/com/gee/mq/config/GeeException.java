package com.gee.mq.config;

import lombok.Data;

@Data
public class GeeException extends RuntimeException {
    private String msg;

    public GeeException(String msg) {
        this.msg = msg;
    }
}
