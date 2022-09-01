package com.aliyun.roompaas.base.log;

import android.support.annotation.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * 支持组合的日志处理器
 * 
 * @author puke
 * @version 2021/9/8
 */
public class ComposeLoggerHandler implements LoggerHandler {

    private final List<LoggerHandler> loggerHandlers;

    public ComposeLoggerHandler(LoggerHandler... loggerHandlers) {
        this.loggerHandlers = Arrays.asList(loggerHandlers);
    }

    @Override
    public void log(LogLevel level, String tag, String msg, @Nullable Throwable e) {
        for (LoggerHandler loggerHandler : loggerHandlers) {
            loggerHandler.log(level, tag, msg, e);
        }
    }
}
