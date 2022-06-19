package com.mf.rpc.core.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.mf.rpc.core.api.RpcRequest;
import com.mf.rpc.core.api.RpcResponse;
import com.mf.rpc.core.filter.Filter;
import okhttp3.*;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class RpcClient {

    static {
        ParserConfig.getGlobalInstance().addAccept("com.mf.rpc.api");
    }
    private final static MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

    public static <T> T create(Class<T> clazz, String url, Filter ...filters){
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new RpcInvocationHandler(clazz, url, filters));
    }

    public static class RpcInvocationHandler implements InvocationHandler{
        private Class<?> clazz;
        private String url;
        private Filter [] filters;

        public RpcInvocationHandler(Class<?> clazz, String url, Filter ...filters){
            this.clazz = clazz;
            this.url = url;
            this.filters = filters;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws IOException {

            RpcRequest request = RpcRequest.builder()
                    .clazzName(clazz.getName())
                    .method(method.getName())
                    .params(args)
                    .build();
            RpcResponse response = post(request, url);

            return JSON.parse(response.getResult().toString());
        }

        private RpcResponse post(RpcRequest request, String url) throws IOException {
            String body = JSON.toJSONString(request);
            System.out.println("request body is:" + body);

            OkHttpClient okHttpClient = new OkHttpClient();
            Request req = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(mediaType, body))
                    .build();

            String res= okHttpClient.newCall(req).execute().body().string();

            return JSON.parseObject(res, RpcResponse.class);

        }
    }
}
