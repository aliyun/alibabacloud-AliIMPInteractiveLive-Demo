package com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.action;

import android.content.Context;

import com.aliyun.roompaas.beauty_common.QueenParam;
import com.aliyun.roompaas.beauty_common.QueenParamFactory;
import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.model.TabInfo;
import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.model.TabItemInfo;
import com.aliyun.roompaas.beauty_pro.beauty.model.QueenCommonParams;
import com.aliyun.roompaas.beauty_pro.utils.BeautyUtils;
import com.aliyun.roompaas.beauty_pro.utils.FileUtils;
import com.aliyun.roompaas.beauty_pro.utils.ResoureUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.aliyun.roompaas.beauty_common.QueenParamHolder.getQueenParam;


public class OnFaceShapeAciton extends IItemAction {

    private final int TAB_ITEM_TYPE = QueenCommonParams.BeautyType.FACE_SHAPE;

    private final String SHAPE_BASE_PATH = "icon" + File.separator + "faceshape" + File.separator;
    private final String SHAPE_ALL_PATH = SHAPE_BASE_PATH + "shape_all";
    private final String SHAPE_ONE_PATH = SHAPE_BASE_PATH + "shape_one";
    private final String SUFFIX_SELECTED_NAME = "_selected.png";
    private final String PREFIX_STRING_TYPE = ResoureUtils.PREFIX_QUOTE;

    public Map<String, Integer> shapeMap;

    public OnFaceShapeAciton() {
        initShapMap();
    }

    private void initShapMap()
    {
        shapeMap = new HashMap<>(32);
        int id = 0;
        shapeMap.put(QueenParamFactory.FaceShapeParams.TAG_SHAPE_GRACE, ++id);// "优雅";
        shapeMap.put(QueenParamFactory.FaceShapeParams.TAG_SHAPE_DELICATE, ++id);// "精致";
        shapeMap.put(QueenParamFactory.FaceShapeParams.TAG_SHAPE_WANGHONG, ++id);//"网红";
        shapeMap.put(QueenParamFactory.FaceShapeParams.TAG_SHAPE_CUTE, ++id);//"可爱";
        shapeMap.put(QueenParamFactory.FaceShapeParams.TAG_SHAPE_BABY, ++id);//"婴儿";

        id = 99;
        shapeMap.put("cutCheek", ++id);// "颧骨";id = 100开始
        shapeMap.put("cutFace", ++id);//"削脸";
        shapeMap.put("thinFace", ++id);//"瘦脸";
        shapeMap.put("longFace_r", ++id);// "脸长(双向)";
        shapeMap.put("lowerJaw_r", ++id);//"下巴(双向)";
        shapeMap.put("thinJaw", ++id);      //瘦下巴
        shapeMap.put("thinMandible", ++id);      //下颌
        shapeMap.put("bigEye", ++id);      //大眼
        shapeMap.put("canthus_r", ++id);      //眼距(双向)
        shapeMap.put("eyeAngle2_r", ++id);      //眼角(双向)
        shapeMap.put("thinNose_r", ++id);      //瘦鼻(双向)
        shapeMap.put("nosewing_r", ++id);      //鼻翼(双向)
        shapeMap.put("nasalHeight", ++id);      //鼻长
        shapeMap.put("mouthWidth_r", ++id);      //嘴型(双向)
        shapeMap.put("mouthHigh_r", ++id);      //嘴唇厚度(双向)
        shapeMap.put("philtrum_r", ++id);      //人中(双向)
        shapeMap.put("hairLine_r", ++id);      //发际线(双向)
        shapeMap.put("smile", ++id);      //smail微笑
    }

    private QueenParam.FaceShapeRecord getParams() {
        return getQueenParam().faceShapeRecord;
    }

    @Override
    public void onAcitonClick(TabItemInfo itemInfo) {
        if (itemInfo == null || itemInfo.itemName == null) return;

        String name = itemInfo.itemName;
        if (name.startsWith(PREFIX_STRING_TYPE)) {
            name = name.substring(PREFIX_STRING_TYPE.length());
        }
        QueenParam.FaceShapeRecord params = QueenParamFactory.FaceShapeParams.getParams(name);
        if (params != null) {
            getQueenParam().faceShapeRecord = params;
        }
    }

