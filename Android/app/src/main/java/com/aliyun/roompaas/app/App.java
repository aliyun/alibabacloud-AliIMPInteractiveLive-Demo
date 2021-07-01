package com.aliyun.roompaas.app;

import android.app.Application;

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

        final RoomEngine roomEngine = RoomEngine.getInstance();

        roomEngine.init(this,
                new EngineConfig.Builder()
                        .appId(Const.APP_ID)
                        .appKey(Const.APP_KEY)
                        .longLinkUrl(Const.LONG_LINK_URL)
                        .envType(Const.ENV_TYPE)
                        .deviceId(Const.DEVICE_ID)
                        .build(),
                new Callbacks.Log<>(TAG, "init engine")
        );
    }

    public static App getApplication() {
        return sApp;
    }
}
