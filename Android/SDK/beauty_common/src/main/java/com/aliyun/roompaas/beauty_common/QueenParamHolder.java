package com.aliyun.roompaas.beauty_common;

import android.text.TextUtils;

import com.taobao.android.libqueen.QueenEngine;
import com.taobao.android.libqueen.models.AlgType;
import com.taobao.android.libqueen.models.BeautyFilterType;
import com.taobao.android.libqueen.models.BeautyParams;
import com.taobao.android.libqueen.models.BlendType;
import com.taobao.android.libqueen.models.FaceShapeType;
import com.taobao.android.libqueen.models.MakeupType;

import java.util.Iterator;
import java.util.List;

public class QueenParamHolder {
    /**
     * 参数加权配置
     */
    private interface QueenParamWeight {
        float FACE_SHAPE_PARAM = 1.0f;
        float FACE_MAKEUP_ALPHA = 1.0f;
        float FACE_LUT_PARAM = 1.0f;
    }

    private static QueenParam sQueenParam;

    public static QueenParam getQueenParam() {
        if (null == sQueenParam) {
            sQueenParam = QueenParamFactory.getDefaultScenesParam();
        }
        return sQueenParam;
    }

    public static void setQueenParam(QueenParam queenParam) {
        sQueenParam = queenParam;
    }

