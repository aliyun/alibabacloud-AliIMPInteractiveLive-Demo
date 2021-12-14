package com.aliyun.roompaas.app.helper;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.aliyun.roompaas.app.util.AppUtil;

/**
 * @author puke
 * @version 2018/8/16
 */
public class KeyboardHelper {

    private static final int DEFAULT_VALVE = AppUtil.getScreenHeight() / 3;

    private final Activity activity;
    private View rootView;
    private int lastVisibleHeight;

    private OnSoftKeyBoardChangeListener onSoftKeyBoardChangeListener;

    public interface OnSoftKeyBoardChangeListener {
        void keyBoardShow(int height);

        void keyBoardHide(int height);
    }

    public KeyboardHelper(Activity activity) {
        this(activity, DEFAULT_VALVE);
    }

    public KeyboardHelper(Activity activity, final int keyboardValve) {
        this.activity = activity;
        // 获取activity的根视图
        rootView = activity.getWindow().getDecorView();
        // 监听视图树中全局布局发生改变或者视图树中的某个视图的可视状态发生改变
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // 获取当前根视图在屏幕上显示的大小
                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);
                int visibleHeight = r.height();
                int keyboardHeight = AppUtil.getScreenHeight() - visibleHeight;

                if (lastVisibleHeight == 0) {
                    lastVisibleHeight = visibleHeight;
                    return;
                }

                if (visibleHeight > lastVisibleHeight) {
                    // 键盘变低
                    if (visibleHeight - lastVisibleHeight > keyboardValve) {
                        // 低过阈值, 则被视为隐藏
                        if (onSoftKeyBoardChangeListener != null) {
                            onSoftKeyBoardChangeListener.keyBoardHide(visibleHeight - lastVisibleHeight);
                        }
                        lastVisibleHeight = visibleHeight;
                    }
                } else if (visibleHeight < lastVisibleHeight) {
                    // 键盘变高
                    if (lastVisibleHeight - visibleHeight > keyboardValve) {
                        // 高过阈值, 则被视为展示
                        if (onSoftKeyBoardChangeListener != null) {
                            onSoftKeyBoardChangeListener.keyBoardShow(lastVisibleHeight - visibleHeight);
                        }
                        lastVisibleHeight = visibleHeight;
                    }
                }
            }
        });
    }

    public void showByEditText(EditText editText) {
        InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null && editText != null && editText.requestFocus()) {
            inputManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public void shrinkByEditText(EditText editText) {
        InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null && editText != null) {
            inputManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
    }

    public void setOnSoftKeyBoardChangeListener(OnSoftKeyBoardChangeListener onSoftKeyBoardChangeListener) {
        this.onSoftKeyBoardChangeListener = onSoftKeyBoardChangeListener;
    }
}
