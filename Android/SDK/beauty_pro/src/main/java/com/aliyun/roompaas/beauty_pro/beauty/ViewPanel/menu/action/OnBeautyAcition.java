package com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.action;


import com.aliyun.roompaas.beauty_common.QueenParam;
import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.model.TabInfo;
import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.model.TabItemInfo;

import java.io.File;
import java.util.List;

import static com.aliyun.roompaas.beauty_common.QueenParamHolder.getQueenParam;


/**
 * 美颜
 */
public class OnBeautyAcition extends IItemAction {

    private final String BEAUTY_ONE_PATH = "icon" + File.separator + "facebeauty"
            + File.separator + "facebeauty_one" + File.separator;

    private final String SUFFIX_NORMAL_NAME = ".png";
    private final String SUFFIX_SELECTED_NAME = SUFFIX_NORMAL_NAME;//"_selected.png";

    private OnModeStyleAction modeStyleAction = new OnModeStyleAction();

    int ID_SKIN_WHITING = 31;                // "美白"
    int ID_SKIN_RED = 32;                    //"红润";
    int ID_SKIN_BUFFING = 33;                //"磨皮";
    int ID_SKIN_SHARPEN = 34;                //"锐化";
    int ID_FACE_BUFFING_NASOLABIALFOLDS = 35;//"祛法令纹";
    int ID_FACE_BUFFING_POUCH = 36;          //"眼袋";
    int ID_FACE_BUFFING_WHITE_TEETH = 37;    //"白牙";
    int ID_FACE_BUFFING_BRIGHTEN_EYE = 38;   //"亮眼";
    int ID_FACE_BUFFING_LIPSTICK = 39;       //"口红";
    int ID_FACE_BUFFING_BLUSH = 40;          //"腮红";
    int ID_FACE_BUFFING_WRINKLES = 41;       // 祛皱纹
    int ID_FACE_BUFFING_BRIGHTEN_FACE = 42;  // 祛暗沉

    int ID_FACE_BUFFING_LIPSTICK_TUHONG = 101;// "土红";
    int ID_FACE_BUFFING_LIPSTICK_FENHONG = 102;// "粉红";
    int ID_FACE_BUFFING_LIPSTICK_FUGUHONG = 103;// "复古红";
    int ID_FACE_BUFFING_LIPSTICK_ZIHONG = 1044;// "紫红";
    int ID_FACE_BUFFING_LIPSTICK_ZHENGHONG = 105;// "正红";
    int ID_FACE_BUFFING_LIPSTICK_JVHONG = 106;// "橘红";
    int ID_FACE_BUFFING_LIPSTICK_ZI = 107;// "紫";
    int ID_FACE_BUFFING_LIPSTICK_JV = 108;// "橘";
    int ID_FACE_BUFFING_LIPSTICK_HUANG = 109;// "黄";


    private interface QueenParamWeight {
        float BEAUTY_WEIGHT = 1.0f;
    }

    private QueenParam.BasicBeautyRecord getParam() {
        return getQueenParam().basicBeautyRecord;
    }

    @Override
    public void onAcitonClick(TabItemInfo itemInfo) {
        if (ID_FEATURE_NONE == itemInfo.itemId) {
            if (itemInfo.parentId == 0) {
                modeStyleAction.onAcitonClick(itemInfo);
            } else if (itemInfo.parentId == itemInfo.itemType) {
                // 总开关关闭
                disableAllBeauty();
            } else if (ID_FACE_BUFFING_LIPSTICK == itemInfo.parentId) {
                // 关闭口红这个子项的功能
                onBeautyLipstickVlicked(itemInfo.itemId);
            }
        } else if (ID_FACE_BUFFING_LIPSTICK_TUHONG <= itemInfo.itemId
                && itemInfo.itemId <= ID_FACE_BUFFING_LIPSTICK_HUANG) {
            enableAllBeauty();
            onBeautyLipstickVlicked(itemInfo.itemId);
        } else if (ID_SKIN_WHITING > itemInfo.itemId) {
            // 模式选择
            modeStyleAction.onAcitonClick(itemInfo);
        } else {
            enableAllBeauty();
        }
    }

