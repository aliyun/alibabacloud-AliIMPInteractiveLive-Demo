package com.aliyun.vpaas.standard.enterprise.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.alibaba.dingpaas.room.RoomDetail
import com.aliyun.roompaas.live.SampleLiveEventHandler
import com.aliyun.roompaas.live.exposable.event.LiveCommonEvent
import com.aliyun.roompaas.uibase.util.AppUtil
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
 * 直播全屏时渲染区域上方的整个布局
 *
 * @author puke
 * @version 2022/6/6
 */
class LiveLandscapeLayout(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
    ComponentHolder {

    private val component = Component()
    private val title: TextView

    init {
        inflate(context, R.layout.ep_live_landscape_layout, this)
        title = findViewById(R.id.landscape_title)

        setOnClickListener {
            if (alpha == 0f) {
                alpha = 1f
            } else {
                alpha = 0f
            }
        }

        findViewById<View>(R.id.landscape_back).setOnClickListener {
            component.isLandscape = false
        }
    }

    private inner class Component : BaseComponent() {
        override fun onEnterRoomSuccess(roomDetail: RoomDetail?) {
            title.text = roomDetail?.roomInfo?.title
        }

        override fun onActivityConfigurationChanged(newConfig: Configuration?) {
            when (newConfig?.orientation) {
                Configuration.ORIENTATION_PORTRAIT -> {
                    // 竖屏
                    visibility = GONE
                }
                Configuration.ORIENTATION_LANDSCAPE -> {
                    // 横屏
                    visibility = VISIBLE
                }
            }
        }

        override fun interceptBackKey(): Boolean {
            if (isLandscape) {
                isLandscape = false
                return true
            }
            return super.interceptBackKey()
        }

        override fun getOrder(): Int {
            return -1
        }
    }

    override fun getComponent(): IComponent {
        return component
    }
}