package com.aliyun.liveroom.demo.custom;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.aliyun.roompaas.base.util.CollectionUtil;
import com.aliyun.standard.liveroom.lib.MessageModel;
import com.aliyun.standard.liveroom.lib.component.view.LiveMessageView;

import java.util.ArrayList;
import java.util.List;

/**
 * 直播页信息面板 (位于页面左下角)
 *
 * @author puke
 * @version 2021/12/13
 */
public class CustomLiveMessageView extends LiveMessageView {

    public CustomLiveMessageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(Color.parseColor("#33ffff00"));

        // 自定义添加默认的消息
        List<MessageModel> messages = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            messages.add(new MessageModel(
                    "自定义组件", String.format("自定义组件的第%s条消息", i + 1)));
        }
        addMessageToPanel(messages);

        // 设置长按后的组件提示
        LongClickHelper.attach(this);
    }

    @Nullable
    @Override
    protected MessageModel getSystemAlertMessageModel() {
        // 隐藏信息面板默认「禁止吸烟酗酒」的那条消息
        return null;
    }

    @Override
    protected void addMessageToPanel(List<MessageModel> addedList) {
        // 通过更改数据模型改变弹幕信息的样式
        if (CollectionUtil.isNotEmpty(addedList)) {
            for (MessageModel message : addedList) {
                message.color = Color.RED;
            }
        }
        super.addMessageToPanel(addedList);
    }

    @Override
    protected boolean enableSystemLogic() {
        // 控制是否显示系统消息
        return true;
    }

    @Override
    protected boolean enableUnreadTipsLogic() {
        // 控制是否显示未读消息提示条
        return true;
    }
}
