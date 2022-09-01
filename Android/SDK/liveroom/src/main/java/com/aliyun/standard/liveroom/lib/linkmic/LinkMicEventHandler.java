package com.aliyun.standard.liveroom.lib.linkmic;

import android.view.View;

import com.aliyun.standard.liveroom.lib.linkmic.model.LinkMicUserModel;

import java.util.List;

/**
 * @author puke
 * @version 2021/12/31
 */
public interface LinkMicEventHandler {

    /**
     * 入会成功 (自己)
     *
     * @param view 渲染视图
     */
    void onJoinedSuccess(View view);

    /**
     * 离会成功 (自己)
     */
    void onLeftSuccess();

    /**
     * 有成员加入连麦
     *
     * @param users 加入连麦的用户
     */
    void onUserJoined(List<LinkMicUserModel> users);

    /**
     * 有成员退出连麦
     *
     * @param users 退出连麦的用户
     */
    void onUserLeft(List<LinkMicUserModel> users);

    /**
     * 远端摄像头画面可用
     *
     * @param userId   视频流用户Id
     * @param isAnchor 是否是主播
     * @param view     视频流载体
     */
    void onCameraStreamAvailable(String userId, boolean isAnchor, View view);

    /**
     * 远端摄像头开关状态改变
     *
     * @param userId 视频流用户Id
     * @param open   true: 开启; false: 关闭;
     */
    void onRemoteCameraStateChanged(String userId, boolean open);

    /**
     * 远端麦克风开关状态改变
     *
     * @param userId 音频流用户Id
     * @param open   true: 开启; false: 关闭;
     */
    void onRemoteMicStateChanged(String userId, boolean open);

    /**
     * 收到邀请
     *
     * @param inviter      邀请者
     * @param invitedUsers 被邀请者
     */
    void onInvited(LinkMicUserModel inviter, List<LinkMicUserModel> invitedUsers);

    /**
     * 邀请被取消 (主播邀请我之后, 我还没接收就被主播再取消)
     */
    void onInviteCanceledForMe();

    /**
     * 邀请被拒绝
     *
     * @param users 被拒绝的用户列表
     */
    void onInviteRejected(List<LinkMicUserModel> users);

    /**
     * 收到连麦申请
     *
     * @param newApplied 是否新申请
     * @param users      事件模型
     */
    void onApplied(boolean newApplied, List<LinkMicUserModel> users);

    /**
     * 连麦申请被取消
     *
     * @param users 事件模型
     */
    void onApplyCanceled(List<LinkMicUserModel> users);

    /**
     * 连麦申请发出后, 收到主播的处理结果
     *
     * @param approve true: 同意申请连麦; false: 拒绝申请连麦;
     * @param userId  申请连麦的用户Id
     */
    void onApplyResponse(boolean approve, String userId);

    /**
     * 有人被踢出连麦
     *
     * @param users 目标用户Id
     */
    void onKicked(List<LinkMicUserModel> users);

    /**
     * 自己的麦克风开关状态发生改变
     *
     * @param allowed true: 开启; false: 关闭;
     * @see #onAllMicAllowed(boolean)
     */
    @Deprecated
    void onSelfMicAllowed(boolean allowed);

    /**
     * 自己的麦克风被静音
     */
    void onSelfMicClosedByAnchor();

    /**
     * 主持人请求打开你的麦克风
     */
    void onAnchorInviteToOpenMic();

    /**
     * 主播允许/不允许所有观众打开麦克风，默认为允许
     * <p>
     * 当主播不允许所有观众打开麦克风时, 如果观众当前麦克风为开启状态, 内部会关闭麦克风
     *
     * @param allowed true: 允许打开麦克风; false: 不允许打开麦克风
     */
    void onAllMicAllowed(boolean allowed);
}
