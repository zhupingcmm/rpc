package com.mf.rpc.core.filter.impl;

import com.mf.rpc.core.filter.Filter;

public class FilterImpl implements Filter {

    @Override
    public boolean filter() {
        System.out.println("filter");
        return true;
    }
}
