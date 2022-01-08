package com.common.msg.api.util;

import com.common.msg.api.bootstrap.Destroyable;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DestroyableUtil {
    private static final Logger log = LoggerFactory.getLogger(DestroyableUtil.class);


    private static Map<String, Destroyable> destroyableMap = new HashMap<>();

    public static void add(String key, Destroyable object) {
        destroyableMap.put(key, object);
        log.info("Hook destroyable object: " + key);
    }

    public static void destroy() {
        for (Map.Entry<String, Destroyable> entry : destroyableMap.entrySet()) {
            ((Destroyable) entry.getValue()).destroy();
            log.info("Destroy object: " + (String) entry.getKey());
        }
        destroyableMap.clear();
    }
}


