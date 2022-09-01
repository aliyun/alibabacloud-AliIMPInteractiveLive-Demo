package com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.action;

import android.content.Context;

import com.aliyun.roompaas.beauty_common.QueenParam;
import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.model.TabInfo;
import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.model.TabItemInfo;
import com.aliyun.roompaas.beauty_pro.utils.BeautyUtils;
import com.aliyun.roompaas.beauty_pro.utils.FileUtils;
import com.aliyun.roompaas.beauty_pro.utils.ResoureUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.aliyun.roompaas.beauty_common.QueenParamHolder.getQueenParam;


public class OnLutAction extends IItemAction {

    private List<TabItemInfo> mLocalResItemsList = null;

    private final String LUT_NAME = "lookups";
    private final String LUT_PATH = FileUtils.wrapRemoteRes(LUT_NAME);
    private final String LUT_ICON_PATH = "icon" + File.separator + "lut";
    private final String SUFFIX_ICON = ".png";
    private final String SUFFIX_SELECTED_NAME = "_selected" + SUFFIX_ICON;

    private final String PREFIX_STRING_TYPE = ResoureUtils.PREFIX_QUOTE;

    private QueenParam.LUTRecord getParam() { return getQueenParam().lutRecord; }

    private String fullItemPath(TabItemInfo itemInfo) {
        String fileName = itemInfo.itemName.startsWith(PREFIX_STRING_TYPE) ? itemInfo.itemName.substring(1) : itemInfo.itemName;
        return LUT_PATH + File.separator + fileName + SUFFIX_ICON;
    }

    @Override
    public void onAcitonClick(TabItemInfo itemInfo) {
        if (itemInfo.itemId < 0) {
            getParam().lutEnable = false;
        } else {
            getParam().lutEnable = true;
            getParam().lutPath = fullItemPath(itemInfo);
        }
    }

    @Override
    public void onValueChanged(TabItemInfo itemInfo, int value) {
        getParam().lutParam = formatParam(value);
    }

    @Override
    public int getFocusIndex(TabInfo tabInfo) {
        int targetIndex = super.getFocusIndex(tabInfo);
        if (getParam().lutEnable) {
            String curPath = getParam().lutPath;
            int index = 0;
            while (index < mLocalResItemsList.size()) {
                String itemPath = fullItemPath(mLocalResItemsList.get(index));
                if (curPath.equals(itemPath)) {
                    targetIndex = index;
                    break;
                }
                ++index;
            }
        }
        return targetIndex;
    }

    @Override
    public int getValueByItem(TabItemInfo itemInfo) {
        int value = 0;
        if (getParam().lutEnable && getParam().lutPath != null) {
            String fileName = itemInfo.itemName.startsWith(PREFIX_STRING_TYPE) ? itemInfo.itemName.substring(1) : itemInfo.itemName;
            if (getParam().lutPath.contains(fileName)) {
                value = deFormatParam(getParam().lutParam);
            }
        }
        return value;
    }

    @Override
    public List<TabItemInfo> getItemList(TabInfo tabInfo) {
        if (mLocalResItemsList == null) {
            mLocalResItemsList = scanList(tabInfo);
        }
        tabInfo.tabItemInfoList.clear();
        tabInfo.tabItemInfoList.addAll(mLocalResItemsList);
        return tabInfo.tabItemInfoList;
    }

    private List<TabItemInfo> scanList(TabInfo tabInfo) {
        // 获取assets目录assetDir下一级所有文件以及文件夹
        Context context = BeautyUtils.sApplication;
        List<TabItemInfo> itemList = null;

        TabItemInfo noneItemInfo = createNoneItemInfo(tabInfo.tabType);
        try {
            String[] fileNames = FileUtils.listRemoteRes(LUT_NAME);
            itemList = new ArrayList<>(fileNames.length+1);
            itemList.add(noneItemInfo);

            int i = 0;
            while (i < fileNames.length) {
                TabItemInfo itemInfo = new TabItemInfo();
                itemInfo.itemType = tabInfo.tabType;
                itemInfo.itemId = i;
                String fileName = fileNames[i];
                int idx = fileName.indexOf(".");
                String name = fileName.substring(0, idx);
                itemInfo.itemName = PREFIX_STRING_TYPE + name;
                itemInfo.itemIconNormal = LUT_ICON_PATH + File.separator + fileName;
                itemInfo.itemIconSelected = ICON_FOCUS;//LUT_ICON_PATH + File.separator + name + SUFFIX_SELECTED_NAME;
                itemInfo.progressMax = 100;
                itemInfo.progressMin = 0;
                itemList.add(itemInfo);
                ++i;
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
        return itemList;

    }
}
