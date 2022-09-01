package com.aliyun.standard.liveroom.lib.wrapper;

import android.view.SurfaceView;
import android.view.View;

import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.live.exposable.LivePlayerService;
import com.aliyun.roompaas.live.exposable.LiveService;
import com.aliyun.roompaas.player.AliLivePlayerConfig;
import com.aliyun.roompaas.player.LivePlayerManager;

/**
 * @author puke
 * @version 2021/9/26
 */
class LivePlayerServiceWrapper implements LivePlayerServiceExtends {

    private final LiveService liveService;
    private final LivePlayerService playerService;

    LivePlayerServiceWrapper(LiveService liveService, LivePlayerService playerService) {
        this.liveService = liveService;
        this.playerService = playerService;
    }

    @Override
    public void refreshPlay() {
        playerService.refreshPlay();
    }

    @Override
    public void resumePlay() {
        playerService.resumePlay();
    }

    @Override
    public void pausePlay() {
        playerService.pausePlay();
    }

    @Override
    public void stopPlay() {
        playerService.stopPlay();
    }

    @Override
    public void setMutePlay(boolean mute) {
        playerService.setMutePlay(mute);
    }

    @Override
    public void tryPlay(Callback<View> callback) {
        playerService.tryPlay(callback);
    }

    @Override
    public SurfaceView playUrl(String url) {
        return playerService.playUrl(url);
    }

    @Override
    public String getLastTriggerPlayUrl() {
        return playerService.getLastTriggerPlayUrl();
    }

    @Override
    public void setViewContentMode(int mode) {
        playerService.setViewContentMode(mode);
    }

    @Override
    public void updatePositionTimerInternalMs(long internal) {
        playerService.updatePositionTimerInternalMs(internal);
    }

    @Override
    public void setUtcTimeListener(LivePlayerManager.UtcTimeListener utcTimeListener) {
        playerService.setUtcTimeListener(utcTimeListener);
    }

    @Override
    public void seekTo(long position) {
        playerService.seekTo(position);
    }

    @Override
    public long getDuration() {
        return playerService.getDuration();
    }

    @Override
    public void setPlayerConfig(AliLivePlayerConfig playerConfig) {
        playerService.setPlayerConfig(playerConfig);
    }
}
