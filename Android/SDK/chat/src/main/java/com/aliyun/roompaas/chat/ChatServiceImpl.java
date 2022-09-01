package com.aliyun.roompaas.chat;

import android.text.TextUtils;

import com.alibaba.dingpaas.base.DPSError;
import com.alibaba.dingpaas.chat.CancelMuteAllCb;
import com.alibaba.dingpaas.chat.CancelMuteAllReq;
import com.alibaba.dingpaas.chat.CancelMuteAllRsp;
import com.alibaba.dingpaas.chat.CancelMuteUserCb;
import com.alibaba.dingpaas.chat.CancelMuteUserReq;
import com.alibaba.dingpaas.chat.CancelMuteUserRsp;
import com.alibaba.dingpaas.chat.ChatModule;
import com.alibaba.dingpaas.chat.ChatRpcInterface;
import com.alibaba.dingpaas.chat.CommentModel;
import com.alibaba.dingpaas.chat.GetTopicInfoCb;
import com.alibaba.dingpaas.chat.GetTopicInfoReq;
import com.alibaba.dingpaas.chat.GetTopicInfoRsp;
import com.alibaba.dingpaas.chat.ListCommentCb;
import com.alibaba.dingpaas.chat.ListCommentReq;
import com.alibaba.dingpaas.chat.ListCommentRsp;
import com.alibaba.dingpaas.chat.MuteAllCb;
import com.alibaba.dingpaas.chat.MuteAllReq;
import com.alibaba.dingpaas.chat.MuteAllRsp;
import com.alibaba.dingpaas.chat.MuteUserCb;
import com.alibaba.dingpaas.chat.MuteUserReq;
import com.alibaba.dingpaas.chat.MuteUserRsp;
import com.alibaba.dingpaas.chat.SendCommentCb;
import com.alibaba.dingpaas.chat.SendCommentReq;
import com.alibaba.dingpaas.chat.SendCommentRsp;
import com.alibaba.dingpaas.chat.SendCustomMessageCb;
import com.alibaba.dingpaas.chat.SendCustomMessageReq;
import com.alibaba.dingpaas.chat.SendCustomMessageRsp;
import com.alibaba.dingpaas.chat.SendCustomMessageToUsersCb;
import com.alibaba.dingpaas.chat.SendCustomMessageToUsersReq;
import com.alibaba.dingpaas.chat.SendCustomMessageToUsersRsp;
import com.alibaba.dingpaas.chat.SendLikeCb;
import com.alibaba.dingpaas.chat.SendLikeReq;
import com.alibaba.dingpaas.chat.SendLikeRsp;
import com.alibaba.dingpaas.room.RoomNotificationModel;
import com.alibaba.fastjson.JSON;
import com.aliyun.roompaas.base.AbstractPluginService;
import com.aliyun.roompaas.base.ModuleRegister;
import com.aliyun.roompaas.base.RoomContext;
import com.aliyun.roompaas.base.annotation.PluginServiceInject;
import com.aliyun.roompaas.base.callback.UICallback;
import com.aliyun.roompaas.base.error.Errors;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.base.log.Logger;
import com.aliyun.roompaas.base.model.PageModel;
import com.aliyun.roompaas.base.monitor.MonitorHubChannel;
import com.aliyun.roompaas.base.util.CollectionUtil;
import com.aliyun.roompaas.base.util.Utils;
import com.aliyun.roompaas.chat.exposable.ChatEventHandler;
import com.aliyun.roompaas.chat.exposable.ChatService;
import com.aliyun.roompaas.chat.exposable.CommentParam;
import com.aliyun.roompaas.chat.exposable.event.CommentEvent;
import com.aliyun.roompaas.chat.exposable.event.CustomMessageEvent;
import com.aliyun.roompaas.chat.exposable.event.LikeEvent;
import com.aliyun.roompaas.chat.exposable.event.MuteAllCommentEvent;
import com.aliyun.roompaas.chat.exposable.event.MuteCommentEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author puke
 * @version 2021/6/21
 */
@PluginServiceInject
public class ChatServiceImpl extends AbstractPluginService<ChatEventHandler> implements ChatService {

    private static final String TAG = ChatServiceImpl.class.getSimpleName();
    private static final String PLUGIN_ID = "chat";

    // 互动消息
    private static final int CHAT_COMMENT_RECEIVED = 10000;
    private static final int CHAT_LIKE_RECEIVED = 10001;
    private static final int CHAT_MUTED = 10002;
    private static final int CHAT_ALL_MUTED = 10003;
    private static final int CHAT_CUSTOM_MESSAGE = 30000;
//    private static final int ROOM_SYSTEM_MESSAGE = 30001;

