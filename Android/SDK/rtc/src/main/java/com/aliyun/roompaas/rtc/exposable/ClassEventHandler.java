package com.aliyun.roompaas.rtc.exposable;

/**
 * @author puke
 * @version 2021/7/2
 */
public interface ClassEventHandler {

    /**
     * 开始上课
     */
    void onClassStart();

    /**
     * 结束上课
     */
    void onClassStop();

    /**
     * 发布任务
     */
    void onTaskPublish();
}
