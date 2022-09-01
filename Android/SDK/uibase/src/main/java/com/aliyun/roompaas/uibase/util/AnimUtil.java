package com.aliyun.roompaas.uibase.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.AnimRes;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;

import com.aliyun.roompaas.base.util.Utils;
import com.aliyun.roompaas.uibase.R;
import com.aliyun.roompaas.uibase.listener.SimpleAnimationListener;
import com.aliyun.roompaas.uibase.listener.SimpleAnimatorListener;

import static android.animation.ValueAnimator.INFINITE;


public class AnimUtil {
    public static final long DEFAULT_TIME = 300;

    public static final float SWITCH_SCENE_BG_ANIM_START_ALPHA = 0f;
    public static final long SWITCH_SCENE_ANIM_DURA = 500;

    private static final int VISIBILITY_UNSET = -1;
    private static final int DEFAULT_VISIBILITY_FOR_ANIM_IN = View.VISIBLE;
    private static final int DEFAULT_VISIBILITY_FOR_ANIM_OUT = View.GONE;

    public static void animIn(View view) {
        animIn(DEFAULT_VISIBILITY_FOR_ANIM_IN, view);
    }

    public static void animIn(int endVisibility, View view) {
        animIn(endVisibility, view, null);
    }

    public static void animIn(View view, @Nullable final Runnable endAction) {
        animIn(DEFAULT_VISIBILITY_FOR_ANIM_IN, view, endAction);
    }

    public static void animIn(int endVisibility, View view, @Nullable final Runnable endAction) {
        animIn(endVisibility, endAction, view);
    }

    public static void animIn(@Nullable final Runnable endAction, View... vs) {
        animIn(DEFAULT_VISIBILITY_FOR_ANIM_IN, endAction, vs);
    }

    public static void animIn(View... vs) {
        animIn(DEFAULT_VISIBILITY_FOR_ANIM_IN, null, vs);
    }

    public static void animIn(int endVisibility, View... vs) {
        animIn(endVisibility, null, vs);
    }

