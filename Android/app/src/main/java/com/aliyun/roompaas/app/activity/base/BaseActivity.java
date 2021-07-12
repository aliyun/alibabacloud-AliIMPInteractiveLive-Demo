package com.aliyun.roompaas.app.activity.base;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.aliyun.roompaas.app.util.StatusBarUtil;
import com.aliyun.roompaas.base.util.CommonUtil;

/**
 * 通用Activity, 封装最基础的复用逻辑
 *
 * @author puke
 * @version 2021/5/13
 */
public class BaseActivity extends AppCompatActivity {

    protected Context context;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        context = this;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.setStatusBarTransparentIfPossible(this);
    }

    protected void showToast(String toast) {
        CommonUtil.showToast(this, toast);
    }
}
