package com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu;

import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.action.OnAiSegmentAction;
import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.action.OnGestureAction;
import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.action.OnSegmentAction;
import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.model.TabInfo;
import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.model.TabItemInfo;
import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.view.BeautyMenuSeekPanel;
import com.aliyun.roompaas.beauty_pro.beauty.model.QueenCommonParams;
import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.action.IItemAction;
import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.action.OnBeautyAcition;
import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.action.OnBlackTechnologyAction;
import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.action.OnBodyAction;
import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.action.OnFaceMakeupAction;
import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.action.OnFaceShapeAciton;
import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.action.OnFaceStickerAction;
import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.action.OnLutAction;
import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.action.OnScenesAction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeautyPanelController implements
        BeautyMenuSeekPanel.OnProgressChangedListener {

    private Map<Integer, IItemAction> mItemActionList;

    public BeautyPanelController() {
        mItemActionList = new HashMap<Integer, IItemAction>(10);
        mItemActionList.put(QueenCommonParams.BeautyType.SCENES, new OnScenesAction());
        mItemActionList.put(QueenCommonParams.BeautyType.BEAUTY, new OnBeautyAcition());
        mItemActionList.put(QueenCommonParams.BeautyType.FACE_SHAPE, new OnFaceShapeAciton());
        mItemActionList.put(QueenCommonParams.BeautyType.FACE_MAKEUP, new OnFaceMakeupAction());
        mItemActionList.put(QueenCommonParams.BeautyType.LUT, new OnLutAction());
        mItemActionList.put(QueenCommonParams.BeautyType.STICKER, new OnFaceStickerAction());
        mItemActionList.put(QueenCommonParams.BeautyType.SEGMENT, new OnSegmentAction());
        mItemActionList.put(QueenCommonParams.BeautyType.AI_SEGMENT, new OnAiSegmentAction());
        mItemActionList.put(QueenCommonParams.BeautyType.BLACK_TECHNOLOGY, new OnBlackTechnologyAction());
        mItemActionList.put(QueenCommonParams.BeautyType.BEAUTY_BODY, new OnBodyAction());
        mItemActionList.put(QueenCommonParams.BeautyType.GESTURE, new OnGestureAction());
    }

    public int getFocusIndex(TabInfo tabInfo) {
        return mItemActionList.get(tabInfo.tabType).getFocusIndex(tabInfo);
    }

    public int getFocusIndex(TabItemInfo itemInfo) {
        return mItemActionList.get(itemInfo.itemType).getFocusIndex(itemInfo);
    }

    public List<TabItemInfo> getTabItemList(TabInfo tabInfo) {
        return mItemActionList.get(tabInfo.tabType).getItemList(tabInfo);
    }

    public List<TabItemInfo> getDiyTabItemList(TabInfo tabInfo) {
        return mItemActionList.get(tabInfo.tabType).getDiyItemList(tabInfo);
    }

    public List<TabItemInfo> getSubTabItemList(TabItemInfo tabItemInfo) {
        return mItemActionList.get(tabItemInfo.itemType).getSubItemList(tabItemInfo);
    }

    public int getValueById(TabItemInfo itemInfo) {
        IItemAction itemAction = mItemActionList.get(itemInfo.itemType);
        int defaultValue = 0;
        if (itemAction != null) {
            defaultValue = itemAction.getValueByItem(itemInfo);
        }
        return defaultValue;
    }

    public void onHandleItemClick(TabItemInfo itemInfo, int position) {
        IItemAction itemAction = mItemActionList.get(itemInfo.itemType);
        if (itemAction != null) {
            itemAction.onItemActionClick(itemInfo, position);
        }
    }

    @Override
    public void onProgressChanged(TabItemInfo itemInfo, int value) {
        IItemAction itemAction = mItemActionList.get(itemInfo.itemType);
        if (itemAction != null) {
            itemAction.onValueChanged(itemInfo, value);
        }
    }
}
