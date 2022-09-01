package com.aliyun.vpaas.standard.ecommerce.custommessage

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONException
import com.aliyun.roompaas.chat.exposable.ChatService
import com.aliyun.roompaas.roombase.Const
import com.aliyun.standard.liveroom.lib.LivePrototype

/**
 * 该类用于承载自定义消息的逻辑
 *
 * @author puke
 * @version 2022/5/12
 */
class CustomMessages {

    companion object {
        /**
         * 去购买
         */
        private const val ACTION_TO_BUY = "toBuy"

        /**
         * 送礼物
         */
        private const val ACTION_SEND_GIFT = "sendGift"

        /**
         * 关注
         */
        private const val ACTION_FOLLOW = "follow"

        /**
         * 上架商品
         */
        private const val ACTION_UPDATE_GOODS = "updateGoods"

        /**
         * 发送自定义消息
         */
        fun doSend(chatService: ChatService, message: BaseCustomMessage) {
            message.userId = Const.getCurrentUserId()
            message.userNick = LivePrototype.getInstance().openLiveParam.nick
            message.action = when (message) {
                is ToBuyMessage -> ACTION_TO_BUY
                is SendGiftMessage -> ACTION_SEND_GIFT
                is FollowMessage -> ACTION_FOLLOW
                is UpdateGoodsMessage -> ACTION_UPDATE_GOODS
                else -> return
            }

            val messageBody = JSON.toJSONString(message)
            chatService.sendCustomMessageToAll(messageBody, null)
        }

        /**
         * 解析收到的消息
         */
        fun parseMessage(body: String?): BaseCustomMessage? {
            body?.run {
                try {
                    val json = JSON.parseObject(body)
                    json?.run {
                        getString("action")?.run {
                            val type = when (this) {
                                ACTION_TO_BUY -> ToBuyMessage::class.java
                                ACTION_SEND_GIFT -> SendGiftMessage::class.java
                                ACTION_FOLLOW -> FollowMessage::class.java
                                ACTION_UPDATE_GOODS -> UpdateGoodsMessage::class.java
                                else -> null
                            }
                            type?.run {
                                return json.toJavaObject(type)
                            }
                        }
                    }
                } catch (e: JSONException) {
                }
            }
            return null
        }
    }
}