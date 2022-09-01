package com.aliyun.roompaas.base.log;

import android.support.annotation.Nullable;

/**
 * @author puke
 * @version 2021/5/27
 */
public interface LoggerHandler {

    void log(LogLevel level, String tag, String msg, @Nullable Throwable e);
}
