package com.aliyun.roompaas.biz;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.alibaba.dingpaas.base.DPSAuthListener;
import com.alibaba.dingpaas.base.DPSAuthService;
import com.alibaba.dingpaas.base.DPSAuthToken;
import com.alibaba.dingpaas.base.DPSAuthTokenExpiredReason;
import com.alibaba.dingpaas.base.DPSAuthTokenGotCallback;
import com.alibaba.dingpaas.base.DPSConnectionStatus;
import com.alibaba.dingpaas.base.DPSError;
import com.alibaba.dingpaas.base.DPSLogoutListener;
import com.alibaba.dingpaas.cloudconfig.CloudconfigNotifyCb;
import com.alibaba.dingpaas.cloudconfig.GetSlsConfigRsp;
import com.alibaba.dingpaas.cloudconfig.GetVisibleConfigCb;
import com.alibaba.dingpaas.cloudconfig.GetVisibleConfigRsp;
import com.alibaba.dingpaas.cloudconfig.StsToken;
import com.alibaba.dingpaas.monitorhub.MonitorhubBizType;
import com.alibaba.dingpaas.monitorhub.MonitorhubModule;
import com.alibaba.dingpaas.monitorhub.MonitorhubStsTokenModel;
import com.alibaba.dingpaas.mps.MPSAuthTokenCallback;
import com.alibaba.dingpaas.mps.MPSEngine;
import com.alibaba.dingpaas.mps.MPSEngineStartListener;
import com.alibaba.dingpaas.mps.MPSLogHandler;
import com.alibaba.dingpaas.mps.MPSLogLevel;
import com.alibaba.dingpaas.mps.MPSManager;
import com.alibaba.dingpaas.mps.MPSManagerCreateListener;
import com.alibaba.dingpaas.mps.MPSSettingService;
import com.alibaba.dingpaas.mps.MpsEngineType;
import com.alibaba.dingpaas.room.GetRoomListCb;
import com.alibaba.dingpaas.room.GetRoomListReq;
import com.alibaba.dingpaas.room.GetRoomListRsp;
import com.alibaba.dingpaas.room.RoomBasicInfo;
import com.alibaba.dingpaas.room.RoomModule;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.aliyun.roompaas.base.AppContext;
import com.aliyun.roompaas.base.BaseSettings;
import com.aliyun.roompaas.base.ModuleRegister;
import com.aliyun.roompaas.base.callback.UICallback;
import com.aliyun.roompaas.base.cloudconfig.CloudConfigCenter;
import com.aliyun.roompaas.base.cloudconfig.CloudConfigMgr;
import com.aliyun.roompaas.base.cloudconfig.IKeys;
import com.aliyun.roompaas.base.error.ErrorMessage;
import com.aliyun.roompaas.base.error.Errors;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.base.log.Logger;
import com.aliyun.roompaas.base.log.LoggerHandler;
import com.aliyun.roompaas.base.model.PageModel;
import com.aliyun.roompaas.base.monitor.MonitorHubChannel;
import com.aliyun.roompaas.base.util.CommonUtil;
import com.aliyun.roompaas.base.util.NetUtils;
import com.aliyun.roompaas.base.util.Utils;
import com.aliyun.roompaas.biz.enums.EnvType;
import com.aliyun.roompaas.biz.exposable.IRoomEngine;
import com.aliyun.roompaas.biz.exposable.IRoomEngineEventHandler;
import com.aliyun.roompaas.biz.exposable.RoomChannel;
import com.aliyun.roompaas.biz.exposable.RoomSceneClass;
import com.aliyun.roompaas.biz.exposable.RoomSceneLive;
import com.aliyun.roompaas.biz.exposable.model.Result;
import com.aliyun.roompaas.biz.exposable.model.RoomParam;
import com.aliyun.roompaas.biz.exposable.model.TokenInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author puke
 * @version 2021/4/28
 */
public class RoomEngine implements IRoomEngine {

    private static final String TAG = "RoomEngine";
    private static final String TAG_CXX = "RoomCXX";
    private static final String UA_APP_NAME = "VPaasSDK";

    @SuppressLint("StaticFieldLeak")
    private static RoomEngine instance;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final UIEventHandler eventHandler = new UIEventHandler();

    private EngineConfig config;
    private MPSEngine mpsEngine;
    private String userId;
    private Context context;
    private Callback<Void> loginCallback;
    private boolean isInit;
//    private final AtomicBoolean isLogin = new AtomicBoolean(false);

