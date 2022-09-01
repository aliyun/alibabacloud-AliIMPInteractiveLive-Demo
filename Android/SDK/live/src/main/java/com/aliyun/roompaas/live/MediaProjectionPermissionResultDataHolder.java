package com.aliyun.roompaas.live;

import android.content.Intent;

/**
 * @author puke
 * @version 2022/8/1
 */
public class MediaProjectionPermissionResultDataHolder {

    private static Intent mediaProjectionPermissionResultData;

    public static Intent getMediaProjectionPermissionResultData() {
        return mediaProjectionPermissionResultData;
    }

    public static void setMediaProjectionPermissionResultData(Intent mediaProjectionPermissionResultData) {
        MediaProjectionPermissionResultDataHolder.mediaProjectionPermissionResultData = mediaProjectionPermissionResultData;
    }
}
