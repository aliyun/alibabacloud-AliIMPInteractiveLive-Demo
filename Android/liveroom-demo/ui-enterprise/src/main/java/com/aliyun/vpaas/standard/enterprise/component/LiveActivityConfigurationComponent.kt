package com.aliyun.vpaas.standard.enterprise.component

import android.content.res.Configuration
import android.graphics.Color
import com.aliyun.roompaas.base.util.ThreadUtil
import com.aliyun.roompaas.uibase.util.BottomMarginAdapter
import com.aliyun.roompaas.uibase.util.ExStatusBarUtils
import com.aliyun.roompaas.uibase.util.HorizontalMarginAdapter
import com.aliyun.roompaas.uibase.util.immersionbar.ImmersionBar
import com.aliyun.standard.liveroom.lib.component.BaseComponent

/**
 * 适配横竖屏切换的UI逻辑 (顶部状态栏和底部Bar间距)
 *
 * @author puke
 * @version 2021/8/30
 */
class LiveActivityConfigurationComponent : BaseComponent() {
    override fun onActivityConfigurationChanged(newConfig: Configuration) {
        val targetLand = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE
        // AppUtil.adjustFullScreenOrNotForOrientation(activity.window, targetLand, !targetLand)

        if (targetLand) {
            // 添加适当延时, 解决状态栏不连贯问题
            ExStatusBarUtils.setStatusBarColor(activity, Color.BLACK, true)
        } else {
            ExStatusBarUtils.setStatusBarColor(activity, Color.WHITE)
        }

        if (liveContext != null) {
            HorizontalMarginAdapter.adjustIfVital(
                activity,
                liveContext.adjustBottomView,
                targetLand
            )
        }
    }
}