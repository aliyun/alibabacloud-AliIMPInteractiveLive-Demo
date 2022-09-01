package com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.action;

import com.aliyun.roompaas.beauty_common.QueenParam;
import com.aliyun.roompaas.beauty_common.QueenParamHolder;
import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.model.TabInfo;
import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.model.TabItemInfo;

import java.util.List;

public class OnBlackTechnologyAction extends IItemAction {

    private final int ID_ET_HUMAN = 1;
    private final int ID_FORCE_SMILE = 2;
    private final int ID_FORCE_CRY = 3;

    @Override
    public void onAcitonClick(TabItemInfo itemInfo) {
        int itemId = itemInfo.itemId;
        switch (itemId) {
            case ID_FEATURE_NONE: disableAllFeature(); break;
            case ID_ET_HUMAN: doActionForETHuman(); break;
            case ID_FORCE_SMILE: doActionForSmile(); break;
            case ID_FORCE_CRY: doActionForCry(); break;
        }
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

    private void disableAllFeature() {
        resetAllFeatures();
    }

    private void resetAllFeatures() {
        // 重新创建一个新的型变参数记录
        QueenParamHolder.getQueenParam().basicBeautyRecord = new QueenParam.BasicBeautyRecord();
        QueenParamHolder.getQueenParam().faceShapeRecord = new QueenParam.FaceShapeRecord();
    }

    private void doAcitonBase() {

        resetAllFeatures();

        // 全局都要
        QueenParamHolder.getQueenParam().basicBeautyRecord.enableFaceBuffing = true;
        QueenParamHolder.getQueenParam().basicBeautyRecord.faceBuffingPouchParam = 0.6f; //去眼袋[0,1]
        QueenParamHolder.getQueenParam().basicBeautyRecord.faceBuffingNasolabialFoldsParam = 0.6f; //去法令纹[0,1]
        QueenParamHolder.getQueenParam().basicBeautyRecord.faceBuffingWhiteTeeth = 0.2f; //白牙[0,1]
        QueenParamHolder.getQueenParam().basicBeautyRecord.faceBuffingBrightenEye = 0.2f; //亮眼[0,1]
        QueenParamHolder.getQueenParam().basicBeautyRecord.faceBuffingLipstick = 0.15f; // 滤镜美妆：口红[0,1]
        QueenParamHolder.getQueenParam().basicBeautyRecord.enableFaceBuffingLipstick = true; // 滤镜美妆：口红开关
        QueenParamHolder.getQueenParam().basicBeautyRecord.faceBuffingBlush = 0.15f; // 滤镜美妆：腮红[0,1]
        QueenParamHolder.getQueenParam().basicBeautyRecord.faceBuffingWrinklesParam = 0.0f; // 祛皱纹[0,1]
        QueenParamHolder.getQueenParam().basicBeautyRecord.faceBuffingBrightenFaceParam = 0.0f; // 祛暗沉[0,1]
    }

    private void doActionForETHuman() {
        doAcitonBase();
        // 美型
        QueenParamHolder.getQueenParam().faceShapeRecord.enableFaceShape = true;
//        getQueenParam().faceShapeRecord.thinFaceParam = 1.98f;
        QueenParamHolder.getQueenParam().faceShapeRecord.lowerJawParam = 3.0f;       // xiaba
        QueenParamHolder.getQueenParam().faceShapeRecord.longFaceParam = 3.0f;   // lianchang
        QueenParamHolder.getQueenParam().faceShapeRecord.cutFaceParam = 3.0f;    // xiaolian
        QueenParamHolder.getQueenParam().faceShapeRecord.cutCheekParam = 3.0f;   // quangu
        QueenParamHolder.getQueenParam().faceShapeRecord.thinMandibleParam = 3.0f;   // xiahe
        QueenParamHolder.getQueenParam().faceShapeRecord.bigEyeParam = 8.0f;
    }

    private void doActionForSmile() {
        doAcitonBase();
        // 美型
        QueenParamHolder.getQueenParam().faceShapeRecord.enableFaceShape = true;
        QueenParamHolder.getQueenParam().faceShapeRecord.smailParam = 2.0f;
    }

    private void doActionForCry() {
        doAcitonBase();
        // 美型
        QueenParamHolder.getQueenParam().faceShapeRecord.enableFaceShape = true;
        QueenParamHolder.getQueenParam().faceShapeRecord.mouthWidthParam = 4.0f;
    }

}