package com.aliyun.standard.liveroom.lib.linkmic.impl;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.aliyun.roompaas.base.IDestroyable;
import com.aliyun.roompaas.base.log.Logger;
import com.aliyun.roompaas.base.util.Utils;
import com.aliyun.roompaas.roombase.Const;
import com.aliyun.roompaas.rtc.exposable.RtcService;
import com.aliyun.roompaas.rtc.exposable.event.RtcStreamEvent;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;

/**
 * Created by KyleCe on 2021/9/13
 */
public class RtcSubscribeDelegate implements IDestroyable {
    public static final String TAG = "RtcSubscribeDelegate";
    private final Reference<RtcService> rtcServiceRef;
    private final HashMap<String, Boolean> subscribeStatusMap;

    public RtcSubscribeDelegate(RtcService rtcService) {
        rtcServiceRef = new WeakReference<>(rtcService);
        subscribeStatusMap = new HashMap<>();
    }

    public static RtcStreamEvent asRtcStreamEvent(String userId) {
        return new RtcStreamEvent.Builder().setUserId(userId).build();
    }

    public void subscribe(@Nullable List<RtcStreamEvent> eventList) {
        if (eventList == null) {
            return;
        }
        for (RtcStreamEvent event : eventList) {
            subscribe(event);
        }
    }

    public void subscribe(@Nullable String userId) {
        subscribe(asRtcStreamEvent(userId));
    }

    public void subscribe(@Nullable RtcStreamEvent event) {
        updateRtcStreamConfig(event, true);
    }

    public void unsubscribe(@Nullable String userId) {
        unsubscribe(asRtcStreamEvent(userId));
    }

    public void unsubscribe(@Nullable List<RtcStreamEvent> eventList) {
        if (eventList == null) {
            return;
        }
        for (RtcStreamEvent event : eventList) {
            unsubscribe(event);
        }
    }

    public void unsubscribe(@Nullable RtcStreamEvent event) {
        updateRtcStreamConfig(event, false);
    }

    private void updateRtcStreamConfig(@Nullable RtcStreamEvent event, boolean enable) {
        Logger.i(TAG, "updateRtcStreamConfig: " + event + ",enable=" + enable);
        RtcService rtcService = Utils.getRef(rtcServiceRef);
        if (rtcService == null || event == null || TextUtils.isEmpty(event.userId)) {
            Logger.i(TAG, "updateRtcStreamConfig: end--invalid param: rtcService=" + rtcService + ",event=" + event);
            return;
        }

        if (!TextUtils.equals(event.userId, Const.getCurrentUserId())) {
            updateSubscribeStatusWhenDiffFromLatest(rtcService, event.userId, enable);
        } else {
            rtcService.publishLocalVideo(enable);
        }
    }

    private void updateSubscribeStatusWhenDiffFromLatest(@NonNull RtcService rtcService, String uid, boolean enable) {
        Boolean old;
        if (!subscribeStatusMap.containsKey(uid) || (old = subscribeStatusMap.get(uid)) == null || (old ^ enable)) {
            rtcService.configRemoteCameraTrack(uid, false, enable);
            subscribeStatusMap.put(uid, enable);
        }
    }

    @Override
    public void destroy() {
        Utils.clear(rtcServiceRef);
        Utils.clear(subscribeStatusMap);
    }
}
