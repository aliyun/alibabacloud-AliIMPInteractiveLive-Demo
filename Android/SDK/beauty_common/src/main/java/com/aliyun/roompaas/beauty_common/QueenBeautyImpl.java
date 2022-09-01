package com.aliyun.roompaas.beauty_common;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.opengl.GLES20;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.WindowManager;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.roompaas.base.cloudconfig.CloudConfigCenter;
import com.aliyun.roompaas.base.log.Logger;
import com.aliyun.roompaas.live.BeautyInterface;
import com.aliyun.roompaas.beauty_base.module.AliLiveBeautyBlendType;
import com.aliyun.roompaas.beauty_base.module.AliLiveBeautyMakeupType;
import com.aliyun.roompaas.beauty_base.module.AliLiveBeautyParams;
import com.aliyun.roompaas.beauty_base.module.AliLiveBeautyType;
import com.aliyun.roompaas.beauty_base.module.BeautyImageFormat;
import com.taobao.android.libqueen.QueenConfig;
import com.taobao.android.libqueen.QueenEngine;
import com.taobao.android.libqueen.Texture2D;
import com.taobao.android.libqueen.exception.InitializationException;
import com.taobao.android.libqueen.models.BeautyFilterType;
import com.taobao.android.libqueen.models.BeautyParams;
import com.taobao.android.libqueen.models.Flip;

import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.List;

import static android.opengl.GLES20.GL_FRAMEBUFFER;

public abstract class QueenBeautyImpl implements BeautyInterface {
    private static final String TAG = "QueenBeautyImpl";

    // 默认美颜参数
    public static final float DEFAULT_BP_SKIN_WHITENING_PARAMS = 0.32f;
    public static final float DEFAULT_BP_SKIN_BUFFING_PARAMS = 0.72f;
    public static final float DEFAULT_BP_SKIN_SHARPEN_PARAMS = 0.2f;
    public static final float DEFAULT_BP_SKIN_SKIN_RED_PARAMS = 0.2f;

    private static final int DEFAULT_MAKEUP_FPS = 15;
    private static final float DEFAULT_MAKEUP_ALPHA = 0.8f;
    private static final float DEFAULT_MAKEUP_MALE_ALPHA = 0.2f;

    private final Context mContext;

    private final Object mCmdLock = new Object();
    private final List<Object> mCmdList = new LinkedList<>();

    private long glThreadId = -1;

    private QueenEngine mMediaChainEngine;

    private Texture2D mOutTexture2D = null;
    private int lastTextureWidth = 0;
    private int lastTextureHeight = 0;

    private int mDeviceOrientation = 0;
    protected String queenSecret;
    private OrientationEventListener mOrientationListener;

    private volatile boolean isBeautyEnable = false;
    private volatile boolean isAlgDataRendered = false;
    private static boolean USE_FRAME_SYNCHRONIZED = true;

    public QueenBeautyImpl(Context context) {
        mContext = context;
        initOrientationListener(context);
    }

    public QueenBeautyImpl(Context context, String queenSecret) {
        mContext = context;
        initOrientationListener(context);
        this.queenSecret = queenSecret;
    }

    @Override
    public void init() {
        if (mMediaChainEngine == null) {
            // 美颜库需要在texture线程中运行，如果未创建美颜引擎, 创建美颜引擎
            Logger.d(TAG, "init");

            try {
                mMediaChainEngine = new QueenEngine(mContext, assembleConfig());
            } catch (InitializationException e) {
                e.printStackTrace();
            }

            isBeautyEnable = CloudConfigCenter.getInstance().enableBeautify();

            if (shouldLoadConfig()){
                loadConfigs();
            }
        }
    }

    protected abstract boolean shouldLoadConfig();

    protected abstract boolean shouldWriteParamToEngine();

    protected QueenConfig assembleConfig(){
        QueenConfig config = new QueenConfig();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("bizHashCode", queenSecret);
        config.extraInfo = jsonObject.toString();
        return config;
    }

    @Override
    public void switchCameraId(int cameraId) {

    }

