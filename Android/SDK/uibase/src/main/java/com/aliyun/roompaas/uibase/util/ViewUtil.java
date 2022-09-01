package com.aliyun.roompaas.uibase.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.Layout;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.aliyun.roompaas.base.log.Logger;
import com.aliyun.roompaas.base.util.Check;
import com.aliyun.roompaas.base.util.ThreadUtil;
import com.aliyun.roompaas.base.util.Utils;

import java.lang.ref.Reference;


/**
 * Created by KyleCe on 2021/5/24
 */
public class ViewUtil {
    public static final String TAG = "ViewUtil";
    private static final int INVALID = -1;
    private static final int TO_ADD_INDEX_UNSPECIFIED = INVALID;

    public static void adjustViewSizeViaPoint(View v, Point target, boolean adjustW, boolean adjustH) {
        ViewGroup.LayoutParams lp;
        if (v == null || (lp = v.getLayoutParams()) == null || (!adjustW && !adjustH)) {
            Logger.i(TAG, "adjustRenderSize: end: invalid param");
            return;
        }

        boolean adjusted = false;
        if (adjustW && lp.width != target.x) {
            lp.width = target.x;
            adjusted = true;
        }
        if (adjustH && lp.height != target.y) {
            lp.height = target.y;
            adjusted = true;
        }
        if (adjusted) {
            v.setLayoutParams(lp);
        }
    }

    public static void addChildMatchParentSafely(@Nullable ViewGroup vg, @Nullable View child) {
        addChildMatchParentSafely(false, vg, child);
    }

    public static void addChildMatchParentSafely(boolean setParentVisible, @Nullable ViewGroup vg, @Nullable View child) {
        addChildMatchParentSafely(setParentVisible, vg, TO_ADD_INDEX_UNSPECIFIED, child);
    }

    public static void addChildMatchParentSafely(boolean setParentVisible, @Nullable ViewGroup vg, int index, @Nullable View child) {
        addChildMatchParentSafely(setParentVisible, vg, index, child, null);
    }

    public static void addChildMatchParentSafely(@Nullable ViewGroup vg, int index, @Nullable View child) {
        addChildMatchParentSafely(vg, index, child, null);
    }

    public static void addChildMatchParentSafely(@Nullable ViewGroup vg, @Nullable View child, @Nullable ViewGroup.LayoutParams lp) {
        addChildMatchParentSafely(vg, TO_ADD_INDEX_UNSPECIFIED, child, lp);
    }

    public static void addChildMatchParentSafely(@Nullable ViewGroup vg, int index, @Nullable View child, @Nullable ViewGroup.LayoutParams lp) {
        addChildMatchParentSafely(false, vg, index, child, lp);
    }

    public static void addChildMatchParentSafely(boolean setParentVisible, @Nullable ViewGroup vg, int index, @Nullable View child, @Nullable ViewGroup.LayoutParams lp) {
        if (vg == null || child == null) {
            return;
        }

        if (setParentVisible) {
            ViewUtil.setVisible(vg);
        }

        if (child.getParent() instanceof ViewGroup) {
            ViewGroup originParent = (ViewGroup) child.getParent();
            if (!originParent.equals(vg)) {
                removeChildFromParent(originParent, child);
            } else {
                // already added to parent
                return;
            }
        }

        if (index != TO_ADD_INDEX_UNSPECIFIED) {
            vg.addView(child, index, lp != null ? lp : new ViewGroup.LayoutParams(-1, -1));
        } else {
            vg.addView(child, lp != null ? lp : new ViewGroup.LayoutParams(-1, -1));
        }
    }

    public static void addChildIfVital(@Nullable ViewGroup vg, @Nullable View child) {
        addChildMatchParentSafely(vg, child, null);
    }

    public static void addChildIfVital(@Nullable ViewGroup vg, @Nullable View child, @Nullable ViewGroup.LayoutParams lp) {
        if (vg == null || child == null) {
            return;
        }

        if (child.getParent() instanceof ViewGroup) {
            ViewGroup originParent = (ViewGroup) child.getParent();
            if (!originParent.equals(vg)) {
                removeChildFromParent(originParent, child);
            } else {
                // already added to parent
                return;
            }
        }

        if (lp != null) {
            vg.addView(child, lp);
        } else {
            vg.addView(child);
        }
    }

