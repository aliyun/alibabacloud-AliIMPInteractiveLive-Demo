package com.aliyun.roompaas.base.monitor;

import android.os.Build;
import android.text.TextUtils;

import com.alibaba.dingpaas.cloudconfig.GetSlsConfigRsp;
import com.alibaba.dingpaas.cloudconfig.SlsReportMode;
import com.alibaba.dingpaas.monitorhub.MonitorhubBizType;
import com.alibaba.dingpaas.monitorhub.MonitorhubDeviceType;
import com.alibaba.dingpaas.monitorhub.MonitorhubAppInfo;
import com.alibaba.dingpaas.monitorhub.MonitorhubEvent;
import com.alibaba.dingpaas.monitorhub.MonitorhubField;
import com.alibaba.dingpaas.monitorhub.MonitorhubHeartbeatCallback;
import com.alibaba.dingpaas.monitorhub.MonitorhubModule;
import com.alibaba.dingpaas.monitorhub.MonitorhubNetType;
import com.alibaba.dingpaas.monitorhub.MonitorhubProcedure;
import com.alibaba.dingpaas.monitorhub.MonitorhubReportConfig;
import com.alibaba.dingpaas.monitorhub.MonitorhubReportModel;
import com.alibaba.dingpaas.monitorhub.MonitorhubSlsConfigModel;
import com.alibaba.dingpaas.monitorhub.MonitorhubStsTokenModel;
import com.alibaba.dingpaas.monitorhub.MonitorhubTraceType;
import com.alibaba.dingpaas.rtc.ConfUserModel;
import com.aliyun.roompaas.base.AppContext;
import com.aliyun.roompaas.base.BuildConfig;
import com.aliyun.roompaas.base.log.Logger;
import com.aliyun.roompaas.base.util.NetUtils;
import com.aliyun.roompaas.base.util.Utils;
import com.aliyun.roompaas.base.util.CommonUtil;

import java.util.HashMap;
import java.util.List;

import static com.aliyun.roompaas.base.util.NetUtils.NETWORK_2G;
import static com.aliyun.roompaas.base.util.NetUtils.NETWORK_3G;
import static com.aliyun.roompaas.base.util.NetUtils.NETWORK_4G;
import static com.aliyun.roompaas.base.util.NetUtils.NETWORK_NONE;
import static com.aliyun.roompaas.base.util.NetUtils.NETWORK_WIFI;
import static com.aliyun.roompaas.base.util.NetUtils.getNetworkState;

/**
 * 数据采集上报
 */
public class MonitorHubChannel {
    private static final String TAG = MonitorHubChannel.class.getSimpleName();
    private static final String SEPARATOR = ",";
    public static final int REPORT_EVENT_SUCCESS_CODE = 0;
    public static final int REPORT_EVENT_ERROR_CODE = -1;


    public static void initMonitorHub(GetSlsConfigRsp slsConfig) {
        MonitorhubReportConfig monitorhubReportConfig = new MonitorhubReportConfig();
        monitorhubReportConfig.heartbeatIntervalS = slsConfig.heartbeatInterval;
        monitorhubReportConfig.reportMode = (slsConfig.reportMode != SlsReportMode.WEBTRACKING ? MonitorhubReportModel.SLS_SDK : MonitorhubReportModel.SLS_WEBTRACKING);
        MonitorhubStsTokenModel monitorhubStsTokenModel = new MonitorhubStsTokenModel(slsConfig.stsToken.accessKeyId, slsConfig.stsToken.accessKeySecret, slsConfig.stsToken.securityToken, slsConfig.stsToken.expireTime);
        monitorhubReportConfig.slsConfig = new MonitorhubSlsConfigModel(slsConfig.slsConfig.endpoint, slsConfig.slsConfig.project, slsConfig.slsConfig.logStore, monitorhubStsTokenModel);

        MonitorhubModule module = MonitorhubModule.getMonitorhubModule();
        module.setConfig(monitorhubReportConfig);

        Logger.i(TAG, "initMonitorHub init.");
        setDefaultAppInfo();
        setHeartbeatCallback(new MonitorhubHeartbeatCallback() {
            @Override
            public HashMap<String, String> onHeartbeatProcess() {
                Logger.i(TAG, "onHeartbeatProcess");
                return MonitorHeartbeatManager.getInstance().getHeartbeatData();
            }
        });
    }

    public static void unInitMonitorHub() {
        Logger.i(TAG, "unInitMonitorHub un init.");
        MonitorhubModule.getMonitorhubModule().uninitMonitorhubModule();
    }

    public static MonitorhubAppInfo getAppInfo() {
        MonitorhubAppInfo appInfo = MonitorhubModule.getMonitorhubModule().getAppInfo();
        return appInfo;
    }

    // region 公共参数
    public static void setAppId(String appId) {
        getAppInfo().setAppId(appId);
    }

    public static void setAppName(String appName) {
        getAppInfo().setAppName(appName);
    }

    public static void setUid(String uid) {
        getAppInfo().setUid(uid);
    }

