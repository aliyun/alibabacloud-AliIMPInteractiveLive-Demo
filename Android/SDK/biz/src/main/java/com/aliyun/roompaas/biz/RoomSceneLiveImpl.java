package com.aliyun.roompaas.biz;

import android.text.TextUtils;

import com.alibaba.dingpaas.base.DPSError;
import com.alibaba.dingpaas.scenelive.CreateLiveCb;
import com.alibaba.dingpaas.scenelive.SceneCreateLiveReq;
import com.alibaba.dingpaas.scenelive.SceneCreateLiveRsp;
import com.alibaba.dingpaas.scenelive.GetLiveDetailCb;
import com.alibaba.dingpaas.scenelive.SceneGetLiveDetailReq;
import com.alibaba.dingpaas.scenelive.SceneGetLiveDetailRsp;
import com.alibaba.dingpaas.scenelive.GetLiveListCb;
import com.alibaba.dingpaas.scenelive.SceneGetLiveListReq;
import com.alibaba.dingpaas.scenelive.SceneGetLiveListRsp;
import com.alibaba.dingpaas.scenelive.SceneLiveInfoModel;
import com.alibaba.dingpaas.scenelive.StopLiveCb;
import com.alibaba.dingpaas.scenelive.SceneStopLiveReq;
import com.alibaba.dingpaas.scenelive.SceneStopLiveRsp;
import com.alibaba.dingpaas.scenelive.UpdateLiveCb;
import com.alibaba.dingpaas.scenelive.SceneUpdateLiveReq;
import com.alibaba.dingpaas.scenelive.SceneUpdateLiveRsp;
import com.alibaba.dingpaas.scenelive.SceneliveRpcInterface;
import com.alibaba.dingpaas.scenelive.SceneliveModule;
import com.aliyun.roompaas.base.annotation.PluginServiceInject;
import com.aliyun.roompaas.base.callback.UICallback;
import com.aliyun.roompaas.base.error.Errors;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.base.model.PageModel;
import com.aliyun.roompaas.base.util.Utils;
import com.aliyun.roompaas.biz.exposable.RoomSceneLive;
import com.aliyun.roompaas.biz.exposable.enums.LiveStatus;
import com.aliyun.roompaas.biz.exposable.model.LiveParam;
import com.aliyun.roompaas.biz.exposable.model.LiveRoomInfo;

/**
 * @author puke
 * @version 2021/9/16
 */
@PluginServiceInject
class RoomSceneLiveImpl implements RoomSceneLive {

    private final String userId;
    private final SceneliveRpcInterface sceneliveRpcInterface;

    RoomSceneLiveImpl(String userId) {
        this.userId = userId;
        this.sceneliveRpcInterface = SceneliveModule.getModule(userId).getRpcInterface();
    }

    @Override
    public void createLive(SceneCreateLiveReq req, Callback<SceneCreateLiveRsp> callback) {
        final UICallback<SceneCreateLiveRsp> uiCallback = new UICallback<>(callback);
        sceneliveRpcInterface.createLive(req, new CreateLiveCb() {
            @Override
            public void onSuccess(SceneCreateLiveRsp rsp) {
                uiCallback.onSuccess(rsp);
            }

            @Override
            public void onFailure(DPSError dpsError) {
                Utils.callErrorWithDps(uiCallback, dpsError);
            }
        });
    }

    @Override
    public void updateLive(LiveRoomInfo info, Callback<Void> callback) {
        final Callback<Void> uiCallback = new UICallback<>(callback);
        if (info == null) {
            uiCallback.onError(Errors.PARAM_ERROR.getMessage());
            return;
        }

        SceneUpdateLiveReq req = new SceneUpdateLiveReq();
        req.liveId = info.liveId;
        req.title = info.title;
        req.notice = info.notice;
        req.coverUrl = info.coverUrl;
        req.extension = info.extension;
        sceneliveRpcInterface.updateLive(req, new UpdateLiveCb() {
            @Override
            public void onSuccess(SceneUpdateLiveRsp rsp) {
                uiCallback.onSuccess(null);
            }

            @Override
            public void onFailure(DPSError dpsError) {
                uiCallback.onError(dpsError.reason);
            }
        });
    }

    @Override
    public void stopLive(String liveId, Callback<Void> callback) {
        final UICallback<Void> uiCallback = new UICallback<>(callback);
        final SceneStopLiveReq req = new SceneStopLiveReq();
        req.userId = userId;
        req.liveId = liveId;
        sceneliveRpcInterface.stopLive(req, new StopLiveCb() {
            @Override
            public void onSuccess(SceneStopLiveRsp rsp) {
                uiCallback.onSuccess(null);
            }

            @Override
            public void onFailure(DPSError dpsError) {
                Utils.callErrorWithDps(uiCallback, dpsError);
            }
        });
    }

    @Override
    public void getLiveList(LiveParam param, Callback<PageModel<SceneLiveInfoModel>> callback) {
        final UICallback<PageModel<SceneLiveInfoModel>> uiCallback = new UICallback<>(callback);
        if (param == null) {
            Utils.invokeInvalidParamError(uiCallback);
            return;
        }

        // 设置默认值
        int pageNum = param.pageNum > 0 ? param.pageNum : 1;
        int pageSize = param.pageSize > 0 ? param.pageSize : 100;
        LiveStatus status = param.status != null ? param.status : LiveStatus.ALL;

        SceneGetLiveListReq req = new SceneGetLiveListReq();
        req.pageSize = pageSize;
        req.pageNumber = pageNum;
        req.status = status.value;
        sceneliveRpcInterface.getLiveList(req, new GetLiveListCb() {
            @Override
            public void onSuccess(SceneGetLiveListRsp rsp) {
                PageModel<SceneLiveInfoModel> pageModel = new PageModel<>();
                pageModel.list = rsp.liveList;
                pageModel.total = rsp.totalCount;
                pageModel.hasMore = rsp.hasMore;
                uiCallback.onSuccess(pageModel);
            }

            @Override
            public void onFailure(DPSError dpsError) {
                Utils.callErrorWithDps(uiCallback, dpsError);
            }
        });
    }

    @Override
    public void getLiveDetail(String liveId, Callback<SceneGetLiveDetailRsp> callback) {
        final UICallback<SceneGetLiveDetailRsp> uiCallback = new UICallback<>(callback);
        if (TextUtils.isEmpty(liveId)) {
            Utils.invokeInvalidParamError(uiCallback);
            return;
        }

        SceneGetLiveDetailReq detailReq = new SceneGetLiveDetailReq();
        detailReq.liveId = liveId;
        sceneliveRpcInterface.getLiveDetail(detailReq, new GetLiveDetailCb() {
            @Override
            public void onSuccess(SceneGetLiveDetailRsp rsp) {
                uiCallback.onSuccess(rsp);
            }

            @Override
            public void onFailure(DPSError dpsError) {
                Utils.callErrorWithDps(uiCallback, dpsError);
            }
        });
    }
}
