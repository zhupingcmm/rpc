package com.mf.rpc.provider.conttroler;

import com.mf.rpc.core.api.RpcRequest;
import com.mf.rpc.core.api.RpcResponse;
import com.mf.rpc.core.server.RpcInvoker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RpcController {
    @Autowired
    private RpcInvoker rpcInvoker;

    @PostMapping("/")
    public RpcResponse handleRpc(@RequestBody RpcRequest rpcRequest) {
        return rpcInvoker.invoke(rpcRequest);
    }
}