    public static void setRoomId(String roomId) {
        getAppInfo().setRoomId(roomId);
    }

    public static void setBizType(String bizType) {
        getAppInfo().setBizType(bizType);
    }

    public static void setBizId(String bizId) {
        getAppInfo().setBizId(bizId);
    }

    public static void setDeviceId(String deviceId) {
        getAppInfo().setDeviceId(deviceId);
    }

    public static String getBizType() {
        return getAppInfo().getBizType();
    }
    // endregion

    private static void setDefaultAppInfo() {
        MonitorhubAppInfo appInfo = getAppInfo();
        try {
            appInfo.setOsName(Build.VERSION_CODES.class.getFields()[Build.VERSION.SDK_INT].getName());
        } catch (Exception e) {
            appInfo.setOsName(String.valueOf(Build.VERSION.SDK_INT));
        }
        appInfo.setDeviceName(Build.MODEL);
        appInfo.setDeviceType(MonitorhubDeviceType.ANDROID);
        appInfo.setOsVersion(String.valueOf(Build.VERSION.SDK_INT));
        appInfo.setAppName(CommonUtil.getAppName(AppContext.getContext()));
        appInfo.setAppVersion(CommonUtil.getVersionCode(AppContext.getContext()));
        appInfo.setPaasSdkVersion(BuildConfig.VERSION_NAME);
        appInfo.setNetType(getNetTypeCategory(NetUtils.getNetworkState(AppContext.getContext())));
    }

    public static void reportNormalEvent(MonitorhubEvent eventId, HashMap<String, String> extraFields, long errorCode, String errorMsg) {
        MonitorhubModule.getMonitorhubModule().reportNormalEvent(eventId, extraFields, errorCode, errorMsg);
    }

    public static void reportTraceEvent(MonitorhubTraceType traceType, String traceId, MonitorhubProcedure procedureId, MonitorhubEvent eventId, HashMap<String, String> extraFields, long errorCode, String errorMsg) {
        MonitorhubModule.getMonitorhubModule().reportTraceEvent(traceType, traceId, procedureId, eventId, extraFields, errorCode, errorMsg);
    }

    public static void setHeartbeatCallback(MonitorhubHeartbeatCallback callback) {
        MonitorhubModule.getMonitorhubModule().setHeartbeatCallback(callback);
    }