    public static void animIn(final int endVisibility, @Nullable final Runnable endAction, final View... vs) {
        if (Utils.isEmpty(vs)) {
            return;
        }

        Animator.AnimatorListener listener = endAction == null ? null : new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                Utils.run(endAction);
                ViewUtil.setVisibilityIfNecessary(endVisibility, vs);
            }
        };

        boolean listenerAttached = false;
        for (View view : vs) {
            if (view == null) {
                continue;
            }
            if (view.getAlpha() != SWITCH_SCENE_BG_ANIM_START_ALPHA) {
                view.setAlpha(SWITCH_SCENE_BG_ANIM_START_ALPHA);
            }
            view.animate().alpha(1).setDuration(SWITCH_SCENE_ANIM_DURA).setListener(!listenerAttached ? listener : null).start();
            ViewUtil.setVisible(view);
            listenerAttached = true;
        }
        if (!listenerAttached) {
            Utils.run(endAction);
        }
    }

    public static void animOut(View view) {
        animOut(VISIBILITY_UNSET, view);
    }

    public static void animOut(int endVisibility, View view) {
        animOut(endVisibility, view, null);
    }

    public static void animOut(View view, final @Nullable Runnable endAction) {
        animOut(VISIBILITY_UNSET, view, endAction);
    }

    public static void animOut(int endVisibility, View view, final @Nullable Runnable endAction) {
        if (view == null) {
            return;
        }
        animOut(endVisibility, endAction, view);
    }

    public static void animOut(View view, final @Nullable Runnable middleUiAction, final @Nullable Runnable endAction) {
        animOut(VISIBILITY_UNSET, view, middleUiAction, endAction);
    }

    public static void animOut(int endVisibility, View view, final @Nullable Runnable middleUiAction, final @Nullable Runnable endAction) {
        if (view == null) {
            return;
        }
        animOut(endVisibility, endAction, view);
        view.postDelayed(middleUiAction, SWITCH_SCENE_ANIM_DURA >> 1);
    }

    public static void animOut(int endVisibility, View... vs) {
        animOut(endVisibility, null, vs);
    }

    public static void animOut(final @Nullable Runnable endAction, View... vs) {
        animOut(VISIBILITY_UNSET, endAction, vs);
    }

    /**
     * we do not use GONE as default end visibility because the GONE may have side effects on Layout
     */
    public static void animOut(final int endVisibility, final @Nullable Runnable endAction, final View... vs) {
        if (Utils.isEmpty(vs)) {
            return;
        }

        Animator.AnimatorListener listener = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                Utils.run(endAction);
                ViewUtil.setVisibilityIfNecessary(endVisibility, vs);
            }
        };

        boolean listenerAttached = false;
        for (View view : vs) {
            if (view == null) {
                continue;
            }
            view.animate().alpha(0).setDuration(SWITCH_SCENE_ANIM_DURA).setListener(!listenerAttached ? listener : null).start();
            listenerAttached = true;
        }
        if (!listenerAttached) {
            Utils.run(endAction);
        }
    }

    public static void animBottomUp(@Nullable View view) {
        animBottomUp(null, view);
    }

    public static void animBottomUp(@Nullable final Runnable endAction, @Nullable View... views) {
        anim(VISIBILITY_UNSET, R.anim.iub_anim_bottom_up, endAction, views);
    }

    public static void animBottomDown(@Nullable View view) {
        animBottomDown(null, view);
    }

    public static void animBottomDown(@Nullable final Runnable endAction, @Nullable View... views) {
        anim(View.GONE, R.anim.iub_anim_bottom_down, endAction, views);
    }

    public static void animRightOut(@Nullable View view) {
        animRightOut(null, view);
    }

    public static void animRightOut(@Nullable final Runnable endAction, @Nullable View... views) {
        animRightOut(View.GONE, endAction, views);
    }
    public static void animRightOut(final int endVisibility, @Nullable final Runnable endAction, @Nullable View... views) {
        anim(endVisibility, R.anim.iub_right_out, endAction, views);
    }

    public static void animRightIn(@Nullable View view) {
        animRightIn(null, view);
    }

    public static void animRightIn(@Nullable final Runnable endAction, @Nullable View... views) {
        animRightIn(VISIBILITY_UNSET, endAction, views);
    }

    public static void animRightIn(final int endVisibility, @Nullable final Runnable endAction, @Nullable View... views) {
        anim(endVisibility, R.anim.iub_right_in, endAction, views);
    }

    public static void anim(final int endVisibility, @AnimRes int id, @Nullable final Runnable endAction, @Nullable final View... views) {
        if (views == null || views.length == 0) {
            return;
        }

        Animation.AnimationListener listener = new SimpleAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                Utils.run(endAction);
                if (endVisibility != VISIBILITY_UNSET) {
                    ViewUtil.setVisibilityIfNecessary(endVisibility, views);
                }
            }
        };

        boolean listenerAttached = false;
        Animation anim = null;
        Context appContext = null;
        ViewUtil.setVisibilityIfNecessary(View.VISIBLE, views);
        for (View view : views) {
            if (view == null || (appContext = Utils.acceptFirstNonNull(appContext, view.getContext())) == null) {
                continue;
            }

            if (anim == null) {
                anim = AnimationUtils.loadAnimation(appContext, id);
            }
            view.startAnimation(anim);
            anim.setAnimationListener(!listenerAttached ? listener : null);
            listenerAttached = true;
        }
        if (!listenerAttached) {
            Utils.run(endAction);
        }
    }

    public static void slidDown(@Nullable View view) {
        slidDown(view, null);
    }

    public static void slidDown(@Nullable View view, final Runnable endAction) {
        slidDown(endAction, view);
    }

    public static void slidDown(@Nullable final Runnable endAction, View... vs) {
        if (vs == null || vs.length == 0) {
            return;
        }

        int screenHeight = AppUtil.getScreenHeight();
        boolean listenerAttached = false;
        for (View view : vs) {
            if (view == null) {
                continue;
            }
            view.animate().translationY(screenHeight).setDuration(SWITCH_SCENE_ANIM_DURA).withEndAction(!listenerAttached ? endAction : null).start();
            listenerAttached = true;
        }
        if (!listenerAttached) {
            Utils.run(endAction);
        }
    }

    public static void expandOrFoldViewViaHeightAnimation(@Nullable final View toExpandView, final int toHeight,
                                                          @Nullable final ValueAnimator.AnimatorUpdateListener updateListener,
                                                          @Nullable final SimpleAnimatorListener animatorListener) {
        if (toExpandView == null) {
            return;
        }
        final int fromHeight = toExpandView.getMeasuredHeight();
        ValueAnimator anim = ValueAnimator.ofInt(fromHeight, toHeight);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams lp = toExpandView.getLayoutParams();
                lp.height = val;
                toExpandView.setLayoutParams(lp);
                if (updateListener != null) {
                    updateListener.onAnimationUpdate(valueAnimator);
                }
            }
        });
        if (animatorListener != null) {
            anim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    animatorListener.onAnimationStart(animation);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    animatorListener.onAnimationEnd(animation);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    animatorListener.onAnimationCancel(animation);
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                    animatorListener.onAnimationRepeat(animation);
                }
            });
        }
        anim.setDuration(DEFAULT_TIME);
        anim.start();
    }

    public static void fadeIn(View... vs) {
        fadeIn(null, vs);
    }

    public static void fadeIn(@Nullable final AnimEndListener listener, View... vs) {
        fadeIn(listener, DEFAULT_TIME, vs);
    }

    public static void fadeIn(long dura, View... vs) {
        fadeIn(null, dura, vs);
    }

    public static void fadeIn(@Nullable final AnimEndListener listener, long dura, View... vs) {
        fade(listener, dura, 0f, 1.0f, vs);
    }

    public static void fadeOut(View... vs) {
        fadeOut(null, vs);
    }

    public static void fadeOut(@Nullable final AnimEndListener listener, View... vs) {
        fadeOut(listener, DEFAULT_TIME, vs);
    }

    public static void fadeOut(long dura, View... vs) {
        fadeOut(null, dura, vs);
    }

    public static void fadeOut(@Nullable final AnimEndListener listener, long dura, View... vs) {
        fade(listener, dura, 1.0f, 0f, vs);
    }

    public static void fade(@Nullable final AnimEndListener listener, long dura, float start, float end, View... vs) {
        if (vs == null || vs.length == 0 || start == end) {
            return;
        }

        AnimatorSet set = new AnimatorSet();
        for (View v : vs) {
            if (v == null) {
                continue;
            }
            set.play(ObjectAnimator.ofFloat(v, "alpha", start, end));
        }
        set.setDuration(dura);
        if (listener != null) {
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    listener.animEnd();
                }
            });
        }
        set.start();
    }

    public static void animBackgroundColor(View view, int from, int to, long time) {
        ValueAnimator colorAnim = ObjectAnimator.ofInt(view, "backgroundColor", from, to);
        colorAnim.setDuration(time);
        colorAnim.setEvaluator(new ArgbEvaluator());
        colorAnim.start();
    }

    public static AnimatorSet translationY(View view, float from, float to, AnimEndListener listener) {
        return translationY(view, from, to, DEFAULT_TIME, listener);
    }

    public static AnimatorSet scale(View view, float from, float to, long animDuration, AnimEndListener listener) {
        AnimatorSet set = genScale(view, from, to, animDuration, listener);
        set.start();
        return set;
    }

    public static AnimatorSet genScale(View view, float from, float to, long animDuration, final AnimEndListener listener) {
        AnimatorSet set = new AnimatorSet();
        set.play(ObjectAnimator.ofFloat(view, "scaleX", from, to));
        set.play(ObjectAnimator.ofFloat(view, "scaleY", from, to));
        set.setDuration(animDuration);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (listener != null) {
                    listener.animEnd();
                }
            }
        });
        return set;
    }

    public static ObjectAnimator rotateView(final View view, int duration) {
        /*view.setPivotX(view.getMeasuredHeight() / 2);
        view.setPivotY(view.getMeasuredWidth() / 2);*/
        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "rotation", 0, 360);
        anim.setRepeatCount(INFINITE);
        anim.setDuration(duration);
        anim.setInterpolator(new LinearInterpolator());
        anim.start();
        return anim;
    }

    public static AnimatorSet rotate(View view, float fromDegree, float toDegree, final AnimEndListener listener) {
        AnimatorSet set = new AnimatorSet();
        view.setPivotX(view.getMeasuredHeight() / 2);
        view.setPivotY(view.getMeasuredWidth() / 2);
        set.play(ObjectAnimator.ofFloat(view, "rotation", fromDegree, toDegree));
        set.setDuration(DEFAULT_TIME);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (listener != null) {
                    listener.animEnd();
                }
            }
        });
        set.start();
        return set;
    }


    public static AnimatorSet scale(View view, float from, float to, AnimEndListener listener) {
        return scale(view, from, to, DEFAULT_TIME, listener);
    }


    public static AnimatorSet translationY(View view, float from, float to, long time, final AnimEndListener listener) {
        AnimatorSet set = new AnimatorSet();
        set.play(ObjectAnimator.ofFloat(view, "translationY", from, to));
        set.setDuration(time);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (listener != null) {
                    listener.animEnd();
                }
            }
        });
        set.start();
        return set;
    }

    public static AnimatorSet translationYWithAlpha(View view, float from, float to, boolean isShow, long time, final AnimEndListener listener) {
        AnimatorSet set = new AnimatorSet();
        set.play(ObjectAnimator.ofFloat(view, "translationY", from, to))
                .with(ObjectAnimator.ofFloat(view, "alpha", isShow ? 0f : 1f, isShow ? 1f : 0f));
        set.setDuration(time);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (listener != null) {
                    listener.animEnd();
                }
            }
        });
        set.start();
        return set;
    }


    public static AnimatorSet translationX(View view, float from, float to, long time, final AnimEndListener listener) {
        if (view == null) return null;
        AnimatorSet set = new AnimatorSet();
        set.play(ObjectAnimator.ofFloat(view, "translationX", from, to));
        set.setDuration(time);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (listener != null) {
                    listener.animEnd();
                }
            }
        });
        set.start();
        return set;
    }

    public static AnimatorSet translationXWithAlpha(View view, float from, float to, boolean isShow, Long time, final AnimEndListener listener) {
        if (view == null) return null;
        AnimatorSet set = new AnimatorSet();
        set.play(ObjectAnimator.ofFloat(view, "translationX", from, to))
                .with(ObjectAnimator.ofFloat(view, "alpha", isShow ? 0f : 1f, isShow ? 1f : 0f));
        set.setDuration(time);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (listener != null) {
                    listener.animEnd();
                }
            }
        });
        set.start();
        return set;
    }

    public static AnimatorSet translationXWithAlpha(View view, float from, float to, float fromAlpha, float toAlpha, Long time, final AnimEndListener listener) {
        if (view == null) return null;
        AnimatorSet set = new AnimatorSet();
        set.play(ObjectAnimator.ofFloat(view, "translationX", from, to))
                .with(ObjectAnimator.ofFloat(view, "alpha", fromAlpha, toAlpha));
        set.setDuration(time);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (listener != null) {
                    listener.animEnd();
                }
            }
        });
        set.start();
        return set;
    }

    public interface AnimEndListener {
        void animEnd();
    }

    public abstract static class AnimListener {
        public void animEnd() {
        }

        public void animUpdate(ValueAnimator valueAnimator) {

        }

        public void animStart() {
        }
    }
}