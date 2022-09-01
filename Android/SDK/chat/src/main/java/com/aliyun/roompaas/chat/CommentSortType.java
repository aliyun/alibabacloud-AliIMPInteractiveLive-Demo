package com.aliyun.roompaas.chat;

/**
 * 弹幕排序方式
 *
 * @author puke
 * @version 2021/5/20
 */
public enum CommentSortType {

    /**
     * 已发布出去的版本有bug, 但为了兼容老版本, 不做API移除操作, 仅添加过期标
     */
    @Deprecated
    TIME_ASC(0),

    /**
     * 同 {@link #TIME_ASC}
     */
    @Deprecated
    TIME_DESC(1),

    /**
     * 按时间升序排列
     */
    ASC_BY_TIME(1),

    /**
     * 按时间降序排列
     */
    DESC_BY_TIME(0),
    ;

    private final int value;

    CommentSortType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
