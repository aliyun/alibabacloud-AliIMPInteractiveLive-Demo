package com.aliyun.roompaas.player;

import android.support.annotation.Nullable;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.aliyun.roompaas.base.log.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author puke
 * @version 2022/5/23
 */
public class LivePlayerManagerHolder {

    private static final String TAG = "LivePlayerManagerHolder";

    // Value标识当前Manger是否需要hold (变小窗时需要hold, 此时生命周期与Activity解绑)
    private static final Map<LivePlayerManager, Boolean> MANAGER_2_HOLD = new HashMap<>();

    @Nullable
    public static LivePlayerManager getHoldPlayerManager() {
        LivePlayerManager ret = null;
        for (Map.Entry<LivePlayerManager, Boolean> entry : MANAGER_2_HOLD.entrySet()) {
            if (entry.getValue()) {
                ret = entry.getKey();
                break;
            }
        }
        Logger.i(TAG, "getHoldPlayerManager, ret=" + ret);
        return ret;
    }

    public static void hold(View renderView) {
        Logger.i(TAG, "hold");
        setHoldValue(renderView, true);
    }

    public static void unHold(View renderView) {
        Logger.i(TAG, "unHold");
        setHoldValue(renderView, false);
    }


    private static void setHoldValue(View renderView, boolean value) {
        LivePlayerManager manager = getLivePlayerManagerFromViewTag(renderView);
        Logger.i(TAG, "setHoldValue, manager=" + manager);
        if (manager == null) {
            return;
        }

        for (Map.Entry<LivePlayerManager, Boolean> entry : MANAGER_2_HOLD.entrySet()) {
            if (entry.getKey() == manager) {
                entry.setValue(value);
                Logger.i(TAG, String.format(
                        "setHoldValue, manager=%s, value=%s", manager, value));
                break;
            }
        }
    }

    private static LivePlayerManager getLivePlayerManagerFromViewTag(View view) {
        View surfaceView = findSurfaceView(view);
        if (surfaceView == null) {
            return null;
        }

        Object tag = surfaceView.getTag(R.id.vpaas_view_with_player_manager);
        if (tag instanceof LivePlayerManager) {
            return (LivePlayerManager) tag;
        } else {
            return null;
        }
    }

    private static View findSurfaceView(View view) {
        if (view instanceof SurfaceView) {
            return view;
        } else if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View target = findSurfaceView(viewGroup.getChildAt(i));
                if (target != null) {
                    return target;
                }
            }
            return null;
        } else {
            return null;
        }
    }

    public static void setLivePlayerManager(LivePlayerManager manager) {
        Logger.i(TAG, "setLivePlayerManager, manager=" + manager);

        if (manager != null) {
            MANAGER_2_HOLD.put(manager, false);
        }
    }

    public static void stopPlay(LivePlayerManager manager) {
        Logger.i(TAG, "stopPlay, manager=" + manager);
        if (manager == null) {
            return;
        }

        if (MANAGER_2_HOLD.containsKey(manager)) {
            if (Boolean.FALSE.equals(MANAGER_2_HOLD.get(manager))) {
                manager.stopPlay();
            }
        } else {
            manager.stopPlay();
        }
    }

    public static void destroy(LivePlayerManager manager) {
        Logger.i(TAG, "destroy, manager=" + manager);
        if (manager == null) {
            return;
        }

        if (MANAGER_2_HOLD.containsKey(manager)) {
            if (Boolean.FALSE.equals(MANAGER_2_HOLD.get(manager))) {
                manager.destroy();
                MANAGER_2_HOLD.remove(manager);
            }
        } else {
            manager.destroy();
        }
    }

    public static void destroyHoldManager() {
        Logger.i(TAG, "destroyHoldManager");
        Iterator<Map.Entry<LivePlayerManager, Boolean>> iterator = MANAGER_2_HOLD.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<LivePlayerManager, Boolean> entry = iterator.next();
            if (entry.getValue()) {
                LivePlayerManager manager = entry.getKey();
                manager.destroy();
                iterator.remove();
                Logger.i(TAG, "destroyHoldManager, manager=" + manager);
            }
        }
    }

    /**
     * 兼容老版本, 保留的api签名
     *
     * @param force 是否强制销毁
     */
    @Deprecated
    public static void destroy(boolean force) {
        Logger.i(TAG, "destroy, force=" + force);
        if (force) {
            destroyHoldManager();
        }
    }
}
