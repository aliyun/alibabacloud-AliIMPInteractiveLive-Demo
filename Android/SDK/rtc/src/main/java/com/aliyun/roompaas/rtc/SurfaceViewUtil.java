package com.aliyun.roompaas.rtc;

import android.content.Context;
import android.graphics.PixelFormat;

import org.webrtc.sdk.SophonSurfaceView;

/**
 * Created by KyleCe on 2021/8/15
 */
public class SurfaceViewUtil {

    public static SophonSurfaceView generateSophonSurfaceView(Context context, boolean zOrderMediaOverlay) {
        return generateSophonSurfaceView(context, false, zOrderMediaOverlay);
    }

    public static SophonSurfaceView generateSophonSurfaceView(Context context, boolean zOrderOnTop, boolean zOrderMediaOverlay) {
        return generateSophonSurfaceView(context, PixelFormat.TRANSLUCENT, zOrderOnTop, zOrderMediaOverlay);
    }

    public static SophonSurfaceView generateSophonSurfaceView(Context context, int format, boolean zOrderOnTop, boolean zOrderMediaOverlay) {
        SophonSurfaceView ssv = new SophonSurfaceView(context);
        ssv.getHolder().setFormat(format);

        // true 在最顶层，会遮挡一切view
        ssv.setZOrderOnTop(zOrderOnTop);

        // true 如已绘制SurfaceView则在surfaceView上一层绘制。
        ssv.setZOrderMediaOverlay(zOrderMediaOverlay);
        return ssv;
    }
}
