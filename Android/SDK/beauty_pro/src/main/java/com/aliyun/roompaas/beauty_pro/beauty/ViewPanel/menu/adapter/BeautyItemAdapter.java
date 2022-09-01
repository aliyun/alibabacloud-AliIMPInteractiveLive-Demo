package com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.aliyun.roompaas.beauty_pro.R;
import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.model.TabItemInfo;
import com.aliyun.roompaas.beauty_pro.utils.ResoureUtils;

import java.util.ArrayList;
import java.util.List;

public class BeautyItemAdapter extends BaseAdapter implements View.OnClickListener {

    private Context mContext;
    private List<TabItemInfo> mItemInfoList;
    private OnItemClickListener mItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(TabItemInfo itemInfo, int position);
    }

    // 当前选中
    private int mSelectPos;
    private String mTabColorSelected;
    private String mTabColorNormal;

    public BeautyItemAdapter(Context context, String normalColor, String selectedColor) {
        mContext = context;
        mTabColorNormal = normalColor;
        mTabColorSelected = selectedColor;
        mItemInfoList = new ArrayList<>();
    }

    public void updateFocusIndex(int focusIndex) {
        mSelectPos = focusIndex;
    }

    public void setData(List<TabItemInfo> tabItemInfoList, int defaultIndex) {
        mSelectPos = defaultIndex;
        mItemInfoList.clear();
        mItemInfoList.addAll(tabItemInfoList);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mItemInfoList.size();
    }

    @Override
    public TabItemInfo getItem(int position) {
        return mItemInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mItemInfoList.get(position).itemId;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.beauty_menu_panel_layout_tab_item, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
            convertView.setOnClickListener(this);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        convertView.setId(position);

        updateItemView(holder, position);

        return convertView;
    }

    private void updateItemView(ViewHolder holder, int position) {
        final TabItemInfo itemInfo = getItem(position);
        String title = ResoureUtils.getString(itemInfo.itemName);
        if (title != null && title.length() > 0) {
            holder.title.setVisibility(View.VISIBLE);
            holder.title.setText(title);
            String colorStr = mSelectPos == position ? mTabColorSelected : mTabColorNormal;
            holder.title.setTextColor(Color.parseColor(colorStr));
        } else {
            holder.title.setVisibility(View.GONE);
        }

        holder.icon.setVisibility(View.GONE);
        holder.iconFocus.setVisibility(View.GONE);
        if (itemInfo.itemIconNormal != null && itemInfo.itemIconNormal.length() > 0) {
            ResoureUtils.updateViewImage(holder.icon, itemInfo.itemIconNormal);
            if (mSelectPos == position) {
                ResoureUtils.updateViewImage(holder.iconFocus, itemInfo.itemIconSelected);
            }
        }

        if (itemInfo.progressCur != 0) {
            holder.valueValidMask.setVisibility(View.VISIBLE);
        } else {
            holder.valueValidMask.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        int position = v.getId();
        final TabItemInfo itemInfo = getItem(position);
        ViewHolder holder = (ViewHolder) v.getTag();
        if (mSelectPos != position || itemInfo.hasSubItems()) {
            mSelectPos = position;
            updateItemView(holder, position);
            notifyDataSetChanged();

            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(itemInfo, position);
            }
        }
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        mItemClickListener = itemClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView icon;
        private ImageView iconFocus;
        private TextView title;
        private TextView valueValidMask;

        public ViewHolder(View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.item_image_normal);
            iconFocus = (ImageView) itemView.findViewById(R.id.item_image_focus);
            title = (TextView) itemView.findViewById(R.id.item_content);
            valueValidMask = (TextView) itemView.findViewById(R.id.item_value_valid_mark);
        }
    }
}
