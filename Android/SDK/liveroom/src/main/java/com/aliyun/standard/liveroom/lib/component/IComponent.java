package com.aliyun.standard.liveroom.lib.component;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.annotation.Nullable;

import com.alibaba.dingpaas.room.RoomDetail;
import com.aliyun.standard.liveroom.lib.LiveContext;

/**
 * @author puke
 * @version 2021/7/28
 */
public interface IComponent {

    /**
     * 组件初始化
     *
     * @param liveContext 直播间上下文
     */
    void onInit(LiveContext liveContext);

    /**
     * 进入房间成功
     *
     * @param roomDetail 房间详情信息
     */
    void onEnterRoomSuccess(RoomDetail roomDetail);

    /**
     * 进入房间失败
     *
     * @param errorMsg 失败信息
     */
    void onEnterRoomError(String errorMsg);

    /**
     * Activity 生命周期-onActivityResume
     */
    void onActivityResume();

    /**
     * Activity 生命周期-onActivityPause
     */
    void onActivityPause();

    /**
     * Activity 生命周期-onActivityDestroy
     */
    void onActivityDestroy();

    /**
     * Activity 生命周期-onActivityFinish
     */
    void onActivityFinish();

    /**
     * {@link Activity#onConfigurationChanged} 回调事件
     */
    void onActivityConfigurationChanged(Configuration newConfig);

    /**
     * Activity的onActivityResult回调事件
     *
     * @param requestCode 请求码
     * @param resultCode  结果码
     * @param data        回调数据
     */
    void onActivityResult(int requestCode, int resultCode, @Nullable Intent data);

    /**
     * 拦截手机物理返回键事件
     *
     * @return true: 拦截; false: 不拦截;
     */
    boolean interceptBackKey();

    /**
     * @return 组件顺序
     */
    int getOrder();
}
