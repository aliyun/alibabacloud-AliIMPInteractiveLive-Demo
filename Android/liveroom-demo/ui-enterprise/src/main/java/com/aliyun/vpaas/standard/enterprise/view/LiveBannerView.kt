package com.aliyun.vpaas.standard.enterprise.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.aliyun.roompaas.base.util.CommonUtil
import com.aliyun.standard.liveroom.lib.component.BaseComponent
import com.aliyun.standard.liveroom.lib.component.ComponentHolder
import com.aliyun.standard.liveroom.lib.component.IComponent
import com.aliyun.vpaas.standard.enterprise.R

/**
 * @author puke
 * @version 2022/6/6
 */
class LiveBannerView(context: Context, attrs: AttributeSet?) : RelativeLayout(context, attrs),
    ComponentHolder {

    private val component: Component = Component()

    init {
        inflate(context, R.layout.ep_live_banner_view, this)
        setOnClickListener {
            component.showToast("跳转广告详情页")
        }
    }

    private inner class Component : BaseComponent() {

        @SuppressLint("SwitchIntDef")
        override fun onActivityConfigurationChanged(newConfig: Configuration?) {
            when (newConfig?.orientation) {
                Configuration.ORIENTATION_PORTRAIT -> {
                    // 竖屏
                    visibility = VISIBLE
                }
                Configuration.ORIENTATION_LANDSCAPE -> {
                    // 横屏
                    visibility = GONE
                }
            }
        }
    }

    override fun getComponent(): IComponent {
        return component
    }
}