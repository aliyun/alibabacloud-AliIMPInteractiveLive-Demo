package com.aliyun.roompaas.base;

import com.aliyun.roompaas.base.exposable.PluginService;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 插件服务工厂的默认实现类<hr/>
 * 插件的"服务-实例"映射关系是通过{@link PluginServiceRepository}注册得到的
 *
 * @author puke
 * @version 2021/6/21
 */
public class DefaultPluginServiceFactory implements PluginServiceFactory {

    private final Map<Class<? extends PluginService<?>>, PluginService<?>> implInstances = new HashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <PS extends PluginService<?>> PS getPluginService(Class<PS> pluginServiceType, RoomContext roomContext) {
        // 考虑反射性能消耗, 优先读取内存缓存的
        PluginService<?> pluginService = implInstances.get(pluginServiceType);
        if (pluginService != null) {
            return ((PS) pluginService);
        }

        Class<? extends PluginService<?>> implType = PluginServiceRepository.getPluginServiceType(pluginServiceType);
        try {
            // 内存缓存中不存在实例时, 通过反射进行创建
            Constructor<? extends PluginService<?>> constructor = implType.getConstructor(RoomContext.class);
            PluginService<?> newInstance = constructor.newInstance(roomContext);
            // 创建完成后, 添加到内存缓存中, 后续直接获取
            implInstances.put(pluginServiceType, newInstance);
            return ((PS) newInstance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Can't call the constructor, the modifier is public?", e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("No correct constructor found.", e);
        } catch (InstantiationException e) {
            throw new RuntimeException("Create plugin service instance error.", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Create plugin service instance error.", e);
        }
    }

    @Override
    public List<PluginService<?>> getAllPluginService() {
        return new ArrayList<>(implInstances.values());
    }
}
