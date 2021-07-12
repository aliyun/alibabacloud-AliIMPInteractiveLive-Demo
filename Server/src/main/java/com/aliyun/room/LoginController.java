package com.aliyun.room;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.aliyun.imp20210630.Client;
import com.aliyun.imp20210630.models.*;
import com.aliyun.tea.utils.StringUtils;
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
        authConfig.endpoint = "imp.aliyuncs.com";

        // 2. 实例化Client
        client = new Client(authConfig);
    }

    @RequestMapping(value = "getToken", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseResult<GetAuthTokenResponseBody.GetAuthTokenResponseBodyResult> getToken(GetAuthTokenRequest params, HttpServletRequest httpServletRequest) {
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

            GetAuthTokenResponse response = client.getAuthToken(params);
            log.info("getToken success:\n{}", JSON.toJSONString(response, SerializerFeature.PrettyFormat));

            if (null == response) {
                return ResponseResult.getFailureResult("get login token failed");
            } else if (!StringUtils.isEmpty(Objects.requireNonNull(response.getBody()).getErrorCode())) {
                return ResponseResult.getFailureResult(Objects.requireNonNull(response.getBody()).getErrorMessage());
            }

            GetAuthTokenResponseBody.GetAuthTokenResponseBodyResult result = Optional.ofNullable(response)
                    .map(GetAuthTokenResponse::getBody)
                    .map(GetAuthTokenResponseBody::getResult)
                    .orElse(null);

            return ResponseResult.getSuccessResult(result);
        } catch (Exception e) {
            log.error("getToken error", e);
            return ResponseResult.getFailureResult(e.getMessage());
        }
    }

    @RequestMapping(value = "createRoom", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseResult<CreateRoomResponseBody.CreateRoomResponseBodyResult> createRoom(CreateRoomRequest params, HttpServletRequest httpServletRequest) {
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
            log.info("request:{}", JSON.toJSONString(params));
            CreateRoomResponse response = client.createRoom(params);
            if (null == response) {
                return ResponseResult.getFailureResult("create room failed");
            } else if (!StringUtils.isEmpty(Objects.requireNonNull(response.getBody()).getErrorCode())) {
                return ResponseResult.getFailureResult(Objects.requireNonNull(response.getBody()).getErrorMessage());
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

    @RequestMapping(value = "destroyRoom", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseResult<DeleteRoomResponseBody> destroyRoom(DeleteRoomRequest params,
                                                              @RequestParam(value = "userId", required = false) String userId,
                                                              HttpServletRequest httpServletRequest) {
        log.info("destroyRoom, params={}", JSON.toJSONString(params));
        if (org.apache.commons.lang3.StringUtils.isBlank(userId)) {
            return ResponseResult.getFailureResult("userId is empty");
        }

        // 验证签名（可选，根据控制台设置的回调鉴权码进行鉴权，保证回调安全性）
        boolean verify = false;
        try {
            verify = SignUtil.isvVerifySign(appSignSecret, "POST", appServerUrl + "/api/login/destroyRoom", httpServletRequest);
        } catch (UnsupportedEncodingException e) {
            log.error("loginController " + "UnsupportedEncoding Error", e);
            return ResponseResult.getFailureResult("UnsupportedEncoding Error");
        }
        // 回调鉴权失败响应
        if (!verify) {
            return ResponseResult.getFailureResult("HttpRequestVerifyFailure");
        }

        try {
            // 1.查询房间房主
            GetRoomRequest getRoomRequest
                    = new GetRoomRequest();
            getRoomRequest.setRoomId(params.getRoomId());
            getRoomRequest.setAppId(params.getAppId());
            GetRoomResponse roomDetail = client.getRoom(getRoomRequest);
            if (null == roomDetail) {
                return ResponseResult.getFailureResult("get room failed");
            } else if (!StringUtils.isEmpty(Objects.requireNonNull(roomDetail.getBody()).getErrorCode())) {
                return ResponseResult.getFailureResult(Objects.requireNonNull(roomDetail.getBody()).getErrorMessage());
            }

            String ownerId = roomDetail.getBody().getResult().getRoomInfo().getRoomOwnerId();

            if (!org.apache.commons.lang3.StringUtils.equals(ownerId, userId)) {
                return ResponseResult.getFailureResult("userId is not ownerId of this room");
            }


            DeleteRoomRequest request = new DeleteRoomRequest();
            log.info("request:{}", JSON.toJSONString(request));
            DeleteRoomResponse response = client.deleteRoom(params);
            if (null == response) {
                return ResponseResult.getFailureResult("get room failed");
            } else if (!StringUtils.isEmpty(Objects.requireNonNull(response.getBody()).getErrorCode())) {
                return ResponseResult.getFailureResult(Objects.requireNonNull(response.getBody()).getErrorMessage());
            }

            log.info("destroyRoom success:\n{}", JSON.toJSONString(response, SerializerFeature.PrettyFormat));
            DeleteRoomResponseBody responseBodyResult = Optional.ofNullable(response)
                    .map(DeleteRoomResponse::getBody)
                    .orElse(null);

            return ResponseResult.getSuccessResult(responseBodyResult);

        } catch (Exception e) {
            log.error("destroyRoom error", e);
            return ResponseResult.getFailureResult(e.getMessage());
        }
    }
}
