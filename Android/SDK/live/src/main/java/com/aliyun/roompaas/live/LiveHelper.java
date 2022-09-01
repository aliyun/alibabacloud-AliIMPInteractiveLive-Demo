package com.aliyun.roompaas.live;

import android.support.annotation.NonNull;

import com.alibaba.dingpaas.base.DPSError;
import com.alibaba.dingpaas.live.ContinuePlaybackTimingCb;
import com.alibaba.dingpaas.live.ContinuePlaybackTimingReq;
import com.alibaba.dingpaas.live.ContinuePlaybackTimingRsp;
import com.alibaba.dingpaas.live.EndLiveTimingCb;
import com.alibaba.dingpaas.live.EndLiveTimingReq;
import com.alibaba.dingpaas.live.EndLiveTimingRsp;
import com.alibaba.dingpaas.live.EndPlaybackTimingCb;
import com.alibaba.dingpaas.live.EndPlaybackTimingReq;
import com.alibaba.dingpaas.live.EndPlaybackTimingRsp;
import com.alibaba.dingpaas.live.GetLiveDetailCb;
import com.alibaba.dingpaas.live.GetLiveDetailReq;
import com.alibaba.dingpaas.live.GetLiveDetailRsp;
import com.alibaba.dingpaas.live.LiveModule;
import com.alibaba.dingpaas.live.LiveRpcInterface;
import com.alibaba.dingpaas.live.PublishLiveCb;
import com.alibaba.dingpaas.live.PublishLiveReq;
import com.alibaba.dingpaas.live.PublishLiveRsp;
import com.alibaba.dingpaas.live.StartLiveTimingCb;
import com.alibaba.dingpaas.live.StartLiveTimingReq;
import com.alibaba.dingpaas.live.StartLiveTimingRsp;
import com.alibaba.dingpaas.live.StartPlaybackTimingCb;
import com.alibaba.dingpaas.live.StartPlaybackTimingReq;
import com.alibaba.dingpaas.live.StartPlaybackTimingRsp;
import com.alibaba.dingpaas.monitorhub.MonitorhubField;
import com.alibaba.dingpaas.room.CreateLiveCb;
import com.alibaba.dingpaas.room.CreateLiveReq;
import com.alibaba.dingpaas.room.CreateLiveRsp;
import com.alibaba.dingpaas.room.RoomDetail;
import com.alibaba.dingpaas.room.RoomExtInterface;
import com.alibaba.dingpaas.room.RoomInfo;
import com.alibaba.dingpaas.room.RoomModule;
import com.alibaba.dingpaas.room.RoomRpcInterface;
import com.alibaba.fastjson.JSON;
import com.aliyun.roompaas.base.PluginManager;
import com.aliyun.roompaas.base.RoomContext;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.base.log.Logger;
import com.aliyun.roompaas.base.monitor.MonitorHubChannel;
import com.aliyun.roompaas.base.util.CollectionUtil;
import com.aliyun.roompaas.base.util.Utils;
import com.aliyun.roompaas.live.exposable.BusinessOptions;

import java.util.List;

/**
 * 直播辅助类<hr/>
 * {@link LiveServiceImpl} 和 {@link LiveInnerServiceImpl} 两个服务都需要要调用的代码部分, 单独抽取出来
 *
 * @author puke
 * @version 2021/6/24
 */
class LiveHelper {

    private static final String TAG = LiveHelper.class.getSimpleName();

    private final RoomContext roomContext;
    private final String userId;

    private final RoomRpcInterface roomRpcInterface;
    private final RoomExtInterface roomExtInterface;
    private final LiveRpcInterface liveRpcInterface;
    private final PluginManager pluginManager;

    public LiveHelper(RoomContext roomContext) {
        this.roomContext = roomContext;
        userId = roomContext.getUserId();
        String roomId = roomContext.getRoomId();
        pluginManager = roomContext.getPluginManager();

        roomRpcInterface = RoomModule.getModule(userId).getRpcInterface();
        roomExtInterface = RoomModule.getModule(userId).getExtInterface();
        liveRpcInterface = LiveModule.getModule(userId).getRpcInterface();
    }

