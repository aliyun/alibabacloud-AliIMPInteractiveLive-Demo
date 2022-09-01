package com.aliyun.standard.liveroom.lib.sp;

import com.aliyun.roompaas.uibase.helper.SpHelper;

/**
 * @author puke
 * @version 2021/5/24
 */
@SpHelper.Sp
public interface EnvSp {

    @SpHelper.Getter()
    String getPreAppServer();

    @SpHelper.Setter
    void setPreAppServer(String preAppServer);

    @SpHelper.Getter(defValue = "ONLINE")
    String getEnv();

    @SpHelper.Setter
    void setEnv(String env);
}
