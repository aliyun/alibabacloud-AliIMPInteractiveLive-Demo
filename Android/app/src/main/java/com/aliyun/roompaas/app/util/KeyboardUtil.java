//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.aliyun.roompaas.app.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.os.Build.VERSION;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.aliyun.roompaas.base.util.CommonUtil;

import java.lang.ref.WeakReference;

public class KeyboardUtil {
    public KeyboardUtil() {
    }

    public static int getFontHeight(TextView textView) {
        Paint paint = new Paint();
        paint.setTextSize(textView.getTextSize());
        FontMetrics fm = paint.getFontMetrics();
        return (int) Math.ceil((double) (fm.bottom - fm.top));
    }

    public static boolean isFullScreen(Activity activity) {
        return (activity.getWindow().getAttributes().flags & 1024) != 0;
    }

    public static boolean isKeyboardShowing(Context context) {
        return context.getResources().getConfiguration().keyboardHidden == 1;
    }

    public static int getSupportSoftInputHeight(Activity activity) {
        Rect r = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
        int screenHeight = activity.getWindow().getDecorView().getRootView().getHeight();
        int softInputHeight = screenHeight - r.bottom;
        if (VERSION.SDK_INT >= 20) {
            softInputHeight -= getSoftButtonsBarHeight(activity);
        }

        return softInputHeight;
    }

    private static int getSoftButtonsBarHeight(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int usableHeight = metrics.heightPixels;
        activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        int realHeight = metrics.heightPixels;
        return realHeight > usableHeight ? realHeight - usableHeight : 0;
    }

    public static void enterDelete(EditText editText) {
        int action = 0;
        int code = 67;
        KeyEvent event = new KeyEvent(action, code);
        editText.onKeyDown(67, event);
    }

    public static void openSoftKeyboard(View editText) {
        if (editText != null) {
            editText.setFocusable(true);
            editText.setFocusableInTouchMode(true);
            editText.requestFocus();
            InputMethodManager inputManager = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputManager != null) {
                inputManager.showSoftInput(editText, 0);
            }
        }

    }

    public static void showUpSoftKeyboard(EditText input, Activity activity) {
        showUpSoftKeyboard(input, activity, false);
    }

    /**
     * 唤起键盘触发布局调整等场景需要等待EditText处理
     */
    public static void showUpSoftKeyboard(final EditText input, final Activity activity, boolean withPostProcess) {
        if (input == null || CommonUtil.isActivityInvalid(activity)) {
            return;
        }

        final WeakReference<Activity> activityRef = new WeakReference<>(activity);
        final WeakReference<EditText> inputRef = new WeakReference<>(input);
        Runnable action = new Runnable() {
            @Override
            public void run() {
                showKeyboardCoreProcess(inputRef, activityRef);
            }
        };

        if (!withPostProcess) {
            action.run();
        } else {
            input.post(action);
        }
    }

    public static void showKeyboardCoreProcess(WeakReference<EditText> inputRef, WeakReference<Activity> activityRef) {
        Activity activity = activityRef != null ? activityRef.get() : null;
        EditText input = inputRef != null ? inputRef.get() : null;
        if (input == null || CommonUtil.isActivityInvalid(activity)) {
            return;
        }

        input.setFocusable(true);
        input.setFocusableInTouchMode(true);
        input.requestFocus();
        InputMethodManager mgr = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (mgr == null) {
            return;
        }
        Window window = activity.getWindow();
        if (window != null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
        mgr.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
    }

    public static void hideKeyboard(Activity activity, EditText input) {
        hideKeyboard(input, activity);
    }

    public static void hideKeyboard(EditText input, Activity activity) {
        if (CommonUtil.isActivityInvalid(activity)) {
            return;
        }
        View view = activity.getCurrentFocus();
        view = view == null ? input : view;

        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (view == null || imm == null) {
            return;
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void closeSoftKeyboard(Context context) {
        if (context instanceof Activity && ((Activity) context).getCurrentFocus() != null) {
            View view = ((Activity) context).getCurrentFocus();
            closeSoftKeyboard(view);
        }
    }

    public static void closeSoftKeyboard(View view) {
        if (view != null && view.getWindowToken() != null) {
            try {
                Context context = view.getContext();
                InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                view.clearFocus();
                if (inputManager != null) {
                    inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            } catch (Exception var3) {
            }

        }
    }
}
