package com.naidelii.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author naidelii
 * Result响应枚举
 */
@Getter
@AllArgsConstructor
public enum ResultCodeEnum {
    /**
     * 操作成功
     */
    SUCCESS("操作成功"),
    /**
     * 操作失败
     */
    FAIL("操作失败");

    /**
     * 响应消息
     */
    private final String message;


}
