package com.mf.rpc.core.server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.mf.rpc.core.api.RpcRequest;
import com.mf.rpc.core.api.RpcResolver;
import com.mf.rpc.core.api.RpcResponse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

public class RpcInvoker {
    private RpcResolver resolver;

    public RpcInvoker(RpcResolver resolver) {
        this.resolver = resolver;
    }

    public RpcResponse invoke(RpcRequest request) {

        String clazzName = request.getClazzName();
        Object instance = resolver.resolve(clazzName);
        try {
            Method method = getMethod(instance.getClass(), request.getMethod());
            Object result = method.invoke(instance, request.getParams());
           return RpcResponse.builder()
                    .result(JSON.toJSONString(result, SerializerFeature.WriteClassName))
                    .status(true)
                    .build();
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            return RpcResponse.builder()
                    .status(false)
                    .exception(e)
                    .build();
        }
    }

    private Method getMethod(Class<?> clazz, String methodName){
        return  Arrays.stream(clazz.getMethods()).filter(m -> methodName.equals(m.getName())).findFirst().get();
    }

}