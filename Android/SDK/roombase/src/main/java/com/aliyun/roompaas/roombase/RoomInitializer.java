package com.aliyun.roompaas.roombase;

import android.content.Context;
import android.support.annotation.Nullable;

import com.aliyun.roompaas.base.AppContext;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.base.log.ComposeLoggerHandler;
import com.aliyun.roompaas.base.log.DefaultLoggerHandler;
import com.aliyun.roompaas.base.log.FileLoggerHandler;
import com.aliyun.roompaas.base.log.Logger;
import com.aliyun.roompaas.base.util.CommonUtil;
import com.aliyun.roompaas.base.util.Utils;
import com.aliyun.roompaas.biz.EngineConfig;
import com.aliyun.roompaas.biz.RoomEngine;
import com.aliyun.roompaas.biz.exposable.TokenInfoGetter;
import com.aliyun.roompaas.biz.exposable.model.TokenInfo;
import com.aliyun.roompaas.roombase.api.GetTokenApi;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by KyleCe on 2021/10/27
 */
public class RoomInitializer<P extends BizInitParam> implements IRoomInitializer<P> {
    public static final String TAG = "RoomBaseInit";

    private final AtomicBoolean isInitialized = new AtomicBoolean(false);

    @Override
    public void init(Context context, P param, @Nullable Callback<Void> callback) {
        initProcess(context, param, callback);
    }

    private void initProcess(Context context, P param, @Nullable Callback<Void> callback) {
        if (param == null || Utils.anyEmpty(param.appId, param.appKey, param.userId, param.serverHost, param.serverSecret)) {
            Utils.callError(callback, "invalid param: " + param);
            isInitialized.set(false);
            return;
        }
        isInitialized.set(true);
        injectConfig(context, param);
        Utils.callSuccess(callback, null);
    }

    @Override
    public void injectConfig(Context context, P param) {
        Const.injectConfig(param);
    }

    @Override
    public void initAndLogin(final Callback<Void> callback) {
        if (!isInitialized.get()) {
            Utils.callError(callback, "initialize first");
            return;
        }
        RoomEngine roomEngine = RoomEngine.getInstance();
        try {
            Field bizType = RoomEngine.class.getDeclaredField("bizType");
            bizType.setAccessible(true);
            bizType.set(roomEngine, this.bizType);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Logger.e(TAG, e.getLocalizedMessage());
        }
        if (roomEngine.isInit()) {
            doLogin(callback);
        } else {
            Context context = AppContext.getContext();
            EngineConfig config = new EngineConfig.Builder()
                    .appId(Const.getAppId())
                    .appKey(Const.getAppKey())
                    .deviceId(CommonUtil.getDeviceId())
                    .tokenInfoGetter(new TokenInfoGetter() {
                        @Override
                        public void getTokenInfo(String userId, Callback<TokenInfo> callback) {
                            GetTokenApi.getToken(userId, callback);
                        }
                    })
                    .loggerHandler(new ComposeLoggerHandler(
                            new DefaultLoggerHandler(),
                            new FileLoggerHandler(context)
                    ))
                    .build();
            roomEngine.init(context, config, new Callback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    doLogin(callback);
                }

                @Override
                public void onError(String errorMsg) {
                    Logger.e(TAG, "dps engine init fail: " + errorMsg);
                    Utils.callError(callback, errorMsg);
                }
            });
        }
    }

    private void doLogin(final Callback<Void> callback) {
        RoomEngine.getInstance().auth(Const.getCurrentUserId(), new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Logger.i(TAG, "get token and auth success");
                Utils.callSuccess(callback, null);
            }

            @Override
            public void onError(String errorMsg) {
                Logger.e(TAG, "get token success, but auth fail: " + errorMsg);
                Utils.callError(callback, errorMsg);
            }
        });
    }

    @Override
    public void destroy() {
        isInitialized.set(false);
    }

    private String bizType;

    @Override
    public void setBizType(String bizType) {
        this.bizType = bizType;
    }
}
