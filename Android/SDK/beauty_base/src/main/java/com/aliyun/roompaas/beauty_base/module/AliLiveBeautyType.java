package com.aliyun.roompaas.beauty_base.module;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * 美颜滤镜类型
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({AliLiveBeautyType.kAliLiveSkinBuffing,
        AliLiveBeautyType.kAliLiveFaceBuffing,
        AliLiveBeautyType.kAliLiveMakeup,
        AliLiveBeautyType.kAliLiveFaceShape,
        AliLiveBeautyType.kAliLiveSkinWhiting,
        AliLiveBeautyType.kAliLiveHSV,
        AliLiveBeautyType.kAliLiveLUT,
})
public @interface AliLiveBeautyType {
    /**
     * 磨皮、锐化
     */
    int kAliLiveSkinBuffing = 0;

    /**
     * 脸部磨皮（去眼袋、法令纹）
     */
    int kAliLiveFaceBuffing = 1;

    /**
     * 美妆
     */
    int kAliLiveMakeup = 2;

    /**
     * 美型
     */
    int kAliLiveFaceShape = 3;

    /**
     * 美白
     */
    int kAliLiveSkinWhiting = 4;

    /**
     * 色相饱和度明度
     */
    int kAliLiveHSV = 5;

    /**
     * 滤镜
     */
    int kAliLiveLUT = 6;
}
