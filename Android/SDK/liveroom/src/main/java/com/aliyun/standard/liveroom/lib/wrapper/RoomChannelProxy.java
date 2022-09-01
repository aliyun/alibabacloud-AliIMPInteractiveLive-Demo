package com.aliyun.standard.liveroom.lib.wrapper;

import com.alibaba.dingpaas.room.RoomDetail;
import com.alibaba.dingpaas.room.RoomUserModel;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.base.exposable.PluginService;
import com.aliyun.roompaas.base.model.PageModel;
import com.aliyun.roompaas.biz.exposable.RoomChannel;
import com.aliyun.roompaas.biz.exposable.RoomEventHandler;
import com.aliyun.roompaas.biz.exposable.model.UserParam;

import java.util.Map;

/**
 * 底层SDK的Api代理类, 该类只做函数代理, 不做其他任何拓展信息
 *
 * @author puke
 * @version 2021/9/26
 */
class RoomChannelProxy implements RoomChannel {

    protected final RoomChannel roomChannel;

    public RoomChannelProxy(RoomChannel roomChannel) {
        this.roomChannel = roomChannel;
    }

    @Override
    public String getUserId() {
        return roomChannel.getUserId();
    }

    @Override
    public String getRoomId() {
        return roomChannel.getRoomId();
    }

    @Override
    public RoomDetail getRoomDetail() {
        return roomChannel.getRoomDetail();
    }

    @Override
    public boolean isOwner() {
        return roomChannel.isOwner();
    }

    @Override
    public boolean isOwner(String userId) {
        return roomChannel.isOwner(userId);
    }

    @Override
    public boolean isAdmin() {
        return roomChannel.isAdmin();
    }

    @Override
    public boolean isAdmin(String userId) {
        return roomChannel.isAdmin(userId);
    }

    @Override
    public void enterRoom(String nick, Callback<Void> callback) {
        roomChannel.enterRoom(nick, callback);
    }

    @Override
    public void enterRoom(String nick, Map<String, String> extension, Callback<Void> callback) {
        roomChannel.enterRoom(nick, extension, callback);
    }

    @Override
    public void listUser(UserParam param, Callback<PageModel<RoomUserModel>> callback) {
        roomChannel.listUser(param, callback);
    }

    @Override
    public void kickUser(String userId, Callback<Void> callback) {
        roomChannel.kickUser(userId, callback);
    }

    @Override
    public void kickUser(String userId, int kickedSeconds, Callback<Void> callback) {
        roomChannel.kickUser(userId, kickedSeconds, callback);
    }

    @Override
    public void updateTitle(String title, Callback<Void> callback) {
        roomChannel.updateTitle(title, callback);
    }

    @Override
    public void updateNotice(String notice, Callback<Void> callback) {
        roomChannel.updateNotice(notice, callback);
    }

    @Override
    public void getRoomDetail(Callback<RoomDetail> callback) {
        roomChannel.getRoomDetail(callback);
    }

    @Override
    public void leaveRoom(Callback<Void> callback) {
        roomChannel.leaveRoom(callback);
    }

    @Override
    public void leaveRoom(boolean existPage, Callback<Void> callback) {
        roomChannel.leaveRoom(existPage, callback);
    }

    @Override
    public <PS extends PluginService<?>> PS getPluginService(Class<PS> pluginServiceType) {
        return roomChannel.getPluginService(pluginServiceType);
    }

    @Override
    public void addEventHandler(RoomEventHandler eventHandler) {
        roomChannel.addEventHandler(eventHandler);
    }

    @Override
    public void removeEventHandler(RoomEventHandler eventHandler) {
        roomChannel.removeEventHandler(eventHandler);
    }

    @Override
    public void removeAllEventHandler() {
        roomChannel.removeAllEventHandler();
    }
}
