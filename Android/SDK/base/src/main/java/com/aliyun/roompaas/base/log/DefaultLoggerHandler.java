package com.aliyun.roompaas.base.log;

import android.util.Log;

import com.aliyun.roompaas.base.AppContext;
import com.aliyun.roompaas.base.util.CommonUtil;

/**
 * 默认日志处理器
 *
 * @author puke
 * @version 2021/5/27
 */
public class DefaultLoggerHandler implements LoggerHandler {
    private static final String MODULE = "RoomPaaS";

    private final boolean loggable;
    private static boolean sLoggable;

    /**
     * to turn on Log for release apk:
     1. input command line: adb shell setprop log.tag.RoomPaaS V
     2. relaunch app
     */
    public static final boolean LOG_IS_LOGGABLE = Log.isLoggable(MODULE, Log.VERBOSE);

    public DefaultLoggerHandler() {
        sLoggable = this.loggable = CommonUtil.isDebug(AppContext.getContext())/*Release包不做日志打印*/ || LOG_IS_LOGGABLE;
    }

    @Override
    public void log(LogLevel level, String tag, String msg, Throwable e) {
        if (!loggable) {
            return;
        }

        tag = "^^^" + tag;
        switch (level) {
            case DEBUG:
                if (e == null) {
                    Log.d(tag, msg);
                } else {
                    Log.d(tag, msg, e);
                }
                break;
            case INFO:
                if (e == null) {
                    Log.i(tag, msg);
                } else {
                    Log.i(tag, msg, e);
                }
                break;
            case WARN:
                if (e == null) {
                    Log.w(tag, msg);
                } else {
                    Log.w(tag, msg, e);
                }
                break;
            case ERROR:
                if (e == null) {
                    Log.e(tag, msg);
                } else {
                    Log.e(tag, msg, e);
                }
                break;
        }
    }

    public static boolean isLoggable(){
        return sLoggable;
    }
}
