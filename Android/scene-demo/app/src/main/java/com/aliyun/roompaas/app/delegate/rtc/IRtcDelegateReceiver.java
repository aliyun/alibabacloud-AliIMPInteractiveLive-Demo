package com.aliyun.roompaas.app.delegate.rtc;

import com.aliyun.roompaas.app.model.RtcUser;
import com.aliyun.roompaas.rtc.exposable.event.ConfUserEvent;

import java.util.Collection;
import java.util.List;

/**
 * Created by KyleCe on 2021/9/16
 */
public interface IRtcDelegateReceiver {
    void onRtcStart();

    void onRtcEnd();

    void onRtcGetKickedOffline();

    void onRtcLinkRequestRejected();

    void onRtcRemoteJoinSuccess(ConfUserEvent userEvent);

    void onRtcJoinRtcSuccess();

    void onRtcJoinRtcError(String msg);

    void onUpdateSelfMicStatus(boolean mute);

    void onUpdateSelfCameraStatus(boolean mute);

    void startRoadPublishSuccess();

    void updateUser(Collection<RtcUser> user);

    void usersLeave(ConfUserEvent leaveUserEvent);

    List<RtcUser> getUserList();
}
