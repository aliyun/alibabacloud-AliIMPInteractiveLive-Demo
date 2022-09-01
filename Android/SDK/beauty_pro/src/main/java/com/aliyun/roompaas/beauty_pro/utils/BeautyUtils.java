package com.aliyun.roompaas.beauty_pro.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.Surface;

import com.alibaba.fastjson.JSON;
import com.aliyun.roompaas.base.AppContext;
import com.aliyun.roompaas.beauty_common.QueenRuntime;
import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.model.BeautyInfo;
import com.taobao.android.libqueen.QueenEngine;
import com.taobao.android.libqueen.Texture2D;

import java.io.File;
import java.lang.ref.WeakReference;

public class BeautyUtils {

    public static Application sApplication = (Application) AppContext.getContext();

    private static final String DEFAULT_BEAUTY_PANEL = "beauty_panel.json";

    public static Context getAppContext() { return sApplication.getApplicationContext(); }

    public static Application getApplication() { return sApplication; }

    public static String getPkgName() { return getAppContext().getPackageName(); }

    private static BeautyInfo sBeautyInfo;
    public static BeautyInfo getDefaultBeautyInfo() {
        if (sBeautyInfo == null) {
            sBeautyInfo = createBeautyInfo(FileUtils.readAssetsFile(sApplication, DEFAULT_BEAUTY_PANEL));
        }
        return sBeautyInfo;
    }

    public static BeautyInfo createBeautyInfo(String json) {
        return JSON.parseObject(json, BeautyInfo.class);
    }

    public static int dip2px(float dpValue) {
        final float scale = sApplication.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static Bitmap decodeBitmapFromAssets(String fileName) {
        return BitmapUtils.decodeBitmapFromAssets(sApplication, fileName);
    }

    public static String captureScreen(Context context, int w, int h, int degree) {
        String capturePath = null;
        WeakReference<QueenEngine> weakReference = QueenRuntime.queenEngienRef;
        QueenEngine engine = weakReference != null ? weakReference.get() : null;
        if (engine != null) {
            Texture2D texture2D = new Texture2D(engine.getEngineHandler());
            texture2D.init(QueenRuntime.sCurTextureId, w, h, false);
//            String path = context.getExternalCacheDir().getPath();
            String path = FileUtils.getAlbumPath();
            String fileName = FileUtils.getCurrentTimePhotoFileName(BitmapUtils.IMAGE_FORMAT_PNG);
            String targetPath = path + File.separator + fileName;
//            boolean result = texture2D.saveToFile(targetPath, Bitmap.CompressFormat.PNG, 100);
            Bitmap srcBmp = texture2D.readToBitmap();
            Bitmap destBmp = BitmapUtils.bitmapFlipYAndRotate(srcBmp, degree);
            if (destBmp != null) {
                boolean result = BitmapUtils.saveToFile(destBmp, targetPath, Bitmap.CompressFormat.PNG, 100);
                if (result) {
                    capturePath = targetPath;
                    notifyMediaStore(context, targetPath);
                }
            }
        }
        return capturePath;
    }

    public static String captureScreen(Context context, Bitmap bitmap) {
        String capturePath = null;
        String path = FileUtils.getAlbumPath();
        String fileName = FileUtils.getCurrentTimePhotoFileName(BitmapUtils.IMAGE_FORMAT_JPG);
        String targetPath = path + File.separator + fileName;
        boolean result = BitmapUtils.saveToFile(bitmap, targetPath, Bitmap.CompressFormat.JPEG, 100);
        if (result) {
            capturePath = targetPath;
            notifyMediaStore(context, targetPath);
        }
        return capturePath;
    }

    private static void notifyMediaStore(Context context, String targetFile) {
        // 其次把文件插入到系统图库
        try {
//            MediaStore.Images.Media.insertImage(context.getContentResolver(),
//                    filePath, fileName, null);
            // 最后通知图库更新
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + targetFile)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static int getDegrees(final Activity activity) {
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
            default: break;
        }
        return degrees;
    }
}
