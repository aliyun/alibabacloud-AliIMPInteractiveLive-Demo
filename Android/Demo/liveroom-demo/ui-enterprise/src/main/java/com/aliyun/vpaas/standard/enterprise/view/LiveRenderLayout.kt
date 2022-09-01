package com.aliyun.vpaas.standard.enterprise.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.aliyun.roompaas.live.SampleLiveEventHandler
import com.aliyun.roompaas.live.exposable.event.LiveCommonEvent
import com.aliyun.roompaas.uibase.util.AppUtil
import com.aliyun.roompaas.uibase.util.ExStatusBarUtils
import com.aliyun.standard.liveroom.lib.Actions
import com.aliyun.standard.liveroom.lib.LiveContext
import com.aliyun.standard.liveroom.lib.component.BaseComponent
import com.aliyun.standard.liveroom.lib.component.ComponentHolder
import com.aliyun.standard.liveroom.lib.component.IComponent
import com.aliyun.standard.liveroom.lib.wrapper.SampleRoomEventHandlerExtends
import com.aliyun.vpaas.standard.enterprise.R
import java.lang.NumberFormatException
import java.text.SimpleDateFormat
import java.util.*

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
            (AppUtil.getScreenHeight() - ExStatusBarUtils.getStatusBarHeight(context))
        else (AppUtil.getScreenWidth() * 9 / 16)
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