package com.aliyun.roompaas.app.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.Uri;

import com.aliyun.roompaas.app.App;

/**
 * 剪切板工具类, 提供复制、粘贴功能
 *
 * @author puke
 * @version 2018/5/24
 */
public class ClipboardUtil {


    /**
     * 复制Uri
     */
    public static void copyUri(Uri uri) {
        if (uri == null) {
            return;
        }
        ClipboardManager clipboardManager = (ClipboardManager) App.getApplication().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboardManager != null) {
            clipboardManager.setPrimaryClip(ClipData.newRawUri(null, uri));
        }
    }


    /**
     * 复制文本
     */
    public static void copyText(String text) {
        ClipboardManager clipboardManager = (ClipboardManager) App.getApplication().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboardManager != null) {
            if (text == null) {
                text = "";
            }
            clipboardManager.setPrimaryClip(ClipData.newPlainText(null, text));
        }
    }

    /**
     * @return 获取剪切板文本
     */
    public static String getClipText() {
        ClipboardManager clipboardManager = (ClipboardManager) App.getApplication().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboardManager != null) {
            ClipData clip = clipboardManager.getPrimaryClip();
            if (clip != null && clip.getItemCount() > 0) {
                return clip.getItemAt(0).coerceToText(App.getApplication()).toString();
            }
            return null;
        }
        return "";
    }

}
