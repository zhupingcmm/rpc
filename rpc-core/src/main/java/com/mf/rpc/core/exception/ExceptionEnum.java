package com.mf.rpc.core.exception;

import lombok.Getter;

public enum ExceptionEnum {

    SYSTEM_ERROR(8888,"系统错误"),
    SERVER_NOT_READY(9999, "后台系统没找到");


    ExceptionEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }
    @Getter
    private int code;
    @Getter
    private String message;


}
