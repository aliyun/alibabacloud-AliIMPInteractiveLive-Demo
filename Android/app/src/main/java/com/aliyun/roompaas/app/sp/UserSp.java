package com.aliyun.roompaas.app.sp;

/**
 * @author puke
 * @version 2021/5/24
 */
@SpHelper.Sp
public interface UserSp {

    @SpHelper.Setter
    void setUserId(String userId);

    @SpHelper.Getter(defValue = "jianli")
    String getUserId();
}
