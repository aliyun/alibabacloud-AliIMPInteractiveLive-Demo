package com.aliyun.standard.liveroom.lib;

import com.aliyun.roompaas.base.log.Logger;
import com.aliyun.roompaas.base.util.ThreadUtil;
import com.aliyun.roompaas.biz.RoomEngine;
import com.aliyun.standard.liveroom.lib.floatwindow.FloatWindowManager;

/**
 * lwp断连任务管理器
 *
 * @author puke
 * @version 2021/10/27
 */
public class LogoutTaskManager {

    private static final String TAG = LogoutTaskManager.class.getSimpleName();

    private static final int LOGOUT_DELAY_TIME = 3 * 60 * 1000;

    private static final Runnable logoutTask = new Runnable() {
        @Override
        public void run() {
            Logger.i(TAG, "start invoke logout task");
            RoomEngine roomEngine = RoomEngine.getInstance();
            if (roomEngine.isLogin()) {
                Logger.i(TAG, "prepare done, start logout");
                roomEngine.logout(null);
            }
        }
    };

    public static void prepareLogout() {
        if (FloatWindowManager.instance().isShowing()) {
            // 小窗时不自动登出
            Logger.i(TAG, "Current has float window, ignore prepare logout");
            return;
        }

        Logger.i(TAG, "prepare logout");
        cancelLogout();
        if (RoomEngine.getInstance().isLogin()) {
            ThreadUtil.postDelay(LOGOUT_DELAY_TIME, logoutTask);
        }
    }

    public static void cancelLogout() {
        Logger.i(TAG, "cancel logout");
        ThreadUtil.cancel(logoutTask);
    }
}
