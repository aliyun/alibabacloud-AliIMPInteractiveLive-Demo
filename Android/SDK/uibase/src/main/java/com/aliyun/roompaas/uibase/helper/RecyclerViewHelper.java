package com.aliyun.roompaas.uibase.helper;

import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aliyun.roompaas.base.util.CollectionUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 封装{@link RecyclerView}辅助类<br/>
 * 主要为了简化使用单ItemType的RecyclerView时冗余的{@link RecyclerView.ViewHolder}和{@link RecyclerView.Adapter}设置流程
 *
 * @author puke
 * @version 2021/5/26
 */
public class RecyclerViewHelper<T> {

    private final RecyclerView recyclerView;
    private final ItemViewFactory itemViewFactory;
    private final HolderRenderer<T> holderRenderer;
    private final Adapter adapter;
    private final List<T> dataList = new ArrayList<>();

    public static <T> RecyclerViewHelper<T> of(@NonNull RecyclerView recyclerView,
                                               @LayoutRes final int itemLayoutRes,
                                               @NonNull HolderRenderer<T> holderRenderer) {
        return new RecyclerViewHelper<>(recyclerView, new ItemViewFactory() {
            @NonNull
            @Override
            public View getItemView(ViewGroup parent) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                return inflater.inflate(itemLayoutRes, parent, false);
            }
        }, holderRenderer);
    }

    private RecyclerViewHelper(@NonNull RecyclerView recyclerView,
                               @NonNull ItemViewFactory itemViewFactory,
                               @NonNull HolderRenderer<T> holderRenderer) {
        this.recyclerView = recyclerView;
        this.itemViewFactory = itemViewFactory;
        this.holderRenderer = holderRenderer;

        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager == null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(
                    recyclerView.getContext(), LinearLayoutManager.VERTICAL, false));
        }
        adapter = new Adapter();
        recyclerView.setAdapter(adapter);
    }

    /**
     * 设置新数据源
     *
     * @param newData 新数据源
     */
    public void setData(List<T> newData) {
        dataList.clear();
        if (newData != null) {
            dataList.addAll(newData);
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * 添加数据源
     *
     * @param addedData 新增的数据源
     */
    public void addData(List<T> addedData) {
        if (CollectionUtil.isNotEmpty(addedData)) {
            int originSize = dataList.size();
            dataList.addAll(addedData);
            adapter.notifyItemRangeInserted(originSize, addedData.size());
        }
    }

    public void insertCell(int index, T toAdd) {
        boolean indexInRange = index >= 0 && (CollectionUtil.isNotEmpty(dataList) && index <= dataList.size() - 1)
                || (CollectionUtil.isEmpty(dataList) && index == 0);
        if (indexInRange) {
            dataList.add(index, toAdd);
            adapter.notifyItemInserted(index);
        }
    }

    /**
     * 移除数据
     *
     * @param positionStart 待移除数据的开始索引值
     * @param removeCount   待移除数据条数
     */
    public void removeData(int positionStart, int removeCount) {
        int size = dataList.size();
        if (positionStart >= 0 && positionStart < size && removeCount <= size) {
            dataList.removeAll(dataList.subList(positionStart, positionStart + removeCount));
            adapter.notifyItemRangeRemoved(positionStart, removeCount);
        }
    }

    /**
     * 移除数据 (不带动画, 全局刷新)
     *
     * @param positionStart 待移除数据的开始索引值
     * @param removeCount   待移除数据条数
     */
    public void removeDataWithoutAnimation(int positionStart, int removeCount) {
        int size = dataList.size();
        if (positionStart >= 0 && positionStart < size && removeCount <= size) {
            dataList.removeAll(dataList.subList(positionStart, positionStart + removeCount));
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * 移除数据
     *
     * @param position 待移除数据的索引值
     */
    public void removeData(int position) {
        if (position >= 0 && position < dataList.size()) {
            dataList.remove(position);
            adapter.notifyItemRemoved(position);
        }
    }

    /**
     * 移除全部数据
     */
    public void removeAll() {
        dataList.clear();
        adapter.notifyDataSetChanged();
    }

    /**
     * 刷新指定项数据源
     *
     * @param position 待刷新的索引值
     */
    public void updateData(int position) {
        if (position >= 0 && position < dataList.size()) {
            adapter.notifyItemChanged(position);
        }
    }

    public int getItemCount() {
        return adapter.getItemCount();
    }

    /**
     * @return 当前的RecyclerView对象
     */
    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    /**
     * @return 数据源 (注: 不可更改)
     */
    public List<T> getDataList() {
        return Collections.unmodifiableList(dataList);
    }

    private class Adapter extends RecyclerView.Adapter<ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // ItemView具体的构造逻辑, 回调给外部处理
            View itemView = itemViewFactory.getItemView(parent);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            // 获取当前数据模型
            T model = dataList.get(position);
            // 通过渲染器回调, 将真实的数据回调抛出去
            holderRenderer.render(holder, model, position, getItemCount());
        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final SparseArray<View> viewCaches = new SparseArray<>();

        ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @SuppressWarnings("unchecked")
        public <V extends View> V getView(@IdRes int id) {
            // 使用ViewCache来解决"抽象层无法穷举成员变量"和"重复findViewById调用"的冲突
            View view = viewCaches.get(id);
            if (view == null) {
                view = itemView.findViewById(id);
                viewCaches.put(id, view);
            }
            return (V) view;
        }
    }

    /**
     * ItemView工厂类
     */
    public interface ItemViewFactory {
        @NonNull
        View getItemView(ViewGroup parent);
    }

    /**
     * ViewHolder渲染器
     *
     * @param <T> 数据模型
     */
    public interface HolderRenderer<T> {
        void render(ViewHolder holder, T model, int position, int itemCount);
    }
}
