package com.mf.rpc.core.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class  RpcResponse<T> {
    private T result;
    private boolean status;
    private Exception exception;

}
