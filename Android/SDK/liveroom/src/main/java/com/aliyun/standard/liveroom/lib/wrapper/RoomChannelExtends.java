package com.aliyun.standard.liveroom.lib.wrapper;

import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.biz.exposable.RoomChannel;
import com.aliyun.standard.liveroom.lib.linkmic.LinkMicService;
import com.aliyun.standard.liveroom.lib.model.LiveRoomModel;

/**
 * @author puke
 * @version 2021/12/14
 */
public interface RoomChannelExtends extends RoomChannel {

    /**
     * 更新直播间信息
     *
     * @param model    更新数据实体
     * @param callback 回调函数
     */
    void updateLiveRoom(LiveRoomModel model, Callback<Void> callback);

    /**
     * @return 连麦服务
     */
    LinkMicService getLinkMicService();


}
