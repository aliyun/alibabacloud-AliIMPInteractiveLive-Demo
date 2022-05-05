package com.aliyun.roompaas.app.model;

import java.io.Serializable;
import java.util.List;

public class ClassListResponse implements Serializable {

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
    public List<ClassBean> classList;
}
