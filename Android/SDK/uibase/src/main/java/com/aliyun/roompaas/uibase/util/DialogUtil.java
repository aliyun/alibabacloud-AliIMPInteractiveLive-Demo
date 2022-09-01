package com.aliyun.roompaas.uibase.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.aliyun.roompaas.base.util.Utils;
import com.aliyun.roompaas.uibase.R;

/**
 * @author puke
 * @version 2021/5/17
 */
public class DialogUtil {
    private static int selectedIndex = -1;

    public interface InputCallback {
        void onInput(String value);
    }

    public static class Action {
        public String text;
        public Runnable runnable;
        public boolean checked;

        public Action(String text, Runnable runnable) {
            this(text, runnable, false);
        }

        public Action(String text, Runnable runnable, boolean checked) {
            this.text = text;
            this.runnable = runnable;
            this.checked = checked;
        }
    }

    public static Dialog doAction(Context context, String title, final Action... actions) {
        return showCustomDialog(context, title, null, null, true, null, null, actions);
    }

    public static Dialog doActionWithInput(Context context, String title
            , @Nullable CharSequence inputHint
            , @Nullable CharSequence inputDefaultText
            , @Nullable InputCallback inputCallback
            , final Action... actions) {
        return showCustomDialog(context, title, null, null, true, inputHint, inputDefaultText, inputCallback, actions);
    }

    public static Dialog input(Context context, String title, String defaultValue, final InputCallback inputCallback) {
        return showCustomDialog(context, title, null, null, false, defaultValue, inputCallback, null);
    }

    public static Dialog confirm(Context context, String message, final Runnable runnable) {
        return confirm(context, message, runnable, null);
    }

    public static Dialog confirm(Context context, String message,
                                 final Runnable confirmListener, final Runnable cancelListener) {
        return showCustomDialog(context, message, confirmListener, cancelListener);
    }

    public static Dialog tips(Context context, String message, final Runnable runnable) {
        return showCustomDialog(context, message, runnable, null, true);
    }

    public static Dialog showCustomDialog(Context context, CharSequence title
            , @Nullable Runnable confirmAction
            , @Nullable Runnable cancelAction) {
        return showCustomDialog(context, title, confirmAction, cancelAction, false);
    }

    public static Dialog showCustomDialog(Context context, CharSequence title
            , @Nullable Runnable confirmAction
            , @Nullable Runnable cancelAction, boolean singleConfirm) {
        return showCustomDialog(context, title, new Pair<CharSequence, Runnable>(null, confirmAction),
                cancelAction == null ? null : new Pair<CharSequence, Runnable>(null, cancelAction), singleConfirm);
    }

    public static Dialog showCustomDialog(Context context, CharSequence title
            , @Nullable Pair<CharSequence, Runnable> confirmPair
            , @Nullable Pair<CharSequence, Runnable> cancelPair) {
        return showCustomDialog(context, title, confirmPair, cancelPair, false);
    }

    public static Dialog showCustomDialog(Context context, CharSequence title
            , @Nullable Pair<CharSequence, Runnable> confirmPair
            , @Nullable Pair<CharSequence, Runnable> cancelPair, boolean singleConfirm) {
        return showCustomDialog(context, title, confirmPair, cancelPair, singleConfirm, null, null, null);
    }

    public static Dialog showCustomDialog(Context context, CharSequence title
            , @Nullable Pair<CharSequence, Runnable> confirmPair
            , @Nullable Pair<CharSequence, Runnable> cancelPair, boolean singleConfirm
            , @Nullable CharSequence inputDefaultText, @Nullable InputCallback inputCallback
            , @Nullable final Action... actions) {
        return showCustomDialog(context, title, confirmPair, cancelPair, singleConfirm, null, inputDefaultText, inputCallback, actions);
    }

