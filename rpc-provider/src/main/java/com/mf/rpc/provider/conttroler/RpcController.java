package com.mf.rpc.provider.conttroler;

import com.mf.rpc.core.api.RpcRequest;
import com.mf.rpc.core.api.RpcResponse;
import com.mf.rpc.core.server.RpcInvoker;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
//@RequiredArgsConstructor
public class RpcController {
    @Autowired
    private RpcInvoker rpcInvoker;

    @PostMapping("/")
    public RpcResponse handleRpc(@RequestBody RpcRequest rpcRequest) {
        System.out.println(rpcRequest);
        return rpcInvoker.invoke(rpcRequest);
    }
}
