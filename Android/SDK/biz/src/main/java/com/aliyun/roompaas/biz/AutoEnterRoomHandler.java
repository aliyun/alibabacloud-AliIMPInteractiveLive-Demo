package com.aliyun.roompaas.biz;

import com.alibaba.fastjson.JSON;
import com.aliyun.roompaas.base.callback.Callbacks;
import com.aliyun.roompaas.base.log.Logger;
import com.aliyun.roompaas.base.util.Utils;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自动进房间的处理器<hr>
 * 针对「服务端在长连接断开一段时间后自动移除房间」的场景做补偿
 *
 * @author puke
 * @version 2022/5/31
 */
public class AutoEnterRoomHandler {

    private static final String TAG = AutoEnterRoomHandler.class.getSimpleName();

    // {key: WeakReference<RoomChannel>}
    private static final Map<String, WeakReference<RoomChannelImpl>> KEY_2_CHANNELS = new ConcurrentHashMap<>();

    /**
     * enterRoom时, 记录当前在房间里的roomChannel
     */
    static void add(RoomChannelImpl roomChannel) {
        String key = getKey(roomChannel);
        Logger.i(TAG, "add, key=" + key);
        KEY_2_CHANNELS.put(key, new WeakReference<>(roomChannel));
    }

    /**
     * leaveRoom时, 移除记录的roomChannel
     */
    static void remove(RoomChannelImpl roomChannel) {
        String key = getKey(roomChannel);
        Logger.i(TAG, "remove, key=" + key);
        KEY_2_CHANNELS.remove(getKey(roomChannel));
    }

    /**
     * 长连接重连时, 对记录的roomChannel执行重新enterRoom操作
     */
    static void doEnterRoomIfNeed(String userId) {
        Logger.i(TAG, "doEnterRoomIfNeed, userId=" + userId);
        for (String key : KEY_2_CHANNELS.keySet()) {
            Logger.i(TAG, "doEnterRoomIfNeed, forEach key=" + key);
            if (key.startsWith(userId + "_")) {
                RoomChannelImpl roomChannel = Utils.getRef(KEY_2_CHANNELS.remove(key));
                if (roomChannel != null) {
                    // 执行进房间的操作
                    String nick = roomChannel.nick;
                    Map<String, String> extension = roomChannel.extension;
                    Logger.i(TAG, String.format(
                            "doEnterRoomIfNeed, forEach enterRoom key=%s, nick=%s, extension=%s",
                            key, nick, JSON.toJSONString(extension)
                    ));
                    Map<String, String> context = new HashMap<>();
                    context.put("enter_room_after_online_flag", "true");
                    roomChannel.enterRoomInternal(nick, extension, context,
                            new Callbacks.Log<Void>(TAG, "doEnterRoomIfNeed, callback " + key));
                }
            }
        }
    }

    private static String getKey(RoomChannelImpl roomChannel) {
        String userId = roomChannel.getUserId();
        String roomId = roomChannel.getRoomId();
        return String.format("%s_%s", userId, roomId);
    }
}
