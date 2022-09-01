package com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu;

import java.util.HashMap;
import java.util.Map;

// 记录当前的选项
public class BeautyPanelBeanHelper {

    private static int V_ID_INIT = 0;
    private static int generateID() {
        return V_ID_INIT++;
    }

    private static Map<Integer, Integer> sTabRecord = new HashMap<Integer, Integer>(10);

    public static int getIndexByItemType(int itemType, int defaultValue) {
        Integer value = sTabRecord.get(Integer.valueOf(itemType));
        return value != null ? value.intValue() : defaultValue;
    }

    public static void putItemTypeWithIndex(int itemType, int index) {
        sTabRecord.put(itemType, index);
    }

}
