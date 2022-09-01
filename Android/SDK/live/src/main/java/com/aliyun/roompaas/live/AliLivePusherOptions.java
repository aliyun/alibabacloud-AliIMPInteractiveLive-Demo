package com.aliyun.roompaas.live;

import com.aliyun.roompaas.live.exposable.AliLiveBeautyOptions;
import com.aliyun.roompaas.live.exposable.AliLiveMediaStreamOptions;

public class AliLivePusherOptions {
    // 美颜参数
    @Deprecated
    public AliLiveBeautyOptions beautyOptions = new AliLiveBeautyOptions.Builder().build();

    // 媒体层信息
    public AliLiveMediaStreamOptions mediaStreamOptions = new AliLiveMediaStreamOptions();

}



