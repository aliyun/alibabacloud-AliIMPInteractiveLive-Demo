package com.aliyun.roompaas.beauty_base.module;


import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 美妆混合模式
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({AliLiveBeautyBlendType.kAliLiveBlendNormal,
        AliLiveBeautyBlendType.kAliLiveBlendLighten,
        AliLiveBeautyBlendType.kAliLiveBlendDarken,
        AliLiveBeautyBlendType.kAliLiveBlendMultiply,
        AliLiveBeautyBlendType.kAliLiveBlendDivide,
        AliLiveBeautyBlendType.kAliLiveBlendAverage,
        AliLiveBeautyBlendType.kAliLiveBlendAdd,
        AliLiveBeautyBlendType.kAliLiveBlendSubtract,
        AliLiveBeautyBlendType.kAliLiveBlendDifference,
        AliLiveBeautyBlendType.kAliLiveBlendNegation,
        AliLiveBeautyBlendType.kAliLiveBlendExclusion,
        AliLiveBeautyBlendType.kAliLiveBlendScreen,
        AliLiveBeautyBlendType.kAliLiveBlendOverlay,
        AliLiveBeautyBlendType.kAliLiveBlendSoftLight,
        AliLiveBeautyBlendType.kAliLiveBlendHardLight,
        AliLiveBeautyBlendType.kAliLiveBlendColorDodge,
        AliLiveBeautyBlendType.kAliLiveBlendColorBurn,
        AliLiveBeautyBlendType.kAliLiveBlendLinearDodge,
        AliLiveBeautyBlendType.kAliLiveBlendLinearBurn,
        AliLiveBeautyBlendType.kAliLiveBlendLinearLight,
        AliLiveBeautyBlendType.kAliLiveBlendVividLight,
        AliLiveBeautyBlendType.kAliLiveBlendPinLight,
        AliLiveBeautyBlendType.kAliLiveBlendHardMix,
        AliLiveBeautyBlendType.kAliLiveBlendReflect,
        AliLiveBeautyBlendType.kAliLiveBlendGlow,
        AliLiveBeautyBlendType.kAliLiveBlendPhoenix,
        AliLiveBeautyBlendType.kAliLiveBlendHue,
        AliLiveBeautyBlendType.kAliLiveBlendSaturation,
        AliLiveBeautyBlendType.kAliLiveBlendLuminosity,
        AliLiveBeautyBlendType.kAliLiveBlendColor,
        AliLiveBeautyBlendType.kAliLiveBlendMax,
})
public @interface AliLiveBeautyBlendType {
    /**
     * 正常
     */
    int kAliLiveBlendNormal = 0;

    /**
     * 变亮
     */
    int kAliLiveBlendLighten = 1;

    /**
     * 变暗
     */
    int kAliLiveBlendDarken = 2;

    /**
     * 正片叠底
     */
    int kAliLiveBlendMultiply = 3;

    /**
     * 划分
     */
    int kAliLiveBlendDivide = 4;

    /**
     * 平均
     */
    int kAliLiveBlendAverage = 5;

    /**
     * 线性减淡
     */
    int kAliLiveBlendAdd = 6;

    /**
     * 减去
     */
    int kAliLiveBlendSubtract = 7;

    /**
     * 差值
     */
    int kAliLiveBlendDifference = 8;

    /**
     * 镜像
     */
    int kAliLiveBlendNegation = 9;

    /**
     * 排除
     */
    int kAliLiveBlendExclusion = 10;

    /**
     * 滤色
     */
    int kAliLiveBlendScreen = 11;

    /**
     * 叠加
     */
    int kAliLiveBlendOverlay = 12;

    /**
     * 柔光
     */
    int kAliLiveBlendSoftLight = 13;

    /**
     * 强光
     */
    int kAliLiveBlendHardLight = 14;

    /**
     * 颜色减淡
     */
    int kAliLiveBlendColorDodge = 15;

    /**
     * 颜色加深
     */
    int kAliLiveBlendColorBurn = 16;

    /**
     * 线性减淡
     */
    int kAliLiveBlendLinearDodge = 17;

    /**
     * 线性加深
     */
    int kAliLiveBlendLinearBurn = 18;

    /**
     * 线性光
     */
    int kAliLiveBlendLinearLight = 19;

    /**
     * 亮光
     */
    int kAliLiveBlendVividLight = 20;

    /**
     * 点光
     */
    int kAliLiveBlendPinLight = 21;

    /**
     * 实色混合
     */
    int kAliLiveBlendHardMix = 22;

    /**
     * 反射
     */
    int kAliLiveBlendReflect = 23;

    /**
     * 发光
     */
    int kAliLiveBlendGlow = 24;

    /**
     * 凤凰
     */
    int kAliLiveBlendPhoenix = 25;

    /**
     * 色相
     */
    int kAliLiveBlendHue = 26;

    /**
     * 饱和度
     */
    int kAliLiveBlendSaturation = 27;

    /**
     * 明亮
     */
    int kAliLiveBlendLuminosity = 28;

    /**
     * 颜色
     */
    int kAliLiveBlendColor = 29;

    /**
     * 最大值
     */
    int kAliLiveBlendMax = 999;
}
