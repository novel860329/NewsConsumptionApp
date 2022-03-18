package com.example.accessibility_detect.questions_test.adapters_test;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

public class MyWebView_test extends WebView {
    String url = "";
    Context mContext;
    public MyWebView_test(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        // TODO Auto-generated constructor stub
    }

    public MyWebView_test(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        // TODO Auto-generated constructor stub
    }

    public MyWebView_test(Context context) {
        super(context);
        mContext = context;
        // TODO Auto-generated constructor stub
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // TODO Auto-generated method stub
//        Log.d("touchevent", "touchevent" );
//        switch (ev.getAction()) {
//            case MotionEvent.ACTION_UP:
//                Intent intent = new Intent(mContext, FullScreenWeb.class);
//                Bundle extras = new Bundle();
//                extras.putString("url", url);
//                intent.putExtras(extras);
//                Log.d("touchevent", "start activity");
//                mContext.startActivity(intent);
//                break;
//        }
        return super.onTouchEvent(ev);
    }

    public void setURL(String url){
        this.url = url;
    }
}