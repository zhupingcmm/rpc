package com.mf.rpc.consumer;

import com.mf.rpc.api.User;
import com.mf.rpc.api.UserService;
import com.mf.rpc.core.client.RpcClient;

public class Consumer {
    public static void main(String[] args) throws InterruptedException {
        RpcClient rpcClient = new RpcClient("localhost:2181");
        UserService userService = rpcClient.createFromRegistry(UserService.class);
        User user = userService.findById(1);
        System.out.println("user::" + user.getName());
    }
}
