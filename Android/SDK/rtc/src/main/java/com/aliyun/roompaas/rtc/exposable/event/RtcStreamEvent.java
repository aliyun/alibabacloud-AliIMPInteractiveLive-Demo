package com.aliyun.roompaas.rtc.exposable.event;

import com.alivc.rtc.AliRtcEngine;

import java.io.Serializable;

/**
 * 订阅的流信息
 * @author puke
 * @version 2021/6/1
 */
public class RtcStreamEvent implements Serializable {

    public String userId;
    public String userName;
    public AliRtcEngine.AliRtcVideoCanvas aliVideoCanvas;
    public AliRtcEngine.AliRtcVideoTrack aliRtcVideoTrack;

    public boolean closeMic;
    public boolean closeCamera;

    @Deprecated
    public boolean isTeacher;
    @Deprecated
    public boolean isLocalStream;
    @Deprecated
    public boolean muteLocalMic;
    @Deprecated
    public boolean muteLocalCamera;
    @Deprecated
    public boolean muteMic;
    @Deprecated
    public boolean muteCamera;

    private RtcStreamEvent(Builder builder) {
        if (builder != null) {
            this.userId = builder.userId;
            this.userName = builder.userName;
            this.aliVideoCanvas = builder.aliVideoCanvas;
            this.isLocalStream = builder.isLocalStream;
            this.isTeacher = builder.isTeacher;
            this.muteLocalCamera = builder.muteLocalCamera;
            this.muteLocalMic = builder.muteLocalMic;
            this.muteMic = builder.muteMic;
            this.muteCamera = builder.muteCamera;
            this.aliRtcVideoTrack = builder.aliRtcVideoTrack;

            this.closeMic = builder.closeMic;
            this.closeCamera = builder.closeCamera;
        }
    }

    public static class Builder {
        private String userId;
        private String userName;
        private AliRtcEngine.AliRtcVideoCanvas aliVideoCanvas;
        private boolean isLocalStream;
        private boolean isTeacher;
        private boolean muteLocalMic, muteLocalCamera, muteMic, muteCamera;
        private AliRtcEngine.AliRtcVideoTrack aliRtcVideoTrack;

        public boolean assembledInUserList;
        public boolean closeMic;
        public boolean closeCamera;

        public Builder setUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder setUserName(String userName) {
            this.userName = userName;
            return this;
        }

        public Builder setAliVideoCanvas(AliRtcEngine.AliRtcVideoCanvas aliVideoCanvas) {
            this.aliVideoCanvas = aliVideoCanvas;
            return this;
        }

        public Builder setLocalStream(boolean localStream) {
            isLocalStream = localStream;
            return this;
        }
        public Builder setTeacher(boolean teacher) {
            isTeacher = teacher;
            return this;
        }

        public Builder setMuteLocalMic(boolean muteLocalMic) {
            this.muteLocalMic = muteLocalMic;
            return this;
        }

        public Builder setAliRtcVideoTrack(AliRtcEngine.AliRtcVideoTrack aliRtcVideoTrack) {
            this.aliRtcVideoTrack = aliRtcVideoTrack;
            return this;
        }

        public Builder setMuteLocalCamera(boolean muteLocalCamera) {
            this.muteLocalCamera = muteLocalCamera;
            return this;
        }
        public Builder setMuteMic(boolean muteMic) {
            this.muteMic = muteMic;
            return this;
        }

        public Builder setMuteCamera(boolean muteCamera) {
            this.muteCamera = muteCamera;
            return this;
        }

        public Builder setAssembledInUserList(boolean assembledInUserList) {
            this.assembledInUserList = assembledInUserList;
            return this;
        }

        public Builder setCloseMic(boolean closeMic) {
            this.closeMic = closeMic;
            return this;
        }

        public Builder setCloseCamera(boolean closeCamera) {
            this.closeCamera = closeCamera;
            return this;
        }

        public RtcStreamEvent build() {
            return new RtcStreamEvent(this);
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof RtcStreamEvent && ((RtcStreamEvent)obj).userId.equals(this.userId);
    }

    @Override
    public String toString() {
        return "RtcStreamEvent{" +
                "userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", closeMic=" + closeMic +
                ", closeCamera=" + closeCamera +
                ", aliVideoCanvas=" + aliVideoCanvas +
                ", isLocalStream=" + isLocalStream +
                ", isTeacher=" + isTeacher +
                ", aliRtcVideoTrack=" + aliRtcVideoTrack +
                ", muteLocalMic=" + muteLocalMic +
                ", muteLocalCamera=" + muteLocalCamera +
                ", muteMic=" + muteMic +
                ", muteCamera=" + muteCamera +
                '}';
    }
}
