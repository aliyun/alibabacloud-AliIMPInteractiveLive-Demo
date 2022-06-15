package com.aliyun.vpaas.standard.ecommerce

import android.content.Context
import android.view.View
import com.aliyun.standard.liveroom.lib.LiveHook
import com.aliyun.standard.liveroom.lib.ViewSlot
import com.aliyun.vpaas.standard.ecommerce.component.JumpToGoodsDetailComponent
import com.aliyun.vpaas.standard.ecommerce.view.LiveGoodsCardView

/**
 * 电商样式的直播样板间实现
 *
 * @author puke
 * @version 2022/4/18
 */
class LiveHook4Ecommerce : LiveHook() {

    init {
        // 添加纯逻辑组件
        this.addComponentSlots(
            { JumpToGoodsDetailComponent() },
        )

        // 添加商品卡片组件
        this.goodsSlot = ViewSlot { context -> LiveGoodsCardView(context!!) }
        // 自定义xml布局
        this.liveLayoutRes = R.layout.activity_live_4_ecommerce
    }
}