    @SuppressWarnings("all")
    public static Dialog showCustomDialog(Context context, CharSequence title
            , @Nullable Pair<CharSequence, Runnable> confirmPair
            , @Nullable Pair<CharSequence, Runnable> cancelPair, boolean singleConfirm
            , @Nullable CharSequence inputHint, @Nullable CharSequence inputDefaultText, @Nullable InputCallback inputCallback
            , @Nullable final Action... actions) {
        if (context == null || Utils.isContextActivityAndInvalid(context)) {
            return null;
        }
        LayoutInflater factory = LayoutInflater.from(context);
        final View view = factory.inflate(R.layout.iub_custom_dialog, null);
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        dialog.setView(view);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        ViewUtil.applyText(view.findViewById(R.id.title), title);
        TextView confirm = view.findViewById(R.id.confirm);
        TextView cancel = view.findViewById(R.id.cancel);

        ViewUtil.setGone(singleConfirm, cancel);

        Pair<CharSequence, Runnable> optionConfirmPair = parseConfirmPairForOptionsDialog(confirmPair, view, actions);
        if (optionConfirmPair != null) {
            confirmPair = optionConfirmPair;
        }

        Pair<CharSequence, Runnable>[] inputPairArray = parseInputPairArray(confirmPair, context, view, dialog, inputHint, inputDefaultText, inputCallback);
        if (Utils.nonNull(inputPairArray)) {
            confirmPair = inputPairArray[0];
            cancelPair = inputPairArray[1];
        }

        bindTextAndAction(confirmPair, cancelPair, dialog, confirm, cancel);
        windowProcess(context, dialog);
        dialog.show();
        return dialog;
    }

    private static void bindTextAndAction(@Nullable final Pair<CharSequence, Runnable> confirmPair,
                                          @Nullable final Pair<CharSequence, Runnable> cancelPair,
                                          final AlertDialog dialog, TextView confirm, TextView cancel) {
        ViewUtil.applyText(confirm, confirmPair != null ? confirmPair.first : null);
        ViewUtil.applyText(cancel, cancelPair != null ? cancelPair.first : null);

        ViewUtil.bindClickActionWithClickCheck(confirm, new Runnable() {
            @Override
            public void run() {
                Utils.run(confirmPair != null ? confirmPair.second : null);
                dialog.dismiss();
            }
        });

        ViewUtil.bindClickActionWithClickCheck(cancel, new Runnable() {
            @Override
            public void run() {
                Utils.run(cancelPair != null ? cancelPair.second : null);
                dialog.dismiss();
            }
        });
    }

    private static Pair<CharSequence, Runnable>[] parseInputPairArray(
            @Nullable Pair<CharSequence, Runnable> confirmPair,
            Context context, View view, Dialog dialog
            , @Nullable CharSequence inputHint, @Nullable CharSequence inputDefaultText, @Nullable final InputCallback inputCallback) {
        if (inputCallback == null || context == null) {
            return null;
        }

        Pair<CharSequence, Runnable>[] result = new Pair[2];
        // input process
        final Activity activity = context instanceof Activity ? (Activity) context : null;
        final EditText input = view.findViewById(R.id.input);
        ViewUtil.setVisible(input);
        ViewUtil.applyText(input, inputDefaultText);

        TextView inputHintTV = view.findViewById(R.id.inputHint);
        ViewUtil.setVisible(!TextUtils.isEmpty(inputHint), inputHintTV);
        ViewUtil.applyText(inputHintTV, inputHint);

        final Runnable existingConfirmTask = confirmPair != null ? confirmPair.second : null;
        result[0] = new Pair<CharSequence, Runnable>("确定", new Runnable() {
            @Override
            public void run() {
                Utils.run(existingConfirmTask);
                String value = input.getText().toString().trim();
                inputCallback.onInput(value);
                KeyboardUtil.hideKeyboard(activity, input);
            }
        });
        result[1] = new Pair<CharSequence, Runnable>("取消", new Runnable() {
            @Override
            public void run() {
                KeyboardUtil.hideKeyboard(activity, input);
            }
        });

        // mute cancel on touch outside, omit the hide keyboard process
        dialog.setCanceledOnTouchOutside(false);

        bringUpKeyboardAndMoveCursorToEnd(activity, input);
        return result;
    }

