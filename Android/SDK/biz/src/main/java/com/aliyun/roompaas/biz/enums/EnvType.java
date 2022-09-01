package com.aliyun.roompaas.biz.enums;

import com.alibaba.dingpaas.base.DPSEnvType;
import com.aliyun.roompaas.biz.RoomConst;

/**
 * @author puke
 * @version 2021/5/24
 */
public enum EnvType {

    PRE(RoomConst.PRE_INFO, DPSEnvType.ENV_TYPE_PRE_RELEASE),
    ONLINE(RoomConst.ONLINE_INFO, DPSEnvType.ENV_TYPE_ONLINE),
    ;

    private final String value;
    private final DPSEnvType dpsEnvType;

    EnvType(String value, DPSEnvType dpsEnvType) {
        this.value = value;
        this.dpsEnvType = dpsEnvType;
    }

    public String getValue() {
        return value;
    }

    public DPSEnvType getDpsEnvType() {
        return dpsEnvType;
    }
}
