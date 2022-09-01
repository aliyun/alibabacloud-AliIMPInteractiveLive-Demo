package com.aliyun.roompaas.uibase.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.aliyun.roompaas.base.AppContext;

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 基于Java动态代理的SharedPreferences实现<br/>
 * 为了方便上层快速实现sp读写能力
 *
 * @author puke
 * @version 2021/5/14
 */
public class SpHelper implements Serializable {

    private static final Map<Class<?>, Object> spInstances = new HashMap<>();

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Sp {
        String value() default "";
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Setter {
        String value() default "";
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Getter {
        String value() default "";

        String defValue() default "";
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Remove {
        String value() default "";
    }

    public static <T> T getInstance(Class<T> spType) {
        Object instance = spInstances.get(spType);
        if (instance == null) {
            Sp annotation = spType.getAnnotation(Sp.class);
            if (annotation == null) {
                throw new RuntimeException("No annotation");
            }

            String spValue = annotation.value();
            final String spName = TextUtils.isEmpty(spValue) ? spType.getName().toLowerCase() : spValue;
            instance = Proxy.newProxyInstance(spType.getClassLoader(), new Class[]{spType}, new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    if (method.isAnnotationPresent(Setter.class)) {
                        // setter 方法
                        if (parameterTypes.length != 1
                                || parameterTypes[0] != String.class) {
                            throw new RuntimeException("Invalid method parameter types: "
                                    + Arrays.toString(method.getParameterTypes()));
                        }

                        Setter setter = method.getAnnotation(Setter.class);
                        assert setter != null;
                        String key = getKey(method, "set", setter.value());
                        set(spName, key, (String) args[0]);
                        return null;
                    } else if (method.isAnnotationPresent(Getter.class)) {
                        // getter 方法
                        if (parameterTypes.length > 0) {
                            throw new RuntimeException("Invalid method parameter types: "
                                    + Arrays.toString(method.getParameterTypes()));
                        }

                        Getter getter = method.getAnnotation(Getter.class);
                        assert getter != null;
                        String key = getKey(method, "get", getter.value());
                        return get(spName, key, getter.defValue());
                    } else if (method.isAnnotationPresent(Remove.class)) {
                        // remove 方法
                        if (parameterTypes.length > 0) {
                            throw new RuntimeException("Invalid method parameter types: "
                                    + Arrays.toString(method.getParameterTypes()));
                        }

                        Remove remove = method.getAnnotation(Remove.class);
                        assert remove != null;
                        String key = getKey(method, "remove", remove.value());
                        remove(spName, key);
                        return null;
                    } else {
                        // unknown
                        throw new RuntimeException("Unknown method: " + method.getName());
                    }
                }

                String getKey(Method method, String prefix, String annotationValue) {
                    if (!TextUtils.isEmpty(annotationValue)) {
                        return annotationValue;
                    }

                    String methodName = method.getName();
                    String keyPartOfMethod = methodName.startsWith(prefix)
                            ? methodName.substring(prefix.length()) : methodName;
                    return keyPartOfMethod.toLowerCase();
                }
            });
            spInstances.put(spType, instance);
        }
        // noinspection unchecked
        return (T) instance;
    }

    private static SharedPreferences getSp(String spName) {
        return AppContext.getContext().getSharedPreferences(spName, Context.MODE_PRIVATE);
    }

    public static void set(String spName, String key, String value) {
        getSp(spName).edit().putString(key, value).apply();
    }

    public static void remove(String spName, String key) {
        getSp(spName).edit().remove(key).apply();
    }

    public static String get(String spName, String key, String defValue) {
        return getSp(spName).getString(key, defValue);
    }
}
