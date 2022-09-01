package com.aliyun.roompaas.biz.exposable.event;

import java.io.Serializable;
import java.util.Map;

/**
 * 进出房间事件
 *
 * @author puke
 * @version 2021/5/17
 */
public class RoomInOutEvent implements Serializable {

    /**
     * 进入/退出
     */
    public boolean enter;

    /**
     * 用户昵称
     */
    public String nick;

    /**
     * 在线人数
     */
    public int onlineCount;

    /**
     * 用户id
     */
    public String userId;

    /**
     * 用户浏览量
     */
    public int uv;

    /**
     * 用户浏览量
     */
    public int pv;

    /**
     * 用户拓展信息
     */
    public Map<String, String> extension;
}
