package com.aliyun.roompaas.app.model;

import java.io.Serializable;

/**
 * Created by KyleCe on 2021/5/27
 */
public class UserInfo implements Serializable {
        //"userInfo": {
        //        "avatarUrl": "http://www.avatarset/Charles.jpg",
        //                "nick": "Charles",
        //                "nickPinyin": "Pinyin_Charles",
        //                "userId": "jianli"
        //    },
    public String avatarUrl;
    public String nick;
    public String nickPinyin;
    public String userId;
}
