package com.aliyun.roompaas.app.activity.classroom.panel;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

/**
 * @author puke
 * @version 2021/5/25
 */
public abstract class BasePanelView extends FrameLayout {

    public BasePanelView(@NonNull Context context) {
        super(context);

        addTextProcess(context);
    }

    private void addTextProcess(@NotNull Context context) {
        String text = getText();
        if (TextUtils.isEmpty(text)) {
            return;
        }

        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setTextColor(Color.RED);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);

        LayoutParams layoutParams = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.gravity = Gravity.CENTER;
        addView(textView, layoutParams);
    }

    @NotNull
    protected abstract String getText();
}
