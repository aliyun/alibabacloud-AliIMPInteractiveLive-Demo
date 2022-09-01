package com.aliyun.roompaas.beauty_common;

import com.taobao.android.libqueen.models.BlendType;
import com.taobao.android.libqueen.models.MakeupType;

import java.util.HashMap;

public class QueenParamFactory {

    public static final int ID_FEATURE_NONE = -1;

    private static final String STICKER_DIR_PATH = "sticker/";

//    临时组合模式,先hardcode资源名
    private static final String LUT_1_PATH = "lookups/ly1.png";
    private static final String LUT_7_PATH = "lookups/ly7.png";
    private static final String LUT_27_PATH = "lookups/lz27.png";
    private static final String LUT_8_PATH = "lookups/lz8.png";
    private static final String LUT_23_PATH = "lookups/lz23.png";
    private static final String LUT_5_PATH = "lookups/lz5.png";
    private static final String LUT_10_PATH = "lookups/lz10.png";

    private static final String FACE_MAKEUP_TYPE_MITAO_PATH = "makeup/mitao.png";
    private static final String FACE_MAKEUP_TYPE_YUANQISHAONV_PATH = "makeup/yuanqishaonv.png";
    private static final String FACE_MAKEUP_TYPE_YOUYA_PATH = "makeup/youya.png";
    private static final String FACE_MAKEUP_TYPE_MEIHUO_PATH = "makeup/meihuo.png";
    private static final String FACE_MAKEUP_TYPE_JICHU_PATH = "makeup/jichu.png";

    private static void initBeautyRecord(QueenParam.BasicBeautyRecord basicBeautyRecord) {
        basicBeautyRecord.enableFaceBuffing = true;
        basicBeautyRecord.faceBuffingPouchParam = 0.6f; //去眼袋[0,1]
        basicBeautyRecord.faceBuffingNasolabialFoldsParam = 0.6f; //去法令纹[0,1]
        basicBeautyRecord.faceBuffingWhiteTeeth = 0.2f; //白牙[0,1]
        basicBeautyRecord.faceBuffingBrightenEye = 0.2f; //亮眼[0,1]
        basicBeautyRecord.faceBuffingLipstick = 0.15f; // 滤镜美妆：口红[0,1]
        basicBeautyRecord.enableFaceBuffingLipstick = true; // 滤镜美妆：口红开关
        basicBeautyRecord.faceBuffingBlush = 0.15f; // 滤镜美妆：腮红[0,1]
        basicBeautyRecord.faceBuffingWrinklesParam = 0.0f; // 祛皱纹[0,1]
        basicBeautyRecord.faceBuffingBrightenFaceParam = 0.0f; // 祛暗沉[0,1]
        basicBeautyRecord.enableSkinWhiting = true;
        basicBeautyRecord.enableSkinBuffing = true;
        basicBeautyRecord.enableSkinRed = true;
    }

    // 默认值为通用场景
    public static QueenParam getDefaultScenesParam() {
        return Scenes.getScenes(Scenes.ID_GENERAL);
    }

    public static QueenParam getOriginalScenesParam() {
        return Scenes.getScenes(Scenes.ID_ORIGINAL);
    }

    ////////////////////////////////场景的类别///////////////////////////////////////////
    public static class Scenes {
        private static final int ID_ORIGINAL = ID_FEATURE_NONE;     // 原貌
        private static final int ID_GENERAL = 1;   // 通用模式
        private static final int ID_ONLINE = 2;    // 电商模式
        private static final int ID_RECREATION = 3;// 生活娱乐
        private static final int ID_EDUCATION = 4; // 教育

        public static HashMap<Integer, QueenParam> sScenesCaches = new HashMap<>(4);
        static {
            initScenesParams();
        }

        public static QueenParam getScenes(int id) {
            return sScenesCaches.get(Integer.valueOf(id));
        }

        public static void resetAllScenes() {
            sScenesCaches.clear();
            BeautyParams.resetAllParams();
            FaceShapeParams.resetAllParams();

            initScenesParams();
        }

