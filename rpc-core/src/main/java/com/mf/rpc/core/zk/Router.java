package com.mf.rpc.core.zk;

import java.util.List;

public interface Router {
    String route(List<String> urls);
}
