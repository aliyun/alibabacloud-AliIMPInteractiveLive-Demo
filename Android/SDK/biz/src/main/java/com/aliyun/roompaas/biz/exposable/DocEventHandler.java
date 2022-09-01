package com.aliyun.roompaas.biz.exposable;

/**
 * Created by KyleCe on 2021/12/15
 */
public interface DocEventHandler {
    /**
     * 弃用
     * 文档转换任务状态同步
     *
     * @param status 0成功 -1失败
     * @see #onDocConversionTaskStatus(int, String, String)
     */
    @Deprecated
    void onDocConversionTaskStatus(int status);

    /**
     * 文档转换任务状态同步
     *
     * @param status      0成功 -1失败
     * @param sourceDocId       源文档 doc id
     * @param targetDocId 转码目标 doc id
     */
    void onDocConversionTaskStatus(int status, String sourceDocId, String targetDocId);
}
