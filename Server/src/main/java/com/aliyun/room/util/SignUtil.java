package com.aliyun.room.util;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * 验签工具类
 */
@Slf4j
public class SignUtil {

    /*
     * 签名公共参数HeaderKey
     */
    public static final String APP_ID = "a-app-id";
    public static final String SIGNATURE = "a-signature"; // 签名结果串
    public static final String SIGNATURE_METHOD = "a-signature-method";
    public static final String TIMESTAMP = "a-timestamp";
    public static final String SIGNATURE_VERSION = "a-signature-version";
    public static final String SIGNATURE_NONCE = "a-signature-nonce";

    private static final Set<String> PROCESSED_HEADERS = Sets.newHashSet();
    static {
        PROCESSED_HEADERS.add(APP_ID);
        PROCESSED_HEADERS.add(SIGNATURE);
        PROCESSED_HEADERS.add(SIGNATURE_METHOD);
        PROCESSED_HEADERS.add(TIMESTAMP);
        PROCESSED_HEADERS.add(SIGNATURE_VERSION);
        PROCESSED_HEADERS.add(SIGNATURE_NONCE);
    }

    // 签名算法固定值
    public static final String SIGNATURE_METHOD_VALUE = "HMAC-SHA1";

    // 签名算法版本固定值
    public static final String SIGNATURE_VERSION_VALUE = "1.0";

    // 签名算法参数
    private static final String ALGORITHM_NAME = "HmacSHA1";
    private static final String ENCODING = "UTF-8";
    private static final String URL_ENCODING = "UTF-8";

    /**
     * 回调请求验签
     *
     * @param signSecret  签名密钥
     * @param method      请求方式，对于回调，取值POST
     * @param callbackUrl 回调服务地址
     * @param request     Http请求 包含请求参数和请求头
     *
     * @return 是否校验通过
     */
    public static boolean isvVerifySign(String signSecret, String method, String callbackUrl, HttpServletRequest request) throws UnsupportedEncodingException {

        // 从request中提取params
        Map<String, String> params = new HashMap<>();
        Map<String,String[]> params_raw = request.getParameterMap();
        Set<String> keys = params_raw.keySet();
        for(String key : keys) {
            String[] value = params_raw.get(key);
            params.put(key, value[0]);
        }

        // 从request中提取headers
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            String value = request.getHeader(key);
            headers.put(key.toLowerCase(), value);
        }

