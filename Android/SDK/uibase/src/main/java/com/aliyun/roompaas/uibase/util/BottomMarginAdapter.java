package com.aliyun.roompaas.uibase.util;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.aliyun.roompaas.base.log.Logger;
import com.aliyun.roompaas.base.util.CommonUtil;
import com.aliyun.roompaas.uibase.R;

import java.util.HashMap;

/**
 * Created by KyleCe on 2021/10/29
 */
public class BottomMarginAdapter {
    public static final String TAG = "BottomMarginAdapter";
    private static HashMap<Integer, Integer> marginMap;

    public static void storeOriginStatus(View v, boolean shown) {
        if (v != null && v.getTag(R.integer.viewTagForNavigationBarVisibility) == null) {
            Logger.i(TAG, "storeOriginStatus: " + shown);
            v.setTag(R.integer.viewTagForNavigationBarVisibility, shown);
            v.setTag(R.integer.viewTagForBottomAdjustViewMargin, parseBottomMargin(v));
        }
    }

    public static void adjust(final View view, boolean show) {
        final Context context;
        if (view == null || (context = view.getContext()) == null) {
            Logger.i(TAG, "adjust: end--invalid param: " + null);
            return;
        }
        int bottomMargin = parseBottomMargin(view);
        int hash = view.hashCode();
        HashMap<Integer, Integer> map = ofMarginMap();
        Object obj;
        Integer stored;
        int veryFirstBottomMargin = map.containsKey(hash) && (stored = map.get(hash)) != null ? stored : 0;
        if (veryFirstBottomMargin == 0) {
            veryFirstBottomMargin = (obj = view.getTag(R.integer.viewTagForBottomAdjustViewMargin)) instanceof Integer ? (int) obj : 0;
        }
        Object objShow;
        Boolean originShow = (objShow = view.getTag(R.integer.viewTagForNavigationBarVisibility)) instanceof Boolean ? (Boolean) objShow : null;

        boolean ok2UpFor1stAdjust = bottomMargin == veryFirstBottomMargin && show && (originShow != null && originShow);
        boolean ok2Up = bottomMargin <= veryFirstBottomMargin && show;
        boolean ok2Down = bottomMargin > veryFirstBottomMargin && !show;
        boolean ok2Refresh = ok2UpFor1stAdjust || ok2Up || ok2Down;
        Logger.i(TAG, hash + ":" + " veryFirstBottomMargin=" + veryFirstBottomMargin + ", bottomMargin=" + bottomMargin + ",originShow=" + originShow + ",newShow=" + show);
        Logger.i(TAG, hash + ":" + " 2up=" + ok2Up + ",2UpFor1stAdjust=" + ok2UpFor1stAdjust + ",2Down=" + ok2Down + ",2Refresh=" + ok2Refresh);
        if (ok2Refresh) {
            int navHeight = ExStatusBarUtils.getNavBarDesignedHeight(context);
            boolean goUp = ok2UpFor1stAdjust || ok2Up;
            final int alter = goUp ? navHeight : -navHeight;
            Runnable adjustAction = new Runnable() {
                @Override
                public void run() {
                    ViewUtil.changeBottomMargin(view, alter);
                    CommonUtil.showDebugToast(context, "调整alter：  " + alter);
                    view.requestLayout();
                }
            };
            int h;
            if ((h = view.getHeight()) != 0 && alter < 0) {
                int sh = AppUtil.getScreenRealHeight();
                int[] pos = new int[2];
                view.getLocationInWindow(pos);
                if (pos[1] + h + alter < sh) {
                    adjustAction.run();
                }
            } else {
                adjustAction.run();
            }
        }
    }

    private static HashMap<Integer, Integer> ofMarginMap() {
        if (marginMap == null) {
            marginMap = new HashMap<>();
        }
        return marginMap;
    }

    public static int parseBottomMargin(View view) {
        ViewGroup.LayoutParams lp;
        if (view != null && (lp = view.getLayoutParams()) instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;
            return mlp.bottomMargin;
        }
        return 0;
    }
}
