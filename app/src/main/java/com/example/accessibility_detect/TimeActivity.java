package com.example.accessibility_detect;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

import labelingStudy.nctu.minuku.DBHelper.appDatabase;
import labelingStudy.nctu.minuku.model.DataRecord.UserDataRecord;

import static com.example.accessibility_detect.Utils.screenshotPermission;
import static labelingStudy.nctu.minuku.config.Constants.Gmail_account;
import static labelingStudy.nctu.minuku.config.SharedVariables.ESMTEST_ALARM;
import static labelingStudy.nctu.minuku.config.SharedVariables.Last_Agree;
import static labelingStudy.nctu.minuku.config.SharedVariables.Last_Dialog_Time;
import static labelingStudy.nctu.minuku.config.SharedVariables.Trigger_list;

//import com.crashlytics.android.Crashlytics;
//import io.fabric.sdk.android.Fabric;

public class TimeActivity extends AppCompatActivity {
    private String TAG = "TimeActivity";
    SharedPreferences pref;
    private appDatabase db;
    private Spinner allow_start, allow_end;
    private Button submit;
    private CheckBox dialog_deny;

//    private EditText agree_dialog_time; // 10/21
    private boolean setTimeWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Fabric.with(this, new Crashlytics());
        pref = getSharedPreferences("test",MODE_PRIVATE);
        db = appDatabase.getDatabase(this);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.setting_time);
        InitLayout();
    }
    private void InitLayout()
    {
        allow_start = (Spinner) findViewById(R.id.start_hour);
        allow_end = (Spinner)findViewById(R.id.end_hour);
        submit = (Button)findViewById(R.id.submit);

        dialog_deny = (CheckBox) findViewById(R.id.agree_cb);

//        String permission_uri = pref.getString("screenshotPermission", null);
//        Log.d(TAG, "permission_uri: " + permission_uri);
        if(screenshotPermission == null){
            dialog_deny.setEnabled(false);
        }
        else{
            dialog_deny.setEnabled(true);
        }

        boolean DialogDeny = false;

        UserDataRecord userRecord = db.userDataRecordDao().getLastRecord();

        if(userRecord != null)
        {
            DialogDeny = userRecord.getDialogDeny();
        }
//        boolean DialogDeny = pref.getBoolean("DialogDeny", false);
        if(DialogDeny){
            dialog_deny.setChecked(true);
        }
        else{
            dialog_deny.setChecked(false);
        }

//        agree_dialog_time = (EditText)findViewById(R.id.agree_et); // 10/21
        submit.setOnClickListener(SubmitClick);
        dialog_deny.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //判斷CheckBox是否有勾選，同mCheckBox.isChecked()
                if (isChecked) {
                    Log.d(TAG, "Deny Dialog");
                    if(userRecord != null)
                    {
                        db.userDataRecordDao().updateDialogDeny(userRecord.get_id(), true);
                    }
//                    pref.edit().putBoolean("DialogDeny", true).apply();
                } else {
                    Log.d(TAG, "Accept Dialog");
                    if(userRecord != null)
                    {
                        db.userDataRecordDao().updateDialogDeny(userRecord.get_id(), false);
                    }
//                    pref.edit().putBoolean("DialogDeny", false).apply();
                }
            }
        });

        //需要記住使用者之前選擇的選項
        UserDataRecord userDataRecord = db.userDataRecordDao().getLastRecord();
        String time = "0";
        if(userDataRecord != null) {
            setTimeWindow = userDataRecord.gettime_confirm();
//            time = userDataRecord.getagree_dialog_time(); 10/21
            Log.d(TAG, "Set Time window: " + setTimeWindow);
        }
//        setTimeWindow = pref.getBoolean("SetTimeWindow", false);

        Log.d(TAG, "Time: " + time);
