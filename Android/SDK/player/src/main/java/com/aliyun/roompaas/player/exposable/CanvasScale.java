package com.aliyun.roompaas.player.exposable;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by KyleCe on 2021/7/6
 */
public class CanvasScale {
    @Retention(RetentionPolicy.RUNTIME)
    @IntDef({Mode.SCALE_FILL, Mode.ASPECT_FIT, Mode.ASPECT_FILL})
    public @interface Mode {
        int SCALE_FILL = 0;
        int ASPECT_FIT = 1;
        int ASPECT_FILL = 2;
    }
}