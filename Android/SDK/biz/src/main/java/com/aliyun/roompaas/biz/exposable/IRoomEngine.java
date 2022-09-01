package com.aliyun.roompaas.biz.exposable;

import android.content.Context;

import com.alibaba.dingpaas.room.RoomBasicInfo;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.base.model.PageModel;
import com.aliyun.roompaas.biz.EngineConfig;
import com.aliyun.roompaas.biz.exposable.model.Result;
import com.aliyun.roompaas.biz.exposable.model.RoomParam;

/**
 * Created by KyleCe on 2021/7/8
 */
public interface IRoomEngine {

    void init(Context context, EngineConfig config, Callback<Void> callback);

    void setEventHandler(IRoomEngineEventHandler eventHandler);

    void auth(String userId, Callback<Void> callback);

    void logout(Callback<Void> callback);

    boolean isInit();

    boolean isLogin();
    
    String getUserId();

    Result<RoomChannel> getRoomChannel(String roomId);

    Result<RoomSceneLive> getRoomSceneLive();

    Result<RoomSceneClass> getRoomSceneClass();

    void getRoomList(RoomParam param, Callback<PageModel<RoomBasicInfo>> callback);
}
