package com.aliyun.roompaas.base.log;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 文件级别的日志处理器
 *
 * @author puke
 * @version 2021/9/8
 */
public class FileLoggerHandler implements LoggerHandler {

    private static final String LOG_DIR_NAME = "AliImpLog";
    private static final String TAG = FileLoggerHandler.class.getSimpleName();
    private static final Executor executor = Executors.newSingleThreadExecutor();

    private final Context context;
    private final File logFile;

    public FileLoggerHandler(Context context) {
        this.context = context;
        File logDir = new File(Environment.getExternalStorageDirectory(), LOG_DIR_NAME);
        Date now = new Date();
        Locale locale = Locale.getDefault();
        String subDir = new SimpleDateFormat("yyyy-MM-dd", locale).format(now);
        String logFilename = new SimpleDateFormat("HH:mm:ss", locale).format(now);
        String logFilePostfix = ".log";
        File logFileDir = new File(logDir, subDir);
        logFile = new File(logFileDir, logFilename + logFilePostfix);
    }

    @Override
    public void log(final LogLevel level, final String tag, final String msg, @Nullable final Throwable e) {
        if (!checkLogFileWritable()) {
            return;
        }

        executor.execute(new Runnable() {
            @Override
            public void run() {
                // 构造日志信息
                String timestamp = new SimpleDateFormat(
                        "HH:mm:ss.SSS", Locale.getDefault()).format(new Date());
                String tagFirstChar = level.name().substring(0, 1);
                String handledMsg = msg == null ? null : msg.trim();
                String block = String.format("%s [%s] %s %s", timestamp, tagFirstChar, tag, handledMsg);

                // 构造异常信息
                if (e != null) {
                    StringBuilder builder = new StringBuilder(e.toString());
                    for (StackTraceElement element : e.getStackTrace()) {
                        builder.append("\tat ").append(element.toString());
                    }
                    block = block + "\n" + builder.toString();
                }

                // 添加到日志文件
                appendBlock(block);
            }
        });
    }

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    private void appendBlock(@NonNull final String block) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(logFile, true);
            fos.write((block + "\n").getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private boolean checkLogFileWritable() {
        if (!hasWriteLogPermission(context)) {
            return false;
        }

        if (logFile.exists()) {
            return true;
        }


        try {
            File logFileDir = logFile.getParentFile();
            if (logFileDir != null && !logFileDir.exists()) {
                if (!logFileDir.mkdirs()) {
                    Log.w(TAG, "create log dir failed.");
                }
            }
            return logFile.createNewFile();
        } catch (IOException ignored) {
        }
        return false;
    }

    /**
     * 判断是否有写Log的权限
     *
     * @param context 上下文
     * @returns 判断结果
     */
    public static boolean hasWriteLogPermission(Context context) {
        int result = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }
}
