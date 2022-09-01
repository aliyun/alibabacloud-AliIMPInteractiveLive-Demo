package com.aliyun.standard.liveroom.lib;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;

import com.aliyun.standard.liveroom.lib.component.ComponentHolder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author puke
 * @version 2021/7/30
 */
public class LiveHook implements Serializable {

    private ViewSlot upperRightSlot;

    private ViewSlot middleSlot;

    private ViewSlot goodsSlot;

    private ViewSlot readySlot;

    private Integer liveLayoutRes;

    private final List<ComponentSlot> componentSlots = new ArrayList<>();

    private final Map<Class<? extends View>, Class<? extends View>> replaceComponentViewTypes = new ConcurrentHashMap<>();

    public ViewSlot getUpperRightSlot() {
        return upperRightSlot;
    }

    public ViewSlot getMiddleSlot() {
        return middleSlot;
    }

    public ViewSlot getGoodsSlot() {
        return goodsSlot;
    }

    public ViewSlot getReadySlot() {
        return readySlot;
    }

    @NonNull
    public List<ComponentSlot> getComponentSlots() {
        return componentSlots;
    }

    @NonNull
    public Map<Class<? extends View>, Class<? extends View>> getReplaceComponentViewTypes() {
        return replaceComponentViewTypes;
    }

    public Integer getLiveLayoutRes() {
        return liveLayoutRes;
    }

    public LiveHook setUpperRightSlot(ViewSlot upperRightSlot) {
        this.upperRightSlot = upperRightSlot;
        return this;
    }

    public LiveHook setMiddleSlot(ViewSlot middleSlot) {
        this.middleSlot = middleSlot;
        return this;
    }

    public LiveHook setGoodsSlot(ViewSlot goodsSlot) {
        this.goodsSlot = goodsSlot;
        return this;
    }

    public LiveHook setReadySlot(ViewSlot readySlot) {
        this.readySlot = readySlot;
        return this;
    }

    public LiveHook addComponentSlots(ComponentSlot... componentSlots) {
        this.componentSlots.addAll(Arrays.asList(componentSlots));
        return this;
    }

    /**
     * 替换某一个指定的组件View
     */
    public <O extends View & ComponentHolder, N extends View & ComponentHolder> LiveHook replaceComponentView(
            Class<O> oldComponentViewType, Class<N> newComponentViewType) {
        replaceComponentViewTypes.put(oldComponentViewType, newComponentViewType);
        return this;
    }

    public LiveHook setLiveLayoutRes(@LayoutRes int liveLayoutRes) {
        this.liveLayoutRes = liveLayoutRes;
        return this;
    }
}
