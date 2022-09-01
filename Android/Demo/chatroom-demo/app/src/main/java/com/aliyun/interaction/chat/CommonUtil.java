package com.aliyun.interaction.chat;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;

import com.aliyun.roompaas.base.BuildConfig;
import com.aliyun.roompaas.base.util.ThreadUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author puke
 * @version 2021/5/8
 */
public class CommonUtil {

    /**
     * @param context 上下文
     * @return 版本号
     */
    public static String getVersionCode(Context context) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo;
        String versionCode = "";
        try {
            packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            versionCode = packageInfo.versionCode + "";
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    /**
     * @param context 上下文
     * @return 版本名称
     */
    public static String getVersionName(Context context) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo;
        String versionName = "";
        try {
            packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    /**
     * @return 获取设备唯一ID
     */
    public static String getDeviceId() {
        String m_szDevIDShort = "35"
                + (Build.BOARD.length() % 10)
                + (Build.BRAND.length() % 10)
                + (Build.CPU_ABI.length() % 10)
                + (Build.DEVICE.length() % 10)
                + (Build.MANUFACTURER.length() % 10)
                + (Build.MODEL.length() % 10)
                + (Build.PRODUCT.length() % 10);
        String serial;
        try {
            serial = Build.class.getField("SERIAL").get(null).toString();
            return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
        } catch (Exception exception) {
            serial = "serial";
        }
        return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
    }

    @AnyThread
    public static void showDebugToast(final Context context, final String toast) {
        if (!BuildConfig.DEBUG) {
            return;
        }
        showToast(context, "[仅Debug展示]:" + toast);
    }

    @AnyThread
    public static void showToast(final Context context, final String toast) {
        if (context == null || TextUtils.isEmpty(toast)) {
            return;
        }

        ThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void run(Runnable runnable) {
        if (runnable != null) {
            runnable.run();
        }
    }

    public static boolean isActivityInvalid(Activity activity) {
        return activity == null || activity.isFinishing();
    }

    @NonNull
    public static String parseFormatString(long ts, String format) {
        if (ts <= 0 || TextUtils.isEmpty(format)) {
            return String.valueOf(ts);
        }
        return new SimpleDateFormat(format, Locale.getDefault()).format(new Date(ts));
    }

    @NonNull
    public static String parseHourMinuteSeconds(long millis) {
        try {
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                    TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                    TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
        } catch (Throwable ignore) {
            return String.valueOf(millis);
        }
    }
}
