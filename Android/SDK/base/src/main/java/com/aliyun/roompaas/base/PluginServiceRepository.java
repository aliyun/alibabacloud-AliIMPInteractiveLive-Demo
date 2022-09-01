package com.aliyun.roompaas.base;

import android.text.TextUtils;

import com.aliyun.roompaas.base.exposable.PluginService;

import java.util.HashMap;
import java.util.Map;

/**
 * 插件服务仓库<hr/>
 * 核心方法:<br/>
 * 1. 注册映射关系 {@link #register} <br/>
 * 2. 读取映射关系 {@link #getPluginService} <br/>
 *
 * @author puke
 * @version 2021/6/21
 */
public class PluginServiceRepository {

    private static final Map<Class<? extends PluginService<?>>, Class<? extends PluginService<?>>> SERVICE_PLUGINS = new HashMap<>();

    /**
     * 注册插件<br/>
     * 参: {@link #register(String, String)}<br/>
     * 添加参数implName的缺省实现逻辑<br/>
     *
     * @param interfaceName 插件接口名称 (类全限定名)
     */
    public static void register(String interfaceName) {
        register(interfaceName, interfaceName + "Impl");
    }

    /**
     * 注册插件
     *
     * @param interfaceName 插件接口名称 (类全限定名)
     * @param implName      插件实现类名称 (类全限定名)
     */
    public static void register(String interfaceName, String implName) {
        if (TextUtils.isEmpty(interfaceName)) {
            throw new RuntimeException("The interface name is empty.");
        }

        if (TextUtils.isEmpty(implName)) {
            throw new RuntimeException("The implementation name is empty.");
        }

        Class<? extends PluginService<?>> interfaceType = getPluginService(interfaceName);
        Class<? extends PluginService<?>> implType = getPluginService(implName);

        register(interfaceType, implType);
    }


    /**
     * 注册插件
     *
     * @param interfaceType 插件接口
     * @param implType      插件实现类
     */
    public static void register(Class<? extends PluginService<?>> interfaceType,
                                Class<? extends PluginService<?>> implType) {
        SERVICE_PLUGINS.put(interfaceType, implType);
    }

    /**
     * @param interfaceType 接口名称 (类全限定名)
     * @return 插件实例类名 (类全限定名)
     */
    public static Class<? extends PluginService<?>> getPluginServiceType(Class<? extends PluginService<?>> interfaceType) {
        if (interfaceType == null) {
            throw new NullPointerException("The interface type is null.");
        }

        Class<? extends PluginService<?>> implType = SERVICE_PLUGINS.get(interfaceType);
        if (implType == null) {
            throw new RuntimeException("No plugin service implementation registered for " + interfaceType.getName());
        }

        return implType;
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends PluginService<?>> getPluginService(String typeName) {
        if (TextUtils.isEmpty(typeName)) {
            throw new RuntimeException("The type's name is empty.");
        }

        try {
            Class<?> type = Class.forName(typeName);

            if (!PluginService.class.isAssignableFrom(type)) {
                throw new RuntimeException("The type must be PluginService's sub class or interface.");
            }

            return ((Class<? extends PluginService<?>>) type);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("The type is incorrect.", e);
        }
    }
}
