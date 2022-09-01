package com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.action;

import com.aliyun.roompaas.beauty_common.QueenParam;
import com.aliyun.roompaas.beauty_common.QueenParamFactory;
import com.aliyun.roompaas.beauty_common.QueenParamHolder;
import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.model.TabInfo;
import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.model.TabItemInfo;

import java.util.List;

public class OnScenesAction extends IItemAction {

    @Override
    public void onAcitonClick(TabItemInfo itemInfo) {
        QueenParam queenParam = QueenParamFactory.Scenes.getScenes(itemInfo.itemId);
        QueenParamHolder.setQueenParam(queenParam);
    }

    @Override
    public List<TabItemInfo> getItemList(TabInfo tabInfo) {
        List<TabItemInfo> itemInfoList = super.getItemList(tabInfo);
        return wrapperItemList(itemInfoList);
    }

    @Override
    public int getFocusIndex(TabInfo tabInfo) {
        List<TabItemInfo> itemList = getItemList(tabInfo);
        int targetIndex = 0;
        QueenParam runtimeParam = QueenParamHolder.getQueenParam();
        for (int i = 0; i < itemList.size(); ++i) {
            QueenParam param = QueenParamFactory.Scenes.getScenes(itemList.get(i).itemId);
            if (param != null && param.hashCode() == runtimeParam.hashCode()) {
                 targetIndex = i;
                 break;
            }
        }
        return targetIndex;
    }
}
