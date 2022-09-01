package com.aliyun.vpaas.standard.ecommerce.view

import android.app.Dialog
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.aliyun.roompaas.base.util.CommonUtil
import com.aliyun.roompaas.uibase.util.BottomSheetDialogUtil
import com.aliyun.vpaas.standard.ecommerce.R


/**
 * 底部: 直播分享组件
 *
 * @author puke
 * @version 2022/5/9
 */
class LiveShareView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    private var dialog: Dialog? = null

    init {
        setBackgroundResource(R.drawable.icon_share)
        setOnClickListener {
            if (dialog == null) {
                dialog = BottomSheetDialogUtil.create(context, R.layout.layout_share_panel).apply {
                    findViewById<View>(R.id.share_panel)?.setOnClickListener {
                        CommonUtil.showToast(context, "Demo示例不支持分享哦~")
                    }
                    findViewById<View>(R.id.share_cancel)?.setOnClickListener {
                        dismiss()
                    }
                }
            }
            dialog?.show()
        }
    }
}