package com.aliyun.standard.liveroom.lib.component.component;

import android.support.annotation.Keep;

import com.aliyun.roompaas.uibase.util.DialogUtil;
import com.aliyun.standard.liveroom.lib.component.BaseComponent;

/**
 * @author puke
 * @version 2022/3/21
 */
@Keep
public class LiveEnterRoomErrorComponent extends BaseComponent {

    @Override
    public void onEnterRoomError(String errorMsg) {
        // 默认实现: 弹窗 + 退出
        DialogUtil.tips(activity, "进入房间失败: " + errorMsg, new Runnable() {
            @Override
            public void run() {
                activity.finish();
            }
        });
    }
}
