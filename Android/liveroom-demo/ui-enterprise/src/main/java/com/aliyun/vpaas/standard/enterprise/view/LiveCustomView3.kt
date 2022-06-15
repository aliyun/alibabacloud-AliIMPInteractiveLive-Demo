package com.aliyun.vpaas.standard.enterprise.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.aliyun.standard.liveroom.lib.component.BaseComponent
import com.aliyun.standard.liveroom.lib.component.ComponentHolder
import com.aliyun.standard.liveroom.lib.component.IComponent

/**
 * @author puke
 * @version 2022/6/6
 */
class LiveCustomView3(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
    ComponentHolder {

    private val component = Component()

    init {
        TextView(context).run {
            setTextColor(Color.parseColor("#333333"))
            textSize = 30f
            text = "自定义视图3"
            gravity = Gravity.CENTER
            addView(this, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        }
    }

    private class Component : BaseComponent() {

    }

    override fun getComponent(): IComponent {
        return component
    }
}