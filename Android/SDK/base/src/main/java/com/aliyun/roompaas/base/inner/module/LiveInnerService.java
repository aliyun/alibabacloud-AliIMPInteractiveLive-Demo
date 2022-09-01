package com.aliyun.roompaas.base.inner.module;

import android.support.annotation.NonNull;

import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.base.inner.InnerService;

/**
 * @author puke
 * @version 2021/6/24
 */
public interface LiveInnerService extends InnerService {

    /**
     * 获取推流地址
     *
     * @param callback 回调函数
     */
    void getPushStreamUrl(@NonNull final Callback<String> callback);

    /**
     * 上报直播状态
     */
    void reportLiveStatus();
}
