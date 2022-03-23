package com.aliyun.roompaas.app.request;

import com.aliyun.roompaas.app.model.RoomModel;

import java.io.Serializable;
import java.util.List;

/**
 * @author puke
 * @version 2021/10/29
 */
public class RoomListResponse implements Serializable {

    /**
     * 该appId下总共房间数
     */
    public int totalCount;

    /**
     * 该pageSize大小下，可以查询多少页
     */
    public int pageTotal;

    /**
     * 是否还有下一页
     */
    public boolean hasMore;

    /**
     * 房间列表数据
     */
    public List<RoomModel> liveList;
}
