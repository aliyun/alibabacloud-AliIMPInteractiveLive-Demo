package com.aliyun.roompaas.base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;

import com.aliyun.roompaas.base.log.Logger;
import com.aliyun.roompaas.base.util.Utils;

/**
 * @author puke
 * @version 2021/6/22
 */
public class AppContext {
    public static final String TAG = "AppContext";
    public static final String CLASS_PATH_FOR_BEAUTY_PRO_REMOTE_RESOURCE = "com.aliyun.roompaas.beauty_pro.remote.ResDownloadDelegate";

    @SuppressLint("StaticFieldLeak")
    private static Context context;
    private static final String[] toLoadClasses = {
            CLASS_PATH_FOR_BEAUTY_PRO_REMOTE_RESOURCE,
    };

    public static void setContext(Context context) {
        if (AppContext.context == null && context != null) {
            AppContext.context = Utils.acceptFirstNonNull(context.getApplicationContext(), context);

            loadClassesIfVital();
        }
    }

    private static void loadClassesIfVital() {
        Logger.i(TAG, "loadClassesIfVital: ");
        for (String clz : toLoadClasses) {
            if (TextUtils.isEmpty(clz)) {
                continue;
            }
            loadClz(clz);
        }
    }

    public static void loadClz(String name) {
        try {
            Class<?> cls = Class.forName(name);
            cls.getConstructor().newInstance();
            Logger.i(TAG, "loadClz done: " + cls);
        } catch (Throwable e) {
            Logger.i(TAG, "loadClz error : " + e);
            Logger.i(TAG, "loadClz error cause: " + e.getCause());
        }
    }

    public static Context getContext() {
        return context;
    }
}
