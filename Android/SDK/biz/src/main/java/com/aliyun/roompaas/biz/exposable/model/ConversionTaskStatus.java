package com.aliyun.roompaas.biz.exposable.model;

import java.io.Serializable;

/**
 * Created by KyleCe on 2021/12/13
 */
public class ConversionTaskStatus implements Serializable {
    public String status;
    public String sourceDocId;
    public String targetDocId;
}
