package com.aliyun.standard.liveroom.lib.wrapper;

import android.view.View;

import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.live.exposable.BusinessOptions;
import com.aliyun.roompaas.live.exposable.LivePusherService;

/**
 * 基于底层场景化SDK的封装, 添加样板间SDK中的
 *
 * @author puke
 * @version 2021/12/15
 */
public interface LivePusherServiceExtends extends LivePusherService {

    @Deprecated
    @Override
    void startLive(BusinessOptions businessOptions, Callback<View> callback);
}
