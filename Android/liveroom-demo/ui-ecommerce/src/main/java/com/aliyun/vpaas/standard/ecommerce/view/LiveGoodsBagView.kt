package com.aliyun.vpaas.standard.ecommerce.view

import android.app.Dialog
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.aliyun.roompaas.uibase.util.BottomSheetDialogUtil
import com.aliyun.standard.liveroom.lib.component.BaseComponent
import com.aliyun.standard.liveroom.lib.component.ComponentHolder
import com.aliyun.standard.liveroom.lib.component.IComponent
import com.aliyun.vpaas.standard.ecommerce.R
import com.aliyun.vpaas.standard.ecommerce.component.JumpToGoodsDetailComponent

/**
 * 左下角: 直播购物袋组件
 *
 * @author puke
 * @version 2022/5/7
 */
class LiveGoodsBagView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
    ComponentHolder {

    private val component = Component()
    private var dialog: Dialog? = null

    init {
        inflate(context, R.layout.live_goodsbag_view, this)
        setOnClickListener { component.handleClick() }
    }

    private inner class Component : BaseComponent() {

        fun handleClick() {
            if (dialog == null) {
                dialog = BottomSheetDialogUtil.create(context, R.layout.layout_goods_panel).apply {
                    findViewById<View>(R.id.goods_panel_close)?.setOnClickListener {
                        // 关闭面板
                        dismiss()
                    }

                    findViewById<View>(R.id.goods_panel_layout)?.setOnClickListener {
                        // 跳转商品详情页
                        postEvent(JumpToGoodsDetailComponent.ACTION_JUM_TO_GOODS_DETAIL)
                    }
                }
            }
            dialog?.run {
                if (isShowing) {
                    dismiss()
                } else {
                    show()
                }
            }
        }

        override fun interceptBackKey(): Boolean {
            dialog?.run {
                if (isShowing) {
                    dismiss()
                    return@interceptBackKey true
                }
            }
            return super.interceptBackKey()
        }

    }

    override fun getComponent(): IComponent {
        return component
    }
}