        private static void initScenesParams() {
            sScenesCaches.put(ID_ORIGINAL, createScenesOriginal());
            sScenesCaches.put(ID_GENERAL, createScenesGeneral());
            sScenesCaches.put(ID_ONLINE, createScenesOnline());
            sScenesCaches.put(ID_RECREATION, createScenesRecreation());
            sScenesCaches.put(ID_EDUCATION, createScenesEducation());
        }

        private static QueenParam createScenesOriginal() {
            QueenParam sScenesOriginal = new QueenParam();
            sScenesOriginal.basicBeautyRecord = BeautyParams.getParams(BeautyParams.ID_ORIGINAL);
            sScenesOriginal.faceShapeRecord = FaceShapeParams.getParams(FaceShapeParams.TAG_SHAPE_ORIGIN);
            return sScenesOriginal;
        }

        // 通用:美颜-基础;美型-精致;美妆-原貌;滤镜-7;贴纸-原貌
        private static QueenParam createScenesGeneral() {
            QueenParam sScenesGeneral = new QueenParam();
            sScenesGeneral.basicBeautyRecord = BeautyParams.getParams(BeautyParams.ID_SIMPLE);
            // 美型-精致
            sScenesGeneral.faceShapeRecord = FaceShapeParams.getParams(FaceShapeParams.TAG_SHAPE_DELICATE);
            sScenesGeneral.faceShapeRecord.enableFaceShape = true;

            // 滤镜1
            sScenesGeneral.lutRecord.lutEnable = true;
            sScenesGeneral.lutRecord.lutPath = LUT_7_PATH;
            sScenesGeneral.lutRecord.lutParam = 0.63f;

            // 美妆
            sScenesGeneral.faceMakeupRecord.enableFaceMakeup = false;
            // 贴纸
            sScenesGeneral.stickerRecord.stickerEnable = false;

            return sScenesGeneral;
        }

        // 电商:美颜-少女;美型-可爱;美妆-元气少女;滤镜-23;贴纸-20
        private static QueenParam createScenesOnline()  {
            QueenParam sScenesOnline = new QueenParam();
            sScenesOnline.basicBeautyRecord = BeautyParams.getParams(BeautyParams.ID_SHAONV);
            // 美型-可爱
            sScenesOnline.faceShapeRecord = FaceShapeParams.getParams(FaceShapeParams.TAG_SHAPE_CUTE);

            // 滤镜8
            sScenesOnline.lutRecord.lutEnable = true;
            sScenesOnline.lutRecord.lutPath = LUT_23_PATH;
            sScenesOnline.lutRecord.lutParam = 0.34f;

            // 美妆
            sScenesOnline.faceMakeupRecord.enableFaceMakeup = true;
            sScenesOnline.faceMakeupRecord.makeupResourcePath[MakeupType.kMakeupWhole] = FACE_MAKEUP_TYPE_YUANQISHAONV_PATH;
            sScenesOnline.faceMakeupRecord.makeupBlendType[MakeupType.kMakeupWhole] = BlendType.kBlendLabMix;

            // 贴纸20
            sScenesOnline.stickerRecord.stickerEnable = true;
            sScenesOnline.stickerRecord.stickerPath = STICKER_DIR_PATH + 20;

            return sScenesOnline;
        }

        // 娱乐:美颜-魅惑;美型-网红;美妆-魅惑;滤镜-5;贴纸-12
        private static QueenParam createScenesRecreation() {
            QueenParam sScenesRecreation = new QueenParam();
            sScenesRecreation.basicBeautyRecord = BeautyParams.getParams(BeautyParams.ID_MEIHUO);
            // 美型-网红
            sScenesRecreation.faceShapeRecord = FaceShapeParams.getParams(FaceShapeParams.TAG_SHAPE_WANGHONG);

            // 滤镜5
            sScenesRecreation.lutRecord.lutEnable = true;
            sScenesRecreation.lutRecord.lutPath = LUT_5_PATH;
            sScenesRecreation.lutRecord.lutParam = 0.65f;

            // 美妆
            sScenesRecreation.faceMakeupRecord.enableFaceMakeup = true;
            sScenesRecreation.faceMakeupRecord.makeupResourcePath[MakeupType.kMakeupWhole] = FACE_MAKEUP_TYPE_MEIHUO_PATH;
            sScenesRecreation.faceMakeupRecord.makeupBlendType[MakeupType.kMakeupWhole] = BlendType.kBlendLabMix;
            // 贴纸12
            sScenesRecreation.stickerRecord.stickerEnable = true;
            sScenesRecreation.stickerRecord.stickerPath = STICKER_DIR_PATH + 12;

            return sScenesRecreation;
        }

