package com.common.msg.api.util;

import java.lang.ref.SoftReference;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class TypeReferenceUtil {
    private static final String DEFAULT_METHOD_NAME = "onMessage";
    private static ConcurrentMap<String, SoftReference<Type>> types = new ConcurrentHashMap<>();

    public static Type getType(Object obj) {

        return getType(obj, "onMessage");
    }

    public static Type getType(Object obj, String methodName) {
        String fullName = obj.getClass().getName() + "." + methodName;
        if (types.containsKey(fullName) && (
                (SoftReference) types.get(fullName)).get() != null) {
            return ((SoftReference<Type>) types.get(fullName)).get();
        }

        return createType(obj, methodName);
    }

    private static synchronized Type createType(Object obj, String methodName) {
        String fullName = obj.getClass().getName() + "." + methodName;
        if (types.containsKey(fullName) && (
                (SoftReference) types.get(fullName)).get() != null) {
            return ((SoftReference<Type>) types.get(fullName)).get();
        }


        Method method = getMethod(obj, methodName);
        Type type = ((ParameterizedType) method.getGenericParameterTypes()[0]).getActualTypeArguments()[0];
        types.put(fullName, new SoftReference<>(type));
        return type;
    }

    private static Method getMethod(Object obj, String methodName) {
        Method[] methods = obj.getClass().getMethods();
        Method method = null;
        for (Method m : methods) {
            if (m.getName().equals(methodName)) {
                method = m;
                break;
            }
        }
        return method;
    }
}


