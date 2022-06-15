package com.aliyun.vpaas.standard.enterprise.custommessage

import java.io.Serializable

/**
 * @author puke
 * @version 2022/5/19
 */
abstract class BaseCustomMessage : Serializable {

    /**
     * 消息类型
     */
    var action: String? = null

    /**
     * 用户Id
     */
    var userId: String? = null

    /**
     * 用户昵称
     */
    var userNick: String? = null
}