    @Override
    public void onValueChanged(TabItemInfo itemInfo, int value) {
        int oldValue = getValueByItem(itemInfo);
        if (value != oldValue) {
            getParams().enableFaceShape = true;
        }

        float paramValue = itemInfo.progressMin < 0 ? formatReverseParam(value, itemInfo.progressMax) : formatParam(value);
        int itemId = itemInfo.itemId;
        readOrWriteParam(itemId, true, paramValue);

        itemInfo.progressCur = value;
    }

    @Override
    public int getValueByItem(TabItemInfo itemInfo) {
        float paramValue = 0.0f;

        int itemId = itemInfo.itemId;
        paramValue = readOrWriteParam(itemId, false, paramValue);

        return itemInfo.progressMin < 0 ? deFormatReverseParam(paramValue, itemInfo.progressMax) : deFormatParam(paramValue);
    }

    @Override
    public List<TabItemInfo> getItemList(TabInfo tabInfo) {
        List<TabItemInfo> itemInfoList = scan4ShapeAll(tabInfo);
        tabInfo.tabItemInfoList.clear();
        tabInfo.tabItemInfoList.addAll(itemInfoList);
        return itemInfoList;
    }

    @Override
    public List<TabItemInfo> getDiyItemList(TabInfo tabInfo) {
        List<TabItemInfo> shapeOneItemInfoList = scan4ShapeOne();
        for (TabItemInfo itemInfo : shapeOneItemInfoList) {
            itemInfo.progressCur = getValueByItem(itemInfo);
        }
        return shapeOneItemInfoList;
    }

    @Override
    public int getFocusIndex(TabInfo tabInfo) {
        List<TabItemInfo> itemList = getItemList(tabInfo);

        QueenParam.FaceShapeRecord runtimeParam = getQueenParam().faceShapeRecord;
        int targetIndex = 0;
        for (int i = 0; i < itemList.size(); ++i) {
            String name = itemList.get(i).itemName;
            if (name.startsWith(PREFIX_STRING_TYPE)) {
                name = name.substring(PREFIX_STRING_TYPE.length());
            }
            QueenParam.FaceShapeRecord params = QueenParamFactory.FaceShapeParams.getParams(name);
            if (params != null && params.hashCode() == runtimeParam.hashCode()) {
                targetIndex = i;
                break;
            }
        }
        return targetIndex;
    }

    private float readOrWriteParam(int itemId, boolean write, float paramValue) {
        if (100 == itemId) {
            // 颧骨[0,1]
            if (write) { getParams().cutCheekParam = paramValue; }
            else { paramValue = getParams().cutCheekParam; }
        } else if (101 == itemId) {
            // 削脸[0,1]
            if (write) { getParams().cutFaceParam = paramValue; }
            else { paramValue = getParams().cutFaceParam; }
        } else if (102 == itemId) {
            //瘦脸[0,1]
            if (write) { getParams().thinFaceParam = paramValue; }
            else { paramValue = getParams().thinFaceParam; }
        } else if (103 == itemId) {
            //脸长(双向)
            if (write) { getParams().longFaceParam = paramValue; }
            else { paramValue = getParams().longFaceParam; }
        } else if (104 == itemId) {
            //下巴(双向)
            if (write) { getParams().lowerJawParam = paramValue; }
            else { paramValue = getParams().lowerJawParam; }
        } else if (105 == itemId) {
            //瘦下巴[0,1]
            if (write) { getParams().thinJawParam = paramValue; }
            else { paramValue = getParams().thinJawParam; }
        } else if (106 == itemId) {
            //下颌
            if (write) { getParams().thinMandibleParam = paramValue; }
            else { paramValue = getParams().thinMandibleParam; }
        } else if (107 == itemId) {
            //大眼
            if (write) { getParams().bigEyeParam = paramValue; }
            else { paramValue = getParams().bigEyeParam; }
        } else if (108 == itemId) {
            //眼距(双向)
            if (write) { getParams().canthusParam = paramValue; }
            else { paramValue = getParams().canthusParam; }
        } else if (109 == itemId) {
            //眼角(双向)
            if (write) { getParams().eyeAngle2Param = paramValue; }
            else { paramValue = getParams().eyeAngle2Param; }
        } else if (110 == itemId) {
            //瘦鼻(双向)
            if (write) { getParams().thinNoseParam = paramValue; }
            else { paramValue = getParams().thinNoseParam; }
        } else if (111 == itemId) {
            //鼻翼(双向)id
            if (write) { getParams().nosewingParam = paramValue; }
            else { paramValue = getParams().nosewingParam; }
        } else if (112 == itemId) {
            //鼻长
            if (write) { getParams().nasalHeightParam = paramValue; }
            else { paramValue = getParams().nasalHeightParam; }
        } else if (113 == itemId) {
            //嘴型(双向)
            if (write) { getParams().mouthWidthParam = paramValue; }
            else { paramValue = getParams().mouthWidthParam; }
        } else if (114 == itemId) {
            //嘴唇厚度(双向)
            if (write) { getParams().mouthHighParam = paramValue; }
            else { paramValue = getParams().mouthHighParam; }
        } else if (115 == itemId) {
            //人中(双向)
            if (write) { getParams().philtrumParam = paramValue; }
            else { paramValue = getParams().philtrumParam; }
        } else if (116 == itemId) {
            //发际线(双向)
            if (write) { getParams().hairLineParam = paramValue; }
            else { paramValue = getParams().hairLineParam; }
        } else if (117 == itemId) {
            //smail微笑
            if (write) { getParams().smailParam = paramValue; }
            else { paramValue = getParams().smailParam; }
        }

        return paramValue;
    }

