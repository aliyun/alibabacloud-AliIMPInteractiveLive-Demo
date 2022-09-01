package com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.action;

import com.aliyun.roompaas.beauty_common.QueenParam;
import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.model.TabInfo;
import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.model.TabItemInfo;
import com.aliyun.roompaas.beauty_pro.utils.FileUtils;
import com.aliyun.roompaas.beauty_pro.utils.ResoureUtils;
import com.taobao.android.libqueen.models.BlendType;
import com.taobao.android.libqueen.models.MakeupType;

import java.io.File;
import java.util.List;

import static com.aliyun.roompaas.beauty_common.QueenParamHolder.getQueenParam;


public class OnFaceMakeupAction extends IItemAction {

    private final String MAKEUP_RES_NAME = "makeup";
    private final String MAKEUP_RES_PATH = FileUtils.wrapRemoteRes(MAKEUP_RES_NAME);
    private final static String ICON_BASE_PATH = "icon/makeup/";
    private final String PREFIX_STRING_TYPE = ResoureUtils.PREFIX_QUOTE;

    private final static int K_MAKEUP_SINGEL_MIN_ID = 100;      // 独立妆的起始id

    private QueenParam.FaceMakeupRecord getParam() {
        return getQueenParam().faceMakeupRecord;
    }

    private String fullItemPath(TabItemInfo itemInfo) {
        String itemName = itemInfo.itemName.startsWith(PREFIX_STRING_TYPE) ? itemInfo.itemName.substring(1) : itemInfo.itemName;
        return MAKEUP_RES_PATH + File.separator + itemName + ".png";
    }

    @Override
    public void onAcitonClick(TabItemInfo itemInfo) {
        if (itemInfo.itemId == ID_FEATURE_NONE) {
            if (itemInfo.parentId == 0) {
                getParam().enableFaceMakeup = false;
            } else {
                // 关闭独立妆的特定部分
                getParam().makeupResourcePath[MakeupType.kMakeupWhole] = null;
                int type = itemInfo.parentId - K_MAKEUP_SINGEL_MIN_ID;
                getParam().makeupResourcePath[type] = null;
            }
            return;
        }

        getParam().enableFaceMakeup = true;
        String resPath = fullItemPath(itemInfo);
        if (itemInfo.itemId < K_MAKEUP_SINGEL_MIN_ID) {
            // 整妆
            getParam().makeupResourcePath[MakeupType.kMakeupWhole] = resPath;
            getParam().makeupBlendType[MakeupType.kMakeupWhole] = BlendType.kBlendLabMix;
        } else {
            // 独立妆
            getParam().makeupResourcePath[MakeupType.kMakeupWhole] = null;
            int type = itemInfo.itemId - K_MAKEUP_SINGEL_MIN_ID;
            getParam().makeupResourcePath[type] = resPath;
        }
    }

    @Override
    public int getFocusIndex(TabInfo tabInfo) {
        int targetIndex = 0;
        if (getParam().enableFaceMakeup) {
            String curWholePath = getParam().makeupResourcePath[MakeupType.kMakeupWhole];
            if (curWholePath != null) {
                List<TabItemInfo> itemList = getItemList(tabInfo);
                int index = 0;
                while (index < itemList.size()) {
                    if (curWholePath.equals(fullItemPath(itemList.get(index)))) {
                        targetIndex = index;
                        break;
                    }
                    ++index;
                }
            }
        }
        return targetIndex;
    }

    @Override
    public void onValueChanged(TabItemInfo itemInfo, int value) {
        int type = itemInfo.itemId > K_MAKEUP_SINGEL_MIN_ID ? (itemInfo.itemId-K_MAKEUP_SINGEL_MIN_ID) : MakeupType.kMakeupWhole;
        getParam().makeupAlpha[type] = formatParam(value);
    }

    @Override
    public int getValueByItem(TabItemInfo itemInfo) {
        int type = itemInfo.itemId > K_MAKEUP_SINGEL_MIN_ID ? (itemInfo.itemId-K_MAKEUP_SINGEL_MIN_ID) : MakeupType.kMakeupWhole;
        float paramValue = getParam().makeupAlpha[type];
        return deFormatParam(paramValue);
    }

    @Override
    public List<TabItemInfo> getItemList(TabInfo tabInfo) {
        List<TabItemInfo> itemInfoList = tabInfo.tabItemInfoList;
        return wrapperItemList(itemInfoList);
    }

    @Override
    public List<TabItemInfo> getDiyItemList(TabInfo tabInfo) {
        List<TabItemInfo> itemInfoList = tabInfo.diyItemInfoList;
        return wrapperItemList(itemInfoList, false);
    }

    @Override
    public List<TabItemInfo> getSubItemList(TabItemInfo tabItemInfo) {
        List<TabItemInfo> itemInfoList = super.getSubItemList(tabItemInfo);
        return wrapperItemList(itemInfoList, tabItemInfo.itemId, true);
    }

    @Override
    protected List<TabItemInfo> wrapperItemList(List<TabItemInfo> itemInfoList) {
        return wrapperItemList(itemInfoList, true);
    }

    private List<TabItemInfo> wrapperItemList(List<TabItemInfo> itemInfoList, boolean needAddNoneItem) {
        if (itemInfoList == null || itemInfoList.size() == 0)
            return itemInfoList;

        if (needAddNoneItem && itemInfoList.get(0).itemId != ID_FEATURE_NONE) {
            TabItemInfo noneItemInfo = createNoneItemInfo(itemInfoList.get(0).itemType);
            itemInfoList.add(0, noneItemInfo);
        }
        for (TabItemInfo itemInfo : itemInfoList) {
            if (itemInfo.itemId == ID_FEATURE_NONE) continue;

            if (!itemInfo.itemIconNormal.startsWith(ICON_BASE_PATH)) {
                itemInfo.itemIconNormal = ICON_BASE_PATH + itemInfo.itemIconNormal;
            }
            itemInfo.itemIconSelected = ICON_FOCUS;
        }
        return itemInfoList;
    }
}
