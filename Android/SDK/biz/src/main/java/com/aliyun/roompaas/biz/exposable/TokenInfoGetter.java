package com.aliyun.roompaas.biz.exposable;

import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.biz.exposable.model.TokenInfo;

/**
 * @author puke
 * @version 2021/9/28
 */
public interface TokenInfoGetter {

    void getTokenInfo(String userId, Callback<TokenInfo> callback);
}
