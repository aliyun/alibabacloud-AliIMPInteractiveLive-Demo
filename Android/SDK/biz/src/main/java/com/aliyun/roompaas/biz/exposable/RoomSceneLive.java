package com.aliyun.roompaas.biz.exposable;

import com.alibaba.dingpaas.scenelive.SceneCreateLiveReq;
import com.alibaba.dingpaas.scenelive.SceneCreateLiveRsp;
import com.alibaba.dingpaas.scenelive.SceneGetLiveDetailRsp;
import com.alibaba.dingpaas.scenelive.SceneLiveInfoModel;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.base.model.PageModel;
import com.aliyun.roompaas.biz.exposable.model.LiveParam;
import com.aliyun.roompaas.biz.exposable.model.LiveRoomInfo;

/**
 * @author puke
 * @version 2021/9/16
 */
public interface RoomSceneLive {

    void createLive(SceneCreateLiveReq req, Callback<SceneCreateLiveRsp> callback);

    void updateLive(LiveRoomInfo info, Callback<Void> callback);

    void stopLive(String liveId, Callback<Void> callback);
    
    void getLiveList(LiveParam param, Callback<PageModel<SceneLiveInfoModel>> callback);
    
    void getLiveDetail(String liveId, Callback<SceneGetLiveDetailRsp> callback);
}
