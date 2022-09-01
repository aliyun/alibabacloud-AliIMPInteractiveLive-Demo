package com.aliyun.roompaas.uibase.util.immersionbar;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.view.WindowInsets;

import com.aliyun.roompaas.uibase.R;

/**
 * Created by KyleCe on 2021/10/19
 */
public class NavigationBarUtils implements Constants {

    public static boolean isNavigationBarShown(Context context) {
        return context instanceof Activity && isNavigationBarShown(((Activity) context).getApplication());
    }

    public static boolean isNavigationBarShown(Activity activity) {
        return activity != null && isNavigationBarShown((activity).getApplication());
    }

    public static boolean isNavigationBarShown(Application application) {
        if (application == null) {
            return false;
        }
        int show = 0;
        if (OSUtils.isMIUI()) {
            show = Settings.Global.getInt(application.getContentResolver(), IMMERSION_MIUI_NAVIGATION_BAR_HIDE_SHOW, 0);
        } else if (OSUtils.isEMUI()) {
            if (OSUtils.isEMUI3_x() || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                show = Settings.System.getInt(application.getContentResolver(), IMMERSION_EMUI_NAVIGATION_BAR_HIDE_SHOW, 0);
            } else {
                show = Settings.Global.getInt(application.getContentResolver(), IMMERSION_EMUI_NAVIGATION_BAR_HIDE_SHOW, 0);
            }
        }
        return show != 1;
    }

    public static void addListenerForNavSateChangeOnlyOncePerView(Activity activity, final OnNavigationStateListener onNavigationStateListener) {
        if (activity == null) {
            return;
        }
        final int height = getNavigationHeight(activity);

        View decor = activity.getWindow().getDecorView();
        if (decor.getTag(R.integer.viewTagForApplyWindowInsetsListener) == null) {
            View.OnApplyWindowInsetsListener listener = new View.OnApplyWindowInsetsListener() {
                @Override
                public WindowInsets onApplyWindowInsets(View v, WindowInsets windowInsets) {
                    boolean isShowing = false;
                    int b = 0;
                    if (windowInsets != null) {
                        b = windowInsets.getSystemWindowInsetBottom();
                        isShowing = (b == height);
                    }
                    if (onNavigationStateListener != null && b <= height) {
                        onNavigationStateListener.onNavigationState(isShowing, b);
                    }
                    return windowInsets;
                }
            };
            decor.setOnApplyWindowInsetsListener(listener);
            decor.setTag(R.integer.viewTagForApplyWindowInsetsListener, listener.hashCode());
        }
    }

    public interface OnNavigationStateListener {
        void onNavigationState(boolean isShowing, int height);
    }

    public static int getNavigationHeight(Context activity) {
        if (activity == null) {
            return 0;
        }
        Resources resources = activity.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height",
                "dimen", "android");
        int height = 0;
        if (resourceId > 0) {
            //获取NavigationBar的高度
            height = resources.getDimensionPixelSize(resourceId);
        }
        return height;
    }
}
