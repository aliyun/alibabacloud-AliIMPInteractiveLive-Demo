package com.aliyun.standard.liveroom.lib;

import android.content.Intent;
import android.support.annotation.CallSuper;

import com.alibaba.dingpaas.monitorhub.MonitorhubBizType;
import com.aliyun.roompaas.base.log.Logger;
import com.aliyun.roompaas.biz.exposable.enums.LiveStatus;
import com.aliyun.roompaas.chat.exposable.ChatService;
import com.aliyun.roompaas.live.exposable.LiveService;
import com.aliyun.roompaas.roombase.BaseRoomActivity;

import java.util.Map;

/**
 * 房间Activity, 封装房间相关的通用处理逻辑
 *
 * @author puke
 * @version 2021/5/27
 */
public abstract class BaseLiveActivity extends BaseRoomActivity {

    private static final String TAG = BaseLiveActivity.class.getSimpleName();
    protected String BIZ_TYPE = MonitorhubBizType.STANDARD_LIVE;

    protected LivePrototype.Role role;
    protected LiveStatus liveStatus;
    protected String liveId;
    protected Map<String, String> extension;
    protected boolean supportLinkMic;

    protected ChatService chatService;
    protected LiveService liveService;

    @Override
    protected void parseParams(Intent intent) {
        setBizType(BIZ_TYPE);
        super.parseParams(intent);
        LiveInnerParam pageParam = (LiveInnerParam) intent.getSerializableExtra(LiveConst.PARAM_KEY_LIVE_INNER_PARAM);
        role = pageParam.role;
        liveStatus = pageParam.liveStatus;
        liveId = pageParam.liveId;
        extension = pageParam.extension;
        supportLinkMic = pageParam.supportLinkMic;

//        role = LivePrototype.Role.ofValue(intent.getStringExtra(LiveConst.PARAM_KEY_ROLE));
//        liveStatus = LiveStatus.of(intent.getIntExtra(LiveConst.PARAM_KEY_STATUS, LiveStatus.NOT_START.value));
//        liveId = intent.getStringExtra(LiveConst.PARAM_KEY_LIVE_ID);
    }

    @Override
    protected String[] parsePermissionArray() {
        if (role == LivePrototype.Role.ANCHOR) {
            // 主播
            return LiveConst.PERMISSIONS_4_ANCHOR;
        } else {
            // 观众
            if (supportLinkMic) {
                // 连麦直播
                return LiveConst.PERMISSIONS_4_AUDIENCE_OF_LINK_MIC;
            } else {
                // 普通直播
                return LiveConst.PERMISSIONS_4_AUDIENCE;
            }
        }
    }

    @CallSuper
    protected void init() {
        super.init();
        if (roomChannel == null) {
            Logger.e(TAG, "roomChannel null ");
            return;
        }

        // 获取插件服务
        chatService = roomChannel.getPluginService(ChatService.class);
        liveService = roomChannel.getPluginService(LiveService.class);
    }
}
