package com.example.accessibility_detect;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import labelingStudy.nctu.minuku.DBHelper.appDatabase;
import labelingStudy.nctu.minuku.model.DataRecord.UserDataRecord;

import static labelingStudy.nctu.minuku.config.Constants.Gmail_account;
import static labelingStudy.nctu.minuku.config.SharedVariables.ESMTEST_ALARM;
import static labelingStudy.nctu.minuku.config.SharedVariables.ReadableTimeAddHour;

public class ESMresult extends AppCompatActivity {
    private SharedPreferences pref;
    private String TAG = "EsmResult";
    List<String> Response = new ArrayList<>();
    List<String> Date = new ArrayList<>();
    appDatabase db;
    TextView esm_textview, diary_textview;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result);

        Log.d(TAG, "onCreate");
        esm_textview = (TextView) findViewById(R.id.esm_result);
        diary_textview = (TextView)findViewById(R.id.diary_result);

        db = appDatabase.getDatabase(this);
        pref = getSharedPreferences("test", MODE_PRIVATE);
        SetContent();
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.result_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.Storage_permission:
                setPermission();
                break;
            case R.id.Phone_info:
                gotoMain();
                break;
            case R.id.Setting:
                gotoSetting();
                break;
            case R.id.Email:
                Intent intent = new Intent (Intent.ACTION_SEND);
                intent.setType("message/rfc822");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{Gmail_account});
                intent.putExtra(Intent.EXTRA_SUBJECT, "關於執行紀錄或實驗ＡＰＰ");
                intent.putExtra(Intent.EXTRA_TEXT, "Hi, 我的device id 是" + pref.getString("UserID", "NA") +", 我有一些問題，");
                intent.setPackage("com.google.android.gm");
                if (intent.resolveActivity(getPackageManager())!=null)
                    startActivity(intent);
                else
                    Toast.makeText(this,"Gmail App is not installed",Toast.LENGTH_LONG).show();
                break;
            case R.id.ESM_Test:
                Intent test_intent = new Intent(this, AlarmReceiver.class);
                test_intent.setAction(ESMTEST_ALARM);
                sendBroadcast(test_intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void setPermission(){
        Intent AccessibilitySetting = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);// 協助工具
        AccessibilitySetting.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Intent UsageSetting = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS); //usage
        UsageSetting.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Intent NotiSetting = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);//notification
        NotiSetting.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Intent LocationSetting = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);    //location
        LocationSetting.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        getApplicationContext().startActivity(AccessibilitySetting);
        getApplicationContext().startActivity(UsageSetting);
        getApplicationContext().startActivity(NotiSetting);
        getApplicationContext().startActivity(LocationSetting);
    }
    private void gotoMain(){
        Intent Mainintent = new Intent(this, MainActivity.class);
        startActivity(Mainintent);
    }
    private void gotoSetting(){
        Intent Settingintent = new Intent(this, TimeActivity.class);
        startActivityForResult(Settingintent,0);
    }

    private void SetContent()
    {
        Log.d(TAG, "setContent");
        Date.clear();
        Response.clear();

        String TodayResponseSet = "";
        String TotalResponseSet = "";
        UserDataRecord userRecord = db.userDataRecordDao().getLastRecord();
        if(userRecord != null) {
            TodayResponseSet = userRecord.getESM_number();
            TotalResponseSet = userRecord.getTotal_ESM_number();
        }

        Log.d(TAG, "TodayResponseSet: " + TodayResponseSet);
        Log.d(TAG, "TotalResponseSet: " + TotalResponseSet);
        int esmTNum = 0;
        int esmNum = 0;

//        String TodayResponseSet = pref.getString("TodayResponse", "");
//        Log.d(TAG, TodayResponseSet);
        String[] TodayResponse = TodayResponseSet.split(",");
        for(int i = 0; i < TodayResponse.length; i++){
            Log.d(TAG, "Today Response: " + TodayResponse[i]);
        }

//        String TotalResponseSet = pref.getString("TotalResponse", "");
//        Log.d(TAG, TotalResponseSet);
        String[] TotalResponse = TotalResponseSet.split(",");
        for(int i = 0; i < TotalResponse.length; i++){
            Log.d(TAG, "Total Response: " + TotalResponse[i]);
        }

//        Long appstart = pref.getLong("appStartHour", 0);
        Long appstart = 0L;
        if(userRecord != null){
            appstart = userRecord.getApp_start();
        }
        String result = "";
        for(int i = 0; i < TotalResponse.length; i++){
            Log.d(TAG, "i: " + i);
            Date.add(DateConverter(appstart));
            appstart = ReadableTimeAddHour(appstart, 24);

            result = "已回答/總共: " + TodayResponse[i] + "/" + TotalResponse[i];
            esmNum += Integer.parseInt(TodayResponse[i]);
            esmTNum += Integer.parseInt(TotalResponse[i]);
            Response.add(result);
        }
//        int esmTNum = 0;
//        int esmNum = 0;
//        int ResponseSize = TotalResponse.length + 1;
//        Log.d(TAG, "size: " + ResponseSize);
//        int i = 0;
//        if( ResponseSize == 1)i = 0;
//        if( ResponseSize > 1)i = 1;
//        for(; i < ResponseSize; i++){
//            Log.d(TAG, "i: " + i);
//            Date.add(DateConverter(appstart));
//            appstart = ReadableTimeAddHour(appstart, 24);
//
//            String result;
//            if(i == (ResponseSize - 1)){
//                String TodayRCount = String.valueOf(pref.getInt(todayMCountString, 0));
//                String TodayCount = String.valueOf(pref.getInt("Esm_Num", 0));
//                result = "已回答/總共: " + TodayRCount + "/" + TodayCount;
//                esmNum += Integer.parseInt(TodayRCount);
//                esmTNum += Integer.parseInt(TodayCount);
//            }
//            else {
//                Log.d(TAG, "In Else");
//                result = "已回答/總共: " + TodayResponse[i] + "/" + TotalResponse[i];
//                esmNum += Integer.parseInt(TodayResponse[i]);
//                esmTNum += Integer.parseInt(TotalResponse[i]);
//            }
//            Response.add(result);
//        }
        for(int j = 0; j < Date.size(); j++){
            Log.d(TAG, "Response: " + Response.get(j));
        }
        esm_textview.setText("總體即時問卷進度(已回答/總共) : " + esmNum + "/" + esmTNum);

        int d = 0;
        int DiaryClick = 0;
        if(userRecord != null) {
            d = Integer.valueOf(userRecord.getTotalDiary_number());
            DiaryClick = Integer.valueOf(userRecord.getDiary_number());
        }
//        int d = pref.getInt("Diary_Num", 0);//日誌通知數目
//        int DiaryClick = pref.getInt("Diary_click", 0);
        diary_textview.setText("總體回顧問卷進度(已回答/總共) : " + DiaryClick + "/" + d);

        ResultListViewAdapter myAdapter = new ResultListViewAdapter(this, Date, Response);
        ListView listView = (ListView) findViewById(R.id.progress_list);
        listView.setAdapter(myAdapter);
    }
    public void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
        SetContent();
    }

    public void onResume(){
        super.onResume();
        Log.d(TAG, "onResume");
    }

    public void onPause(){
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onStop(){
        super.onStop();
        this.finish();
        Log.d(TAG, "onStop");
    }

    public String DateConverter(Long time){
        Long year = time/1000000;
        time = time % 1000000;
        Long month = time/10000;
        time = time % 10000;
        Long day = time / 100;
        return year + "/" + month + "/" + day;
    }
}

