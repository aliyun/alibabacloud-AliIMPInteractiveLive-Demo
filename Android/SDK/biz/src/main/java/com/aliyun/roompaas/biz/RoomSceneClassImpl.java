package com.aliyun.roompaas.biz;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.alibaba.dingpaas.base.DPSError;
import com.alibaba.dingpaas.sceneclass.CreateClassCb;
import com.alibaba.dingpaas.sceneclass.CreateClassReq;
import com.alibaba.dingpaas.sceneclass.CreateClassRsp;
import com.alibaba.dingpaas.sceneclass.GetClassDetailCb;
import com.alibaba.dingpaas.sceneclass.GetClassDetailReq;
import com.alibaba.dingpaas.sceneclass.GetClassDetailRsp;
import com.alibaba.dingpaas.sceneclass.SceneclassRpcInterface;
import com.alibaba.dingpaas.sceneclass.SceneclassModule;
import com.alibaba.dingpaas.sceneclass.StartClassCb;
import com.alibaba.dingpaas.sceneclass.StartClassReq;
import com.alibaba.dingpaas.sceneclass.StartClassRsp;
import com.alibaba.dingpaas.sceneclass.StopClassCb;
import com.alibaba.dingpaas.sceneclass.StopClassReq;
import com.alibaba.dingpaas.sceneclass.StopClassRsp;
import com.aliyun.roompaas.base.ModuleRegister;
import com.aliyun.roompaas.base.annotation.PluginServiceInject;
import com.aliyun.roompaas.base.callback.UICallback;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.base.monitor.MonitorHubChannel;
import com.aliyun.roompaas.base.util.Utils;
import com.aliyun.roompaas.biz.exposable.RoomSceneClass;

/**
 * Created by KyleCe on 2021/10/28
 */
@PluginServiceInject
class RoomSceneClassImpl implements RoomSceneClass {

    private final String userId;
    private final SceneclassRpcInterface sceneclassRpcInterface;

    static {
        ModuleRegister.registerLwpModule(SceneclassModule.getModuleInfo());
    }

    RoomSceneClassImpl(String userId) {
        this.userId = userId;
        this.sceneclassRpcInterface = SceneclassModule.getModule(userId).getRpcInterface();
    }

    @Override
    public void createClass(String title, String createNickname, @Nullable Callback<CreateClassRsp> ck) {
        CreateClassReq req = new CreateClassReq(title, createNickname);
        final UICallback<CreateClassRsp> uiCallback = new UICallback<>(ck);
        sceneclassRpcInterface.createClass(req, new CreateClassCb() {
            @Override
            public void onSuccess(CreateClassRsp createClassRsp) {
                Utils.callSuccess(uiCallback, createClassRsp);
            }

            @Override
            public void onFailure(DPSError dpsError) {
                Utils.callErrorWithDps(uiCallback, dpsError);
            }
        });
    }

    @Override
    public void getClassDetail(@NonNull String classId, @Nullable Callback<GetClassDetailRsp> ck) {
        final UICallback<GetClassDetailRsp> uiCallback = new UICallback<>(ck);
        if (TextUtils.isEmpty(classId)) {
            Utils.invokeInvalidParamError(uiCallback);
            return;
        }

        GetClassDetailReq req = new GetClassDetailReq(classId);
        sceneclassRpcInterface.getClassDetail(req, new GetClassDetailCb() {
            @Override
            public void onSuccess(GetClassDetailRsp getClassDetailRsp) {
                Utils.callSuccess(uiCallback, getClassDetailRsp);
            }

            @Override
            public void onFailure(DPSError dpsError) {
                Utils.callErrorWithDps(uiCallback, dpsError);
            }
        });
    }

    @Override
    public void startClass(@NonNull String classId, @Nullable Callback<StartClassRsp> ck) {
        final UICallback<StartClassRsp> uiCallback = new UICallback<>(ck);
        if (TextUtils.isEmpty(classId)) {
            Utils.invokeInvalidParamError(uiCallback);
            return;
        }

        StartClassReq req = new StartClassReq(classId);
        sceneclassRpcInterface.startClass(req, new StartClassCb() {
            @Override
            public void onSuccess(StartClassRsp startClassRsp) {
                MonitorHubChannel.reportStartClass(MonitorHubChannel.REPORT_EVENT_SUCCESS_CODE, null);
                Utils.callSuccess(uiCallback, startClassRsp);
            }

            @Override
            public void onFailure(DPSError dpsError) {
                MonitorHubChannel.reportStartClass(dpsError.getCode(), dpsError.getReason());
                Utils.callErrorWithDps(uiCallback, dpsError);
            }
        });
    }

    @Override
    public void stopClass(@NonNull String classId, @Nullable Callback<StopClassRsp> ck) {
        final UICallback<StopClassRsp> uiCallback = new UICallback<>(ck);
        if (TextUtils.isEmpty(classId)) {
            Utils.invokeInvalidParamError(uiCallback);
            return;
        }

        StopClassReq req = new StopClassReq(classId);
        sceneclassRpcInterface.stopClass(req, new StopClassCb() {
            @Override
            public void onSuccess(StopClassRsp stopClassRsp) {
                MonitorHubChannel.reportSopClass(MonitorHubChannel.REPORT_EVENT_SUCCESS_CODE, null);
                Utils.callSuccess(uiCallback, stopClassRsp);
            }

            @Override
            public void onFailure(DPSError dpsError) {
                MonitorHubChannel.reportSopClass(dpsError.getCode(), dpsError.getReason());
                Utils.callErrorWithDps(uiCallback, dpsError);
            }
        });
    }
}
