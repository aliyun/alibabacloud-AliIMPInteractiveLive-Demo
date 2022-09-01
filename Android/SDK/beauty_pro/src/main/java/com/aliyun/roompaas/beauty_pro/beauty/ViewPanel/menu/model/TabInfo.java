package com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.model;

import java.io.Serializable;
import java.util.List;

public class TabInfo implements Serializable {

    public int tabId;
    public int tabType;
    public boolean diyEnable;
    public String tabName;
    public String tabColorNormal;
    public String tabColorSelected;
    public int tabDefaultSelectedIndex;
    public List<TabItemInfo> tabItemInfoList;
    public List<TabItemInfo> diyItemInfoList;

}
