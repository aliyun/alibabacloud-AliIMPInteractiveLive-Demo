package com.aliyun.roompaas.biz;

import android.content.Context;
import android.text.TextUtils;

import com.alibaba.dingpaas.base.DPSError;
import com.alibaba.dingpaas.room.EnterRoomCb;
import com.alibaba.dingpaas.room.EnterRoomReq;
import com.alibaba.dingpaas.room.EnterRoomRsp;
import com.alibaba.dingpaas.room.GetRoomDetailCb;
import com.alibaba.dingpaas.room.GetRoomDetailReq;
import com.alibaba.dingpaas.room.GetRoomDetailRsp;
import com.alibaba.dingpaas.room.GetRoomUserListCb;
import com.alibaba.dingpaas.room.GetRoomUserListReq;
import com.alibaba.dingpaas.room.GetRoomUserListRsp;
import com.alibaba.dingpaas.room.KickRoomUserCb;
import com.alibaba.dingpaas.room.KickRoomUserReq;
import com.alibaba.dingpaas.room.KickRoomUserRsp;
import com.alibaba.dingpaas.room.LeaveRoomCb;
import com.alibaba.dingpaas.room.LeaveRoomReq;
import com.alibaba.dingpaas.room.LeaveRoomRsp;
import com.alibaba.dingpaas.room.PluginInstance;
import com.alibaba.dingpaas.room.PluginInstanceInfo;
import com.alibaba.dingpaas.room.PluginInstanceItem;
import com.alibaba.dingpaas.room.RoomDetail;
import com.alibaba.dingpaas.room.RoomExtInterface;
import com.alibaba.dingpaas.room.RoomInfo;
import com.alibaba.dingpaas.room.RoomModule;
import com.alibaba.dingpaas.room.RoomNotificationModel;
import com.alibaba.dingpaas.room.RoomRpcInterface;
import com.alibaba.dingpaas.room.RoomUserModel;
import com.alibaba.dingpaas.room.UpdateRoomNoticeCb;
import com.alibaba.dingpaas.room.UpdateRoomNoticeReq;
import com.alibaba.dingpaas.room.UpdateRoomNoticeRsp;
import com.alibaba.dingpaas.room.UpdateRoomTitleCb;
import com.alibaba.dingpaas.room.UpdateRoomTitleReq;
import com.alibaba.dingpaas.room.UpdateRoomTitleRsp;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.aliyun.roompaas.base.DefaultPluginServiceFactory;
import com.aliyun.roompaas.base.EventHandlerManager;
import com.aliyun.roompaas.base.PluginManager;
import com.aliyun.roompaas.base.PluginServiceFactory;
import com.aliyun.roompaas.base.RoomContext;
import com.aliyun.roompaas.base.callback.UICallback;
import com.aliyun.roompaas.base.error.Errors;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.base.exposable.PluginService;
import com.aliyun.roompaas.base.inner.InnerService;
import com.aliyun.roompaas.base.inner.InnerServiceManager;
import com.aliyun.roompaas.base.log.Logger;
import com.aliyun.roompaas.base.model.PageModel;
import com.aliyun.roompaas.base.monitor.MonitorHubChannel;
import com.aliyun.roompaas.base.util.CollectionUtil;
import com.aliyun.roompaas.base.util.CommonUtil;
import com.aliyun.roompaas.base.util.ThreadUtil;
import com.aliyun.roompaas.base.util.Utils;
import com.aliyun.roompaas.biz.exposable.DocEventHandler;
import com.aliyun.roompaas.biz.exposable.RoomChannel;
import com.aliyun.roompaas.biz.exposable.RoomEventHandler;
import com.aliyun.roompaas.biz.exposable.RoomEventHandlerExtends;
import com.aliyun.roompaas.biz.exposable.event.KickUserEvent;
import com.aliyun.roompaas.biz.exposable.event.RoomInOutEvent;
import com.aliyun.roompaas.biz.exposable.model.ConversionTaskStatus;
import com.aliyun.roompaas.biz.exposable.model.UserParam;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author puke
 * @version 2021/4/28
 */
