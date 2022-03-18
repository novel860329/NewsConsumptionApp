package com.example.accessibility_detect.questions_test.widgets_test;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;


public class NoSwipeViewPager_test extends ViewPager
{
    public NoSwipeViewPager_test(Context context) {
        super(context);
    }

    public NoSwipeViewPager_test(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false;
    }
}
