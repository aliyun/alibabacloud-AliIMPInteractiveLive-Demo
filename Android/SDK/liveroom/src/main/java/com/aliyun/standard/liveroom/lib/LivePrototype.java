package com.aliyun.standard.liveroom.lib;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.alibaba.dingpaas.monitorhub.MonitorhubBizType;
import com.alibaba.dingpaas.scenelive.SceneCreateLiveReq;
import com.alibaba.dingpaas.scenelive.SceneCreateLiveRsp;
import com.alibaba.dingpaas.scenelive.SceneGetLiveDetailRsp;
import com.alibaba.fastjson.JSON;
import com.aliyun.roompaas.base.base.Function;
import com.aliyun.roompaas.base.callback.UICallback;
import com.aliyun.roompaas.base.error.Errors;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.base.log.Logger;
import com.aliyun.roompaas.base.util.Utils;
import com.aliyun.roompaas.biz.RoomEngine;
import com.aliyun.roompaas.biz.exposable.RoomChannel;
import com.aliyun.roompaas.biz.exposable.RoomSceneLive;
import com.aliyun.roompaas.biz.exposable.enums.LiveStatus;
import com.aliyun.roompaas.biz.exposable.model.Result;
import com.aliyun.roompaas.live.exposable.AliLiveMediaStreamOptions;
import com.aliyun.roompaas.player.exposable.CanvasScale;
import com.aliyun.roompaas.roombase.BaseOpenParam;
import com.aliyun.roompaas.roombase.BizInitParam;
import com.aliyun.roompaas.roombase.Const;
import com.aliyun.roompaas.roombase.IRoomInitializer;
import com.aliyun.roompaas.roombase.RoomInitializer;
import com.aliyun.standard.liveroom.lib.model.LiveRoomModel;

import java.io.Serializable;
import java.util.Map;


/**
 * @author puke
 * @version 2021/7/20
 */
public class LivePrototype implements Serializable {

    private static final String TAG = LivePrototype.class.getSimpleName();
    private static final byte[] sInstanceLock = new byte[0];

    private static LivePrototype sInstance;

    private OpenLiveParam openLiveParam;
    private LiveHook liveHook;
    private boolean isSwitching;
    private IRoomInitializer<InitParam> roomInitializer;
    private boolean isInited;

    public static LivePrototype getInstance() {
        if (sInstance == null) {
            synchronized (sInstanceLock) {
                if (sInstance == null) {
                    sInstance = new LivePrototype();
                }
            }
        }
        return sInstance;
    }

    private LivePrototype() {
    }

    public void init(Context context, InitParam param) {
        init(context, param, null);
    }

