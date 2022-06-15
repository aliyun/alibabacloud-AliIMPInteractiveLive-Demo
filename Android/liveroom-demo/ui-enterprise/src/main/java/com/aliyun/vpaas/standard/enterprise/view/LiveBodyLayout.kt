package com.aliyun.vpaas.standard.enterprise.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.support.design.widget.TabLayout
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.aliyun.standard.liveroom.lib.component.BaseComponent
import com.aliyun.standard.liveroom.lib.component.ComponentHolder
import com.aliyun.standard.liveroom.lib.component.IComponent
import com.aliyun.standard.liveroom.lib.component.MultiComponentHolder
import com.aliyun.vpaas.standard.enterprise.R

/**
 * 页面主题内容 (包含横滑Tab和Tab对应面板两部分)
 *
 * @author puke
 * @version 2022/6/6
 */
class LiveBodyLayout(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs),
    MultiComponentHolder {

    companion object {
        const val ACTION_SHOW_CHAT_TAB = "ShowChatTab"
        const val ACTION_SHOW_LIVE_TAB = "ShowLiveTab"
        const val ACTION_SHOW_OTHER_TAB = "ShowOtherTab"

        enum class TabType(val text: String, val viewType: Class<out View>) {
            CHAT("互动消息", LiveChatView::class.java),
            LIVE("直播消息", LiveDetailView::class.java),
            CUSTOM1("自定义项1", LiveCustomView1::class.java),
            CUSTOM2("自定义项2", LiveCustomView2::class.java),
            CUSTOM3("自定义项3", LiveCustomView3::class.java),
        }
    }

    private val component = Component()
    private val tabLayout: TabLayout
    private val viewPager: ViewPager
    private val views = mutableListOf<View>()

    init {
        orientation = VERTICAL
        inflate(context, R.layout.ep_live_body_layout, this)
        tabLayout = findViewById(R.id.body_tab_layout)
        viewPager = findViewById(R.id.body_view_pager)

        val tabTypes = TabType.values()
        for (tabType in tabTypes) {
            tabLayout.addTab(tabLayout.newTab())
            views.add(
                tabType.viewType.getConstructor(Context::class.java, AttributeSet::class.java)
                    .newInstance(context, null)
            )
        }

        tabLayout.setupWithViewPager(viewPager)
        viewPager.adapter = PagerAdapterImpl()
        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                // 切换ViewPager时, 通过发内部事件通知到LiveBottomLayout组件
                val action = when (TabType.values()[position]) {
                    TabType.CHAT -> {
                        ACTION_SHOW_CHAT_TAB
                    }
                    TabType.LIVE -> {
                        ACTION_SHOW_LIVE_TAB
                    }
                    else -> {
                        ACTION_SHOW_OTHER_TAB
                    }
                }
                component.postEvent(action)
            }
        })
        viewPager.currentItem = 0
    }

    private inner class PagerAdapterImpl : PagerAdapter() {
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val view = views[position]
            container.addView(
                view, LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            val view = views[position]
            if (view.parent == container) {
                container.removeView(view)
            }
        }

        override fun getPageTitle(position: Int): CharSequence {
            return TabType.values()[position].text
        }

        override fun getCount(): Int {
            return views.size
        }

        override fun isViewFromObject(p0: View, p1: Any): Boolean {
            return p0 == p1
        }
    }

    private inner class Component : BaseComponent() {
        @SuppressLint("SwitchIntDef")
        override fun onActivityConfigurationChanged(newConfig: Configuration?) {
            when (newConfig?.orientation) {
                Configuration.ORIENTATION_PORTRAIT -> {
                    // 竖屏
                    visibility = VISIBLE
                }
                Configuration.ORIENTATION_LANDSCAPE -> {
                    // 横屏
                    visibility = GONE
                }
            }
        }
    }

    override fun getComponents(): List<IComponent> {
        // 此处返回多个Component对象
        val components = mutableListOf<IComponent>(component)
        views
            .filter { it is ComponentHolder }
            .forEach { components.add((it as ComponentHolder).component) }
        return components
    }
}