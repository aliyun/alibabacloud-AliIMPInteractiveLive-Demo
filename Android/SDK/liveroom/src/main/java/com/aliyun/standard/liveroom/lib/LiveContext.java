package com.aliyun.standard.liveroom.lib;

import android.app.Activity;
import android.view.View;

import com.aliyun.roompaas.biz.exposable.enums.LiveStatus;
import com.aliyun.standard.liveroom.lib.event.EventManager;
import com.aliyun.standard.liveroom.lib.wrapper.RoomChannelExtends;

/**
 * @author puke
 * @version 2021/7/29
 */
public interface LiveContext {

    RoomChannelExtends getRoomChannel();

    Activity getActivity();

    LivePrototype.Role getRole();

    String getNick();

    LiveStatus getLiveStatus();

    EventManager getEventManager();

    boolean isPushing();

    boolean isSwitchUser();

    void setPushing(boolean isPushing);

    /**
     * @return 判断当前是否是横屏
     */
    boolean isLandscape();

    /**
     * 设置是否横屏
     *
     * @param landscape true:横屏; false:竖屏;
     */
    void setLandscape(boolean landscape);

    boolean supportLinkMic();

    View getAdjustBottomView();
}
