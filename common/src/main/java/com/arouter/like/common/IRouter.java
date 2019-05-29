package com.arouter.like.common;

import java.util.Map;

public interface IRouter {

    void register(Map<String,Class> routerMap);

}
