package com.aliyun.vpaas.standard.enterprise.view

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.aliyun.roompaas.uibase.util.AppUtil
import com.aliyun.roompaas.uibase.util.immersionbar.ImmersionBar
import com.aliyun.standard.liveroom.lib.component.BaseComponent
import com.aliyun.standard.liveroom.lib.component.ComponentHolder
import com.aliyun.standard.liveroom.lib.component.IComponent
import com.aliyun.vpaas.standard.enterprise.R

/**
 * 直播渲染区域
 *
 * @author puke
 * @version 2022/6/6
 */
class LiveRenderLayout(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
    ComponentHolder {

    private val component = Component()
    private val liveInfoView: View

    init {
        inflate(context, R.layout.ep_live_render_layout, this)
        liveInfoView = findViewById(R.id.render_live_info_view)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 宽高比
        val height = if (component.isLandscape)
            (AppUtil.getScreenRealHeight() - ImmersionBar.getStatusBarHeight(context as Activity))
        else (AppUtil.getScreenRealWidth() * 9 / 16)
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY))
    }

    private inner class Component : BaseComponent() {
        override fun onActivityConfigurationChanged(newConfig: Configuration?) {
            when (newConfig?.orientation) {
                Configuration.ORIENTATION_PORTRAIT -> {
                    // 竖屏
                    liveInfoView.visibility = VISIBLE
                }
                Configuration.ORIENTATION_LANDSCAPE -> {
                    // 横屏
                    liveInfoView.visibility = GONE
                }
            }
        }
    }

    override fun getComponent(): IComponent {
        return component
    }
}