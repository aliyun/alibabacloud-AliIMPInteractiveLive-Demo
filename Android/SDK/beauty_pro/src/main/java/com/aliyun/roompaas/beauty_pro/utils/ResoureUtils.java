package com.aliyun.roompaas.beauty_pro.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

public class ResoureUtils {

    public static final String PREFIX_QUOTE   = "@";
    private static final String TYPE_STRING         = "string";

    public static Resources getResources() {
        return BeautyUtils.getAppContext().getResources();
    }

    private static int getStringId(String resName) {
        return getResources().getIdentifier(resName, TYPE_STRING, BeautyUtils.getPkgName());
    }

    public static String getString(String resName) {
        String result = resName;
        if (resName.startsWith(PREFIX_QUOTE)) {
            int resId = getStringId(resName.substring(1));
            result = resId > 0 ? getResources().getString(resId) : resName.substring(1);
        }
        return result;
    }

    public static int getMipmapDrawableId(String resName) {
        return getResources().getIdentifier(resName, "mipmap", BeautyUtils.getPkgName());
    }

    public static Drawable getDrawable(String resName) {
        Drawable result = null;
        if (resName.startsWith(PREFIX_QUOTE)) {
            int resId = getMipmapDrawableId(resName.substring(1));
            result = getResources().getDrawable(resId);
        }
        return result;
    }

    /**
     * px 转 dp
     * 48px - 16dp
     * 50px - 17dp*/
    public static int px2dip(Context context, float pxValue) {
        float scale = getScreenDendity(context);
        return (int)((pxValue / scale) + 0.5f);
    }

    /**屏幕密度比例*/
    public static float getScreenDendity(Context context){
        return context.getResources().getDisplayMetrics().density;
    }

    public static void updateViewImage(ImageView view, String resName) {
        if (resName == null || resName.length() == 0)
            return;

        if (resName.startsWith(PREFIX_QUOTE)) {
            view.setVisibility(View.VISIBLE);
            view.setImageDrawable(getDrawable(resName));
        } else {
            Bitmap iconBmp = BeautyUtils.decodeBitmapFromAssets(resName);
            if (iconBmp != null) {
                view.setVisibility(View.VISIBLE);
                view.setImageBitmap(iconBmp);
            }
        }
    }

}
