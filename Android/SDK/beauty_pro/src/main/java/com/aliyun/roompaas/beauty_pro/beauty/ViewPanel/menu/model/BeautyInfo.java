package com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.model;

import java.io.Serializable;
import java.util.List;

public class BeautyInfo implements Serializable {

    public String panelBg;
    public int tabWidth;
    public int tabHeight;
    public int tabNameTextSize;
    public String tabColorNormal;
    public String tabColorSelected;
    public List<TabInfo> tabInfoList;
    public List<TabInfo> scenesList;

}