    // 发送弹幕的间隔时间 (单位: ms)
    private static final int SEND_COMMENT_INTERVAL_MS = 1000;

    static {
        ModuleRegister.registerLwpModule(ChatModule.getModuleInfo());
    }

    private final ChatRpcInterface chatRpcInterface;
    private final LikeHelper likeHelper;

    private long lastSendTimestamp;
    private GetTopicInfoRsp chatDetail;

    public ChatServiceImpl(RoomContext roomContext) {
        super(roomContext);
        chatRpcInterface = ChatModule.getModule(userId).getRpcInterface();
        likeHelper = new LikeHelper(new LikeRequestCallback());
    }

    @Override
    public GetTopicInfoRsp getChatDetail() {
        return chatDetail;
    }

    @Override
    public void getChatDetail(Callback<GetTopicInfoRsp> callback) {
        final UICallback<GetTopicInfoRsp> uiCallback = new UICallback<>(callback);
        String chatId = getInstanceId();
        if (chatId == null) {
            uiCallback.onError(Errors.INNER_STATE_ERROR.getMessage());
            return;
        }

        GetTopicInfoReq req = new GetTopicInfoReq();
        req.topicId = chatId;
        chatRpcInterface.getTopicInfo(req, new GetTopicInfoCb() {
            @Override
            public void onSuccess(GetTopicInfoRsp rsp) {
                chatDetail = rsp;
                uiCallback.onSuccess(rsp);
            }

            @Override
            public void onFailure(DPSError dpsError) {
                Utils.callErrorWithDps(uiCallback, dpsError);
            }
        });
    }

    @Override
    public void sendComment(String content, Callback<String> callback) {
        sendComment(content, null, callback);
    }

    @Override
    public void sendComment(String content, HashMap<String, String> extension, Callback<String> callback) {
        final UICallback<String> uiCallback = new UICallback<>(callback);
        String chatId = getInstanceId();
        if (chatId == null) {
            uiCallback.onError(Errors.INNER_STATE_ERROR.getMessage());
            return;
        }

        if (TextUtils.isEmpty(content)) {
            Utils.invokeInvalidParamError(uiCallback);
            return;
        }

        // 保证至少有一次调用
        getDetailIfNotCalled();

        long now = System.currentTimeMillis();
        if (now - lastSendTimestamp < getSendCommentIntervalMs()) {
            // 超出发送频率限制
            uiCallback.onError(Errors.TOO_MUCH_FREQUENT.getMessage());
            return;
        }

        lastSendTimestamp = now;
        SendCommentReq req = new SendCommentReq();
        req.topicId = chatId;
        req.content = content;
        req.extension = extension;
        chatRpcInterface.sendComment(req, new SendCommentCb() {
            @Override
            public void onSuccess(SendCommentRsp rsp) {
                uiCallback.onSuccess(rsp.comment.commentId);
            }

            @Override
            public void onFailure(DPSError dpsError) {
                uiCallback.onError(dpsError.reason);
            }
        });
    }


    // 发送弹幕的频率控制
    private int getSendCommentIntervalMs() {
        if (chatDetail != null) {
            int sendCommentInterval = chatDetail.sendCommentInterval;
            if (sendCommentInterval > 0) {
                return sendCommentInterval;
            }
        }
        return SEND_COMMENT_INTERVAL_MS;
    }

    private void getDetailIfNotCalled() {
        if (chatDetail == null) {
            getChatDetail(null);
        }
    }

    @Override
    public void sendCustomMessageToAll(String body, Callback<String> callback) {
        final UICallback<String> uiCallback = new UICallback<>(callback);
        String chatId = getInstanceId();
        if (chatId == null) {
            uiCallback.onError(Errors.INNER_STATE_ERROR.getMessage());
            return;
        }

        SendCustomMessageReq req = new SendCustomMessageReq();
        req.topicId = chatId;
        req.body = body;
        chatRpcInterface.sendCustomMessage(req, new SendCustomMessageCb() {
            @Override
            public void onSuccess(SendCustomMessageRsp rsp) {
                uiCallback.onSuccess(rsp.messageId);
            }

            @Override
            public void onFailure(DPSError dpsError) {
                Utils.callErrorWithDps(uiCallback, dpsError);
            }
        });
    }

    @Override
    public void sendCustomMessageToUsers(String body, List<String> users, Callback<String> callback) {
        final UICallback<String> uiCallback = new UICallback<>(callback);
        String chatId = getInstanceId();
        if (chatId == null) {
            uiCallback.onError(Errors.INNER_STATE_ERROR.getMessage());
            return;
        }
        if (CollectionUtil.isEmpty(users)) {
            Utils.invokeInvalidParamError(uiCallback);
            return;
        }

        SendCustomMessageToUsersReq req = new SendCustomMessageToUsersReq();
        req.topicId = chatId;
        req.body = body;
        req.receiverList = new ArrayList<>(users);
        chatRpcInterface.sendCustomMessageToUsers(req, new SendCustomMessageToUsersCb() {
            @Override
            public void onSuccess(SendCustomMessageToUsersRsp rsp) {
                uiCallback.onSuccess(rsp.messageId);
            }

            @Override
            public void onFailure(DPSError dpsError) {
                Utils.callErrorWithDps(uiCallback, dpsError);
            }
        });
    }