    public static void reAddChildByForce(@Nullable ViewGroup vg, @Nullable View child) {
        reAddChildByForce(vg, child, null);
    }

    @SuppressWarnings("all")
    public static void reAddChildByForce(@Nullable ViewGroup vg, @Nullable View child, @Nullable ViewGroup.LayoutParams lp) {
        if (Utils.anyNull(vg, child)) {
            return;
        }
        ViewUtil.removeSelfSafely(child);
        if (lp != null) {
            vg.addView(child, lp);
        } else {
            vg.addView(child);
        }
    }

    public static void requestParentLayout(View info) {
        if (info != null && info.getParent() instanceof ViewGroup) {
            ((ViewGroup) info.getParent()).requestLayout();
        }
    }

    /**
     * require the views to have the same type of ViewParent to make sure the LayoutParams take effects on both views
     *
     * @param big  to switch
     * @param mini to switch
     */
    public static void switchViewPosition(@Nullable final View big, @Nullable final View mini, boolean centerWhiteBoardSwitched) {
        ViewParent bigVP, miniVP;
        if (big == null || mini == null || !((bigVP = big.getParent()) instanceof ViewGroup)
                || !((miniVP = mini.getParent()) instanceof ViewGroup)) {
            Logger.i(TAG, "switchViewPosition end: invalid param:first=" + big + ",second=" + mini);
            return;
        }

        final ViewGroup bigParent = (ViewGroup) bigVP;
        final ViewGroup miniParent = (ViewGroup) miniVP;

        final int bigIndex = findChildIndex(bigParent, big);
        final int miniIndex = findChildIndex(miniParent, mini);
        final ViewGroup.LayoutParams bigLP = big.getLayoutParams();
        final ViewGroup.LayoutParams miniLP = mini.getLayoutParams();
        if (bigIndex == -1 || miniIndex == -1 || bigLP == null || miniLP == null) {
            Logger.i(TAG, "switchViewPosition end: invalid param:firstIndex=" + bigIndex + ",centerBigIndex=" + miniIndex);
            return;
        }

        bigParent.removeViewAt(bigIndex);
        miniParent.removeViewAt(miniIndex);

        bigParent.addView(mini, bigIndex, bigLP);
        miniParent.addView(big, miniIndex, miniLP);
    }

    public static int findChildIndex(@Nullable ViewGroup vg, @Nullable View child) {
        int count;
        if (vg == null || child == null || (count = vg.getChildCount()) == 0 || !vg.equals(child.getParent())) {
            return INVALID;
        }

        for (int i = 0; i < count; i++) {
            View v = vg.getChildAt(i);
            if (child.equals(v)) {
                return i;
            }
        }

        return INVALID;
    }

    public static void removeAll(@Nullable ViewGroup vg) {
        int count;
        if (vg == null || (count = vg.getChildCount()) == 0) {
            return;
        }

        for (int i = 0; i < count; i++) {
            View self = vg.getChildAt(i);
            if (self == null) {
                continue;
            }
            removeChildFromParent(vg, self);
        }
    }

    public static void removeSelfSafely(@Nullable View self) {
        ViewParent vp;
        if (self == null || !((vp = self.getParent()) instanceof ViewGroup)) {
            return;
        }

        removeChildFromParent((ViewGroup) vp, self);
    }

    public static void removeChildFromParent(@NonNull ViewGroup vg, @NonNull View self) {
        self.clearAnimation();
        vg.removeView(self);
    }

    @Nullable
    public static View findFirstSurfaceViewAtLevel0(@Nullable ViewGroup vg) {
        int size;
        if (vg == null || (size = vg.getChildCount()) == 0) {
            return null;
        }

        for (int i = 0; i < size; i++) {
            View child = vg.getChildAt(i);
            if (child instanceof SurfaceView) {
                return child;
            }
        }
        return null;
    }

    public static void setTextWithCursorEnd(EditText et, CharSequence cs) {
        if (et == null || TextUtils.isEmpty(cs)) {
            return;
        }

        et.setText(cs);
        bringCursorToEnd(et);
    }

