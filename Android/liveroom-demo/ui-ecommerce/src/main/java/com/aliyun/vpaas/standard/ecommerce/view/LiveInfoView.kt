package com.aliyun.vpaas.standard.ecommerce.view

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.alibaba.dingpaas.room.RoomDetail
import com.alibaba.dingpaas.scenelive.SceneGetLiveDetailRsp
import com.aliyun.roompaas.base.exposable.Callback
import com.aliyun.roompaas.biz.RoomEngine
import com.aliyun.roompaas.biz.SampleRoomEventHandler
import com.aliyun.roompaas.biz.exposable.event.RoomInOutEvent
import com.aliyun.roompaas.chat.SampleChatEventHandler
import com.aliyun.roompaas.chat.exposable.event.CustomMessageEvent
import com.aliyun.roompaas.roombase.Const
import com.aliyun.standard.liveroom.lib.Actions
import com.aliyun.standard.liveroom.lib.LiveContext
import com.aliyun.standard.liveroom.lib.component.BaseComponent
import com.aliyun.standard.liveroom.lib.component.ComponentHolder
import com.aliyun.standard.liveroom.lib.component.IComponent
import com.aliyun.standard.liveroom.lib.wrapper.SampleRoomEventHandlerExtends
import com.aliyun.vpaas.standard.ecommerce.R
import com.aliyun.vpaas.standard.ecommerce.custommessage.CustomMessages
import com.aliyun.vpaas.standard.ecommerce.custommessage.FollowMessage
import com.aliyun.vpaas.standard.ecommerce.model.Message
import com.bumptech.glide.Glide

/**
 * 左上角: 直播信息组件
 *
 * @author puke
 * @version 2022/5/7
 */
class LiveInfoView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
    ComponentHolder {

    private val component = Component()
    private val avatar: ImageView
    private val title: TextView
    private val desc: TextView
    private val follow: TextView

    init {
        inflate(context, R.layout.live_info_view, this)
        avatar = findViewById(R.id.live_info_avatar)
        title = findViewById(R.id.live_info_title)
        desc = findViewById(R.id.live_info_desc)
        follow = findViewById(R.id.live_info_follow)
        follow.setOnClickListener {
            it.visibility = GONE
            component.handleFollow()
        }
    }

    private inner class Component : BaseComponent() {

        override fun onInit(liveContext: LiveContext?) {
            super.onInit(liveContext)
            // 主播不显示关注
            follow.visibility = if (isOwner) GONE else VISIBLE

            roomChannel.addEventHandler(object : SampleRoomEventHandlerExtends() {
                override fun onLiveRoomExtensionChanged(extension: MutableMap<String, String>?) {
                    extension?.get("anchorAvatarURL")?.run {
                        Glide.with(context).load(this).into(avatar)
                    }
                }

                override fun onEnterOrLeaveRoom(event: RoomInOutEvent?) {
                    setViewCount(event?.pv)
                }
            })

            chatService.addEventHandler(object : SampleChatEventHandler() {
                override fun onCustomMessageReceived(event: CustomMessageEvent?) {
                    CustomMessages.parseMessage(event?.data)?.run {
                        val myUserId = Const.getCurrentUserId()
                        if (this is FollowMessage && TextUtils.equals(userId, myUserId)) {
                            // 自己关注后, 做隐藏逻辑 (兼容底部点击"我也关注")
                            follow.visibility = GONE
                        }
                    }
                }
            })
        }

        override fun onEnterRoomSuccess(roomDetail: RoomDetail?) {
            setViewCount(roomDetail?.roomInfo?.pv)
        }

        @SuppressLint("SetTextI18n")
        private fun setViewCount(pv: Int?) {
            pv?.run { desc.text = "$pv 观看" }
        }

        override fun onEvent(action: String?, vararg args: Any?) {
            when (action) {
                Actions.GET_LIVE_DETAIL_SUCCESS -> {
                    liveService?.liveDetail?.liveInfo?.run {
                        this@LiveInfoView.title.text = title
                    }
                }
            }
        }

        fun handleFollow() {
            CustomMessages.doSend(chatService, FollowMessage())
        }
    }

    override fun getComponent(): IComponent {
        return component
    }
}