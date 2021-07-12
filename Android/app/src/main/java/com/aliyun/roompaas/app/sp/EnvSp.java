package com.aliyun.roompaas.app.sp;

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
