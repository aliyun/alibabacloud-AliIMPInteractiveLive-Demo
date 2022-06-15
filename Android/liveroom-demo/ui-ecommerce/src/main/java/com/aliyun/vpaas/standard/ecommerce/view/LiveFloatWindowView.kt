package com.aliyun.vpaas.standard.ecommerce.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.aliyun.standard.liveroom.lib.LiveContext
import com.aliyun.standard.liveroom.lib.LivePrototype
import com.aliyun.standard.liveroom.lib.component.BaseComponent
import com.aliyun.standard.liveroom.lib.component.ComponentHolder
import com.aliyun.standard.liveroom.lib.component.IComponent
import com.aliyun.standard.liveroom.lib.component.view.LiveRenderView
import com.aliyun.standard.liveroom.lib.floatwindow.FloatWindowManager
import com.aliyun.standard.liveroom.lib.floatwindow.FloatWindowPermissionUtil
import com.aliyun.vpaas.standard.ecommerce.R

/**
 * 底部: 直播切小窗组件
 *
 * @author puke
 * @version 2022/5/9
 */
class LiveFloatWindowView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
    ComponentHolder {

    private val component = Component()

    init {
        setBackgroundResource(R.drawable.icon_floatwindow)
        setOnClickListener { component.handleFloatClick() }
    }

    private inner class Component : BaseComponent() {
        override fun onInit(liveContext: LiveContext?) {
            super.onInit(liveContext)
            visibility = if (liveContext?.role == LivePrototype.Role.ANCHOR) GONE else VISIBLE
        }

        fun handleFloatClick() {
            if (!FloatWindowManager.instance().isShowing) {
                // 检测悬浮窗权限
                FloatWindowPermissionUtil.checkPermission(activity) {
                    // 有权限时, 开始弹窗
                    component.postEvent(LiveRenderView.ACTION_SHOW_FLOAT_WINDOW)
                    component.activity.finish()
                }
            }
        }
    }

    override fun getComponent(): IComponent {
        return component
    }
}