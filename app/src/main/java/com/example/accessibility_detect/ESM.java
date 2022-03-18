package com.example.accessibility_detect;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

//import com.crashlytics.android.Crashlytics;
//
//import io.fabric.sdk.android.Fabric;
import labelingStudy.nctu.minuku.Utilities.CSVHelper;

public class ESM extends Activity {
    private String TAG = "ESM Survey";
    WebView web_view;
    private Button esm_button;
    private Button diary_button;
    private SharedPreferences pref;
    protected void onCreate(final Bundle savedInstanceState) {
        Log.d(TAG, "Oncreate");
        super.onCreate(null);
//        Fabric.with(this, new Crashlytics());
        //isESM = true;
        setContentView(R.layout.questionaire_main);

        pref = getSharedPreferences("test",MODE_PRIVATE);
        processExtraData();
//        Intent intent = getIntent();
//        isDiary = intent.getBooleanExtra("Diary", false);
//
//        web_view = (WebView) findViewById(R.id.QuestionnaireWeb);
//        WebSettings settings = web_view.getSettings();
//        settings.setJavaScriptEnabled(true);
//        settings.setDomStorageEnabled(true);
//        settings.setAppCacheEnabled(false);
//        web_view.setWebViewClient(new WebViewClient()); //不調用系統瀏覽器
//
//        String filepath = pref.getString("FilePath","NA");
//        Log.d(TAG, "FilePath: " + filepath);
//
//        String UserID = pref.getString("UserNum","NA");
//        Log.d(TAG, "UserId: " + UserID);
//
//        String DeviceID = pref.getString("UserID","NA");
//        Log.d(TAG, "DeviceId: " + DeviceID);
//
//        int EsmClick = pref.getInt("Esm_click", 0);
//        EsmClick++;
//        pref.edit().putInt("Esm_click", EsmClick).apply();
//
//        int EsmNum = pref.getInt("Esm_Num", 0);
//        Log.d(TAG, "Esm number: " + EsmNum);
//        Log.d(TAG, "Diary? " + isDiary);
//        if(isDiary){//dairy
//            Log.d(TAG, "This is diary");
//
//            web_view.loadUrl("https://nctucommunication.qualtrics.com/jfe/form/SV_eRnz32OpfLkGJAF?" +
//                    "UserID=" + UserID + "&DeviceID=" + DeviceID + "&ChooseImage=" + filepath);
//            Log.d(TAG, "nctucommunication.qualtrics.com/jfe/form/SV_6zENhBtDLeXFLlH?" +
//                    "UserID=" + UserID + "&DeviceID=" + DeviceID + "&ChooseImage=" + filepath);
//        }else{
//            web_view.loadUrl("https://nctucommunication.qualtrics.com/jfe/form/SV_9zbzscrwlJNxLPD?" +
//                    "UserID=" + UserID + "&DeviceID=" + DeviceID + "&ChooseImage=" + filepath);
//            Log.d(TAG, "https://nctucommunication.qualtrics.com/jfe/form/SV_8ogL69pgbhFKwHX?" +
//                    "UserID=" + UserID + "&DeviceID=" + DeviceID + "&ChooseImage=" + filepath );
//        }
//        //this.finish();
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && web_view.canGoBack()) {
            web_view.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    protected void onStart()
    {
        super.onStart();
        Log.d(TAG, "onStart");
    }
    protected void onResume()
    {
        super.onResume();
        Log.d(TAG, "onResume");
    }
    protected void onPause()
    {
        super.onPause();
        Log.d(TAG, "onPause");
    }
    protected void onStop()
    {
        super.onStop();
        Log.d(TAG, "onStop");
    }
    public void onRestart() {
        super.onRestart();

        boolean e = getSharedPreferences("test",MODE_PRIVATE).getBoolean("NewDiary", false);
        Log.d(TAG, "New Diary: " + e);
        if(e) {
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }
    }

    protected void onDestroy(){
        super.onDestroy();
        if(web_view != null) {
            web_view.stopLoading();
            web_view.getSettings().setJavaScriptEnabled(false);
            web_view.clearHistory();
            web_view.removeAllViews();
            web_view.destroy();
            web_view = null;
        }
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);//must store the new intent unless getIntent() will return the old one
        processExtraData();
    }
    private void processExtraData(){
        Intent intent = getIntent();

        web_view = (WebView) findViewById(R.id.QuestionnaireWeb);
        WebSettings settings = web_view.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAppCacheEnabled(false);
        web_view.setWebViewClient(new WebViewClient()); //不調用系統瀏覽器

        String filepath = intent.getStringExtra("FilePath");
        //String filepath = pref.getString("FilePath","NA");
        Log.d(TAG, "FilePath: " + filepath);

        String UserID = pref.getString("UserNum","NA");
        Log.d(TAG, "UserId: " + UserID);

        String DeviceID = pref.getString("UserID","NA");
        Log.d(TAG, "DeviceId: " + DeviceID);

        {//dairy
            int DiaryClick = pref.getInt("Diary_click", 0);
//            DiaryClick++;
//            pref.edit().putInt("Diary_click", DiaryClick).apply();
            Log.d(TAG, "Diary number: " + DiaryClick);
            CSVHelper.storeToCSV("ESM.csv", "FilePath: " + filepath + "\n" + "UserId: " + UserID +
                    "\n" + "Esm number: " + DiaryClick + "Is Diary: true");
            Log.d(TAG, "This is diary");
            getSharedPreferences("test",MODE_PRIVATE).edit().putBoolean("NewDiary", false).apply();
            web_view.loadUrl("https://nctucommunication.qualtrics.com/jfe/form/SV_3CNDfQLcLvC3BSl?" +
                    "UserID=" + UserID + "&DeviceID=" + DeviceID + "&ChooseImage=" + filepath);
            Log.d(TAG, "nctucommunication.qualtrics.com/jfe/form/SV_6zENhBtDLeXFLlH?" +
                    "UserID=" + UserID + "&DeviceID=" + DeviceID + "&ChooseImage=" + filepath);
//            try{
//                diary_button = MainActivity.diary_button;
//                diary_button.setEnabled(false);
//            }catch(Exception e){
//                Log.d(TAG, "Cannot find diary button");
//            }
        }
//        else{
//            int EsmNum = pref.getInt(todayMCountString, 0);
//            Log.d(TAG, "Esm number: " + EsmNum);
//            CSVHelper.storeToCSV("ESM.csv", "FilePath: " + filepath + "\n" + "UserId: " + UserID +
//                    "\n" + "Esm number: " + EsmNum + "Is Diary: " + isDiary);
//            getSharedPreferences("test",MODE_PRIVATE).edit().putBoolean("NewEsm", false).apply();
//            web_view.loadUrl("https://nctucommunication.qualtrics.com/jfe/form/SV_9zbzscrwlJNxLPD?" +
//                    "UserID=" + UserID + "&DeviceID=" + DeviceID + "&ChooseImage=" + filepath);
//            Log.d(TAG, "https://nctucommunication.qualtrics.com/jfe/form/SV_8ogL69pgbhFKwHX?" +
//                    "UserID=" + UserID + "&DeviceID=" + DeviceID + "&ChooseImage=" + filepath );
////            try{
////                esm_button = MainActivity.esm_button;
////                esm_button.setEnabled(false);
////            }catch(Exception e){
////                Log.d(TAG, "Cannot find esm button");
////            }
//        }

        //this.finish();
    }
}
