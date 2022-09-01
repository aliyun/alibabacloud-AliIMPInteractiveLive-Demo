package com.aliyun.standard.liveroom.lib.linkmic;

import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.rtc.RtcLayoutModel;
import com.aliyun.roompaas.rtc.exposable.RTCBypassPeerVideoConfig;

import java.util.List;

/**
 * Rtc主播服务
 *
 * @author puke
 * @version 2022/1/5
 */
public interface AnchorService extends CommonService {

    /**
     * 邀请观众加入连麦<hr/>
     * 发出邀请后, 接收方会收到{@link LinkMicEventHandler#onInvited}回调
     *
     * @param userIds  被邀请用户Id
     * @param callback 回调函数
     */
    void invite(List<String> userIds, Callback<Void> callback);

    /**
     * 取消邀请<hr/>
     * 取消邀请后, 接收方会收到{@link LinkMicEventHandler#onKicked}回调
     * TODO 此处不合理, 考虑优化
     *
     * @param userIds  被取消邀请的用户Id
     * @param callback 回调函数
     */
    void cancelInvite(List<String> userIds, Callback<Void> callback);

    /**
     * 处理观众的加入连麦申请<hr/>
     * 在{@link LinkMicEventHandler#onApplied}回调中使用
     *
     * @param userId   待处理的用户Id
     * @param agree    true: 同意申请; false: 拒绝申请;
     * @param callback 回调函数
     */
    void handleApply(String userId, boolean agree, Callback<Void> callback);

    /**
     * 踢出连麦<hr/>
     * 踢出后, 接收方会收到{@link LinkMicEventHandler#onKicked}回调
     *
     * @param userIds  被踢用户Id列表
     * @param callback 回调函数
     */
    void kick(List<String> userIds, Callback<Void> callback);

//    /**
//     * 设置是否允许远端麦克风开启<hr/>
//     * 设置后, 接收方会收到{@link MicEventHandler#onRemoteMicStateChanged}回调
//     *
//     * @param userId 目标用户Id
//     * @param enable true: 开启; false: 关闭;
//     */
//    void setRemoteMicEnable(String userId, boolean enable);
//
//    /**
//     * 设置是否允许所有远端麦克风开启<hr/>
//     * 设置后, 接收方会收到{@link MicEventHandler#onRemoteMicStateChanged}回调
//     * TODO 这里有歧义, 是否记录原始状态
//     *
//     * @param enable true: 开启; false: 关闭;
//     */
//    void setAllRemoteMicEnable(boolean enable);

    /**
     * 开始直播
     */
    void startLive();

    /**
     * 暂停直播
     *
     * @param callback 回调函数
     */
    void pauseLive(Callback<Void> callback);

    /**
     * 结束直播
     *
     * @param callback 回调函数
     */
    void stopLive(Callback<Void> callback);

    /**
     * 设置预设的旁路直播布局方式
     *
     * @param layoutModel 布局方式
     * @param userIds     目标用户Id列表 (从左上到右下依次排序)
     * @param callback    回调函数
     */
    void setEnumBypassLiveLayout(RtcLayoutModel layoutModel, List<String> userIds, Callback<Void> callback);

    /**
     * 设置自定义的旁路直播布局方式
     *
     * @param configs  自定义配置信息
     * @param callback 回调函数
     */
    void setCustomBypassLiveLayout(List<RTCBypassPeerVideoConfig> configs, Callback<Void> callback);
}
