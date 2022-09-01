package com.aliyun.roompaas.live.cloudconfig;

import com.alivc.live.pusher.AlivcLivePushConfig;
import com.aliyun.roompaas.base.IReset;
import com.aliyun.roompaas.base.cloudconfig.base.IBaseCloudConfig;

public interface ILiveMediaStrategy extends IReset, IBaseCloudConfig {
    AlivcLivePushConfig updateLivePushConfig(AlivcLivePushConfig config);
}
