package com.aliyun.roompaas.live.exposable.model;

import java.io.Serializable;

/**
 * @author puke
 * @version 2021/8/19
 */
public class LiveInfoModel implements Serializable {

    /**
     * 标题
     */
    public String title;

    /**
     * 简介
     */
    public String introduction;

    /**
     * 封面
     */
    public String coverUrl;

    /**
     * 自定义字段
     */
    public String userDefineField;
}
