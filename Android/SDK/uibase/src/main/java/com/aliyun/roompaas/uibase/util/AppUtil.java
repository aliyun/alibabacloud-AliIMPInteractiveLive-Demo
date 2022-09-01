package com.aliyun.roompaas.uibase.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.res.ResourcesCompat;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.aliyun.roompaas.base.AppContext;
import com.aliyun.roompaas.base.util.Utils;

import java.io.Serializable;
import java.net.URL;

/**
 * @author puke
 * @version 2021/5/12
 */
public class AppUtil implements Serializable {
    public static final boolean SDK_AT_LEAST_P = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P;

    public static Context getAppContext() {
        return AppContext.getContext();
    }

    public static Resources getResources() {
        return getAppContext().getResources();
    }

    public static int getDimenPixel(@DimenRes int id) {
        return getDimensionPixelOffset(id);
    }

    public static int getDimensionPixelOffset(@DimenRes int id) {
        return getResources().getDimensionPixelOffset(id);
    }

    @ColorInt
    public static int getColor(@ColorRes int id) {
        return getResources().getColor(id);
    }

    public static String getString(@StringRes int id) {
        return id == -1 ? "" : getResources().getString(id);
    }

    @Nullable
    public static Drawable getDrawable(@DrawableRes int id) {
        return ResourcesCompat.getDrawable(getResources(), id, null);
    }

