package com.aide.ui.re;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

public class CustomDrawerLayout extends DrawerLayout {
    private RecyclerView recyclerView;

    public CustomDrawerLayout(Context context) {
        super(context);
    }

    public CustomDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomDrawerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * 设置关联的 RecyclerView（面包屑）
     */
    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // 如果 RecyclerView 存在且可水平滚动，不拦截事件（让 RecyclerView 处理滑动）
        if (recyclerView != null && recyclerView.canScrollHorizontally(1)) {
            return false;
        }
        // 否则，由 DrawerLayout 正常处理事件
        return super.onInterceptTouchEvent(ev);
    }
}
