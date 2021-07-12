package com.aliyun.roompaas.app.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.FloatRange;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class StatusBarUtil {

    public static int DEFAULT_COLOR = 0;
    public static float DEFAULT_ALPHA = 0;//Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? 0.2f : 0.3f;
    public static final int MIN_API = 19;

    public static int s_SystemUiVisibilityOrigin;

    public StatusBarUtil() {
    }

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

    /**
     * 获取状态栏高度
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

    public static void setStatusBarTransparentIfPossible(Activity activity) {
        if (Build.VERSION.SDK_INT >= 23) {
            s_SystemUiVisibilityOrigin = activity.getWindow().getDecorView().getSystemUiVisibility();
            setStatusBarColor(activity, Color.TRANSPARENT, false);
        }
    }

    public static void setStatusBarColor(Activity activity, int color) {
        setStatusBarColor(activity, color, false);
    }

    public static void setStatusBarColor(Activity activity, int color, boolean darkMode) {
        if (Build.VERSION.SDK_INT >= 23) {
            Window window = activity.getWindow();
            window.addFlags(-2147483648);
            window.clearFlags(67108864);
            if (!darkMode) {
                window.getDecorView().setSystemUiVisibility(8192);
            } else {
                window.getDecorView().setSystemUiVisibility(s_SystemUiVisibilityOrigin);
            }

            window.setStatusBarColor(color);
            if ("Xiaomi".equals(Build.MANUFACTURER)) {
                setXiaomiStatusBarDarkMode(!darkMode, activity);
            }
        }

    }

    private static void setXiaomiStatusBarDarkMode(boolean darkmode, Activity activity) {
        if (Build.VERSION.SDK_INT >= 23) {
            Class clazz = activity.getWindow().getClass();

            try {
                int darkModeFlag = 0;
                Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
                Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
                darkModeFlag = field.getInt(layoutParams);
                Method extraFlagField = clazz.getMethod("setExtraFlags", Integer.TYPE, Integer.TYPE);
                extraFlagField.invoke(activity.getWindow(), darkmode ? darkModeFlag : 0, darkModeFlag);
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

    public static int getNavBarHeight(Context context) {
        if (!hasNavBar(context)) {
            return 0;
        }
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        int height = resources.getDimensionPixelSize(resourceId);
        return height;
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
            return (String)getInvoke.invoke(osSystem, key);
        } catch (Exception var3) {
            var3.printStackTrace();
            return "";
        }
    }

}