    public void getPushStreamUrl(@NonNull final Callback<String> callback) {
        getPushStreamUrl(null, callback);
    }

    public void getPushStreamUrl(BusinessOptions options, @NonNull final Callback<String> callback) {
        // 查询房间直播
        List<String> liveIds = pluginManager.getInstanceIds(LiveServiceImpl.PLUGIN_ID);
        // 取出第一个
        final String liveId = CollectionUtil.getFirst(liveIds);
        if (liveId != null) {
            // 有直播
            getPushStreamUrlById(liveId, callback);
        } else {
            // 无直播, 先创建
            CreateLiveReq req = new CreateLiveReq();
            req.anchorId = userId;
            req.roomId = roomContext.getRoomId();
            RoomDetail roomDetail = roomContext.getRoomDetail();
            if (roomDetail != null) {
                RoomInfo roomInfo = roomDetail.roomInfo;
                if (roomInfo != null) {
                    req.title = roomInfo.title;
                }
            }
            if (options != null) {
                req.preStartDate = options.liveStartTime;
                req.preEndDate = options.liveEndTime;
                req.coverUrl = options.liveCoverUrl;
                req.userDefineField = options.extension;
            }
            roomExtInterface.createLive(req, new CreateLiveCb() {
                @Override
                public void onSuccess(CreateLiveRsp rsp) {
                    // 创建成功后, 存下liveId, 然后start
                    pluginManager.addInstanceId(LiveServiceImpl.PLUGIN_ID, rsp.liveId);
                    getPushStreamUrlById(rsp.liveId, callback);
                }

                @Override
                public void onFailure(DPSError dpsError) {
                    Utils.callErrorWithDps(callback, dpsError);
                }
            });
        }
    }

    private void getPushStreamUrlById(String liveId, @NonNull final Callback<String> callback) {
        GetLiveDetailReq req = new GetLiveDetailReq();
        req.uuid = liveId;
        liveRpcInterface.getLiveDetail(req, new GetLiveDetailCb() {
            @Override
            public void onSuccess(GetLiveDetailRsp liveDetail) {
                Logger.i(TAG, "get live detail, " + JSON.toJSONString(liveDetail.live));
                callback.onSuccess(liveDetail.live.pushUrl);
            }

            @Override
            public void onFailure(DPSError dpsError) {
                Utils.callErrorWithDps(callback, dpsError);
            }
        });
        MonitorHubChannel.setBizId(liveId);
    }

    public void reportLiveStatus() {
        String pushedLiveId = CollectionUtil.getFirst(
                pluginManager.getInstanceIds(LiveServiceImpl.PLUGIN_ID));
        if (pushedLiveId != null) {
            PublishLiveReq req = new PublishLiveReq();
            req.uuid = pushedLiveId;
            liveRpcInterface.publishLive(req, new PublishLiveCb() {
                @Override
                public void onSuccess(PublishLiveRsp publishLiveRsp) {
                    Logger.i(TAG, "publishLive success");
                    MonitorHubChannel.publishLive(MonitorhubField.MFFIELD_COMMON_RTMP,
                            MonitorHubChannel.REPORT_EVENT_SUCCESS_CODE, null);
                }

                @Override
                public void onFailure(DPSError dpsError) {
                    Logger.e(TAG, "publishLive error: " + dpsError.reason);
                    MonitorHubChannel.publishLive(MonitorhubField.MFFIELD_COMMON_RTMP,
                            dpsError.code, dpsError.reason);
                }
            });
        }
    }

