package com.aliyun.roompaas.app.sp;

import com.aliyun.roompaas.app.Const;

@SpHelper.Sp
public interface RoomTypeSp {

    @SpHelper.Getter(defValue = Const.BIZ_TYPE.BUSINESS)
    String getRoomType();

    @SpHelper.Setter
    void setRoomType(@Const.BIZ_TYPE String roomType);
}