    @Override
    public void release() {
        Logger.d(TAG, "release");

        destroyOrientationListener();

        if (mOutTexture2D != null) {
            mOutTexture2D.release();
            mOutTexture2D = null;
        }

        if (mMediaChainEngine != null) {
            mMediaChainEngine.release();
            mMediaChainEngine = null;
        }

        isBeautyEnable = false;
    }

    @Override
    public void setBeautyEnable(boolean enable) {
        Logger.d(TAG, "setBeautyEnable: " + enable);
        isBeautyEnable = enable;
    }

    private void loadConfigs() {
        // 默认开启基础美颜 美白、磨皮、锐化、红润
        mMediaChainEngine.enableBeautyType(BeautyFilterType.kSkinWhiting, true);
        mMediaChainEngine.setBeautyParam(BeautyParams.kBPSkinWhitening, DEFAULT_BP_SKIN_WHITENING_PARAMS);  //美白 [0,1]
        mMediaChainEngine.setBeautyParam(BeautyParams.kBPSkinRed, DEFAULT_BP_SKIN_SKIN_RED_PARAMS);  //红润 [0,1]

        mMediaChainEngine.enableBeautyType(BeautyFilterType.kSkinBuffing, true);
        mMediaChainEngine.setBeautyParam(BeautyParams.kBPSkinBuffing, DEFAULT_BP_SKIN_BUFFING_PARAMS); //磨皮 [0,1]
        mMediaChainEngine.setBeautyParam(BeautyParams.kBPSkinSharpen, DEFAULT_BP_SKIN_SHARPEN_PARAMS); //锐化 [0,1]
    }

    @Override
    public void setBeautyType(int type, boolean enable) {
        SetBeautyTypeCmd cmd = new SetBeautyTypeCmd();
        cmd.type = type;
        cmd.enable = enable;

        if (!isCurrentTextureThread(cmd)) {
            return;
        }

        Logger.d(TAG, "threadID=" + Thread.currentThread().getId() + ", setBeautyType: " + type + ", " + enable);
        if (mMediaChainEngine != null) {
            mMediaChainEngine.enableBeautyType(type, enable);
        }
    }

    @Override
    public void setBeautyParams(int type, float value) {
        SetBeautyParamCmd cmd = new SetBeautyParamCmd();
        cmd.type = type;
        cmd.value = value;

        if (!isCurrentTextureThread(cmd)) {
            return;
        }

        Logger.d(TAG, "threadID=" + Thread.currentThread().getId() + ", setBeautyParams: " + type + ", " + value);
        if (type == AliLiveBeautyParams.kAliLiveParamsBPSkinBuffing || type == AliLiveBeautyParams.kAliLiveParamsBPSkinSharpen) {
            setBeautyType(AliLiveBeautyType.kAliLiveSkinBuffing, true);
        } else if (type == AliLiveBeautyParams.kAliLiveParamsBPSkinWhitening) {
            setBeautyType(AliLiveBeautyType.kAliLiveSkinWhiting, true);
        }
        mMediaChainEngine.setBeautyParam(type, value);
    }

    @Override
    public void setFaceShapeParams(int type, float value) {
        SetFaceShapeCmd cmd = new SetFaceShapeCmd();
        cmd.type = type;
        cmd.value = value;
        if (!isCurrentTextureThread(cmd)) {
            return;
        }

        Logger.d(TAG, "threadID=" + Thread.currentThread().getId() + ", setFaceShape: " + type + ", " + value);
        setBeautyType(AliLiveBeautyType.kAliLiveFaceShape, true);
        mMediaChainEngine.updateFaceShape(type, value);
    }

    @Override
    public void setMakeupParams(int type, String path) {
        MakeupImageCmd cmd = new MakeupImageCmd();
        cmd.type = type;
        cmd.path = path;
        if (!isCurrentTextureThread(cmd)) {
            return;
        }

        Logger.d(TAG, "threadID=" + Thread.currentThread().getId() + ", setMakeupParams: " + type + ", " + path);
        setBeautyType(AliLiveBeautyType.kAliLiveMakeup, true);
        mMediaChainEngine.setMakeupImage(type, new String[]{path}, getBlendTypeByMakeupType(type), DEFAULT_MAKEUP_FPS);
        mMediaChainEngine.setMakeupAlpha(type, DEFAULT_MAKEUP_ALPHA, DEFAULT_MAKEUP_MALE_ALPHA);
    }

