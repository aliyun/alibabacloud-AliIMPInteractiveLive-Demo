package com.aliyun.roompaas.app.viewmodel;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.alibaba.dingpaas.base.DPSError;
import com.alibaba.dingpaas.room.CreateWhiteboardCb;
import com.alibaba.dingpaas.room.CreateWhiteboardRsp;
import com.alibaba.dingpaas.wb.OpenWhiteboardRsp;
import com.alibaba.dingpaas.wb.StartWhiteboardRecordingRsp;
import com.alibaba.fastjson.JSON;
import com.aliyun.roompaas.app.model.DocumentAccessInfo;
import com.aliyun.roompaas.app.viewmodel.inter.IWhiteBoardOperate;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.base.log.Logger;
import com.aliyun.roompaas.biz.exposable.RoomChannel;
import com.aliyun.roompaas.whiteboard.exposable.ToolbarOrientation;
import com.aliyun.roompaas.whiteboard.exposable.WhiteboardService;
import com.aliyun.roompaas.whiteboard.exposable.event.WhiteBoardOption;

/**
 * Created by KyleCe on 2021/5/27
 */
public class WhiteBoardVM implements IWhiteBoardOperate {
    public static final String TAG = "WhiteBoardVM";

    private final RoomChannel roomChannel;
    private final WhiteboardService whiteboardService;

    private String whiteBoardInstanceId;

    private DocumentAccessInfo accessInfo;
    private final boolean CONFIG_DS_DOMAIN = false; // 不配置版本

    public WhiteBoardVM(@NonNull RoomChannel roomChannel) {
        this.roomChannel = roomChannel;
        this.whiteboardService = roomChannel.getPluginService(WhiteboardService.class);
    }

    @Override
    public void whiteBoardProcess() {
        String whiteboardId = whiteboardService.getWhiteboardId();
        if (whiteboardId != null) {
            whiteBoardInstanceId = whiteboardId;
            openWhiteBoardProcess(whiteBoardInstanceId);
        } else {
            createWhiteBoard();
        }
    }

    private void createWhiteBoard() {
        whiteboardService.crateWhiteBoard(new CreateWhiteboardCb() {
            @Override
            public void onSuccess(CreateWhiteboardRsp createWhiteboardRsp) {
                Log.i(TAG, "onSuccess: ");
                String id;
                if (createWhiteboardRsp != null && !TextUtils.isEmpty((id = createWhiteboardRsp.getWhiteboardId()))) {
                    whiteBoardInstanceId = id;
                    openWhiteBoardProcess(whiteBoardInstanceId);
                } else {
                    Log.i(TAG, "onSuccess error: invalid id");
                }
            }

            @Override
            public void onFailure(DPSError dpsError) {
                Log.i(TAG, "onFailure: ");
            }
        });
    }

    private void openWhiteBoardProcess(String instanceId) {
        if (TextUtils.isEmpty(instanceId)) {
            return;
        }

        whiteboardService.openWhiteboardService(instanceId, new Callback<OpenWhiteboardRsp>() {
            @Override
            public void onSuccess(OpenWhiteboardRsp data) {
                Logger.i(TAG, "openWhiteboardService onSuccess: " + data);
                if (data != null && (accessInfo = convert(data)) != null) {
                    accessInfo.docKey = whiteBoardInstanceId;
                    boolean isTeacherAndOwner = roomChannel.isOwner();
                    accessInfo.permission = isTeacherAndOwner ? DocumentAccessInfo.PERMISSION_READ_AND_WRITE : DocumentAccessInfo.PERMISSION_READ_ONLY;
                    WhiteBoardOption option = new WhiteBoardOption();
                    option.forceSync = true;
                    whiteboardService.initWhiteBoard(JSON.toJSONString(accessInfo), option);
                    //Utils.run(whiteBoardInitializedAction);
                }
            }

            @Override
            public void onError(String errorMsg) {
                Logger.e(TAG, "openWhiteboardService onError: " + errorMsg);
            }
        });
    }

    @Nullable
    private DocumentAccessInfo convert(OpenWhiteboardRsp rsp) {
        if (rsp == null) {
            return null;
        }

        DocumentAccessInfo info = new DocumentAccessInfo();
        info.accessToken = rsp.documentAccessInfo.accessToken;
        info.collabHost = rsp.documentAccessInfo.collabHost;
        info.permission = rsp.documentAccessInfo.permission;
        if (CONFIG_DS_DOMAIN) {
            info.wsDomain = rsp.documentAccessInfo.wsDomain;
        }
        return info;
    }

    @Override
    public String getRoomId() {
        return roomChannel.getRoomId();
    }

    @Override
    public void openWhiteBoard(Callback<View> callback) {
        whiteboardService.openWhiteBoard(callback);
    }

    @Override
    public void setToolbarOrientation(ToolbarOrientation orientation) {
        whiteboardService.setToolbarOrientation(orientation);
    }

    @Override
    public void setToolbarVisibility(int visibility) {
        whiteboardService.setToolbarVisibility(visibility);
    }

    @Override
    public void getScale(Callback<Float> callback) {
        whiteboardService.getScale(callback);
    }

    @Override
    public void setScale(float scale, @Nullable Runnable resultAction) {
        whiteboardService.setScale(scale, resultAction);
    }

    @Override
    public void startWhiteboardRecording() {
        whiteboardService.startWhiteboardRecording(new Callback<StartWhiteboardRecordingRsp>() {
            @Override
            public void onSuccess(StartWhiteboardRecordingRsp data) {
                Logger.i(TAG, "onSuccess: ");
            }

            @Override
            public void onError(String errorMsg) {
                Logger.e(TAG, "onError: " + errorMsg);
            }
        });
    }
}
