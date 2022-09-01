package com.aliyun.roompaas.app.delegate.rtc;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.alivc.rtc.AliRtcEngine;
import com.aliyun.roompaas.app.Const;
import com.aliyun.roompaas.base.IDestroyable;
import com.aliyun.roompaas.base.log.Logger;
import com.aliyun.roompaas.base.util.Utils;
import com.aliyun.roompaas.biz.exposable.RoomChannel;
import com.aliyun.roompaas.rtc.RtcStreamEventHelper;
import com.aliyun.roompaas.rtc.exposable.RtcService;
import com.aliyun.roompaas.rtc.exposable.event.RtcStreamEvent;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Created by KyleCe on 2021/9/13
 */
public class RtcSubscribeDelegate implements IDestroyable {
    public static final String TAG = "RtcSubscribeDelegate";
    private final Reference<RtcService> rtcServiceRef;
    private final Reference<RoomChannel> roomChannelRef;
    @NonNull
    private final HashMap<String, Boolean> subscribeStatusMap;
    private final HashMap<String, AliRtcEngine.AliRtcVideoTrack> subscribedTrackMap;

    public RtcSubscribeDelegate(RtcService rtcService, RoomChannel roomChannel) {
        rtcServiceRef = new WeakReference<>(rtcService);
        roomChannelRef = new WeakReference<>(roomChannel);
        subscribeStatusMap = new HashMap<>();
        subscribedTrackMap = new HashMap<>();
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
        subscribe(RtcStreamEventHelper.asRtcStreamEvent(userId));
    }

    public void subscribe(@Nullable Collection<String> idc) {
        if (Utils.isEmpty(idc)) {
            return;
        }

        for (String id : idc) {
            subscribe(RtcStreamEventHelper.asRtcStreamEvent(id));
        }
    }

    public void subscribe(@Nullable RtcStreamEvent event) {
        updateRtcStreamConfig(event, true);
    }

    public void unsubscribe(@Nullable String userId) {
        unsubscribe(RtcStreamEventHelper.asRtcStreamEvent(userId));
    }

    public void unsubscribe(@Nullable Collection<String> idc) {
        if (Utils.isEmpty(idc)) {
            return;
        }

        for (String id : idc) {
            unsubscribe(RtcStreamEventHelper.asRtcStreamEvent(id));
        }
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
        Logger.i(TAG, "updateRtcStreamConfig: enable=" + enable + ",event=" + event);
        RtcService rtcService = Utils.getRef(rtcServiceRef);
        String uid;
        if (rtcService == null || event == null || TextUtils.isEmpty((uid = event.userId))) {
            Logger.i(TAG, "updateRtcStreamConfig: end--invalid param: rtcService=" + rtcService + ",event=" + event);
            return;
        }

        if (!TextUtils.equals(uid, Const.currentUserId)) {
            updateSubscribeStatusWhenDiffFromLatest(rtcService, event, enable);
        }
    }

    private void updateSubscribeStatusWhenDiffFromLatest(@NonNull RtcService rtcService, @NonNull RtcStreamEvent event, boolean enable) {
        Boolean old;
        String uid = event.userId;
        boolean subStatusChanged = !subscribeStatusMap.containsKey(uid) || (old = subscribeStatusMap.get(uid)) == null || (old ^ enable);
        boolean subTrackChanged = (subscribedTrackMap.containsKey(uid) && subscribedTrackMap.get(uid) != event.aliRtcVideoTrack);
        if (subStatusChanged || subTrackChanged) {
            Logger.i(TAG, "updateSubscribeStatusWhenDiffFromLatest: enable=" + enable + ",event=" + event);
            rtcService.configRemoteCameraTrack(uid, isOwnerAndTeacher(uid), enable);
            subscribeStatusMap.put(uid, enable);
            if (enable) {
                subscribedTrackMap.put(uid, event.aliRtcVideoTrack);
            } else {
                subscribedTrackMap.remove(uid);
            }
        }
    }

    private boolean isOwnerAndTeacher(String uid) {
        RoomChannel rc;
        return (rc = Utils.getRef(roomChannelRef)) != null && rc.isOwner(uid);
    }

    @Override
    public void destroy() {
        Utils.clear(rtcServiceRef);
        Utils.clear(subscribeStatusMap, subscribedTrackMap);
    }
}
