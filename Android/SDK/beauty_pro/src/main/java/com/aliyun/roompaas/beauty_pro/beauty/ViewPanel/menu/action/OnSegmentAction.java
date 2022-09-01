package com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.action;

import com.aliyun.roompaas.beauty_common.QueenParam;
import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.model.TabInfo;
import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.model.TabItemInfo;

import java.util.List;

import static com.aliyun.roompaas.beauty_common.QueenParamHolder.getQueenParam;


public class OnSegmentAction extends IItemAction {
    private final int ID_BG_SEGMENT_GREEN = 1;
    private final int ID_BG_SEGMENT_GREEN_AUTO = 2;
    private final int ID_BG_SEGMENT_BLUE = 3;
    private final int ID_BG_SEGMENT_BLUE_AUTO = 4;

    private QueenParam.SegmentRecord getParam() {
        return getQueenParam().segmentRecord;
    }

    private void disableSegment() {
        getParam().enableBlueSegment = false;
        getParam().enableGreenSegment = false;
    }

    private void disableSegmentAI() {
        disableSegment();
        getParam().enableAiSegment = false;
        getParam().aiSegmentBackgroundPath = null;
    }

    @Override
    public void onAcitonClick(TabItemInfo itemInfo) {
        if (itemInfo.itemId < 0) {
            disableSegment();
            return;
        }

        disableSegmentAI();

        if (ID_BG_SEGMENT_GREEN == itemInfo.itemId) {
            getParam().enableGreenSegment = true;
            getParam().enableGreenSegmentAutoThreshold = false;
        } else if (ID_BG_SEGMENT_GREEN_AUTO == itemInfo.itemId) {
            getParam().enableGreenSegment = true;
            getParam().enableGreenSegmentAutoThreshold = true;
        } else if (ID_BG_SEGMENT_BLUE == itemInfo.itemId) {
            getParam().enableBlueSegment = true;
            getParam().enableBlueSegmentAutoThreshold = false;
        } else if (ID_BG_SEGMENT_BLUE_AUTO == itemInfo.itemId) {
            getParam().enableBlueSegment = true;
            getParam().enableBlueSegmentAutoThreshold = true;
        }
    }

    @Override
    public void onValueChanged(TabItemInfo itemInfo, int value) {
        if (ID_BG_SEGMENT_GREEN == itemInfo.itemId) {
            getParam().greenSegmentThreshold = QueenParam.SegmentRecord.formatThresholdParam(value);
        } else if (ID_BG_SEGMENT_BLUE == itemInfo.itemId) {
            getParam().blueSegmentThreshold = QueenParam.SegmentRecord.formatThresholdParam(value);
        }
    }

    @Override
    public int getValueByItem(TabItemInfo itemInfo) {
        int value = 0;
        if (ID_BG_SEGMENT_GREEN == itemInfo.itemId) {
            value = QueenParam.SegmentRecord.deFormatThresholdParam(getParam().greenSegmentThreshold);
        } else if (ID_BG_SEGMENT_BLUE == itemInfo.itemId) {
            value = QueenParam.SegmentRecord.deFormatThresholdParam(getParam().blueSegmentThreshold);
        }
        return value;
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
