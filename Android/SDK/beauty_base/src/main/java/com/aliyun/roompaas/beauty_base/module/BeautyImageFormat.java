package com.aliyun.roompaas.beauty_base.module;


import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@IntDef({BeautyImageFormat.kDefault, BeautyImageFormat.kRGB, BeautyImageFormat.kNV21, BeautyImageFormat.kRGBA})
public @interface BeautyImageFormat {
    int kDefault = -1;
    int kRGB = 0;
    int kNV21 = 1;
    int kRGBA = 2;
}
