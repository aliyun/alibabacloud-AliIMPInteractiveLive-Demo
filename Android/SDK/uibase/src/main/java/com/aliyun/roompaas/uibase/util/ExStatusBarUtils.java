package com.aliyun.roompaas.uibase.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.aliyun.roompaas.base.log.Logger;
import com.aliyun.roompaas.base.util.Utils;
import com.aliyun.roompaas.uibase.R;
import com.aliyun.roompaas.uibase.util.immersionbar.NavigationBarUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ExStatusBarUtils {
    public static final String TAG = "ExStatusBarUtils";
    public static int DEFAULT_COLOR = 0;
    public static float DEFAULT_ALPHA = 0;//Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? 0.2f : 0.3f;
    public static final int MIN_API = 19;

    public static int s_SystemUiVisibilityOrigin;

    //<editor-fold desc="沉侵">
    public static void immersive(Activity activity) {
        immersive(activity, DEFAULT_COLOR, DEFAULT_ALPHA);
    }

    public static void immersive(Activity activity, int color, @FloatRange(from = 0.0, to = 1.0) float alpha) {
        immersive(activity.getWindow(), color, alpha);
    }

    public static void immersive(Activity activity, int color) {
        immersive(activity.getWindow(), color, 1f);
    }

    public static void immersive(Window window) {
        immersive(window, DEFAULT_COLOR, DEFAULT_ALPHA);
    }

    public static void immersive(Window window, int color) {
        immersive(window, color, 1f);
    }

    public static void makeWindowFullIncludeNavigationBar(Window window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View view = window.getDecorView();
            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            window.getDecorView().setSystemUiVisibility(flags);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }
    }

    private static final int sViewTagForSystemUiVisibility = 2022061000;
    private static final int sViewTagForStatusBarColor = 2022061001;
    private static final int sViewTagForNavigationBarColor = 2022061002;

    public static void intoFullScreenWithSystemUi(Window window) {
        View view = window.getDecorView();
        int flags = view.getSystemUiVisibility();
        storeIfNotYet(view, sViewTagForSystemUiVisibility, flags);
        flags |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                // Hide the nav bar and status bar
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        storeIfNotYet(view, sViewTagForStatusBarColor, window.getStatusBarColor());
        storeIfNotYet(view, sViewTagForNavigationBarColor, window.getNavigationBarColor());

        view.setSystemUiVisibility(flags);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
    }

    public static void exitFullScreenWithSystemUi(Window window) {
        View view = window.getDecorView();

        Object systemUiTag = view.getTag(sViewTagForSystemUiVisibility);
        if (systemUiTag instanceof Integer ) {
            view.setSystemUiVisibility( (Integer) systemUiTag);
            window.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        Object statusColorTag = view.getTag(sViewTagForStatusBarColor);
        if (statusColorTag instanceof Integer) {
            setStatusBarColor(window, (Integer) statusColorTag);
        }
        Object navColorTag = view.getTag(sViewTagForNavigationBarColor);
        if (navColorTag instanceof Integer) {
            window.setNavigationBarColor((Integer) navColorTag);
        }
    }

    public static void storeIfNotYet(View view, int key, Object tag) {
        if (view != null && tag != null) {
            Object val = view.getTag(key);
            if (val == null) {
                view.setTag(key, tag);
            }
        }
    }

    public static void immersive(Window window, int color, @FloatRange(from = 0.0, to = 1.0) float alpha) {
        if (Build.VERSION.SDK_INT >= 21) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(mixtureColor(color, alpha));

            int systemUiVisibility = window.getDecorView().getSystemUiVisibility();
            systemUiVisibility |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            systemUiVisibility |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            window.getDecorView().setSystemUiVisibility(systemUiVisibility);
        } else if (Build.VERSION.SDK_INT >= 19) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            setTranslucentView((ViewGroup) window.getDecorView(), color, alpha);
        } else if (Build.VERSION.SDK_INT >= MIN_API && Build.VERSION.SDK_INT > 16) {
            int systemUiVisibility = window.getDecorView().getSystemUiVisibility();
            systemUiVisibility |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            systemUiVisibility |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            window.getDecorView().setSystemUiVisibility(systemUiVisibility);
        }
    }

    /**
     * 创建假的透明栏
     */
    public static void setTranslucentView(ViewGroup container, int color, @FloatRange(from = 0.0, to = 1.0) float alpha) {
        if (Build.VERSION.SDK_INT >= 19) {
            int mixtureColor = mixtureColor(color, alpha);
            View translucentView = container.findViewById(android.R.id.custom);
            if (translucentView == null && mixtureColor != 0) {
                translucentView = new View(container.getContext());
                translucentView.setId(android.R.id.custom);
                ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, getStatusBarHeight(container.getContext()));
                container.addView(translucentView, lp);
            }
            if (translucentView != null) {
                translucentView.setBackgroundColor(mixtureColor);
            }
        }
    }

    public static int mixtureColor(int color, @FloatRange(from = 0.0, to = 1.0) float alpha) {
        int a = (color & 0xff000000) == 0 ? 0xff : color >>> 24;
        return (color & 0x00ffffff) | (((int) (a * alpha)) << 24);
    }

    public static void setStatusBarTransparentIfPossible(Activity activity) {
        if (Build.VERSION.SDK_INT >= 23) {
            s_SystemUiVisibilityOrigin = activity.getWindow().getDecorView().getSystemUiVisibility();
            setStatusBarColor(activity, Color.TRANSPARENT, false);
        }
    }

    public static void setStatusBarColor(Activity activity, int color) {
        setStatusBarColor(activity.getWindow(), color);
    }
    public static void setStatusBarColor(Window window, int color) {
        setStatusBarColor(window, color, false);
    }

    public static void setStatusBarColor(Activity activity, int color, boolean darkMode) {
        setStatusBarColor(activity.getWindow(), color, darkMode);
    }

    public static void setStatusBarColor(Window window, int color, boolean darkMode) {
        if (Build.VERSION.SDK_INT >= 23) {
            window.addFlags(-2147483648);
            window.clearFlags(67108864);
            if (!darkMode) {
                window.getDecorView().setSystemUiVisibility(8192);
            } else {
                window.getDecorView().setSystemUiVisibility(s_SystemUiVisibilityOrigin);
            }

            window.setStatusBarColor(color);
            if ("Xiaomi".equals(Build.MANUFACTURER)) {
                setXiaomiStatusBarDarkMode(!darkMode, window);
            }
        }
    }

    private static void setXiaomiStatusBarDarkMode(boolean darkmode, Activity activity) {
        setXiaomiStatusBarDarkMode(darkmode, activity.getWindow());
    }

    private static void setXiaomiStatusBarDarkMode(boolean darkmode, Window window) {
        if (Build.VERSION.SDK_INT >= 23) {
            Class clazz = window.getClass();

            try {
                int darkModeFlag = 0;
                Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
                Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
                darkModeFlag = field.getInt(layoutParams);
                Method extraFlagField = clazz.getMethod("setExtraFlags", Integer.TYPE, Integer.TYPE);
                extraFlagField.invoke(window, darkmode ? darkModeFlag : 0, darkModeFlag);
            } catch (Exception var7) {
                var7.printStackTrace();
            }
        }
    }

    public static boolean hasNavBar(Context context) {
        boolean result = false;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            result = rs.getBoolean(id);
        }
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                result = false;
            } else if ("0".equals(navBarOverride)) {
                result = true;
            }
        } catch (Exception e) {

        }
        return result;
    }

    public static void setStatusBarTextColorBlack(Activity activity, boolean blackText) {
        if (isTransparentStatusBar()) {
            if (s_SystemUiVisibilityOrigin == -1) {
                s_SystemUiVisibilityOrigin = activity.getWindow().getDecorView().getSystemUiVisibility();
            }

            Window window = activity.getWindow();
            if (window != null) {
                window.addFlags(-2147483648);
                window.clearFlags(67108864);
                if (blackText) {
                    window.getDecorView().setSystemUiVisibility(9216);
                } else if (s_SystemUiVisibilityOrigin != -1) {
                    window.getDecorView().setSystemUiVisibility(s_SystemUiVisibilityOrigin);
                }
            }
        }

    }

    public static boolean isTransparentStatusBar() {
        if (Build.VERSION.SDK_INT >= 21) {
            if (!isXiaomiDevice()) {
                return true;
            }

            String version = getXiaomiVersion();
            if (TextUtils.isEmpty(version)) {
                return false;
            }

            try {
                int ver = Integer.valueOf(version.replace("V", ""));
                if (isXiaomiDevice() && ver >= 9) {
                    return true;
                }
            } catch (Exception var2) {
                var2.printStackTrace();
            }
        }

        return false;
    }

    private static boolean isXiaomiDevice() {
        return "Xiaomi".equals(Build.MANUFACTURER);
    }

    private static String getXiaomiVersion() {
        String key = "ro.miui.ui.version.name";

        try {
            Class osSystem = Class.forName("android.os.SystemProperties");
            Method getInvoke = osSystem.getMethod("get", String.class);
            return (String) getInvoke.invoke(osSystem, key);
        } catch (Exception var3) {
            var3.printStackTrace();
            return "";
        }
    }

    public static final int IS_SET_PADDING_KEY = 54648632;
    private static final int STATUS_VIEW_ID = R.id.status_view;
    private static final int TRANSLUCENT_VIEW_ID = R.id.translucent_view;

    //------------单色明暗度状态栏------------

    /**
     * 设置普通toolbar中状态栏颜色以及明暗度
     *
     * @param activity
     * @param color
     * @param statusBarAlpha
     */
    public static void setStatusColor(Activity activity, @ColorInt int color, int statusBarAlpha) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            activity.getWindow().setStatusBarColor(statusColorIntensity(color, statusBarAlpha));
        } else {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            setStatusViewToAct(activity, color, statusBarAlpha);
            setRootView(activity);
        }
    }

    /**
     * 设置普通toolbar中状态栏颜色
     *
     * @param activity
     * @param color
     */
    public static void setStatusColor(Activity activity, @ColorInt int color) {
        setStatusColor(activity, color, 0);
    }

    /**
     * 设置toolbar带drawerLayout状态栏透明度,5.0以上使用默认系统的第二颜色colorPrimaryDark
     * 但是drawerLayout打开的时候会有一条statusbar高度的半透明条
     * 如果想修改，请到style中设置<item name="colorPrimaryDark">@color/colorPrimary</item>
     * 注：必须将drawerLayout设置android:fitsSystemWindows="true"
     * 还有下方的toolbar，
     * 一般不要设置滑动toolbar,在4.4系统会有问题，下部如果有tablayout会遮挡
     *
     * @param activity
     * @param statusBarAlpha
     */
    public static void setDyeDrawerStatusAlpha(Activity activity, int statusBarAlpha) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            activity.getWindow().setStatusBarColor(Color.argb(statusBarAlpha, 0, 0, 0));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            ViewGroup contentLayout = (ViewGroup) activity.findViewById(android.R.id.content);
            contentLayout.getChildAt(0).setFitsSystemWindows(false);
            setTranslucentStatusViewToAct(activity, statusBarAlpha);
        }
    }

    /**
     * toolbar可伸缩版本
     * 设置toolbar带drawerLayout状态栏透明,5.0以上使用默认系统的第二颜色colorPrimaryDark
     * 如果想修改，请到style中设置<item name="colorPrimaryDark">@color/colorPrimary</item>
     * 4.4版本跟随toolbar颜色
     * 但是drawerLayout打开的时候会有一条statusbar高度的半透明条
     * 注：必须将drawerLayout设置android:fitsSystemWindows="true"
     * CoordinatorLayout设置背景颜色，因为4.4状态栏的颜色会跟着它走
     * 下边内容布局设置背景颜色
     *
     * @param activity
     */
    public static void setDyeDrawerStatusTransparent(Activity activity, CoordinatorLayout coordinatorLayout) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            ViewGroup contentLayout = (ViewGroup) activity.findViewById(android.R.id.content);
            contentLayout.getChildAt(0).setFitsSystemWindows(false);
            coordinatorLayout.setFitsSystemWindows(true);
            View mStatusBarView = contentLayout.getChildAt(0);
            //改变颜色时避免重复添加statusBarView
            if (mStatusBarView != null && mStatusBarView.getMeasuredHeight() == getStatusBarHeight(activity)) {
                return;
            }
            mStatusBarView = new View(activity);
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    getStatusBarHeight(activity));
            contentLayout.addView(mStatusBarView, lp);

        }
    }

    /**
     * 颜色不要用0x000000模式的，要用Color.rgb/argb(),或者activity.getResource().getColor等等
     * 设置普通toolbar带drawerLayout状态栏
     * 1.设置toolbar的颜色和颜色光暗度
     * 2.drawerLayout的顶部透明度
     * ,不要把toolbar设置为可滑动
     * 注：必须将drawerLayout设置android:fitsSystemWindows="true"
     *
     * @param activity
     * @param color
     * @param statusBarAlpha
     */
    public static void setDyeDrawerStatusColor(Activity activity, DrawerLayout drawerLayout, @ColorInt int color, int statusBarAlpha) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
        } else {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        // 生成一个状态栏大小的矩形
        // 添加 statusBarView 到布局中
        ViewGroup contentLayout = (ViewGroup) drawerLayout.getChildAt(0);
        View statusBarView = contentLayout.findViewById(STATUS_VIEW_ID);
        if (statusBarView != null) {
            if (statusBarView.getVisibility() == View.GONE) {
                statusBarView.setVisibility(View.VISIBLE);
            }
            statusBarView.setBackgroundColor(color);
        } else {
            contentLayout.addView(createStatusBarView(activity, color, 0), 0);
        }
        // 内容布局不是 LinearLayout 时,设置padding top
        if (!(contentLayout instanceof LinearLayout) && contentLayout.getChildAt(1) != null) {
            contentLayout.getChildAt(1)
                    .setPadding(contentLayout.getPaddingLeft(), getStatusBarHeight(activity) + contentLayout.getPaddingTop(),
                            contentLayout.getPaddingRight(), contentLayout.getPaddingBottom());
        }
        // 设置属性
        setDrawerLayoutProperty(drawerLayout, contentLayout);
        setTranslucentStatusViewToAct(activity, statusBarAlpha);
    }

    /**
     * 隐藏statusView
     *
     * @param activity
     */
    public static void hideStatusView(Activity activity) {
        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        View fakeStatusBarView = decorView.findViewById(STATUS_VIEW_ID);
        if (fakeStatusBarView != null) {
            fakeStatusBarView.setVisibility(View.GONE);
        }
        View fakeTranslucentView = decorView.findViewById(TRANSLUCENT_VIEW_ID);
        if (fakeTranslucentView != null) {
            fakeTranslucentView.setVisibility(View.GONE);
        }
    }

    //----------透明状态栏，可调整透明度-------------

    /**
     * 设置真正的状态栏透明度
     *
     * @param activity
     * @param statusBarAlpha
     */
    public static void setStatusAlpha(Activity activity, int statusBarAlpha) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            activity.getWindow().setStatusBarColor(Color.argb(statusBarAlpha, 0, 0, 0));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            setTranslucentStatusViewToAct(activity, statusBarAlpha);
            setRootView(activity);
        }
    }

    /**
     * 设置ImageView为第一控件的全透明状态栏
     *
     * @param activity
     */
    public static void setTransparentStatusBar(Activity activity, View topView) {
        setTranslucentStatusBar(activity, topView, 0);
    }

    /**
     * 设置ImageView为第一控件的可以调整透明度的状态栏
     *
     * @param activity
     */
    public static void setTranslucentStatusBar(Activity activity, View topView, int alpha) {
        setARGBStatusBar(activity, topView, 0, 0, 0, alpha);
    }

    /**
     * 设置透明状态栏版本的状态栏的ARGB
     *
     * @param activity
     * @param topView
     * @param r
     * @param g
     * @param b
     * @param alpha
     */
    public static void setARGBStatusBar(Activity activity, View topView, int r, int g, int b, int alpha) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            activity.getWindow().setStatusBarColor(Color.argb(alpha, r, g, b));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            setARGBStatusViewToAct(activity, r, g, b, alpha);
        }
        if (topView != null) {
            boolean isSetPadding = topView.getTag(IS_SET_PADDING_KEY) != null;
            if (!isSetPadding) {
                topView.setPadding(topView.getPaddingLeft(), topView.getPaddingTop() + getStatusBarHeight(activity), topView.getPaddingRight(), topView.getPaddingBottom());
                topView.setTag(IS_SET_PADDING_KEY, true);
            }
        }
    }

    /**
     * drawerlayout中设置全透明状态栏
     *
     * @param activity
     * @param drawerLayout
     * @param topView
     */
    public static void setTransparentStatusForDrawer(Activity activity, DrawerLayout drawerLayout, View topView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            drawerLayout.setFitsSystemWindows(true);
            drawerLayout.setClipToPadding(false);
        }
        setTransparentStatusBar(activity, topView);
    }

    /**
     * drawerlayout中设置透明状态栏的透明度
     * drawer布局和主布局都会看到
     *
     * @param activity
     * @param drawerLayout
     * @param topView
     * @param statusBarAlpha
     */
    public static void setStatusAlphaForDrawer(Activity activity, DrawerLayout drawerLayout, View topView, int statusBarAlpha) {
        setStatusColorAndCAlphaForDrawer(activity, drawerLayout, topView, 0x000000, statusBarAlpha);
    }

    /**
     * drawerlayout中设置透明状态栏的颜色和透明度
     * drawer布局和主布局都会看到
     *
     * @param activity
     * @param drawerLayout
     * @param topView
     * @param color
     * @param statusBarAlpha
     */
    public static void setStatusColorAndCAlphaForDrawer(Activity activity, DrawerLayout drawerLayout, View topView, @ColorInt int color, int statusBarAlpha) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        setStatusARGBForDrawer(activity, drawerLayout, topView, r, g, b, statusBarAlpha);
    }

    public static void setStatusARGBForDrawer(Activity activity, DrawerLayout drawerLayout, View topView, int r, int g, int b, int statusBarAlpha) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            drawerLayout.setFitsSystemWindows(true);
            drawerLayout.setClipToPadding(false);
        }
        setARGBStatusBar(activity, topView, r, g, b, statusBarAlpha);
    }

    /**
     * 在有fragment的activity中使用
     * 注：需要在有状态栏的fragment的最顶端加一个状态栏大小的view
     *
     * @param activity
     * @param alpha
     */
    public static void setTranslucentForImageViewInFragment(Activity activity, int alpha) {
        setTranslucentStatusBar(activity, null, alpha);
    }

    //----------------私有方法----------------------

    /**
     * 设置 DrawerLayout 属性
     *
     * @param drawerLayout              DrawerLayout
     * @param drawerLayoutContentLayout DrawerLayout 的内容布局
     */
    private static void setDrawerLayoutProperty(DrawerLayout drawerLayout, ViewGroup drawerLayoutContentLayout) {
        ViewGroup drawer = (ViewGroup) drawerLayout.getChildAt(1);
        drawerLayout.setFitsSystemWindows(false);
        drawerLayoutContentLayout.setFitsSystemWindows(false);
        drawerLayoutContentLayout.setClipToPadding(true);
        drawer.setFitsSystemWindows(false);
    }

    /**
     * 设置状态栏view的颜色并添加到界面中，如果找到状态栏view则直接设置，否则创建一个再设置
     *
     * @param activity
     * @param color
     * @param statusBarAlpha
     */
    private static void setStatusViewToAct(Activity activity, @ColorInt int color, int statusBarAlpha) {
        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        View fakeStatusBarView = decorView.findViewById(STATUS_VIEW_ID);
        if (fakeStatusBarView != null) {
            if (fakeStatusBarView.getVisibility() == View.GONE) {
                fakeStatusBarView.setVisibility(View.VISIBLE);
            }
            fakeStatusBarView.setBackgroundColor(statusColorIntensity(color, statusBarAlpha));
        } else {
            decorView.addView(createStatusBarView(activity, color, statusBarAlpha));
        }
    }

    /**
     * 设置状态栏view的透明度，如果找到状态栏view则直接设置，否则创建一个再设置
     *
     * @param activity
     * @param statusBarAlpha
     */
    private static void setTranslucentStatusViewToAct(Activity activity, int statusBarAlpha) {
        setARGBStatusViewToAct(activity, 0, 0, 0, statusBarAlpha);
    }

    /**
     * 设置状态栏view的ARGB，如果找到状态栏view则直接设置，否则创建一个再设置
     *
     * @param activity
     * @param statusBarAlpha
     */
    private static void setARGBStatusViewToAct(Activity activity, int r, int g, int b, int statusBarAlpha) {

        ViewGroup contentView = (ViewGroup) activity.findViewById(android.R.id.content);
        View fakeStatusBarView = contentView.findViewById(TRANSLUCENT_VIEW_ID);
        if (fakeStatusBarView != null) {
            if (fakeStatusBarView.getVisibility() == View.GONE) {
                fakeStatusBarView.setVisibility(View.VISIBLE);
            }
            fakeStatusBarView.setBackgroundColor(Color.argb(statusBarAlpha, r, g, b));
        } else {
            contentView.addView(createARGBStatusBarView(activity, r, g, b, statusBarAlpha));
        }
    }

    /**
     * 创建和状态栏一样高的矩形，用于改变状态栏颜色和明暗度
     *
     * @param activity
     * @param color
     * @param alpha
     * @return
     */
    private static View createStatusBarView(Activity activity, @ColorInt int color, int alpha) {
        // 绘制一个和状态栏一样高的矩形
        View statusBarView = new View(activity);
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getStatusBarHeight(activity));
        statusBarView.setLayoutParams(params);
        statusBarView.setBackgroundColor(statusColorIntensity(color, alpha));
        statusBarView.setId(STATUS_VIEW_ID);
        return statusBarView;
    }

    /**
     * 创建和状态栏一样高的矩形，用于改变状态栏透明度
     *
     * @param activity
     * @param alpha
     * @return
     */
    private static View createTranslucentStatusBarView(Activity activity, int alpha) {
        return createARGBStatusBarView(activity, 0, 0, 0, alpha);
    }

    /**
     * 创建和状态栏一样高的矩形，用于改变状态栏ARGB
     *
     * @param activity
     * @param r
     * @param g
     * @param b
     * @param alpha
     * @return
     */
    private static View createARGBStatusBarView(Activity activity, int r, int g, int b, int alpha) {
        // 绘制一个和状态栏一样高的矩形
        View statusBarView = new View(activity);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getStatusBarHeight(activity));
        statusBarView.setLayoutParams(params);
        statusBarView.setBackgroundColor(Color.argb(alpha, r, g, b));
        statusBarView.setId(TRANSLUCENT_VIEW_ID);
        return statusBarView;
    }

    /**
     * 得到statusbar高度
     *
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        int result = 24;
        int resId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) {
            result = context.getResources().getDimensionPixelSize(resId);
        } else {
            result = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    result, Resources.getSystem().getDisplayMetrics());
        }
        return result;
    }


    /**
     * 计算状态栏颜色明暗度
     *
     * @param color color值
     * @param alpha alpha值
     * @return 最终的状态栏颜色
     */
    private static int statusColorIntensity(@ColorInt int color, int alpha) {
        if (alpha == 0) {
            return color;
        }
        float a = 1 - alpha / 255f;
        int red = color >> 16 & 0xff;
        int green = color >> 8 & 0xff;
        int blue = color & 0xff;
        red = (int) (red * a + 0.5);
        green = (int) (green * a + 0.5);
        blue = (int) (blue * a + 0.5);
        return 0xff << 24 | red << 16 | green << 8 | blue;
    }

    /**
     * 配置状态栏之下的View
     *
     * @param activity
     */
    public static void setRootView(Activity activity) {
        ViewGroup parent = (ViewGroup) activity.findViewById(android.R.id.content);
        for (int i = 0, count = parent.getChildCount(); i < count; i++) {
            View childView = parent.getChildAt(0);
            if (childView instanceof ViewGroup) {
                childView.setFitsSystemWindows(true);
                ((ViewGroup) childView).setClipToPadding(true);
            }
        }
    }

    public static void adjustTopMarginForImmersive(final View v) {
        adjustTopForImmersiveIfNecessary(v, true);
    }

    public static void adjustTopPaddingForImmersive(final View v) {
        adjustTopForImmersiveIfNecessary(v, false);
    }

    private static void adjustTopForImmersiveIfNecessary(final View v, final boolean trueForMarginFalsePadding) {
        final Context context;
        if (v == null || (context = v.getContext()) == null) {
            return;
        }

        ViewUtil.addOnGlobalLayoutListener(v, new Runnable() {
            @Override
            public void run() {
                int[] location = new int[2];
                v.getLocationInWindow(location);
                int statusBarHeight = getStatusBarHeight(context);
                boolean needToAdjustTopMargin = location[1] < statusBarHeight;
                if (needToAdjustTopMargin) {
                    if (trueForMarginFalsePadding) {
                        ViewUtil.changeTopMargin(v, statusBarHeight);
                    } else {
                        ViewUtil.changeTopPadding(v, statusBarHeight);
                    }
                }
            }
        });
    }

    public static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static void adjustBottomForNavigationIfNecessary(final Activity activity, final View v) {
        if (v == null || Utils.isActivityInvalid(activity)) {
            return;
        }
        ViewUtil.addOnGlobalLayoutListener(v, new Runnable() {
            @Override
            public void run() {
                Logger.i(TAG, "view layout triggered");
                final NavigationBarUtils.OnNavigationStateListener listener = new NavigationBarUtils.OnNavigationStateListener() {
                    @Override
                    public void onNavigationState(boolean shown, int height) {
                        BottomMarginAdapter.storeOriginStatus(v, shown);
                        BottomMarginAdapter.adjust(v, shown);
                    }
                };

                boolean isNavShown = ExStatusBarUtils.isNavShownViaDisplayedHeight(activity);
                listener.onNavigationState(isNavShown, -1);

                NavigationBarUtils.addListenerForNavSateChangeOnlyOncePerView(activity, listener);
            }
        }, false);
    }

    public static int getNavBarDesignedHeight(Context context) {
        int result = 0;
        if (context instanceof Activity) {
            result = getNavigationBarShownHeight((Activity) context);
        }

        if (result == 0) {
            result = getNavBarHeightViaResourceId(context);
        }
        return result;
    }

    public static int getNavBarHeightViaResourceId(Context context) {
        if (!hasNavBar(context)) {
            return 0;
        }
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        return resources.getDimensionPixelSize(resourceId);
    }

    public static boolean isNavShownViaDisplayedHeight(Activity activity) {
        return getNavigationBarShownHeight(activity) > 0;
    }

    public static int getNavigationBarShownHeight(Activity activity) {
        Window window = activity.getWindow();
        View decorView = window.getDecorView();
        Rect rect = new Rect();
        decorView.getWindowVisibleDisplayFrame(rect);
        DisplayMetrics outMetrics = new DisplayMetrics();
        WindowManager windowManager = window.getWindowManager();
        windowManager.getDefaultDisplay().getRealMetrics(outMetrics);
        return outMetrics.heightPixels - rect.bottom;
    }
}