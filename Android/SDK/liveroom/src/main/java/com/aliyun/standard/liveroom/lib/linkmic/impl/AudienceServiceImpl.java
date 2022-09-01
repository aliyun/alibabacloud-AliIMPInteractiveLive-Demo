package com.aliyun.standard.liveroom.lib.linkmic.impl;

import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.base.log.Logger;
import com.aliyun.roompaas.biz.exposable.RoomChannel;
import com.aliyun.roompaas.rtc.exposable.RtcUserStatus;
import com.aliyun.standard.liveroom.lib.linkmic.AudienceService;

/**
 * @author puke
 * @version 2022/1/5
 */
public class AudienceServiceImpl extends CommonServiceImpl implements AudienceService {

    private static final String TAG = AudienceServiceImpl.class.getSimpleName();

    public AudienceServiceImpl(RoomChannel roomChannel) {
        super(roomChannel);
    }

    @Override
    public void handleInvite(boolean agree) {
        if (state != State.INVITED) {
            Logger.w(TAG, String.format("handleInvite, current state is %s, not invited.", state));
            return;
        }

        if (agree) {
            // 同意, 加入
            join();
        } else {
            // 拒绝, 拒绝并上报状态
            state = State.OFFLINE;
            rtcService.reportJoinStatus(RtcUserStatus.JOIN_FAILED, null);
        }
    }

    @Override
    public void apply(Callback<Void> callback) {
        if (state == State.ONLINE) {
            Logger.w(TAG, "apply, current state is online.");
            return;
        }

        state = State.APPLYING;
        rtcService.applyJoinRtc(true, callback);
    }

    @Override
    public void cancelApply(Callback<Void> callback) {
        if (state != State.APPLYING) {
            Logger.w(TAG, String.format("cancelApply, current state is %s, not applying.", state));
            return;
        }

        state = State.OFFLINE;
        rtcService.applyJoinRtc(false, callback);
    }

    @Override
    public void handleApplyResponse(boolean join) {
        if (state != State.APPLYING) {
            Logger.w(TAG, String.format("handleApplyResponse, current state is %s, not applying.", state));
            return;
        }

        if (join) {
            // 加入
            join();
        } else {
            // 拒绝, 更改并上报状态
            state = State.OFFLINE;
            // 跟iOS对齐, 不需要添加
//            rtcService.reportJoinStatus(RtcUserStatus.JOIN_FAILED, null);
        }
    }
}