        // 教育:美颜-流行;美型-优雅;美妆-基础妆;滤镜-27;贴纸-原貌
        private static QueenParam createScenesEducation()  {
            QueenParam sScenesEducation = new QueenParam();
            sScenesEducation.basicBeautyRecord = BeautyParams.getParams(BeautyParams.ID_FASHION);
            // 美型-优雅
            sScenesEducation.faceShapeRecord = FaceShapeParams.getParams(FaceShapeParams.TAG_SHAPE_GRACE);

            // 滤镜27
            sScenesEducation.lutRecord.lutEnable = true;
            sScenesEducation.lutRecord.lutPath = LUT_27_PATH;
            sScenesEducation.lutRecord.lutParam = 0.25f;

            // 美妆-基础妆
            sScenesEducation.faceMakeupRecord.enableFaceMakeup = true;
            sScenesEducation.faceMakeupRecord.makeupResourcePath[MakeupType.kMakeupWhole] = FACE_MAKEUP_TYPE_JICHU_PATH;
            sScenesEducation.faceMakeupRecord.makeupBlendType[MakeupType.kMakeupWhole] = BlendType.kBlendLabMix;
            // 贴纸
            sScenesEducation.stickerRecord.stickerEnable = false;

            return sScenesEducation;
        }
    }
    ////////////////////////////////场景的类别 end///////////////////////////////////////////


    ////////////////////////////////美颜的类别///////////////////////////////////////////
    public static class BeautyParams {
        public static final int ID_ORIGINAL = ID_FEATURE_NONE;     // 原貌
        public static final int ID_SIMPLE = 2;
        public static final int ID_FASHION = 3;
        public static final int ID_MEIHUO = 4;
        public static final int ID_KEAI = 5;
        public static final int ID_SHAONV = 6;

        public static HashMap<Integer, QueenParam.BasicBeautyRecord> sParamsCaches = new HashMap<>(6);
        static {
            initParams();
        }

        public static QueenParam.BasicBeautyRecord getParams(int id) {
            return sParamsCaches.get(Integer.valueOf(id));
        }

        public static void resetAllParams() {
            sParamsCaches.clear();
            initParams();
        }

        public static void initParams() {
            sParamsCaches.put(ID_ORIGINAL, createModeOriginal());
            sParamsCaches.put(ID_SIMPLE, createModeSimple());
            sParamsCaches.put(ID_FASHION, createModeFashion());
            sParamsCaches.put(ID_MEIHUO, createModeMeiHuo());
            sParamsCaches.put(ID_KEAI, createModeKeAi());
            sParamsCaches.put(ID_SHAONV, createModeShaoNv());
        }

        private static QueenParam.BasicBeautyRecord createModeOriginal() {
            QueenParam.BasicBeautyRecord sModeOriginal = new QueenParam.BasicBeautyRecord();
            sModeOriginal.clear();
            return sModeOriginal;
        }

        // 基础模式
        private static QueenParam.BasicBeautyRecord createModeSimple() {
            QueenParam.BasicBeautyRecord sModeSimple = new QueenParam.BasicBeautyRecord();
            initBeautyRecord(sModeSimple);
            sModeSimple.skinWhitingParam = 0.32f; // 美白
            sModeSimple.skinBuffingParam = 0.72f;  // 磨皮
            sModeSimple.skinSharpenParam = 0.1f;  // 锐化
            sModeSimple.skinRedParam = 0.1f;      // 红润
            return sModeSimple;
        }

