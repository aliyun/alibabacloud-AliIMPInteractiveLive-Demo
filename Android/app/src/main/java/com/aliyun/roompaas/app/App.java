package com.aliyun.roompaas.app;

import android.app.Application;
import android.content.Context;

import com.aliyun.roompaas.app.sensitive.AllSensitive;
import com.aliyun.roompaas.base.callback.Callbacks;
import com.aliyun.roompaas.biz.EngineConfig;
import com.aliyun.roompaas.biz.RoomEngine;

/**
 * @author puke
 * @version 2021/5/8
 */
public class App extends Application {

    private static final String TAG = App.class.getSimpleName();

    private static App sApp;

    @Override
    public void onCreate() {
        super.onCreate();
        sApp = this;

        RoomEngine roomEngine = RoomEngine.getInstance();
        AllSensitive.environmentConcern(roomEngine);
        roomEngine.init(this,
                new EngineConfig.Builder()
                        .appId(Const.getAppId())
                        .appKey(Const.getAppKey())
                        .deviceId(Const.DEVICE_ID)
                        .build(),
                new Callbacks.Log<>(TAG, "init engine")
        );
    }

    public static App getApplication() {
        return sApp;
    }

    public static Context getAppContext() {
        return sApp.getApplicationContext();
    }

}
