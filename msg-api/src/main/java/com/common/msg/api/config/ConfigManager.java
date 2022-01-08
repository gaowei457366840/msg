package com.common.msg.api.config;

import com.common.msg.api.spring.PropertiesResolveService;
import com.common.msg.api.spring.SpringContextHolder;
import com.common.msg.api.util.PropertiesUtil;
import com.common.msg.api.util.StringUtil;

import java.util.Properties;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ConfigManager {
    private static final Logger log = LoggerFactory.getLogger(ConfigManager.class);

    private final PropertiesResolveService propertiesResolveService;

    private static final String DEFAULT_FILE = "za-msg.properties,kafka.properties,ons.properties";

    private static final String KEY_PREFIX_CONFIG = ".properties";

    private static String cachedSarName;

    private volatile Properties properties;


    private ConfigManager() {
        PropertiesResolveService propertiesResolveService1 = null;
        try {
            propertiesResolveService1 = (PropertiesResolveService) SpringContextHolder.getBean(PropertiesResolveService.class);
        } catch (IllegalStateException e) {
            log.info("[za-msg-api] Can't resolve spring 'beanFactory'.");
        }
        this.propertiesResolveService = propertiesResolveService1;
        this.properties = init();
    }

    private static class ConfigHolder {
        static ConfigManager instance = new ConfigManager();
    }

    private static ConfigManager getInstance() {
        return ConfigHolder.instance;
    }

    public static int getInt(String name, int value) {
        String val = getInstance().getValue(name);
        int result = value;
        if (!StringUtil.isNullOrEmpty(val)) {
            result = Integer.parseInt(val);
        }
        log.debug("Load property '" + name + "' = " + result);
        return result;
    }

    public static String getString(String name, String value) {
        String result = getInstance().getValue(name);
        if (StringUtil.isNullOrEmpty(result)) {
            result = value;
        }
        log.debug("Load property '" + name + "' = " + result);
        return result;
    }

    public static boolean getBoolean(String name, boolean value) {
        String val = getInstance().getValue(name);
        boolean result = value;
        if (!StringUtil.isNullOrEmpty(val)) {
            result = Boolean.valueOf(val).booleanValue();
        }
        log.debug("Load property '" + name + "' = " + result);
        return result;
    }

    public static long getLong(String name, long value) {
        String val = getInstance().getValue(name);
        long result = value;
        if (!StringUtil.isNullOrEmpty(val)) {
            result = Long.valueOf(val).longValue();
        }
        log.debug("Load property '" + name + "' = " + result);
        return result;
    }

    private String getValue(String name) {
        if (StringUtil.isNullOrEmpty(name)) {
            return null;
        }
        String value = null;
        try {
            if (this.propertiesResolveService != null) {
                value = this.propertiesResolveService.getPropertiesValue(name);
            }
        } catch (IllegalArgumentException e) {
            log.debug("Failed load property '" + name + "' from spring.");
        }
        if (this.properties != null && StringUtil.isNullOrEmpty(value)) {
            value = this.properties.getProperty(name);
        }
        return value;
    }

    public static void setValue(String key, Object val) {

        String val1 = "";

        if (val != null) {

            val1 = String.valueOf(val);
        }

        getInstance().innerSetValue(key, String.valueOf(val1));
    }


    public static String getSarName(String defaultValue) {
        if (!StringUtil.isNullOrEmpty(cachedSarName)) {
            return cachedSarName;
        }
        String applicationName = getString("spring.application.name", "");
        if (StringUtil.isNullOrEmpty(applicationName)) {
            String sarName = getString("sar.name", defaultValue);
            if (StringUtil.isNullOrEmpty(sarName)) {
                throw new NullPointerException("Please config \"sar.name\" or \"spring.application.name\" in properties.");
            }
            cachedSarName = sarName;
        } else {
            cachedSarName = applicationName;
        }
        return cachedSarName;
    }

    private void innerSetValue(String key, String val) {
        if (this.properties == null) {
            synchronized (ConfigManager.class) {
                if (this.properties == null) {
                    this.properties = new Properties();
                }
            }
        }

        this.properties.setProperty(key, val);
    }


    private Properties init() {

        Properties localProperties = null;

        StringTokenizer stringTokenizer = new StringTokenizer("za-msg.properties,kafka.properties,ons.properties", ",");
        while (stringTokenizer.hasMoreElements()) {
            if (localProperties == null) {
                localProperties = getProperties(stringTokenizer.nextToken());
                continue;
            }
            Properties newProperties = getProperties(stringTokenizer.nextToken());
            if (null != newProperties && newProperties.size() > 0) {
                localProperties.putAll(newProperties);
            }
        }


        log.info("[za-msg-api] Load properties succeed, props : " + ((localProperties != null) ? localProperties.toString() : null));
        return localProperties;
    }

    private static Properties getProperties(String path) {

        Properties properties = null;
        try {

            properties = PropertiesUtil.loadProperties(ConfigManager.class, path.trim());

            log.info("[za-msg-api] Succeed to load property file:" + path);

        } catch (Exception e) {

            log.info("[za-msg-api] Failed to load property file:" + path);
        }

        return properties;
    }
}


