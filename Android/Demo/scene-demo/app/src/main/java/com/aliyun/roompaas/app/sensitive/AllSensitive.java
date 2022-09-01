package com.aliyun.roompaas.app.sensitive;

import android.content.Context;
import android.content.Intent;
import android.widget.TextView;

import com.aliyun.roompaas.app.Const;
import com.aliyun.roompaas.app.util.AppUtil;
import com.aliyun.roompaas.app.util.DialogUtil;
import com.aliyun.roompaas.base.util.ThreadUtil;
import com.aliyun.roompaas.base.util.Utils;
import com.aliyun.roompaas.biz.RoomEngine;

/**
 * Created by KyleCe on 2021/7/6
 */
public class AllSensitive implements ISensitive {
    public static void switchEnv(TextView view, Context context) {
    }

    public static void showRelaunchAppConfirmDialog(Context context, boolean toIgnoreCondition, Runnable action) {
        if (toIgnoreCondition) {
            return;
        }

        String message = "切换需要重启App，是否确认";
        DialogUtil.confirm(context, message, () -> {
            Utils.run(action);
            relaunchApp(context);
        });
    }

    private static void relaunchApp(Context context) {
        ThreadUtil.postDelay(1000, () -> {
            final Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
            }
            AppUtil.exitApplication();
        });
    }

    public static void environmentConcern(RoomEngine roomEngine) {
    }

    public static String parseAppServer() {
        return Const.SERVER_HOST_BUSINESS;
    }
}
