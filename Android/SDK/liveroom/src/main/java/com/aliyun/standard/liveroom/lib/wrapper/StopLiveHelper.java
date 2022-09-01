package com.aliyun.standard.liveroom.lib.wrapper;

import android.text.TextUtils;

import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.base.log.Logger;
import com.aliyun.roompaas.biz.RoomEngine;
import com.aliyun.roompaas.biz.exposable.RoomSceneLive;
import com.aliyun.roompaas.biz.exposable.model.Result;
import com.aliyun.roompaas.live.exposable.LivePusherService;
import com.aliyun.roompaas.live.exposable.LiveService;

/**
 * @author puke
 * @version 2022/4/28
 */
class StopLiveHelper {

    private static final String TAG = StopLiveHelper.class.getSimpleName();

    static void stop(LiveService liveService, LivePusherService pusherService, Callback<Void> callback) {
        final String liveId = liveService.getInstanceId();

        // 为防止服务端并发锁, 这里不会直接调用原子能力的 destroyLive 接口
        pusherService.stopLive(false, callback);

        // 包装对外透出的stopLive方法, 额外添加场景化接口的调用
        if (!TextUtils.isEmpty(liveId)) {
            Result<RoomSceneLive> result = RoomEngine.getInstance().getRoomSceneLive();
            if (!result.success) {
                return;
            }

            result.value.stopLive(liveId, new Callback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    Logger.i(TAG, "RoomSceneLive stopLive success, liveId: " + liveId);
                }

                @Override
                public void onError(String errorMsg) {
                    Logger.e(TAG,
                            "RoomSceneLive stopLive failed, liveId: "
                                    + liveId + ", errorMsg: " + errorMsg);
                }
            });
        }
    }
}
