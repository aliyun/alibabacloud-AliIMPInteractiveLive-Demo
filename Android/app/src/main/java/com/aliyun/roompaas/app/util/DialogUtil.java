package com.aliyun.roompaas.app.util;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.EditText;

/**
 * @author puke
 * @version 2021/5/17
 */
public class DialogUtil {

    public interface InputCallback {
        void onInput(String value);
    }

    public interface SingleChoiceCallback {
        void onChoice(int index);
    }

    public static class Action {
        public String text;
        public Runnable runnable;

        public Action(String text, Runnable runnable) {
            this.text = text;
            this.runnable = runnable;
        }
    }

    public static void singleChoice(Context context, String title, String[] choices, int checkedItem, final SingleChoiceCallback callback) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setSingleChoiceItems(choices, checkedItem, (dialog, which) -> {
                    dialog.dismiss();
                    if (callback != null) {
                        callback.onChoice(which);
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    public static void doAction(Context context, String title, final Action... actions) {
        CharSequence[] items = new CharSequence[actions.length];
        for (int i = 0; i < actions.length; i++) {
            items[i] = actions[i].text;
        }

        new AlertDialog.Builder(context)
                .setTitle(title)
                .setItems(items, (dialog, which) -> {
                    dialog.dismiss();
                    Runnable runnable = actions[which].runnable;
                    if (runnable != null) {
                        runnable.run();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    public static void input(Context context, String title, String defaultValue, final InputCallback inputCallback) {
        final EditText input = new EditText(context);
        input.setText(defaultValue);

        new AlertDialog.Builder(context)
                .setTitle(title)
                .setView(input)
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", (dialog, which) -> {
                    String value = input.getText().toString().trim();
                    if (inputCallback != null) {
                        inputCallback.onInput(value);
                    }
                })
                .show();
    }

    public static void confirm(Context context, String message, final Runnable runnable) {
        confirm(context, message, runnable, null);
    }

    public static void confirm(Context context, String message,
                               final Runnable confirmListener, final Runnable cancelListener) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setNegativeButton("取消", (dialog, which) -> {
                    if (cancelListener != null) {
                        cancelListener.run();
                    }
                })
                .setPositiveButton("确定", (dialog, which) -> {
                    if (confirmListener != null) {
                        confirmListener.run();
                    }
                })
                .setOnCancelListener(dialog -> {
                    if (cancelListener != null) {
                        cancelListener.run();
                    }
                })
                .show();
    }

    public static void tips(Context context, String message, final Runnable runnable) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton("确定", (dialog, which) -> {
                    if (runnable != null) {
                        runnable.run();
                    }
                })
                .setOnDismissListener(dialog -> {
                    if (runnable != null) {
                        runnable.run();
                    }
                })
                .show();
    }
}
