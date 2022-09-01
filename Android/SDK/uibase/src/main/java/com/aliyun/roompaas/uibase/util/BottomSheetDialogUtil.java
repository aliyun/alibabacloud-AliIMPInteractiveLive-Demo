package com.aliyun.roompaas.uibase.util;

import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.BottomSheetDialog;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

/**
 * 底部弹窗
 *
 * @author puke
 * @version 2022/5/12
 */
public class BottomSheetDialogUtil {

    public static BottomSheetDialog create(Context context, int layout) {
        return create(context, layout, true);
    }

    public static BottomSheetDialog create(Context context, int layout, boolean removeBackground) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);

        View view = View.inflate(context, layout, null);
        dialog.setContentView(view);

        if (removeBackground) {
            // 去除Dialog默认白色
            ViewParent parent = view.getParent();
            if (parent instanceof ViewGroup) {
                ((ViewGroup) parent).setBackgroundColor(Color.TRANSPARENT);
            }
        }

        return dialog;
    }
}