class RoomChannelImpl extends EventHandlerManager<RoomEventHandler> implements RoomChannel {

    private static final String TAG = RoomChannelImpl.class.getSimpleName();

    private static final int DEFAULT_KICKED_SECONDS = 300;

    // 房间消息
    private static final int ROOM_IN_OUT = 20000;
    private static final int ROOM_UPDATE_TITLE = 20001;
    private static final int ROOM_UPDATE_NOTICE = 20002;
    private static final int ROOM_KICK_USER = 20003;
    private static final int ROOM_UPDATE_LIVE_ROOM_EXTENSION = 20004;
    private static final int ROOM_UPDATE_NOTICE_VERSION2 = 20005;
    private static final int ROOM_DOC_CONVERSION_TASK_STATUS = 2001;
    private static final int ROOM_DOC_CONVERSION_TASK_STATUS_WITH_DOC_ID = 2002;
    private static final String MESSAGE_BODY_DOC_CONVERSION_TASK_STATUS_SUCCESS = "CONVERSION_TASK_STATUS_SUCCESS";
    private static final String MESSAGE_BODY_DOC_CONVERSION_TASK_STATUS_FAILED = "CONVERSION_TASK_STATUS_FAILED";
    /**
     * 与客户端之间的可靠消息约定类型与内容
     * MESSAGE_TYPE_DOC_CONVERSION_TASK_STATUS_TYPE = 2001;
     * MESSAGE_BODY_DOC_CONVERSION_TASK_STATUS_SUCCESS = "CONVERSION_TASK_STATUS_SUCCESS";
     * MESSAGE_BODY_DOC_CONVERSION_TASK_STATUS_FAILED = "CONVERSION_TASK_STATUS_FAILED";
     * MESSAGE_BIZ_TYPE = "Doc";
     */

    // init
    private final Context context;
    private final String userId;
    private final String roomId;
    private final OnDestroyListener destroyListener;
    private final PluginServiceFactory pluginServiceFactory;
    private final InnerServiceManager innerServiceManager;
    private final RoomContext roomContext;
    private final RoomExtInterface roomExtInterface;
    private final RoomRpcInterface roomRpcInterface;
    private final PluginManager pluginManager;

    // state
    private RoomDetail roomDetail;

    // 用来自动enterRoom
    String nick;
    Map<String, String> extension;

    public interface OnDestroyListener {
        void onDestroy(RoomChannel channel);
    }

