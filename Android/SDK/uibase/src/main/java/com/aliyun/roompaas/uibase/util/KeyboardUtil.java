//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.aliyun.roompaas.uibase.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.os.Build;
import android.os.Build.VERSION;
import android.support.annotation.IntegerRes;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aliyun.roompaas.base.log.Logger;
import com.aliyun.roompaas.base.util.Utils;
import com.aliyun.roompaas.uibase.listener.OnKeyboardVisibilityListener;

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

    public static boolean isKeyboardShown(View view) {
        View rootView;
        if (view == null || (rootView = view.getRootView()) == null) {
            return false;
        }
        final int softKeyboardHeight = 100;
        Rect r = new Rect();
        rootView.getWindowVisibleDisplayFrame(r);
        DisplayMetrics dm = rootView.getResources().getDisplayMetrics();
        int heightDiff = rootView.getBottom() - r.bottom;
        return heightDiff > softKeyboardHeight * dm.density;
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
        if (input == null || Utils.isActivityInvalid(activity)) {
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
        if (input == null || Utils.isActivityInvalid(activity)) {
            return;
        }

        input.setFocusable(true);
        input.setFocusableInTouchMode(true);
        input.requestFocus();
        InputMethodManager mgr = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (mgr == null) {
            return;
        }
        // Caution!!
        // do not set always visible
        // it will cause keyboard auto show up even dialog dismiss on Some Device
        //Window window = activity.getWindow();
        //if (window != null) {
        //    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        //}
        mgr.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
    }

    public static void hideKeyboard(Activity activity, EditText input) {
        hideKeyboard(input, activity);
    }

    public static void hideKeyboard(EditText input, Activity activity) {
        InputMethodManager imm;
        if (Utils.isActivityInvalid(activity)
                || (imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE)) == null) {
            return;
        }

        View currentFocus = activity.getCurrentFocus();
        View content = activity.findViewById(android.R.id.content);
        boolean hidden = hideKeyboard(imm, input)
                || hideKeyboard(imm, currentFocus)
                || hideKeyboard(imm, content);
        Logger.i("hidden result:" + hidden);
        input.clearFocus();
    }

    public static boolean hideKeyboard(InputMethodManager imm, View view) {
        return imm != null && view != null && imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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

    public static ViewTreeObserver.OnGlobalLayoutListener addKeyboardVisibilityChangeListener(View view, final OnKeyboardVisibilityListener listener) {
        if (view == null || listener == null) {
            return null;
        }

        final WeakReference<View> viewRef = new WeakReference<>(view);
        final WeakReference<OnKeyboardVisibilityListener> listenerRef = new WeakReference<>(listener);

        ViewTreeObserver.OnGlobalLayoutListener layoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            private boolean alreadyOpen;
            private final int defaultKeyboardHeightDP = 100;
            private final int EstimatedKeyboardDP = defaultKeyboardHeightDP + (VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? 48 : 0);
            private final Rect rect = new Rect();

            @Override
            public void onGlobalLayout() {
                View v = viewRef.get();
                OnKeyboardVisibilityListener lis = listenerRef.get();
                if (v == null || lis == null) {
                    return;
                }

                int estimatedKeyboardHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, EstimatedKeyboardDP, v.getResources().getDisplayMetrics());
                v.getWindowVisibleDisplayFrame(rect);
                int heightDiff = v.getRootView().getHeight() - (rect.bottom - rect.top);
                boolean isShown = heightDiff >= estimatedKeyboardHeight;

                if (isShown == alreadyOpen) {
                    Logger.i("Keyboard state", "Ignoring global layout change...");
                    return;
                }
                alreadyOpen = isShown;
                lis.onKeyboardVisibilityChanged(isShown);
            }
        };
        view.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
        return layoutListener;
    }

    public static boolean removeKeyboardVisibilityChangeListener(View view, final ViewTreeObserver.OnGlobalLayoutListener listener) {
        if (view == null || listener == null) {
            return false;
        }

        view.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
        return true;
    }

    public static void adjustMarginSpace(View adjustor, boolean isVisible
            , @IntegerRes int visibleWeightIntegerId, @IntegerRes int inVisibleWeightIntegerId) {
        Context context;
        if (adjustor == null || (context = adjustor.getContext()) == null) {
            return;
        }
        ViewGroup.LayoutParams lp = adjustor.getLayoutParams();
        if (lp instanceof LinearLayout.LayoutParams) {
            LinearLayout.LayoutParams llp = (LinearLayout.LayoutParams) lp;
            llp.weight = context.getResources().getInteger(isVisible ? visibleWeightIntegerId : inVisibleWeightIntegerId);
            adjustor.requestLayout();
        }
    }
}