    @Override
    public void banComment(final String userId, int muteSeconds, Callback<Void> callback) {
        final UICallback<Void> uiCallback = new UICallback<>(callback);
        String chatId = getInstanceId();
        if (chatId == null) {
            uiCallback.onError(Errors.INNER_STATE_ERROR.getMessage());
            return;
        }

        MuteUserReq req = new MuteUserReq();
        req.topicId = chatId;
        req.muteUser = userId;
        req.muteTime = muteSeconds;
        chatRpcInterface.muteUser(req, new MuteUserCb() {
            @Override
            public void onSuccess(MuteUserRsp rsp) {
                uiCallback.onSuccess(null);
                MonitorHubChannel.muteUserById(userId, MonitorHubChannel.REPORT_EVENT_SUCCESS_CODE
                , null);
            }

            @Override
            public void onFailure(DPSError dpsError) {
                Utils.callErrorWithDps(uiCallback, dpsError);
                MonitorHubChannel.muteUserById(userId, dpsError.code, dpsError.reason);
            }
        });
    }

    @Override
    public void cancelBanComment(final String userId, Callback<Void> callback) {
        final UICallback<Void> uiCallback = new UICallback<>(callback);
        String chatId = getInstanceId();
        if (chatId == null) {
            uiCallback.onError(Errors.INNER_STATE_ERROR.getMessage());
            return;
        }

        CancelMuteUserReq req = new CancelMuteUserReq();
        req.topicId = chatId;
        req.cancelMuteUser = userId;
        chatRpcInterface.cancelMuteUser(req, new CancelMuteUserCb() {
            @Override
            public void onSuccess(CancelMuteUserRsp rsp) {
                uiCallback.onSuccess(null);
                MonitorHubChannel.cancelMuteUserById(userId, MonitorHubChannel.REPORT_EVENT_SUCCESS_CODE,
                        null);
            }

            @Override
            public void onFailure(DPSError dpsError) {
                Utils.callErrorWithDps(uiCallback, dpsError);
                MonitorHubChannel.cancelMuteUserById(userId, dpsError.code, dpsError.reason);
            }
        });
    }

    @Override
    public void banAllComment(Callback<Void> callback) {
        final UICallback<Void> uiCallback = new UICallback<>(callback);
        String chatId = getInstanceId();
        if (chatId == null) {
            uiCallback.onError(Errors.INNER_STATE_ERROR.getMessage());
            return;
        }

        MuteAllReq req = new MuteAllReq();
        req.topicId = chatId;
        chatRpcInterface.muteAll(req, new MuteAllCb() {
            @Override
            public void onSuccess(MuteAllRsp rsp) {
                uiCallback.onSuccess(null);
                MonitorHubChannel.muteAll(MonitorHubChannel.REPORT_EVENT_SUCCESS_CODE, null);
            }

            @Override
            public void onFailure(DPSError dpsError) {
                Utils.callErrorWithDps(uiCallback, dpsError);
                MonitorHubChannel.muteAll(dpsError.code, dpsError.reason);
            }
        });
    }

    @Override
    public void cancelBanAllComment(Callback<Void> callback) {
        final UICallback<Void> uiCallback = new UICallback<>(callback);
        String chatId = getInstanceId();
        if (chatId == null) {
            uiCallback.onError(Errors.INNER_STATE_ERROR.getMessage());
            return;
        }

        CancelMuteAllReq req = new CancelMuteAllReq();
        req.topicId = chatId;
        chatRpcInterface.cancelMuteAll(req, new CancelMuteAllCb() {
            @Override
            public void onSuccess(CancelMuteAllRsp rsp) {
                uiCallback.onSuccess(null);
                MonitorHubChannel.cancelMuteAll(MonitorHubChannel.REPORT_EVENT_SUCCESS_CODE, null);
            }

            @Override
            public void onFailure(DPSError dpsError) {
                Utils.callErrorWithDps(uiCallback, dpsError);
                MonitorHubChannel.cancelMuteAll(dpsError.code, dpsError.reason);

            }
        });
    }

    @Override
    public void sendLike() {
        likeHelper.doLike();
    }