        return verifySign(signSecret, method, callbackUrl, params, headers);
    }

    /**
     * 校验接口请求签名
     *
     * @param signSecret 签名密钥
     * @param method     {@link RequestMethod}，不区分大小写
     * @param path       示例：/api/service，必须以"/"开头
     * @param params     请求参数
     * @param headers    请求头部（含签名），仅读取以下头部，header 名全小写
     *                     业务参数：
     *                      「a-token」
     *                     公共参数：
     *                      「a-app-id」
     *                      「a-signature」
     *                      「a-signature-method」
     *                      「a-timestamp」
     *                      「a-signature-version」
     *                      「a-signature-nonce」
     * @return 是否校验通过
     */
    public static boolean verifySign(String signSecret, String method, String path, Map<String, String> params,
                                     Map<String, String> headers) throws UnsupportedEncodingException {

        // 1. 获取签名结果串
        String signature = headers.remove(SIGNATURE);

        // 2. 请求params、headers参数检查，非空检查及签名方式、算法版本检查
        RequestMethod requestMethod = RequestMethod.getByValue(method);

        Validate.notNull(signature, "The signature must not be null");
        Validate.notNull(headers, "The headers must not be null");
        Validate.notBlank(signSecret, "The signSecret must not be blank");
        Validate.notNull(requestMethod, "Unsupported method: " + method);
        Validate.notBlank(path, "The path must not be blank");
        Validate.notNull(params, "The params must not be null");
        Validate.notNull(headers, "The headers must not be null");

        Validate.isTrue(SIGNATURE_METHOD_VALUE.equals(headers.get(SIGNATURE_METHOD)), "Unsupported signature method: " + headers.get(SIGNATURE_METHOD));
        Validate.isTrue(SIGNATURE_VERSION_VALUE.equals(headers.get(SIGNATURE_VERSION)), "Unsupported signature version: " + headers.get(SIGNATURE_VERSION));

        // 3.构造签名字符串stringToSign
        //   3.1 从headers中提取签名公共参数signedHeaders
        Map<String, String> signedHeaders = Maps.newHashMap();
        for (String header : PROCESSED_HEADERS) {
            String value = headers.get(header);
            if (StringUtils.isBlank(value)) {
                continue;
            }
            signedHeaders.put(header, value);
        }

        //   3.2 构造规范化的params/signedHeaders字符串（参数按key排序后，组合成&key=value形式）
        String queryString = canonicalizedQuery(params);
        String headerString = canonicalizedQuery(signedHeaders);

        //   3.3 与method, path一起构造签名字符串stringToSign
        String stringToSign = buildSignString(requestMethod.name(), path, queryString, headerString);

        // 1.计算签名
        String expectedSignature = sign(stringToSign, signSecret + "&");
        // 2.校验签名
        return signature.equals(expectedSignature);
    }

    /***
     * 计算签名
     * @param stringToSign 签名字符串
     * @param signSecret   签名密钥
     * @return 计算得到的签名
     */
    public static String sign(String stringToSign, String signSecret) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM_NAME);
            mac.init(new SecretKeySpec(
                    signSecret.getBytes(ENCODING),
                    ALGORITHM_NAME
            ));
            byte[] signData = mac.doFinal(stringToSign.getBytes(ENCODING));
            String signBase64 = DatatypeConverter.printBase64Binary(signData);
            return percentEncode(signBase64);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeyException e) {
            throw new IllegalArgumentException(e.toString(), e);
        }
    }

    /**
     * URL编码
     * 使用UTF-8字符集按照 RFC3986 规则编码请求参数和参数取值
     */
    public static String percentEncode(String value) throws UnsupportedEncodingException {
        return value != null ? URLEncoder.encode(value, URL_ENCODING).replace("+", "%20")
                .replace("*", "%2A").replace("%7E", "~") : null;
    }

    /**
     * 将参数排序后，进行规范化设置，组合成请求字符串
     * @param queryParamsMap   所有请求参数
     * @return 规范化的请求字符串
     */
    public static String canonicalizedQuery(Map<String, String> queryParamsMap) throws UnsupportedEncodingException {
        String[] sortedKeys = queryParamsMap.keySet().toArray(new String[]{});
        Arrays.sort(sortedKeys);
        StringBuilder canonicalizedQueryString = new StringBuilder();
        for (String key : sortedKeys) {
            canonicalizedQueryString.append("&")
                    .append(percentEncode(key)).append("=")
                    .append(percentEncode(queryParamsMap.get(key)));
        }
        return canonicalizedQueryString.substring(1);
    }

    /***
     * 构造签名字符串
     * @param method       HTTP请求的方法
     * @param urlPath      HTTP请求的路径
     * @param queryString  规范化的请求字符串
     * @param headerString 规范化的头部字符串
     * @return 签名字符串
     */
    public static String buildSignString(String method, String urlPath, String queryString, String headerString) throws UnsupportedEncodingException {
        StringBuilder strBuilderSign = new StringBuilder();
        strBuilderSign.append(method);
        strBuilderSign.append("+");
        strBuilderSign.append(percentEncode(urlPath));
        strBuilderSign.append("+");
        strBuilderSign.append(percentEncode(queryString));
        strBuilderSign.append("+");
        strBuilderSign.append(percentEncode(headerString));

        return strBuilderSign.toString();
    }

    /**
     * HTTP请求类型
     */
    @Getter
    @AllArgsConstructor
    public enum RequestMethod {
        /**
         * GET
         */
        GET("GET"),
        /**
         * POST
         */
        POST("POST"),
        /**
         * DELETE
         */
        DELETE("DELETE");

        private static final Map<String, RequestMethod> VALUE_MAP = Maps.newHashMap();

        private final String value;

        static {
            for (RequestMethod requestMethod : RequestMethod.values()) {
                VALUE_MAP.put(requestMethod.getValue(), requestMethod);
            }
        }

        public static RequestMethod getByValue(String value) {
            return VALUE_MAP.get(value.toUpperCase());
        }
    }

}
