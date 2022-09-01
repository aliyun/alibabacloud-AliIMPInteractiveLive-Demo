package com.aliyun.roompaas.uibase.util;

import android.graphics.Color;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.Random;

/**
 * @author puke
 * @version 2021/5/21
 */
public class ColorUtil {

    private static Random random = new Random();

    public static int randomColor() {
        return Color.rgb(
                random.nextInt(255),
                random.nextInt(255),
                random.nextInt(255)
        );
    }

    @Nullable
    public static Integer parseColor(String colorStr) {
        if (TextUtils.isEmpty(colorStr)) {
            return null;
        }
        try {
            return Color.parseColor(colorStr);
        } catch (Throwable ignore) {
            return null;
        }
    }
}
