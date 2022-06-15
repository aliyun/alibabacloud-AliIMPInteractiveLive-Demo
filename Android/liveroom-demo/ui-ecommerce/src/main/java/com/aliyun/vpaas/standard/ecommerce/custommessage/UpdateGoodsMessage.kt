package com.aliyun.vpaas.standard.ecommerce.custommessage

import com.alibaba.fastjson.annotation.JSONField

/**
 * 上架商品消息
 *
 * @author puke
 * @version 2022/5/19
 */
class UpdateGoodsMessage : BaseCustomMessage() {

    /**
     * 展示时长
     */
    var showSeconds: Int = 10

    /**
     * 商品内容
     */
    var goodsDetail: GoodsDetail? = null

    class GoodsDetail {
        @JSONField(name = "goods_id")
        var goodsId: String? = "001"

        @JSONField(name = "goods_image_url")
        var goodsImageUrl: String? =
            "https://gw.alicdn.com/imgextra/i4/O1CN011pKnYH1CQqqxe2pRi_!!6000000000076-2-tps-780-243.png"
    }
}