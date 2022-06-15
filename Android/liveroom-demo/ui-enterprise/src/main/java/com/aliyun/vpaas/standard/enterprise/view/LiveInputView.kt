package com.aliyun.vpaas.standard.enterprise.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import com.alibaba.dingpaas.chat.GetTopicInfoRsp
import com.aliyun.roompaas.live.SampleLiveEventHandler
import com.aliyun.roompaas.live.exposable.event.LiveCommonEvent
import com.aliyun.roompaas.uibase.util.ViewUtil
import com.aliyun.standard.liveroom.lib.Actions
import com.aliyun.standard.liveroom.lib.LiveContext
import com.aliyun.standard.liveroom.lib.component.BaseComponent
import com.aliyun.standard.liveroom.lib.component.IComponent
import com.aliyun.standard.liveroom.lib.component.MultiComponentHolder
import com.aliyun.standard.liveroom.lib.component.view.LiveInputView
import com.aliyun.vpaas.standard.enterprise.R

/**
 * @author puke
 * @version 2021/7/29
 */
class LiveInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : LiveInputView(context, attrs), MultiComponentHolder {

    companion object {
        const val TAG = "LiveInputView"
    }

    private val extendComponent: Component = Component()

    init {
        // 更改样式
        commentInput.setBackgroundResource(R.drawable.ep_bg_comment_input)
        ViewUtil.applyTextColor(commentInput, inputEnabledColor())
    }

    override fun updateMuteState(chatDetail: GetTopicInfoRsp?) {
        if (chatDetail != null
            && !chatDetail.muteAll
            && !chatDetail.mute
            && !extendComponent.isOwner
        ) {
            // 自定义未禁言时的样式设置
            setInputStyle(true, inputHintInDialog())
        } else {
            super.updateMuteState(chatDetail)
        }
    }

    override fun setInputStyle(enable: Boolean, hintRes: Int) {
        super.setInputStyle(enable, hintRes)
        commentInput.setTextColor(Color.parseColor(if (enable) "#333333" else "#999999"))
    }

    override fun inputDisabledColor(): Int {
        return R.color.ep_comment_input_hint_disabled
    }

    override fun inputEnabledColor(): Int {
        return R.color.ep_comment_input_hint_enabled
    }

    override fun inputHintInDialog(): Int {
        return R.string.ep_live_input_default_tips
    }

    private inner class Component : BaseComponent() {

        override fun onInit(liveContext: LiveContext) {
            super.onInit(liveContext)
            liveService.addEventHandler(object : SampleLiveEventHandler() {
                override fun onLiveStarted(event: LiveCommonEvent?) {
                    // 开播后, 允许点击
                    enableClick()
                }
            })
        }

        override fun onEvent(action: String, vararg args: Any) {
            when (action) {
                Actions.GET_LIVE_DETAIL_SUCCESS -> {
                    val liveNotStarted = liveService?.liveDetail?.liveInfo?.status ?: 0 == 0
                    if (liveNotStarted) {
                        // 开播前, 不允许点击
                        disableClick()
                    }
                }
            }
        }
    }

    override fun getComponents(): MutableList<IComponent> {
        // 添加拓展Component
        return mutableListOf(component, extendComponent)
    }
}