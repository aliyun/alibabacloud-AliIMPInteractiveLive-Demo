package com.aliyun.roompaas.app.model;

import com.aliyun.roompaas.app.util.ColorUtil;
import com.aliyun.roompaas.rtc.exposable.RtcUserStatus;

import java.io.Serializable;

/**
 * @author puke
 * @version 2021/6/15
 */
public class RtcUser implements Serializable {

    public String userId;

    public String nick;

    public RtcUserStatus status;

    public final int color = ColorUtil.randomColor();

    public RtcUser() {
    }

    private RtcUser(Builder builder) {
        userId = builder.userId;
        nick = builder.nick;
        status = builder.status;
    }


    public static final class Builder {
        private String userId;
        private String nick;
        private RtcUserStatus status;

        public Builder() {
        }

        public Builder userId(String val) {
            userId = val;
            return this;
        }

        public Builder nick(String val) {
            nick = val;
            return this;
        }

        public Builder status(RtcUserStatus val) {
            status = val;
            return this;
        }

        public RtcUser build() {
            return new RtcUser(this);
        }
    }
}
