package com.aliyun.standard.liveroom.lib;

import android.content.Intent;
import android.support.annotation.NonNull;

/**
 * @author puke
 * @version 2021/12/2
 */
public interface StartActivityCallback {

    void beforeStartActivity(@NonNull Intent intent);
}