    private static Pair<CharSequence, Runnable> parseConfirmPairForOptionsDialog(
            @Nullable Pair<CharSequence, Runnable> confirmPair, View view, @NonNull final Action[] actions) {
        if (Utils.isNotEmpty(actions)) {
            RadioGroup optionGroup = view.findViewById(R.id.optionGroup);
            RadioButton[] options = new RadioButton[]{
                    view.findViewById(R.id.option0),
                    view.findViewById(R.id.option1),
                    view.findViewById(R.id.option2),
                    view.findViewById(R.id.option3),
                    view.findViewById(R.id.option4),
            };

            ViewUtil.setVisible(optionGroup);
            for (int i = 0, len = actions.length; i < len; i++) {
                if (actions[i] != null && actions[i].checked) {
                    options[i].setChecked(true);
                }
                ViewUtil.setVisible(options[i]);
                ViewUtil.applyText(options[i], actions[i].text);
            }

            selectedIndex = -1;
            optionGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    View radioButton = group.findViewById(checkedId);
                    selectedIndex = group.indexOfChild(radioButton);
                }
            });
            final Runnable existingConfirmTask = confirmPair != null ? confirmPair.second : null;
            confirmPair = new Pair<CharSequence, Runnable>("确定", new Runnable() {
                @Override
                public void run() {
                    Utils.run(existingConfirmTask);
                    if (selectedIndex == -1) {
                        return;
                    }
                    if (actions[selectedIndex] != null) {
                        Utils.run(actions[selectedIndex].runnable);
                    }
                }
            });
        }
        return confirmPair;
    }

    private static void windowProcess(Context context, AlertDialog dialog) {
        Window window = dialog.getWindow();
        if (window != null) {
            ColorDrawable back = new ColorDrawable(Color.TRANSPARENT);
            InsetDrawable inset = new InsetDrawable(back, 20/*margin*/);
            window.setBackgroundDrawable(inset);

            boolean immersive = context instanceof Activity && AppUtil.isActivityImmersive((Activity) context);
            if (immersive) {
                AppUtil.intoImmersive(window);
            }
        }
    }

    private static void bringUpKeyboardAndMoveCursorToEnd(@Nullable Activity activity, @NonNull EditText input) {
        if (activity == null) {
            return;
        }
        KeyboardUtil.showUpSoftKeyboard(input, activity, true);
        ViewUtil.bringCursorToEnd(input);
    }

    @SuppressWarnings("SameParameterValue")
    public static Dialog createDialogOfNoAnim(Context context, int width, int height, int layoutRes) {
        return createDialogOfSlider(context, width, height, Gravity.CENTER, R.style.DialogNoAnim, layoutRes);
    }

    @SuppressWarnings("SameParameterValue")
    public static Dialog createDialogOfRight(Context context, int width, int layoutRes) {
        int height = WindowManager.LayoutParams.MATCH_PARENT;
        return createDialogOfSlider(context, width, height, Gravity.END,
                R.style.DialogFromRight, layoutRes);
    }

    public static Dialog createDialogOfBottom(Context context, int height, int layoutRes) {
        return createDialogOfBottom(context, height, layoutRes, false);
    }

    public static Dialog createDialogOfBottom(Context context, int height, int layoutRes,
                                              boolean transparentBg) {
        int themeResId = transparentBg ?
                R.style.DialogFromBottomWithTransparentBg : R.style.DialogFromBottom;
        int width = WindowManager.LayoutParams.MATCH_PARENT;
        Dialog dialog = createDialogOfSlider(context, width, height, Gravity.BOTTOM,
                themeResId, layoutRes);
        if (transparentBg) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        return dialog;
    }

    private static Dialog createDialogOfSlider(Context context, int width, int height, int gravity,
                                               int themeResId, int layoutRes) {
        // 自定义dialog显示布局
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(context).inflate(layoutRes, null);
        // 自定义dialog显示风格
        Dialog dialog = new Dialog(context, themeResId);
        // 弹窗点击周围空白处弹出层自动消失弹窗消失(false时为点击周围空白处弹出层不自动消失)
        dialog.setCanceledOnTouchOutside(true);
        // 将布局设置给Dialog
        dialog.setContentView(view);
        // 获取当前Activity所在的窗体
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = gravity;
        wlp.width = width;
        wlp.height = height;
        window.setAttributes(wlp);
        return dialog;
    }

    public static boolean dismiss(@Nullable Dialog dialog) {
        if (isShowing(dialog)) {
            dialog.dismiss();
            return true;
        }
        return false;
    }

    public static boolean isShowing(@Nullable Dialog dialog) {
        return dialog != null && dialog.isShowing();
    }
}
