package com.aliyun.vpaas.standard.enterprise.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.aliyun.roompaas.chat.SampleChatEventHandler
import com.aliyun.roompaas.chat.exposable.event.CustomMessageEvent
import com.aliyun.roompaas.live.SampleLiveEventHandler
import com.aliyun.roompaas.live.exposable.event.LiveCommonEvent
import com.aliyun.standard.liveroom.lib.Actions
import com.aliyun.standard.liveroom.lib.LiveContext
import com.aliyun.standard.liveroom.lib.component.BaseComponent
import com.aliyun.standard.liveroom.lib.component.ComponentHolder
import com.aliyun.standard.liveroom.lib.component.IComponent
import com.aliyun.vpaas.standard.enterprise.R
import com.aliyun.vpaas.standard.enterprise.custommessage.CustomMessages
import com.aliyun.vpaas.standard.enterprise.custommessage.SendGiftMessage
import com.aliyun.vpaas.standard.enterprise.dialog.FixLandscapeBottomSheetDialog
import com.aliyun.vpaas.standard.enterprise.util.GiftAnimationUtil
import com.aliyun.vpaas.standard.enterprise.util.UserNickUtil


/**
 * 底部: 直播礼物🎁组件
 *
 * @author puke
 * @version 2022/5/9
 */
class LiveGiftView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
    ComponentHolder {

    companion object {
        const val SEND_GIFT_TYPE_FLOWER = "flower"
        const val SEND_GIFT_TYPE_BOAT = "boat"
        const val SEND_GIFT_TYPE_PLANE = "plane"
        const val SEND_GIFT_TYPE_ROCKET = "rocket"
    }

    private val component = Component()

    init {
        setBackgroundResource(R.drawable.ep_icon_gift_selector)
        setOnClickListener {
            showGiftPanel()
        }
    }

    private fun showGiftPanel() {
        FixLandscapeBottomSheetDialog(
            context
        ).run {
            val view = inflate(context, R.layout.ep_layout_gift_list, null)
            setContentView(view)

            // 去除Dialog默认白色
            // 参: https://www.jianshu.com/p/8151403b4a19
            val parent = view.parent
            if (parent is ViewGroup) {
                parent.setBackgroundColor(Color.TRANSPARENT)
            }

            // 送鲜花
            findViewById<View>(R.id.send_flower)?.setOnClickListener {
                component.sendGiftType(SEND_GIFT_TYPE_FLOWER)
                dismiss()
            }

            // 送游艇
            findViewById<View>(R.id.send_boat)?.setOnClickListener {
                component.sendGiftType(SEND_GIFT_TYPE_BOAT)
                dismiss()
            }

            // 送飞机
            findViewById<View>(R.id.send_plane)?.setOnClickListener {
                component.sendGiftType(SEND_GIFT_TYPE_PLANE)
                dismiss()
            }

            // 送火箭
            findViewById<View>(R.id.send_rocket)?.setOnClickListener {
                component.sendGiftType(SEND_GIFT_TYPE_ROCKET)
                dismiss()
            }
            show()
        }
    }


    private inner class Component : BaseComponent() {

        override fun onInit(liveContext: LiveContext?) {
            super.onInit(liveContext)
            chatService.addEventHandler(object : SampleChatEventHandler() {
                override fun onCustomMessageReceived(event: CustomMessageEvent?) {
                    // 收到自定义消息
                    CustomMessages.parseMessage(event?.data)?.run {
                        when (this) {
                            is SendGiftMessage -> {
                                // 送礼物
                                val handledUserNick = UserNickUtil.handleUserNick(userNick)
                                GiftAnimationUtil.showAnimation(activity, type)
                                showToast("${handledUserNick}为主播送来了礼物~~")
                            }
                            else -> {
                            }
                        }
                    }
                }
            })

            liveService.addEventHandler(object : SampleLiveEventHandler() {
                override fun onLiveStarted(event: LiveCommonEvent?) {
                    isEnabled = true
                }
            })
        }

        fun sendGiftType(type: String) {
            // 发消息
            SendGiftMessage().run {
                this.type = type
                CustomMessages.doSend(chatService, this)
            }
        }

        override fun onEvent(action: String?, vararg args: Any?) {
            when (action) {
                Actions.GET_LIVE_DETAIL_SUCCESS -> {
                    val liveNotStarted = liveService?.liveDetail?.liveInfo?.status ?: 0 == 0
                    if (liveNotStarted) {
                        isEnabled = false
                    }
                }
            }
        }
    }

    override fun getComponent(): IComponent {
        return component
    }
}