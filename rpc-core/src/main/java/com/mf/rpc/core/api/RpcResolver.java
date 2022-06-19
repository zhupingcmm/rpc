package com.mf.rpc.core.api;

public interface RpcResolver<T> {
    T resolve(String clazzName);
}
