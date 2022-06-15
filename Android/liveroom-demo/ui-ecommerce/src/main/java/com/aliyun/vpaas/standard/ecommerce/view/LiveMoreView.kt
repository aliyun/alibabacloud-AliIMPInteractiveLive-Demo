package com.aliyun.vpaas.standard.ecommerce.view

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import com.alibaba.fastjson.JSON
import com.aliyun.roompaas.chat.SampleChatEventHandler
import com.aliyun.roompaas.chat.exposable.event.CustomMessageEvent
import com.aliyun.roompaas.roombase.Const
import com.aliyun.roompaas.uibase.util.AppUtil
import com.aliyun.roompaas.uibase.util.BottomSheetDialogUtil
import com.aliyun.standard.liveroom.lib.LiveContext
import com.aliyun.standard.liveroom.lib.component.BaseComponent
import com.aliyun.standard.liveroom.lib.component.ComponentHolder
import com.aliyun.standard.liveroom.lib.component.IComponent
import com.aliyun.vpaas.standard.ecommerce.custommessage.CustomMessages
import com.aliyun.vpaas.standard.ecommerce.R
import com.aliyun.vpaas.standard.ecommerce.custommessage.SendGiftMessage
import com.aliyun.vpaas.standard.ecommerce.custommessage.UpdateGoodsMessage
import com.aliyun.vpaas.standard.ecommerce.util.GiftAnimationUtil
import com.aliyun.vpaas.standard.ecommerce.util.UserNickUtil

/**
 * 底部: 直播更多操作组件
 *
 * @author puke
 * @version 2022/5/9
 */
class LiveMoreView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
    ComponentHolder {

    companion object {
        const val SEND_GIFT_TYPE_FLOWER = "flower"
        const val SEND_GIFT_TYPE_BOAT = "boat"
        const val SEND_GIFT_TYPE_PLANE = "plane"
        const val SEND_GIFT_TYPE_ROCKET = "rocket"
    }

    private val component = Component()

    init {
        setBackgroundResource(R.drawable.icon_more)
        setOnClickListener {
            BottomSheetDialogUtil.create(context, R.layout.layout_more_function).run {
                // 送礼物
                findViewById<View>(R.id.send_gift)?.setOnClickListener {
                    dismiss()
                    showGiftPanel()
                }

                // 上架商品
                findViewById<View>(R.id.update_goods)?.run {
                    visibility = if (component.isOwner) VISIBLE else GONE
                    setOnClickListener {
                        dismiss()
                        component.updateGoods()
                    }
                }
                show()
            }
        }
    }

    private fun showGiftPanel() {
        BottomSheetDialogUtil.create(context, R.layout.layout_gift_list).run {
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

        fun sendGiftType(type: String) {
            // 做动画
//            GiftAnimationUtil.showAnimation(activity, type)

            // 发消息
            SendGiftMessage().apply {
                this.type = type
                CustomMessages.doSend(chatService, this)
            }
        }

        fun updateGoods() {
            UpdateGoodsMessage().run {
                showSeconds = 5
                goodsDetail = UpdateGoodsMessage.GoodsDetail().apply {
                    goodsId = "111"
                    goodsImageUrl =
                        "https://gw.alicdn.com/imgextra/i4/O1CN011pKnYH1CQqqxe2pRi_!!6000000000076-2-tps-780-243.png"
                }
                CustomMessages.doSend(chatService, this)
            }
        }
    }

    override fun getComponent(): IComponent {
        return component
    }
}