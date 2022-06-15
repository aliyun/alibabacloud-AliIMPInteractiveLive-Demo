package com.aliyun.vpaas.standard.ecommerce.model

import android.support.annotation.DrawableRes
import com.aliyun.vpaas.standard.ecommerce.R
import com.aliyun.vpaas.standard.ecommerce.util.UserLevel
import java.io.Serializable

/**
 * @author puke
 * @version 2022/5/19
 */
class FlyItem : Serializable {
    var content: String? = null

    var background: Int? = null

    var icon: Int? = null
}