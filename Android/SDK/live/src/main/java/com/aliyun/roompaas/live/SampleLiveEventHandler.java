package com.aliyun.roompaas.live;

import android.support.annotation.Nullable;

import com.aliyun.player.bean.ErrorInfo;
import com.aliyun.roompaas.live.exposable.event.LiveCommonEvent;
import com.aliyun.roompaas.live.exposable.LiveEventHandler;

import java.util.Map;

/**
 * @author puke
 * @version 2021/7/2
 */
public class SampleLiveEventHandler implements LiveEventHandler {

    @Override
    public void onLiveCreated(LiveCommonEvent event) {

    }

    @Override
    public void onLiveStarted(LiveCommonEvent event) {

    }

    @Override
    public void onLiveStopped(LiveCommonEvent event) {

    }

    @Override
    public void onLiveStreamStarted(LiveCommonEvent event) {

    }

    @Override
    public void onLiveStreamStopped(LiveCommonEvent event) {

    }

    @Override
    public void onPusherEvent(LiveEvent event, @Nullable Map<String, Object> extras) {

    }

    @Override
    @Deprecated
    public void onPusherEvent(LiveEvent event) {

    }

    @Override
    public void onRenderStart() {

    }

    @Override
    public void onLoadingBegin() {

    }

    @Override
    public void onLoadingProgress(int progress) {

    }

    @Override
    public void onLoadingEnd() {

    }

    @Override
    public void onPlayerError(ErrorInfo errorInfo) {

    }

    @Override
    public void onPlayerError() {

    }

    @Override
    public void onPrepared() {

    }

    @Override
    public void onPlayerEnd() {

    }

    @Override
    public void onPlayerCurrentPosition(long position) {

    }

    @Override
    public void onPlayerBufferedPosition(long position) {

    }

    @Override
    public void onPlayerStatusChange(int status) {

    }

    @Override
    public void onPlayerVideoSizeChanged(int width, int height) {
        
    }

    @Override
    public void onPlayerDownloadSpeedChanged(long kb) {

    }
}
