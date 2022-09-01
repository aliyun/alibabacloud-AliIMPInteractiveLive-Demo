package com.aliyun.roompaas.biz.exposable;

/**
 * RoomEngine 事件回调
 */
public interface IRoomEngineEventHandler {

    void onEngineEvent(int event);

    interface EngineEvent {
        int INIT_SUCCESS = 0;
        int INIT_FAIL = 1;
        int LOGIN_SUCCESS = 2;
        int LOGIN_FAIL = 3;
        int LOGOUT_SUCCESS = 4;
        int LOGOUT_FAIL = 5;
        int KICK_OUT = 6;
    }
}
