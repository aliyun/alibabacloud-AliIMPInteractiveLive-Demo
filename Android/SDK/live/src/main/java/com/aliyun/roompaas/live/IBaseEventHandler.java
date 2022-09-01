package com.aliyun.roompaas.live;

public abstract class IBaseEventHandler {

    public void onError() {

    }

    // region pushInfo
    // 首帧渲染通知
    public void onFirstFramePreviewed() {

    }

    // 发送第一个音视频包成功
    public void onFirstAVFramePushed() {

    }

    // 预览开始
    public void onPreviewStarted() {

    }

    //  预览结束
    public void onPreviewStopped() {

    }

    // 推流开始
    public void onPushStarted() {

    }

    // 推流结束
    public void onPushStopped() {

    }
    //endregion

    // region Network
    public void onConnectionFail() {

    }

    public void onConnectionLost() {

    }

    public void onNetworkPoor() {

    }
    // endregion


}
