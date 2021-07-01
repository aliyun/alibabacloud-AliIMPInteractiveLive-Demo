package com.aliyun.room;

import com.aliyun.imp_room20210515.models.GetLoginTokenRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author puke
 * @version 2021/3/24
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GetTokenParams extends GetLoginTokenRequest.GetLoginTokenRequestRequestParams {

    private String appId;
}