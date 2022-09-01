package com.aliyun.roompaas.live.exposable;

/**
 * 美颜设置，支持美白、磨皮、锐化、红润等参数
 */
public class AliLiveBeautyOptions {
    // 美白
    public int beautyWhite = 32;
    // 磨皮
    public int beautyBuffing = 72;
    @Deprecated
    public int beautyCheekPink = 15;
    @Deprecated
    public int beautyBrightness = 50;
    // 红润
    public int beautyRuddy = 20;
    @Deprecated
    public int slimFace = 40;
    @Deprecated
    public int beautyBigEye = 30;
    @Deprecated
    public int shortenFace = 50;
    // 锐化
    public int skinSharpen = 20;

    public AliLiveBeautyOptions(Builder builder) {
        if (builder != null) {
            beautyWhite = builder.beautyWhite;
            beautyBuffing = builder.beautyBuffing;
            beautyCheekPink = builder.beautyCheekPink;
            beautyBrightness = builder.beautyBrightness;
            beautyRuddy = builder.beautyRuddy;
            beautyBigEye = builder.beautyBigEye;
            shortenFace = builder.shortenFace;
            slimFace = builder.slimFace;
            skinSharpen = builder.skinSharpen;
        }
    }

    public static final class Builder {
        private int beautyWhite = 32;
        private int beautyBuffing = 72;
        private int beautyCheekPink = 15;
        private int beautyBrightness = 50;
        private int beautyRuddy = 20;
        private int slimFace = 40;
        private int beautyBigEye = 30;
        private int shortenFace = 50;
        private int skinSharpen = 20;

        public Builder() {

        }

        /**
         * 美颜大眼[0,100]
         * @param val
         * @return
         */
        public Builder beautyBigEye(int val) {
            beautyBigEye = val;
            return this;
        }

        /**
         * 美颜亮度[0,100]
         * @param val
         * @return
         */
        @Deprecated
        public Builder beautyBrightness(int val) {
            beautyBrightness = val;
            return this;
        }

        /**
         * 磨皮程度[0,100]
         * @param val
         * @return
         */
        public Builder beautyBuffing(int val) {
            beautyBuffing = val;
            return this;
        }

        /**
         * 美颜腮红[0,100]
         * @param val
         * @return
         */
        @Deprecated
        public Builder beautyCheekPink(int val) {
            beautyCheekPink = val;
            return this;
        }

        /**
         * 美颜红润[0,100]
         * @param val
         * @return
         */
        public Builder beautyRuddy(int val) {
            beautyRuddy = val;
            return this;
        }

        /**
         * 美颜收下巴[0,100]
         * @param val
         * @return
         */
        @Deprecated
        public Builder beautyShortenFace(int val) {
            shortenFace = val;
            return this;
        }

        /**
         * 美颜瘦脸[0,100]
         * @param val
         * @return
         */
        @Deprecated
        public Builder slimFace(int val) {
            slimFace = val;
            return this;
        }

        /**
         * 美白程序[0,100]
         * @param val
         * @return
         */
        public Builder beautyWhite(int val) {
            beautyWhite = val;
            return this;
        }

        /**
         * 锐化处理
         * @param val
         * @return
         */
        public Builder skinSharpen(int val) {
            skinSharpen = val;
            return this;
        }

        public AliLiveBeautyOptions build() {
            return new AliLiveBeautyOptions(this);
        }
    }

}