    public void init(Context context, InitParam param, final Callback<Void> callback) {
        ofInitializer().init(context, param, new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
                isInited = true;
                if (callback != null) {
                    callback.onSuccess(null);
                }
            }

            @Override
            public void onError(String errorMsg) {
                if (callback != null) {
                    callback.onError(null);
                }
            }
        });
    }

    @NonNull
    private IRoomInitializer<InitParam> ofInitializer() {
        if (roomInitializer == null) {
            roomInitializer = new RoomInitializer<>();
            roomInitializer.setBizType(MonitorhubBizType.STANDARD_LIVE);
        }
        return roomInitializer;
    }

    public void setLiveHook(LiveHook liveHook) {
        this.liveHook = liveHook;
    }

    public LiveHook getLiveHook() {
        return liveHook;
    }

    @Nullable
    public <T> T getHook(@NonNull Function<LiveHook, T> func) {
        if (liveHook != null) {
            return func.apply(liveHook);
        }
        return null;
    }

    public void setup(final Context context, final OpenLiveParam param, Callback<String> callback) {
        Logger.i(TAG, "setup to open live page");
        checkInit();
        final UICallback<String> uiCallback = new UICallback<>(callback);
        if (param == null) {
            uiCallback.onError("param must not be null");
            return;
        }
        if (param.role == null) {
            uiCallback.onError("role must not be null");
            return;
        }
        if (TextUtils.isEmpty(param.nick)) {
            uiCallback.onError("nick参数不能为空");
            return;
        }

        this.openLiveParam = param;
        ofInitializer().initAndLogin(new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
                // 初始化 和 登录 成功
                Role role = param.role;
                String liveId = param.liveId;
                boolean hasLive = !TextUtils.isEmpty(liveId);

                if (role == Role.ANCHOR) {
                    // 主播身份
                    if (hasLive) {
                        // 有直播, 直接进入直播间
                        openLiveRoom(context, null, param, uiCallback);
                    } else {
                        // 无直播, 先创建再进入
                        createAndOpenLiveRoom(context, param, uiCallback);
                    }
                } else {
                    // 观众身份
                    if (hasLive) {
                        // 有直播, 直接进入直播间
                        openLiveRoom(context, null, param, uiCallback);
                    } else {
                        // 无直播, 报错
                        uiCallback.onError(Errors.PARAM_ERROR.getMessage());
                    }
                }
            }

            @Override
            public void onError(String errorMsg) {
                // 初始化 或 登录 失败
                uiCallback.onError(errorMsg);
            }
        });
    }

    /**
     * 获取RoomChannel对象实例
     *
     * @param liveId   直播Id
     * @param callback 回调函数
     */
    public void setup(final String liveId, final Callback<RoomChannel> callback) {
        Logger.i(TAG, "setup to get RoomChannel");
        checkInit();
        final UICallback<RoomChannel> uiCallback = new UICallback<>(callback);
        ofInitializer().initAndLogin(new Callback<Void>() {
            @Override
            public void onSuccess(final Void aVoid) {
                Result<RoomSceneLive> sceneLiveResult = RoomEngine.getInstance().getRoomSceneLive();
                if (!sceneLiveResult.success) {
                    Logger.e(TAG, "sceneLiveResult fail");
                    uiCallback.onError(sceneLiveResult.errorMsg);
                    return;
                }

                RoomSceneLive sceneLive = sceneLiveResult.value;
                if (sceneLive == null) {
                    Logger.e(TAG, "sceneLive == null");
                    uiCallback.onError(Errors.INNER_STATE_ERROR.getMessage());
                    return;
                }

                sceneLive.getLiveDetail(liveId, new Callback<SceneGetLiveDetailRsp>() {
                    @Override
                    public void onSuccess(SceneGetLiveDetailRsp rsp) {
                        String roomId = rsp.roomId;
                        if (TextUtils.isEmpty(roomId)) {
                            Logger.e(TAG, "roomId is empty");
                            uiCallback.onError(Errors.INNER_STATE_ERROR.getMessage());
                            return;
                        }

                        Result<RoomChannel> result = RoomEngine.getInstance().getRoomChannel(roomId);
                        if (result.success) {
                            Logger.i(TAG, "getRoomChannel success");
                            uiCallback.onSuccess(result.value);
                        } else {
                            Logger.e(TAG, "getRoomChannel fail: " + result.errorMsg);
                            uiCallback.onError(result.errorMsg);
                        }
                    }

                    @Override
                    public void onError(String errorMsg) {
                        Logger.e(TAG, "getLiveDetail fail: " + errorMsg);
                        uiCallback.onError(errorMsg);
                    }
                });
            }

            @Override
            public void onError(String errorMsg) {
                Logger.e(TAG, "login fail: " + errorMsg);
                uiCallback.onError(errorMsg);
            }
        });
    }

    @Deprecated
    public String getCurrentNick() {
        return openLiveParam == null ? null : openLiveParam.nick;
    }

    @NonNull
    public OpenLiveParam getOpenLiveParam() {
        return openLiveParam == null ? new OpenLiveParam() : openLiveParam;
    }

    private void createAndOpenLiveRoom(final Context context, final OpenLiveParam param, @NonNull final UICallback<String> uiCallback) {
        Result<RoomSceneLive> result = RoomEngine.getInstance().getRoomSceneLive();
        if (!result.success) {
            uiCallback.onError(result.errorMsg);
            return;
        }

        SceneCreateLiveReq req = new SceneCreateLiveReq();
        String currentUserId = Const.getCurrentUserId();
        req.anchorId = currentUserId;
        req.anchorNick = param.nick;
        req.enableLinkMic = param.supportLinkMic;

        LiveRoomModel model = param.liveRoomModel;
        if (model != null) {
            req.title = TextUtils.isEmpty(model.title) ? "互动直播" : model.title;
            req.notice = model.notice;
            req.coverUrl = model.coverUrl;
            req.extension = model.extension;
        } else {
            req.title = String.format("用户%s的直播", currentUserId);
        }

        result.value.createLive(req, new Callback<SceneCreateLiveRsp>() {
            @Override
            public void onSuccess(SceneCreateLiveRsp data) {
                param.liveId = data.liveId;
                openLiveRoom(context, data, param, uiCallback);
            }

            @Override
            public void onError(String errorMsg) {
                uiCallback.onError(errorMsg);
            }
        });
    }

    private void openLiveRoom(final Context context, SceneCreateLiveRsp createLiveRsp, final OpenLiveParam param,
                              @NonNull final UICallback<String> callback) {
        final LiveInnerParam innerParam = new LiveInnerParam();
        innerParam.liveId = param.liveId;
        innerParam.role = param.role;
        innerParam.supportLinkMic = param.supportLinkMic;

        final Role role = param.role;
        if (createLiveRsp != null && !TextUtils.isEmpty(createLiveRsp.roomId)) {
            // 有roomId时, 直接跳转 (主播新建直播的场景)
            innerParam.liveStatus = LiveStatus.NOT_START;
            innerParam.extension = createLiveRsp.extension;
            Router.openBusinessRoomPage(context, createLiveRsp.roomId, innerParam, param);
            callback.onSuccess(param.liveId);
            return;
        }

        // 无roomId时, 先查询roomId, 再跳转
        final String liveId = param.liveId;
        Result<RoomSceneLive> result = RoomEngine.getInstance().getRoomSceneLive();
        if (!result.success) {
            callback.onError(result.errorMsg);
            return;
        }

        Logger.i(TAG, "getRoomIdByLiveId, liveId = " + liveId);
        result.value.getLiveDetail(liveId, new Callback<SceneGetLiveDetailRsp>() {
            @Override
            public void onSuccess(SceneGetLiveDetailRsp data) {
                Logger.i(TAG, "getRoomIdByLiveId success, data = " + JSON.toJSONString(data));
                // 参数校验
                final String roomId;
                if (data == null || TextUtils.isEmpty(roomId = data.roomId)) {
                    Logger.e(TAG, "live detail data error");
                    callback.onError(Errors.INNER_STATE_ERROR.getMessage());
                    return;
                }

                // 角色校验
                boolean isOwner = TextUtils.equals(Const.getCurrentUserId(), data.anchorId);
                if ((role == Role.ANCHOR && !isOwner) || (role == Role.AUDIENCE && isOwner)) {
                    String message = Errors.ROLE_NOT_MATCH.getMessage();
                    Logger.e(TAG, message);
                    callback.onError(message);
                    return;
                }

                // 状态校验 (未配置回放的已结束直播, 直接提醒错误)
                if (!param.supportPlayback && data.status == LiveStatus.END.value) {
                    String message = Errors.LIVE_END.getMessage();
                    Logger.e(TAG, message);
                    callback.onError(message);
                    return;
                }

                if (isOwner) {
                    // 二次赋值, 针对于主播端的场景, 不依赖外部传递, 直接取实际值
                    innerParam.supportLinkMic = data.enableLinkMic;
                }

                innerParam.liveStatus = LiveStatus.of(data.status);
                innerParam.extension = data.extension;
                Router.openBusinessRoomPage(context, roomId, innerParam, param);
                callback.onSuccess(liveId);
            }

            @Override
            public void onError(String errorMsg) {
                Logger.e(TAG, "getRoomIdByLiveId error, errorMsg = " + errorMsg);
                callback.onError(errorMsg);
            }
        });
    }

    public void switchUser(final String userId, final String userNick, Callback<Void> callback) {
        switchUser(userId, userNick, null, callback);
    }

    public void switchUser(final String userId, final String userNick,
                           final Map<String, String> userExtension, Callback<Void> callback) {
        checkInit();
        final UICallback<Void> uiCallback = new UICallback<Void>(callback) {
            @Override
            public void onSuccess(Void data) {
                isSwitching = false;
                super.onSuccess(data);
            }

            @Override
            public void onError(String errorMsg) {
                isSwitching = false;
                super.onError(errorMsg);
            }
        };

        if (isSwitching) {
            Logger.w(TAG, "switch is invoking");
            uiCallback.onError("操作频繁，请稍后再试");
            return;
        }

        isSwitching = true;

        if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(userNick)) {
            Logger.e(TAG, "userId or userNick is empty");
            Utils.invokeInvalidParamError(uiCallback);
            return;
        }

        if (openLiveParam == null) {
            Logger.e(TAG, "switchUser: open live param is null");
            uiCallback.onError("参数错误");
            return;
        }

        // 1. 保存新用户信息
        Const.injectNewUser(userId);

        // 2. 离开房间
        Logger.i(TAG, "ready leave room");
        LiveActivity.leaveRoom(new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
                // 3. 切换登录 (游客身份登出, 用户身份登录)
                Logger.i(TAG, "leave room success");
                RoomEngine.getInstance().auth(userId, new Callback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        // 4. 重新加载Activity初始化逻辑
                        Logger.i(TAG, "auth success");
                        LiveActivity.reInit(userNick, userExtension);
                        LivePrototype.getInstance().getOpenLiveParam().nick = userNick;
                        Logger.i(TAG, "reInit finish");
                        uiCallback.onSuccess(null);
                    }

                    @Override
                    public void onError(String errorMsg) {
                        Logger.i(TAG, "auth fail: " + errorMsg);
                        uiCallback.onError(errorMsg);
                    }
                });
            }

            @Override
            public void onError(String errorMsg) {
                Logger.e(TAG, "leave room fail: " + errorMsg);
                uiCallback.onError(errorMsg);
            }
        });
    }

    private void checkInit() {
        if (!isInited) {
            throw new RuntimeException("先进行初始化操作");
        }
    }

    /**
     * 初始化参数，所有参数为必传参数，从控制台获取
     */
    public static class InitParam extends BizInitParam {
    }

    /**
     * 打开直播间参数，role为必传参数
     */
    public static class OpenLiveParam extends BaseOpenParam {
        public static final int DEFAULT_LIVE_SHOW_MODE = CanvasScale.Mode.ASPECT_FILL;

        public String liveId;
        public Role role;

        // 可选参数
        @CanvasScale.Mode
        public int liveShowMode = DEFAULT_LIVE_SHOW_MODE;
        // 观众端显示模式自适应
        public boolean showModeAutoFitForAudience = true;
        @Deprecated
        public boolean isAudioOnly = false;
        public boolean supportPlayback = false;
        public boolean supportBackgroundPlay = false;
        public boolean supportLinkMic = false;
        public boolean supportLoadingView = true;
        public boolean commentConfigDisableSendFailToast = false;
        public boolean lowDelay = true;
        public boolean screenCaptureMode = false;
        public AliLiveMediaStreamOptions mediaPusherOptions = null;
        public boolean loadHistoryComment = true;
        public LiveRoomModel liveRoomModel;
        public boolean screenLandscape = false;

        public Integer activityFlags = null;
        public StartActivityCallback startActivityCallback = null;
    }

    /**
     * 角色
     */
    public enum Role {
        /**
         * 主播
         */
        ANCHOR("anchor"),
        /**
         * 观众
         */
        AUDIENCE("audience"),
        ;

        public final String value;

        Role(String value) {
            this.value = value;
        }

        public static Role ofValue(String value) {
            return ANCHOR.value.equals(value) ? ANCHOR : AUDIENCE;
        }
    }
}
