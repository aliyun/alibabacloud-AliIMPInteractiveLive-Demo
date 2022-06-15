package com.aliyun.vpaas.standard.ecommerce.view

import android.content.Context
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import com.aliyun.roompaas.base.util.CommonUtil
import com.aliyun.standard.liveroom.lib.Actions
import com.aliyun.standard.liveroom.lib.component.BaseComponent
import com.aliyun.standard.liveroom.lib.component.ComponentHolder
import com.aliyun.standard.liveroom.lib.component.IComponent
import com.aliyun.vpaas.standard.ecommerce.R

/**
 * 右上方: 直播礼物组件
 *
 * @author puke
 * @version 2022/4/18
 */
class LiveGiftView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
    ComponentHolder {

    private val component = Component()
    private val gift: ImageView;

    init {
        inflate(context, R.layout.live_gift_view, this)
        gift = findViewById<ImageView>(R.id.view_gift).apply {
            setOnClickListener {
                val goodsCardShow = tag == true
                if (goodsCardShow) {
                    component.postEvent(Actions.HIDE_GOODS_CARD)
                } else {
                    component.postEvent(Actions.SHOW_GOODS_CARD)
                }
                tag = !goodsCardShow
            }
        }

        visibility = GONE
        // doAnimation()
    }

    private fun doAnimation() {
        val doOnceAnimation = { scale: Float, duration: Long, endAction: () -> Unit ->
            gift.animate()
                .setDuration(duration)
                .setInterpolator(LinearInterpolator())
                .scaleX(scale)
                .scaleY(scale)
                .withEndAction { endAction() }
                .start()
        }

        doOnceAnimation(1.2f, 300) {
            doOnceAnimation(.8f, 500) {
                doOnceAnimation(1f, 300) {
                    doAnimation()
                }
            }
        }
    }

    override fun getComponent(): IComponent {
        return component
    }

    private inner class Component : BaseComponent() {

    }
}