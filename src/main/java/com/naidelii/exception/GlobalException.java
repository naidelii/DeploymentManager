package com.naidelii.exception;

/**
 * @author naidelii
 * 全局异常
 */
public class GlobalException extends RuntimeException {

    /**
     * 错误提示
     */
    private String message;


    public GlobalException() {
    }

    public GlobalException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

}
