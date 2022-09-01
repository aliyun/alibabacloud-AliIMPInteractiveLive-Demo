package com.aliyun.roompaas.rtc;

/**
 * RTC混流布局方案
 */
public enum RtcLayoutModel {
    /**
     * 默认模式，一个主画面
     */
    ONE_GRID(1),
    /**
     * 一大四小
     */
    ONE_SUPPORT_FOUR(2),
    /**
     * 九宫格
     */
    NINE_GRID(3);

    private int model;

    RtcLayoutModel(int model) {
        this.model = model;
    }

    public int getModel() {
        return model;
    }

    public static RtcLayoutModel of(int model) {
        for (RtcLayoutModel layoutModel : values()) {
            if (layoutModel.model == model) {
                return layoutModel;
            }
        }
        return ONE_GRID;
    }
}
