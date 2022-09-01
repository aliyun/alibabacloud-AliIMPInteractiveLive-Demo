package com.aliyun.roompaas.uibase.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.support.annotation.ColorRes;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.aliyun.roompaas.base.IDestroyable;
import com.aliyun.roompaas.base.log.Logger;
import com.aliyun.roompaas.uibase.R;
import com.aliyun.roompaas.uibase.listener.SimpleTextWatcher;
import com.aliyun.roompaas.uibase.util.DialogUtil;
import com.aliyun.roompaas.uibase.util.KeyboardUtil;
import com.aliyun.roompaas.uibase.util.ViewUtil;
import com.aliyun.roompaas.uibase.util.immersionbar.ImmersionBar;

/**
 * @author puke
 * @version 2021/7/29
 */
public abstract class DialogInputView extends FrameLayout implements IDestroyable {
    public static final String TAG = "LiveInputView";

    protected final TextView commentInput;

    private Dialog dialog;
    private int largestInputLocationY;
    private static final int MINI_KEYBOARD_ALTER = 200;
    private CharSequence latestUnsentInputContent;

    public DialogInputView(@NonNull Context context) {
        this(context, null, 0);
    }

    public DialogInputView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DialogInputView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, getDefaultLayoutResId(), this);
        commentInput = findViewById(respondingEditTextId());
        ViewUtil.bindClickActionWithClickCheck(commentInput, new Runnable() {
            @Override
            public void run() {
                onInputClick();
            }
        });
    }

    @LayoutRes
    protected abstract int getDefaultLayoutResId();

    @IdRes
    protected abstract int respondingEditTextId();

    @StringRes
    protected abstract int inputHintInDialog();

    protected int getSendCommentMaxLength() {
        return 50;
    }

    protected void onRespondingViewClicked() {
    }

    protected void onCommentLenReachLimit(int maxLen) {

    }

    protected void onSendClickContentEmpty() {

    }

    protected void onCommentSubmit(String inputText) {

    }

    @ColorRes
    protected int inputDisabledColor(){
        return 0;
    }

    @ColorRes
    protected int inputEnabledColor(){
        return 0;
    }

    public void disableClick() {
        commentInput.setClickable(false);
        commentInput.setFocusable(false);
        commentInput.setEnabled(false);
        ViewUtil.applyTextColor(commentInput, inputDisabledColor());
    }

    public void enableClick(){
        commentInput.setClickable(true);
        commentInput.setFocusable(true);
        commentInput.setEnabled(true);
        ViewUtil.applyTextColor(commentInput, inputEnabledColor());
    }

    protected void onInputClick() {
        onRespondingViewClicked();
        Context context = getContext();
        dialog = createDialog(context);
        final EditText dialogInput = dialog.findViewById(R.id.dialog_comment_input);
        dialogInput.setHint(inputHintInDialog());
        View dialogRootView = dialog.findViewById(R.id.dialog_root);

        // 点击空白区域, 隐藏键盘和dialog
        dialogRootView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboardAndDismissDialog(dialogInput);
            }
        });

        // 键盘消失, 隐藏dialog
        exitWhenKeyboardCollapse(dialogInput);

        // 添加最大长度限制
        final int sendCommentMaxLength = getSendCommentMaxLength();
        dialogInput.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                latestUnsentInputContent = s;
                if (sendCommentMaxLength > 0 && s.length() > sendCommentMaxLength) {
                    dialogInput.setText(s.subSequence(0, sendCommentMaxLength));
                    dialogInput.setSelection(sendCommentMaxLength);
                    onCommentLenReachLimit(sendCommentMaxLength);
                }
            }
        });
        if (!TextUtils.isEmpty(latestUnsentInputContent)) {
            dialogInput.setText(latestUnsentInputContent);
            dialogInput.setSelection(latestUnsentInputContent.length());
        }

        // 添加提交事件处理
        dialogInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendProcess(dialogInput);
                    return true;
                }
                return false;
            }
        });

        ViewUtil.bindClickActionWithClickCheck(dialog.findViewById(R.id.sendButton), new Runnable() {
            @Override
            public void run() {
                sendProcess(dialogInput);
            }
        });

        ViewUtil.addOnGlobalLayoutListener(dialogInput, new Runnable() {
            @Override
            public void run() {
                KeyboardUtil.showUpSoftKeyboard(dialogInput, (Activity) getContext());
                dialogInput.animate().setStartDelay(150).setDuration(150).alpha(1).start();
            }
        });
        clearFlags(dialog.getWindow(), WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        applySoftInputMode(dialog.getWindow(), WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        dialog.show();

        boolean disableImmersive = context instanceof IImmersiveSupport && ((IImmersiveSupport) context).shouldDisableImmersive();
        if (!disableImmersive && immersiveInsteadOfShowingStatusBar()) {
            ImmersionBar.with((Activity) getContext(), dialog).init();
        } else {
            dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }
    }

    private void sendProcess(final EditText dialogInput) {
        String inputText = dialogInput.getText().toString().trim();
        if (TextUtils.isEmpty(inputText)) {
            onSendClickContentEmpty();
        } else {
            latestUnsentInputContent = "";
            onCommentSubmit(inputText);
            hideKeyboardAndDismissDialog(dialogInput);
        }
    }

    protected boolean immersiveInsteadOfShowingStatusBar(){
        return true;
    }

    private void exitWhenKeyboardCollapse(final EditText dialogInput) {
        largestInputLocationY = 0;
        final long startT = System.currentTimeMillis();
        final long shownDelay = 300;
        dialogInput.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int[] location = new int[2];
                dialogInput.getLocationInWindow(location);
                int locationY = location[1];
                Logger.i(TAG, "onGlobalLayout: locationY=" + locationY);

                largestInputLocationY = Math.max(largestInputLocationY, locationY);

                if (System.currentTimeMillis() - startT > shownDelay && largestInputLocationY - locationY > MINI_KEYBOARD_ALTER && locationY > 0
                        && !KeyboardUtil.isKeyboardShown(dialogInput)) {
                    dismissAndRelease();
                    dialogInput.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });
    }

    private void hideKeyboardAndDismissDialog(EditText dialogInput) {
        KeyboardUtil.hideKeyboard((Activity) getContext(), dialogInput);
        dismissAndRelease();
    }

    private void dismissAndRelease() {
        if (dialog != null) {
            DialogUtil.dismiss(dialog);
            applySoftInputMode(dialog.getWindow(), WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED);
            dialog = null;
        }
    }

    private void clearFlags(Window window, int flags) {
        if (window != null) {
            window.clearFlags(flags);
        }
    }

    private void applySoftInputMode(Window window, int mode) {
        if (window != null) {
            window.setSoftInputMode(mode);
        }
    }

    private static Dialog createDialog(Context context) {
        // 自定义dialog显示布局
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(context).inflate(R.layout.iub_dialog_input, null);
        // 自定义dialog显示风格
        Dialog dialog = new Dialog(context, R.style.Dialog4Input);
        // 弹窗点击周围空白处弹出层自动消失弹窗消失(false时为点击周围空白处弹出层不自动消失)
        dialog.setCanceledOnTouchOutside(true);
        // 将布局设置给Dialog
        dialog.setContentView(view);
        // 获取当前Activity所在的窗体
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.BOTTOM;
        wlp.width = LayoutParams.MATCH_PARENT;
        wlp.height = LayoutParams.MATCH_PARENT;
        window.setAttributes(wlp);
        return dialog;
    }

    @Override
    public void destroy() {
        DialogUtil.dismiss(dialog);
        dialog = null;
    }
}
