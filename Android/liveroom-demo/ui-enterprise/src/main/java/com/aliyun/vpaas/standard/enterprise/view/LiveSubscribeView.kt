package com.aliyun.vpaas.standard.enterprise.view

import android.app.Dialog
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import com.aliyun.roompaas.base.util.CommonUtil
import com.aliyun.vpaas.standard.enterprise.R
import java.util.regex.Pattern


/**
 * 底部: 预约组件
 *
 * @author puke
 * @version 2022/5/9
 */
class LiveSubscribeView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    init {
        inflate(context, R.layout.ep_live_subscribe_view, this)
        findViewById<Button>(R.id.subscribe_submit)?.run {
            setOnClickListener { showDialog(this) }
        }
    }

    private fun showDialog(button: Button) {
        Dialog(context).run {
            setCancelable(true)
            setCanceledOnTouchOutside(true)
            requestWindowFeature(Window.FEATURE_NO_TITLE)

            setContentView(R.layout.ep_subscribe_dialog)

            window?.run {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                attributes?.run {
                    width = LayoutParams.MATCH_PARENT
                    height = LayoutParams.WRAP_CONTENT
                }
            }

            val input = findViewById<EditText>(R.id.dialog_input)

            findViewById<View>(R.id.dialog_action_confirm).setOnClickListener {
                val phone = input.text.toString().trim()
                if (TextUtils.isEmpty(phone)) {
                    CommonUtil.showToast(context, "手机号不能为空")
                    return@setOnClickListener
                }

                if (!Pattern.matches("^1[3456789]\\d{9}$", phone)) {
                    CommonUtil.showToast(context, "请输入正确的手机号格式")
                    return@setOnClickListener
                }

                CommonUtil.showToast(context, "预约成功")
                button.run {
                    isEnabled = false
                    text = "已经预约"
                    setBackgroundResource(R.drawable.ep_bg_live_subscribe_disabled)
                }
                dismiss()
            }

            findViewById<View>(R.id.dialog_action_cancel).setOnClickListener {
                dismiss()
            }

            show()
        }
    }
}