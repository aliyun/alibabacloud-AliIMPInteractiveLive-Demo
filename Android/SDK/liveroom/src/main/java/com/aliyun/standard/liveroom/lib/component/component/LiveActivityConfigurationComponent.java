package com.aliyun.standard.liveroom.lib.component.component;

import android.content.res.Configuration;
import android.support.annotation.Keep;

import com.aliyun.roompaas.uibase.util.AppUtil;
import com.aliyun.roompaas.uibase.util.HorizontalMarginAdapter;
import com.aliyun.standard.liveroom.lib.component.BaseComponent;

/**
 * @author puke
 * @version 2021/8/30
 */
@Keep
public class LiveActivityConfigurationComponent extends BaseComponent {

    @Override
    public void onActivityConfigurationChanged(Configuration newConfig) {
        boolean targetLand = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;
        AppUtil.adjustFullScreenOrNotForOrientation(activity.getWindow(), targetLand, !targetLand);
        if (!getOpenLiveParam().disableImmersive && liveContext != null) {
            HorizontalMarginAdapter.adjustIfVital(activity, liveContext.getAdjustBottomView(), targetLand);
        }
    }
}
