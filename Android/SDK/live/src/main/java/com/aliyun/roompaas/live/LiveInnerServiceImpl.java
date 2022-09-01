package com.aliyun.roompaas.live;

import android.support.annotation.NonNull;

import com.aliyun.roompaas.base.RoomContext;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.base.inner.module.LiveInnerService;

/**
 * @author puke
 * @version 2021/6/24
 */
public class LiveInnerServiceImpl implements LiveInnerService {

    private final LiveHelper liveHelper;

    public LiveInnerServiceImpl(RoomContext roomContext) {
        liveHelper = new LiveHelper(roomContext);
    }

    @Override
    public void getPushStreamUrl(@NonNull Callback<String> callback) {
        liveHelper.getPushStreamUrl(callback);
    }

    @Override
    public void reportLiveStatus() {
        liveHelper.reportLiveStatus();
    }
}
