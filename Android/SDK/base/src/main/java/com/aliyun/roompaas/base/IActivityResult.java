package com.aliyun.roompaas.base;

import android.content.Intent;

/**
 * Created by KyleCe on 2021/8/26
 */
public interface IActivityResult {
    void onActivityResult(int requestCode, int resultCode, Intent data);
}
