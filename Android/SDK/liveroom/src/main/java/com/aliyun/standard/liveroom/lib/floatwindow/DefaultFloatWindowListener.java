package com.aliyun.standard.liveroom.lib.floatwindow;

import android.app.Activity;
import android.content.Intent;

import com.aliyun.roompaas.base.util.Utils;
import com.aliyun.roompaas.player.LivePlayerManagerHolder;
import com.aliyun.standard.liveroom.lib.LiveActivity;

/**
 * 小窗默认处理器
 *
 * @author puke
 * @version 2021/12/24
 */
public class DefaultFloatWindowListener implements FloatWindowListener {

    private final Activity activity;
    private final Intent lastLiveIntent;

    public DefaultFloatWindowListener(Activity activity) {
        this.activity = activity;
        this.lastLiveIntent = activity.getIntent();
    }

    @Override
    public void onContentClick(FloatWindowManager manager) {
        // 点击内容, 默认跳转直播间页面
        Intent intent = new Intent(activity, LiveActivity.class);
        intent.putExtras(lastLiveIntent);
        activity.startActivity(intent);
    }

    @Override
    public void onCloseClick(FloatWindowManager manager) {
        // 点击小窗右上角的关闭按钮
        // 1. 释放播放器
        LivePlayerManagerHolder.destroyHoldManager();
        // 2. 关闭小窗
        manager.dismiss(false);
        // 3. 结束Activity
        if (Utils.isActivityValid(activity)) {
            activity.finish();
        }
    }
}
