package com.aliyun.roompaas.app.helper;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.aliyun.roompaas.app.R;
import com.aliyun.roompaas.app.util.AppUtil;


/**
 * 底部悬浮视图辅助类
 *
 * @author puke
 * @version 2019/6/10
 */
public class ActivityFloatHelper {

    // 默认空出屏幕的 3/8
    private static final int DEFAULT_TOP_OFFSET = AppUtil.getScreenHeight() * 3 / 8;

    public final ViewGroup rootLayout;
    public final View mask;
    public final ViewGroup bodyContainer;

    private final Activity activity;
    private final View view;
    private final int layout;

    private int animationDuration = 300;
    private int topOffset = DEFAULT_TOP_OFFSET;

    private ValueAnimator animator;
    private boolean isShow;

    @Nullable
    private View inflatedLayer;

    @SuppressLint("InflateParams")

    public ActivityFloatHelper(Activity activity, @LayoutRes int layout) {
        this.activity = activity;
        this.layout = layout;

        LayoutInflater inflater = LayoutInflater.from(activity);
        view = inflater.inflate(R.layout.view_activity_float, null);

        rootLayout = view.findViewById(R.id.view_root_layout);
        mask = view.findViewById(R.id.view_mask);
        bodyContainer = view.findViewById(R.id.view_body_container);
        bodyContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });

        rootLayout.setOnClickListener(v -> hide());
    }

    public <V extends View> V findViewById(@IdRes int id) {
        return view.findViewById(id);
    }

    public ActivityFloatHelper setAnimationDuration(int animationDuration) {
        this.animationDuration = animationDuration;
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
        return this;
    }

    public ActivityFloatHelper setTopOffset(int topOffset) {
        this.topOffset = topOffset;
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
        return this;
    }

    @Nullable
    public View show() {
        if (!isShow) {
            inflatedLayer = addViewIfNeed();
            isShow = true;
            initAnimatorIfNeed();
            animator.start();
        }
        return inflatedLayer;
    }

    public void hide() {
        if (!isShow) {
            return;
        }
        isShow = false;
        addViewIfNeed();
        initAnimatorIfNeed();
        animator.reverse();
    }

    @Nullable
    private View addViewIfNeed() {
        if (view.getParent() == null) {
            // 默认出于屏幕之下
            ViewGroup.MarginLayoutParams layoutParams = new ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
            layoutParams.topMargin = AppUtil.getScreenHeight() - topOffset;
            activity.addContentView(view, layoutParams);

            setFloatLayoutMarginTop(topOffset);
            inflatedLayer = View.inflate(activity, layout, bodyContainer);
        }
        return inflatedLayer;
    }

    public boolean isShow() {
        return isShow;
    }

    private void initAnimatorIfNeed() {
        if (animator != null) {
            return;
        }
        int initTopMargin = AppUtil.getScreenHeight() - topOffset;
        animator = ValueAnimator.ofInt(initTopMargin, 0);
        animator.setDuration(animationDuration);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation, boolean isReverse) {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation, boolean isReverse) {
                mask.setVisibility(isReverse ? View.GONE : View.VISIBLE);
                if (isReverse) {
                    view.setVisibility(View.GONE);
                }
            }
        });
        animator.addUpdateListener(animation -> {
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) rootLayout.getLayoutParams();
            layoutParams.topMargin = (int) animation.getAnimatedValue();
            rootLayout.setLayoutParams(layoutParams);
        });
    }

    private void setFloatLayoutMarginTop(int topOffset) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) bodyContainer.getLayoutParams();
        layoutParams.topMargin = topOffset;
        bodyContainer.setLayoutParams(layoutParams);
    }

}