    // region 课堂事件
    public static void reportStartClass(int errorCode, String errorMsg) {
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_PAASSDK_RTC_START_CLASS, null, errorCode, errorMsg);
    }

    public static void reportSopClass(int errorCode, String errorMsg) {
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_PAASSDK_RTC_STOP_CLASS, null, errorCode, errorMsg);
    }

    public static void reportEnterClass(int errorCode, String errorMsg) {
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_PAASSDK_RTC_ENTER_CLASS, null, errorCode, errorMsg);
    }

    public static void reportExitClass(int errorCode, String errorMsg) {
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_PAASSDK_RTC_EXIT_CLASS, null, errorCode, errorMsg);
    }

    public static void reportLivePlay(int errorCode, String errorMsg) {
        if (MonitorhubBizType.STANDARD_CLASS.equals(getBizType())) {
            reportShowStream(errorCode, errorMsg);
        } else if (MonitorhubBizType.STANDARD_LIVE.equals(getBizType())) {
            reportPlayLivePlay(errorCode, errorMsg);
        }
    }

    public static void reportPlayLivePlay(int errorCode, String errorMsg) {
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_CLIENT_PLAY_LIVE_PLAY,
                null, 0, null);
    }

    public static void reportLiveStop(int errorCode, String errorMsg) {
        if (MonitorhubBizType.STANDARD_CLASS.equals(getBizType())) {
            reportStopStream(errorCode, errorMsg);
        } else if (MonitorhubBizType.STANDARD_LIVE.equals(getBizType())) {
            reportPlayLiveStop(errorCode, errorMsg);
        }
    }

    public static void reportPlayLiveStop(int errorCode, String errorMsg) {
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_CLIENT_PLAY_LIVE_STOP,
                null, 0, null);
    }

    public static void reportShowStream(int errorCode, String errorMsg) {
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_PAASSDK_RTC_SHOW_STREAM, null, errorCode, errorMsg);
    }

    public static void reportStopStream(int errorCode, String errorMsg) {
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_PAASSDK_RTC_STOP_STREAM, null, errorCode, errorMsg);
    }

    public static void reportStartPreview(int errorCode, String errorMsg) {
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_PAASSDK_RTC_START_PREVIEW, null, errorCode, errorMsg);
    }

    public static void reportStopPreview(int errorCode, String errorMsg) {
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_PAASSDK_RTC_STOP_PREVIEW, null, errorCode, errorMsg);
    }

    public static void reportCreateRTC(String rtcId, int errorCode, String errorMsg) {
        HashMap<String, String> params = null;
        if (rtcId != null) {
            params = new HashMap<>(1);
            params.put(MonitorhubField.MFFIELD_PAASSDK_RTC_ACT_RTC_ID, rtcId);
        }
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_PAASSDK_RTC_CREATE_RTC,
                params, errorCode, errorMsg);
    }

    public static void reportDestroyRTC(String rtcId, int errorCode, String errorMsg) {
        HashMap<String, String> params = null;
        if (rtcId != null) {
            params = new HashMap<>(1);
            params.put(MonitorhubField.MFFIELD_PAASSDK_RTC_ACT_RTC_ID, rtcId);
        }
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_PAASSDK_RTC_DESTORY_RTC, params, errorCode, errorMsg);
    }

    public static void reportPushLiveStream(String rtcId, String pushUrl, int errorCode, String errorMsg) {
        HashMap<String, String> params = null;
        if (rtcId != null) {
            params = new HashMap<>(2);
            params.put(MonitorhubField.MFFIELD_PAASSDK_RTC_ACT_RTC_ID, rtcId);
            params.put(MonitorhubField.MFFIELD_COMMON_PUSH_URL, pushUrl);
        }
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_PAASSDK_RTC_PUSH_LIVESTREAM, null, errorCode, errorMsg);
    }

    public static void reportJoinChannel(String rtcId, int errorCode, String errorMsg) {
        HashMap<String, String> params = null;
        if (rtcId != null) {
            params = new HashMap<>(1);
            params.put(MonitorhubField.MFFIELD_PAASSDK_RTC_ACT_RTC_ID, rtcId);
        }
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_PAASSDK_RTC_SDK_JOIN_CHANNEL, params, errorCode, errorMsg);
    }

    public static void reportLeaveChannel(String rtcId, int errorCode, String errorMsg) {
        HashMap<String, String> params = null;
        if (rtcId != null) {
            params = new HashMap<>(1);
            params.put(MonitorhubField.MFFIELD_PAASSDK_RTC_ACT_RTC_ID, rtcId);
        }
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_PAASSDK_RTC_SDK_LEAVE_CHANNEL, params, errorCode, errorMsg);
    }

    public static void reportInviteJoinRTC(String rtcId, List<ConfUserModel> userList, int errorCode, String errorMsg) {
        HashMap<String, String> params = new HashMap<>(2);;
        if (rtcId != null) {
            params.put(MonitorhubField.MFFIELD_PAASSDK_RTC_ACT_RTC_ID, rtcId);
        }
        if (userList != null) {
            params.put(MonitorhubField.MFFIELD_PAASSDK_RTC_USER_LIST, String.format("[%s]", TextUtils.join(SEPARATOR, userList)));
        }
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_PAASSDK_RTC_INVITE_JOIN_RTC, params, errorCode, errorMsg);
    }

    public static void reportKickMembers(String rtcId, List<String> userList, int errorCode, String errorMsg) {
        HashMap<String, String> params = new HashMap<>(2);
        StringBuilder stringBuilder = new StringBuilder();
        if (rtcId != null) {
            params.put(MonitorhubField.MFFIELD_PAASSDK_RTC_ACT_RTC_ID, rtcId);
        }
        if (userList != null) {
            params.put(MonitorhubField.MFFIELD_PAASSDK_RTC_USER_LIST, String.format("[%s]", TextUtils.join(SEPARATOR, userList)));
        }
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_PAASSDK_RTC_KICK_MEMBERS, params, errorCode, errorMsg);
    }

    public static void reportStartRecord(int errorCode, String errorMsg) {
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_PAASSDK_RTC_START_RECORD, null, errorCode, errorMsg);
    }

    public static void reportStopRecord(int errorCode, String errorMsg) {
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_PAASSDK_RTC_STOP_RECORD, null, errorCode, errorMsg);
    }

    public static void reportFirstFrameRender(HashMap<String, String> params, int errorCode, String errorMsg) {
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_PAASSDK_RTC_FIRST_FRAME_RENDER, params, errorCode, errorMsg);
    }

    public static void reportRTCError(int errorCode, String errorMsg) {
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_PAASSDK_RTC_RTC_ERROR, null, errorCode, errorMsg);
    }

    public static void reportPerfLow(int errorCode, String errorMsg) {
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_PAASSDK_RTC_PERFORMANCE_LOW, null, errorCode, errorMsg);
    }

    public static void reportPerfNormal(int errorCode, String errorMsg) {
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_PAASSDK_RTC_PERFORMANCE_NORMAL, null, errorCode, errorMsg);
    }

    public static void reportNetDisconnect(int errorCode, String errorMsg) {
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_PAASSDK_RTC_NET_DISCONNECT, null, errorCode, errorMsg);
    }

    public static void reportNetReconnect(int errorCode, String errorMsg) {
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_PAASSDK_RTC_NET_RECONNECT, null, errorCode, errorMsg);
    }

    public static void reportNetReconnectSuccess(int errorCode, String errorMsg) {
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_PAASSDK_RTC_NET_RECONNECT_SUC, null, errorCode, errorMsg);
    }

    public static void reportNetChange(int errorCode, String errorMsg) {
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_PAASSDK_RTC_NET_CHANGED, null, errorCode, errorMsg);
    }

    public static void reportGetRTCToken(String rtcId, int errorCode, String errorMsg) {
        HashMap<String, String> params = new HashMap<>(1);
        params.put(MonitorhubField.MFFIELD_PAASSDK_RTC_LIVE_ID, rtcId);
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_PAASSDK_RTC_GET_RTC_TOKEN, params, errorCode, errorMsg);
    }
    public static void reportRemoteOffline(int reason, int errorCode, String errorMsg) {
        HashMap<String, String> params = new HashMap<>(1);
        params.put(MonitorhubField.MFFIELD_COMMON_REASON, String.valueOf(reason));
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_PAASSDK_RTC_REMOTE_OFFLINE, params, errorCode, errorMsg);
    }
    // endregion

    // region MetaPath
    public enum ACT_CONN_STATE {
        NO_CON(0),
        CONNING(1),
        CONNED(2),
        LOGIN(3),
        LOGIN_SUC(4);

        private int value;

        ACT_CONN_STATE(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }

    public static void reportConnStateChange(int state, int engineType, int netType, int errorCode, String errorMsg) {
        HashMap<String, String> params = new HashMap<>(3);
        params.put(MonitorhubField.MFFIELD_METAPATH_CLIENT_LINK_ACT_CONN_STATE, String.valueOf(state));
        params.put(MonitorhubField.MFFIELD_METAPATH_CLIENT_LINK_ACT_ENGINE_TYPE, String.valueOf(engineType));
        params.put(MonitorhubField.MFFIELD_METAPATH_CLIENT_LINK_ACT_NET_TYPE, getNetTypeCategory(netType));
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_METAPATH_CLIENT_LINK_CONN_STATE_CHANGE, params, errorCode, errorMsg);
    }

    private static String getNetTypeCategory(int netType) {
        switch (netType) {
            case NETWORK_NONE:
                return MonitorhubNetType.NET_NO_WIRE;
            case NETWORK_2G:
            case NETWORK_3G:
                return MonitorhubNetType.NET_3G;
            case NETWORK_4G:
                return MonitorhubNetType.NET_4G;
            case NETWORK_WIFI:
                return MonitorhubNetType.NET_WIFI;
        }
        return MonitorhubNetType.NET_UNKNOWN;
    }
    // endregion

    // region WhiteBoard

    /**
     * 创建白板
     * @param whiteboardId
     * @param errorCode
     * @param errorMsg
     */
    public static void createWhiteboard(String whiteboardId, int errorCode, String errorMsg) {
        HashMap<String, String> params = null;
        if (whiteboardId != null) {
            params = new HashMap<>(1);
            params.put(MonitorhubField.MFFIELD_PAASSDK_WB_ACT_WHITEBOARD_ID, whiteboardId);
        }
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_PAASSDK_WB_CREATE_WHITEBOARD,
                params, errorCode, errorMsg);
    }

    /**
     * 销毁白板
     * @param whiteboardId
     * @param errorCode
     * @param errorMsg
     */
    public static void destroyWhiteboard(String whiteboardId, int errorCode, String errorMsg) {
        HashMap<String, String> params = null;
        if (whiteboardId != null) {
            params = new HashMap<>(1);
            params.put(MonitorhubField.MFFIELD_PAASSDK_WB_ACT_WHITEBOARD_ID, whiteboardId);
        }
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_PAASSDK_WB_DESTORY_WHITEBOARD,
                params, errorCode, errorMsg);
    }

    /**
     * 开始白板录制
     * @param whiteboardId
     * @param errorCode
     * @param errorMsg
     */
    public static void startWhiteboardRecord(String whiteboardId, String startTime, String recordId, int errorCode, String errorMsg) {
        HashMap<String, String> params = null;
        if (whiteboardId != null) {
            params = new HashMap<>(3);
            params.put(MonitorhubField.MFFIELD_PAASSDK_WB_ACT_WHITEBOARD_ID, whiteboardId);
            params.put(MonitorhubField.MFFIELD_PAASSDK_WB_ACT_START_TIME, startTime);
            params.put(MonitorhubField.MFFIELD_PAASSDK_WB_ACT_RECORD_ID, recordId);
        }
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_PAASSDK_WB_START_WHITEBOARD_RECORD,
                params, errorCode, errorMsg);
    }

    /**
     * 停止白板录制
     * @param whiteboardId
     * @param errorCode
     * @param errorMsg
     */
    public static void stopWhiteboardRecord(String whiteboardId, String stopTime, String recordId, int errorCode, String errorMsg) {
        HashMap<String, String> params = null;
        if (whiteboardId != null) {
            params = new HashMap<>(3);
            params.put(MonitorhubField.MFFIELD_PAASSDK_WB_ACT_WHITEBOARD_ID, whiteboardId);
            params.put(MonitorhubField.MFFIELD_PAASSDK_WB_ACT_STOP_TIME, stopTime);
            params.put(MonitorhubField.MFFIELD_PAASSDK_WB_ACT_RECORD_ID, recordId);
        }
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_PAASSDK_WB_STOP_WHITEBOARD_RECORD,
                params, errorCode, errorMsg);
    }

    /**
     * 暂停白板录制
     * @param whiteboardId
     * @param errorCode
     * @param errorMsg
     */
    public static void pauseWhiteboardRecord(String whiteboardId, String pauseTime, String recordId, int errorCode, String errorMsg) {
        HashMap<String, String> params = null;
        if (whiteboardId != null) {
            params = new HashMap<>(3);
            params.put(MonitorhubField.MFFIELD_PAASSDK_WB_ACT_WHITEBOARD_ID, whiteboardId);
            params.put(MonitorhubField.MFFIELD_PAASSDK_WB_ACT_PAUSE_TIME, pauseTime);
            params.put(MonitorhubField.MFFIELD_PAASSDK_WB_ACT_RECORD_ID, recordId);
        }
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_PAASSDK_WB_PAUSE_WHITEBOARD_RECORD,
                params, errorCode, errorMsg);
    }

    /**
     * 恢复白板录制
     * @param whiteboardId
     * @param errorCode
     * @param errorMsg
     */
    public static void resumeWhiteboardRecord(String whiteboardId, String resumeTime, String recordId, int errorCode, String errorMsg) {
        HashMap<String, String> params = null;
        if (whiteboardId != null) {
            params = new HashMap<>(3);
            params.put(MonitorhubField.MFFIELD_PAASSDK_WB_ACT_WHITEBOARD_ID, whiteboardId);
            params.put(MonitorhubField.MFFIELD_PAASSDK_WB_ACT_RESUME_TIME, resumeTime);
            params.put(MonitorhubField.MFFIELD_PAASSDK_WB_ACT_RECORD_ID, recordId);
        }
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_PAASSDK_WB_RESUME_WHITEBOARD_RECORD,
                params, errorCode, errorMsg);
    }
    // endregion

    // region Chat

    /**
     * 全员禁言
     * @param errorCode
     * @param errorMsg
     */
    public static void muteAll(int errorCode, String errorMsg) {
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_PAASSDK_CHAT_MUTE_ALL, null, errorCode, errorMsg);
    }

    /**
     * 取消全员禁言
     * @param errorCode
     * @param errorMsg
     */
    public static void cancelMuteAll(int errorCode, String errorMsg) {
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_PAASSDK_CHAT_CANCEL_MUTE_ALL, null, errorCode, errorMsg);
    }

    /**
     * 对用户禁言
     * @param userId
     * @param errorCode
     * @param errorMsg
     */
    public static void muteUserById(String userId, int errorCode, String errorMsg) {
        HashMap<String, String> params = null;
        if (userId != null) {
            params = new HashMap<>(1);
            params.put(MonitorhubField.MFFIELD_COMMON_TARGET_UID, userId);
        }
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_PAASSDK_CHAT_MUTE_USER, params, errorCode, errorMsg);
    }

    /**
     * 取消对用户禁言
     * @param userId
     * @param errorCode
     * @param errorMsg
     */
    public static void cancelMuteUserById(String userId, int errorCode, String errorMsg) {
        HashMap<String, String> params = null;
        if (userId != null) {
            params = new HashMap<>(1);
            params.put(MonitorhubField.MFFIELD_COMMON_TARGET_UID, userId);
        }
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_PAASSDK_CHAT_CANCEL_MUTE_USER, params, errorCode, errorMsg);
    }
    // endregion

    // region Room
    /**
     * 登录
     * @param errorCode
     * @param errorMsg
     */
    public static void login(int errorCode, String errorMsg) {
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_PAASSDK_ROOM_LOGIN, null, errorCode, errorMsg);
    }

    /**
     * 登出
     * @param errorCode
     * @param errorMsg
     */
    public static void logout(int errorCode, String errorMsg) {
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_PAASSDK_ROOM_LOGOUT, null, errorCode, errorMsg);
    }

    /**
     * 进入房间
     * @param errorCode
     * @param errorMsg
     */
    public static void enterRoom(int errorCode, String errorMsg) {
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_PAASSDK_ROOM_ENTER_ROOM, null, errorCode, errorMsg);
    }

    /**
     * 离开房间
     * @param errorCode
     * @param errorMsg
     */
    public static void leaveRoom(int errorCode, String errorMsg) {
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_PAASSDK_ROOM_LEAVE_ROOM, null, errorCode, errorMsg);
    }

    /**
     * 切后台
     * @param errorCode
     * @param errorMsg
     */
    public static void goBackground(int errorCode, String errorMsg) {
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_PAASSDK_ROOM_GO_BACKGROUD, null, errorCode, errorMsg);
    }

    /**
     * 切前台
     * @param errorCode
     * @param errorMsg
     */
    public static void goFrontground(int errorCode, String errorMsg) {
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_PAASSDK_ROOM_GO_FRONTGROUD, null, errorCode, errorMsg);
    }
    // endregion

    // region live.publish

    /**
     * 开始直播
     * @param errorCode
     * @param errorMsg
     */
    public static void startLive(int errorCode, String errorMsg) {
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_CLIENT_PUBLISH_START_LIVE,
                null, errorCode, errorMsg);
    }

    /**
     * rtmp推流
     * @param pushUrl
     * @param errorCode
     * @param errorMsg
     */
    public static void rtmpConnect(String pushUrl, int errorCode, String errorMsg) {
        HashMap<String, String> params = null;
        params = new HashMap<>(2);
        String cdnIp = null;
        try {
            cdnIp = NetUtils.getIPAddress(AppContext.getContext());
        } catch (Exception e) {
            Logger.e(e.getMessage());
        }
        params.put(MonitorhubField.MFFIELD_COMMON_CDN_IP,  Utils.isEmpty(cdnIp) ? "" : cdnIp);
        params.put(MonitorhubField.MFFIELD_COMMON_PUSH_URL, Utils.isEmpty(pushUrl) ? "" : pushUrl);
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_CLIENT_PUBLISH_RTMP_CONNECT,
                params, errorCode, errorMsg);
    }

    /**
     * 发布直播
     * @param engine
     * @param errorCode
     * @param errorMsg
     */
    public static void publishLive(String engine, int errorCode, String errorMsg) {
        HashMap<String, String> params = null;
        params = new HashMap<>(1);
        params.put(MonitorhubField.MFFIELD_METAPATH_CLIENT_LINK_ACT_ENGINE_TYPE,  engine);
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_CLIENT_PUBLISH_PUBLISH_LIVE,
                params, errorCode, errorMsg);
    }

    /**
     * 视频启动
     * @param codec
     * @param coder
     * @param width
     * @param height
     * @param fps
     * @param bitrate
     * @param gop
     * @param errorCode
     * @param errorMsg
     */
    public static void videoEncoderInit(String codec, String coder, String width, String height, String fps, String bitrate, String gop, int errorCode, String errorMsg) {
        HashMap<String, String> params = null;
        params = new HashMap<>(7);
        params.put(MonitorhubField.MFFIELD_COMMON_ENCODEC, codec);
        params.put(MonitorhubField.MFFIELD_COMMON_ENCODER, coder);
        params.put(MonitorhubField.MFFIELD_COMMON_WIDTH, width);
        params.put(MonitorhubField.MFFIELD_COMMON_HEIGHT, height);
        params.put(MonitorhubField.MFFIELD_COMMON_FPS, fps);
        params.put(MonitorhubField.MFFIELD_COMMON_BITRATE, bitrate);
        params.put("gop_size", gop);
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_CLIENT_PUBLISH_VIDEO_ENCODER_INIT,
                params, errorCode, errorMsg);
    }

    /**
     * 音频启动
     * @param codec
     * @param coder
     * @param profile
     * @param sample
     * @param channel
     * @param bitrate
     * @param errorCode
     * @param errorMsg
     */
    public static void audioEncoderInit(String codec, String coder, String profile, String sample, String channel, String bitrate, int errorCode, String errorMsg) {
        HashMap<String, String> params = new HashMap<>(6);
        params.put(MonitorhubField.MFFIELD_COMMON_ENCODEC, codec);
        params.put(MonitorhubField.MFFIELD_COMMON_ENCODER, coder);
        params.put(MonitorhubField.MFFIELD_COMMON_PROFILE, profile);
        params.put(MonitorhubField.MFFIELD_COMMON_SAMPLE, sample);
        params.put(MonitorhubField.MFFIELD_COMMON_CHANNEL, channel);
        params.put(MonitorhubField.MFFIELD_COMMON_BITRATE, bitrate);
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_CLIENT_PUBLISH_AUDIO_ENCODER_INIT,
                params, errorCode, errorMsg);
    }

    /**
     * 编码帧率过低
     * @param errorCode
     * @param errorMsg
     */
    public static void encodeLowFPS(int errorCode, String errorMsg) {
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_CLIENT_PUBLISH_ENCODER_LOW_FPS,
                null, errorCode, errorMsg);
    }

    /**
     * 发送帧率过低
     * @param errorCode
     * @param errorMsg
     */
    public static void netLowFPS(int errorCode, String errorMsg) {
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_CLIENT_PUBLISH_NET_LOW_FPS,
                null, errorCode, errorMsg);
    }

    /**
     * 码率设置发生变化
     * @param bitrate
     * @param errorCode
     * @param errorMsg
     */
    public static void bitrateChange(String bitrate, int errorCode, String errorMsg) {
        HashMap<String, String> params = null;
        params = new HashMap<>(1);
        params.put(MonitorhubField.MFFIELD_COMMON_BITRATE, bitrate);
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_CLIENT_PUBLISH_BITRATE_CHANGE,
                params, errorCode, errorMsg);
    }

    /**
     * 暂停直播
     * @param errorCode
     * @param errorMsg
     */
    public static void pauseLive(int errorCode, String errorMsg) {
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_CLIENT_PUBLISH_PAUSE_LIVE,
                null, errorCode, errorMsg);
    }

    /**
     * 停止直播
     * @param errorCode
     * @param errorMsg
     */
    public static void stopLive(int errorCode, String errorMsg) {
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_CLIENT_PUBLISH_STOP_LIVE,
                null, errorCode, errorMsg);
    }

    /**
     * rtmp停流
     * @param errorCode
     * @param errorMsg
     */
    public static void rtmpClose(int errorCode, String errorMsg) {
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_CLIENT_PUBLISH_RTMP_CLOSE,
                null, errorCode, errorMsg);
    }

    /**
     * 网络断开
     * @param errorCode
     * @param errorMsg
     */
    public static void netDisconnect(int errorCode, String errorMsg) {
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_CLIENT_PUBLISH_NET_DISCONNECT,
                null, errorCode, errorMsg);
    }

    /**
     * 网络重连开始
     * @param errorCode
     * @param errorMsg
     */
    public static void netReconnectStart(String cdnIp, String url, int errorCode, String errorMsg) {
        HashMap<String, String> params = null;
        params = new HashMap<>(2);
        params.put(MonitorhubField.MFFIELD_METAPATH_CLIENT_LINK_ACT_ENGINE_TYPE,  Utils.isEmpty(cdnIp) ? "" : cdnIp);
        params.put(MonitorhubField.MFFIELD_COMMON_PUSH_URL,  Utils.isEmpty(url) ? "" : url);
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_CLIENT_PUBLISH_NET_RECONNECT_START,
                params, errorCode, errorMsg);
    }

    /**
     * 网络重连成功
     * @param errorCode
     * @param errorMsg
     */
    public static void netReconnectSuccess(int errorCode, String errorMsg) {
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_CLIENT_PUBLISH_NET_RECONNECT_SUCESS,
                null, errorCode, errorMsg);
    }
    // endregion

    // region rtc.heart

    public static void rtcStats(String channelId, long availableSendKbitrate, long sentKbitrate, long rcvdKbitrate,
                                long sentBytes, long rcvdBytes, float cpuUsage,
                                float systemCpuUsage, long videoRcvdKbitrate, long videoSentKbitrate,
                                long callDuration, long sentLossRate, long sentLossPkts,
                                long sentExpectedPkts, long rcvdLossRate, long rcvdLossPkts,
                                long rcvdExpectedPkts, long lastmileDelay) {
        HashMap<String, String> params = null;
        params = new HashMap<>();
        params.put(MonitorhubField.MFFIELD_COMMON_RTC_CHANNEL_ID, channelId);
        params.put(MonitorhubField.MFFIELD_COMMON_RTC_SENT_K_BITRATE, String.valueOf(sentKbitrate));
        params.put(MonitorhubField.MFFIELD_COMMON_RTC_RCVD_K_BITRATE, String.valueOf(rcvdKbitrate));
        params.put(MonitorhubField.MFFIELD_COMMON_RTC_SENT_BYTES, String.valueOf(sentBytes));
        params.put(MonitorhubField.MFFIELD_COMMON_RTC_RCVD_BYTES, String.valueOf(rcvdBytes));
        params.put(MonitorhubField.MFFIELD_SYSINFO_CPU_USAGE_PROC, String.valueOf(cpuUsage));
        params.put(MonitorhubField.MFFIELD_SYSINFO_CPU_USAGE_SYS, String.valueOf(systemCpuUsage));
        params.put(MonitorhubField.MFFIELD_COMMON_RTC_VIDEO_RCVD_K_BITRATE, String.valueOf(videoRcvdKbitrate));
        params.put(MonitorhubField.MFFIELD_COMMON_RTC_VIDEO_SENT_K_BITRATE, String.valueOf(videoSentKbitrate));
        params.put(MonitorhubField.MFFIELD_COMMON_RTC_CALL_DURATION, String.valueOf(callDuration));
        params.put(MonitorhubField.MFFIELD_COMMON_RTC_SENT_LOSS_RATE, String.valueOf(sentLossRate));
        params.put(MonitorhubField.MFFIELD_COMMON_RTC_SENT_LOSS_PKTS, String.valueOf(sentLossPkts));
        params.put(MonitorhubField.MFFIELD_COMMON_RTC_SENT_EXPECTED_PKTS, String.valueOf(sentExpectedPkts));
        params.put(MonitorhubField.MFFIELD_COMMON_RTC_RCVD_LOSS_RATE, String.valueOf(rcvdLossRate));
        params.put(MonitorhubField.MFFIELD_COMMON_RTC_RCVD_LOSS_PKTS, String.valueOf(rcvdLossPkts));
        params.put(MonitorhubField.MFFIELD_COMMON_RTC_RCVD_EXPECTED_PKTS, String.valueOf(rcvdExpectedPkts));
        params.put(MonitorhubField.MFFIELD_COMMON_RTC_LASTMILE_DELAY, String.valueOf(lastmileDelay));
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_PAASSDK_RTC_STATS, params, REPORT_EVENT_SUCCESS_CODE, null);
    }

    public static void localVideoStats(String channelId, int encodeFps, int sentBitrate, int sentFps, String sourceType) {
        HashMap<String, String> params = null;
        params = new HashMap<>();
        params.put(MonitorhubField.MFFIELD_COMMON_RTC_CHANNEL_ID, channelId);
        params.put(MonitorhubField.MFFIELD_COMMON_RTC_VIDEO_ENCODE_FPS, String.valueOf(encodeFps));
        params.put(MonitorhubField.MFFIELD_COMMON_RTC_VIDEO_SENT_BITRATE, String.valueOf(sentBitrate));
        params.put(MonitorhubField.MFFIELD_COMMON_RTC_VIDEO_SENT_FPS, String.valueOf(sentFps));
        params.put(MonitorhubField.MFFIELD_PAASSDK_RTC_SOURCE_TYPE, sourceType);
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_PAASSDK_RTC_LOCAL_VIDEO_STATS, params, REPORT_EVENT_SUCCESS_CODE, null);
    }

    public static void localAudioStats(String channelId, int numChannel, int sentBitrate, int sentSamplerate, String sourceType) {
        HashMap<String, String> params = null;
        params = new HashMap<>();
        params.put(MonitorhubField.MFFIELD_COMMON_RTC_CHANNEL_ID, channelId);
        params.put(MonitorhubField.MFFIELD_COMMON_RTC_AUDIO_SENT_BITRATE, String.valueOf(sentBitrate));
        params.put(MonitorhubField.MFFIELD_COMMON_RTC_AUDIO_SENT_SAMPLERATE, String.valueOf(sentSamplerate));
        params.put(MonitorhubField.MFFIELD_PAASSDK_RTC_SOURCE_TYPE, String.valueOf(sourceType));
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_PAASSDK_RTC_LOCAL_AUDIO_STATS, params, REPORT_EVENT_SUCCESS_CODE, null);
    }

    public static void remoteVideoStats(String channelId, int decodeFps, int frozenTimes, int height, int renderFps, String sourceType, String userId, int width) {
        HashMap<String, String> params = null;
        params = new HashMap<>();
        params.put(MonitorhubField.MFFIELD_COMMON_RTC_CHANNEL_ID, channelId);
        params.put(MonitorhubField.MFFIELD_COMMON_RTC_VIDEO_DECODE_FPS, String.valueOf(decodeFps));
        params.put(MonitorhubField.MFFIELD_COMMON_RTC_VIDEO_FROZEN_TIMES, String.valueOf(frozenTimes));
        params.put(MonitorhubField.MFFIELD_COMMON_HEIGHT, String.valueOf(height));
        params.put(MonitorhubField.MFFIELD_COMMON_RTC_VIDEO_RENDER_FPS, String.valueOf(renderFps));
        params.put(MonitorhubField.MFFIELD_PAASSDK_RTC_SOURCE_TYPE, sourceType);
        params.put(MonitorhubField.MFFIELD_COMMON_TARGET_UID, userId);
        params.put(MonitorhubField.MFFIELD_COMMON_WIDTH, String.valueOf(width));
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_PAASSDK_RTC_LOCAL_AUDIO_STATS, params, REPORT_EVENT_SUCCESS_CODE, null);
    }

    public static void remoteAudioStats(String channelId, int audioLossRate, int jitter_buffer_delay, int network_transport_delay, int quality, int rcvdBitrate, String sourceType, String userId, int totalFrozenTimes) {
        HashMap<String, String> params = null;
        params = new HashMap<>();
        params.put(MonitorhubField.MFFIELD_COMMON_RTC_CHANNEL_ID, channelId);
        params.put(MonitorhubField.MFFIELD_COMMON_RTC_AUDIO_LOSS_RATE, String.valueOf(audioLossRate));
        params.put(MonitorhubField.MFFIELD_COMMON_RTC_AUDIO_JITTER_BUFFER_DELAY, String.valueOf(jitter_buffer_delay));
        params.put(MonitorhubField.MFFIELD_COMMON_RTC_AUDIO_NETWORK_TRANSPORT_DELAY, String.valueOf(network_transport_delay));
        params.put(MonitorhubField.MFFIELD_COMMON_RTC_AUDIO_QUALITY, String.valueOf(quality));
        params.put(MonitorhubField.MFFIELD_COMMON_RTC_AUDIO_RCVD_BITRATE, String.valueOf(rcvdBitrate));
        params.put(MonitorhubField.MFFIELD_PAASSDK_RTC_SOURCE_TYPE, sourceType);
        params.put(MonitorhubField.MFFIELD_COMMON_TARGET_UID, userId);
        params.put(MonitorhubField.MFFIELD_COMMON_RTC_AUDIO_TOTAL_FROZEN_TIMES, String.valueOf(totalFrozenTimes));
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_PAASSDK_RTC_REMOTE_AUDIO_STATS, params, REPORT_EVENT_SUCCESS_CODE, null);


        params.put(MonitorhubField.MFFIELD_CLASS_STUDENT_HEART_SCREEN_SENT_FPS, channelId);

    }

    // endregion
}