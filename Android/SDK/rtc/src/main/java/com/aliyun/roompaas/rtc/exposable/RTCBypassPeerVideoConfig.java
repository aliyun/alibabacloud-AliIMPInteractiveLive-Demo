package com.aliyun.roompaas.rtc.exposable;

public final class RTCBypassPeerVideoConfig {
/*
 *   全屏的画面，则参数宽为1.0, 高1.0, 中心坐标为(0.5,0.5)
 */

    /**
     * 必填，格子左上角x坐标，归一化后的百分比值, 举例：对于宽为720像素，x=0.1，代表左上角x坐标为720*0.1=72像素
     */
    public float x = 0.0F;

    /**
     * 必填，格子左上角y坐标，归一化后的百分比值
     */
    public float y = 0.0F;

    /**
     * 必填，格子宽，归一化后的百分比值
     */
    public float width = 0.0F;

    /**
     * 必填，格子高，归一化后的百分比值
     */
    public float height = 0.0F;

    /**
     * 必填，格子zOrder，值越大，越在上面
     */
    public int zOrder = 0;
    public String userId = "";

    public RTCBypassPeerVideoConfig(float x, float y, float width, float height, int zOrder, String userId) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.zOrder = zOrder;
        this.userId = userId;
    }

    public RTCBypassPeerVideoConfig() {
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public float getWidth() {
        return this.width;
    }

    public float getHeight() {
        return this.height;
    }

    public int getZOrder() {
        return this.zOrder;
    }

    public String getUserId() {
        return this.userId;
    }

    public String toString() {
        return "PaneModel{x=" + this.x + ",y=" + this.y + ",width=" + this.width + ",height=" + this.height + ",zOrder=" + this.zOrder + ",userId=" + this.userId + "}";
    }
}
