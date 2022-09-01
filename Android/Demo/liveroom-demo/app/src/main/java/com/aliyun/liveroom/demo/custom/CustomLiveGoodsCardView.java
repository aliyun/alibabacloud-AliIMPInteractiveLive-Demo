package com.aliyun.liveroom.demo.custom;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;

import com.aliyun.roompaas.uibase.util.AppUtil;

/**
 * @author puke
 * @version 2021/12/13
 */
public class CustomLiveGoodsCardView extends AppCompatTextView {

    public CustomLiveGoodsCardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        setBackgroundColor(Color.parseColor("#3300ff00"));
        setGravity(Gravity.CENTER);
        setText("商品卡片视图");
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);

        setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                AppUtil.dp(100)
        ));

        // 设置长按后的组件提示
        LongClickHelper.attach(this);
    }
}
