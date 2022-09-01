package com.aliyun.vpaas.standard.enterprise

import com.aliyun.standard.liveroom.lib.LiveHook
import com.aliyun.vpaas.standard.enterprise.component.LiveActivityConfigurationComponent

/**
 * 企业直播样式的直播样板间实现
 *
 * @author puke
 * @version 2022/4/18
 */
class LiveHook4EnterPrise : LiveHook() {

    init {
        // 添加纯逻辑组件
        this.addComponentSlots(
            { LiveActivityConfigurationComponent() },
        )

        // 自定义xml布局
        this.liveLayoutRes = R.layout.ep_activity_live_4_enterprise
    }
}