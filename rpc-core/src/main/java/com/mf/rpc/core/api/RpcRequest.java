package com.mf.rpc.core.api;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RpcRequest {
    private String clazzName;
    private String method;
    private Object[] params;

}
