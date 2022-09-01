package com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.action;

import com.aliyun.roompaas.beauty_common.QueenParam;
import com.aliyun.roompaas.beauty_common.QueenParamFactory;
import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.model.TabInfo;
import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.model.TabItemInfo;

import java.util.List;

import static com.aliyun.roompaas.beauty_common.QueenParamHolder.getQueenParam;


public class OnModeStyleAction extends IItemAction {

    @Override
    public List<TabItemInfo> getItemList(TabInfo tabInfo) {
        List<TabItemInfo> itemInfoList = super.getItemList(tabInfo);
        if (itemInfoList == null || itemInfoList.size() == 0)
            return itemInfoList;

        if (itemInfoList.get(0).itemId != ID_FEATURE_NONE) {
            TabItemInfo noneItemInfo = createNoneItemInfo(tabInfo.tabType);
            itemInfoList.add(0, noneItemInfo);
            tabInfo.tabDefaultSelectedIndex += 1;   // 需要顺延一位
        }
        for (TabItemInfo itemInfo : itemInfoList) {
            if (itemInfo.itemId == ID_FEATURE_NONE) continue;

            itemInfo.itemIconNormal = ICON_NORMAL;
            itemInfo.itemIconSelected = ICON_FOCUS;
        }
        return itemInfoList;
    }

    @Override
    public void onAcitonClick(TabItemInfo itemInfo) {
        QueenParam.BasicBeautyRecord beautyParams = QueenParamFactory.BeautyParams.getParams(itemInfo.itemId);
        getQueenParam().basicBeautyRecord = beautyParams;
    }

    @Override
    public int getFocusIndex(TabInfo tabInfo) {
        List<TabItemInfo> itemList = getItemList(tabInfo);
        int targetIndex = 0;
        QueenParam.BasicBeautyRecord runtimeParam = getQueenParam().basicBeautyRecord;
        for (int i = 0; i < itemList.size(); ++i) {
            QueenParam.BasicBeautyRecord param = QueenParamFactory.BeautyParams.getParams(itemList.get(i).itemId);
            if (param != null && param.hashCode() == runtimeParam.hashCode()) {
                targetIndex = i;
                break;
            }
        }
        return targetIndex;
    }

/*
    private void doActionForOrigin() {
        // 原生效果
    }

    private void doActionForSimple() {

//        // 滤镜1
//        getQueenParam().lutRecord.lutEnable = true;
//        getQueenParam().lutRecord.lutPath = LUT_1_PATH;
//
//        // 美型
//        getQueenParam().faceShapeRecord.enableFaceShape = false;
//
//        // 美妆
//        getQueenParam().faceMakeupRecord.enableFaceMakeup = false;
//        // 贴纸
//        getQueenParam().stickerRecord.stickerEnable = false;
    }

    private void doActionForFashion() {

//        // 滤镜1
//        getQueenParam().lutRecord.lutEnable = true;
//        getQueenParam().lutRecord.lutPath = LUT_27_PATH;
//        // 美型
//        getQueenParam().faceShapeRecord.enableFaceShape = true;
//        getQueenParam().faceShapeRecord.thinFaceParam = 0.88f;
//        getQueenParam().faceShapeRecord.bigEyeParam = 0.82f;
//        getQueenParam().faceShapeRecord.nosewingParam = 0.33f;
//        getQueenParam().faceShapeRecord.thinNoseParam = 0.88f;
//        getQueenParam().faceShapeRecord.thinJawParam = 0.3f;
//
//        // 美妆
//        getQueenParam().faceMakeupRecord.enableFaceMakeup = false;
//        // 贴纸
//        getQueenParam().stickerRecord.stickerEnable = false;
    }

    private void doActionForMeiHuo() {

//        // 滤镜8
//        getQueenParam().lutRecord.lutEnable = true;
//        getQueenParam().lutRecord.lutPath = LUT_8_PATH;
//        // 美型
//        getQueenParam().faceShapeRecord.enableFaceShape = true;
//        getQueenParam().faceShapeRecord.thinFaceParam = 0.88f;
//        getQueenParam().faceShapeRecord.bigEyeParam = 0.82f;
//        getQueenParam().faceShapeRecord.nosewingParam = 0.33f;
//        getQueenParam().faceShapeRecord.thinNoseParam = 0.88f;
//        getQueenParam().faceShapeRecord.thinJawParam = 0.3f;
//        // 美妆
//        getQueenParam().faceMakeupRecord.enableFaceMakeup = true;
//        getQueenParam().faceMakeupRecord.makeupResourcePath[MakeupType.kMakeupWhole] = FACE_MAKEUP_TYPE_MITAO_PATH;
//        getQueenParam().faceMakeupRecord.makeupBlendType[MakeupType.kMakeupWhole] = BlendType.kBlendNormal;
//
//        // 贴纸
//        getQueenParam().stickerRecord.stickerEnable = false;
    }

    private void doActionForKEAI() {

//        // 滤镜10
//        getQueenParam().lutRecord.lutEnable = true;
//        getQueenParam().lutRecord.lutPath = LUT_5_PATH;
//        // 美型
//        getQueenParam().faceShapeRecord.enableFaceShape = true;
//        getQueenParam().faceShapeRecord.thinFaceParam = 0.88f;
//        getQueenParam().faceShapeRecord.bigEyeParam = 0.82f;
//        getQueenParam().faceShapeRecord.nosewingParam = 0.33f;
//        getQueenParam().faceShapeRecord.thinNoseParam = 0.88f;
//        getQueenParam().faceShapeRecord.thinJawParam = 0.3f;
//        // 美妆
//        getQueenParam().faceMakeupRecord.enableFaceMakeup = true;
//        getQueenParam().faceMakeupRecord.makeupResourcePath[MakeupType.kMakeupWhole] = FACE_MAKEUP_TYPE_YOUYA_PATH;
//        getQueenParam().faceMakeupRecord.makeupBlendType[MakeupType.kMakeupWhole] = BlendType.kBlendNormal;
//        // 贴纸12
//        getQueenParam().stickerRecord.stickerEnable = true;
//        getQueenParam().stickerRecord.stickerPath = CameraPanelBeanHelper.ContentDefine.STICKER_DIR_PATH + 12;

    }

    private void doAcitonForSHAONV() {

//        // 滤镜10
//        getQueenParam().lutRecord.lutEnable = true;
//        getQueenParam().lutRecord.lutPath = LUT_10_PATH;
//        // 美型
//        getQueenParam().faceShapeRecord.enableFaceShape = true;
//        getQueenParam().faceShapeRecord.thinFaceParam = 0.88f;
//        getQueenParam().faceShapeRecord.bigEyeParam = 0.82f;
//        getQueenParam().faceShapeRecord.nosewingParam = 0.33f;
//        getQueenParam().faceShapeRecord.thinNoseParam = 0.88f;
//        getQueenParam().faceShapeRecord.thinJawParam = 0.3f;
//        // 美妆
//        getQueenParam().faceMakeupRecord.enableFaceMakeup = true;
//        getQueenParam().faceMakeupRecord.makeupResourcePath[MakeupType.kMakeupWhole] = FACE_MAKEUP_TYPE_YUANQISHAONV_PATH;
//        getQueenParam().faceMakeupRecord.makeupBlendType[MakeupType.kMakeupWhole] = BlendType.kBlendNormal;
//        // 贴纸12
//        getQueenParam().stickerRecord.stickerEnable = true;
//        getQueenParam().stickerRecord.stickerPath = CameraPanelBeanHelper.ContentDefine.STICKER_DIR_PATH + 3;
    }
    */
}