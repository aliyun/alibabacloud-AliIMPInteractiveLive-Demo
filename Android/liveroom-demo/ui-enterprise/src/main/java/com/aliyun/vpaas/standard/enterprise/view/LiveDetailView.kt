package com.aliyun.vpaas.standard.enterprise.view

import android.content.Context
import android.graphics.text.TextRunShaper
import android.text.TextUtils
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import com.alibaba.dingpaas.room.RoomDetail
import com.alibaba.dingpaas.scenelive.SceneGetLiveDetailRsp
import com.aliyun.roompaas.base.exposable.Callback
import com.aliyun.roompaas.biz.RoomEngine
import com.aliyun.standard.liveroom.lib.LiveContext
import com.aliyun.standard.liveroom.lib.component.BaseComponent
import com.aliyun.standard.liveroom.lib.component.ComponentHolder
import com.aliyun.standard.liveroom.lib.component.IComponent
import com.aliyun.standard.liveroom.lib.wrapper.SampleRoomEventHandlerExtends
import com.aliyun.vpaas.standard.enterprise.R
import com.bumptech.glide.Glide

/**
 * 第2项Tab: 直播信息
 *
 * @author puke
 * @version 2022/6/6
 */
class LiveDetailView(context: Context, attrs: AttributeSet?) : ScrollView(context, attrs),
    ComponentHolder {

    private val component = Component()
    private val avatar: ImageView
    private val anchorNick: TextView
    private val anchorIntroduction: TextView
    private val liveIntroduction: TextView

    init {
        overScrollMode = OVER_SCROLL_NEVER
        inflate(context, R.layout.ep_viewpager_detail, this)
        avatar = findViewById(R.id.detail_avatar)
        anchorNick = findViewById(R.id.detail_anchor_nick)
        anchorIntroduction = findViewById(R.id.detail_anchor_introduction)
        liveIntroduction = findViewById(R.id.detail_live_introduction)
    }

    private inner class Component : BaseComponent() {
        override fun onInit(liveContext: LiveContext?) {
            super.onInit(liveContext)
            roomChannel.addEventHandler(object : SampleRoomEventHandlerExtends() {
                override fun onLiveRoomExtensionChanged(extension: MutableMap<String, String>?) {
                    anchorIntroduction.text = extension?.get("anchorIntroduction")
                    val liveIntroductionContent = extension?.get("liveIntroduction")
                    if (TextUtils.isEmpty(liveIntroductionContent)) {
                        liveIntroduction.visibility = GONE
                    } else {
                        liveIntroduction.visibility = VISIBLE
                        liveIntroduction.text = liveIntroductionContent
                    }
                    extension?.get("anchorAvatarURL")?.run {
                        Glide.with(context).load(this).centerCrop().into(avatar)
                    }
                }
            })
        }

        override fun onEnterRoomSuccess(roomDetail: RoomDetail?) {
            super.onEnterRoomSuccess(roomDetail)
            val liveId = liveService?.instanceId
            if (!TextUtils.isEmpty(liveId)) {
                val sceneLive = RoomEngine.getInstance().roomSceneLive?.value
                sceneLive?.getLiveDetail(liveId, object : Callback<SceneGetLiveDetailRsp> {
                    override fun onSuccess(data: SceneGetLiveDetailRsp?) {
                        anchorNick.text = data?.anchorNick
                    }

                    override fun onError(errorMsg: String?) {
                    }
                })
            }
        }

        override fun onEvent(action: String?, vararg args: Any?) {
        }
    }

    override fun getComponent(): IComponent {
        return component
    }
}