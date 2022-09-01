package com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.action;

import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.model.TabInfo;
import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.model.TabItemInfo;

import java.util.List;

// 手势响应
public class OnGestureAction extends IItemAction {
    @Override
    public void onAcitonClick(TabItemInfo itemInfo) {

    }

    @Override
    public void onValueChanged(TabItemInfo itemInfo, int value) {

    }

    @Override
    public int getValueByItem(TabItemInfo itemInfo) {
        return 0;
    }

    @Override
    public List<TabItemInfo> getItemList(TabInfo tabInfo) {
        List<TabItemInfo> itemInfoList = super.getItemList(tabInfo);
        if (itemInfoList.get(0).itemId != ID_FEATURE_NONE) {
            TabItemInfo noneItemInfo = createNoneItemInfo(tabInfo.tabType);
            itemInfoList.add(0, noneItemInfo);
        }
        return itemInfoList;
    }
}
