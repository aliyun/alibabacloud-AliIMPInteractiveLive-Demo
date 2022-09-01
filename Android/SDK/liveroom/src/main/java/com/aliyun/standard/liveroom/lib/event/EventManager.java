package com.aliyun.standard.liveroom.lib.event;

import android.text.TextUtils;

import com.aliyun.roompaas.base.log.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * 事件管理器
 *
 * @author puke
 * @version 2021/7/30
 */
public class EventManager {

    private static final String TAG = EventManager.class.getSimpleName();

    private final List<EventListener> listeners = new ArrayList<>();

    public void post(String action, Object... args) {
        if (TextUtils.isEmpty(action)) {
            Logger.w(TAG, "Not allowed to post empty action.");
            return;
        }

        for (EventListener listener : listeners) {
            listener.onEvent(action, args);
        }
    }

    public void register(EventListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void unregister(EventListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    public void unregisterAll() {
        listeners.clear();
    }
}
