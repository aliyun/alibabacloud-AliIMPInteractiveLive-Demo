package com.aliyun.standard.liveroom.lib;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatViewInflater;
import android.util.AttributeSet;
import android.view.View;

import com.aliyun.roompaas.base.log.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * 直播间默认组件hook拦截, 支持外部进行默认组件的替换能力
 *
 * @author puke
 * @version 2021/11/10
 */
public class LiveViewInflater extends AppCompatViewInflater {

    private static final String TAG = LiveViewInflater.class.getSimpleName();

    private static final String COMPONENT_VIEW_PACKAGE = "com.aliyun.standard.liveroom.lib.component.view";

    private static final Class<?>[] viewConstructorArgTypes = new Class[]{Context.class, AttributeSet.class};
    private static final Object[] viewConstructorArgs = new Object[2];

    @Nullable
    @Override
    protected View createView(Context context, String name, AttributeSet attrs) {
        LiveHook liveHook = LivePrototype.getInstance().getLiveHook();
        if (liveHook != null && name.startsWith(COMPONENT_VIEW_PACKAGE)) {
            try {
                Class<?> currentComponentViewType = Class.forName(name, false, getClass().getClassLoader());
                Map<Class<? extends View>, Class<? extends View>> replaceComponentViewTypes =
                        liveHook.getReplaceComponentViewTypes();
                Class<? extends View> replaceType = replaceComponentViewTypes.get(currentComponentViewType);
                if (replaceType != null) {
                    viewConstructorArgs[0] = context;
                    viewConstructorArgs[1] = attrs;
                    View view = replaceType.getConstructor(viewConstructorArgTypes).newInstance(viewConstructorArgs);
                    Logger.i(TAG, String.format("Replace %s with %s", currentComponentViewType, replaceType));
                    return view;
                }
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException
                    | InvocationTargetException | NoSuchMethodException e) {
                Logger.e(TAG, String.format("Hook createView with %s failure", name), e);
            } finally {
                viewConstructorArgs[0] = null;
                viewConstructorArgs[1] = null;
            }
        }
        return null;
    }
}
