package com.mf.rpc.core.zk.Impl;

import com.mf.rpc.core.zk.Router;

import java.util.List;

public class RouterImpl implements Router {
    @Override
    public String route(List<String> urls) {
        System.out.println(urls);
        return urls.get(0);
    }
}
