package com.aliyun.room.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Http请求Controller的统一处理工具类
 */
@Slf4j
@Component
public class HttpControllerUtil {

    /**
     * 打印请求日志
     *
     * @param request
     */
    public void logRequestInfo(HttpServletRequest request) {
        if (request == null) {
            return;
        }

        Map<String, String> reqHeaders = new HashMap<>();

        Enumeration<String> headers = request.getHeaderNames();
        while (headers != null && headers.hasMoreElements()) {
            String header = headers.nextElement();
            String value = request.getHeader(header);
            reqHeaders.put(header, value);
        }

        log.info("[HttpControllerManager] logRequestInfo , url {}, query {}, headers {}", request.getRequestURI(), request.getQueryString(),
                JSON.toJSONString(reqHeaders));
    }

    /**
     * 打印HTTP请求的Params, Header, Cookie, Session, json request body等信息
     *
     * @param request HttpServletRequest
     * @param tag     打印log的TAG
     */
    public static String logRequestInfo(HttpServletRequest request, String tag) {

        // 打印所有 request Params
        Map<String, String[]> map = request.getParameterMap();
        Set<String> keys = map.keySet();
        for (String key : keys) {
            String[] value = map.get(key);
            log.info("[Test]" + tag + " Params " + key + " = " + value[0]);
        }

        // 打印所有 request Header
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            String value = request.getHeader(key);
            log.info("[Test]" + tag + " Header " + key + " = " + value);
        }

        // 打印所有 request Cookie
        Cookie[] cookies = request.getCookies();
        if (null == cookies || cookies.length == 0) {
            log.info("[Test]POST Cookie null");
        } else {
            for (Cookie c : cookies) {
                log.info("[Test]" + tag + " Cookie " + c.getName() + " = " + c.getValue());
            }
        }

        // 打印所有 request Session
        HttpSession session = request.getSession();
        Enumeration<String> sessionNames = session.getAttributeNames();
        if (null == sessionNames || !sessionNames.hasMoreElements()) {
            log.info("[Test]" + tag + " Session null");
        } else {
            while (sessionNames.hasMoreElements()) {
                String key = sessionNames.nextElement();
                String value = session.getAttribute(key).toString();
                log.info("[Test]" + tag + " Session " + key + " = " + value);
            }
        }

        // request Body
        BufferedReader br;
        String readStr;
        StringBuilder bodyStr = new StringBuilder();

        try {
            br = request.getReader();
            while ((readStr = br.readLine()) != null) {
                bodyStr.append(readStr);
            }
        } catch (IOException e) {
            log.error("msg {}", e.getMessage(), e);
        }

        log.info("[Test]" + tag + " body " + bodyStr + "\n");

        return bodyStr.toString();
    }

}
