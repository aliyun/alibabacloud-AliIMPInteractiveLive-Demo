package com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.aliyun.roompaas.base.IClear;
import com.aliyun.roompaas.base.util.ThreadUtil;
import com.aliyun.roompaas.base.util.Utils;
import com.aliyun.roompaas.beauty_base.BeautyCompat;
import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.view.BeautyMenuPanel;
import com.aliyun.roompaas.beauty_pro.remote.ResDownloadDelegate;
import com.aliyun.roompaas.uibase.util.AppUtil;
import com.aliyun.roompaas.uibase.util.ViewUtil;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @WARNING keep Path and Name
 * @see BeautyCompat#VIEW_PRO_CLASS_FULL_PATH
 */
public class MenuContainer extends FrameLayout implements IClear {

    private IButtonHandler mButtonHandler;

    private BeautyPanelController mBeautyPanelController;
    private BeautyMenuPanel mBeautyMenuPanel;

    private LinearLayout mContainerBeautyMenu;
    private LinearLayout mContainerScenesMenu;

    private OnClickListener mOnClickListenerProxy;
    private ProgressBar progressBar;
    private ScheduledFuture<?> queryStatusFuture;

    private boolean inited = false;

    public MenuContainer(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public MenuContainer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public MenuContainer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }


    private void initView(Context context) {
//        LayoutInflater.from(context).inflate(R.layout.beauty_bottom_line, this);
//        final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT
//        );
//        params.gravity = Gravity.BOTTOM|Gravity.CENTER;
//        setLayoutParams(params);
    }

    public void setBeautyPanelController(BeautyPanelController controller) {

        mBeautyPanelController = controller;

        initButtonHandler();
        initView();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (inited) {
            return;
        }

        inited = true;
        Runnable readyAction = new Runnable() {
            @Override
            public void run() {
                if (ResDownloadDelegate.isResReady()) {
                    ThreadUtil.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.cancel(queryStatusFuture);
                            BeautyCompat.removeSelfSafely(progressBar);
                            setBeautyPanelController(new BeautyPanelController());
                        }
                    });
                }
            }
        };

        if (ResDownloadDelegate.isResReady()) {
            readyAction.run();
        } else {
            if (progressBar == null) {
                progressBar = new ProgressBar(getContext());
                int _30dp = AppUtil.dp(30);
                LayoutParams lp = new LayoutParams(_30dp, _30dp);
                lp.bottomMargin = _30dp;
                lp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                addView(progressBar, lp);
            }

            Utils.cancel(queryStatusFuture);
            queryStatusFuture = ThreadUtil.scheduleAtFixedRate(readyAction, 2000, 2000, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // Utils.clear(this);
    }

    private void initButtonHandler() {
        mButtonHandler = new IButtonHandler() {
            @Override
            public void onEnterHome() {
                doHideAnim(mContainerScenesMenu);
                doHideAnim(mContainerBeautyMenu);
                mBeautyMenuPanel.onHideMenu();
            }

            @Override
            public void onEnterScenes() {
                doShowAnim(mContainerScenesMenu);
            }

            @Override
            public void onEnterBeauty() {
                doShowAnim(mContainerBeautyMenu);
                mBeautyMenuPanel.onShowMenu();
            }

            @Override
            public void onCaptureScreen() {
                if (mOnClickListenerProxy != null) {
                    View view = new View(MenuContainer.this.getContext());
                    mOnClickListenerProxy.onClick(view);
                }
            }

            @Override
            public void onModeChanged() {
                if (mOnClickListenerProxy != null) {
                    View view = new View(MenuContainer.this.getContext());
                    mOnClickListenerProxy.onClick(view);
                }
            }
        };
    }

    public void setOnClickListenerProxy(OnClickListener mOnClickListenerProxy) {
        this.mOnClickListenerProxy = mOnClickListenerProxy;
    }

    private void initView() {
        mBeautyMenuPanel = new BeautyMenuPanel(getContext());
        mBeautyMenuPanel.setBeautyPanelController(mBeautyPanelController);
        mContainerBeautyMenu = createMenuContainer(mBeautyMenuPanel);

        addView(mContainerBeautyMenu);

//        mScenesMenuPanel = new ScenesMenuPanel(getContext());
//        mScenesMenuPanel.setBeautyPanelController(mBeautyPanelController);
//        mContainerScenesMenu = createMenuContainer(mScenesMenuPanel);
//        mContainerScenesMenu.setBackgroundColor(getResources().getColor(R.color.menu_bg_color));
//        addView(mContainerScenesMenu);
//
//        mMainMenuPanel = new HomeMenuPanel(getContext());
//        mMainMenuPanel.setOnButtonHandler(mButtonHandler);
//        addView(mMainMenuPanel);

        mContainerBeautyMenu.setVisibility(View.VISIBLE);
//        mContainerScenesMenu.setVisibility(View.GONE);
    }

    private LinearLayout createMenuContainer(View contentPanel) {
        LinearLayout menuContainer = new LinearLayout(getContext());
        final LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.BOTTOM;

        menuContainer.setLayoutParams(params);
        menuContainer.setOrientation(LinearLayout.VERTICAL);
        menuContainer.addView(contentPanel);

//        MenuBottomLinePanel bottomPanel = new MenuBottomLinePanel(getContext());
//        bottomPanel.setOnButtonHandler(mButtonHandler);
//        menuContainer.addView(bottomPanel);

//        menuContainer.setBackgroundColor(getResources().getColor(R.color.menu_bg_color));
        return menuContainer;
    }

    public void switchShow() {
        boolean isShow = isShow();
        if (isShow) {
            doHideAnim(this);
        } else {
            doShowAnim(this);
        }
    }

    public boolean isShow() {
        return getVisibility() == View.VISIBLE;
    }

    public static void doShowAnim(View view) {
        if (view.getVisibility() == View.GONE) {
            TranslateAnimation showAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                    Animation.RELATIVE_TO_SELF, 0.0f,
                    Animation.RELATIVE_TO_SELF, 1.0f,
                    Animation.RELATIVE_TO_SELF, 0.0f);
            showAnim.setDuration(200);
            view.startAnimation(showAnim);

            view.setVisibility(View.VISIBLE);
        }
    }

    public static void doHideAnim(View view) {
        if (view.getVisibility() == View.VISIBLE) {
            TranslateAnimation hideAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                    Animation.RELATIVE_TO_SELF, 0.0f,
                    Animation.RELATIVE_TO_SELF, 0.0f,
                    Animation.RELATIVE_TO_SELF, 1.0f);
            hideAnim.setDuration(200);
            view.startAnimation(hideAnim);

            view.setVisibility(View.GONE);
        }
    }

    private void doShowAlpha(View view) {
        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(200);

        view.startAnimation(animation);
        view.setVisibility(View.VISIBLE);
    }

    private void doHideAlpha(View view) {
        Animation animation = new AlphaAnimation(1.0f, 0.0f);
        animation.setDuration(200);

        view.startAnimation(animation);
        view.setVisibility(View.GONE);
    }

    @Override
    public void clear() {
        mBeautyPanelController = null;
        mButtonHandler = null;
        mBeautyMenuPanel = null;
        progressBar = null;
        Utils.cancel(queryStatusFuture);
        ViewUtil.removeSelfSafely(mContainerBeautyMenu);
    }


    interface IButtonHandler {
        void onEnterHome();

        void onEnterScenes();

        void onEnterBeauty();

        void onCaptureScreen();

        void onModeChanged();
    }
}