    public static void bringCursorToEnd(EditText et) {
        if (et == null || TextUtils.isEmpty(et.getText())) {
            return;
        }
        et.setSelection(et.getText().length());
    }

    public static boolean isTextViewEllipsized(@Nullable TextView tv) {
        if (tv == null) {
            return false;
        }

        Layout l = tv.getLayout();
        int lines;
        if (l != null && (lines = l.getLineCount()) > 0) {
            return l.getEllipsisCount(lines - 1) > 0;
        }
        return false;
    }

    public static <V extends View> void applyText(@Nullable Reference<V> viewRef, @StringRes int id) {
        applyText(Utils.getRef(viewRef), id);
    }

    public static void applyText(@Nullable View view, @StringRes int id) {
        applyText(view, view != null && view.getContext() != null ? view.getContext().getResources().getString(id) : null);
    }

    public static <V extends View> void applyText(@Nullable Reference<V> viewRef, @Nullable CharSequence cs) {
        applyText(Utils.getRef(viewRef), cs);
    }

    public static void applyText(@Nullable View view, @Nullable CharSequence cs) {
        if (!(view instanceof TextView) || cs == null) {
            return;
        }

        ((TextView) view).setText(cs);
    }

    public static void applyHint(@Nullable View view, @StringRes int id) {
        applyHint(view, view != null && view.getContext() != null ? view.getContext().getResources().getString(id) : null);
    }

    public static void applyHint(@Nullable View view, @Nullable CharSequence cs) {
        if (!(view instanceof TextView) || cs == null) {
            return;
        }

        ((TextView) view).setHint(cs);
    }

    public static void applyTextColor(@Nullable View view, @ColorRes int id) {
        applyTextColor(view, id, null);
    }

    public static void applyTextColor(@Nullable View view, @ColorRes int id, @Nullable Context context) {
        Context ctx = context != null ? context : view == null ? null : view.getContext();
        if (!(view instanceof TextView) || id == 0 || ctx == null) {
            return;
        }

        ((TextView) view).setTextColor(ctx.getResources().getColor(id));
    }

    public static void applyImageResource(@Nullable View view, @DrawableRes int drawableRes) {
        if (!(view instanceof ImageView) || drawableRes == 0) {
            return;
        }

        ((ImageView) view).setImageResource(drawableRes);
    }

    public static void applyDrawable(@Nullable View view, Drawable drawable) {
        if (!(view instanceof ImageView) || drawable == null) {
            return;
        }

        ((ImageView) view).setImageDrawable(drawable);
    }

    public static void applyBitmap(@Nullable View view, Bitmap bitmap) {
        if (!(view instanceof ImageView) || bitmap == null || bitmap.isRecycled()) {
            return;
        }

        ((ImageView) view).setImageBitmap(bitmap);
    }

    public static void applySelected(boolean selected, View... vs) {
        if (Utils.isEmpty(vs)) {
            return;
        }

        for (View v : vs) {
            selectOrUnselectView(v, selected);
        }
    }

    private static void selectOrUnselectView(@Nullable View v, boolean selected) {
        if (v == null) {
            return;
        }

        if (v.isSelected() && !selected) {
            v.setSelected(false);
        } else if (!v.isSelected() && selected) {
            v.setSelected(true);
        }
    }


    public static void switchEnableAndClickable(boolean true4Positive, View... vs) {
        if (true4Positive) {
            enableAndClickable(vs);
        } else {
            disableAndUnClickable(vs);
        }
    }

    public static void enableAndClickable(View... vs) {
        ViewUtil.enableView(vs);
        ViewUtil.clickableOrNotView(true, vs);
    }

    public static void disableAndUnClickable(View... vs) {
        ViewUtil.disableView(vs);
        ViewUtil.clickableOrNotView(false, vs);
    }

    public static void enableView(View... vs) {
        enableOrDisableView(true, vs);
    }

    public static void disableView(View... vs) {
        enableOrDisableView(false, vs);
    }

    private static void enableOrDisableView(boolean enable, View... vs) {
        if (Utils.isEmpty(vs)) {
            return;
        }

        for (View v : vs) {
            enableOrDisableView(v, enable);
        }
    }