    // 内部使用, Demo层反射改动, 勿重命名，勿混淆
    @Keep
    private EnvType envType = EnvType.ONLINE;
    private String bizType = MonitorhubBizType.BASIC_SDK;
    private DPSAuthListener dpsAuthListener;

    public static RoomEngine getInstance() {
        if (instance == null) {
            instance = new RoomEngine();
        }
        return instance;
    }

    static {
        Logger.d(TAG, "=========loadLibrary start");
        System.loadLibrary("openssl");
        System.loadLibrary("gaea");
        System.loadLibrary("fml");
        System.loadLibrary("dmojo_support");
        System.loadLibrary("sqlite3");
        System.loadLibrary("dps");
        System.loadLibrary("vpaassdk");
        Logger.d(TAG, "=========loadLibrary end");

        MPSEngine.setLogHandler(MPSLogLevel.MPS_LOG_LEVEL_DEBUG, new MPSLogHandler() {
            @Override
            public void onLog(MPSLogLevel logLevel, String message) {
                switch (logLevel) {
                    case MPS_LOG_LEVEL_DEBUG:
                        Logger.d(TAG_CXX, message);
                        break;
                    case MPS_LOG_LEVEL_WARNING:
                        Logger.w(TAG_CXX, message);
                        break;
                    case MPS_LOG_LEVEL_INFO:
                        Logger.i(TAG_CXX, message);
                        break;
                    default:
                        Logger.e(TAG_CXX, message);
                        break;
                }
            }
        });
    }

    @Override
    public void init(Context context, EngineConfig config, final Callback<Void> callback) {
        Logger.i(TAG, "RoomEngine init");
        if (isInit) {
            Logger.w(TAG, "RoomEngine init again, already init");
            if (callback != null) {
                callback.onSuccess(null);
            }
            return;
        }

        Context applicationContext = context.getApplicationContext();

        this.context = applicationContext;
        this.config = config;

        // 参数校验
        if (config.tokenInfoGetter == null) {
            Logger.e(TAG, "init方法的EngineConfig参数缺少tokenInfoGetter设置");
            Utils.invokeInvalidParamError(callback);
            return;
        }

        // 配置日志处理器
        LoggerHandler loggerHandler = config.loggerHandler;
        if (loggerHandler != null) {
            setLoggerHandler(loggerHandler);
        }

        // 创建DPS引擎
        if (RoomConst.USE_META_PATH) {
            mpsEngine = MPSEngine.createMPSEngine(MpsEngineType.MPS_ENGINE_TYPE_META);
        } else {
            mpsEngine = MPSEngine.createMPSEngine(MpsEngineType.MPS_ENGINE_TYPE_DPS);
        }

        if (mpsEngine == null) {
            Logger.e(TAG, "MPSEngine创建失败");
            Utils.invokeError(callback, Errors.INNER_STATE_ERROR);
            return;
        }

        // 获取配置服务
        MPSSettingService setting = mpsEngine.getSettingService();
        initService(applicationContext, setting, config);

        // 注册房间的lwp网络模块 (基础模块, 固定加载)
        mpsEngine.registerModule(RoomModule.getModuleInfo());

        // 注册云控模块 (基础模块, 固定加载)
        CloudConfigMgr.INSTANCE.register(mpsEngine);

        // 动态加载插件模块
        ModuleRegister.scanAndLoadPluginModule();

        // 初始化MonitorhubModule
        MonitorhubModule.getMonitorhubModule().initMonitorhubModule();

        // 启动引擎
        final UICallback<Void> uiCallback = new UICallback<>(callback);
        mpsEngine.start(new MPSEngineStartListener() {
            @Override
            public void onSuccess() {
                isInit = true;
                uiCallback.onSuccess(null);
            }

            @Override
            public void onFailure(final DPSError dpsError) {
                Utils.callErrorWithDps(uiCallback, dpsError);
            }
        });
    }

    public EngineConfig getConfig() {
        return config;
    }

    @Override
    public void setEventHandler(IRoomEngineEventHandler eventHandler) {
        this.eventHandler.setEventHandler(eventHandler);
    }

    /**
     * 自定义日志处理逻辑
     *
     * @param loggerHandler 日志处理器
     */
    public void setLoggerHandler(@NonNull LoggerHandler loggerHandler) {
        Logger.setLoggerHandler(loggerHandler);
    }

