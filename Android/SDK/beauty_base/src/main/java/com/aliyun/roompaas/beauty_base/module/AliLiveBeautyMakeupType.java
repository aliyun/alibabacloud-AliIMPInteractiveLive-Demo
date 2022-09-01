package com.aliyun.roompaas.beauty_base.module;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 美妆参数
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({AliLiveBeautyMakeupType.kAliLiveMakeupWhole,
        AliLiveBeautyMakeupType.kAliLiveMakeupHighlight,
        AliLiveBeautyMakeupType.kAliLiveMakeupEyeball,
        AliLiveBeautyMakeupType.kAliLiveMakeupMouth,
        AliLiveBeautyMakeupType.kAliLiveMakeupEyeBrow,
        AliLiveBeautyMakeupType.kAliLiveMakeupBlush,
        AliLiveBeautyMakeupType.kAliLiveMakeupMax,
})
public @interface AliLiveBeautyMakeupType {
    /**
     * 整妆
     */
    int kAliLiveMakeupWhole = 0;

    /**
     * 高光
     */
    int kAliLiveMakeupHighlight = 1;

    /**
     * 美瞳
     */
    int kAliLiveMakeupEyeball = 2;

    /**
     * 口红
     */
    int kAliLiveMakeupMouth = 3;

    /**
     * 眼妆
     */
    int kAliLiveMakeupEyeBrow = 5;

    /**
     * 腮红
     */
    int kAliLiveMakeupBlush = 6;

    /**
     * 最大值
     */
    int kAliLiveMakeupMax = 7;
}
