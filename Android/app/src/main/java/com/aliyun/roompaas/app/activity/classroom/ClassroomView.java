package com.aliyun.roompaas.app.activity.classroom;

import android.content.res.Configuration;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aliyun.roompaas.app.R;
import com.aliyun.roompaas.app.activity.classroom.panel.ChatView;
import com.aliyun.roompaas.app.activity.classroom.panel.DocumentView;
import com.aliyun.roompaas.app.activity.classroom.panel.StudentView;
import com.aliyun.roompaas.app.helper.KeyboardHelper;
import com.aliyun.roompaas.app.util.ScreenUtil;
import com.aliyun.roompaas.base.util.ViewUtil;

/**
 * @author puke
 * @version 2021/5/24
 */
public class ClassroomView {

    private enum PanelType {
        STUDENT("学员"),
        DOCUMENT("文档"),
        CHAT("讨论");

        final String text;

        PanelType(String text) {
            this.text = text;
        }
    }

    final ClassroomActivity activity;
    final TextView title;
    final ViewGroup roadRenderContainer;
    final ViewGroup rtcRenderContainer;
    final View startClass;
    final RecyclerView studentList;
    final LinearLayout tabLayout;
    final ViewPager panel;
    final View bottomLayout;
    final EditText input;
    final Button send;
    final KeyboardHelper keyboardHelper;
    final RecyclerView functionList;

    final StudentView studentView;
    final DocumentView documentView;
    final ChatView chatView;

    public ClassroomView(final ClassroomActivity activity) {
        this.activity = activity;
        findView(R.id.classroom_back).setOnClickListener(v -> activity.finish());
        title = findView(R.id.classroom_title);
        roadRenderContainer = findView(R.id.classroom_road_render_container);
        rtcRenderContainer = roadRenderContainer;
        startClass = findView(R.id.classroom_start_class);
        ViewUtil.bindClickActionWithClickCheck(startClass, activity::onStartClass);
        studentList = findView(R.id.studentRTCViewList);
        tabLayout = findView(R.id.classroom_tab);
        panel = findView(R.id.classroom_panel);
        bottomLayout = findView(R.id.classroom_bottom_layout);
        input = findView(R.id.classroom_input);
        send = findView(R.id.classroom_send);
        functionList = findView(R.id.function_list_recycler_view);

        // 面板视图
        studentView = new StudentView(activity);
        documentView = new DocumentView(activity);
        chatView = new ChatView(activity);

        // 设置渲染容器背景
        roadRenderContainer.setBackgroundColor(Color.parseColor("#33000000"));

        // 添加Tab项
        PanelType[] panelTypes = PanelType.values();
        for (int i = 0; i < panelTypes.length; i++) {
            PanelType panelType = panelTypes[i];
            TextView textView = new TextView(activity);
            textView.setBackgroundResource(R.drawable.bg_classroom_tab);
            textView.setSelected(i == 0);
            textView.setText(panelType.text);
            textView.setTextColor(Color.parseColor("#333333"));
            textView.setGravity(Gravity.CENTER);
            int finalI = i;
            textView.setOnClickListener(v -> {
                // 处理点击Tab后, 联动处理当前的ViewPager
                panel.setCurrentItem(finalI);
            });
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams.weight = 1;
            tabLayout.addView(textView, layoutParams);
        }

        // 添加ViewPager-Tab联动
        panel.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                setCurrentTab(position);
            }
        });

        // 设置相邻实例上限 (为了不销毁实例)
        panel.setOffscreenPageLimit(panelTypes.length);
        // 设置多面板
        panel.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return panelTypes.length;
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == object;
            }

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                final View view;
                PanelType panelType = panelTypes[position];
                switch (panelType) {
                    case STUDENT:
                        view = studentView;
                        break;
                    case DOCUMENT:
                        view = documentView;
                        break;
                    case CHAT:
                        view = chatView;
                        break;
                    default:
                        throw new RuntimeException("Unsupported panel type: " + panelType);
                }
                container.addView(view, new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                ));
                return view;
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                super.destroyItem(container, position, object);
                container.removeView((View) object);
            }
        });

        input.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                activity.onSend(input.getText().toString().trim());
                return true;
            }
            return false;
        });
        send.setOnClickListener(v -> activity.onSend(input.getText().toString().trim()));

        keyboardHelper = new KeyboardHelper(activity);
        keyboardHelper.setOnSoftKeyBoardChangeListener(new KeyboardHelper.OnSoftKeyBoardChangeListener() {
            @Override
            public void keyBoardShow(int height) {
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) bottomLayout.getLayoutParams();
                layoutParams.bottomMargin = 0;
                bottomLayout.setLayoutParams(layoutParams);
            }

            @Override
            public void keyBoardHide(int height) {
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) bottomLayout.getLayoutParams();
                layoutParams.bottomMargin = 0;
                bottomLayout.setLayoutParams(layoutParams);
            }
        });

        initStudentList();
        initFunctionList();
    }

    // 初始化学生列表
    void initStudentList() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        studentList.setLayoutManager(linearLayoutManager);
    }

    void initFunctionList() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        functionList.setLayoutManager(linearLayoutManager);
    }

    void clearInput() {
        input.setText(null);
    }

    void shrinkKeyboard() {
        keyboardHelper.shrinkByEditText(input);
    }

    /**
     * 切换旁路流和rtc流的渲染视图
     *
     * @param showRoadRender true: 开启旁路流; false: 关闭旁路流;
     */
    void setRenderVisible(boolean showRoadRender) {
        if (showRoadRender) {
            roadRenderContainer.setVisibility(View.VISIBLE);
            rtcRenderContainer.setVisibility(View.GONE);
            rtcRenderContainer.removeAllViews();
        } else {
            roadRenderContainer.setVisibility(View.GONE);
            roadRenderContainer.removeAllViews();
            rtcRenderContainer.setVisibility(View.VISIBLE);
        }
    }

    private void setCurrentTab(int selectedIndex) {
        for (int i = 0; i < tabLayout.getChildCount(); i++) {
            View child = tabLayout.getChildAt(i);
            child.setSelected(i == selectedIndex);
        }
    }

    private <V extends View> V findView(int idRes) {
        return activity.findViewById(idRes);
    }

    public void setOrientation(int orientation) {
        tabLayout.setVisibility(orientation == Configuration.ORIENTATION_LANDSCAPE ? View.GONE : View.VISIBLE);
        panel.setVisibility(orientation == Configuration.ORIENTATION_LANDSCAPE ? View.GONE : View.VISIBLE);
        bottomLayout.setVisibility(orientation == Configuration.ORIENTATION_LANDSCAPE ? View.GONE : View.VISIBLE);
        findView(R.id.classroom_up_layout).setLayoutParams(orientation == Configuration.ORIENTATION_LANDSCAPE ?
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT) :
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) ScreenUtil.pxFromDp(activity.getApplicationContext(), 250)));
    }

    public void updateTitle(String str){
        title.setText(str);
    }
}
