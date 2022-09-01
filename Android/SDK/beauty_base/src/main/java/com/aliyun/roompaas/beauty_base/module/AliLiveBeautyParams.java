package com.aliyun.roompaas.beauty_base.module;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 美颜参数
 * 值为float，除基础美颜外需要先将功能打开，对应参数才有效
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({AliLiveBeautyParams.kAliLiveParamsBPSkinBuffing,
        AliLiveBeautyParams.kAliLiveParamsBPSkinSharpen,
        AliLiveBeautyParams.kAliLiveParamsBPSkinWhitening,
        AliLiveBeautyParams.kAliLiveParamsBPPouch,
        AliLiveBeautyParams.kAliLiveParamsBPNasolabialFolds,
        AliLiveBeautyParams.kAliLiveParamsBPLUT,
        AliLiveBeautyParams.kAliLiveParamsBPWhiteTeeth,
})
public @interface AliLiveBeautyParams {
    ////////////// 基础美颜选项 //////////////
    ///////////// 需要开启{AliLiveBeautyType kAliLiveSkinBuffing}和{AliLiveBeautyType kAliLiveSkinWhiting} //////////////
    /**
     * 磨皮，值的范围[0, 1]，默认0.6
     */
    int kAliLiveParamsBPSkinBuffing = 1;

    /**
     * 锐化，值的范围[0, 1]，默认0.8
     */
    int kAliLiveParamsBPSkinSharpen = 2;

    /**
     * 美白，值的范围[0, 1]，默认0.6
     */
    int kAliLiveParamsBPSkinWhitening = 3;

    ////////////// 脸部美颜参数选项 //////////////
    ////////////// 需要开启{AliLiveBeautyType kAliLiveFaceBuffing} //////////////
    /**
     * 去眼袋，值的范围[0, 1]
     */
    int kAliLiveParamsBPPouch = 4;

    /**
     * 去法令纹，值的范围[0, 1]
     */
    int kAliLiveParamsBPNasolabialFolds = 5;

    ////////////// 滤镜参数选项 //////////////
    ////////////// 需要开启{AliLiveBeautyType kAliLiveFaceBuffing} //////////////
    /**
     * 色卡滤镜强度，值的范围[0, 1]
     */
    int kAliLiveParamsBPLUT = 6;

    /**
     * 牙齿美白
     */
    int kAliLiveParamsBPWhiteTeeth = 7;
}
