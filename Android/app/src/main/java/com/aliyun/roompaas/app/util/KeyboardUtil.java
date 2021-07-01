//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.aliyun.roompaas.app.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.FontMetrics;
import android.os.Build.VERSION;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

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