    @Override
    public void onValueChanged(TabItemInfo itemInfo, int value) {
        float paramValue = formatParam(value);
        float changedValue = paramValue;
        if (ID_FACE_BUFFING_LIPSTICK_TUHONG > itemInfo.itemId
            || itemInfo.itemId > ID_FACE_BUFFING_LIPSTICK_HUANG) {  // 口红色号是单选，不需要用指示器表明是否开启
            itemInfo.progressCur = value;
        }
        int itemId = itemInfo.itemId;
        if (ID_SKIN_WHITING == itemId) {
            // "美白"
            changedValue = getParam().enableSkinWhiting ? paramValue : getParam().skinWhitingParam;
            changedValue *= QueenParamWeight.BEAUTY_WEIGHT;
            getParam().skinWhitingParam = changedValue;
        } else if (ID_SKIN_RED == itemId) {
            //"红润";
            changedValue = getParam().enableSkinRed ? paramValue : getParam().skinRedParam;
            changedValue *= QueenParamWeight.BEAUTY_WEIGHT;
            getParam().skinRedParam = changedValue;
        } else if (ID_SKIN_BUFFING == itemId) {
            //"磨皮";
            changedValue = getParam().enableSkinBuffing ? paramValue : getParam().skinBuffingParam;
            changedValue *= QueenParamWeight.BEAUTY_WEIGHT;
            getParam().skinBuffingParam = changedValue;
        } else if (ID_SKIN_SHARPEN == itemId) {
            //"锐化";
            changedValue = getParam().enableSkinBuffing ? paramValue : getParam().skinSharpenParam;
            changedValue *= QueenParamWeight.BEAUTY_WEIGHT;
            getParam().skinSharpenParam = changedValue;
        } else if (ID_FACE_BUFFING_NASOLABIALFOLDS == itemId) {
            //"祛法令纹";
            changedValue = getParam().enableFaceBuffing ? paramValue : getParam().faceBuffingNasolabialFoldsParam;
            changedValue *= QueenParamWeight.BEAUTY_WEIGHT;
            getParam().faceBuffingNasolabialFoldsParam = changedValue;
        } else if (ID_FACE_BUFFING_POUCH == itemId) {
            //"眼袋";
            changedValue = getParam().enableFaceBuffing ? paramValue : getParam().faceBuffingPouchParam;
            changedValue *= QueenParamWeight.BEAUTY_WEIGHT;
            getParam().faceBuffingPouchParam = changedValue;
        } else if (ID_FACE_BUFFING_WHITE_TEETH == itemId) {
            //"白牙";
            changedValue = getParam().enableFaceBuffing ? paramValue : getParam().faceBuffingWhiteTeeth;
            changedValue *= QueenParamWeight.BEAUTY_WEIGHT;
            getParam().faceBuffingWhiteTeeth = changedValue;
        } else if (ID_FACE_BUFFING_BRIGHTEN_EYE == itemId) {
            //"亮眼";
            changedValue = getParam().enableFaceBuffing ? paramValue : getParam().faceBuffingBrightenEye;
            changedValue *= QueenParamWeight.BEAUTY_WEIGHT;
            getParam().faceBuffingBrightenEye = changedValue;
        } else if (ID_FACE_BUFFING_LIPSTICK_TUHONG <= itemInfo.itemId
                && itemInfo.itemId <= ID_FACE_BUFFING_LIPSTICK_HUANG) {
            //"口红";
            changedValue = getParam().enableFaceBuffingLipstick ? paramValue : getParam().faceBuffingLipstick;
            changedValue *= QueenParamWeight.BEAUTY_WEIGHT;
            getParam().faceBuffingLipstick = changedValue;
        } else if (ID_FACE_BUFFING_BLUSH == itemId) {
            //"腮红";
            changedValue = getParam().enableFaceBuffing ? paramValue : getParam().faceBuffingBlush;
            changedValue *= QueenParamWeight.BEAUTY_WEIGHT;
            getParam().faceBuffingBlush = changedValue;
        } else if (ID_FACE_BUFFING_WRINKLES == itemId) {
            //"祛皱纹"
            changedValue = getParam().enableFaceBuffing ? paramValue : getParam().faceBuffingWrinklesParam;
            changedValue *= QueenParamWeight.BEAUTY_WEIGHT;
            getParam().faceBuffingWrinklesParam = changedValue;
        } else if (ID_FACE_BUFFING_BRIGHTEN_FACE == itemId) {
            //"祛暗沉"
            changedValue = getParam().enableFaceBuffing ? paramValue : getParam().faceBuffingBrightenFaceParam;
            changedValue *= QueenParamWeight.BEAUTY_WEIGHT;
            getParam().faceBuffingBrightenFaceParam = changedValue;
        }
    }

