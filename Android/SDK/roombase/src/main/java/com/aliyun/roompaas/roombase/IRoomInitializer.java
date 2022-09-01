package com.aliyun.roompaas.roombase;

import android.content.Context;
import android.support.annotation.Nullable;

import com.aliyun.roompaas.base.IDestroyable;
import com.aliyun.roompaas.base.exposable.Callback;

/**
 * Created by KyleCe on 2021/10/27
 */
public interface IRoomInitializer<P extends BizInitParam> extends IDestroyable {

    void injectConfig(Context context, P param);

    void init(Context context, P param, @Nullable Callback<Void> callback);

    void initAndLogin(Callback<Void> callback);

    void setBizType(String bizType);

}
