package com.aliyun.roompaas.base.inner;

import com.aliyun.roompaas.base.RoomContext;
import com.aliyun.roompaas.base.inner.module.LiveInnerService;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * SDK内部插件间通信管理器<hr/>
 * 1. 插件之间是相互独立的并列关系, 不能相互调用<br/>
 * 2. 需要调用时, 通过下沉{@link InnerService}到base模块的方式<br/>
 * <p>
 * 参{@link LiveInnerService}
 *
 * @author puke
 * @version 2021/6/24
 */
@SuppressWarnings("unchecked")
public class InnerServiceManager {

    private static final Map<Class<? extends InnerService>, Class<? extends InnerService>> IMPL_MAPPING = new HashMap<>();

    private final Map<Class<? extends InnerService>, InnerService> instances = new HashMap<>();

    public static void register(String interfaceName, String implName) {
        try {
            Class<?> interfaceType = Class.forName(interfaceName);
            Class<?> implType = Class.forName(implName);
            register(
                    (Class<? extends InnerService>) interfaceType,
                    (Class<? extends InnerService>) implType
            );
        } catch (ClassNotFoundException e) {
            String format = String.format("Register failure, interface name is %s, impl name is %s",
                    interfaceName, implName);
            throw new RuntimeException(format, e);
        }
    }

    public static void register(Class<? extends InnerService> interfaceType,
                                Class<? extends InnerService> implType) {
        IMPL_MAPPING.put(interfaceType, implType);
    }

    public <IS extends InnerService> IS getService(Class<IS> innerServiceType, RoomContext roomContext) {
        InnerService innerService = instances.get(innerServiceType);
        if (innerService != null) {
            return (IS) innerService;
        }

        Class<? extends InnerService> implType = IMPL_MAPPING.get(innerServiceType);
        if (implType == null) {
            throw new RuntimeException(
                    String.format("No service registered for %s.", innerServiceType.getName())
            );
        }

        try {
            Constructor<IS> constructor = (Constructor<IS>) implType.getConstructor(RoomContext.class);
            IS instance = constructor.newInstance(roomContext);
            this.instances.put(innerServiceType, instance);
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Create inner service instance failure.", e);
        }
    }
}