    public static int sp(float sp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, displayMetrics);
    }

    public static int dp(float dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics);
    }

    public static int dimensionPixelOffset(@DimenRes int id) {
        return getResources().getDimensionPixelOffset(id);
    }

    public static void jumpTo(Context context, Class<? extends Activity> activityType) {
        context.startActivity(new Intent(context, activityType));
    }

    public static int getScreenHeight() {
        return getResources().getDisplayMetrics().heightPixels;
    }

    public static int getScreenWidth() {
        return getResources().getDisplayMetrics().widthPixels;
    }

    public static int getScreenRealHeight() {
        WindowManager windowManager =
                (WindowManager) getAppContext().getSystemService(Context.WINDOW_SERVICE);
        final Display display = windowManager.getDefaultDisplay();
        Point outPoint = new Point();
        // include navigation bar
        display.getRealSize(outPoint);
        return outPoint.y;
    }

    public static int getScreenRealWidth() {
        WindowManager windowManager =
                (WindowManager) getAppContext().getSystemService(Context.WINDOW_SERVICE);
        final Display display = windowManager.getDefaultDisplay();
        Point outPoint = new Point();
        // include navigation bar
        display.getRealSize(outPoint);
        return outPoint.x;
    }

    public static int getDeviceHeight() {
        return Math.max(getScreenHeight(), getScreenWidth());
    }

    public static int getDeviceWidth() {
        return Math.min(getScreenHeight(), getScreenWidth());
    }

    /**
     * 三星note10+ 机器上判断不可靠
     * @return
     */
    public static int getConfigurationOrientation(){
        return getResources().getConfiguration().orientation;
    }

    public static boolean isOrientationLandscape() {
        return getConfigurationOrientation() == Configuration.ORIENTATION_LANDSCAPE;
    }

    public static boolean isOrientationPortrait() {
        return getConfigurationOrientation() == Configuration.ORIENTATION_PORTRAIT;
    }

    public static boolean isOrientationAnyLandscape(int ori){
        return ori == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                || ori == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                || ori == ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE
                || ori == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                ;
    }

    public static boolean isUrlValid(String url) {
        try {
            URL u = new URL(url);
            u.toURI();
            return true;
        } catch (Throwable ignore) {
            return false;
        }
    }

    public static void intoImmersive(Activity activity) {
        if (activity != null) {
            extendToFullscreen(activity.getWindow());
            extendToNotchIfPossible(activity.getWindow());
        }
    }

    public static void intoImmersive(Window window) {
        if (window != null) {
            extendToFullscreen(window);
            extendToNotchIfPossible(window);
        }
    }

    public static void extendToFullscreen(Window window) {
        if (window == null) {
            return;
        }
        try {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } catch (Throwable ignore) {
        }
    }

    public static void extendToNotchIfPossible(Window window) {
        if (SDK_AT_LEAST_P && window != null) {
            try {
                window.getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            } catch (Throwable ignore) {
            }
        }
    }

    public static boolean isActivityImmersive(@Nullable Activity activity) {
        Window window;
        if (activity == null || activity.isFinishing() || (window = activity.getWindow()) == null) {
            return false;
        }

        WindowManager.LayoutParams lp = window.getAttributes();
        boolean modeShortEdges = !SDK_AT_LEAST_P || lp.layoutInDisplayCutoutMode == WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        boolean fullscreen = (lp.flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) == WindowManager.LayoutParams.FLAG_FULLSCREEN;
        return modeShortEdges & fullscreen;
    }

    public static void adjustFullScreenOrNotForOrientationAuto(Window window) {
        adjustFullScreenOrNotForOrientation(window, getConfigurationOrientation());
    }

    public static void adjustFullScreenOrNotForOrientation(Window window, int orientation) {
        adjustFullScreenOrNotForOrientation(window, orientation == Configuration.ORIENTATION_LANDSCAPE
                , orientation == Configuration.ORIENTATION_PORTRAIT);
    }

    public static void adjustFullScreenOrNotForOrientation(Window window, boolean land, boolean port) {
        if (window == null) {
            return;
        }

        if (land) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else if (port) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }
    }

    public static void fitFullScreenForOrientationAutoWithSystemUi(Window window) {
        fitFullScreenForOrientationWithSystemUi(window, getConfigurationOrientation());
    }

    public static void fitFullScreenForOrientationWithSystemUi(Window window, int orientation) {
        fitFullScreenForOrientationWithSystemUi(window, orientation == Configuration.ORIENTATION_LANDSCAPE
                , orientation == Configuration.ORIENTATION_PORTRAIT);
    }

    public static void fitFullScreenForOrientationWithSystemUi(Window window, boolean land, boolean port) {
        if (window == null) {
            return;
        }

        if (land) {
            ExStatusBarUtils.intoFullScreenWithSystemUi(window);
        } else if (port) {
            ExStatusBarUtils.exitFullScreenWithSystemUi(window);
        }
    }

    public static void killProcess() {
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public static void exitApplication() {
        killProcess();
        System.exit(0);
    }

    public static void hideSystemUI(Activity activity) {
        if (Utils.isActivityInvalid(activity)) {
            return;
        }
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = activity.getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    public static boolean isNetworkInvalid(Context context) {
        return !isNetworkAvailable(context);
    }

    public static boolean isNetworkAvailable(Context context) {
        if (context == null) {
            return false;
        }
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isAvailable();
    }

    /**
     * 设置状态栏全透明
     *
     * @param activity 需要设置的activity
     */
    public static void setTransparent(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        }
        transparentStatusBar(activity);
        setRootView(activity);
    }

    /**
     * 使状态栏透明
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static void transparentStatusBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // 需要设置这个flag contentView才能延伸到状态栏
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            // 状态栏覆盖在contentView上面，设置透明使contentView的背景透出来
            activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
        } else {
            // 让contentView延伸到状态栏并且设置状态栏颜色透明
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    /**
     * 设置根布局参数
     */
    private static void setRootView(Activity activity) {
        ViewGroup parent = (ViewGroup) activity.findViewById(android.R.id.content);
        for (int i = 0, count = parent.getChildCount(); i < count; i++) {
            View childView = parent.getChildAt(i);
            if (childView instanceof ViewGroup) {
                childView.setFitsSystemWindows(true);
                ((ViewGroup) childView).setClipToPadding(true);
            }
        }
    }

    /**
     * 设置阻止截屏
     *
     * @param activity
     */
    public static void setFlagSecure(Activity activity) {
        if (activity != null) {
            Window window = activity.getWindow();
            if (window != null) {
                window.addFlags(WindowManager.LayoutParams.FLAG_SECURE);
            }
        }
    }
}
