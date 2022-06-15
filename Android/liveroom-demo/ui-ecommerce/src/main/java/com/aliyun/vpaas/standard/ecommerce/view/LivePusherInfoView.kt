package com.aliyun.vpaas.standard.ecommerce.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.support.v7.widget.AppCompatTextView
import android.util.AttributeSet
import com.alibaba.fastjson.JSON
import com.aliyun.roompaas.base.util.CommonUtil
import com.aliyun.roompaas.base.util.DeviceInfo
import com.aliyun.roompaas.live.exposable.AliLiveMediaStreamOptions
import com.aliyun.roompaas.uibase.util.AppUtil
import com.aliyun.standard.liveroom.lib.BuildConfig
import com.aliyun.standard.liveroom.lib.LiveContext
import com.aliyun.standard.liveroom.lib.component.BaseComponent
import com.aliyun.standard.liveroom.lib.component.ComponentHolder
import com.aliyun.standard.liveroom.lib.component.IComponent
import com.aliyun.standard.liveroom.lib.wrapper.SampleRoomEventHandlerExtends

/**
 * 左中部: 主播端设备信息组件 (内部开发调试使用, 外部客户可忽略)
 *
 * @author puke
 * @version 2022/5/21
 */
class LivePusherInfoView(context: Context?, attrs: AttributeSet?) :
    AppCompatTextView(context, attrs), ComponentHolder {

    private val component = Component()

    init {
        setBackgroundColor(Color.parseColor("#33000000"))
        val padding = AppUtil.dp(6f)
        setPadding(padding, padding, padding, padding)
        setTextColor(Color.parseColor("#ffffff"))
        textSize = 12f
    }

    private inner class Component : BaseComponent() {
        @SuppressLint("SetTextI18n")
        override fun onInit(liveContext: LiveContext?) {
            super.onInit(liveContext)
            // 仅对debug包可见
            if (!CommonUtil.isDebug(context)) {
                visibility = GONE
                return
            }

            roomChannel.addEventHandler(object : SampleRoomEventHandlerExtends() {
                override fun onLiveRoomExtensionChanged(extension: MutableMap<String, String>?) {
                    extension?.get("pusherOptions")?.run {
                        JSON.parseObject(this, AliLiveMediaStreamOptions::class.java)?.run {
                            val deviceInfo =
                                JSON.parseObject(extension["deviceInfo"], DeviceInfo::class.java)

                            text =
                                "机型: ${extension["pusherDeviceBrand"]}-${extension["pusherDeviceModel"]}\n" +
                                        "Score: ${deviceInfo?.deviceScore}\n" +
                                        "Level: ${deviceInfo?.deviceLevel}\n" +
                                        "CpuCount: ${deviceInfo?.cpuCount}\n" +
                                        "CpuMHz: ${deviceInfo?.cpuMHz}\n" +
                                        "Memory: ${deviceInfo?.memory}\n" +
                                        "Android版本: ${extension["pusherDeviceVersion"]}\n" +
                                        "GOP: ${videoEncodeGop}\n" +
                                        "码率: ${videoBitrate}\n" +
                                        "FPS: ${fps}\n" +
                                        "分辨率: ${resolution}"
                        }
                    }
                }
            })
        }
    }

    override fun getComponent(): IComponent {
        return component
    }
}