        // 流行模式
        private static QueenParam.BasicBeautyRecord createModeFashion() {
            QueenParam.BasicBeautyRecord sModeFashion = new QueenParam.BasicBeautyRecord();
            initBeautyRecord(sModeFashion);
            sModeFashion.skinWhitingParam = 0.26f; // 美白
            sModeFashion.skinBuffingParam = 0.65f;  // 磨皮
            sModeFashion.skinSharpenParam = 0.10f;  // 锐化
            sModeFashion.skinRedParam = 0.48f;      // 红润
            sModeFashion.faceBuffingPouchParam = 0.75f;      // 眼袋
            sModeFashion.faceBuffingNasolabialFoldsParam = 0.73f; //去法令纹[0,1]
            sModeFashion.faceBuffingWhiteTeeth = 0.54f; //白牙[0,1]
            sModeFashion.faceBuffingBrightenEye = 0.96f; //亮眼[0,1]
            sModeFashion.enableFaceBuffingLipstick = true; // 滤镜美妆：口红开关
            sModeFashion.faceBuffingLipstick = 0.32f; // 滤镜美妆：口红[0,1]
            sModeFashion.faceBuffingLipstickColorParams = 0.0f;
            sModeFashion.faceBuffingLipstickGlossParams = 0.35f;
            sModeFashion.faceBuffingLipstickBrightnessParams = 0.0f;
            sModeFashion.faceBuffingBlush = 0.20f; // 滤镜美妆：腮红[0,1]
            sModeFashion.faceBuffingWrinklesParam = 0.0f; // 祛皱纹[0,1]
            sModeFashion.faceBuffingBrightenFaceParam = 0.0f; // 祛暗沉[0,1]

            return sModeFashion;
        }

        private static QueenParam.BasicBeautyRecord createModeMeiHuo() {
            QueenParam.BasicBeautyRecord sModeMeiHuo = new QueenParam.BasicBeautyRecord();
            initBeautyRecord(sModeMeiHuo);
            sModeMeiHuo.skinWhitingParam = 0.36f; // 美白
            sModeMeiHuo.skinBuffingParam = 0.68f;  // 磨皮
            sModeMeiHuo.skinSharpenParam = 0.13f;  // 锐化
            sModeMeiHuo.skinRedParam = 0.5f;      // 红润
            sModeMeiHuo.faceBuffingPouchParam = 0.84f;      // 眼袋
            sModeMeiHuo.faceBuffingNasolabialFoldsParam = 0.78f; //去法令纹[0,1]
            sModeMeiHuo.faceBuffingWhiteTeeth = 0.72f; //白牙[0,1]
            sModeMeiHuo.faceBuffingBrightenEye = 0.89f; //亮眼[0,1]
            sModeMeiHuo.enableFaceBuffingLipstick = true; // 滤镜美妆：口红开关
            sModeMeiHuo.faceBuffingLipstick = 0.32f; // 滤镜美妆：口红[0,1]
            sModeMeiHuo.faceBuffingLipstickColorParams = 0.f;
            sModeMeiHuo.faceBuffingLipstickGlossParams = 1.0f;
            sModeMeiHuo.faceBuffingLipstickBrightnessParams = 0.2f;
            sModeMeiHuo.faceBuffingBlush = 0.20f; // 滤镜美妆：腮红[0,1]
            sModeMeiHuo.faceBuffingWrinklesParam = 0.0f; // 祛皱纹[0,1]
            sModeMeiHuo.faceBuffingBrightenFaceParam = 0.0f; // 祛暗沉[0,1]

            return sModeMeiHuo;
        }

        private static QueenParam.BasicBeautyRecord createModeKeAi() {
            QueenParam.BasicBeautyRecord sModeKeAi = new QueenParam.BasicBeautyRecord();
            initBeautyRecord(sModeKeAi);
            sModeKeAi.skinWhitingParam = 0.36f; // 美白
            sModeKeAi.skinBuffingParam = 0.56f;  // 磨皮
            sModeKeAi.skinSharpenParam = 0.20f;  // 锐化
            sModeKeAi.skinRedParam = 0.12f;      // 红润

            return sModeKeAi;
        }

