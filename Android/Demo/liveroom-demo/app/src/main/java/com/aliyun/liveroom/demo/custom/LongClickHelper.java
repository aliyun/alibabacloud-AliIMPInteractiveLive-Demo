package com.aliyun.liveroom.demo.custom;

import android.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;

/**
 * 长按自定义视图, 弹窗提示对应组件名
 *
 * @author puke
 * @version 2021/12/13
 */
public class LongClickHelper {

    public static void attach(View view) {
        String message = String.format("当前组件为「%s」", view.getClass().getSimpleName());
        setLongClickListener(view, message, true);
    }

    private static void setLongClickListener(View view, String message, boolean isRoot) {
        if (isRoot || view.isClickable()) {
            view.setOnLongClickListener(v -> {
                new AlertDialog.Builder(v.getContext())
                        .setTitle("提示")
                        .setMessage(message)
                        .setPositiveButton("确定", null)
                        .show();
                return false;
            });
        }

        if ((view instanceof ViewGroup)) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                setLongClickListener(child, message, false);
            }
        }
    }
}