    @Override
    public void auth(final String userId, @NonNull final Callback<Void> callback) {
        if (mpsEngine == null) {
            String message = "请在auth之前先调用RoomEngine#init方法";
            Logger.e(TAG, message);
            throw new RuntimeException(message);
        }

        String lastUserId = this.userId;
        Logger.i(TAG, String.format(
                "start auth, last userId is %s, and current userId is %s", lastUserId, userId));
        if (TextUtils.isEmpty(userId)) {
            Logger.e(TAG, "userId is empty");
            Utils.invokeInvalidParamError(callback);
            return;
        }

        if (TextUtils.equals(lastUserId, userId) && isLogin()) {
            // 相同账号, 且登录成功时, 不做额外处理
            Logger.w(TAG, "same userId, and already login, don't need to login");
            callback.onSuccess(null);
            return;
        }

        if (TextUtils.isEmpty(lastUserId)) {
            // 非切换账号时, 走正常的登录逻辑
            Logger.i(TAG, String.format("user %s login", userId));
            performAuth(userId, callback);
            return;
        }

        // 切换账号时, 先退出上个账号
        Logger.i(TAG, String.format("switch user from %s to %s", lastUserId, userId));
        logout(new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Logger.i(TAG, "auth with logout success");
                performAuth(userId, callback);
            }

            @Override
            public void onError(String errorMsg) {
                Logger.i(TAG, "auth with logout fail: " + errorMsg);
                callback.onError(errorMsg);
            }
        });
    }

    private void performAuth(final String userId, Callback<Void> callback) {
        this.userId = userId;
        this.loginCallback = new UICallback<>(callback);

        getMpsManager(userId, new Callback<MPSManager>() {
            @Override
            public void onSuccess(MPSManager mpsManager) {
                if (mpsManager == null) {
                    Logger.e(TAG, "mpsManager is null");
                    return;
                }
                DPSAuthService authService = mpsManager.getAuthService();
                if (authService == null) {
                    Logger.e(TAG, "login, authService is null");
                    return;
                }
                if (dpsAuthListener != null) {
                    try {
                        authService.removeListener(dpsAuthListener);
                    } catch (Exception e) {
                        Logger.e(TAG, "remove auth listener error", e);
                    }
                }
                dpsAuthListener = new DPSAuthListener() {
                    @Override
                    public void onConnectionStatusChanged(DPSConnectionStatus status) {
                        Logger.i(TAG, "onConnectionStatusChanged, status = " + status);
                        if (status == DPSConnectionStatus.CS_AUTHED) {
                            AutoEnterRoomHandler.doEnterRoomIfNeed(userId);
                            eventHandler.onEngineEvent(IRoomEngineEventHandler.EngineEvent.LOGIN_SUCCESS);

                            Utils.callSuccess(loginCallback, null);
                            loginCallback = null;

                            getVisibleConfig();
                            MonitorHubChannel.login(MonitorHubChannel.REPORT_EVENT_SUCCESS_CODE, null);
                        }

                        final MonitorHubChannel.ACT_CONN_STATE connState;
                        switch (status) {
                            case CS_UNCONNECTED:
                                connState = MonitorHubChannel.ACT_CONN_STATE.NO_CON;
                                break;
                            case CS_CONNECTING:
                                connState = MonitorHubChannel.ACT_CONN_STATE.CONNING;
                                break;
                            case CS_CONNECTED:
                                connState = MonitorHubChannel.ACT_CONN_STATE.CONNED;
                                break;
                            case CS_AUTHING:
                                connState = MonitorHubChannel.ACT_CONN_STATE.LOGIN;
                                break;
                            case CS_AUTHED:
                                connState = MonitorHubChannel.ACT_CONN_STATE.LOGIN_SUC;
                                break;
                            default:
                                return;
                        }
                        MonitorHubChannel.reportConnStateChange(connState.getValue(),
                                MPSEngine.getEngineType().getValue(), NetUtils.getNetworkState(AppContext.getContext()),
                                MonitorHubChannel.REPORT_EVENT_SUCCESS_CODE, null);
                    }

                    @Override
                    public void onGetAuthCodeFailed(int i, final String errorMsg) {
                        Logger.e(TAG, "onGetAuthCodeFailed: " + errorMsg);
                        eventHandler.onEngineEvent(IRoomEngineEventHandler.EngineEvent.LOGIN_FAIL);

                        Utils.callError(loginCallback, errorMsg);
                        loginCallback = null;
                        MonitorHubChannel.login(i, errorMsg);
                    }

                    @Override
                    public void onLocalLogin() {
                        Logger.i(TAG, "onLocalLogin");
                    }

                    @Override
                    public void onKickout(String s) {
                        Logger.i(TAG, "onKickout: " + s);
                        eventHandler.onEngineEvent(IRoomEngineEventHandler.EngineEvent.KICK_OUT);
                    }

                    @Override
                    public void onDeviceStatus(int i, int i1, int i2, long l) {
                        Logger.i(TAG, "onDeviceStatus");
                    }

                    @Override
                    public void onMainServerCookieRefresh(String s) {
                        Logger.i(TAG, "onMainServerCookieRefresh: " + s);
                    }
                };
                authService.addListener(dpsAuthListener);
                authService.login();
            }

            @Override
            public void onError(final String errorMsg) {
                Logger.e(TAG, "login error, " + errorMsg);
                eventHandler.onEngineEvent(IRoomEngineEventHandler.EngineEvent.LOGIN_FAIL);
                Utils.callError(loginCallback, errorMsg);
                loginCallback = null;
                MonitorHubChannel.login(MonitorHubChannel.REPORT_EVENT_ERROR_CODE, errorMsg);
            }
        });
    }

    private void getMpsManager(final String userId, @NonNull final Callback<MPSManager> callback) {
        Logger.i(TAG, "getMpsManager, userId=" + userId);
        if (mpsEngine == null) {
            Logger.e(TAG, "RoomEngine的init方法未调用或调用失败");
            Utils.invokeError(callback, Errors.INNER_STATE_ERROR);
            return;
        }

        final MPSManager mpsManager = mpsEngine.getMPSManager(userId);
        if (mpsManager != null) {
            Logger.i(TAG, "getMpsManager not empty, userId=" + userId);
            callback.onSuccess(mpsManager);
            return;
        }

        Logger.i(TAG, "createMPSManager, userId=" + userId);
        mpsEngine.createMPSManager(userId, new MPSManagerCreateListener() {
            @Override
            public void onSuccess(MPSManager mpsManager) {
                Logger.i(TAG, "createMPSManager success, userId=" + userId);
                CloudConfigMgr.INSTANCE.init(userId);
                CloudConfigMgr.INSTANCE.setCloudConfigNotifyCb(new CloudconfigNotifyCb() {
                    @Override
                    public void onGetSlsConfig(GetSlsConfigRsp getSlsConfigRsp) {
                        initMonitorHub(getSlsConfigRsp);
                    }

                    @Override
                    public void onUpdateSlsStsToken(StsToken stsToken) {
                        MonitorhubModule.getMonitorhubModule().updateStsToken(new MonitorhubStsTokenModel(stsToken.accessKeyId, stsToken.accessKeySecret, stsToken.securityToken, stsToken.expireTime));
                    }
                });
                callback.onSuccess(mpsManager);
            }

            @Override
            public void onFailure(DPSError dpsError) {
                Logger.i(TAG, String.format(
                        "createMPSManager error, userId=%s, error=%s", userId, dpsError.reason));
                Utils.callErrorWithDps(callback, dpsError);
            }
        });
    }

    @Override
    public void logout(Callback<Void> callback) {
        final UICallback<Void> uiCallback = new UICallback<>(callback);
        Logger.i(TAG, String.format("logout, current userId is %s", userId));
        final MPSManager mpsManager;
        if (mpsEngine == null
                || (mpsManager = mpsEngine.getMPSManager(userId)) == null) {
            Logger.w(TAG, "logout but dpsManager is null");
            RoomEngine.this.userId = null;
            eventHandler.onEngineEvent(IRoomEngineEventHandler.EngineEvent.LOGOUT_SUCCESS);
            uiCallback.onSuccess(null);
            return;
        }

        DPSAuthService authService = mpsManager.getAuthService();
        if (authService == null) {
            Logger.e(TAG, "logout, authService is null");
            return;
        }
        authService.logout(new DPSLogoutListener() {
            @Override
            public void onSuccess() {
                Logger.i(TAG, String.format("logout success, current userId is %s", userId));
                RoomEngine.this.userId = null;
                eventHandler.onEngineEvent(IRoomEngineEventHandler.EngineEvent.LOGOUT_SUCCESS);
                uiCallback.onSuccess(null);
                MonitorHubChannel.logout(MonitorHubChannel.REPORT_EVENT_SUCCESS_CODE, null);
                MonitorHubChannel.unInitMonitorHub();
            }

            @Override
            public void onFailure(DPSError dpsError) {
                Logger.e(TAG, String.format(
                        "logout fail: %s, current userId is %s", dpsError.reason, userId));
                eventHandler.onEngineEvent(IRoomEngineEventHandler.EngineEvent.LOGOUT_FAIL);
                Utils.callErrorWithDps(uiCallback, dpsError);
                MonitorHubChannel.logout(dpsError.code, dpsError.reason);
            }
        });
    }

    @Override
    public boolean isInit() {
        return isInit;
    }

    @Override
    public boolean isLogin() {
        if (mpsEngine != null && !TextUtils.isEmpty(userId)) {
            MPSManager mpsManager = mpsEngine.getMPSManager(userId);
            if (mpsManager != null) {
                DPSAuthService authService = mpsManager.getAuthService();
                if (authService != null) {
                    return authService.getConnectionStatus() == DPSConnectionStatus.CS_AUTHED;
                }
            }
        }
        return false;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public Result<RoomChannel> getRoomChannel(String roomId) {
        if (!isLogin()) {
            return Result.error(ErrorMessage.NOT_LOGIN);
        }
        RoomChannel roomChannel = new RoomChannelImpl(context, userId, roomId, null);
        MonitorHubChannel.setRoomId(roomId);
        return Result.success(roomChannel);
    }

    @Override
    public Result<RoomSceneLive> getRoomSceneLive() {
        if (!isLogin()) {
            return Result.error(ErrorMessage.NOT_LOGIN);
        }
        RoomSceneLive roomSceneLive = new RoomSceneLiveImpl(userId);
        return Result.success(roomSceneLive);
    }

    @Override
    public Result<RoomSceneClass> getRoomSceneClass() {
        if (!isLogin()) {
            return Result.error(ErrorMessage.NOT_LOGIN);
        }
        RoomSceneClass roomSceneLive = new RoomSceneClassImpl(userId);
        return Result.success(roomSceneLive);
    }

    private void initService(final Context context, MPSSettingService setting, EngineConfig config) {
        File externalCacheDir = context.getExternalCacheDir();
        if (externalCacheDir == null) {
            throw new RuntimeException("Access external cache dir failure.");
        }
        String dataPath = externalCacheDir.getPath() + "/uid";
        // Set data path where database will be created in this folder
        setting.setDataPath(dataPath);
        File file = new File(dataPath);
        if (!file.exists()) {
            // noinspection ResultOfMethodCallIgnored
            file.mkdir();
        }

        // Env Type
        Logger.i(TAG, "Current envType is " + envType);
        setting.setEnvType(envType.getDpsEnvType());
        // App Key
        setting.setAppKey(config.appKey);
        // App ID
        setting.setAppID(config.appId);
        BaseSettings.setAppId(config.appId);
        // App Name
        setting.setAppName(UA_APP_NAME);
        // App version
        setting.setAppVersion(BuildConfig.VERSION_NAME);
        // Device id (Simple way below)
        setting.setDeviceId(config.deviceId);
        // Device name
        setting.setDeviceName(android.os.Build.MODEL);
        // Device type
        setting.setDeviceType(android.os.Build.BRAND);
        // Local
        setting.setDeviceLocale(Locale.getDefault().getLanguage());
        // OS Name
        setting.setOSName(CommonUtil.OS);
        // OS version
        setting.setOSVersion(android.os.Build.VERSION.RELEASE);
        // Long link conn type,
//        setting.setLongLinkConnectionType(DPSConnectionType.CONNECTION_TYPE_BIFROST);
        // Long link addr
        final String longLinkUrl;
        if (RoomConst.USE_META_PATH == true) {
            if (envType == EnvType.PRE) {
                longLinkUrl = RoomConst.META_LONG_URL_4_PRE;
            } else {
                longLinkUrl = RoomConst.META_LONG_URL_4_ONLINE;
            }
            setting.setLonglinkServerAddress(longLinkUrl);
        } else {
            if (envType == EnvType.PRE) {
                longLinkUrl = RoomConst.LONG_LINK_URL_4_PRE;
            } else {
                longLinkUrl = RoomConst.LONG_LINK_URL_4_ONLINE;
            }
            setting.setLonglinkServerAddress(longLinkUrl);
        }

        // File upload server addr, provide when needed
//        setting.setFileUploadServerAddress(config.fileUploadUrl);
        // Media host
//        ArrayList<DPSMediaHost> mediaHosts = new ArrayList<>();
//        mediaHosts.add(new DPSMediaHost(DPSMediaHostType.MEDIA_HOST_TYPE_AUTH, config.mediaHost));
//        setting.setMediaHost(mediaHosts);
        // File upload connection type.
//        setting.setFileUploadConnectionType(DPSConnectionType.CONNECTION_TYPE_DINGTALK_FILE);
        setting.setDisableSslVerify(true);
        // Auth Token callback
        setting.setAuthTokenCallback(new DefaultMPSAuthTokenCallback());
    }

    @Override
    public void getRoomList(RoomParam param, Callback<PageModel<RoomBasicInfo>> callback) {
        if (!isInit) {
            Logger.e(TAG, "room engine not init");
            throw new RuntimeException("call RoomEngine#init first.");
        }

        final UICallback<PageModel<RoomBasicInfo>> uiCallback = new UICallback<>(callback);
        GetRoomListReq req = new GetRoomListReq();
        req.pageNum = param.pageNum;
        req.pageSize = param.pageSize;
        RoomModule.getModule(userId).getRpcInterface().getRoomList(req, new GetRoomListCb() {
            @Override
            public void onSuccess(GetRoomListRsp rsp) {
                PageModel<RoomBasicInfo> pageModel = new PageModel<>();
                pageModel.list = rsp.roomInfoList;
                pageModel.total = rsp.total;
                pageModel.hasMore = rsp.hasMore;
                uiCallback.onSuccess(pageModel);
            }

            @Override
            public void onFailure(DPSError dpsError) {
                Utils.callErrorWithDps(uiCallback, dpsError);
            }
        });
    }

    private class DefaultMPSAuthTokenCallback implements MPSAuthTokenCallback {

        @Override
        public void onCallback(final String userId, final DPSAuthTokenGotCallback dpsAuthTokenGotCallback,
                               DPSAuthTokenExpiredReason dpsAuthTokenExpiredReason) {
            config.tokenInfoGetter.getTokenInfo(userId, new Callback<TokenInfo>() {
                @Override
                public void onSuccess(TokenInfo tokenInfo) {
                    DPSAuthToken dpsAuthToken = new DPSAuthToken();
                    dpsAuthToken.accessToken = tokenInfo.accessToken;
                    dpsAuthToken.refreshToken = tokenInfo.refreshToken;
                    dpsAuthTokenGotCallback.onSuccess(dpsAuthToken);
                }

                @Override
                public void onError(String errorMsg) {
                    dpsAuthTokenGotCallback.onFailure(0, errorMsg);
                }
            });
        }
    }

    private class UIEventHandler implements IRoomEngineEventHandler {
        IRoomEngineEventHandler eventHandler;

        public void setEventHandler(IRoomEngineEventHandler eventHandler) {
            this.eventHandler = eventHandler;
        }

        @Override
        public void onEngineEvent(final int event) {
            if (eventHandler != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        eventHandler.onEngineEvent(event);
                    }
                });
            }
        }
    }

    // Auth成功后，初始化上报逻辑
    private void initMonitorHub(GetSlsConfigRsp slsConfig) {
        MonitorHubChannel.initMonitorHub(slsConfig);
        MonitorHubChannel.setUid(getUserId());
        MonitorHubChannel.setAppId(getConfig().appId);
        MonitorHubChannel.setDeviceId(getConfig().deviceId);
        MonitorHubChannel.setBizType(bizType);
    }

    private void getVisibleConfig() {
        List keyList = new ArrayList();
        keyList.add(IKeys.LiveRoom.LIVE_VISIBLE_CONFIG);
        CloudConfigMgr.INSTANCE.getVisibleConfig(keyList, new GetVisibleConfigCb() {
            @Override
            public void onSuccess(GetVisibleConfigRsp getVisibleConfigRsp) {
                Logger.i(TAG, "onSuccess: " + getVisibleConfigRsp);
                String resolution = null;
                if (getVisibleConfigRsp != null && getVisibleConfigRsp.keyConfigMap != null
                        && !TextUtils.isEmpty((resolution = getVisibleConfigRsp.keyConfigMap.get(IKeys.LiveRoom.LIVE_VISIBLE_CONFIG)))) {
                    try {
                        CloudConfigCenter.getInstance().setVisibleConfigByLive(JSON.parseObject(resolution));
                        Logger.i(TAG, "resolution " + resolution);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(DPSError dpsError) {
                Logger.e(TAG, "onFailure: " + dpsError.reason);
            }
        });
    }
}
