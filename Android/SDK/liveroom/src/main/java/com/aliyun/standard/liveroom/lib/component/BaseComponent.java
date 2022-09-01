package com.aliyun.standard.liveroom.lib.component;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.alibaba.dingpaas.room.RoomDetail;
import com.aliyun.roompaas.biz.exposable.enums.LiveStatus;
import com.aliyun.roompaas.chat.exposable.ChatService;
import com.aliyun.roompaas.live.exposable.LiveService;
import com.aliyun.standard.liveroom.lib.LiveContext;
import com.aliyun.standard.liveroom.lib.LivePrototype;
import com.aliyun.standard.liveroom.lib.event.EventListener;
import com.aliyun.standard.liveroom.lib.event.EventManager;
import com.aliyun.standard.liveroom.lib.wrapper.LivePlayerServiceExtends;
import com.aliyun.standard.liveroom.lib.wrapper.LivePusherServiceExtends;
import com.aliyun.standard.liveroom.lib.wrapper.LiveServiceExtends;
import com.aliyun.standard.liveroom.lib.wrapper.RoomChannelExtends;

/**
 * @author puke
 * @version 2021/7/28
 */
public class BaseComponent implements IComponent, EventListener {

    protected LiveContext liveContext;
    protected EventManager eventManager;
    protected Activity activity;
    protected RoomChannelExtends roomChannel;
    protected LiveServiceExtends liveService;
    protected ChatService chatService;

    @CallSuper
    @Override
    public void onInit(LiveContext liveContext) {
        this.liveContext = liveContext;
        this.eventManager = liveContext.getEventManager();
        this.eventManager.register(this);
        this.activity = liveContext.getActivity();
        this.roomChannel = liveContext.getRoomChannel();
        this.liveService = (LiveServiceExtends) roomChannel.getPluginService(LiveService.class);
        this.chatService = roomChannel.getPluginService(ChatService.class);
    }

    @Override
    public void onEnterRoomSuccess(RoomDetail roomDetail) {

    }

    @Override
    public void onEnterRoomError(String errorMsg) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

    }

    @Override
    public void onActivityResume() {

    }

    @Override
    public void onActivityPause() {

    }

    @Override
    public void onActivityDestroy() {

    }

    @Override
    public void onActivityFinish() {

    }

    @Override
    public void onActivityConfigurationChanged(Configuration newConfig) {

    }

    @Override
    public boolean interceptBackKey() {
        return false;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public void onEvent(String action, Object... args) {

    }

    public final boolean needPlayback() {
        return liveContext != null && liveContext.getLiveStatus() == LiveStatus.END && supportPlayback();
    }

    public final boolean supportPlayback() {
        return getOpenLiveParam().supportPlayback;
    }

    public final boolean supportLinkMic() {
        return liveContext.supportLinkMic();
    }

    /**
     * 发送事件
     *
     * @param action 事件标识
     * @param args   事件参数
     */
    public void postEvent(String action, Object... args) {
        eventManager.post(action, args);
    }

    public boolean isOwner() {
        return liveContext.getRole() == LivePrototype.Role.ANCHOR;
    }

    public void showToast(String message) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
    }

    @NonNull
    public LivePrototype.OpenLiveParam getOpenLiveParam() {
        return LivePrototype.getInstance().getOpenLiveParam();
    }

    public LivePusherServiceExtends getPusherService() {
        return liveService.getPusherService();
    }

    public LivePlayerServiceExtends getPlayerService() {
        return liveService.getPlayerService();
    }

    public boolean isLandscape() {
        return liveContext.isLandscape();
    }

    public void setLandscape(boolean landscape) {
        liveContext.setLandscape(landscape);
    }
}
