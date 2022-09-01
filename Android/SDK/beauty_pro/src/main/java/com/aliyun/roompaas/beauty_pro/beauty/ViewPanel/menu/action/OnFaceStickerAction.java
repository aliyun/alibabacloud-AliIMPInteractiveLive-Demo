package com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.action;

import android.content.Context;

import com.aliyun.roompaas.beauty_common.QueenParam;
import com.aliyun.roompaas.beauty_pro.R;
import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.model.TabInfo;
import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.model.TabItemInfo;
import com.aliyun.roompaas.beauty_pro.utils.BeautyUtils;
import com.aliyun.roompaas.beauty_pro.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.aliyun.roompaas.beauty_common.QueenParamHolder.getQueenParam;


public class OnFaceStickerAction extends IItemAction {

    private final String STICKER_NAME = "sticker";
    private final String STICKER_PATH = FileUtils.wrapRemoteRes(STICKER_NAME);
    private final static String STICKER_DEFAULT_ICON_NAME = "icon.png";

    private List<TabItemInfo> mLocalResItemsList = null;

    private QueenParam.StickerRecord getParam() { return getQueenParam().stickerRecord; }

    private String fullStickerPath(int itemId) {
        return STICKER_PATH + File.separator + itemId;
    }

    @Override
    public void onAcitonClick(TabItemInfo itemInfo) {
        if (itemInfo.itemId < 0) {
            getParam().stickerEnable = false;
        } else {
            getParam().stickerEnable = true;
            getParam().stickerPath = fullStickerPath(itemInfo.itemId);
        }
    }

    @Override
    public int getFocusIndex(TabInfo tabInfo) {
        int targetIndex = 0;
        if (!getParam().stickerEnable || mLocalResItemsList.size() == 0) return targetIndex;
        String path = getParam().stickerPath;
        if (path != null && path.length() > 0) {
            int index = 0;
            while (index < mLocalResItemsList.size()) {
                TabItemInfo itemInfo = mLocalResItemsList.get(index);
                if (path.equals(fullStickerPath(itemInfo.itemId))) {
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
        return 0;
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

        try {
            String[] fileNames = FileUtils.listRemoteRes(STICKER_NAME);
            itemList = new ArrayList<>(fileNames.length+1);
            TabItemInfo noneItemInfo = createNoneItemInfo(tabInfo.tabType);
            itemList.add(noneItemInfo);
            tabInfo.tabDefaultSelectedIndex += 1;
            String name = context.getResources().getString(R.string.face_sticker);
            int i = 0;
            while (i < fileNames.length) {
                TabItemInfo itemInfo = new TabItemInfo();
                itemInfo.itemType = tabInfo.tabType;
                itemInfo.itemId = Integer.parseInt(fileNames[i]);
                itemInfo.itemName = name + (i+1);
                itemInfo.itemIconNormal = STICKER_NAME + File.separator + fileNames[i] + File.separator + STICKER_DEFAULT_ICON_NAME;
                itemInfo.itemIconSelected = ICON_FOCUS;
                itemList.add(itemInfo);
                ++i;
            }
        }catch (Exception ignore) {
        }
        return itemList;
    }
}
