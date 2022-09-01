package com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.model;

import java.io.Serializable;
import java.util.List;

public class TabItemInfo implements Comparable<TabItemInfo>, Serializable {
    public int itemId;
    public int parentId;
    public int itemType;
    public String itemName;
    public String itemIconNormal;
    public String itemIconSelected;
    public int progressCur;
    public int progressMax;
    public int progressMin;
    public List<TabItemInfo> subItemInfosList;
    public List<TabItemInfo> diyItemInfoList;

    public boolean hasSubItems() {
        return subItemInfosList != null && subItemInfosList.size() > 0;
    }

    public boolean showProgress() { return progressMax > progressMin && progressMax > 0; }

    @Override
    public int compareTo(TabItemInfo o) {
        return Integer.compare(this.itemId, o.itemId);
    }
}
