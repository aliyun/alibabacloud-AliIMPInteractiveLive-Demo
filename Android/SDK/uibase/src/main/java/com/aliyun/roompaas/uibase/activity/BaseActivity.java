package com.aliyun.roompaas.uibase.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.aliyun.roompaas.base.util.CommonUtil;
import com.aliyun.roompaas.base.util.PermissionStrategy;
import com.aliyun.roompaas.base.util.ThreadUtil;
import com.aliyun.roompaas.base.util.Utils;
import com.aliyun.roompaas.uibase.util.BottomMarginAdapter;
import com.aliyun.roompaas.uibase.util.immersionbar.ImmersionBar;
import com.aliyun.roompaas.uibase.util.immersionbar.OnNavigationBarListener;
import com.aliyun.roompaas.uibase.view.IImmersiveSupport;

public abstract class BaseActivity extends AppCompatActivity implements IImmersiveSupport {

    protected Context context;
    private PermissionStrategy permissionStrategy;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        context = this;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!shouldDisableImmersive()) {
            statusBarProcess(this);
        } else {
            Utils.run(actionWhenDisableImmersive());
        }

        permissionStrategy = new PermissionStrategy(this, parsePermissionArray(), permissionIgnoreStrictCheck()
                , asPermissionGrantedAction(), asPermissionRejectedAction(), asPermissionGuidanceAction());
    }

    @Override
    protected void onResume() {
        super.onResume();
        permissionStrategy.onResume(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.destroy(permissionStrategy);
    }

    protected boolean permissionIgnoreStrictCheck(){
        return false;
    }

    protected String[] parsePermissionArray() {
        return new String[0];
    }

    protected Runnable asPermissionGrantedAction() {
        return null;
    }

    protected Runnable asPermissionRejectedAction() {
        return null;
    }

    protected Runnable asPermissionGuidanceAction() {
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionStrategy.handleRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public boolean shouldDisableImmersive() {
        return true;
    }

    protected Runnable actionWhenDisableImmersive(){
        return null;
    }

    private void statusBarProcess(Activity activity) {
        ImmersionBar.with(activity)
                .transparentBar()
                .setOnNavigationBarListener(new OnNavigationBarListener() {
                    @Override
                    public void onNavigationBarChange(boolean show) {
                        BottomMarginAdapter.adjust(toAdjustBottomView(), show);
                    }
                })
                .init();
    }

    @Nullable
    protected View toAdjustBottomView() {
        return null;
    }

    protected void showToast(@StringRes int id) {
        CommonUtil.showToast(this, id);
    }

    protected void showToast(String toast) {
        CommonUtil.showToast(this, toast);
    }

    protected void postToMain(@Nullable Runnable task) {
        ThreadUtil.runOnUiThread(task);
    }

    protected void postDelay(@Nullable Runnable task, long delayMillis) {
        ThreadUtil.postDelay(delayMillis, task);
    }

    protected boolean isActivityValid() {
        return !isFinishing() && !isDestroyed();
    }
}