    public RoomChannelImpl(Context context, String userId, String roomId, OnDestroyListener destroyListener) {
        // 初始化参数
        this.context = context;
        this.userId = userId;
        this.roomId = roomId;
        this.destroyListener = destroyListener;
        this.pluginServiceFactory = new DefaultPluginServiceFactory();
        this.innerServiceManager = new InnerServiceManager();
        this.roomContext = new RoomContextImpl();

        this.roomExtInterface = RoomModule.getModule(userId).getExtInterface();
        this.roomRpcInterface = RoomModule.getModule(userId).getRpcInterface();
        this.pluginManager = new PluginManager();

        // 设置sync消息监听
        this.roomExtInterface.setListener(roomId, new InnerSyncEventDispatcher());
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public String getRoomId() {
        return roomId;
    }

    @Override
    public void listUser(final UserParam param, Callback<PageModel<RoomUserModel>> callback) {
        final UICallback<PageModel<RoomUserModel>> uiCallback = new UICallback<>(callback);
        GetRoomUserListReq req = new GetRoomUserListReq();
        req.roomId = roomId;
        req.pageNum = param.pageNum;
        req.pageSize = param.pageSize;
        roomRpcInterface.getRoomUserList(req, new GetRoomUserListCb() {
            @Override
            public void onSuccess(GetRoomUserListRsp rsp) {
                PageModel<RoomUserModel> pageModel = new PageModel<>();
                pageModel.list = rsp.userList;
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

    @Override
    public void kickUser(String userId, Callback<Void> callback) {
        kickUser(userId, DEFAULT_KICKED_SECONDS, callback);
    }

    @Override
    public void kickUser(String userId, int kickedSeconds, Callback<Void> callback) {
        final UICallback<Void> uiCallback = new UICallback<>(callback);
        if (!isOwner()) {
            Logger.e(TAG, "audience hasn't permission");
            uiCallback.onError(Errors.BIZ_PERMISSION_DENIED.getMessage());
            return;
        }

        KickRoomUserReq req = new KickRoomUserReq();
        req.roomId = roomId;
        req.kickUser = userId;
        req.blockTime = kickedSeconds;
        roomRpcInterface.kickRoomUser(req, new KickRoomUserCb() {
            @Override
            public void onSuccess(KickRoomUserRsp rsp) {
                uiCallback.onSuccess(null);
            }

            @Override
            public void onFailure(DPSError dpsError) {
                Utils.callErrorWithDps(uiCallback, dpsError);
            }
        });
    }

    @Override
    public void updateTitle(String title, Callback<Void> callback) {
        final UICallback<Void> uiCallback = new UICallback<>(callback);
        if (!isOwner()) {
            Logger.e(TAG, "audience hasn't permission");
            uiCallback.onError(Errors.BIZ_PERMISSION_DENIED.getMessage());
            return;
        }

        UpdateRoomTitleReq req = new UpdateRoomTitleReq();
        req.roomId = roomId;
        req.title = title;
        roomRpcInterface.updateRoomTitle(req, new UpdateRoomTitleCb() {
            @Override
            public void onSuccess(UpdateRoomTitleRsp rsp) {
                uiCallback.onSuccess(null);
            }

            @Override
            public void onFailure(DPSError dpsError) {
                Utils.callErrorWithDps(uiCallback, dpsError);
            }
        });
    }

    @Override
    public void updateNotice(String notice, Callback<Void> callback) {
        final UICallback<Void> uiCallback = new UICallback<>(callback);
        if (!isOwner()) {
            Logger.e(TAG, "audience hasn't permission");
            uiCallback.onError(Errors.BIZ_PERMISSION_DENIED.getMessage());
            return;
        }

        UpdateRoomNoticeReq req = new UpdateRoomNoticeReq();
        req.roomId = roomId;
        req.notice = notice;
        roomRpcInterface.updateRoomNotice(req, new UpdateRoomNoticeCb() {
            @Override
            public void onSuccess(UpdateRoomNoticeRsp rsp) {
                uiCallback.onSuccess(null);
            }

            @Override
            public void onFailure(DPSError dpsError) {
                Utils.callErrorWithDps(uiCallback, dpsError);
            }
        });
    }

    @Override
    public void enterRoom(String nick, final Callback<Void> callback) {
        enterRoom(nick, null, callback);
    }

    @Override
    public void enterRoom(String nick, Map<String, String> extension, final Callback<Void> callback) {
        enterRoomInternal(nick, extension, null, callback);
    }

    void enterRoomInternal(String nick, Map<String, String> extension, Map<String, String> context, final Callback<Void> callback) {
        Logger.i(TAG, String.format("enterRoom, nick: %s, roomId: %s", nick, roomId));
        this.nick = nick;
        this.extension = extension;
        EnterRoomReq req = new EnterRoomReq();
        req.roomId = roomId;
        req.nick = nick;
        if (extension != null) {
            req.extension = new HashMap<>(extension);
        }
        if (context != null) {
            req.context = new HashMap<>(context);
        }
        final UICallback<Void> uiCallback = new UICallback<>(callback);
        roomRpcInterface.enterRoom(req, new EnterRoomCb() {
            @Override
            public void onSuccess(EnterRoomRsp enterRoomRsp) {
                Logger.i(TAG, "enterRoom success");
                AutoEnterRoomHandler.add(RoomChannelImpl.this);
                getRoomDetail(new Callback<RoomDetail>() {
                    @Override
                    public void onSuccess(RoomDetail data) {
                        uiCallback.onSuccess(null);
                    }

                    @Override
                    public void onError(String errorMsg) {
                        uiCallback.onError(errorMsg);
                    }
                });
                MonitorHubChannel.enterRoom(MonitorHubChannel.REPORT_EVENT_SUCCESS_CODE, null);
            }

            @Override
            public void onFailure(DPSError dpsError) {
                Logger.i(TAG, "enterRoom fail, " + dpsError.reason);
                Utils.callErrorWithDps(uiCallback, dpsError);
                MonitorHubChannel.enterRoom(dpsError.code, dpsError.reason);
            }
        });
    }

    @Override
    public RoomDetail getRoomDetail() {
        return roomDetail;
    }

    private RoomDetail ConvertGetRoomDetailRspToRoomDetail(GetRoomDetailRsp getRoomDetailRsp) {
        RoomInfo roomInfo = new RoomInfo();
        roomInfo.adminIdList = getRoomDetailRsp.adminIdList;
        roomInfo.extension = getRoomDetailRsp.extension;
        roomInfo.notice = getRoomDetailRsp.notice;
        roomInfo.onlineCount = getRoomDetailRsp.onlineCount;
        roomInfo.ownerId = getRoomDetailRsp.ownerId;
        roomInfo.pv = getRoomDetailRsp.pv;
        roomInfo.roomId = getRoomDetailRsp.roomId;
        roomInfo.title = getRoomDetailRsp.title;
        roomInfo.uv = getRoomDetailRsp.uv;

        ArrayList<PluginInstanceItem> instanceList = new ArrayList<PluginInstanceItem>();
        for (PluginInstance instanceItem : getRoomDetailRsp.pluginInstanceModelList) {
            PluginInstanceItem pluginInstanceItem = new PluginInstanceItem();
            pluginInstanceItem.createTime = instanceItem.createTime;
            pluginInstanceItem.extension = instanceItem.extension;
            pluginInstanceItem.instanceId = instanceItem.instanceId;
            pluginInstanceItem.pluginId = instanceItem.pluginId;
            instanceList.add(pluginInstanceItem);
        }
        PluginInstanceInfo pluginInstanceInfo = new PluginInstanceInfo();
        pluginInstanceInfo.instanceList = instanceList;
        roomInfo.pluginInstanceInfo = pluginInstanceInfo;
        RoomDetail roomDetail = new RoomDetail();
        roomDetail.roomInfo = roomInfo;

        return roomDetail;
    }

    @Override
    public void getRoomDetail(final Callback<RoomDetail> callback) {
        final UICallback<RoomDetail> uiCallback = new UICallback<>(callback);
        roomRpcInterface.getRoomDetail(new GetRoomDetailReq(roomId), new GetRoomDetailCb() {
            @Override
            public void onSuccess(GetRoomDetailRsp getRoomDetailRsp) {
                // 存储最新房间信息
                RoomChannelImpl.this.roomDetail = ConvertGetRoomDetailRspToRoomDetail(getRoomDetailRsp);
                // 从RoomDetail中同步最新的Plugin信息
                syncPluginInstanceFromRoomDetail(roomDetail);

                uiCallback.onSuccess(roomDetail);
            }

            @Override
            public void onFailure(DPSError dpsError) {
                Utils.callErrorWithDps(uiCallback, dpsError);
            }
        });
    }

    private void syncPluginInstanceFromRoomDetail(RoomDetail roomDetail) {
        PluginInstanceInfo pluginInstanceInfo = null;
        if (roomDetail != null) {
            RoomInfo roomInfo = roomDetail.getRoomInfo();
            if (roomInfo != null) {
                pluginInstanceInfo = roomInfo.pluginInstanceInfo;
            }
        }
        pluginManager.setPluginInstanceInfo(pluginInstanceInfo);
    }

    @Override
    public void leaveRoom(final Callback<Void> callback) {
        leaveRoom(true, callback);
    }

    @Override
    public void leaveRoom(boolean existPage, final Callback<Void> callback) {
        doLeaveRoom(existPage, callback);
    }

    // 离开房间
    private void doLeaveRoom(final boolean existPage, Callback<Void> callback) {
        // 通知插件服务离开房间的事件
        List<PluginService<?>> allPluginService = pluginServiceFactory.getAllPluginService();
        for (PluginService<?> pluginService : allPluginService) {
            pluginService.onLeaveRoom(existPage);
        }

        final UICallback<Void> uiCallback = new UICallback<>(callback);
        AutoEnterRoomHandler.remove(RoomChannelImpl.this);
        roomRpcInterface.leaveRoom(new LeaveRoomReq(roomId), new LeaveRoomCb() {
            @Override
            public void onSuccess(LeaveRoomRsp leaveRoomRsp) {
                roomExtInterface.setListener(roomId, null);

                if (destroyListener != null) {
                    destroyListener.onDestroy(RoomChannelImpl.this);
                }
                uiCallback.onSuccess(null);
                MonitorHubChannel.leaveRoom(MonitorHubChannel.REPORT_EVENT_SUCCESS_CODE, null);
            }

            @Override
            public void onFailure(DPSError dpsError) {
                Utils.callErrorWithDps(uiCallback, dpsError);
                MonitorHubChannel.leaveRoom(dpsError.code, dpsError.reason);
            }
        });
    }

    @Override
    public boolean isOwner() {
        return isOwner(userId);
    }

    @Override
    public boolean isOwner(String userId) {
        RoomInfo roomInfo = roomDetail == null ? null : roomDetail.roomInfo;
        if (roomInfo == null && BuildConfig.DEBUG) {
            CommonUtil.showDebugToast(context, "isOwner: roomInfo null");
        }

        return roomInfo != null && TextUtils.equals(userId, roomInfo.ownerId);
    }

    @Override
    public boolean isAdmin() {
        return isAdmin(userId);
    }

    @Override
    public boolean isAdmin(String userId) {
        if (roomDetail != null && !TextUtils.isEmpty(userId)) {
            RoomInfo roomInfo = roomDetail.roomInfo;
            if (roomInfo != null) {
                ArrayList<String> adminIdList = roomInfo.adminIdList;
                if (CollectionUtil.isNotEmpty(adminIdList)) {
                    return adminIdList.contains(userId);
                }
            }
        }
        return false;
    }

    @Override
    public <PS extends PluginService<?>> PS getPluginService(Class<PS> pluginServiceType) {
        return pluginServiceFactory.getPluginService(pluginServiceType, roomContext);
    }

    private void postEvent(Consumer<RoomEventHandler> consumer) {
        dispatch(consumer);
    }

    private class InnerSyncEventDispatcher extends SyncEventDispatcher {

        @Override
        public void onRoomMessage(final RoomNotificationModel model) {
            ThreadUtil.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dispatchRoomMessage(model);
                }
            });
        }

        private void dispatchRoomMessage(final RoomNotificationModel model) {
            Logger.i(TAG, "dispatchRoomMessage: " + model);
            switch (model.type) {
                case ROOM_IN_OUT:
                    final RoomInOutEvent inOutModel = JSON.parseObject(model.data, RoomInOutEvent.class);
                    postEvent(new Consumer<RoomEventHandler>() {
                        @Override
                        public void consume(RoomEventHandler eventHandler) {
                            eventHandler.onEnterOrLeaveRoom(inOutModel);
                        }
                    });
                    break;
                case ROOM_UPDATE_TITLE:
                    postEvent(new Consumer<RoomEventHandler>() {
                        @Override
                        public void consume(RoomEventHandler eventHandler) {
                            eventHandler.onRoomTitleChanged(model.data);
                        }
                    });
                    break;
                case ROOM_UPDATE_NOTICE:
                    postEvent(new Consumer<RoomEventHandler>() {
                        @Override
                        public void consume(RoomEventHandler eventHandler) {
                            eventHandler.onRoomNoticeChanged(model.data);
                        }
                    });
                    break;
                case ROOM_UPDATE_NOTICE_VERSION2:
                    postEvent(new Consumer<RoomEventHandler>() {
                        @Override
                        public void consume(RoomEventHandler eventHandler) {
                            NoticeBean inOutModel = JSON.parseObject(model.data, NoticeBean.class);
                            eventHandler.onRoomNoticeChanged(inOutModel != null ? inOutModel.notice : "");
                        }
                    });
                    break;
                case ROOM_KICK_USER:
                    final KickUserEvent kickUserEvent = JSON.parseObject(model.data, KickUserEvent.class);
                    postEvent(new Consumer<RoomEventHandler>() {
                        @Override
                        public void consume(RoomEventHandler eventHandler) {
                            eventHandler.onRoomUserKicked(kickUserEvent);
                        }
                    });
                    break;
                case ROOM_UPDATE_LIVE_ROOM_EXTENSION:
                    Type type = new TypeReference<Map<String, String>>() {
                    }.getType();
                    Map<String, String> extension = null;
                    try {
                        extension = JSON.parseObject(model.data, type);
                    } catch (Exception e) {
                        Logger.e(TAG, "Parse update live room extension event error", e);
                    }
                    if (extension == null) {
                        extension = new HashMap<>();
                    }
                    final Map<String, String> finalExtension = extension;
                    postEvent(new Consumer<RoomEventHandler>() {
                        @Override
                        public void consume(RoomEventHandler eventHandler) {
                            if ((eventHandler instanceof RoomEventHandlerExtends)) {
                                ((RoomEventHandlerExtends) eventHandler).onLiveRoomExtensionChanged(finalExtension);
                            }
                        }
                    });
                    break;
                default:
                    Logger.w(TAG, "unknown room message: " + JSON.toJSONString(model));
                    break;
            }
        }

        @Override
        public void onDocMessage(final RoomNotificationModel model) {
            super.onDocMessage(model);
            Logger.i(TAG, "onDocMessage: " + model);
            switch (model.type) {
                case ROOM_DOC_CONVERSION_TASK_STATUS:
                    postEvent(new Consumer<RoomEventHandler>() {
                        @Override
                        public void consume(RoomEventHandler eventHandler) {
                            if ((eventHandler instanceof DocEventHandler)) {
                                ((DocEventHandler) eventHandler).onDocConversionTaskStatus(parseStatus(model.data));
                            }
                        }
                    });
                    break;
                case ROOM_DOC_CONVERSION_TASK_STATUS_WITH_DOC_ID:
                    postEvent(new Consumer<RoomEventHandler>() {
                        @Override
                        public void consume(RoomEventHandler eventHandler) {
                            ConversionTaskStatus bean = JSON.parseObject(model.data, ConversionTaskStatus.class);
                            if ((eventHandler instanceof DocEventHandler) && bean != null) {
                                ((DocEventHandler) eventHandler).onDocConversionTaskStatus(parseStatus(bean.status), bean.sourceDocId, bean.targetDocId);
                            }
                        }
                    });
                    break;
                default:
                    Logger.w(TAG, "unknown onDocMessage message: " + JSON.toJSONString(model));
                    break;
            }
        }

        private int parseStatus(String str) {
            return MESSAGE_BODY_DOC_CONVERSION_TASK_STATUS_SUCCESS.equals(str) ? 0 :
                    MESSAGE_BODY_DOC_CONVERSION_TASK_STATUS_FAILED.equals(str) ? -1 : -1;
        }

        @Override
        List<PluginService<?>> getPluginServices() {
            return pluginServiceFactory.getAllPluginService();
        }
    }

    private class RoomContextImpl implements RoomContext {

        @Override
        public String getUserId() {
            return userId;
        }

        @Override
        public String getRoomId() {
            return roomId;
        }

        @Override
        public Context getContext() {
            return context;
        }

        @Override
        public PluginManager getPluginManager() {
            return pluginManager;
        }

        @Override
        public RoomDetail getRoomDetail() {
            return roomDetail;
        }

        @Override
        public boolean isOwner(String userId) {
            return RoomChannelImpl.this.isOwner(userId);
        }

        @Override
        public <IS extends InnerService> IS getInnerService(Class<IS> innerServiceType) {
            return innerServiceManager.getService(innerServiceType, this);
        }
    }
}