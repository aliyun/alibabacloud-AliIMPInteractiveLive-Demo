package com.aliyun.vpaas.standard.ecommerce.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.aliyun.roompaas.base.util.CommonUtil
import com.aliyun.vpaas.standard.ecommerce.R

/**
 * 底部: 直播输入框组件
 *
 * @author puke
 * @version 2022/5/9
 */
class LiveInputView(context: Context, attrs: AttributeSet?) :
    com.aliyun.standard.liveroom.lib.component.view.LiveInputView(context, attrs) {

    init {
        commentInput.textSize = 14f
        commentInput.setText(R.string.live_input_tips)
    }
}