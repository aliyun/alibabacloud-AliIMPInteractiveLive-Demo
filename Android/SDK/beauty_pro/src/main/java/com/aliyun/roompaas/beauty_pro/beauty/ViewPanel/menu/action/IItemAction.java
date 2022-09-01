package com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.action;

import com.aliyun.roompaas.beauty_common.QueenParamFactory;
import com.aliyun.roompaas.beauty_common.QueenRuntime;
import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.model.TabInfo;
import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.model.TabItemInfo;

import java.util.List;

public abstract class IItemAction {

    protected final static int ID_FEATURE_NONE = QueenParamFactory.ID_FEATURE_NONE;

//    protected final String ICON_DISABLE = "icon" + File.separator + "disable.png";
//    protected final String ICON_FOCUS = "icon" + File.separator + "focus.png";
//    protected final String ICON_NORMAL = "icon" + File.separator + "beauty.png";
    protected final String ICON_DISABLE = "@disable";
    protected final String ICON_FOCUS = "@focus";
    protected final String ICON_NORMAL = "@beauty_ic_smooth";

    protected int mLastClickPosition = 0;

    public void onItemActionClick(TabItemInfo itemInfo, int position) {
        QueenRuntime.isEnableQueen = true;
        mLastClickPosition = position;
        onAcitonClick(itemInfo);
    }

    protected abstract void onAcitonClick(TabItemInfo itemInfo);

    public void onValueChanged(TabItemInfo itemInfo, int value) {}

    public int getValueByItem(TabItemInfo itemInfo) { return 0; }

    public List<TabItemInfo> getItemList(TabInfo tabInfo) { return tabInfo.tabItemInfoList; }

    public List<TabItemInfo> getDiyItemList(TabInfo tabInfo) { return tabInfo != null ? tabInfo.diyItemInfoList : null; }

    public List<TabItemInfo> getSubItemList(TabItemInfo tabItemInfo) { return tabItemInfo != null ? tabItemInfo.subItemInfosList : null; }

    public int getFocusIndex(TabInfo tabInfo) {
//        return tabInfo.tabDefaultSelectedIndex;
        return mLastClickPosition;
    }

    public int getFocusIndex(TabItemInfo tabItemInfo) {
        return 0;
    }

    int deFormatParam(float param) {
        return (int) (param * 100);
    }
    float formatParam(int param) {
        return param / 100.0f;
    }
    float formatReverseParam(int param, int max) {
        return param/(max*1.0f) * -1f;
    }
    int deFormatReverseParam(float param, int max) {
        return (int) (param * -1f * max);
    }

    protected TabItemInfo createNoneItemInfo(int itemType) {
        TabItemInfo noneItemInfo = new TabItemInfo();
        noneItemInfo.itemType = itemType;
        noneItemInfo.itemId = ID_FEATURE_NONE;
        noneItemInfo.itemName = "@close";
        noneItemInfo.itemIconNormal = ICON_DISABLE;
        noneItemInfo.itemIconSelected = ICON_FOCUS;

        return noneItemInfo;
    }

    protected List<TabItemInfo> wrapperItemList(List<TabItemInfo> itemInfoList) {
        return wrapperItemList(itemInfoList, 0);
    }

    protected List<TabItemInfo> wrapperItemList(List<TabItemInfo> itemInfoList, int parentId) {
        return wrapperItemList(itemInfoList, parentId, true);
    }

    protected List<TabItemInfo> wrapperItemList(List<TabItemInfo> itemInfoList, int parentId, boolean addNoneItem) {
        if (itemInfoList == null || itemInfoList.size() == 0)
            return itemInfoList;

        if (addNoneItem && itemInfoList.get(0).itemId != ID_FEATURE_NONE) {
            TabItemInfo noneItemInfo = createNoneItemInfo(itemInfoList.get(0).itemType);
//            noneItemInfo.parentId = parentId;
            itemInfoList.add(0, noneItemInfo);
        }
        for (TabItemInfo itemInfo : itemInfoList) {
            itemInfo.parentId = parentId;
            if (itemInfo.itemId == ID_FEATURE_NONE) continue;
            itemInfo.itemIconNormal = ICON_NORMAL;
            itemInfo.itemIconSelected = ICON_FOCUS;
        }
        return itemInfoList;
    }
}
