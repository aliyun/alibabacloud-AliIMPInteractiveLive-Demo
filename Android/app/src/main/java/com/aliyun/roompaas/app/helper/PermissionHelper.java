package com.aliyun.roompaas.app.helper;

import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.aliyun.roompaas.base.util.CollectionUtil;

import java.util.List;

import java8.util.J8Arrays;
import java8.util.stream.Collectors;

/**
 * @author puke
 * @version 2021/5/27
 */
public class PermissionHelper {

    private final Activity activity;
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
            boolean allGranted = J8Arrays.stream(grantResults)
                    .anyMatch(grantResult -> grantResult == PackageManager.PERMISSION_GRANTED);
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
     * 权限检测
     *
     * @param applyIfAbsent 对没有的权限自动进行申请
     * @return 是否拥有权限
     */
    public boolean hasPermission(boolean applyIfAbsent) {
        // 检测出未赋予的权限
        List<String> absentPermissions = J8Arrays.stream(permissions)
                .filter(permission -> !isGranted(permission))
                .collect(Collectors.toList());

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
        int checkResult = ContextCompat.checkSelfPermission(activity, permission);
        return checkResult == PackageManager.PERMISSION_GRANTED;
    }
}
