package com.arouter.like.modulea;

import com.arouter.like.common.IRouter;

import java.util.Map;

public class ARouterImpl implements IRouter {

    private static final String AActivity_PATH = "router://a_activity";

    @Override
    public void register(Map<String, Class> routerMap) {
        routerMap.put(AActivity_PATH, AActivity.class);
    }
}
