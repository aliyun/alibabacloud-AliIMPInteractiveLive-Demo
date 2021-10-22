package com.aliyun.roompaas.app.activity.classroom.panel;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;


/**
 * @author puke
 * @version 2021/5/25
 */
public abstract class BasePanelView extends FrameLayout {

    public BasePanelView(@NonNull Context context) {
        super(context);

        addTextProcess(context);
    }

    private void addTextProcess(@NonNull Context context) {
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

    @NonNull
    protected abstract String getText();
}
