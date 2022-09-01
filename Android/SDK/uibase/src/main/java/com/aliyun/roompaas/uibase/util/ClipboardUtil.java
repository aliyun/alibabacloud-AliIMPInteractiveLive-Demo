package com.aliyun.roompaas.uibase.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.aliyun.roompaas.base.AppContext;

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
        ClipboardManager clipboardManager = (ClipboardManager) AppContext.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboardManager != null) {
            clipboardManager.setPrimaryClip(ClipData.newRawUri(null, uri));
        }
    }


    /**
     * 复制文本
     */
    public static int copyText(@Nullable String text) {
        text = TextUtils.isEmpty(text) ? "" : text;
        ClipboardManager clipboardManager = (ClipboardManager) AppContext.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboardManager != null) {
            clipboardManager.setPrimaryClip(ClipData.newPlainText(null, text));
            return 0;
        }

        return -1;
    }

    /**
     * @return 获取剪切板文本
     */
    public static String getClipText() {
        ClipboardManager clipboardManager = (ClipboardManager) AppContext.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboardManager != null) {
            ClipData clip = clipboardManager.getPrimaryClip();
            if (clip != null && clip.getItemCount() > 0) {
                return clip.getItemAt(0).coerceToText(AppContext.getContext()).toString();
            }
            return null;
        }
        return "";
    }

}