    @Override
    public List<TabItemInfo> getDiyItemList(TabInfo tabInfo) {
        List<TabItemInfo> itemInfoList = super.getDiyItemList(tabInfo);

        for (TabItemInfo itemInfo:itemInfoList) {
            itemInfo.progressCur = getValueByItem(itemInfo);

            itemInfo.parentId = tabInfo.tabType;
            if (!itemInfo.itemIconNormal.contains(SUFFIX_NORMAL_NAME)) {
                itemInfo.itemIconNormal = BEAUTY_ONE_PATH + itemInfo.itemIconNormal + SUFFIX_NORMAL_NAME;
                itemInfo.itemIconSelected = BEAUTY_ONE_PATH + itemInfo.itemIconSelected + SUFFIX_SELECTED_NAME;
            }
        }

        return itemInfoList;
    }

    @Override
    public List<TabItemInfo> getSubItemList(TabItemInfo tabItemInfo) {
        List<TabItemInfo> itemInfoList = super.getSubItemList(tabItemInfo);
        return wrapperItemList(itemInfoList, tabItemInfo.itemId);
    }

    @Override
    public List<TabItemInfo> getItemList(TabInfo tabInfo) {
        return modeStyleAction.getItemList(tabInfo);
    }

    @Override
    public int getValueByItem(TabItemInfo itemInfo) {
        int itemId = itemInfo.itemId;
        float paramValue = 0.0f;
        float defaultValue = 0.0f;
        if (ID_SKIN_WHITING == itemId) {
            // "美白"
            paramValue = getParam().enableSkinWhiting ? getParam().skinWhitingParam : defaultValue;
        } else if (ID_SKIN_RED == itemId) {
            //"红润";
            paramValue = getParam().enableSkinRed ? getParam().skinRedParam : defaultValue;
        } else if (ID_SKIN_BUFFING == itemId) {
            //"磨皮";
            paramValue = getParam().enableSkinBuffing ? getParam().skinBuffingParam : defaultValue;
        } else if (ID_SKIN_SHARPEN == itemId) {
            //"锐化";
            paramValue = getParam().enableSkinBuffing ? getParam().skinSharpenParam : defaultValue;
        } else if (ID_FACE_BUFFING_NASOLABIALFOLDS == itemId) {
            //"祛法令纹";
            paramValue = getParam().enableFaceBuffing ? getParam().faceBuffingNasolabialFoldsParam : defaultValue;
        } else if (ID_FACE_BUFFING_POUCH == itemId) {
            //"眼袋";
            paramValue = getParam().enableFaceBuffing ? getParam().faceBuffingPouchParam : defaultValue;
        } else if (ID_FACE_BUFFING_WHITE_TEETH == itemId) {
            //"白牙";
            paramValue = getParam().enableFaceBuffing ? getParam().faceBuffingWhiteTeeth : defaultValue;
        } else if (ID_FACE_BUFFING_BRIGHTEN_EYE == itemId) {
            //"亮眼";
            paramValue = getParam().enableFaceBuffing ? getParam().faceBuffingBrightenEye : defaultValue;
        } else if (ID_FACE_BUFFING_LIPSTICK == itemId ||
                (ID_FACE_BUFFING_LIPSTICK_TUHONG <= itemInfo.itemId
                        && itemInfo.itemId <= ID_FACE_BUFFING_LIPSTICK_HUANG)) {
            //"口红";
            paramValue = getParam().enableFaceBuffingLipstick ? getParam().faceBuffingLipstick : defaultValue;
        } else if (ID_FACE_BUFFING_BLUSH == itemId) {
            //"腮红";
            paramValue = getParam().enableFaceBuffing ? getParam().faceBuffingBlush : defaultValue;
        } else if (ID_FACE_BUFFING_WRINKLES == itemId) {
            //"祛皱纹"
            paramValue = getParam().enableFaceBuffing ? getParam().faceBuffingWrinklesParam : defaultValue;
        } else if (ID_FACE_BUFFING_BRIGHTEN_FACE == itemId) {
            //"祛暗沉"
            paramValue = getParam().enableFaceBuffing ? getParam().faceBuffingBrightenFaceParam : defaultValue;
        }
        // 需要按照param进行换算
        paramValue /= QueenParamWeight.BEAUTY_WEIGHT;
        return deFormatParam(paramValue);
    }

