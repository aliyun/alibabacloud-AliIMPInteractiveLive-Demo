package com.aliyun.vpaas.standard.ecommerce.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.v7.widget.AppCompatImageView
import android.view.ViewGroup
import com.aliyun.roompaas.base.util.ThreadUtil
import com.aliyun.roompaas.chat.SampleChatEventHandler
import com.aliyun.roompaas.chat.exposable.event.CustomMessageEvent
import com.aliyun.roompaas.uibase.util.AppUtil
import com.aliyun.standard.liveroom.lib.Actions
import com.aliyun.standard.liveroom.lib.LiveContext
import com.aliyun.standard.liveroom.lib.component.BaseComponent
import com.aliyun.standard.liveroom.lib.component.ComponentHolder
import com.aliyun.standard.liveroom.lib.component.IComponent
import com.aliyun.vpaas.standard.ecommerce.R
import com.aliyun.vpaas.standard.ecommerce.component.JumpToGoodsDetailComponent
import com.aliyun.vpaas.standard.ecommerce.custommessage.CustomMessages
import com.aliyun.vpaas.standard.ecommerce.custommessage.UpdateGoodsMessage
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition

/**
 * 弹幕和输入框之间: 直播商品卡片组件
 *
 * @author puke
 * @version 2022/4/18
 */
class LiveGoodsCardView(context: Context) : AppCompatImageView(context), ComponentHolder {

    private val component = Component()
    private val hideGoodsCardTask: Runnable

    init {
        setImageResource(R.drawable.sample_goods_card)
        adjustViewBounds = true

        layoutParams = ViewGroup.MarginLayoutParams(
            AppUtil.dp(260f),
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            leftMargin = AppUtil.dp(14f)
            bottomMargin = AppUtil.dp(16f)
        }

        setOnClickListener {
            // 跳转商品详情页
            component.postEvent(JumpToGoodsDetailComponent.ACTION_JUM_TO_GOODS_DETAIL)
        }

        hideGoodsCardTask = Runnable { component.postEvent(Actions.HIDE_GOODS_CARD) }
    }

    private inner class Component : BaseComponent() {
        override fun onInit(liveContext: LiveContext?) {
            super.onInit(liveContext)
            chatService.addEventHandler(object : SampleChatEventHandler() {
                override fun onCustomMessageReceived(event: CustomMessageEvent?) {
                    CustomMessages.parseMessage(event?.data)?.run {
                        if (this is UpdateGoodsMessage) {
                            handleUpdateGoodsMessage(this)
                        }
                    }
                }
            })
        }

        private fun handleUpdateGoodsMessage(message: UpdateGoodsMessage) {
            message.goodsDetail?.goodsImageUrl?.run {
                setImageResource(0)
                Glide.with(context).load(this).into(this@LiveGoodsCardView)
                component.postEvent(Actions.SHOW_GOODS_CARD)
                ThreadUtil.cancel(hideGoodsCardTask)
                ThreadUtil.postDelay(message.showSeconds * 1000L) {
                    component.postEvent(Actions.HIDE_GOODS_CARD)
                }
            }
        }

        override fun onActivityFinish() {
            ThreadUtil.cancel(hideGoodsCardTask)
        }
    }

    override fun getComponent(): IComponent {
        return component
    }
}