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
import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.model.BeautyInfo;
import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.model.TabInfo;
import com.aliyun.roompaas.beauty_pro.utils.ResoureUtils;

public class BeautyTabAdapter extends BaseAdapter implements View.OnClickListener {

    private Context mContext;
    private BeautyInfo mBeautyInfo;
    private OnTabChangeListener mTabClickListener;
    private View mCurTabSelected;

    public BeautyTabAdapter(Context context, BeautyInfo beautyInfo) {
        mContext = context;
        mBeautyInfo = beautyInfo;
    }

    @Override
    public int getCount() {
        return mBeautyInfo.tabInfoList.size();
    }

    @Override
    public TabInfo getItem(int i) {
        return mBeautyInfo.tabInfoList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return mBeautyInfo.tabInfoList.get(i).tabId;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.beauty_menu_panel_layout_tab, viewGroup, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
            convertView.setOnClickListener(this);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.updateView(i);

        if (i == 0) {
            setSelectePosition(convertView);
        }
        return convertView;
    }

    @Override
    public void onClick(View view) {
        setSelectePosition(view);

        if (mTabClickListener != null) {
            ViewHolder viewHolder = (ViewHolder) view.getTag();
            int position = (int)viewHolder.tabTitle.getTag();
            mTabClickListener.onTabChange(getItem(position), position);
        }
    }

    private void setSelectePosition(View view) {
        if (mCurTabSelected != null) {
            ViewHolder preViewHolder = (ViewHolder)mCurTabSelected.getTag();
            preViewHolder.setLoseFocus();
        }
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.setFocus();

        mCurTabSelected = view;
    }

    public void setOnTabClickListener(OnTabChangeListener tabClickListener) {
        mTabClickListener = tabClickListener;
    }

    public interface OnTabChangeListener {
        void onTabChange(TabInfo tabInfo, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tabTitle;
        private ImageView tabLine;

        private int mNormalTextColor = 0;
        private int mFocusTextColor = 0;

        public ViewHolder(View itemView) {
            super(itemView);
            tabTitle = (TextView) itemView.findViewById(R.id.tab_item_title);
            tabLine = (ImageView) itemView.findViewById(R.id.tab_item_line);

            mNormalTextColor = Color.parseColor(mBeautyInfo.tabColorNormal);
            mFocusTextColor = Color.parseColor(mBeautyInfo.tabColorSelected);
        }

        public void updateView(int position) {
            tabTitle.setTextColor(mNormalTextColor);
            tabLine.setVisibility(View.GONE);

            String title = ResoureUtils.getString(getItem(position).tabName);
            tabTitle.setText(title);
            tabTitle.setTag(position);
        }

        public void setFocus() {
            tabTitle.setTextColor(mFocusTextColor);
            tabLine.setVisibility(View.VISIBLE);
            tabLine.setBackgroundColor(mFocusTextColor);
        }

        public void setLoseFocus() {
            tabTitle.setTextColor(mNormalTextColor);
            tabLine.setVisibility(View.GONE);
        }
    }

}
