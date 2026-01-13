package com.aide.ui.re;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

public class NestedHorizontalScroll extends HorizontalScrollView {

    public NestedHorizontalScroll(Context context) {
        super(context);
    }

    public NestedHorizontalScroll(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // 当用户触摸面包屑区域时，请求父容器不要拦截事件
        // 这样就可以平滑滑动面包屑，而不会导致侧栏关闭
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
        return super.onInterceptTouchEvent(ev);
    }
}
