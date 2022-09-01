package com.aliyun.roompaas.live.exposable;

import com.alibaba.dingpaas.live.LiveDetail;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.base.exposable.PluginService;
import com.aliyun.roompaas.live.AliLivePusherOptions;
import com.aliyun.roompaas.live.exposable.model.LiveInfoModel;

/**
 * @author puke
 * @version 2021/6/21
 */
public interface LiveService extends PluginService<LiveEventHandler> {

    /**
     * @return 判断是否有直播
     */
    boolean hasLive();

    /**
     * @return 查询直播详情 (同步方法, 仅限于内部请求过后才有值)
     */
    LiveDetail getLiveDetail();

    /**
     * 查询直播详情 (异步网络请求)
     *
     * @param callback 回调函数
     */
    void getLiveDetail(Callback<LiveDetail> callback);

    /**
     * 更新直播间信息
     *
     * @param model 直播间信息
     */
    void updateLiveInfo(LiveInfoModel model, Callback<Void> callback);

    /**
     * @return 获取直播播放服务
     */
    LivePlayerService getPlayerService();

    /**
     * @return 获取直播推流服务
     */
    LivePusherService getPusherService();

    /**
     * 获取直播推流服务
     * @param options
     * @return
     */
    LivePusherService getPusherService(AliLivePusherOptions options);
}
