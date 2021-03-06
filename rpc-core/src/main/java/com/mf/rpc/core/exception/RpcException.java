package com.mf.rpc.core.exception;

import lombok.Data;

@Data
public class RpcException extends RuntimeException {
    private int code;
    private String message;


    public RpcException (int code, String message) {
        this.code = code;
        this.message = message;
    }
    public RpcException(ExceptionEnum responseEnum) {
        this.code = responseEnum.getCode();
        this.message = responseEnum.getMessage();
    }
}
