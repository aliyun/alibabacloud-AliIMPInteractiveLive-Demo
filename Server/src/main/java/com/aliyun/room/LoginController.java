package com.aliyun.room;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.aliyun.imp_room20210515.Client;
import com.aliyun.imp_room20210515.models.*;
import com.aliyun.teaopenapi.models.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import com.aliyun.room.util.SignUtil;
import com.aliyun.room.util.HttpControllerUtil;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.Objects;
import java.util.Optional;

/**
 * @version 2021/3/24
 */
@Slf4j
@CrossOrigin()
@RestController
@RequestMapping("api/login")
public class LoginController {

    @Value("${paas.accessKey}")
    private String accessKey;

    @Value("${paas.secretKey}")
    private String secretKey;

    @Value("${paas.appId}")
    private String appId;

    @Value("${impServer.appServerUrl}")
    private String appServerUrl;

    @Value("${impServer.appSignSecret}")
    private String appSignSecret;

    private Client client;

    @PostConstruct
    private void init() throws Exception {
        // 1. 配置Config
        Config authConfig = new Config();
        authConfig.accessKeyId = accessKey;
        authConfig.accessKeySecret = secretKey;
        // 请指定线上配置域名
        authConfig.endpoint = "imp-room.aliyuncs.com";

        // 2. 实例化Client
        client = new Client(authConfig);
    }

    @RequestMapping(value = "getToken", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseResult<GetLoginTokenResponseBody.GetLoginTokenResponseBodyResult> getToken(GetTokenParams params, HttpServletRequest httpServletRequest) {
        log.info("getToken, params={}", JSON.toJSONString(params));
        HttpControllerUtil.logRequestInfo(httpServletRequest, "getToken");
        // 验证签名（可选，根据控制台设置的回调鉴权码进行鉴权，保证回调安全性）
        boolean verify = false;
        try {
            verify = SignUtil.isvVerifySign(appSignSecret, "POST", appServerUrl + "/api/login/getToken", httpServletRequest);
        } catch (UnsupportedEncodingException e) {
            log.error("loginController " + "UnsupportedEncoding Error", e);
            return ResponseResult.getFailureResult("UnsupportedEncoding Error");
        }
        // 回调鉴权失败响应
        if (!verify) {
            return ResponseResult.getFailureResult("HttpRequestVerifyFailure");
        }

        try {
            GetLoginTokenRequest request = new GetLoginTokenRequest();
            request.setAppId(Optional.ofNullable(params.getAppId()).orElse(this.appId));
            request.setRequestParams(params);
            GetLoginTokenResponse response = client.getLoginToken(request);
            log.info("getToken success:\n{}", JSON.toJSONString(response, SerializerFeature.PrettyFormat));

            if (null == response) {
                return ResponseResult.getFailureResult("get login token failed");
            }

            GetLoginTokenResponseBody.GetLoginTokenResponseBodyResult result = Optional.ofNullable(response)
                    .map(GetLoginTokenResponse::getBody)
                    .map(GetLoginTokenResponseBody::getResult)
                    .orElse(null);

            return ResponseResult.getSuccessResult(result);
        } catch (Exception e) {
            log.error("getToken error", e);
            return ResponseResult.getFailureResult(e.getMessage());
        }
    }

    @RequestMapping(value = "createRoom", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseResult<CreateRoomResponseBody.CreateRoomResponseBodyResult> createRoom(CreateRoomRequest.CreateRoomRequestRequest params, HttpServletRequest httpServletRequest) {
        log.info("createRoom, params={}", JSON.toJSONString(params));
        HttpControllerUtil.logRequestInfo(httpServletRequest, "createRoom");
        // 验证签名（可选，根据控制台设置的回调鉴权码进行鉴权，保证回调安全性）
        boolean verify = false;
        try {
            verify = SignUtil.isvVerifySign(appSignSecret, "POST", appServerUrl + "/api/login/createRoom", httpServletRequest);
        } catch (UnsupportedEncodingException e) {
            log.error("loginController " + "UnsupportedEncoding Error", e);
            return ResponseResult.getFailureResult("UnsupportedEncoding Error");
        }
        // 回调鉴权失败响应
        if (!verify) {
            return ResponseResult.getFailureResult("HttpRequestVerifyFailure");
        }

        try {
            CreateRoomRequest request = new CreateRoomRequest();
            request.setRequest(params);
            log.info("request:{}", JSON.toJSONString(request));
            CreateRoomResponse response = client.createRoom(request);
            if (null == response || !Objects.requireNonNull(response.getBody()).getResponseSuccess()) {
                return ResponseResult.getFailureResult("create room failed");
            }

            log.info("createRoom success:\n{}", JSON.toJSONString(response, SerializerFeature.PrettyFormat));
            CreateRoomResponseBody.CreateRoomResponseBodyResult responseBodyResult = Optional.ofNullable(response)
                    .map(CreateRoomResponse::getBody)
                    .map(CreateRoomResponseBody::getResult)
                    .orElse(null);


            return ResponseResult.getSuccessResult(responseBodyResult);

        } catch (Exception e) {
            log.error("createRoom error", e);
            return ResponseResult.getFailureResult(e.getMessage());
        }
    }
}
