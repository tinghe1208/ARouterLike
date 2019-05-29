package com.arouter.like.moduleb;

import com.arouter.like.common.IRouter;

import java.util.Map;

public class BRouterImpl implements IRouter {

    private static final String BActivity_PATH = "router://b_activity";

    @Override
    public void register(Map<String, Class> routerMap) {
        routerMap.put(BActivity_PATH, BActivity.class);
    }
}
