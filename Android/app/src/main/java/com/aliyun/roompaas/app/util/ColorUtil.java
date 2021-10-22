package com.aliyun.roompaas.app.util;

import android.graphics.Color;

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
}
