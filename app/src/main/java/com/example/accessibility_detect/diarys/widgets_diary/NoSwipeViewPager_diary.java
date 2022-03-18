package com.example.accessibility_detect.diarys.widgets_diary;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;


public class NoSwipeViewPager_diary extends ViewPager
{
    public NoSwipeViewPager_diary(Context context) {
        super(context);
    }

    public NoSwipeViewPager_diary(Context context, AttributeSet attrs) {
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
