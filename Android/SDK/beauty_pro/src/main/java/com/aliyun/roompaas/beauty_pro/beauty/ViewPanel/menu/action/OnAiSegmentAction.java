package com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.action;

import android.content.Context;

import com.aliyun.roompaas.beauty_common.QueenParam;
import com.aliyun.roompaas.beauty_common.QueenParamHolder;
import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.model.TabInfo;
import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.model.TabItemInfo;
import com.aliyun.roompaas.beauty_pro.utils.BeautyUtils;
import com.aliyun.roompaas.beauty_pro.utils.FileUtils;
import com.aliyun.roompaas.beauty_pro.utils.ResoureUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OnAiSegmentAction extends IItemAction {

    private final static String AI_SEGMENT_PATH = "background";
//    private final static String AI_SEGMENT_DEFAULT_ICON_NAME = "icon.png";
    private final String PREFIX_STRING_TYPE = ResoureUtils.PREFIX_QUOTE;

    private final int ID_AI_SEGMENT_BLUR = 0;

    private QueenParam.SegmentRecord getParam() {
        return QueenParamHolder.getQueenParam().segmentRecord;
    }


    @Override
    public void onAcitonClick(TabItemInfo itemInfo) {
        disableSegmentAI();

        if (itemInfo.itemId < 0) {
            return;
        }

        if (itemInfo.itemId == ID_AI_SEGMENT_BLUR) {
            // 第一个是虚幻背景
            getParam().enableAiSegment = true;
            getParam().aiSegmentAsync = false;
        } else {
            String itemName = itemInfo.itemName.startsWith(PREFIX_STRING_TYPE) ? itemInfo.itemName.substring(1) : itemInfo.itemName;
            String resPath = AI_SEGMENT_PATH + File.separator + itemName;
            QueenParamHolder.getQueenParam().segmentRecord.aiSegmentBackgroundPath = resPath;
        }
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
        List<TabItemInfo> itemInfoList = scanList(tabInfo);

        tabInfo.tabItemInfoList.clear();
        tabInfo.tabItemInfoList.addAll(itemInfoList);
        return itemInfoList;
    }

    private List<TabItemInfo> scanList(TabInfo tabInfo) {
        // 获取assets目录assetDir下一级所有文件以及文件夹
        Context context = BeautyUtils.sApplication;
        List<TabItemInfo> itemList = null;

        try {
            String[] fileNames = FileUtils.listRemoteRes(AI_SEGMENT_PATH);
            itemList = new ArrayList<>(fileNames.length+2);
            TabItemInfo noneItemInfo = createNoneItemInfo(tabInfo.tabType);
            itemList.add(noneItemInfo);
            itemList.add(createBlurItemInfo(tabInfo.tabType));

            int i = 0;
            while (i < fileNames.length) {
                String fileName = fileNames[i++];
                String[] dirList = FileUtils.listRemoteRes(AI_SEGMENT_PATH + File.separator + fileName);
                if (dirList == null || dirList.length<=0) continue;

                TabItemInfo itemInfo = new TabItemInfo();
                itemInfo.itemType = tabInfo.tabType;
                itemInfo.itemId = i;
                itemInfo.itemName = PREFIX_STRING_TYPE + fileName;
                itemInfo.itemIconNormal = ICON_NORMAL;
                itemInfo.itemIconSelected = ICON_FOCUS;
                itemList.add(itemInfo);
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
        return itemList;
    }

    private TabItemInfo createBlurItemInfo(int itemType) {
        TabItemInfo noneItemInfo = new TabItemInfo();
        noneItemInfo.itemType = itemType;
        noneItemInfo.itemId = ID_AI_SEGMENT_BLUR;
        noneItemInfo.itemName = "@bg_segment_ai_blur";
        noneItemInfo.itemIconNormal = ICON_NORMAL;
        noneItemInfo.itemIconSelected = ICON_FOCUS;

        return noneItemInfo;
    }

    private void disableSegmentAI() {
        getParam().enableBlueSegment = false;
        getParam().enableGreenSegment = false;
        getParam().enableAiSegment = false;
        getParam().aiSegmentBackgroundPath = null;
    }
}
