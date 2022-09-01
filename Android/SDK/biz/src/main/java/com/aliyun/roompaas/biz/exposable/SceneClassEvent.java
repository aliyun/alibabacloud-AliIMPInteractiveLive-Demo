package com.aliyun.roompaas.biz.exposable;

import java.io.Serializable;

/**
 * Created by KyleCe on 2021/10/28
 */
public interface SceneClassEvent extends Serializable {
    String CLASS_STARTED = "StartClass";
    String CLASS_STOPPED = "StopClass";
}
