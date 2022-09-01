package com.aliyun.roompaas.rtc;

import com.aliyun.roompaas.base.util.OrderlyTaskInvoker;

/**
 * 基于{@link OrderlyTaskInvoker}进行封装的rtc回调辅助类<hr/>
 * 旨在解决rtc连麦链路对外的回调时, 先回调online事件, 再回调其他事件
 *
 * @author puke
 * @version 2022/4/27
 */
class RtcCallbackHelper {

    private final OrderlyTaskInvoker orderlyTaskInvoker = new OrderlyTaskInvoker();

    public void notifyOnline() {
        orderlyTaskInvoker.invoke();
    }

    public void invokeAfterOnline(Runnable task) {
        orderlyTaskInvoker.submitTask(task);
    }
}