//        String time = pref.getString("agree_dialog_time", "");
        if(setTimeWindow){
            allow_start.setEnabled(false);
            allow_end.setEnabled(false);
            String[] start_list = getResources().getStringArray(R.array.start_list);
            String[] end_list = getResources().getStringArray(R.array.end_list);
            int MinHour = Integer.parseInt(userDataRecord.getquestionnaire_startTime());
            int MaxHour = Integer.parseInt(userDataRecord.getquestionnaire_endTime());
            Log.d(TAG, "min and max = " + MinHour + " " + MaxHour);
//            int MinHour = pref.getInt("MinHour", 9);
//            int MaxHour = pref.getInt("MaxHour", 22);
            for(int i = 0; i < start_list.length; i++){
                int startTime = Integer.parseInt(start_list[i].split(":")[0]);
                int endTime = Integer.parseInt(end_list[i].split(":")[0]);
                if(MinHour == startTime)allow_start.setSelection(i);
                if(MaxHour == endTime)allow_end.setSelection(i);
            }
        }
        else{
            allow_start.setSelection(4);
            allow_end.setSelection(5);
        }
        // 10/21
//        agree_dialog_time.setTextColor(getResources().getColor(R.color.black, null));
//        agree_dialog_time.setCursorVisible(true);
//        agree_dialog_time.setText(time);
//        agree_dialog_time.setOnTouchListener(new View.OnTouchListener() {
//            @SuppressLint("ClickableViewAccessibility")
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (MotionEvent.ACTION_DOWN == event.getAction()) {
//                    agree_dialog_time.setCursorVisible(true);// 再次点击显示光标
//                    agree_dialog_time.setTextColor(getResources().getColor(R.color.black, null));
//                }
//                return false;
//            }
//        });
//        agree_dialog_time.setEnabled(false);
    }



    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.time_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.Storage_permission:
                setPermission();
                break;
            case  R.id.Phone_info:
                gotoMain();
                break;
            case R.id.Result:
                Intent Esmintent = new Intent(this, ESMresult.class);
                startActivity(Esmintent);
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

    private View.OnClickListener SubmitClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            UserDataRecord userDataRecord = db.userDataRecordDao().getLastRecord();
            long _id = userDataRecord.get_id();
            if (!setTimeWindow) {
                String[] start_list = getResources().getStringArray(R.array.start_list);
                String[] end_list = getResources().getStringArray(R.array.end_list);
                String[] shot_list = getResources().getStringArray(R.array.shot_list);
                int startTime = allow_start.getSelectedItemPosition();
                int endTime = allow_end.getSelectedItemPosition();

                String startString = start_list[startTime];
                String endString = end_list[endTime];

                int startHr = Integer.parseInt(startString.split(":")[0]);
                int endHr = Integer.parseInt(endString.split(":")[0]);
                if(startHr < endHr){
                    if (endHr - startHr < 12) {
                        //跳dialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(TimeActivity.this);
                        builder.setMessage("允許發問卷的時間需大於12小時");
                        builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                    else {
                        String time = "";
//                    String time = agree_dialog_time.getText().toString();//幾小時候不要詢問我 10/21
                        Log.d(TAG, "Time==" + time);
                        if(time.equals("")){
                            time = "0";
                        }
                        if(Integer.parseInt(time) > 24){
                            AlertDialog.Builder builder = new AlertDialog.Builder(TimeActivity.this);
                            builder.setMessage("截圖同意視窗請勿超過24小時");
                            builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                        else {
                            Intent intent = new Intent();
                            intent.putExtra("StartTime", start_list[startTime]);
                            intent.putExtra("EndTime", end_list[endTime]);
                            allow_start.setEnabled(false);
                            allow_end.setEnabled(false);
//                        pref.edit().putBoolean("SetTimeWindow", true).apply();
//                        pref.edit().putInt("MinHour", startHr).apply();
//                        pref.edit().putInt("MaxHour", endHr).apply();

                            db.userDataRecordDao().updateQuestionnaire_startTime(_id, startString.split(":")[0]);
                            db.userDataRecordDao().updateQuestionnaire_endTime(_id, endString.split(":")[0]);
                            db.userDataRecordDao().updateTimeConfirm(_id, true);

                            //把舊的diary alarm殺掉，再發一個新的

//                            AlarmReceiver.cancel_reminder(TimeActivity.this);

                            Log.d(TAG, "Cancel Alarm in Time activity");

                            boolean isdiary = pref.getBoolean("DiaryClick", false);
                            if(!isdiary) {
//                                AlarmReceiver.cancel_dairy(TimeActivity.this);
                                Calendar dairy_cal = Calendar.getInstance(); //取得時間
                                if(endHr < startHr){
                                    dairy_cal.add(Calendar.DAY_OF_MONTH, 1);
                                }
                                dairy_cal.set(Calendar.HOUR_OF_DAY , endHr - 1);
                                dairy_cal.set(Calendar.MINUTE, 0);
                                dairy_cal.set(Calendar.SECOND, 0);
                                Log.d(TAG, dairy_cal.toString());
//                                AlarmReceiver.add_dairy(TimeActivity.this, dairy_cal);
                            }
                            Log.d(TAG, "Add Alarm in Time activity");

                            Calendar reminder_cal = Calendar.getInstance(); //取得時間
                            if(endHr < startHr){
                                reminder_cal.add(Calendar.DAY_OF_MONTH, 1);
                            }
                            reminder_cal.set(Calendar.HOUR_OF_DAY, endHr - 1);
                            reminder_cal.set(Calendar.MINUTE, 5);
                            reminder_cal.set(Calendar.SECOND, 0);
                            Log.d(TAG, reminder_cal.toString());
//                            AlarmReceiver.add_reminder(TimeActivity.this, reminder_cal);

                            //初始化
                            for(int i = 0; i < Trigger_list.length; i++){
                                Last_Agree.put(Trigger_list[i], false);
                                Last_Dialog_Time.put(Trigger_list[i], 0L);
                            }
                            // 10/21
//                        agree_dialog_time.setTextColor(getResources().getColor(R.color.colorAccent, null));
//                        agree_dialog_time.setCursorVisible(false);
//                        db.userDataRecordDao().updateagree_dialog_time(_id, time);
//                        pref.edit().putString("agree_dialog_time", time).apply();

                            AlertDialog.Builder builder = new AlertDialog.Builder(TimeActivity.this);
                            builder.setMessage("設定完成");
                            builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
                            builder.setNegativeButton("回上一頁", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    TimeActivity.this.finish();
                                }
                            });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                            TimeActivity.this.setResult(RESULT_OK, intent);
                        }
                    }
                }
                else if(endHr < startHr){ // startHr = 12, endHr = 3
                    if(endHr + 24 - startHr < 12){
                        //跳dialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(TimeActivity.this);
                        builder.setMessage("允許發問卷的時間需大於12小時");
                        builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                    else {
                        String time = "";
//                    String time = agree_dialog_time.getText().toString();//幾小時候不要詢問我 10/21
                        Log.d(TAG, "Time==" + time);
                        if(time.equals("")){
                            time = "0";
                        }
                        if(Integer.parseInt(time) > 24){
                            AlertDialog.Builder builder = new AlertDialog.Builder(TimeActivity.this);
                            builder.setMessage("截圖同意視窗請勿超過24小時");
                            builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                        else {
                            Intent intent = new Intent();
                            intent.putExtra("StartTime", start_list[startTime]);
                            intent.putExtra("EndTime", end_list[endTime]);
                            allow_start.setEnabled(false);
                            allow_end.setEnabled(false);
//                        pref.edit().putBoolean("SetTimeWindow", true).apply();
//                        pref.edit().putInt("MinHour", startHr).apply();
//                        pref.edit().putInt("MaxHour", endHr).apply();

                            db.userDataRecordDao().updateQuestionnaire_startTime(_id, startString.split(":")[0]);
                            db.userDataRecordDao().updateQuestionnaire_endTime(_id, endString.split(":")[0]);
                            db.userDataRecordDao().updateTimeConfirm(_id, true);

                            //把舊的diary alarm殺掉，再發一個新的

//                            AlarmReceiver.cancel_reminder(TimeActivity.this);

                            Log.d(TAG, "Cancel Alarm in Time activity");

                            boolean isdiary = pref.getBoolean("DiaryClick", false);
                            if(!isdiary) {
//                                AlarmReceiver.cancel_dairy(TimeActivity.this);
                                Calendar dairy_cal = Calendar.getInstance(); //取得時間
                                if(endHr < startHr){
                                    dairy_cal.add(Calendar.DAY_OF_MONTH, 1);
                                }
                                dairy_cal.set(Calendar.HOUR_OF_DAY , endHr - 1);
                                dairy_cal.set(Calendar.MINUTE, 0);
                                dairy_cal.set(Calendar.SECOND, 0);
                                Log.d("AlarmReceiver", "TimeActivity diary cal: " + (dairy_cal.get(Calendar.MONTH) + 1) + "/" + dairy_cal.get(Calendar.DAY_OF_MONTH) + " " + dairy_cal.get(Calendar.HOUR_OF_DAY));
//                                AlarmReceiver.add_dairy(TimeActivity.this, dairy_cal);
                            }
                            Log.d(TAG, "Add Alarm in Time activity");

                            Calendar reminder_cal = Calendar.getInstance(); //取得時間
                            if(endHr < startHr){
                                reminder_cal.add(Calendar.DAY_OF_MONTH, 1);
                            }
                            reminder_cal.set(Calendar.HOUR_OF_DAY, endHr - 1);
                            reminder_cal.set(Calendar.MINUTE, 5);
                            reminder_cal.set(Calendar.SECOND, 0);
                            Log.d("AlarmReceiver", "TimeActivity reminder cal: " + (reminder_cal.get(Calendar.MONTH) + 1) + "/" + reminder_cal.get(Calendar.DAY_OF_MONTH) + " " + reminder_cal.get(Calendar.HOUR_OF_DAY));
//                            AlarmReceiver.add_reminder(TimeActivity.this, reminder_cal);

                            //初始化
                            for(int i = 0; i < Trigger_list.length; i++){
                                Last_Agree.put(Trigger_list[i], false);
                                Last_Dialog_Time.put(Trigger_list[i], 0L);
                            }
                            // 10/21
//                        agree_dialog_time.setTextColor(getResources().getColor(R.color.colorAccent, null));
//                        agree_dialog_time.setCursorVisible(false);
//                        db.userDataRecordDao().updateagree_dialog_time(_id, time);
//                        pref.edit().putString("agree_dialog_time", time).apply();

                            AlertDialog.Builder builder = new AlertDialog.Builder(TimeActivity.this);
                            builder.setMessage("設定完成");
                            builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
                            builder.setNegativeButton("回上一頁", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    TimeActivity.this.finish();
                                }
                            });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                            TimeActivity.this.setResult(RESULT_OK, intent);
                        }
                    }
                }
                else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(TimeActivity.this);
                    builder.setMessage("允許發問卷的時間需大於12小時");
                    builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
//                if (endHr - startHr < 12) {
//                    //跳dialog
//                    AlertDialog.Builder builder = new AlertDialog.Builder(TimeActivity.this);
//                    builder.setMessage("允許發問卷的時間需大於12小時");
//                    builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//                        }
//                    });
//                    AlertDialog dialog = builder.create();
//                    dialog.show();
//                }
//                else {
//                    String time = "";
////                    String time = agree_dialog_time.getText().toString();//幾小時候不要詢問我 10/21
//                    Log.d(TAG, "Time==" + time);
//                    if(time.equals("")){
//                        time = "0";
//                    }
//                    if(Integer.parseInt(time) > 24){
//                        AlertDialog.Builder builder = new AlertDialog.Builder(TimeActivity.this);
//                        builder.setMessage("截圖同意視窗請勿超過24小時");
//                        builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                            }
//                        });
//                        AlertDialog dialog = builder.create();
//                        dialog.show();
//                    }
//                    else {
//                        Intent intent = new Intent();
//                        intent.putExtra("StartTime", start_list[startTime]);
//                        intent.putExtra("EndTime", end_list[endTime]);
//                        allow_start.setEnabled(false);
//                        allow_end.setEnabled(false);
////                        pref.edit().putBoolean("SetTimeWindow", true).apply();
////                        pref.edit().putInt("MinHour", startHr).apply();
////                        pref.edit().putInt("MaxHour", endHr).apply();
//
//                        db.userDataRecordDao().updateQuestionnaire_startTime(_id, startString.split(":")[0]);
//                        db.userDataRecordDao().updateQuestionnaire_endTime(_id, endString.split(":")[0]);
//                        db.userDataRecordDao().updateTimeConfirm(_id, true);
//
//                        //把舊的diary alarm殺掉，再發一個新的
//
//                        AlarmReceiver.cancel_reminder(TimeActivity.this);
//
//                        Log.d(TAG, "Cancel Alarm in Time activity");
//
//                        boolean isdiary = pref.getBoolean("DiaryClick", false);
//                        if(!isdiary) {
//                            AlarmReceiver.cancel_dairy(TimeActivity.this);
//                            Calendar dairy_cal = Calendar.getInstance(); //取得時間
//                            if(endHr < startHr){
//                                dairy_cal.add(Calendar.DAY_OF_MONTH, 1);
//                            }
//                            dairy_cal.set(Calendar.HOUR_OF_DAY , endHr - 1);
//                            dairy_cal.set(Calendar.MINUTE, 0);
//                            dairy_cal.set(Calendar.SECOND, 0);
//                            Log.d(TAG, dairy_cal.toString());
//                            AlarmReceiver.add_dairy(TimeActivity.this, dairy_cal);
//                        }
//                        Log.d(TAG, "Add Alarm in Time activity");
//
//                        Calendar reminder_cal = Calendar.getInstance(); //取得時間
//                        if(endHr < startHr){
//                            reminder_cal.add(Calendar.DAY_OF_MONTH, 1);
//                        }
//                        reminder_cal.set(Calendar.HOUR_OF_DAY, endHr - 1);
//                        reminder_cal.set(Calendar.MINUTE, 5);
//                        reminder_cal.set(Calendar.SECOND, 0);
//                        Log.d(TAG, reminder_cal.toString());
//                        AlarmReceiver.add_reminder(TimeActivity.this, reminder_cal);
//                        //初始化
//                        for(int i = 0; i < Trigger_list.length; i++){
//                            Last_Agree.put(Trigger_list[i], false);
//                            Last_Dialog_Time.put(Trigger_list[i], 0L);
//                        }
//                        // 10/21
////                        agree_dialog_time.setTextColor(getResources().getColor(R.color.colorAccent, null));
////                        agree_dialog_time.setCursorVisible(false);
////                        db.userDataRecordDao().updateagree_dialog_time(_id, time);
////                        pref.edit().putString("agree_dialog_time", time).apply();
//
//                        AlertDialog.Builder builder = new AlertDialog.Builder(TimeActivity.this);
//                        builder.setMessage("設定完成");
//                        builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                            }
//                        });
//                        builder.setNegativeButton("回上一頁", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                TimeActivity.this.finish();
//                            }
//                        });
//                        AlertDialog dialog = builder.create();
//                        dialog.show();
//                        TimeActivity.this.setResult(RESULT_OK, intent);
//                    }
//                }
            }
            else{
                String time = "";
//                String time = agree_dialog_time.getText().toString();//幾小時候不要詢問我 10/21
                Log.d(TAG, "Time==" + time);
                if(time.equals("")){
                    time = "0";
                }
                if(Integer.parseInt(time) > 24){
                    AlertDialog.Builder builder = new AlertDialog.Builder(TimeActivity.this);
                    builder.setMessage("截圖同意視窗請勿超過24小時");
                    builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else{
                    //初始化
                    for(int i = 0; i < Trigger_list.length; i++){
                        Last_Agree.put(Trigger_list[i], false);
                        Last_Dialog_Time.put(Trigger_list[i], 0L);
                    }
                    // 10/21
//                    agree_dialog_time.setTextColor(getResources().getColor(R.color.colorAccent, null));
//                    agree_dialog_time.setCursorVisible(false);

//                    db.userDataRecordDao().updateagree_dialog_time(_id, time);
//                    pref.edit().putString("agree_dialog_time", time).apply();

                    AlertDialog.Builder builder = new AlertDialog.Builder(TimeActivity.this);
                    builder.setMessage("設定完成");
                    builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
                    builder.setNegativeButton("回上一頁", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            TimeActivity.this.finish();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        }
    };

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

    public void onRestart() {
        super.onRestart();
        Log.d(TAG,"onRestart");
        InitLayout();
    }

    public void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
        InitLayout();
    }
}