    private static void enableOrDisableView(@Nullable View v, boolean enable) {
        if (v == null) {
            return;
        }

        if (v.isEnabled() && !enable) {
            v.setEnabled(false);
        } else if (!v.isEnabled() && enable) {
            v.setEnabled(true);
        }
    }

    public static void clickableView(View... vs) {
        clickableOrNotView(true, vs);
    }

    public static void notClickableView(View... vs) {
        clickableOrNotView(false, vs);
    }

    public static void clickableOrNotView(boolean clickable, View... vs) {
        if (Utils.isEmpty(vs)) {
            return;
        }

        for (View v : vs) {
            clickableOrNotView(v, clickable);
        }
    }

    private static void clickableOrNotView(@Nullable View v, boolean clickable) {
        if (v == null) {
            return;
        }

        if (v.isClickable() && !clickable) {
            v.setClickable(false);
        } else if (!v.isClickable() && clickable) {
            v.setClickable(true);
        }
        v.setFocusable(clickable);
    }

    public static void performClick(@Nullable View v){
        if (v != null) {
            v.performClick();
        }
    }

    public static void applyAlpha(float alpha, View... vs) {
        if (Utils.isEmpty(vs)) {
            return;
        }

        for (View v : vs) {
            if (v == null || v.getAlpha() == alpha) {
                continue;
            }

            v.setAlpha(alpha);
        }
    }

    public static boolean isVisible(View view) {
        return view != null && view.getVisibility() == View.VISIBLE;
    }

    public static boolean isNotVisible(View view) {
        return view != null && view.getVisibility() != View.VISIBLE;
    }

    public static boolean isGone(View view) {
        return view != null && view.getVisibility() == View.GONE;
    }

    public static boolean isInvisible(View view) {
        return view != null && view.getVisibility() == View.INVISIBLE;
    }

    public static void setVisibleOrGone(boolean true4Visible, View... vs) {
        switchVisibilityIfNecessary(true4Visible, vs);
    }

    public static void toggleVisibleGone(boolean firstVisible, View vis, View gone) {
        switchVisibilityIfNecessary(firstVisible, vis);
        switchVisibilityIfNecessary(!firstVisible, gone);
    }

    public static void setGone(View... vs) {
        setGone(true, vs);
    }

    public static void setGone(boolean condition, View... vs) {
        setVisibilityIfNecessary(condition, View.GONE, vs);
    }

    public static void setVisible(View... vs) {
        setVisible(true, vs);
    }

    public static void setVisible(boolean condition, View... vs) {
        setVisibilityIfNecessary(condition, View.VISIBLE, vs);
    }

    public static void setInvisible(View... view) {
        setInvisible(true, view);
    }

    public static void setInvisible(boolean condition, View... vs) {
        setVisibilityIfNecessary(condition, View.INVISIBLE, vs);
    }

    public static void switchVisibilityIfNecessary(boolean true4Visible, View... vs) {
        setVisibilityIfNecessary(true, true4Visible ? View.VISIBLE : View.GONE, vs);
    }

    public static void setVisibilityIfNecessary(@Nullable View view, int visibility) {
        setVisibilityIfNecessary(true, view, visibility);
    }

    public static void setVisibilityIfNecessary(boolean condition, @Nullable View view, int visibility) {
        setVisibilityIfNecessary(condition, visibility, view);
    }

    public static void setVisibilityIfNecessary(int visibility, @Nullable View... vs) {
        setVisibilityIfNecessary(true, visibility, vs);
    }

    public static void setVisibilityIfNecessary(boolean condition, final int visibility, @Nullable View... vs) {
        if (!condition || !isVisibilityValid(visibility) || vs == null || vs.length == 0) {
            return;
        }

        for (final View v : vs) {
            if (v != null && v.getVisibility() != visibility) {
                if (ThreadUtil.isMainThread()) {
                    v.setVisibility(visibility);
                } else {
                    ThreadUtil.postToUiThread(new Runnable() {
                        @Override
                        public void run() {
                            v.setVisibility(visibility);
                        }
                    });
                }
            }
        }
    }

    public static boolean isVisibilityValid(int vis) {
        return vis == View.GONE || vis == View.INVISIBLE || vis == View.VISIBLE;
    }

