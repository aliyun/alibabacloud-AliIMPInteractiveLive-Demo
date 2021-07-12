package com.aliyun.roompaas.app.viewmodel.inter;

import android.view.View;

import com.aliyun.roompaas.base.exposable.Callback;


/**
 * Created by KyleCe on 2021/5/27
 */
public interface IWhiteBoardOperate {

    String getRoomId();

    void openWhiteBoard(Callback<View> callback);
}
