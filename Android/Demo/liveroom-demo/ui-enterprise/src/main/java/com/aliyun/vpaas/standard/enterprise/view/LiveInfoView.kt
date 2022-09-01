package com.aliyun.vpaas.standard.enterprise.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.text.TextUtils
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.alibaba.dingpaas.room.RoomDetail
import com.alibaba.dingpaas.scenelive.SceneGetLiveDetailRsp
import com.aliyun.roompaas.base.exposable.Callback
import com.aliyun.roompaas.biz.RoomEngine
import com.aliyun.roompaas.biz.exposable.event.RoomInOutEvent
import com.aliyun.standard.liveroom.lib.LiveContext
import com.aliyun.standard.liveroom.lib.component.BaseComponent
import com.aliyun.standard.liveroom.lib.component.ComponentHolder
import com.aliyun.standard.liveroom.lib.component.IComponent
import com.aliyun.standard.liveroom.lib.wrapper.SampleRoomEventHandlerExtends
import com.aliyun.vpaas.standard.enterprise.R
import com.bumptech.glide.Glide

/**
 * 直播信息组件
 *
 * @author puke
 * @version 2022/5/7
 */
class LiveInfoView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
    ComponentHolder {

    private val component = Component()
    private val avatar: ImageView
    private val nick: TextView
    private val desc: TextView

    init {
        inflate(context, R.layout.ep_live_info_view, this)
        avatar = findViewById(R.id.live_info_avatar)
        nick = findViewById(R.id.live_info_nick)
        desc = findViewById(R.id.live_info_desc)
    }

    private inner class Component : BaseComponent() {

        override fun onInit(liveContext: LiveContext?) {
            super.onInit(liveContext)
            roomChannel.addEventHandler(object : SampleRoomEventHandlerExtends() {
                override fun onLiveRoomExtensionChanged(extension: MutableMap<String, String>?) {
                    extension?.get("anchorAvatarURL")?.run {
                        Glide.with(context).load(this).centerCrop().into(avatar)
                    }
                }

                override fun onEnterOrLeaveRoom(event: RoomInOutEvent?) {
                    setViewCount(event?.pv)
                }
            })
        }

        override fun onEnterRoomSuccess(roomDetail: RoomDetail?) {
            setViewCount(roomDetail?.roomInfo?.pv)

            val liveId = liveService?.instanceId
            if (!TextUtils.isEmpty(liveId)) {
                val sceneLive = RoomEngine.getInstance().roomSceneLive?.value
                sceneLive?.getLiveDetail(liveId, object : Callback<SceneGetLiveDetailRsp> {
                    override fun onSuccess(data: SceneGetLiveDetailRsp?) {
                        this@LiveInfoView.nick.text = data?.anchorNick
                    }

                    override fun onError(errorMsg: String?) {
                    }
                })
            }
        }

        @SuppressLint("SetTextI18n")
        private fun setViewCount(pv: Int?) {
            pv?.run { desc.text = "$pv 观看" }
        }
    }

    override fun getComponent(): IComponent {
        return component
    }
}