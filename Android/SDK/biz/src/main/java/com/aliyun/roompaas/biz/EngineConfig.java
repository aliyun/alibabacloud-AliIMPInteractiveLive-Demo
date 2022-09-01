package com.aliyun.roompaas.biz;

import com.aliyun.roompaas.base.log.LoggerHandler;
import com.aliyun.roompaas.biz.exposable.TokenInfoGetter;

/**
 * @author puke
 * @version 2021/4/28
 */
public class EngineConfig {

    public String appId;
    public String appKey;
    public String deviceId;
    public TokenInfoGetter tokenInfoGetter;
    public LoggerHandler loggerHandler;

    private EngineConfig(Builder builder) {
        appId = builder.appId;
        appKey = builder.appKey;
        deviceId = builder.deviceId;
        tokenInfoGetter = builder.tokenInfoGetter;
        loggerHandler = builder.loggerHandler;
    }


    public static final class Builder {
        private String appId;
        private String appKey;
        private String deviceId;
        private TokenInfoGetter tokenInfoGetter;
        private LoggerHandler loggerHandler;

        public Builder() {
        }

        public Builder appId(String val) {
            appId = val;
            return this;
        }

        public Builder appKey(String val) {
            appKey = val;
            return this;
        }

        public Builder deviceId(String val) {
            deviceId = val;
            return this;
        }

        public Builder tokenInfoGetter(TokenInfoGetter val) {
            tokenInfoGetter = val;
            return this;
        }

        public Builder loggerHandler(LoggerHandler val) {
            loggerHandler = val;
            return this;
        }

        public EngineConfig build() {
            return new EngineConfig(this);
        }
    }
}
