package com.aliyun.roompaas.app.util;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.aliyun.roompaas.app.R;
import com.aliyun.roompaas.base.util.Utils;
import com.aliyun.roompaas.uibase.util.ViewUtil;

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

    public static void doAction(Context context, String title, final Action... actions) {
        showCustomDialog(context, title, null, null, true, null, null, actions);
    }

    public static void doActionWithInput(Context context, String title
            , @Nullable CharSequence inputHint
            , @Nullable CharSequence inputDefaultText
            , @Nullable InputCallback inputCallback
            , final Action... actions) {
        showCustomDialog(context, title, null, null, true, inputHint, inputDefaultText, inputCallback, actions);
    }

    public static void input(Context context, String title, String defaultValue, final InputCallback inputCallback) {
        showCustomDialog(context, title, null, null, false,
                defaultValue, inputCallback);
    }

    public static void confirm(Context context, String message, final Runnable runnable) {
        confirm(context, message, runnable, null);
    }

    public static void confirm(Context context, String message,
                               final Runnable confirmListener, final Runnable cancelListener) {
        showCustomDialog(context, message, confirmListener, cancelListener);
    }

    public static void tips(Context context, String message, final Runnable runnable) {
        showCustomDialog(context, message, runnable, null, true);
    }

    public static void showCustomDialog(Context context, CharSequence title
            , @Nullable Runnable confirmAction
            , @Nullable Runnable cancelAction) {
        showCustomDialog(context, title, confirmAction, cancelAction, false);
    }

    public static void showCustomDialog(Context context, CharSequence title
            , @Nullable Runnable confirmAction
            , @Nullable Runnable cancelAction, boolean singleConfirm) {
        showCustomDialog(context, title, new Pair<>(null, confirmAction),
                cancelAction == null ? null : new Pair<>(null, cancelAction), singleConfirm);
    }

    public static void showCustomDialog(Context context, CharSequence title
            , @Nullable Pair<CharSequence, Runnable> confirmPair
            , @Nullable Pair<CharSequence, Runnable> cancelPair) {
        showCustomDialog(context, title, confirmPair, cancelPair, false);
    }

    public static void showCustomDialog(Context context, CharSequence title
            , @Nullable Pair<CharSequence, Runnable> confirmPair
            , @Nullable Pair<CharSequence, Runnable> cancelPair, boolean singleConfirm) {
        showCustomDialog(context, title, confirmPair, cancelPair, singleConfirm, null, null, null);
    }

    public static void showCustomDialog(Context context, CharSequence title
            , @Nullable Pair<CharSequence, Runnable> confirmPair
            , @Nullable Pair<CharSequence, Runnable> cancelPair, boolean singleConfirm
            , @Nullable CharSequence inputDefaultText, @Nullable InputCallback inputCallback) {
        showCustomDialog(context, title, confirmPair, cancelPair, singleConfirm, inputDefaultText, inputCallback, null);
    }

    public static void showCustomDialog(Context context, CharSequence title
            , @Nullable Pair<CharSequence, Runnable> confirmPair
            , @Nullable Pair<CharSequence, Runnable> cancelPair, boolean singleConfirm
            , @Nullable CharSequence inputDefaultText, @Nullable InputCallback inputCallback
            , @Nullable final Action... actions) {
        showCustomDialog(context, title, confirmPair, cancelPair, singleConfirm, null, inputDefaultText, inputCallback, actions);
    }

    @SuppressWarnings("all")
    public static void showCustomDialog(Context context, CharSequence title
            , @Nullable Pair<CharSequence, Runnable> confirmPair
            , @Nullable Pair<CharSequence, Runnable> cancelPair, boolean singleConfirm
            , @Nullable CharSequence inputHint, @Nullable CharSequence inputDefaultText, @Nullable InputCallback inputCallback
            , @Nullable final Action... actions) {
        if (context == null) {
            return;
        }
        LayoutInflater factory = LayoutInflater.from(context);
        final View view = factory.inflate(R.layout.custom_dialog, null);
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        dialog.setView(view);
        dialog.setCanceledOnTouchOutside(true);

        ViewUtil.applyText(view.findViewById(R.id.title), title);
        TextView confirm = view.findViewById(R.id.confirm);
        TextView cancel = view.findViewById(R.id.cancel);

        ViewUtil.setGone(singleConfirm, cancel);

        Pair<CharSequence, Runnable> optionConfirmPair = parseConfirmPairForOptionsDialog(confirmPair, view, actions);
        if (optionConfirmPair != null) {
            confirmPair = optionConfirmPair;
        }

        Pair<CharSequence, Runnable>[] inputPairArray = parseInputPairArray(confirmPair, context, view, dialog, inputHint, inputDefaultText, inputCallback);
        if (inputPairArray != null && !Utils.anyNull(inputPairArray)) {
            confirmPair = inputPairArray[0];
            cancelPair = inputPairArray[1];
        }

        bindTextAndAction(confirmPair, cancelPair, dialog, confirm, cancel);
        marginProcess(dialog);
        dialog.show();
    }

    private static void bindTextAndAction(@Nullable Pair<CharSequence, Runnable> confirmPair,
                                          @Nullable Pair<CharSequence, Runnable> cancelPair,
                                          AlertDialog dialog, TextView confirm, TextView cancel) {
        ViewUtil.applyText(confirm, confirmPair != null ? confirmPair.first : null);
        ViewUtil.applyText(cancel, cancelPair != null ? cancelPair.first : null);

        ViewUtil.bindClickActionWithClickCheck(confirm, () -> {
            Utils.run(confirmPair != null ? confirmPair.second : null);
            dialog.dismiss();
        });

        ViewUtil.bindClickActionWithClickCheck(cancel, () -> {
            Utils.run(cancelPair != null ? cancelPair.second : null);
            dialog.dismiss();
        });
    }

    private static Pair<CharSequence, Runnable>[] parseInputPairArray(
            @Nullable Pair<CharSequence, Runnable> confirmPair,
            Context context, View view, Dialog dialog
            , @Nullable CharSequence inputHint, @Nullable CharSequence inputDefaultText, @Nullable InputCallback inputCallback) {
        if (inputCallback == null || context == null) {
            return null;
        }

        Pair<CharSequence, Runnable>[] result = new Pair[2];
        // input process
        Activity activity = context instanceof Activity ? (Activity) context : null;
        EditText input = view.findViewById(R.id.input);
        ViewUtil.setVisible(input);
        ViewUtil.applyText(input, inputDefaultText);

        TextView inputHintTV = view.findViewById(R.id.inputHint);
        ViewUtil.setVisible(!TextUtils.isEmpty(inputHint), inputHintTV);
        ViewUtil.applyText(inputHintTV, inputHint);

        final Runnable existingConfirmTask = confirmPair != null ? confirmPair.second : null;
        result[0] = new Pair<>("??????", () -> {
            Utils.run(existingConfirmTask);
            String value = input.getText().toString().trim();
            inputCallback.onInput(value);
            KeyboardUtil.hideKeyboard(activity, input);
        });
        result[1] = new Pair<>("??????", () -> KeyboardUtil.hideKeyboard(activity, input));

        // mute cancel on touch outside, omit the hide keyboard process
        dialog.setCanceledOnTouchOutside(false);

        bringUpKeyboardAndMoveCursorToEnd(activity, input);
        return result;
    }

    private static Pair<CharSequence, Runnable> parseConfirmPairForOptionsDialog(
            @Nullable Pair<CharSequence, Runnable> confirmPair, View view, @NonNull Action[] actions) {
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
            optionGroup.setOnCheckedChangeListener((group, checkedId) -> {
                View radioButton = group.findViewById(checkedId);
                selectedIndex = group.indexOfChild(radioButton);
            });
            final Runnable existingConfirmTask = confirmPair != null ? confirmPair.second : null;
            confirmPair = new Pair<>("??????", () -> {
                Utils.run(existingConfirmTask);
                if (selectedIndex == -1) {
                    return;
                }
                if (actions[selectedIndex] != null) {
                    Utils.run(actions[selectedIndex].runnable);
                }
            });
        }
        return confirmPair;
    }

    private static void marginProcess(AlertDialog dialog) {
        Window window = dialog.getWindow();
        if (window != null) {
            ColorDrawable back = new ColorDrawable(Color.TRANSPARENT);
            InsetDrawable inset = new InsetDrawable(back, 20/*margin*/);
            window.setBackgroundDrawable(inset);
        }
    }

    private static void bringUpKeyboardAndMoveCursorToEnd(@Nullable Activity activity, @NonNull EditText input) {
        if (activity == null) {
            return;
        }
        KeyboardUtil.showUpSoftKeyboard(input, activity, true);
        input.setSelection(input.getText().length());
    }
}
