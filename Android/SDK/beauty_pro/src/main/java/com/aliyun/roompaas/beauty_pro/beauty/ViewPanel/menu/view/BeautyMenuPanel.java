package com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.aliyun.roompaas.beauty_pro.R;
import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.BeautyPanelBeanHelper;
import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.adapter.BeautyItemAdapter;
import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.adapter.BeautyTabAdapter;
import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.model.BeautyInfo;
import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.model.TabInfo;
import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.model.TabItemInfo;
import com.aliyun.roompaas.beauty_pro.beauty.model.QueenCommonParams;
import com.aliyun.roompaas.uibase.util.AppUtil;
import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.BeautyPanelController;
import com.aliyun.roompaas.beauty_pro.utils.BeautyUtils;
import com.aliyun.roompaas.beauty_pro.utils.ResoureUtils;

import java.util.List;

public class BeautyMenuPanel extends FrameLayout {

    private Context mContext;
    private BeautyMenuSeekPanel mMenuSeekPanel;
    private SimpleHorizontalScrollView  mTabsScrollView;
    private SimpleHorizontalScrollView  mTabItemsScrollView;

    private LinearLayout mTabItemsParentView;
    private LinearLayout mTabItemsSonView;

    private TabInfo mCurTabInfo;

    private BeautyScrollMenuSubPanel mSimpleSubMenuPanel;
    private BeautyInfo mBeautyInfo = BeautyUtils.getDefaultBeautyInfo();

    private BeautyPanelController mBeautyPanelController;

    private BeautyItemAdapter mCurItemListAdapter;

    int ITEM_4_CONFIG_DIY = -1;
    int ITEM_4_CONFIG_AUTO = -2;

    private int BASE_SUB_TYPE = 0;
    private final int BASE_SUB_TYPE_STEP = 100000;