        private static QueenParam.BasicBeautyRecord createModeShaoNv() {
            QueenParam.BasicBeautyRecord sModeShaoNv = new QueenParam.BasicBeautyRecord();
            initBeautyRecord(sModeShaoNv);
            sModeShaoNv.skinWhitingParam = 0.40f; // 美白
            sModeShaoNv.skinBuffingParam = 0.72f;  // 磨皮
            sModeShaoNv.skinSharpenParam = 0.10f;  // 锐化
            sModeShaoNv.skinRedParam = 0.26f;      // 红润
            sModeShaoNv.faceBuffingWhiteTeeth = 0.60f;  // 白牙

            sModeShaoNv.faceBuffingPouchParam = 0.8f; //去眼袋[0,1]
            sModeShaoNv.faceBuffingNasolabialFoldsParam = 0.9f; //去法令纹[0,1]
            sModeShaoNv.faceBuffingWhiteTeeth = 0.2f; //白牙[0,1]
            sModeShaoNv.faceBuffingBrightenEye = 0.8f; //亮眼[0,1]
            sModeShaoNv.faceBuffingBlush = 0.35f; // 滤镜美妆：腮红[0,1]
            sModeShaoNv.faceBuffingWrinklesParam = 0.0f; // 祛皱纹[0,1]
            sModeShaoNv.faceBuffingBrightenFaceParam = 0.0f; // 祛暗沉[0,1]

            return sModeShaoNv;
        }

    }
    ////////////////////////////////美颜的类别 end///////////////////////////////////////////

    ////////////////////////////////美型的类别///////////////////////////////////////////
    public static class FaceShapeParams {
        public static final String TAG_SHAPE_ORIGIN = "close";
        public static final String TAG_SHAPE_GRACE = "grace";
        public static final String TAG_SHAPE_DELICATE = "delicate";
        public static final String TAG_SHAPE_WANGHONG = "wanghong";
        public static final String TAG_SHAPE_CUTE = "cute";
        public static final String TAG_SHAPE_BABY = "baby";

        public static HashMap<String, QueenParam.FaceShapeRecord> sParamsCaches = new HashMap<>(6);
        static {
            initParams();
        }

        public static QueenParam.FaceShapeRecord getParams(String tag) {
            return sParamsCaches.get(tag);
        }

        public static void resetAllParams() {
            sParamsCaches.clear();
            initParams();
        }

        public static void initParams() {
            sParamsCaches.put(TAG_SHAPE_ORIGIN, createOriginShape());
            sParamsCaches.put(TAG_SHAPE_GRACE, createGraceShape());
            sParamsCaches.put(TAG_SHAPE_DELICATE, createDelicateShape());
            sParamsCaches.put(TAG_SHAPE_WANGHONG, createWangHongShape());
            sParamsCaches.put(TAG_SHAPE_CUTE, createCuteShape());
            sParamsCaches.put(TAG_SHAPE_BABY, createBabyShape());
        }

        // 原貌
        private static QueenParam.FaceShapeRecord createOriginShape() {
            QueenParam.FaceShapeRecord sOriginShapeRecord = new QueenParam.FaceShapeRecord();
            sOriginShapeRecord.enableFaceShape = false;
            return sOriginShapeRecord;
        }

        // 优雅
        private static QueenParam.FaceShapeRecord createGraceShape() {
            QueenParam.FaceShapeRecord sGraceFaceShapeRecord = new QueenParam.FaceShapeRecord();
            sGraceFaceShapeRecord.enableFaceShape = true;
            sGraceFaceShapeRecord.cutFaceParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(33);
            sGraceFaceShapeRecord.thinFaceParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(58);
            sGraceFaceShapeRecord.longFaceParam = QueenParam.FaceShapeRecord.formatReverseParam(17);
            sGraceFaceShapeRecord.lowerJawParam = QueenParam.FaceShapeRecord.formatReverseParam(7);
            sGraceFaceShapeRecord.bigEyeParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(52);
            sGraceFaceShapeRecord.thinNoseParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(0);
            sGraceFaceShapeRecord.mouthWidthParam = QueenParam.FaceShapeRecord.formatReverseParam(18);
            sGraceFaceShapeRecord.thinMandibleParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(0);
            sGraceFaceShapeRecord.cutCheekParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(0);

            return sGraceFaceShapeRecord;
        }

