package com.aliyun.roompaas.base;

import com.aliyun.roompaas.base.exposable.PluginService;

import java.util.List;

/**
 * 插件服务工厂类, 对外提供插件实例
 *
 * @author puke
 * @version 2021/6/21
 */
public interface PluginServiceFactory {

    /**
     * @param <PS>              具象的插件类
     * @param pluginServiceType 插件服务类型
     * @param roomContext       房间上下文
     * @return 插件服务实例
     */
    <PS extends PluginService<?>> PS getPluginService(Class<PS> pluginServiceType, RoomContext roomContext);

    /**
     * @return 获取全部的插件实例列表
     */
    List<PluginService<?>> getAllPluginService();
}
