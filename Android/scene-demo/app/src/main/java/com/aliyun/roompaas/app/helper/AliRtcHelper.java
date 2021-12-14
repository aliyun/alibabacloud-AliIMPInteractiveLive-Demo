package com.aliyun.roompaas.app.helper;

import android.content.Context;

import com.alivc.rtc.AliRtcEngine;
import com.aliyun.roompaas.rtc.SurfaceViewUtil;

/**
 * Created by KyleCe on 2021/9/15
 */
public class AliRtcHelper {
    public static void fillCanvasViewIfNecessary(AliRtcEngine.AliRtcVideoCanvas aliVideoCanvas
            , Context context, boolean zOrderMediaOverlay) {
        if (aliVideoCanvas.view == null) {
            aliVideoCanvas.view = SurfaceViewUtil.generateSophonSurfaceView(context, zOrderMediaOverlay);
            aliVideoCanvas.renderMode = AliRtcEngine.AliRtcRenderMode.AliRtcRenderModeFill;
        }
    }

    public static AliRtcEngine.AliRtcVideoTrack interceptTrack(AliRtcEngine.AliRtcVideoTrack videoTrack) {
        return videoTrack == AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackBoth
                ? AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackScreen
                : videoTrack;
    }
}
