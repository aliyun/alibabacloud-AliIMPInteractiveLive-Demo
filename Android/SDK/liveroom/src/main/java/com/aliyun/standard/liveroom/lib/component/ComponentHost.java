package com.aliyun.standard.liveroom.lib.component;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.aliyun.standard.liveroom.lib.R;

/**
 * @author puke
 * @version 2021/8/30
 */
public class ComponentHost extends View implements ComponentHolder {

    private final IComponent component;

    public ComponentHost(Context context) {
        this(context, null, 0);
    }

    public ComponentHost(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ComponentHost(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setVisibility(GONE);

        String componentClassName = parseComponentClassName(attrs);
        component = buildComponent(componentClassName);
    }

    protected String parseComponentClassName(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ComponentHost);
        String componentClassName = typedArray.getString(R.styleable.ComponentHost_component);
        typedArray.recycle();
        if (TextUtils.isEmpty(componentClassName)) {
            throw new RuntimeException("No component attribute found");
        }
        return componentClassName;
    }

    private IComponent buildComponent(String componentClassName) {
        try {
            Class<?> componentType = Class.forName(componentClassName);
            return (IComponent) componentType.newInstance();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("can't find class", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("can't invoke component constructor", e);
        } catch (InstantiationException e) {
            throw new RuntimeException("create component instance fail", e);
        }
    }

    @Override
    public IComponent getComponent() {
        return component;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // zero size
        int minSizeSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY);
        super.onMeasure(minSizeSpec, minSizeSpec);
    }
}
