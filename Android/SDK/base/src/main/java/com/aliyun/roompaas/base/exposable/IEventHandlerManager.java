package com.aliyun.roompaas.base.exposable;

/**
 * @author puke
 * @version 2021/7/2
 */
public interface IEventHandlerManager<EH> {

    void addEventHandler(EH eventHandler);

    void removeEventHandler(EH eventHandler);

    void removeAllEventHandler();
}
