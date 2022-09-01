package com.aliyun.standard.liveroom.lib.component.component;

import android.support.annotation.Keep;
import android.text.TextUtils;

import com.aliyun.roompaas.biz.SampleRoomEventHandler;
import com.aliyun.roompaas.biz.exposable.event.KickUserEvent;
import com.aliyun.roompaas.uibase.util.DialogUtil;
import com.aliyun.standard.liveroom.lib.LiveContext;
import com.aliyun.standard.liveroom.lib.component.BaseComponent;

/**
 * @author puke
 * @version 2021/8/30
 */
@Keep
public class LiveKickOutComponent extends BaseComponent {

    @Override
    public void onInit(LiveContext liveContext) {
        super.onInit(liveContext);

        // 监听房间事件
        roomChannel.addEventHandler(new SampleRoomEventHandler() {
            @Override
            public void onRoomUserKicked(KickUserEvent event) {
                if (TextUtils.equals(roomChannel.getUserId(), event.kickUser)) {
                    // 被踢人, 弹窗提示离开页面 (确定或取消均离开)
                    Runnable handler = new Runnable() {
                        @Override
                        public void run() {
                            activity.finish();
                        }
                    };
                    DialogUtil.confirm(activity, "您被管理员移除直播间", handler, handler);
                }
            }
        });
    }
}