        // 精致
        private static QueenParam.FaceShapeRecord createDelicateShape() {
            QueenParam.FaceShapeRecord sDelicateShapeRecord = new QueenParam.FaceShapeRecord();
            sDelicateShapeRecord.enableFaceShape = true;
            sDelicateShapeRecord.cutFaceParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(6);
            sDelicateShapeRecord.thinFaceParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(22);
            sDelicateShapeRecord.longFaceParam = QueenParam.FaceShapeRecord.formatReverseParam(10);
            sDelicateShapeRecord.lowerJawParam = QueenParam.FaceShapeRecord.formatReverseParam(33);
            sDelicateShapeRecord.bigEyeParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(0);
            sDelicateShapeRecord.thinNoseParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(0);
            sDelicateShapeRecord.mouthWidthParam = QueenParam.FaceShapeRecord.formatReverseParam(0);
            sDelicateShapeRecord.thinMandibleParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(0);
            sDelicateShapeRecord.cutCheekParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(0);

            return sDelicateShapeRecord;
        }

        // 网红
        private static QueenParam.FaceShapeRecord createWangHongShape() {
            QueenParam.FaceShapeRecord sWangHongShapeRecord = new QueenParam.FaceShapeRecord();
            sWangHongShapeRecord.enableFaceShape = true;
            sWangHongShapeRecord.thinFaceParam = 0.88f;
            sWangHongShapeRecord.bigEyeParam = 0.82f;
            sWangHongShapeRecord.nosewingParam = 0.83f;
            sWangHongShapeRecord.thinNoseParam = 0.88f;
            sWangHongShapeRecord.thinJawParam = 0.5f;

            return sWangHongShapeRecord;
        }

        // 可爱
        private static QueenParam.FaceShapeRecord createCuteShape() {
            QueenParam.FaceShapeRecord sCuteShapeRecord = new QueenParam.FaceShapeRecord();
            sCuteShapeRecord.enableFaceShape = true;
            sCuteShapeRecord.thinFaceParam = 0.38f;
            sCuteShapeRecord.bigEyeParam = 0.42f;
            sCuteShapeRecord.nosewingParam = 0.33f;
            sCuteShapeRecord.thinNoseParam = 0.88f;
            sCuteShapeRecord.thinJawParam = 0.3f;

            return sCuteShapeRecord;
        }

        // 婴儿
        private static QueenParam.FaceShapeRecord createBabyShape() {
            QueenParam.FaceShapeRecord sBabyFaceShapeRecord = new QueenParam.FaceShapeRecord();
            sBabyFaceShapeRecord.enableFaceShape = true;
            sBabyFaceShapeRecord.cutFaceParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(15);
            sBabyFaceShapeRecord.thinFaceParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(6);
            sBabyFaceShapeRecord.longFaceParam = QueenParam.FaceShapeRecord.formatReverseParam(27);
            sBabyFaceShapeRecord.lowerJawParam = QueenParam.FaceShapeRecord.formatReverseParam(-10);
            sBabyFaceShapeRecord.bigEyeParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(16);
            sBabyFaceShapeRecord.thinNoseParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(0);
            sBabyFaceShapeRecord.mouthWidthParam = QueenParam.FaceShapeRecord.formatReverseParam(-8);
            sBabyFaceShapeRecord.thinMandibleParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(0);
            sBabyFaceShapeRecord.cutCheekParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(0);

            return sBabyFaceShapeRecord;
        }
    }
    ////////////////////////////////美颜的类别 end///////////////////////////////////////////


}
