package com.aliyun.roompaas.app.request;

import java.util.Map;

/**
 * @author puke
 * @version 2021/5/14
 */
public class CreateRoomRequest extends BaseReq {

    public String domain;
    public String bizType;
    public String templateId;
    public String title;
    public String notice;
    public String ownerId;

    @Override
    public void appendParams(Map<String, String> params) {
        params.put("domain", this.domain);
        params.put("bizType", this.bizType);
        params.put("templateId", this.templateId);
        params.put("title", this.title);
        params.put("notice", this.notice);
        params.put("ownerId", this.ownerId);
    }
}
