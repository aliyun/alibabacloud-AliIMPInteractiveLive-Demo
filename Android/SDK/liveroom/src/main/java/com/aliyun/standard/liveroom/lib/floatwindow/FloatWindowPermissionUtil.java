package com.aliyun.standard.liveroom.lib.floatwindow;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

/**
 * @author puke
 * @version 2021/12/27
 */
public class FloatWindowPermissionUtil {

    public static final int REQUEST_FLOAT_CODE = 1001;

    /**
     * 判断悬浮窗权限权限
     */
    public static boolean checkPermission(Context context) {
        // TODO: 2021/12/27 权限需要做多设备适配
        boolean result = true;
        if (Build.VERSION.SDK_INT >= 23) {
            try {
//                Method canDrawOverlays = Settings.class.getDeclaredMethod("canDrawOverlays", Context.class);
//                result = (boolean) canDrawOverlays.invoke(null, context);
                result = Settings.canDrawOverlays(context);
            } catch (Exception e) {
                Log.e("ServiceUtils", Log.getStackTraceString(e));
            }
        }
        return result;
    }

    /**
     * 检查悬浮窗权限是否开启
     */
    public static void checkPermission(final Activity activity, Runnable block) {
        if (checkPermission(activity)) {
            block.run();
        } else {
            new AlertDialog.Builder(activity)
                    .setMessage("请开启悬浮窗权限")
                    .setNegativeButton("取消", null)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            navToPermissionSettingPage(activity);
                        }
                    })
                    .show();
        }
    }

    public static void navToPermissionSettingPage(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        intent.setData(Uri.parse("package:" + activity.getPackageName()));
        activity.startActivityForResult(intent, REQUEST_FLOAT_CODE);
    }
}