    @Override
    public void setFilterParams(String path) {
        SetFilterParamsCmd cmd = new SetFilterParamsCmd();
        cmd.path = path;
        if (!isCurrentTextureThread(cmd)) {
            return;
        }

        Logger.d(TAG, "threadID=" + Thread.currentThread().getId() + ", setFilterParams: " + path);
        setBeautyType(AliLiveBeautyType.kAliLiveLUT, true);
        if (mMediaChainEngine != null) {
            mMediaChainEngine.setFilter(path);
        }
    }

    @Override
    public void setMaterialParams(String path) {
        SetMaterialParamsCmd cmd = new SetMaterialParamsCmd();
        cmd.path = path;
        cmd.add = true;
        if (!isCurrentTextureThread(cmd)) {
            return;
        }

        Logger.d(TAG, "threadID=" + Thread.currentThread().getId() + ", setMaterialParams: " + path);
        if (mMediaChainEngine != null) {
            mMediaChainEngine.addMaterial(path);
        }
    }

    @Override
    public void removeMaterialParams(String path) {
        SetMaterialParamsCmd cmd = new SetMaterialParamsCmd();
        cmd.path = path;
        cmd.add = false;
        if (!isCurrentTextureThread(cmd)) {
            return;
        }

        Logger.d(TAG, "threadID=" + Thread.currentThread().getId() + ", removeMaterialParams: " + path);
        if (mMediaChainEngine != null) {
            mMediaChainEngine.removeMaterial(path);
        }
    }

    @Override
    public int onTextureInput(int inputTexture, int textureWidth, int textureHeight) {
        glThreadId = Thread.currentThread().getId();

        if (mMediaChainEngine == null || !isBeautyEnable) {
            return inputTexture;
        }

        updateSettings();

        int[] oldFboId = new int[1];
        GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING, IntBuffer.wrap(oldFboId));

        mMediaChainEngine.setInputTexture(inputTexture, textureWidth, textureHeight, false);

        //如果画面旋转的话，就需要重新创建设置大小
        if (lastTextureWidth != textureWidth || lastTextureHeight != textureHeight) {
            if (mOutTexture2D != null) {
                mOutTexture2D.release();
                mOutTexture2D = null;
            }
            lastTextureWidth = textureWidth;
            lastTextureHeight = textureHeight;
            mMediaChainEngine.setScreenViewport(0, 0, textureWidth, textureHeight);
        }

        if (mOutTexture2D == null) {
            mOutTexture2D = mMediaChainEngine.autoGenOutTexture(true);
        }

        if (mOutTexture2D == null) {
            return inputTexture;
        }

        long now = SystemClock.uptimeMillis();

        boolean hasRunAlg = false;
        boolean isCameraFront = mCurCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT;

        if (USE_FRAME_SYNCHRONIZED) {
            mMediaChainEngine.setInputFlip(Flip.kNone);
            if (outAngle == 90 || outAngle == 270) {// 右 out = 90 / 左 out = 270
                mMediaChainEngine.setRenderAndFaceFlip(Flip.kNone, Flip.kFlipY);
                mMediaChainEngine.updateInputTextureBufferAndRunAlg((outAngle + 180) % 360, (outAngle + 180) % 360, isCameraFront ? Flip.kFlipY : Flip.kNone, false);
            } else {// 正 out = 180 / 倒立 out = 0
                mMediaChainEngine.setRenderAndFaceFlip(Flip.kNone, Flip.kFlipY);
                mMediaChainEngine.updateInputTextureBufferAndRunAlg(180 - outAngle, 180 - outAngle, isCameraFront ? Flip.kFlipY : Flip.kNone, false);
            }
            hasRunAlg = true;
        } else if (mAlgNativeBufferPtr != 0) {
            mMediaChainEngine.updateInputNativeBufferAndRunAlg(mAlgNativeBufferPtr, mAlgDataFormat, mAlgDataWidth, mAlgDataHeight, nAlgDataStride, inputAngle, outAngle, flipAxis);
            hasRunAlg = true;
        }

        if (shouldWriteParamToEngine()){
            QueenParamHolder.writeParamToEngine(mMediaChainEngine, false);
        }

