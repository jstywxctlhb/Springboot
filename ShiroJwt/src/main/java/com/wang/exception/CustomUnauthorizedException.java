package com.wang.exception;

/**
 * 自定义401无权限异常(UnauthorizedException)
 */
public class CustomUnauthorizedException extends RuntimeException {

    public CustomUnauthorizedException() {

    }

    public CustomUnauthorizedException(String msg) {
        super(msg);
    }
}