    @Override
    public void listComment(CommentParam param, Callback<PageModel<CommentModel>> callback) {
        final UICallback<PageModel<CommentModel>> uiCallback = new UICallback<>(callback);
        String chatId = getInstanceId();
        if (chatId == null) {
            uiCallback.onError(Errors.INNER_STATE_ERROR.getMessage());
            return;
        }

        final ListCommentReq req = new ListCommentReq();
        req.pageNum = param.pageNum;
        req.pageSize = param.pageSize;
        CommentSortType sortType = param.sortType == null ? CommentSortType.ASC_BY_TIME : param.sortType;
        req.sortType = sortType.getValue();
        req.topicId = chatId;
        chatRpcInterface.listComment(req, new ListCommentCb() {
            @Override
            public void onSuccess(ListCommentRsp rsp) {
                PageModel<CommentModel> pageModel = new PageModel<>();
                pageModel.list = rsp.commentModelList;
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
    public void onLeaveRoom(boolean existPage) {
        super.onLeaveRoom(existPage);
        likeHelper.release();
    }

    @Override
    public String getPluginId() {
        return PLUGIN_ID;
    }

    @Override
    public void onSyncEvent(RoomNotificationModel model) {
        switch (model.type) {
            case CHAT_COMMENT_RECEIVED:
                final CommentEvent commentEvent = JSON.parseObject(model.data, CommentEvent.class);
                dispatch(new Consumer<ChatEventHandler>() {
                    @Override
                    public void consume(ChatEventHandler eventHandler) {
                        eventHandler.onCommentReceived(commentEvent);
                    }
                });
                break;
            case CHAT_LIKE_RECEIVED:
                final LikeEvent likeEvent = JSON.parseObject(model.data, LikeEvent.class);
                dispatch(new Consumer<ChatEventHandler>() {
                    @Override
                    public void consume(ChatEventHandler eventHandler) {
                        eventHandler.onLikeReceived(likeEvent);
                    }
                });
                break;
            case CHAT_MUTED:
                final MuteCommentEvent muteCommentEvent = JSON.parseObject(model.data, MuteCommentEvent.class);
                syncChatDetail(muteCommentEvent);
                dispatch(new Consumer<ChatEventHandler>() {
                    @Override
                    public void consume(ChatEventHandler eventHandler) {
                        eventHandler.onCommentMutedOrCancel(muteCommentEvent);
                    }
                });
                break;
            case CHAT_ALL_MUTED:
                final MuteAllCommentEvent muteAllCommentEvent = JSON.parseObject(model.data, MuteAllCommentEvent.class);
                syncChatDetail(muteAllCommentEvent);
                dispatch(new Consumer<ChatEventHandler>() {
                    @Override
                    public void consume(ChatEventHandler eventHandler) {
                        eventHandler.onCommentAllMutedOrCancel(muteAllCommentEvent);
                    }
                });
                break;
            case CHAT_CUSTOM_MESSAGE:
                final CustomMessageEvent customMessageEvent = new CustomMessageEvent();
                customMessageEvent.messageId = model.messageId;
                customMessageEvent.data = model.data;
                dispatch(new Consumer<ChatEventHandler>() {
                    @Override
                    public void consume(ChatEventHandler eventHandler) {
                        eventHandler.onCustomMessageReceived(customMessageEvent);
                    }
                });
                break;
            default:
                Logger.w(TAG, "unknown chat message: " + JSON.toJSONString(model));
                break;
        }
    }

    private void syncChatDetail(MuteCommentEvent event) {
        GetTopicInfoRsp chatDetail = getChatDetail();
        if (event != null && chatDetail != null && TextUtils.equals(event.muteUserOpenId, userId)) {
            chatDetail.mute = event.mute;
        }
    }

    private void syncChatDetail(MuteAllCommentEvent event) {
        GetTopicInfoRsp chatDetail = getChatDetail();
        if (event != null && chatDetail != null) {
            chatDetail.muteAll = event.mute;
        }
    }

    private class LikeRequestCallback implements LikeHelper.Callback {

        @Override
        public void onRequest(int likeCount) {
            String chatId = getInstanceId();
            if (chatId == null) {
                Logger.e(TAG, "room state error, chatId is null");
                return;
            }

            final SendLikeReq req = new SendLikeReq();
            req.topicId = chatId;
            req.count = likeCount;
            Logger.i(TAG, "send like start, like count is " + likeCount);
            chatRpcInterface.sendLike(req, new SendLikeCb() {
                @Override
                public void onSuccess(SendLikeRsp rsp) {
                    Logger.i(TAG, "send like success");
                }

                @Override
                public void onFailure(DPSError dpsError) {
                    Logger.e(TAG, String.format("send like fail. code: %d, reason: %s", dpsError.code, dpsError.reason));
                }
            });
        }
    }
}
