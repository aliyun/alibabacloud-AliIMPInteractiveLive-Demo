package com.aliyun.standard.liveroom.lib.linkmic;

import com.aliyun.roompaas.base.exposable.Callback;

/**
 * Rtc观众服务
 *
 * @author puke
 * @version 2022/1/5
 */
public interface AudienceService extends CommonService {

    /**
     * 处理主播的加入连麦邀请<hr/>
     * 在{@link LinkMicEventHandler#onInvited}回调中使用
     *
     * @param agree true: 同意邀请; false: 拒绝邀请;
     */
    void handleInvite(boolean agree);

    /**
     * 申请连麦<hr/>
     * 发出申请后, 接收方会收到{@link LinkMicEventHandler#onApplied}回调
     *
     * @param callback 回调函数
     */
    void apply(Callback<Void> callback);

    /**
     * 取消申请连麦<hr/>
     * 取消申请后, 接收方会收到{@link LinkMicEventHandler#onApplied}回调
     *
     * @param callback 回调函数
     */
    void cancelApply(Callback<Void> callback);

    /**
     * 处理申请连麦的响应结果
     *
     * @param join 是否加入连麦
     */
    void handleApplyResponse(boolean join);
}
