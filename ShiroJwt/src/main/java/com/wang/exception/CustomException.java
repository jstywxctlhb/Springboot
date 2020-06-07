package com.wang.exception;

/**
 * 自定义异常(CustomException)
 */
public class CustomException extends RuntimeException {

    public CustomException() {

    }

    public CustomException(String msg) {
        super(msg);
    }
}