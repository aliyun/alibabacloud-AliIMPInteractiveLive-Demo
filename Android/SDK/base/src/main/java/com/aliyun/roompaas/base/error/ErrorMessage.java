package com.aliyun.roompaas.base.error;

/**
 * @author puke
 * @version 2021/9/28
 */
public interface ErrorMessage {

    String NOT_LOGIN = "当前未登录";

    String MSG_STR_CLASS_NOT_EXIST = "无效课堂ID，请检查输入是否正确、与创建者AppId是否一致";
    String MSG_STR_CLASS_CREATOR_UID_NOT_MATCH = "class creator uid not match";
    String MSG_STR_NETWORK_DOWN = "network down";

    String getMessage();
}