    private List<TabItemInfo> scan4ShapeAll(TabInfo tabInfo) {
        // 获取assets目录assetDir下一级所有文件以及文件夹
        Context context = BeautyUtils.sApplication;
        List<TabItemInfo> itemList = null;
        try {
            String[] fileNames = FileUtils.listRemoteRes(SHAPE_ALL_PATH);
            itemList = new ArrayList<>(fileNames.length + 1);
            TabItemInfo noneItemInfo = createNoneItemInfo(tabInfo.tabType);
            itemList.add(noneItemInfo);
            tabInfo.tabDefaultSelectedIndex += 1;

            for (String fileName:fileNames) {
                if (fileName.contains(SUFFIX_SELECTED_NAME)) continue;

                TabItemInfo itemInfo = new TabItemInfo();
                itemInfo.itemType = tabInfo.tabType;

                int idx = fileName.indexOf(".");
                String name = fileName.substring(0, idx);
                itemInfo.itemId = shapeMap.get(name);
                itemInfo.itemName = PREFIX_STRING_TYPE + name;
                itemInfo.itemIconNormal = SHAPE_ALL_PATH + File.separator + fileName;
                itemInfo.itemIconSelected = SHAPE_ALL_PATH + File.separator + name + SUFFIX_SELECTED_NAME;
                itemList.add(itemInfo);
            }
        } catch (Exception e) { e.printStackTrace();}
        return itemList;
    }

    private List<TabItemInfo> scan4ShapeOne() {
        // 获取assets目录assetDir下一级所有文件以及文件夹
        Context context = BeautyUtils.sApplication;
        List<TabItemInfo> itemList = null;
        try {
            String[] fileNames = FileUtils.listRemoteRes(SHAPE_ONE_PATH);
            itemList = new ArrayList<>(fileNames.length);

            for (String fileName:fileNames) {
                if (fileName.contains(SUFFIX_SELECTED_NAME)) continue;

                TabItemInfo itemInfo = new TabItemInfo();
                itemInfo.itemType = TAB_ITEM_TYPE;

                int idx = fileName.indexOf(".");
                String name = fileName.substring(0, idx);
                itemInfo.itemId = shapeMap.get(name);
                itemInfo.itemName = PREFIX_STRING_TYPE + name;
                itemInfo.itemIconNormal = SHAPE_ONE_PATH + File.separator + fileName;
                itemInfo.itemIconSelected = SHAPE_ONE_PATH + File.separator + name + SUFFIX_SELECTED_NAME;
                if (name.contains("_r")) {
                    // 双向的强度调节
                    itemInfo.progressMax = 100;
                    itemInfo.progressMin = -100;
                } else {
                    itemInfo.progressMax = 100;
                    itemInfo.progressMin = 0;
                }

                itemList.add(itemInfo);
            }
        } catch (Exception e) { e.printStackTrace();}
        return itemList;
    }

}