    /**
     * 观众-开始观看直播数据上报
     */
    public void startLiveTiming() {
        String pushedLiveId = CollectionUtil.getFirst(
                pluginManager.getInstanceIds(LiveServiceImpl.PLUGIN_ID));
        if (pushedLiveId != null) {
            StartLiveTimingReq req = new StartLiveTimingReq();
            req.uuid = pushedLiveId;
            liveRpcInterface.startLiveTiming(req, new StartLiveTimingCb() {
                @Override
                public void onSuccess(StartLiveTimingRsp startLiveTimingRsp) {
                    Logger.i(TAG, "startLiveTiming success");
                }

                @Override
                public void onFailure(DPSError dpsError) {
                    Logger.e(TAG, String.format("startLiveTiming error. code: %d, reason: %s", dpsError.code, dpsError.reason));
                }
            });
        }
    }

    /**
     * 观众-结束直播观看数据上报
     */
    public void endLiveTiming() {
        String pushedLiveId = CollectionUtil.getFirst(
                pluginManager.getInstanceIds(LiveServiceImpl.PLUGIN_ID));
        if (pushedLiveId != null) {
            EndLiveTimingReq req = new EndLiveTimingReq();
            req.uuid = pushedLiveId;
            liveRpcInterface.endLiveTiming(req, new EndLiveTimingCb() {
                @Override
                public void onSuccess(EndLiveTimingRsp endLiveTimingRsp) {
                    Logger.i(TAG, "endLiveTiming success");
                }

                @Override
                public void onFailure(DPSError dpsError) {
                    Logger.e(TAG, String.format("endLiveTiming error. code: %d, reason: %s", dpsError.code, dpsError.reason));
                }
            });
        }
    }

    /**
     * 观众-开始观看回放数据上报
     */
    public void startPlaybackTiming(String transId) {
        String pushedLiveId = CollectionUtil.getFirst(
                pluginManager.getInstanceIds(LiveServiceImpl.PLUGIN_ID));
        if (pushedLiveId != null) {
            StartPlaybackTimingReq req = new StartPlaybackTimingReq();
            req.uuid = pushedLiveId;
            req.transId = transId;
            liveRpcInterface.startPlaybackTiming(req, new StartPlaybackTimingCb() {
                @Override
                public void onSuccess(StartPlaybackTimingRsp startPlaybackTimingRsp) {
                    Logger.i(TAG, "startPlaybackTiming success");
                }

                @Override
                public void onFailure(DPSError dpsError) {
                    Logger.e(TAG, String.format("startPlaybackTiming error. code: %d, reason: %s", dpsError.code, dpsError.reason));
                }
            });
        }
    }

    /**
     * 观众-观看过程中间隔数据上报
     */
    public void continuePlaybackTiming() {
        String pushedLiveId = CollectionUtil.getFirst(
                pluginManager.getInstanceIds(LiveServiceImpl.PLUGIN_ID));
        if (pushedLiveId != null) {
            ContinuePlaybackTimingReq req = new ContinuePlaybackTimingReq();
            req.uuid = pushedLiveId;
            liveRpcInterface.continuePlaybackTiming(req, new ContinuePlaybackTimingCb() {
                @Override
                public void onSuccess(ContinuePlaybackTimingRsp continuePlaybackTimingRsp) {
                    Logger.i(TAG, "continuePlaybackTiming success");
                }

                @Override
                public void onFailure(DPSError dpsError) {
                    Logger.e(TAG, "continuePlaybackTiming error: " + dpsError.reason);
                }
            });
        }
    }

    /**
     * 观众-结束回放观看时数据上报
     */
    public void endPlaybackTiming() {
        String pushedLiveId = CollectionUtil.getFirst(
                pluginManager.getInstanceIds(LiveServiceImpl.PLUGIN_ID));
        if (pushedLiveId != null) {
            EndPlaybackTimingReq req = new EndPlaybackTimingReq();
            req.uuid = pushedLiveId;
            liveRpcInterface.endPlaybackTiming(req, new EndPlaybackTimingCb() {
                @Override
                public void onSuccess(EndPlaybackTimingRsp endPlaybackTimingRsp) {
                    Logger.i(TAG, "endPlaybackTiming success");
                }

                @Override
                public void onFailure(DPSError dpsError) {
                    Logger.e(TAG, "endPlaybackTiming error: " + dpsError.reason);
                }
            });
        }
    }
}