    public static void applyVisibleOrGoneViaCondition(boolean visibleOrGone, View... vs) {
        if (vs == null || vs.length == 0) {
            return;
        }

        for (View v : vs) {
            setVisibilityIfNecessary(v, visibleOrGone ? View.VISIBLE : View.GONE);
        }
    }

    public static void setEnable(boolean enabled, View... vs) {
        if (Utils.isEmpty(vs)) {
            return;
        }

        for (View v : vs) {
            if (v != null && (v.isEnabled() ^ enabled)) {
                v.setEnabled(enabled);
            }
        }
    }

    public static void bindClickActionWithClickCheck(final Runnable pureAction, View... vs) {
        if (vs == null || vs.length == 0) {
            return;
        }

        bindClickActionWithClickCheck(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.run(pureAction);
            }
        }, vs);
    }

    public static void bindClickActionWithClickCheck(@Nullable View view, final Runnable pureAction) {
        if (view == null) {
            return;
        }
        bindClickActionWithClickCheck(view, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.run(pureAction);
            }
        });
    }

    public static void bindClickActionWithClickCheck(@Nullable final View.OnClickListener clickListener, View... vs) {
        if (Utils.isEmpty(vs)) {
            return;
        }
        for (View view : vs) {
            bindClickActionWithClickCheck(view, clickListener);
        }
    }

    public static void bindClickActionWithClickCheck(@Nullable View view, @Nullable final View.OnClickListener clickListener) {
        if (view == null) {
            return;
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Check.checkClickEvent()) {
                    return;
                }
                if (clickListener != null) {
                    clickListener.onClick(v);
                }
            }
        });
    }

    public static void bindClickActionWithClickCheck(@Nullable View view, final long clickInterval,
                                                     @Nullable final View.OnClickListener clickListener) {
        if (view == null) {
            return;
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Check.checkClickEvent(clickInterval)) {
                    return;
                }
                if (clickListener != null) {
                    clickListener.onClick(v);
                }
            }
        });
    }

    public static void bindClickListener(@Nullable View.OnClickListener l, @Nullable View findRoot, int... ids) {
        if (l == null || findRoot == null || ids == null || ids.length == 0) {
            return;
        }

        View v;
        for (int id : ids) {
            if (id <= 0 || (v = findRoot.findViewById(id)) == null) {
                continue;
            }
            v.setOnClickListener(l);
        }
    }

    public static void addKeyEventEnterResponse(final @Nullable Runnable action, View... vs) {
        if (action == null || Utils.isEmpty(vs)) {
            return;
        }

        for (View view : vs) {
            if (view == null) {
                continue;
            }
            view.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                            (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        action.run();
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    public static int removeAndDestroyWebView(View view) {
        WebView webView = null;
        if (view instanceof WebView) {
            webView = (WebView) view;
        }

        if (webView == null && (view instanceof ViewGroup)) {
            webView = findDeepFirstWebView((ViewGroup) view);
        }

        if (webView == null) {
            return -1;
        }

        try {
            removeSelfSafely(webView);

            webView.clearHistory();

            // NOTE: clears RAM cache, if you pass true, it will also clear the disk cache.
            // Probably not a great idea to pass true if you have other WebViews still alive.
            webView.clearCache(true);

            // Loading a blank page is optional, but will ensure that the WebView isn't doing anything when you destroy it.
            webView.loadUrl("about:blank");

            webView.onPause();
            webView.removeAllViews();
            webView.destroyDrawingCache();

            // NOTE: This pauses JavaScript execution for ALL WebViews,
            // do not use if you have other WebViews still alive.
            // If you create another WebView after calling this,
            // make sure to call mWebView.resumeTimers().
            webView.pauseTimers();

            // NOTE: This can occasionally cause a segfault below API 17 (4.2)
            webView.destroy();
            return 1;
        } catch (Throwable ignore) {
            return -1;
        }
    }

    public static WebView findDeepFirstWebView(View v) {
        if ((!(v instanceof ViewGroup))) {
            return null;
        }
        ViewGroup vg = (ViewGroup) v;
        if (vg.getChildCount() == 0) {
            return null;
        }

        for (int i = 0, len = vg.getChildCount(); i < len; i++) {
            View child = vg.getChildAt(i);
            if (child instanceof WebView) {
                return (WebView) child;
            }

            if (child instanceof ViewGroup) {
                return findDeepFirstWebView(child);
            }
        }

        return null;
    }

    public static<V extends View> V findDeepFirst(View v, Class<?> clz) {
        if ((!(v instanceof ViewGroup))) {
            return null;
        }
        ViewGroup vg = (ViewGroup) v;
        if (vg.getChildCount() == 0) {
            return null;
        }

        for (int i = 0, len = vg.getChildCount(); i < len; i++) {
            View child = vg.getChildAt(i);
            if (child.getClass() == clz) {
                return (V) child;
            }

            if (child instanceof ViewGroup) {
                return findDeepFirst(child, clz);
            }
        }

        return null;
    }

    public static void reload(@Nullable final WebView wv) {
        if (wv != null) {
            ThreadUtil.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    wv.reload();
                }
            });
        }
    }

    public static void resumeWebViewTimers(@Nullable final WebView wv) {
        if (wv != null) {
            ThreadUtil.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    wv.resumeTimers();
                }
            });
        }
    }

    public static void pauseWebViewTimers(@Nullable final WebView wv) {
        if (wv != null) {
            ThreadUtil.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    wv.pauseTimers();
                }
            });
        }
    }

    public static void changeTopPadding(View v, int alter) {
        changePadding(v, 0, alter, 0, 0);
    }

    public static void changePadding(View v, final int alterL, final int alterT, final int alterR, final int alterB) {
        if (v == null) {
            return;
        }
        v.setPadding(v.getPaddingLeft() + alterL, v.getPaddingTop() + alterT,
                v.getPaddingRight() + alterR, v.getPaddingBottom() + alterB);
    }

    public static void changeTopMargin(final View v, final int alter) {
        changeMargin(v, 0, alter, 0, 0);
    }

    public static void changeTopMarginAbsolute(final View v, final int absolute) {
        changeMarginAbsolute(v, -1, absolute, -1, -1);
    }

    public static void changeBottomMargin(final View v, final int alter) {
        changeMargin(v, 0, 0, 0, alter);
    }

    public static void changeBottomMarginAbsolute(final View v, final int absolute) {
        changeMarginAbsolute(v, -1, -1, -1, absolute);
    }

    public static void changeMargin(final View v, final int alterL, final int alterT, final int alterR, final int alterB) {
        final ViewGroup.LayoutParams lp;
        if (v != null && (lp = v.getLayoutParams()) instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;
            mlp.setMargins(mlp.leftMargin + alterL, mlp.topMargin + alterT
                    , mlp.rightMargin + alterR, mlp.bottomMargin + alterB);
        }
    }

    /**
     * @param v         target
     * @param absoluteL -1 to ignore
     * @param absoluteT -1 to ignore
     * @param absoluteR -1 to ignore
     * @param absoluteB -1 to ignore
     */
    public static void changeMarginAbsolute(final View v, final int absoluteL, final int absoluteT, final int absoluteR, final int absoluteB) {
        final ViewGroup.LayoutParams lp;
        if (v != null && (lp = v.getLayoutParams()) instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;
            mlp.setMargins(absoluteL != -1 ? absoluteL : mlp.leftMargin, absoluteT != -1 ? absoluteT : mlp.topMargin
                    , absoluteR != -1 ? absoluteR : mlp.rightMargin, absoluteB != -1 ? absoluteB : mlp.bottomMargin);
        }
    }

    public static void tackActWithLayoutCheck(@Nullable View v, @Nullable Runnable action) {
        if (v == null || action == null) {
            return;
        }

        if (v.getWidth() != 0) {
            action.run();
        } else {
            addOnGlobalLayoutListener(v, action);
        }
    }

    public static void addOnGlobalLayoutListener(@Nullable View v, @Nullable Runnable action) {
        addOnGlobalLayoutListener(v, action, true);
    }

    public static void addOnGlobalLayoutListener(@Nullable final View v, @Nullable final Runnable action, final boolean autoRemoveRef) {
        if (v == null) {
            return;
        }
        v.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Utils.run(action);

                if (autoRemoveRef) {
                    v.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });
    }
}
