package com.aliyun.roompaas.base;

import com.alibaba.dingpaas.base.DPSModuleInfo;
import com.alibaba.dingpaas.mps.MPSEngine;
import com.aliyun.roompaas.base.exposable.PluginService;
import com.aliyun.roompaas.base.log.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 模块注册器<hr/>
 * <p>
 * 1. 注册lwp网络模块<br/>
 * 2. 注册插件服务(对外)模块<br/>
 * 3. 注册内部服务(对内)模块<br/>
 *
 * @author puke
 * @version 2021/6/24
 */
public class ModuleRegister {

    private static final String TAG = ModuleRegister.class.getSimpleName();

    private static final List<String> NEED_LOAD_CLASS_BEFORE_ROOM_ENGINE_INIT = Arrays.asList(
            "com.aliyun.roompaas.chat.ChatServiceImpl",
            "com.aliyun.roompaas.document.DocumentServiceImpl",
            "com.aliyun.roompaas.document.DocLWPDelegate",
            "com.aliyun.roompaas.base.metaai.MetaAIDelegate", // need load class before room
            "com.aliyun.roompaas.live.LiveServiceImpl",
            "com.aliyun.roompaas.rtc.RtcServiceImpl",
            "com.aliyun.roompaas.rtc.ClassServiceImpl",
            "com.aliyun.roompaas.biz.RoomSceneClassImpl",
            "com.aliyun.roompaas.whiteboard.WhiteboardServiceImpl"
    );

    private static final String PLUGIN_SERVICE_PREFIX = "com.aliyun.roompaas";
    private static final String PLUGIN_PACKAGE_POSTFIX = "ServiceImpl";


    /**
     * 注册DPS网路模块
     *
     * @param dpsModuleInfo C++层lwp网络模块
     */
    public static void registerLwpModule(DPSModuleInfo dpsModuleInfo) {
        MPSEngine mpsEngine = MPSEngine.getMPSEngine();
        if (mpsEngine == null) {
            Logger.e(TAG, "The mpsEngine is null.");
        } else {
            mpsEngine.registerModule(dpsModuleInfo);
        }
    }

    /**
     * 扫描并加载插件模块<hr/>
     * 1. {@link Class#forName} 触发插件实现类的static代码块<br/>
     * 2. {@link PluginServiceRepository#register} 注册插件服务<br/>
     */
    public static void scanAndLoadPluginModule() {
        try {
            long start = System.currentTimeMillis();

            // 扫描所有的需加载类
            List<Class<?>> classTypes = scanAllNeedLoadClassTypes();

            // 注册插件服务模块
            registerPluginServices(classTypes);

            // 耗时统计
            long duration = System.currentTimeMillis() - start;
            Logger.i(TAG, String.format("Call loadPluginModuleAuto take %s ms.", duration));
        } catch (Exception e) {
            Logger.e(TAG, "Call loadPluginModuleAuto failure.", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static void registerPluginServices(List<Class<?>> serviceImplTypes) {
        for (Class<?> serviceImplType : serviceImplTypes) {
            try {
                Class<?>[] interfaces = serviceImplType.getInterfaces();
                for (Class<?> anInterface : interfaces) {
                    if (PluginService.class.isAssignableFrom(anInterface)) {
                        Class<? extends PluginService<?>> interfaceType = (Class<? extends PluginService<?>>) anInterface;
                        Class<? extends PluginService<?>> implType = (Class<? extends PluginService<?>>) serviceImplType;
                        // 注册具体的插件服务
                        PluginServiceRepository.register(interfaceType, implType);
                        break;
                    }
                }
            } catch (Throwable t) {
                Logger.e(TAG, String.format("Register %s failure.", serviceImplType.getName()), t);
            }
        }
    }

    private static List<Class<?>> scanAllNeedLoadClassTypes() {
        List<Class<?>> serviceTypes = new ArrayList<>();
        for (String serviceTypeName : NEED_LOAD_CLASS_BEFORE_ROOM_ENGINE_INIT) {
            try {
                long time1 = System.currentTimeMillis();
                Class<?> type = Class.forName(serviceTypeName); // 关键动作：触发static注册模块方法
                long time2 = System.currentTimeMillis();
                Logger.i(TAG, String.format("加载 %s take %s ms.", type.getSimpleName(), time2 - time1));
                serviceTypes.add(type);
            } catch (ClassNotFoundException t) {
                Logger.e(TAG, String.format("No %s found.", serviceTypeName));
            }
        }
        return serviceTypes;
    }
}
