package com.aliyun.roompaas.app.sensitive;

import android.content.Context;
import android.widget.TextView;

import com.aliyun.roompaas.biz.RoomEngine;

/**
 * Created by KyleCe on 2021/7/6
 */
public class AllSensitive implements ISensitive {
    public static void switchEnv(TextView view, Context context) {
    }

    public static void showRelaunchAppConfirmDialog(Context context, boolean toIgnoreCondition, Runnable action) {
    }

    public static void environmentConcern(RoomEngine roomEngine) {
    }

    public static String parseAppServer() {
        return "";
    }
}
