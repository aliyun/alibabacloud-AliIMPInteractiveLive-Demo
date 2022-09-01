package com.aliyun.vpaas.standard.enterprise.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.alibaba.dingpaas.room.RoomDetail
import com.aliyun.roompaas.uibase.util.ExStatusBarUtils
import com.aliyun.standard.liveroom.lib.component.BaseComponent
import com.aliyun.standard.liveroom.lib.component.ComponentHolder
import com.aliyun.standard.liveroom.lib.component.IComponent
import com.aliyun.vpaas.standard.enterprise.R

/**
 * 页面顶部导航栏
 *
 * @author puke
 * @version 2022/5/30
 */
class LiveNavigationView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
    ComponentHolder {

    private val component = Component()
    private val title: TextView

    init {
        inflate(context, R.layout.ep_live_navigation_view, this)
        findViewById<View>(R.id.navigation_back)?.setOnClickListener {
            component.handleBackClick()
        }
        title = findViewById(R.id.navigation_title)
    }

    override fun getComponent(): IComponent {
        return component
    }

    private inner class Component : BaseComponent() {
        override fun onEnterRoomSuccess(roomDetail: RoomDetail?) {
            super.onEnterRoomSuccess(roomDetail)
            title.text = roomDetail?.roomInfo?.title
        }

        fun handleBackClick() {
            activity.finish()
        }

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
}