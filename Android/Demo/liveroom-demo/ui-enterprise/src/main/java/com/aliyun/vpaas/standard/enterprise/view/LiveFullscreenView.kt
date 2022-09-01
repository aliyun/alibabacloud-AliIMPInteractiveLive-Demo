package com.aliyun.vpaas.standard.enterprise.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.widget.ImageView
import com.aliyun.roompaas.uibase.util.AppUtil
import com.aliyun.standard.liveroom.lib.component.BaseComponent
import com.aliyun.standard.liveroom.lib.component.ComponentHolder
import com.aliyun.standard.liveroom.lib.component.IComponent
import com.aliyun.vpaas.standard.enterprise.R

/**
 * 切换全屏组件
 *
 * @author puke
 * @version 2022/6/10
 */
class LiveFullscreenView(context: Context?, attrs: AttributeSet?) :
    AppCompatImageView(context, attrs), ComponentHolder {

    private val component = Component()

    init {
        val padding = AppUtil.dp(1f)
        setPadding(padding, padding, padding, padding)
        setImageResource(R.drawable.icon_fullscreen_expand)

        setOnClickListener {
            component.isLandscape = true
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