package com.example.accessibility_detect.questions_test;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import com.example.accessibility_detect.R;

public class FullScreenWeb_test  extends Activity {
    WebView webDisplay;
    String TAG = "Full Website";
    Button btnClose;
    TextView text;
    @SuppressLint("NewApi")

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.layout_full_web);

        Bundle extras = getIntent().getExtras();
        String url = "";
        if (extras != null) {
            url = extras.getString("url");
        }

        webDisplay = (WebView) findViewById(R.id.webDisplay);
        text = (TextView) findViewById(R.id.txt);
//        btnClose = (Button)findViewById(R.id.btnClose);

        Log.d(TAG, "After find by view");

        WebSettings settings = webDisplay.getSettings();
        settings.setJavaScriptEnabled(true);

        webDisplay.setFocusable(false);
        webDisplay.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        webDisplay.getSettings().setDomStorageEnabled(true);
        webDisplay.getSettings().setAppCacheEnabled(false);

        if (Build.VERSION.SDK_INT >= 21) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        //FOR WEBPAGE SLOW UI
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webDisplay.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            webDisplay.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        webDisplay.loadUrl(url);
        webDisplay.setWebViewClient(new WebViewClient());
    }
    protected void onDestroy(){
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if(webDisplay != null) {
            webDisplay.stopLoading();
            webDisplay.getSettings().setJavaScriptEnabled(false);
            webDisplay.clearHistory();
            webDisplay.removeAllViews();
            webDisplay.destroy();
            webDisplay = null;
        }
    }
}