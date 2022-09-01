package com.aliyun.standard.liveroom.lib.linkmic.impl;

import com.aliyun.roompaas.biz.exposable.RoomChannel;
import com.aliyun.standard.liveroom.lib.LiveContext;
import com.aliyun.standard.liveroom.lib.linkmic.AudienceService;
import com.aliyun.standard.liveroom.lib.linkmic.LeaveRoomListener;
import com.aliyun.standard.liveroom.lib.linkmic.LinkMicService;

/**
 * @author puke
 * @version 2021/12/31
 */
public class LinkMicServiceImpl implements LinkMicService, LeaveRoomListener {

    private static final String RTC_SERVICE_NAME = "com.aliyun.roompaas.rtc.exposable.RtcService";

    private final RoomChannel roomChannel;

    private AudienceService audienceService;

    public LinkMicServiceImpl(LiveContext liveContext, RoomChannel roomChannel) {
        this.roomChannel = roomChannel;

        // 检查外部是否添加Rtc依赖, 未添加Rtc依赖时, 直接阻断流程 (连麦功能强依赖Rtc)
        try {
            Class.forName(RTC_SERVICE_NAME);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("使用连麦功能, 请先添加Rtc依赖");
        }
    }

    @Override
    public AudienceService getAudienceService() {
        if (audienceService == null) {
            audienceService = new AudienceServiceImpl(roomChannel);
        }
        return audienceService;
    }

    @Override
    public void onLeaveRoom() {
        if (audienceService instanceof LeaveRoomListener) {
            ((LeaveRoomListener) audienceService).onLeaveRoom();
        }
    }
}