    public static void writeParamToEngine(QueenEngine queenEngine, boolean canUseAsyncAlg) {
//        Log.e("lzx_queen", "writeParamToEngine : " + getQueenParam());
        if (null != queenEngine) {
            queenEngine.enableFacePointDebug(QueenRuntime.sFaceDetectDebug);

            queenEngine.setGreenScreen(
                    getQueenParam().segmentRecord.enableGreenSegment || getQueenParam().segmentRecord.enableBlueSegment  ? getQueenParam().segmentRecord.greenSegmentBackgroundPath : "",
                    getQueenParam().segmentRecord.enableBlueSegment,
                    getQueenParam().segmentRecord.enableBlueSegment ? getQueenParam().segmentRecord.blueSegmentThreshold : getQueenParam().segmentRecord.greenSegmentThreshold,
                    getQueenParam().segmentRecord.enableBlueSegment ? getQueenParam().segmentRecord.enableBlueSegmentAutoThreshold : getQueenParam().segmentRecord.enableGreenSegmentAutoThreshold
            );

            // AI抠图背景虚化
            queenEngine.enableBeautyType(BeautyFilterType.kBTBackgroundProcess, getQueenParam().segmentRecord.enableAiSegment);
            queenEngine.setAISegmentForegroundPadding(getQueenParam().segmentRecord.aiSegmentForegroundPadding);

            queenEngine.setAlgAsych(AlgType.kBokehAiSegment, canUseAsyncAlg && getQueenParam().segmentRecord.aiSegmentAsync);

            // AI抠图背景资源设置
            addMaterial(queenEngine, true, getQueenParam().segmentRecord.aiSegmentBackgroundPath,
                    getQueenParam().segmentRecord.usingAiSegmentBackgroundPathList);

            // 美颜
            queenEngine.enableBeautyType(BeautyFilterType.kSkinWhiting, getQueenParam().basicBeautyRecord.enableSkinWhiting);//美白开关
            queenEngine.setBeautyParam(BeautyParams.kBPSkinWhitening, getQueenParam().basicBeautyRecord.skinWhitingParam);  //美白 [0,1]
            queenEngine.setBeautyParam(BeautyParams.kBPSkinRed,
                    getQueenParam().basicBeautyRecord.enableSkinRed ? getQueenParam().basicBeautyRecord.skinRedParam : 0.0f);  //红润 [0,1]

            queenEngine.enableBeautyType(BeautyFilterType.kSkinBuffing, getQueenParam().basicBeautyRecord.enableSkinBuffing);//磨皮开关
            queenEngine.setBeautyParam(BeautyParams.kBPSkinBuffing, getQueenParam().basicBeautyRecord.skinBuffingParam);  //磨皮 [0,1]
            queenEngine.setBeautyParam(BeautyParams.kBPSkinSharpen, getQueenParam().basicBeautyRecord.skinSharpenParam);  //锐化 [0,1]

            queenEngine.enableBeautyType(BeautyFilterType.kFaceBuffing, getQueenParam().basicBeautyRecord.enableFaceBuffing); //高级美颜开关
            queenEngine.setBeautyParam(BeautyParams.kBPNasolabialFolds, getQueenParam().basicBeautyRecord.faceBuffingNasolabialFoldsParam); //去法令纹[0,1]
            queenEngine.setBeautyParam(BeautyParams.kBPPouch, getQueenParam().basicBeautyRecord.faceBuffingPouchParam); //去眼袋[0,1]
            queenEngine.setBeautyParam(BeautyParams.kBPWhiteTeeth, getQueenParam().basicBeautyRecord.faceBuffingWhiteTeeth); //白牙[0,1]
            queenEngine.setBeautyParam(BeautyParams.kBPLipstick,
                    getQueenParam().basicBeautyRecord.enableFaceBuffingLipstick ?
                    getQueenParam().basicBeautyRecord.faceBuffingLipstick : 0.0f
            ); //滤镜美妆：口红[0,1]
            queenEngine.setBeautyParam(BeautyParams.kBPBrightenEye, getQueenParam().basicBeautyRecord.faceBuffingBrightenEye); //亮眼[0,1]
            queenEngine.setBeautyParam(BeautyParams.kBPBlush, getQueenParam().basicBeautyRecord.faceBuffingBlush); //滤镜美妆：腮红[0,1]
            queenEngine.setBeautyParam(BeautyParams.kBPLipstickColorParam, getQueenParam().basicBeautyRecord.faceBuffingLipstickColorParams);
            queenEngine.setBeautyParam(BeautyParams.kBPLipstickGlossParam, getQueenParam().basicBeautyRecord.faceBuffingLipstickGlossParams);
            queenEngine.setBeautyParam(BeautyParams.kBPLipstickBrightnessParam, getQueenParam().basicBeautyRecord.faceBuffingLipstickBrightnessParams);
            queenEngine.setBeautyParam(BeautyParams.kBPWrinkles, getQueenParam().basicBeautyRecord.faceBuffingWrinklesParam); //去皱纹[0,1]
            queenEngine.setBeautyParam(BeautyParams.kBPBrightenFace, getQueenParam().basicBeautyRecord.faceBuffingBrightenFaceParam); //去暗沉[0,1]

            // 美型
            queenEngine.enableBeautyType(BeautyFilterType.kFaceShape, getQueenParam().faceShapeRecord.enableFaceShape, QueenRuntime.sFaceShapeDebug);
            queenEngine.updateFaceShape(FaceShapeType.typeCutCheek     , getQueenParam().faceShapeRecord.cutCheekParam * QueenParamWeight.FACE_SHAPE_PARAM);
            queenEngine.updateFaceShape(FaceShapeType.typeCutFace      , getQueenParam().faceShapeRecord.cutFaceParam * QueenParamWeight.FACE_SHAPE_PARAM);
            queenEngine.updateFaceShape(FaceShapeType.typeThinFace     , getQueenParam().faceShapeRecord.thinFaceParam * QueenParamWeight.FACE_SHAPE_PARAM);
            queenEngine.updateFaceShape(FaceShapeType.typeLongFace     , getQueenParam().faceShapeRecord.longFaceParam * QueenParamWeight.FACE_SHAPE_PARAM);
            queenEngine.updateFaceShape(FaceShapeType.typeLowerJaw     , getQueenParam().faceShapeRecord.lowerJawParam * QueenParamWeight.FACE_SHAPE_PARAM);
            queenEngine.updateFaceShape(FaceShapeType.typeHigherJaw    , getQueenParam().faceShapeRecord.higherJawParam * QueenParamWeight.FACE_SHAPE_PARAM);
            queenEngine.updateFaceShape(FaceShapeType.typeThinJaw      , getQueenParam().faceShapeRecord.thinJawParam * QueenParamWeight.FACE_SHAPE_PARAM);
            queenEngine.updateFaceShape(FaceShapeType.typeThinMandible , getQueenParam().faceShapeRecord.thinMandibleParam * QueenParamWeight.FACE_SHAPE_PARAM);
            queenEngine.updateFaceShape(FaceShapeType.typeBigEye       , getQueenParam().faceShapeRecord.bigEyeParam * QueenParamWeight.FACE_SHAPE_PARAM);
            queenEngine.updateFaceShape(FaceShapeType.typeEyeAngle1    , getQueenParam().faceShapeRecord.eyeAngle1Param * QueenParamWeight.FACE_SHAPE_PARAM);
            queenEngine.updateFaceShape(FaceShapeType.typeCanthus      , getQueenParam().faceShapeRecord.canthusParam * QueenParamWeight.FACE_SHAPE_PARAM);
            queenEngine.updateFaceShape(FaceShapeType.typeCanthus1     , getQueenParam().faceShapeRecord.canthus1Param * QueenParamWeight.FACE_SHAPE_PARAM);
            queenEngine.updateFaceShape(FaceShapeType.typeEyeAngle2    , getQueenParam().faceShapeRecord.eyeAngle2Param * QueenParamWeight.FACE_SHAPE_PARAM);
            queenEngine.updateFaceShape(FaceShapeType.typeEyeTDAngle   , getQueenParam().faceShapeRecord.eyeTDAngleParam * QueenParamWeight.FACE_SHAPE_PARAM);
            queenEngine.updateFaceShape(FaceShapeType.typeThinNose     , getQueenParam().faceShapeRecord.thinNoseParam * QueenParamWeight.FACE_SHAPE_PARAM);
            queenEngine.updateFaceShape(FaceShapeType.typeNosewing     , getQueenParam().faceShapeRecord.nosewingParam * QueenParamWeight.FACE_SHAPE_PARAM);
            queenEngine.updateFaceShape(FaceShapeType.typeNasalHeight  , getQueenParam().faceShapeRecord.nasalHeightParam * QueenParamWeight.FACE_SHAPE_PARAM);
            queenEngine.updateFaceShape(FaceShapeType.typeNoseTipHeight, getQueenParam().faceShapeRecord.noseTipHeightParam * QueenParamWeight.FACE_SHAPE_PARAM);
            queenEngine.updateFaceShape(FaceShapeType.typeMouthWidth   , getQueenParam().faceShapeRecord.mouthWidthParam * QueenParamWeight.FACE_SHAPE_PARAM);
            queenEngine.updateFaceShape(FaceShapeType.typeMouthSize    , getQueenParam().faceShapeRecord.mouthSizeParam * QueenParamWeight.FACE_SHAPE_PARAM);
            queenEngine.updateFaceShape(FaceShapeType.typeMouthHigh    , getQueenParam().faceShapeRecord.mouthHighParam * QueenParamWeight.FACE_SHAPE_PARAM);
            queenEngine.updateFaceShape(FaceShapeType.typePhiltrum     , getQueenParam().faceShapeRecord.philtrumParam * QueenParamWeight.FACE_SHAPE_PARAM);
            queenEngine.updateFaceShape(FaceShapeType.typeHairLine     , getQueenParam().faceShapeRecord.hairLineParam * QueenParamWeight.FACE_SHAPE_PARAM);
            queenEngine.updateFaceShape(FaceShapeType.typeSmile        , getQueenParam().faceShapeRecord.smailParam * QueenParamWeight.FACE_SHAPE_PARAM);

            // 美妆
            queenEngine.enableBeautyType(BeautyFilterType.kMakeup, getQueenParam().faceMakeupRecord.enableFaceMakeup, QueenRuntime.sFaceMakeupDebug);
            if (getQueenParam().faceMakeupRecord.enableFaceMakeup) {
                for (int makeupType = 0; makeupType < MakeupType.kMakeupMax; makeupType++) {
                    String makeUpResourcePath = getQueenParam().faceMakeupRecord.makeupResourcePath[makeupType];
                    if (!TextUtils.isEmpty(makeUpResourcePath)) {
                        String[] path = new String[] {makeUpResourcePath};
                        int blendType = getQueenParam().faceMakeupRecord.makeupBlendType[makeupType];
                        float alpha = getQueenParam().faceMakeupRecord.makeupAlpha[makeupType];
                        queenEngine.setMakeupImage(makeupType, path, blendType, 15);
                        queenEngine.setMakeupAlpha(makeupType,
                                alpha * QueenParamWeight.FACE_MAKEUP_ALPHA,
                                (1.0f-alpha) * QueenParamWeight.FACE_MAKEUP_ALPHA);
                    } else {
                        String[] path = new String[] {};
                        queenEngine.setMakeupImage(makeupType, path, BlendType.kBlendNormal, 15);
                    }
                }
            }

            // 滤镜
            queenEngine.enableBeautyType(BeautyFilterType.kLUT, getQueenParam().lutRecord.lutEnable);
            if (getQueenParam().lutRecord.lutEnable) {
                queenEngine.setFilter(getQueenParam().lutRecord.lutPath); //设置滤镜
                queenEngine.setBeautyParam(BeautyParams.kBPLUT, getQueenParam().lutRecord.lutParam * QueenParamWeight.FACE_LUT_PARAM); //滤镜强度
            }

            // 贴纸
            addMaterial(queenEngine, getQueenParam().stickerRecord.stickerEnable, getQueenParam().stickerRecord.stickerPath,
                    QueenParam.StickerRecord.usingStickerPathList);
        }
    }

    private static void addMaterial(QueenEngine queenEngine, boolean functionEnabled, String materialPath, List<String> usingList) {
        if (usingList != null && usingList.size() > 0) {
            Iterator<String> iterator = usingList.iterator();
            while (iterator.hasNext()) {
                String usingStickerPath = iterator.next();
                if (!functionEnabled || !TextUtils.equals(usingStickerPath, materialPath)) {
                    queenEngine.removeMaterial(usingStickerPath);
                    iterator.remove();
                }
            }
        }

        if (functionEnabled) {
            boolean hadNotAdded = usingList == null || !usingList.contains(materialPath);
            if (!TextUtils.isEmpty(materialPath) && hadNotAdded) {
                queenEngine.addMaterial(materialPath);
                usingList.add(materialPath);
            }
        }
    }

    public static void relaseQueenParams() {
        QueenParamFactory.Scenes.resetAllScenes();
        setQueenParam(QueenParamFactory.getDefaultScenesParam());
    }

}
