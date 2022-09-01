package com.aliyun.roompaas.beauty_pro.beauty.model;

public class QueenCommonParams {

    public interface BeautyType {

//        int SCENES = 9000;      //
        int SCENES = 10000;
        int BEAUTY = 11000;   //"美颜";
        int FACE_SHAPE = 12000;  //"美型";
        int FACE_MAKEUP = 13000;    //"美妆";
        int LUT = 14000;    //"滤镜";
        int STICKER = 15000;    //"贴纸";
        int SEGMENT = 16000;    //"背景";
        int AI_SEGMENT = 17000; //"实景抠图";
        int GESTURE = 18000;    // 手势
        int BEAUTY_BODY = 19000;    // 美体
        int BLACK_TECHNOLOGY = 20000;   // 黑科技
    }

}
