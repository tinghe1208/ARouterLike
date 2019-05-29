package com.arouter.like.common;

import android.content.Context;
import android.content.Intent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Router {

    private static Router INSTANCE;
    private Map<String, Class> mRouterMap = new ConcurrentHashMap<>();

    //单例
    private static Router getInstance() {
        if (INSTANCE == null) {
            synchronized (Router.class) {
                if (INSTANCE == null) {
                    INSTANCE = new Router();
                }
            }
        }
        return INSTANCE;
    }

    private Router() {
        init();
    }

    private void init() { }

    /**
     * Activity跳转
     * @param context
     * @param activityUrl Activity路由路径。
     */
    public static void startActivity(Context context, String activityUrl) {
        Router router = getInstance();
        Class<?> targetActivityClass = router.mRouterMap.get(activityUrl);

        Intent intent = new Intent(context,targetActivityClass);
        context.startActivity(intent);
    }
}
