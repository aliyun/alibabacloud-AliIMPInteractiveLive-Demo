package com.aliyun.roompaas.app.viewmodel;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.alibaba.dingpaas.base.DPSError;
import com.alibaba.dingpaas.room.CreateWhiteboardCb;
import com.alibaba.dingpaas.room.CreateWhiteboardRsp;
import com.alibaba.fastjson.JSON;
import com.aliyun.roompaas.app.api.OpenWhiteBoardAPI;
import com.aliyun.roompaas.app.model.DocumentAccessInfo;
import com.aliyun.roompaas.app.response.OpenWhiteBoardResponse;
import com.aliyun.roompaas.app.response.Response;
import com.aliyun.roompaas.app.viewmodel.inter.IWhiteBoardOperate;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.biz.exposable.RoomChannel;
import com.aliyun.roompaas.whiteboard.exposable.WhiteboardService;

/**
 * Created by KyleCe on 2021/5/27
 */
public class WhiteBoardVM implements IWhiteBoardOperate {
    public static final String TAG = "WhiteBoardVM";

    private final RoomChannel roomChannel;
    private final WhiteboardService whiteboardService;

    private String whiteBoardInstanceId;

    private static final int PERMISSION_NONE = 0;
    private static final int PERMISSION_READ_ONLY = 1;
    private static final int PERMISSION_READ_AND_WRITE = 2;

    public WhiteBoardVM(@NonNull RoomChannel roomChannel) {
        this.roomChannel = roomChannel;
        this.whiteboardService = roomChannel.getPluginService(WhiteboardService.class);
    }

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

        OpenWhiteBoardAPI.openWhiteBoard(instanceId, roomChannel.getUserId(), new Callback<Response<OpenWhiteBoardResponse>>() {
            @Override
            public void onSuccess(Response<OpenWhiteBoardResponse> data) {
                Log.i(TAG, "onSuccess: ");
                OpenWhiteBoardResponse response;
                DocumentAccessInfo accessInfo;
                if (data != null && (response = data.result) != null && (accessInfo = response.documentAccessInfo) != null) {
                    accessInfo.docKey = whiteBoardInstanceId;
                    accessInfo.permission = roomChannel.isOwner() ? PERMISSION_READ_AND_WRITE : PERMISSION_READ_ONLY;
                    whiteboardService.initWhiteBoard(JSON.toJSONString(accessInfo));
                }
            }

            @Override
            public void onError(String errorMsg) {
                Log.i(TAG, "onError: ");
            }
        });
    }

    @Override
    public String getRoomId() {
        return roomChannel.getRoomId();
    }

    @Override
    public void openWhiteBoard(Callback<View> callback) {
        whiteboardService.openWhiteBoard(callback);
    }
}
