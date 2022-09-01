package com.aliyun.standard.liveroom.lib.wrapper;

import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.live.AliLivePusherOptions;
import com.aliyun.roompaas.live.exposable.LivePusherService;
import com.aliyun.roompaas.live.exposable.LiveService;
import com.aliyun.roompaas.live.exposable.model.LiveInfoModel;
import com.aliyun.standard.liveroom.lib.LivePrototype;

/**
 * @author puke
 * @version 2021/12/14
 */
public interface LiveServiceExtends extends LiveService {

    /**
     * 重新声明该方法为了打过期标, 建议低代码用户使用 @{@link RoomChannelExtends#updateLiveRoom} 接口
     *
     * @param model    直播间信息
     * @param callback 回调函数
     */
    @Deprecated
    @Override
    void updateLiveInfo(LiveInfoModel model, Callback<Void> callback);

    /**
     * 不建议直接使用该方法 (需要透传参数时, 通过{@link LivePrototype.OpenLiveParam#mediaPusherOptions}传递)
     *
     * @param options 推流参数
     * @return 推流服务
     */
    @Deprecated
    @Override
    LivePusherService getPusherService(AliLivePusherOptions options);

    @Override
    LivePusherServiceExtends getPusherService();

    @Override
    LivePlayerServiceExtends getPlayerService();

    /**
     * 获取连麦直播的推流端服务<hr>
     * 在普通直播的基础上, 新增了连麦的能力
     *
     * @return 连麦直播的推流端服务
     */
    LinkMicPusherService getLinkMicPusherService();
}
