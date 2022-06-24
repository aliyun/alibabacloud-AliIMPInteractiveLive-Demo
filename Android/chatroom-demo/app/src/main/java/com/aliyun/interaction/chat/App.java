package com.aliyun.interaction.chat;

import android.app.Application;
import android.content.Context;

import com.alibaba.dingpaas.base.DPSAuthToken;
import com.aliyun.roompaas.base.callback.Callbacks;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.biz.EngineConfig;
import com.aliyun.roompaas.biz.RoomEngine;
import com.aliyun.roompaas.biz.exposable.RoomChannel;
import com.aliyun.roompaas.biz.exposable.TokenInfoGetter;
import com.aliyun.roompaas.biz.exposable.model.Result;
import com.aliyun.roompaas.biz.exposable.model.TokenInfo;
import com.aliyun.roompaas.chat.SampleChatEventHandler;
import com.aliyun.roompaas.chat.exposable.ChatService;
import com.aliyun.roompaas.chat.exposable.event.CommentEvent;
import com.aliyun.roompaas.chat.exposable.event.CustomMessageEvent;

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
        roomEngine.init(this,
                new EngineConfig.Builder()
                        .appId(Const.APP_ID)
                        .appKey(Const.APP_KEY)
                        .deviceId(Const.DEVICE_ID)
                        .tokenInfoGetter(new TokenInfoGetter() {
                            @Override
                            public void getTokenInfo(String userId, Callback<TokenInfo> callback) {
                                GetTokenApi.getToken(userId, callback);
                            }
                        })
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
