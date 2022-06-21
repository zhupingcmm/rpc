package com.mf.rpc.core.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.mf.rpc.core.api.RpcRequest;
import com.mf.rpc.core.api.RpcResponse;
import com.mf.rpc.core.exception.ExceptionEnum;
import com.mf.rpc.core.exception.RpcException;
import com.mf.rpc.core.filter.Filter;
import com.mf.rpc.core.zk.Impl.RouterImpl;
import com.mf.rpc.core.zk.Router;
import com.mf.rpc.core.zk.ZkSerializer;
import lombok.Getter;
import lombok.Setter;
import okhttp3.*;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

public class RpcClient {

    private static Logger logger = LogManager.getLogger(RpcClient.class);

    static {
        ParserConfig.getGlobalInstance().addAccept("com.mf.rpc.api");
    }

    private static final MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
    private static final String ZK_ROOT = "/rpc-consumer";

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

    /**
     * @description connect to zk, get the backend servers url, add listener with these node in zk
     */
    private void initConnectZk () {
        logger.info("start to connect zk: %s");
        ZkClient zkClient = new ZkClient("localhost:2181");
        zkClient.setZkSerializer(new ZkSerializer());
        if (!zkClient.exists(ZK_ROOT)) {
            throw new RpcException(ExceptionEnum.SERVER_NOT_READY);
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
        // 从zk上获取所有的后台地址
        List<String> urls = this.getServers()
                .keySet()
                .stream()
                .map(key -> this.getServers().get(key))
                .collect(Collectors.toList());
        // 进行路由，从中间取出一个合适的地址，进行路由转发
        Router router = new RouterImpl();
        String url = router.route(urls);

        return create(clazz, url, filters);
    }

    private  <T> T create(Class<T> clazz, String url, Filter ...filters){
        // jdk 动态代理
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new RpcInvocationHandler(clazz, url, filters));
    }

    public static class RpcInvocationHandler implements InvocationHandler{

        @Setter
        @Getter
        private RpcResponse rpcResponse;

        private Class<?> clazz;
        private String url;
        private Filter [] filters;

        public RpcInvocationHandler(Class<?> clazz, String url, Filter ...filters){
            this.clazz = clazz;
            this.url = url;
            this.filters = filters;
        }

        /**
         *
         * @param proxy the proxy instance that the method was invoked on
         *
         * @param method the {@code Method} instance corresponding to
         * the interface method invoked on the proxy instance.  The declaring
         * class of the {@code Method} object will be the interface that
         * the method was declared in, which may be a superinterface of the
         * proxy interface that the proxy class inherits the method through.
         *
         * @param args an array of objects containing the values of the
         * arguments passed in the method invocation on the proxy instance,
         * or {@code null} if interface method takes no arguments.
         * Arguments of primitive types are wrapped in instances of the
         * appropriate primitive wrapper class, such as
         * {@code java.lang.Integer} or {@code java.lang.Boolean}.
         *
         * @return 远程调用的结果，也就是方法调用的结果
         * @throws IOException
         */
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws IOException {

            RpcRequest request = RpcRequest.builder()
                    .clazzName(clazz.getName())
                    .method(method.getName())
                    .params(args)
                    .build();
            // 进行过滤
            if (filters != null) {
                for (Filter filter: filters) {
                    if (!filter.filter()) {
                        return null;
                    }
                }
            }
            // 执行方法调用
            post(request, url);

            return JSON.parse(this.rpcResponse.getResult().toString());
        }

        private void handleResponse(Response response) {
            try {
                this.rpcResponse = JSON.parseObject(response.body().string(), RpcResponse.class) ;
            } catch (IOException e) {
                throw new RpcException(ExceptionEnum.SYSTEM_ERROR);
            }
        }

        private void post(RpcRequest request, String url) throws IOException {
            String body = JSON.toJSONString(request);
            System.out.println("request body is:" + body);
            CountDownLatch countDownLatch = new CountDownLatch(1);

            OkHttpClient okHttpClient = new OkHttpClient();
            Request req = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(mediaType, body))
                    .build();

            okHttpClient.newCall(req).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    System.out.println(e);
                    countDownLatch.countDown();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    handleResponse(response);
                    countDownLatch.countDown();
                }
            });
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                throw new RpcException(ExceptionEnum.SYSTEM_ERROR);
            }
        }
    }
}
