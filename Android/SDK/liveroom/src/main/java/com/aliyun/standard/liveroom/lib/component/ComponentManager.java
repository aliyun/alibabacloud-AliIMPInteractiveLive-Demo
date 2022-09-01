package com.aliyun.standard.liveroom.lib.component;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.dingpaas.room.RoomDetail;
import com.aliyun.roompaas.base.base.Consumer;
import com.aliyun.roompaas.base.util.CollectionUtil;
import com.aliyun.standard.liveroom.lib.ComponentSlot;
import com.aliyun.standard.liveroom.lib.LiveContext;
import com.aliyun.standard.liveroom.lib.LiveHook;
import com.aliyun.standard.liveroom.lib.LivePrototype;
import com.aliyun.standard.liveroom.lib.event.EventManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * @author puke
 * @version 2021/7/29
 */
public class ComponentManager extends EventManager {

    private final List<IComponent> components = new ArrayList<>();

    public void scanComponent(@NonNull View view) {
        List<IComponent> components = scanComponentInternal(view);
        this.components.addAll(components);
        sortComponents();
    }

    public void addComponentFromHook() {
        LiveHook liveHook = LivePrototype.getInstance().getLiveHook();
        if (liveHook == null) {
            return;
        }

        List<ComponentSlot> componentSlots = liveHook.getComponentSlots();
        for (ComponentSlot componentSlot : componentSlots) {
            IComponent component = componentSlot.getComponent();
            if (component != null) {
                components.add(component);
            }
        }
        sortComponents();
    }

    private void sortComponents() {
        // 根据order对组件进行排序
        Collections.sort(components, new Comparator<IComponent>() {
            @Override
            public int compare(IComponent o1, IComponent o2) {
                return o1.getOrder() - o2.getOrder();
            }
        });
    }

    public void dispatchInit(@NonNull final LiveContext liveContext) {
        dispatch(new Consumer<IComponent>() {
            @Override
            public void accept(IComponent component) {
                component.onInit(liveContext);
            }
        });
    }

    public void dispatchEnterRoomSuccess(final RoomDetail roomDetail) {
        dispatch(new Consumer<IComponent>() {
            @Override
            public void accept(IComponent component) {
                component.onEnterRoomSuccess(roomDetail);
            }
        });
    }

    public void dispatchEnterRoomError(final String errorMsg) {
        dispatch(new Consumer<IComponent>() {
            @Override
            public void accept(IComponent component) {
                component.onEnterRoomError(errorMsg);
            }
        });
    }

    public void dispatchActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data) {
        dispatch(new Consumer<IComponent>() {
            @Override
            public void accept(IComponent component) {
                component.onActivityResult(requestCode, resultCode, data);
            }
        });
    }

    public void dispatchActivityPause() {
        dispatch(new Consumer<IComponent>() {
            @Override
            public void accept(IComponent component) {
                component.onActivityPause();
            }
        });
    }

    public void dispatchActivityResume() {
        dispatch(new Consumer<IComponent>() {
            @Override
            public void accept(IComponent component) {
                component.onActivityResume();
            }
        });
    }

    public void dispatchActivityDestroy() {
        dispatch(new Consumer<IComponent>() {
            @Override
            public void accept(IComponent component) {
                component.onActivityDestroy();
            }
        });
    }

    public void dispatchActivityFinish() {
        dispatch(new Consumer<IComponent>() {
            @Override
            public void accept(IComponent component) {
                component.onActivityFinish();
            }
        });
    }

    public void dispatchActivityConfigurationChanged(final Configuration newConfig) {
        dispatch(new Consumer<IComponent>() {
            @Override
            public void accept(IComponent component) {
                component.onActivityConfigurationChanged(newConfig);
            }
        });
    }

    public boolean interceptBackKey() {
        for (IComponent component : components) {
            if (component.interceptBackKey()) {
                return true;
            }
        }
        return false;
    }

    private void dispatch(Consumer<IComponent> consumer) {
        for (IComponent component : components) {
            consumer.accept(component);
        }
    }

    private List<IComponent> scanComponentInternal(View view) {
        List<IComponent> result = new ArrayList<>();
        if (view instanceof ComponentHolder) {
            IComponent component = ((ComponentHolder) view).getComponent();
            mergeNotRepeat(result, Collections.singletonList(component));
        }
        // 支持一个View对应多个Component的场景
        if (view instanceof MultiComponentHolder) {
            MultiComponentHolder multiComponentHolder = (MultiComponentHolder) view;
            mergeNotRepeat(result, multiComponentHolder.getComponents());
        }
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                mergeNotRepeat(result, scanComponentInternal(child));
            }
        }
        return result;
    }

    private static void mergeNotRepeat(List<IComponent> origins, List<IComponent> added) {
        if (CollectionUtil.isNotEmpty(added)) {
            for (IComponent component : added) {
                if (component != null && !origins.contains(component)) {
                    origins.add(component);
                }
            }
        }
    }
}
