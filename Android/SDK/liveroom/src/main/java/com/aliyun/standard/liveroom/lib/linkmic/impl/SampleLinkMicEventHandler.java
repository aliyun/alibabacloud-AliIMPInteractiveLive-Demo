package com.aliyun.standard.liveroom.lib.linkmic.impl;

import android.view.View;

import com.aliyun.standard.liveroom.lib.linkmic.LinkMicEventHandler;
import com.aliyun.standard.liveroom.lib.linkmic.model.LinkMicUserModel;

import java.util.List;

/**
 * @author puke
 * @version 2022/1/10
 */
public class SampleLinkMicEventHandler implements LinkMicEventHandler {

    @Override
    public void onJoinedSuccess(View view) {
        
    }

    @Override
    public void onLeftSuccess() {

    }

    @Override
    public void onUserJoined(List<LinkMicUserModel> users) {

    }

    @Override
    public void onUserLeft(List<LinkMicUserModel> users) {

    }

    @Override
    public void onCameraStreamAvailable(String userId, boolean isAnchor, View view) {

    }

    @Override
    public void onRemoteCameraStateChanged(String userId, boolean open) {

    }

    @Override
    public void onRemoteMicStateChanged(String userId, boolean open) {

    }

    @Override
    public void onInvited(LinkMicUserModel inviter, List<LinkMicUserModel> invitedUsers) {

    }

    @Override
    public void onInviteCanceledForMe() {

    }

    @Override
    public void onInviteRejected(List<LinkMicUserModel> users) {

    }

    @Override
    public void onApplied(boolean newApplied, List<LinkMicUserModel> users) {

    }

    @Override
    public void onApplyCanceled(List<LinkMicUserModel> users) {

    }

    @Override
    public void onApplyResponse(boolean approve, String userId) {

    }

    @Override
    public void onKicked(List<LinkMicUserModel> users) {

    }

    @Override
    public void onSelfMicAllowed(boolean allowed) {

    }

    @Override
    public void onSelfMicClosedByAnchor() {

    }

    @Override
    public void onAnchorInviteToOpenMic() {

    }

    @Override
    public void onAllMicAllowed(boolean allowed) {

    }
}
