package com.aliyun.roompaas.base.util;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.aliyun.roompaas.base.IDestroyable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author puke
 * @version 2021/5/27
 */
public class PermissionHelper implements IDestroyable {

    private Activity activity;
    private final int requestCode;
    private final String[] permissions;

    private Runnable grantedCallback;
    private Runnable rejectedCallback;

    public PermissionHelper(Activity activity, int requestCode, String... permissions) {
        this.activity = activity;
        this.requestCode = requestCode;
        this.permissions = permissions;
    }

    public void setGrantedCallback(Runnable grantedCallback) {
        this.grantedCallback = grantedCallback;
    }

    public void setRejectedCallback(Runnable rejectedCallback) {
        this.rejectedCallback = rejectedCallback;
    }

    /**
     * 处理{@link Activity#onRequestPermissionsResult}回调
     */
    public void handleRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                               @NonNull int[] grantResults) {
        if (requestCode == this.requestCode) {
            boolean allGranted = true;
            for (int grantResult : grantResults) {
                allGranted = grantResult == PackageManager.PERMISSION_GRANTED;
                if (!allGranted) {
                    break;
                }
            }
            if (allGranted) {
                if (grantedCallback != null) {
                    grantedCallback.run();
                }
            } else {
                if (rejectedCallback != null) {
                    rejectedCallback.run();
                }
            }
        }
    }

    /**
     * 权限检测 (和{@link #hasPermission}的区别在于, 该方法会触发{@link #grantedCallback}回调)
     */
    public void checkPermission() {
        if (hasPermission(true)) {
            if (grantedCallback != null) {
                grantedCallback.run();
            }
        }
    }

    /**
     * 权限检测
     *
     * @return 是否拥有权限
     */
    public boolean hasPermission() {
        return hasPermission(false);
    }

    /**
     * @return 触发权限申请返回true，否则false
     */
    public boolean requestPermissionIfVital() {
        return !hasPermission(true);
    }

    /**
     * 权限检测
     *
     * @param applyIfAbsent 对没有的权限自动进行申请
     * @return 是否拥有权限
     */
    public boolean hasPermission(boolean applyIfAbsent) {
        List<String> absentPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (!isGranted(permission)) {
                absentPermissions.add(permission);
            }
        }

        if (CollectionUtil.isEmpty(absentPermissions)) {
            return true;
        }

        // 对未赋予的权限进行申请
        if (applyIfAbsent) {
            ActivityCompat.requestPermissions(
                    activity, absentPermissions.toArray(new String[0]), requestCode);
        }
        return false;
    }

    private boolean isGranted(String permission) {
        return isGranted(activity, permission);
    }

    public static boolean isGranted(Activity activity, String permission){
        return permission != null && Utils.isActivityValid(activity) && ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public void execute4Granted() {
        Utils.run(grantedCallback);
    }

    public void execute4Rejected() {
        Utils.run(rejectedCallback);
    }

    @Override
    public void destroy() {
        grantedCallback = null;
        rejectedCallback = null;
        activity = null;
    }
}
