package com.mf.rpc.core.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.mf.rpc.core.api.RpcRequest;
import com.mf.rpc.core.api.RpcResponse;
import com.mf.rpc.core.filter.Filter;
import com.mf.rpc.core.zk.Impl.RouterImpl;
import com.mf.rpc.core.zk.Router;
import com.mf.rpc.core.zk.ZkSerializer;
import lombok.Getter;
import lombok.Setter;
import okhttp3.*;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class RpcClient {

    static {
        ParserConfig.getGlobalInstance().addAccept("com.mf.rpc.api");
    }
    private final static MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
    private final static String ZK_ROOT = "/rpc-consumer";

    @Getter
    @Setter
    private Map<String, String> servers = new ConcurrentHashMap();
    @Getter
    @Setter
    private String zkUrl;
    public RpcClient (String zkUrl) {
        this.zkUrl = zkUrl;
        initConnectZk();
    }

    private void initConnectZk () {
        ZkClient zkClient = new ZkClient("localhost:2181");
        zkClient.setZkSerializer(new ZkSerializer());
        if (!zkClient.exists(ZK_ROOT)) {
            throw new RuntimeException("System error");
        }

        List<String> children =zkClient.getChildren(ZK_ROOT);
        children.forEach(c -> {
            String childrenPath = ZK_ROOT + "/" + c;
            String url = zkClient.readData(childrenPath);
            servers.put(childrenPath, url);

            IZkDataListener dataListener = new IZkDataListener() {
                @Override
                public void handleDataChange(String s, Object o) throws Exception {
                    System.out.println("zk change data" + s);
                    servers.put(s, (String) o);
                }

                @Override
                public void handleDataDeleted(String s) throws Exception {
                    System.out.println("zk delete data");
                    servers.remove(s);
                }
            };
            zkClient.subscribeDataChanges(childrenPath, dataListener);
        });
    }

    public <T> T createFromRegistry(final Class<T> clazz,  Filter ...filters) {

        Router router = new RouterImpl();
        List<String> urls = this.getServers()
                .keySet()
                .stream()
                .map(key -> this.getServers().get(key))
                .collect(Collectors.toList());
        String url = router.route(urls);
        return create(clazz, url, filters);
    }

    public <T> T create(Class<T> clazz, String url, Filter ...filters){
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
