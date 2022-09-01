package com.aliyun.roompaas.rtc;

import com.alibaba.dingpaas.room.RoomNotificationModel;
import com.aliyun.roompaas.base.AbstractPluginService;
import com.aliyun.roompaas.base.RoomContext;
import com.aliyun.roompaas.base.annotation.PluginServiceInject;
import com.aliyun.roompaas.rtc.exposable.ClassEventHandler;
import com.aliyun.roompaas.rtc.exposable.ClassService;

/**
 * @author puke
 * @version 2021/6/21
 */
@PluginServiceInject
public class ClassServiceImpl extends AbstractPluginService<ClassEventHandler> implements ClassService {

    private static final String TAG = ClassServiceImpl.class.getSimpleName();
    private static final String PLUGIN_ID = "class";

    private static final int TYPE_START = 1;
    private static final int TYPE_STOP = 2;
    private static final int TYPE_TASK_PUBLISH = 3;

    public ClassServiceImpl(RoomContext roomContext) {
        super(roomContext);
    }

    @Override
    public void onSyncEvent(RoomNotificationModel model) {
        switch (model.type) {
            case TYPE_START:
                dispatch(new Consumer<ClassEventHandler>() {
                    @Override
                    public void consume(ClassEventHandler eventHandler) {
                        eventHandler.onClassStart();
                    }
                });
                break;
            case TYPE_STOP:
                dispatch(new Consumer<ClassEventHandler>() {
                    @Override
                    public void consume(ClassEventHandler eventHandler) {
                        eventHandler.onClassStop();
                    }
                });
                break;
            case TYPE_TASK_PUBLISH:
                dispatch(new Consumer<ClassEventHandler>() {
                    @Override
                    public void consume(ClassEventHandler eventHandler) {
                        eventHandler.onTaskPublish();
                    }
                });
                break;
        }
    }

    @Override
    public String getPluginId() {
        return PLUGIN_ID;
    }
}