        float[] matrix = isCameraFront ? new float[]{
                -1.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f, 0.0f,
                1.0f, 0.0f, 0.0f, 1.0f,
        } : new float[]{
                1.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f,
        };

        int retCode = mMediaChainEngine.renderTexture(matrix);
        isAlgDataRendered = true;

        Logger.i(TAG, Thread.currentThread().getId() + " - " +"render : " + (SystemClock.uptimeMillis()-now) + "ms, hasRunAlg: " + hasRunAlg + ", textureW: " + textureWidth + ", textureH: " + textureHeight + ", outAngle: " + outAngle);
        if (retCode == -9 || retCode == -10) {
            Logger.d(TAG, "queen error code:" + retCode + ",please ensure license valid");
            return inputTexture;
        }

        GLES20.glBindFramebuffer(GL_FRAMEBUFFER, oldFboId[0]);

        return mOutTexture2D.getTextureId();
    }

    @Override
    public int onTextureUpdate(int inputTexture, int textureWidth, int textureHeight) {
        int texId = inputTexture;
        if (!QueenRuntime.isEnableQueen || mMediaChainEngine == null || !isBeautyEnable) {
            return texId;
        }

        mMediaChainEngine.setInputTexture(inputTexture, textureWidth, textureHeight, false);
        mMediaChainEngine.setScreenViewport(0, 0, textureWidth, textureHeight);
        if (mOutTexture2D == null || mLastInputTextureW != textureWidth || mLastInputTextureH != textureHeight) {
            if (mOutTexture2D != null) mOutTexture2D.release();
            mOutTexture2D = mMediaChainEngine.autoGenOutTexture();
            mMediaChainEngine.updateOutTexture(mOutTexture2D.getTextureId(), textureWidth, textureHeight);
        }
        mLastInputTextureW = textureWidth;
        mLastInputTextureH = textureHeight;

        int inputAngle = QueenCameraHelper.get().inputAngle;
        int outAngle = QueenCameraHelper.get().outAngle;
        boolean isLandscape = inputAngle % 180 == 0;    // 是否是横屏
        if (isLandscape) {
            inputAngle = (inputAngle + 180) % 360;
        } else {
            outAngle = (outAngle + 180) % 360;
        }
        int flipAxis = QueenCameraHelper.get().flipAxis;
        mMediaChainEngine.setSegmentInfoFlipY(true);
        mMediaChainEngine.updateInputTextureBufferAndRunAlg(inputAngle, outAngle, flipAxis, false);
        QueenParamHolder.writeParamToEngine(mMediaChainEngine, false);

        int retCode = mMediaChainEngine.render();
        if (retCode == -9 || retCode == -10) {
            return texId;
        }
        texId = mOutTexture2D.getTextureId();

        return texId;
    }

    private int mLastInputTextureW = 0;
    private int mLastInputTextureH = 0;

    @Override
    public void onDrawFrame(byte[] image, int format, int width, int height, int stride) {
        if (mMediaChainEngine != null && isBeautyEnable) {
            int displayOrientation = getDisplayOrientation();

            int inputAngle = 0;
            int outputAngle = 0;

            if (displayOrientation == 90 || displayOrientation == 270) { //横屏
                inputAngle = (270 - mDeviceOrientation + 360) % 360;
                outputAngle = (displayOrientation - mDeviceOrientation + 360) % 360;
            } else if (displayOrientation == 0 || displayOrientation == 180) { //竖屏
                inputAngle = (270 - mDeviceOrientation + 360) % 360;
                outputAngle = (180 + displayOrientation - mDeviceOrientation + 360) % 360;
            }

            mMediaChainEngine.updateInputDataAndRunAlg(image, format, width, height, stride, inputAngle, outputAngle, 0);
        }
    }

    private Camera.CameraInfo mCameraInfo = new Camera.CameraInfo();
    private int mCurCameraId = -1;
    private long mAlgNativeBufferPtr;
    private int mAlgDataFormat, mAlgDataWidth, mAlgDataHeight, nAlgDataStride;
    private int inputAngle;
    private int outAngle;
    private int flipAxis;

    private void setCameraAngles4Back() {
        int displayOrientation = getDisplayOrientation();
        inputAngle = (mCameraInfo.orientation + mDeviceOrientation) % 360;
        int angle = mDeviceOrientation % 360;
        outAngle = (angle - displayOrientation + 360) % 360;

        if (displayOrientation == 0 || displayOrientation == 180) { //竖屏
            outAngle = (180 + displayOrientation - mDeviceOrientation + 360) % 360;
            if (mDeviceOrientation % 180 == 90) {
                outAngle = (180 + mDeviceOrientation) % 360;
            }
        }
        flipAxis = Flip.kFlipY;
    }

    private void setCameraAngles4Front() {
        inputAngle = 0;
        outAngle = 0;

        int displayOrientation = getDisplayOrientation();
        if (displayOrientation == 90 || displayOrientation == 270) { //横屏
            inputAngle = (270 - mDeviceOrientation + 360) % 360;
            outAngle = (displayOrientation - mDeviceOrientation + 360) % 360;
        } else if (displayOrientation == 0 || displayOrientation == 180) { //竖屏
            inputAngle = (270 - mDeviceOrientation + 360) % 360;
            outAngle = (180 + displayOrientation - mDeviceOrientation + 360) % 360;
        }

        flipAxis = Flip.kFlipY;
    }

    @Override
    public void onDrawFrame(long imageNativeBufferPtr, int format, int width, int height, int stride, int cameraId) {
        if (mMediaChainEngine != null) {

            if (cameraId != mCurCameraId) {
                Camera.getCameraInfo(cameraId, mCameraInfo);
                mCurCameraId = cameraId;
            }

            boolean isCameraFront = cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT;
            if (isCameraFront) {
                setCameraAngles4Front();
            } else {
                setCameraAngles4Back();
            }

            if (isAlgDataRendered && !USE_FRAME_SYNCHRONIZED) {
                long now = SystemClock.uptimeMillis();
                // 1、测试发现，onDrawFrame接口和onTextureInput接口虽然是不同的线程回调，但会顺序执行;
                //    onDrawFrame回调频率高于onTextureInput，所以保证算法结果被用于渲染之后，才会再次执行，否则会有一次冗余的算法过程，也会导致卡顿
                // 2、新版本的queen不支持在非渲染线程执行，所以先在native侧将buffer拷贝存储起来，在渲染线程执行算法
                long bufferSize = 0;
                switch (format) {
                    case BeautyImageFormat.kNV21:
                        bufferSize = (long) (width * height * 1.5);
                        break;
                    case BeautyImageFormat.kRGB:
                        bufferSize = width*height*3;
                        break;
                    case BeautyImageFormat.kRGBA:
                        bufferSize = width*height*4;
                        break;
                    default:
                        break;
                }
                if (bufferSize > 0) {
                    mAlgNativeBufferPtr = mMediaChainEngine.copyNativeBuffer(imageNativeBufferPtr, bufferSize);
                    mAlgDataFormat = format;
                    mAlgDataWidth = width;
                    mAlgDataHeight = height;
                    nAlgDataStride = stride;
                }
                isAlgDataRendered = false;
                Logger.i(TAG, Thread.currentThread().getId() + " - " + "updateInputNativeBufferAndRunAlg : " + (SystemClock.uptimeMillis() - now) + "ms");
            }
        }
    }

    @Override
    public String getVersion() {
        return "";
    }

    @Override
    public Object getBeautyEngine() {
        return mMediaChainEngine;
    }

    @AliLiveBeautyBlendType
    private static int getBlendTypeByMakeupType(@AliLiveBeautyMakeupType int type) {
        if (type == AliLiveBeautyMakeupType.kAliLiveMakeupMouth) {
            return AliLiveBeautyBlendType.kAliLiveBlendSoftLight;
        } else if (type == AliLiveBeautyMakeupType.kAliLiveMakeupHighlight) {
            return AliLiveBeautyBlendType.kAliLiveBlendOverlay;
        }
        return AliLiveBeautyBlendType.kAliLiveBlendNormal;
    }

    // TODO: Warning: patch code, need to be replaced next version. We should get camera orientation by texture callback.
    private void initOrientationListener(@NonNull Context context) {
        mOrientationListener = new OrientationEventListener(context, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                // This is a method called frequently, which is bad for performance.
                if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
                    return;
                }

                if (orientation > 340 || orientation < 10) {
                    orientation = 0;
                } else if (orientation > 80 && orientation < 100) {
                    orientation = 90;
                } else if (orientation > 170 && orientation < 190) {
                    orientation = 180;
                } else if (orientation > 260 && orientation < 280) {
                    orientation = 270;
                } else {
                    return;
                }

                orientation = (orientation + 45) / 90 * 90;

                if (mDeviceOrientation != orientation) {
                    // 不能在此处更改mDisplayOrientation，屏幕旋转可能还未生效
                    Logger.d(TAG, "Orientation Changed! displayOrientation: " + mDeviceOrientation + "->" + orientation);
                    mDeviceOrientation = orientation;
                }
            }
        };

        if (mOrientationListener.canDetectOrientation()) {
            Logger.d(TAG, "Can detect orientation");
            mOrientationListener.enable();
        } else {
            Logger.d(TAG, "Cannot detect orientation");
            mOrientationListener.disable();
        }
    }

    private void destroyOrientationListener() {
        if (mOrientationListener != null) {
            mOrientationListener.disable();
            mOrientationListener = null;
        }
    }

    private int getDisplayOrientation() {
        int displayOrientation = 0;
        if (mContext != null) {
            int angle = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
            switch (angle) {
                case Surface.ROTATION_0:
                    displayOrientation = 0;
                    break;
                case Surface.ROTATION_90:
                    displayOrientation = 90;
                    break;
                case Surface.ROTATION_180:
                    displayOrientation = 180;
                    break;
                case Surface.ROTATION_270:
                    displayOrientation = 270;
                    break;
                default:
                    break;
            }
        }
        return displayOrientation;
    }

    private void updateSettings() {
        synchronized (mCmdLock) {
            List<Object> tmpCmdList = new LinkedList<>(mCmdList);
            mCmdList.clear();

            // 更新缓存的配置
            for (Object item : tmpCmdList) {
                if (item instanceof SetBeautyTypeCmd) {
                    SetBeautyTypeCmd info = (SetBeautyTypeCmd) item;
                    setBeautyType(info.type, info.enable);
                } else if (item instanceof SetBeautyParamCmd) {
                    SetBeautyParamCmd info = (SetBeautyParamCmd) item;
                    setBeautyParams(info.type, info.value);
                } else if (item instanceof SetFilterParamsCmd) {
                    SetFilterParamsCmd info = (SetFilterParamsCmd) item;
                    setFilterParams(info.path);
                } else if (item instanceof SetMaterialParamsCmd) {
                    SetMaterialParamsCmd info = (SetMaterialParamsCmd) item;
                    if (info.add) {
                        setMaterialParams(info.path);
                    } else {
                        removeMaterialParams(info.path);
                    }
                } else if (item instanceof SetFaceShapeCmd) {
                    SetFaceShapeCmd info = (SetFaceShapeCmd) item;
                    setFaceShapeParams(info.type, info.value);
                } else if (item instanceof MakeupImageCmd) {
                    MakeupImageCmd info = (MakeupImageCmd) item;
                    setMakeupParams(info.type, info.path);
                }
            }
        }
    }

    // 如果引擎未创建或者不是texture线程，先缓存设置
    private boolean isCurrentTextureThread(Object cmd) {
        long currentThreadId = Thread.currentThread().getId();
        if (mMediaChainEngine != null && glThreadId == currentThreadId) {
            return true;
        }

        Logger.w(TAG, "now not in texture thread " + glThreadId + ", " + currentThreadId);
        synchronized (mCmdLock) {
            mCmdList.add(cmd);
        }
        return false;
    }

    private static class SetBeautyTypeCmd {
        public int type;
        public boolean enable;
    }

    private static class SetBeautyParamCmd {
        public int type;
        public float value;
    }

    private static class SetFilterParamsCmd {
        public String path;
    }

    private static class SetMaterialParamsCmd {
        public String path;
        // true:添加；false:移除
        public boolean add;
    }

    private static class SetFaceShapeCmd {
        public int type;
        public float value;
    }

    private static class MakeupImageCmd {
        public int type;
        public String path;
    }
}