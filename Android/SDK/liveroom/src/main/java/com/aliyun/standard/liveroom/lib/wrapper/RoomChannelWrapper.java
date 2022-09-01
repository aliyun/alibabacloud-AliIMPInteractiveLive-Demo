package com.aliyun.standard.liveroom.lib.wrapper;

import com.alibaba.dingpaas.scenelive.SceneGetLiveDetailRsp;
import com.aliyun.roompaas.base.callback.UICallback;
import com.aliyun.roompaas.base.error.Errors;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.base.exposable.PluginService;
import com.aliyun.roompaas.biz.RoomEngine;
import com.aliyun.roompaas.biz.exposable.RoomChannel;
import com.aliyun.roompaas.biz.exposable.RoomSceneLive;
import com.aliyun.roompaas.biz.exposable.model.LiveRoomInfo;
import com.aliyun.roompaas.biz.exposable.model.Result;
import com.aliyun.roompaas.live.exposable.LiveService;
import com.aliyun.standard.liveroom.lib.LiveContext;
import com.aliyun.standard.liveroom.lib.linkmic.LeaveRoomListener;
import com.aliyun.standard.liveroom.lib.linkmic.LinkMicService;
import com.aliyun.standard.liveroom.lib.linkmic.impl.LinkMicServiceImpl;
import com.aliyun.standard.liveroom.lib.model.LiveRoomModel;

import java.util.Map;

/**
 * {@link RoomChannelExtends} 接口拓展实现类, 拓展底层sdk的功能
 *
 * @author puke
 * @version 2021/9/26
 */
public class RoomChannelWrapper extends RoomChannelProxy implements RoomChannelExtends {

    private final LiveContext liveContext;

    public String nick;
    private LinkMicService linkMicService;
    private LiveService liveService;

    public RoomChannelWrapper(LiveContext liveContext, RoomChannel roomChannel) {
        super(roomChannel);
        this.liveContext = liveContext;
    }

    @Override
    public void enterRoom(String nick, Callback<Void> callback) {
        super.enterRoom(nick, callback);
        this.nick = nick;
    }

    @Override
    public void enterRoom(String nick, Map<String, String> extension, Callback<Void> callback) {
        super.enterRoom(nick, extension, callback);
        this.nick = nick;
    }

    /**
     * 获取插件实例
     *
     * @param pluginServiceType 插件服务类型
     * @param <PS>              具象的插件类
     * @return 插件服务实例
     */
    @Override
    @SuppressWarnings("unchecked")
    public <PS extends PluginService<?>> PS getPluginService(Class<PS> pluginServiceType) {
//        if (RTC_SERVICE_NAME.equals(pluginServiceType.getName())) {
//            throw new UnsupportedOperationException("互动直播低代码SDK不支持RtcService, 请用getMicService替代");
//        }

        // 获取底层原子服务
        PS pluginService = roomChannel.getPluginService(pluginServiceType);

        // 包装直播服务
        if (LiveService.class.isAssignableFrom(pluginServiceType)) {
            if (liveService == null) {
                liveService = new LiveServiceWrapper(liveContext, (LiveService) pluginService);
            }
            return (PS) liveService;
        }

        return pluginService;
    }

    @Override
    public void updateLiveRoom(final LiveRoomModel model, final Callback<Void> callback) {
        final Callback<Void> uiCallback = new UICallback<>(callback);
        if (model == null) {
            uiCallback.onError(Errors.PARAM_ERROR.getMessage());
            return;
        }

        Result<RoomSceneLive> result = RoomEngine.getInstance().getRoomSceneLive();
        if (result.success) {
            final RoomSceneLive sceneLive = result.value;
            final String liveId = roomChannel.getPluginService(LiveService.class).getInstanceId();
            sceneLive.getLiveDetail(liveId, new Callback<SceneGetLiveDetailRsp>() {
                @Override
                public void onSuccess(SceneGetLiveDetailRsp data) {
                    LiveRoomInfo info = new LiveRoomInfo();
                    info.liveId = liveId;
                    info.title = ifNullElse(model.title, data.title);
                    info.notice = ifNullElse(model.notice, data.notice);
                    info.coverUrl = ifNullElse(model.coverUrl, data.coverUrl);
                    info.extension = ifNullElse(model.extension, data.extension);
                    sceneLive.updateLive(info, callback);
                }

                private <T> T ifNullElse(T ifValue, T elseValue) {
                    return ifValue != null ? ifValue : elseValue;
                }

                @Override
                public void onError(String errorMsg) {
                    uiCallback.onError(errorMsg);
                }
            });
        } else {
            uiCallback.onError(result.errorMsg);
        }
    }

    @Override
    public void leaveRoom(Callback<Void> callback) {
        super.leaveRoom(callback);
        if (linkMicService instanceof LeaveRoomListener) {
            ((LeaveRoomListener) linkMicService).onLeaveRoom();
        }
    }

    @Override
    public void leaveRoom(boolean existPage, Callback<Void> callback) {
        super.leaveRoom(existPage, callback);
        if (linkMicService instanceof LeaveRoomListener) {
            ((LeaveRoomListener) linkMicService).onLeaveRoom();
        }
    }

    @Override
    public LinkMicService getLinkMicService() {
        if (linkMicService == null) {
            linkMicService = new LinkMicServiceImpl(liveContext, this);
        }
        return linkMicService;
    }
}
