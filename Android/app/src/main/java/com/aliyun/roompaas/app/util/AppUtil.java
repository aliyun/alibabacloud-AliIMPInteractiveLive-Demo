package com.aliyun.roompaas.app.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.aliyun.roompaas.app.App;

/**
 * @author puke
 * @version 2021/5/12
 */
public class AppUtil {

    public static int sp(float sp) {
        DisplayMetrics displayMetrics = App.getApplication().getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, displayMetrics);
    }

    public static int dp(float dp) {
        DisplayMetrics displayMetrics = App.getApplication().getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics);
    }

    public static void jumpTo(Context context, Class<? extends Activity> activityType) {
        context.startActivity(new Intent(context, activityType));
    }

    public static int getScreenHeight() {
        return App.getApplication().getResources().getDisplayMetrics().heightPixels;
    }

    public static int getScreenWidth() {
        return App.getApplication().getResources().getDisplayMetrics().widthPixels;
    }
}