    public BeautyMenuPanel(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public BeautyMenuPanel(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public BeautyMenuPanel(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public void setBeautyPanelController(BeautyPanelController panelController) {
        mBeautyPanelController = panelController;
        mMenuSeekPanel.setOnProgressChangeListener(mBeautyPanelController);

        handleFocusDefaultTab();
    }

    public void onShowMenu() {
        // TODO: 刷新頁面
        if (mCurItemListAdapter != null) {
            int focusIndex = mBeautyPanelController.getFocusIndex(mCurTabInfo);
            mCurItemListAdapter.updateFocusIndex(focusIndex);
            mCurItemListAdapter.notifyDataSetChanged();
        }
    }

    public void onHideMenu() {

    }

    private void handleFocusDefaultTab() {
        TabInfo defaultTabInfo = mBeautyInfo.tabInfoList.get(0);
        List<TabItemInfo> itemList = mBeautyPanelController.getTabItemList(defaultTabInfo);
        TabItemInfo defaultTabItemInfo = itemList.get(defaultTabInfo.tabDefaultSelectedIndex);
        handleSeekbarShow(defaultTabItemInfo);

        handleTabItemClicked(defaultTabItemInfo, defaultTabInfo.tabDefaultSelectedIndex);

        handleTabChanged(defaultTabInfo);
    }

    private void initView(final Context context) {
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.menu_panel_layout_beauty, this);
        final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.BOTTOM;
        setLayoutParams(params);

        mTabsScrollView = findViewById(R.id.beauty_panel_tabs);
        mTabItemsScrollView = findViewById(R.id.beauty_panel_tab_items);
        mMenuSeekPanel = findViewById(R.id.beauty_rl_seek_bar);

        mBeautyInfo = BeautyUtils.getDefaultBeautyInfo();
        BeautyTabAdapter tabAdapter = new BeautyTabAdapter(context, mBeautyInfo);
        mTabsScrollView.setAdapter(tabAdapter);
        tabAdapter.setOnTabClickListener(new BeautyTabAdapter.OnTabChangeListener() {
            @Override
            public void onTabChange(TabInfo tabInfo, int position) {
                // tab切换
                TabItemInfo selectedTabItemInfo = handleTabChanged(tabInfo);
                handleSeekbarShow(selectedTabItemInfo);
            }
        });

        // 默认第几个tab
        BeautyItemAdapter tabItemAdapter = new BeautyItemAdapter(context, mBeautyInfo.tabColorNormal, mBeautyInfo.tabColorSelected);
        mTabItemsScrollView.setAdapter(tabItemAdapter);
        tabItemAdapter.setOnItemClickListener(mOnItemClickListener);
    }

    private View getTabItemsParentItemView() {
        if (mTabItemsParentView == null) {
            createTabItemsView4ConfigAuto();
        }
        return mTabItemsParentView;
    }

    private View getTabItemsSonItemView() {
        if (mTabItemsSonView == null) {
            createTabItemsView4ConfigDiy();
        }
        return mTabItemsSonView;
    }

    private void handleSeekbarShow(final TabItemInfo tabItemInfo) {
        int visible = tabItemInfo.showProgress() ? View.VISIBLE : View.GONE;
        mMenuSeekPanel.setVisibility(visible);
        if (tabItemInfo != null && tabItemInfo.showProgress()) {
            int value = mBeautyPanelController.getValueById(tabItemInfo);
            mMenuSeekPanel.updateProgressViewData(tabItemInfo, value);
        }
    }

    private TabItemInfo handleTabChanged(final TabInfo tabInfo) {
        mCurTabInfo = tabInfo;
        BASE_SUB_TYPE = 0;

         handleTips4Features(tabInfo.tabType);

        Adapter tabItemAdapter = mTabItemsScrollView.getAdapter();
        List<TabItemInfo> itemList = mBeautyPanelController.getTabItemList(tabInfo);
        int defaultFocusIndex = mBeautyPanelController.getFocusIndex(tabInfo);
        int focusIdx = defaultFocusIndex;
//        int focusIdx = BeautyPanelBeanHelper.getIndexByItemType(tabInfo.tabType + BASE_SUB_TYPE, defaultFocusIndex);
        if (focusIdx >= itemList.size()) {
            int id = tabInfo.tabType + BASE_SUB_TYPE;
            new Exception(tabInfo.tabName + " for " + id).printStackTrace();
            focusIdx = 0;
        }

        mCurItemListAdapter = (BeautyItemAdapter)tabItemAdapter;
        mCurItemListAdapter.setData(itemList, focusIdx);

        // 防止列表无数据时,列表项靠右靠齐,显示跑偏.
        if (tabInfo.diyEnable && itemList.size() > 1) {
            getTabItemsSonItemView().setVisibility(View.VISIBLE);
        } else {
            getTabItemsSonItemView().setVisibility(View.GONE);
        }
        getTabItemsParentItemView().setVisibility(View.GONE);
        TabItemInfo tabItemInfo = itemList.get(focusIdx);
        return tabItemInfo;
    }

    private void handleTabItemClicked(TabItemInfo tabItemInfo, int position) {
        if (position == ITEM_4_CONFIG_DIY) {
            // 展开子页面
            BASE_SUB_TYPE += BASE_SUB_TYPE_STEP;
            Adapter tabItemAdapter = mTabItemsScrollView.getAdapter();
            int focusIdx = -1;//BeautyPanelBeanHelper.getIndexByItemType(mCurTabInfo.tabType + BASE_SUB_TYPE, -1);
            List<TabItemInfo> itemList = mBeautyPanelController.getDiyTabItemList(mCurTabInfo);
            mCurItemListAdapter = (BeautyItemAdapter)tabItemAdapter;
            mCurItemListAdapter.setData(itemList, focusIdx);

            updateConfigAutoResource(mCurTabInfo);
            getTabItemsParentItemView().setVisibility(View.VISIBLE);
            getTabItemsSonItemView().setVisibility(View.GONE);
        } else if (position == ITEM_4_CONFIG_AUTO) {
            BASE_SUB_TYPE -= BASE_SUB_TYPE_STEP;
            Adapter tabItemAdapter = mTabItemsScrollView.getAdapter();
            int focusIdx = mBeautyPanelController.getFocusIndex(mCurTabInfo);//BeautyPanelBeanHelper.getIndexByItemType(mCurTabInfo.tabType + BASE_SUB_TYPE, -1);
            List<TabItemInfo> itemList = mBeautyPanelController.getTabItemList(mCurTabInfo);
            mCurItemListAdapter = (BeautyItemAdapter)tabItemAdapter;
            mCurItemListAdapter.setData(itemList, focusIdx);

            getTabItemsParentItemView().setVisibility(View.GONE);
            getTabItemsSonItemView().setVisibility(View.VISIBLE);
            handleSeekbarShow(itemList.get(focusIdx));

        } else if (tabItemInfo.hasSubItems()) {
            // 保存当前选择项
//            BeautyPanelBeanHelper.putItemTypeWithIndex(tabItemInfo.itemType + tabItemInfo.parentId + BASE_SUB_TYPE, position);

            // 展开子项子页面
            findViewById(R.id.panel_menu_container).setVisibility(View.GONE);

            if (mSimpleSubMenuPanel == null) {
                mSimpleSubMenuPanel = createSubPanelView();
                ((ViewGroup)findViewById(R.id.panel_container)).addView(mSimpleSubMenuPanel);
            }
            mSimpleSubMenuPanel.setVisibility(View.VISIBLE);
            String name = ResoureUtils.getString(tabItemInfo.itemName);
            mSimpleSubMenuPanel.setSubPanelTitle(name);
            Adapter subPanelAdapter = mSimpleSubMenuPanel.getAdapter();
            int focusIdx = BeautyPanelBeanHelper.getIndexByItemType(tabItemInfo.itemType + tabItemInfo.itemId + BASE_SUB_TYPE, 0);
            mCurItemListAdapter = (BeautyItemAdapter)subPanelAdapter;
            mCurItemListAdapter.setData(mBeautyPanelController.getSubTabItemList(tabItemInfo), focusIdx);
        } else {
            if (mBeautyPanelController != null) {
                mBeautyPanelController.onHandleItemClick(tabItemInfo, position);
            }
            // 保存当前选择项
            BeautyPanelBeanHelper.putItemTypeWithIndex(tabItemInfo.itemType + tabItemInfo.parentId + BASE_SUB_TYPE, position);
        }
    }

    private void updateConfigAutoResource(TabInfo tabInfo) {
        if (mTabItemsParentView != null) {
            ImageView imgView = mTabItemsParentView.findViewById(R.id.item_image_normal);
            TextView title = mTabItemsParentView.findViewById(R.id.item_content);
            if (imgView != null && title != null) {
                int imgResId = R.mipmap.cfg_auto_beauty;
                int titleResId = R.string.config_auto_beauty;
                if (tabInfo.tabType == QueenCommonParams.BeautyType.BEAUTY) {
                    imgResId = R.mipmap.cfg_auto_beauty;
                    titleResId = R.string.config_auto_beauty;
                } else if (tabInfo.tabType == QueenCommonParams.BeautyType.FACE_MAKEUP) {
                    imgResId = R.mipmap.cfg_auto_makeup;
                    titleResId = R.string.config_auto_makeup;
                } else if (tabInfo.tabType == QueenCommonParams.BeautyType.FACE_SHAPE) {
                    imgResId = R.mipmap.cfg_auto_shap;
                    titleResId = R.string.config_auto_shape;
                }
                imgView.setImageResource(imgResId);
                title.setText(titleResId);
            }
        }
    }

    private View createTabItemsView4ConfigAuto() {
        mTabItemsParentView = findViewById(R.id.beauty_panel_parent_container);
        LinearLayout parentView = (LinearLayout)LayoutInflater.from(mContext).inflate(R.layout.beauty_menu_panel_layout_tab_item_auto_diy, mTabItemsParentView, true);
//        ImageView imgView1 = parentView.findViewById(R.id.item_image_normal);
//        imgView1.setImageResource(R.mipmap.cfg_auto_shap);
//        TextView title = parentView.findViewById(R.id.item_content);
//        title.setText(R.string.config_auto);

        parentView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                handleTabItemClicked(null, ITEM_4_CONFIG_AUTO);
            }
        });
        return parentView;
    }

    private View createTabItemsView4ConfigDiy() {
        mTabItemsSonView = findViewById(R.id.beauty_panel_son_container);
        LinearLayout sonView = (LinearLayout)LayoutInflater.from(mContext).inflate(R.layout.beauty_menu_panel_layout_tab_item_auto_diy, mTabItemsSonView, true);
        ImageView imgView1 = sonView.findViewById(R.id.item_image_normal);
        imgView1.setImageResource(R.mipmap.cfg_diy);
        TextView title = sonView.findViewById(R.id.item_content);
        title.setText(R.string.config_diy);
        title.setTextColor(AppUtil.getColor(R.color.colorBaseStyle));

        sonView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                handleTabItemClicked(null, ITEM_4_CONFIG_DIY);
            }
        });

        return sonView;
    }

    private BeautyScrollMenuSubPanel createSubPanelView() {
        BeautyScrollMenuSubPanel subPanel = new BeautyScrollMenuSubPanel(mContext);
        BeautyItemAdapter beautyItemAdapter = new BeautyItemAdapter(mContext, mBeautyInfo.tabColorNormal, mBeautyInfo.tabColorSelected);
        subPanel.setAdapter(beautyItemAdapter);

        beautyItemAdapter.setOnItemClickListener(mOnItemClickListener);

        subPanel.setOnSubPanelClickListener(new BeautyScrollMenuSubPanel.OnSubPanelClickListener() {
            @Override
            public void onBackClick() {
                mSimpleSubMenuPanel.setVisibility(View.GONE);
                findViewById(R.id.panel_menu_container).setVisibility(View.VISIBLE);
            }
        });

        return subPanel;
    }

    private BeautyItemAdapter.OnItemClickListener mOnItemClickListener = new BeautyItemAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(TabItemInfo itemInfo, int position) {
            // 具体item项点击
            handleTabItemClicked(itemInfo, position);

            if (itemInfo.hasSubItems()) { // 如果有子项，需要按照展开后的focus子项决定进度条是否展示
                int focusIdx = BeautyPanelBeanHelper.getIndexByItemType(itemInfo.itemType + itemInfo.itemId + BASE_SUB_TYPE, 0);
                List<TabItemInfo> itemList = mBeautyPanelController.getSubTabItemList(itemInfo);
                handleSeekbarShow(itemList.get(focusIdx));
            } else {
                handleSeekbarShow(itemInfo);
            }
        }
    };

    private static String BUILD_TYPE = "full";
    private void handleTips4Features(int itemType) {
        boolean valid = false;
        int[] usedType = new int[]{};

        if (BUILD_TYPE.equals("lite")) {
            usedType = new int[]{QueenCommonParams.BeautyType.BEAUTY, QueenCommonParams.BeautyType.LUT};
        } else if (BUILD_TYPE.equals("pro")) {
            usedType = new int[]{QueenCommonParams.BeautyType.BEAUTY, QueenCommonParams.BeautyType.LUT,
                    QueenCommonParams.BeautyType.FACE_SHAPE, QueenCommonParams.BeautyType.FACE_MAKEUP,
                    QueenCommonParams.BeautyType.STICKER };
        }

        for (int t:usedType) {
            if (itemType == t) { valid = true; break; }
        }
        if (!valid && usedType.length > 0) {
            Toast.makeText(mContext, R.string.tips_feature_disable_for_version, Toast.LENGTH_SHORT).show();
        }
    }
}
