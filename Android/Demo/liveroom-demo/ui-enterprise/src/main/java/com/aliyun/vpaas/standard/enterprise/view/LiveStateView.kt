package com.aliyun.vpaas.standard.enterprise.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.aliyun.roompaas.live.SampleLiveEventHandler
import com.aliyun.roompaas.live.exposable.event.LiveCommonEvent
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
 * @author puke
 * @version 2022/6/10
 */
class LiveStateView(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs),
    ComponentHolder {

    companion object {
        const val STATUS_NOT_STARTED = 0
        const val STATUS_LIVING = 1
        const val STATUS_END = 2
    }

    private val component = Component()

    private val liveStateText: TextView
    private val liveStateTips: TextView

    private var preStartTime: Long = 0

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        inflate(context, R.layout.ep_live_state_view, this)

        liveStateText = findViewById(R.id.render_live_state_text)
        liveStateTips = findViewById(R.id.render_live_state_tips)
    }

    private inner class Component : BaseComponent() {
        override fun onInit(liveContext: LiveContext?) {
            super.onInit(liveContext)
            roomChannel.addEventHandler(object : SampleRoomEventHandlerExtends() {
                override fun onLiveRoomExtensionChanged(extension: MutableMap<String, String>?) {
                    extension?.get("preStartTime")?.run {
                        try {
                            preStartTime = this.toLong()
                        } catch (e: NumberFormatException) {
                        }
                    }
                }
            })

            liveService?.addEventHandler(object : SampleLiveEventHandler() {
                override fun onLiveStarted(event: LiveCommonEvent?) {
                    // 直播开始时, 隐藏直播状态提示 (刚进入的时候没开播, 在页面内部时开播了)
                    visibility = GONE
                    liveStateTips.visibility = GONE
                }

                override fun onLiveStopped(event: LiveCommonEvent?) {
                    // 直播结束时, 显示直播状态提示
                    visibility = VISIBLE
                    liveStateText.text = "直播已结束"
                }
            })
        }

        @SuppressLint("SetTextI18n")
        override fun onEvent(action: String?, vararg args: Any?) {
            if (action == Actions.GET_LIVE_DETAIL_SUCCESS) {
                liveService?.liveDetail?.liveInfo?.let {
                    liveService?.liveDetail?.liveInfo?.status?.run {
                        when (this) {
                            STATUS_NOT_STARTED -> {
                                // 直播未开始时, 显示"直播尚未开始"的提示条
                                visibility = VISIBLE
                                liveStateText.text = "直播未开始"

                                if (preStartTime > 0) {
                                    val format = SimpleDateFormat(
                                        "yyyy-MM-dd HH:mm", Locale.getDefault()
                                    )
                                    val showDate = format.format(Date(preStartTime))
                                    liveStateTips.visibility = VISIBLE
                                    liveStateTips.text = "将于${showDate}开播"
                                }
                            }
                            STATUS_END -> {
                                // 直播已结束
                                visibility = VISIBLE
                                liveStateText.text = "直播已结束"
                            }
                        }
                    }
                }
            }
        }
    }

    override fun getComponent(): IComponent {
        return component
    }
}