    @Override
    public int getFocusIndex(TabInfo tabInfo) {
        return modeStyleAction.getFocusIndex(tabInfo);
    }

    private void onBeautyLipstickVlicked(int itemId) {
        // 滤镜美妆-口红
        if (itemId == ID_FEATURE_NONE) {
            disableFaceBuffingLipStick();
        } else if (itemId == ID_FACE_BUFFING_LIPSTICK_TUHONG) {
            setFaceBuffingLipStickParam(-0.125f, 0.25f, 0.4f);
        } else if (itemId == ID_FACE_BUFFING_LIPSTICK_FENHONG) {
            setFaceBuffingLipStickParam(-0.1f, 0.125f, 0.0f);
        } else if (itemId == ID_FACE_BUFFING_LIPSTICK_FUGUHONG) {
            setFaceBuffingLipStickParam(0.0f, 1.0f, 0.2f);
        } else if (itemId == ID_FACE_BUFFING_LIPSTICK_ZIHONG) {
            setFaceBuffingLipStickParam(-0.2f, 0.35f, 0.0f);
        } else if (itemId == ID_FACE_BUFFING_LIPSTICK_ZHENGHONG) {
            setFaceBuffingLipStickParam(-0.08f, 1.0f, 0.0f);
        } else if (itemId == ID_FACE_BUFFING_LIPSTICK_JVHONG) {
            setFaceBuffingLipStickParam(0.0f, 0.35f, 0.0f);
        } else if (itemId == ID_FACE_BUFFING_LIPSTICK_ZI) {
            setFaceBuffingLipStickParam(-0.42f, 0.35f, 0.0f);
        } else if (itemId == ID_FACE_BUFFING_LIPSTICK_JV) {
            setFaceBuffingLipStickParam(0.125f, 0.25f, 0.0f);
        } else if (itemId == ID_FACE_BUFFING_LIPSTICK_HUANG) {
            setFaceBuffingLipStickParam(0.25f, 0.45f, 0.0f);
        }
    }

    private void setFaceBuffingLipStickParam(float colorParam, float glossParam, float brightnessParam) {
        getParam().enableFaceBuffingLipstick = true;
        getParam().faceBuffingLipstickColorParams = colorParam * QueenParamWeight.BEAUTY_WEIGHT;
        getParam().faceBuffingLipstickGlossParams = glossParam * QueenParamWeight.BEAUTY_WEIGHT;
        getParam().faceBuffingLipstickBrightnessParams = brightnessParam * QueenParamWeight.BEAUTY_WEIGHT;
    }

    private void disableFaceBuffingLipStick() {
        getParam().enableFaceBuffingLipstick = false;
    }

    private void disableAllBeauty() {
        getParam().enableSkinWhiting = false;
        getParam().enableSkinRed = false;
        getParam().enableFaceBuffing = false;
        getParam().enableSkinBuffing = false;
    }

    private void enableAllBeauty() {
        getParam().enableSkinWhiting = true;
        getParam().enableSkinRed = true;
        getParam().enableFaceBuffing = true;
        getParam().enableSkinBuffing = true;
    }
}
