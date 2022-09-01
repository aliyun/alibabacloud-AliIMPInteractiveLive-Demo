package com.aliyun.roompaas.base.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 有序的任务执行器<hr/>
 * 1. 提供将一系列操作按照预置的优先级执行的能力<br/>
 * 2. 首次执行需要手动调用{@link #invoke}方法<br/>
 * 3. 一旦达到执行过一次(不重置时), 后续不再需要手动调用{@link #invoke}, 提交即执行<br/>
 * 4. 重置会回到初始状态<br/>
 * 5. 非线程安全, 要在同一线程调度<br/>
 *
 * @author puke
 * @version 2022/4/27
 */
public class OrderlyTaskInvoker {

    private static final int DEFAULT_ORDER = 0;

    private final Map<Integer, List<Runnable>> order2Tasks = new ConcurrentHashMap<>();
    private boolean invoked = false;

    /**
     * 提交一个任务
     *
     * @param task 待执行的任务
     */
    public void submitTask(Runnable task) {
        submitTask(DEFAULT_ORDER, task);
    }

    /**
     * 提交一个任务
     *
     * @param order 优先级 (越小越靠前)
     * @param task  待执行的任务
     */
    public void submitTask(int order, Runnable task) {
        if (task == null) {
            // 空值保护
            return;
        }

        if (invoked) {
            // 执行过之后, 直接调用
            task.run();
            return;
        }

        // 未执行过, 任务暂存下来, 等待外部的执行指令
        List<Runnable> tasks = order2Tasks.get(order);
        if (tasks == null) {
            tasks = new ArrayList<>();
            order2Tasks.put(order, tasks);
        }
        tasks.add(task);
    }

    /**
     * 开始执行
     */
    public void invoke() {
        if (invoked) {
            return;
        }

        invoked = true;
        List<Integer> orders = new ArrayList<>(order2Tasks.keySet());
        Collections.sort(orders);
        for (Integer order : orders) {
            List<Runnable> tasks = order2Tasks.get(order);
            if (CollectionUtil.isNotEmpty(tasks)) {
                for (Runnable task : tasks) {
                    task.run();
                }
            }
        }
        order2Tasks.clear();
    }
    
    public void reset() {
        invoked = false;
        order2Tasks.clear();
    }
}
