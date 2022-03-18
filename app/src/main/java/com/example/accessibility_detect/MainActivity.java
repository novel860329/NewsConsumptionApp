package com.example.accessibility_detect;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.accessibility_detect.diarys.QuestionActivity_diary;
import com.example.accessibility_detect.questions.QuestionActivity;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import labelingStudy.nctu.minuku.DBHelper.appDatabase;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.model.DataRecord.UserDataRecord;

import static com.example.accessibility_detect.AlarmReceiver.DIARY_ID;
import static com.example.accessibility_detect.AlarmReceiver.ESM_ID;
import static labelingStudy.nctu.minuku.config.Constants.Gmail_account;
import static labelingStudy.nctu.minuku.config.SharedVariables.CanFillEsm;
import static labelingStudy.nctu.minuku.config.SharedVariables.ESMTEST_ALARM;
import static labelingStudy.nctu.minuku.config.SharedVariables.upload_btn;

//import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity {
    public static Button upload_button;
    private Button photo_button;
    private Button confirm_button;
    public static Button esm_button;
    public static Button diary_button;
    private List<String> listPermissionsNeeded;
    private EditText userid_editview;
    private TextView deviceid_textview;
    private TextView phonetype_textview;
    private TextView version_textview;
    private TextView upload_text;
    private appDatabase db;
    private static String TAG = "MainActivity";
    private int picture_number = 70;
    private static final int QUESTIONNAIRE_REQUEST = 2020;
    private SharedPreferences pref;
    private String[] t = new String[picture_number + 1];
    public static final int PHONE_STATE = 100;
    public static final int REQUEST_CAMERA = 200;
    MyAccessibilityService mobileAccessibilityService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Fabric.with(this, new Crashlytics());
        //startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);
        pref = getSharedPreferences("test", MODE_PRIVATE);
        db = appDatabase.getDatabase(MainActivity.this);
        InitLayout();
        if (!checkPermission()) {
            requestPermission();
        } else {
            Log.d(TAG, "check permission false");

            // 5/10
//            ESMjump("NA");
//            Calendar cal = Calendar.getInstance();
//            add_dairy(this, cal);

            SetTextviewContent();
            if(!isMyServiceRunning(MyBackgroundService.class))
            {
                startService();
            }
//            Toast.makeText(this, "已獲得存取權限", Toast.LENGTH_LONG).show();
            //this.finish();
        }

        UserDataRecord userRecord = db.userDataRecordDao().getLastRecord();
        boolean iskilled = false;
        if(userRecord != null){
            iskilled = userRecord.getIsKilled();
        }
//        boolean iskilled = pref.getBoolean("IsKilled", false);
        if(iskilled){
            finish();
        }
    }


    private void InitLayout()
    {
        upload_button = (Button)findViewById(R.id.upload_button);
        photo_button = (Button)findViewById(R.id.photo_button);
        esm_button = (Button)findViewById(R.id.esm_button);
        diary_button = (Button)findViewById(R.id.diary_button);
        confirm_button = (Button)findViewById(R.id.Confirm);
        deviceid_textview = (TextView) findViewById(R.id.device_id);
        phonetype_textview = (TextView)findViewById(R.id.phone_type);
        version_textview = (TextView)findViewById(R.id.android_version);
        userid_editview = (EditText)findViewById(R.id.user_id);
        upload_text = (TextView)findViewById(R.id.upload_text);

        deviceid_textview.setText("");
        phonetype_textview.setText("");
        version_textview.setText("");

        upload_button.setOnClickListener(UplaodClick);
        photo_button.setOnClickListener(PhotoClick);
        confirm_button.setOnClickListener(ConfirmClick);
        esm_button.setOnClickListener(EsmClick);
        diary_button.setOnClickListener(DiaryClick);
        UserDataRecord userRecord = db.userDataRecordDao().getLastRecord();
        String UserID = "NA";
        if(userRecord != null) {
            UserID = userRecord.getUserId();
        }
//        String UserID = getSharedPreferences("test",MODE_PRIVATE).getString("UserNum","NA");
        if(!UserID.equals("NA")){
            userid_editview.setText(UserID);
            confirm_button.setEnabled(false);
            userid_editview.setFocusable(false);
            userid_editview.setFocusableInTouchMode(false);
        }
    }
    private void SetTextviewContent()
    {
        pref.edit().putBoolean("Question_interrupt", true).apply();
        deviceid_textview.setText("設備編號 : " + Constants.DEVICE_ID);
        phonetype_textview.setText("手機型號 : " + Build.MANUFACTURER + " " + Build.MODEL);
        version_textview.setText("Android版本 : " + Build.VERSION.RELEASE);
        upload_button.setEnabled(upload_btn);

        boolean f = pref.getBoolean("UploadClick", false);
        String returnmageName = pref.getString("ReturnImageName", "NA");
        if(!returnmageName.equals("NA")){
            returnmageName = returnmageName.substring(0, 10) + "/" + returnmageName.substring(11);
        }
        if(f)upload_text.setText("今天資料上傳完成");
        else upload_text.setText("今天資料未上傳");

        UserDataRecord userDataRecord = db.userDataRecordDao().getLastRecord();
        long esm_time = 0L;
        boolean CanFillDiary = false;
        boolean CanFillESM = false;
        if(userDataRecord != null){
            esm_time = userDataRecord.getLastEsmTime();
            CanFillDiary = userDataRecord.getCanFillDiary();
        }
//        String esmtime = getReadableTime(LastEsmTime);
//        String esmtime = pref.getString("ESMtime", "0-0-0 0:0:0");//2020-04-11 14:29:35
//        String nowtime = getReadableTime(System.currentTimeMillis()).split(" ")[1];
        long now_time = System.currentTimeMillis();
//        long esm_time = 0;
//        try{
//            esm_time = Long.parseLong(dateToStamp(esmtime));
//        }catch(Exception e){
//            e.printStackTrace();
//        }

        Log.d(TAG, "testing: " + esm_time + " " + now_time);
//        String[] esmtime_split = esmtime.split(":");
//        String[] nowtime_split = nowtime.split(":");
//        int ESM_time = Integer.parseInt(esmtime_split[0]) * 3600 + Integer.parseInt(esmtime_split[1])*60 + Integer.parseInt(esmtime_split[2]);//170317
//        int now_time = Integer.parseInt(nowtime_split[0]) * 3600 + Integer.parseInt(nowtime_split[1])*60 + Integer.parseInt(esmtime_split[2]);//170317

        if(now_time - esm_time > 900*1000){
            esm_button.setEnabled(false);
            if(userDataRecord != null) {
                db.userDataRecordDao().updateCanFillESM(userDataRecord.get_id(), false);
            }
            CanFillEsm = false;
        }


        if(userDataRecord != null){
            CanFillESM = userDataRecord.getCanFillESM();
        }
        Log.d(TAG, "can fill? " + CanFillESM);
        if(CanFillESM){
            esm_button.setEnabled(true);
        }
        else{
            esm_button.setEnabled(false);
        }

        // 2/14 回顧問卷alarm沒trigger

        if(CanFillDiary){
            diary_button.setEnabled(true);
        }
        else{
            diary_button.setEnabled(false);
        }

        // 2/14 回顧問卷alarm沒trigger
//        diary_button.setEnabled(true);
    }

    private View.OnClickListener UplaodClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            pref.edit().putBoolean("UploadClick", true).apply();
            Intent upload_intent = new Intent(getApplicationContext(), WiFireminder.class);
            startActivity(upload_intent);
        }
    };
    private View.OnClickListener PhotoClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            Intent GalleryIntent = getApplicationContext().getPackageManager().getLaunchIntentForPackage("com.google.android.apps.photos");
//            ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//            // 通過呼叫ActivityManager的getRunningAppProcesses()方法獲得系統裡所有正在執行的程序
//            List<ActivityManager.RunningAppProcessInfo> appProcessList = mActivityManager
//                    .getRunningAppProcesses();
//            for (ActivityManager.RunningAppProcessInfo appProcess : appProcessList){
//                int pid = appProcess.pid; // pid
//                String processName = appProcess.processName; // 程序名
//                Log.d("test", "processName: " + processName + " pid: " + pid);
//            }
//            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

            Intent GalleryIntent = getApplicationContext().getPackageManager().getLaunchIntentForPackage("com.simplemobiletools.gallery");
            if(GalleryIntent == null){
                GalleryIntent = getApplicationContext().getPackageManager().getLaunchIntentForPackage("com.google.android.apps.photos");
            }
            if(GalleryIntent != null) {
                GalleryIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(GalleryIntent);
            }
        }
    };
    private View.OnClickListener ConfirmClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String UID = userid_editview.getText().toString();

            UserDataRecord userRecord = db.userDataRecordDao().getLastRecord();
            long _id = userRecord.get_id();
            db.userDataRecordDao().updateUserid(_id, UID);
            db.userDataRecordDao().updateUserConfirm(_id, true);

//            getSharedPreferences("test",MODE_PRIVATE).edit().putString("UserNum", UID).apply(); // 9/14

            confirm_button.setEnabled(false);
            userid_editview.setTextColor(getResources().getColor(R.color.colorAccent, null));
            userid_editview.setFocusable(false);
            userid_editview.setFocusableInTouchMode(false);
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("設定完成");
            builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    };
    private View.OnClickListener EsmClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, QuestionActivity.class); //Intent(this, 點下去會跳到ESM class)
            intent.putExtra("json_questions", MainActivity.this.loadQuestionnaireJson("questions_example.json"));
            intent.putExtra("relatedIdForQ", pref.getInt("QuestionnaireID", 0));
            intent.putExtra("notiId",String.valueOf(ESM_ID));
            NotificationManager notificationManager = (NotificationManager) MainActivity.this.getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancel(ESM_ID);
            startActivity(intent);
        }
    };
    private View.OnClickListener DiaryClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, QuestionActivity_diary.class); //Intent(this, 點下去會跳到ESM class)
            intent.putExtra("json_questions", loadQuestionnaireJson("diarys_example.json"));
            intent.putExtra("relatedIdForQ",pref.getInt("QuestionnaireID", 0));
            intent.putExtra("notiId",String.valueOf(DIARY_ID));
            NotificationManager notificationManager = (NotificationManager) MainActivity.this.getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancel(DIARY_ID);
            startActivity(intent);
        }
    };

//    @Override
//    protected void onStop() {
//        super.onStop();
//        Log.d(TAG,"onStop");
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        Log.d(TAG,"onDestroy");
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        Log.d(TAG,"onPause");
//    }

    //@Override
    /*protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG,"onStart");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG,"onRestart");
    }*/

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.Storage_permission:
                setPermission();
                break;
            case R.id.Setting:
                gotoSetting();
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

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.d(TAG, "onRequestResult: " + requestCode);
        switch (requestCode) {
            case PHONE_STATE:
                int phone_index = 0;
                for(int i = 0; i < permissions.length; i++){
                    if(permissions[i].equals("android.permission.READ_PHONE_STATE")){
                        phone_index = i;
                    }
                }
                if (grantResults.length > 0 && grantResults[phone_index] == PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    Log.d(TAG, "Permission is: " + String.valueOf(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED));
                    TelephonyManager  tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    Toast.makeText(this, "已獲得存取權限", Toast.LENGTH_LONG).show();
                    Constants.DEVICE_ID = tm.getDeviceId();
                    if(Constants.DEVICE_ID  == null){
                        Constants.DEVICE_ID = Settings.Secure.getString(
                                this.getContentResolver(),
                                Settings.Secure.ANDROID_ID);
                    }
                    pref.edit()
                            .putString("UserID",  Constants.DEVICE_ID)
                            .apply();
                    Log.d(TAG, "userID = " +  Constants.DEVICE_ID );

                    SetTextviewContent();

                    if(!isMyServiceRunning(MyBackgroundService.class))
                    {
                        startService();
                    }

                }
                else {
                    Toast.makeText(this,"Permission Denied. We can't get phone number.", Toast.LENGTH_LONG).show();
                }
                //this.finish();
                break;
        }
    }

    private void requestPermission(){
        ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), PHONE_STATE);
    }
    //Check whether the user has granted the WRITE_STORAGE permission//
    public boolean checkPermission() {
        Log.d(TAG, "checkPermission");
        int hasWriteExternalStoragePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int hasCameraPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int hasReadPhoneStatePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        int permissionFineLocation = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionCoarseLocation = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionActivity = 1;
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            permissionActivity = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION);
//        }
        listPermissionsNeeded = new ArrayList<>();

        if (hasWriteExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(hasCameraPermission != PackageManager.PERMISSION_GRANTED){
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if(hasReadPhoneStatePermission != PackageManager.PERMISSION_GRANTED){
            listPermissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (permissionFineLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (permissionCoarseLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        }
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            Log.d(TAG, "version 10 Q");
//            if (permissionActivity != PackageManager.PERMISSION_GRANTED) {
//                listPermissionsNeeded.add(Manifest.permission.ACTIVITY_RECOGNITION);
//            }
//        }

        if(!listPermissionsNeeded.isEmpty())return false;
        else return true;
    }

    public void startService() {

        /*Log.d(TAG,"Foreground Service start");
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        serviceIntent.putExtra("inputExtra", "Your ID is: " + Constants.DEVICE_ID);
        ContextCompat.startForegroundService(this, serviceIntent);*/

        /*mobileAccessibilityService = new MyAccessibilityService();
        Intent intent = new Intent(this, MyAccessibilityService.class);
        this.startService(intent);*/
//        signIn();
        SharedPreferences settings = getSharedPreferences("test", Context.MODE_PRIVATE);
        settings.edit().clear().apply();
        SharedPreferences setting = getSharedPreferences(Constants.sharedPrefString, Context.MODE_PRIVATE);
        setting.edit().clear().apply();
        startService(new Intent(MainActivity.this, MyBackgroundService.class));
    }

//    private void signIn() {
//        // Launches the sign in flow, the result is returned in onActivityResult
//        Log.d(TAG, "Google sign in");
//        Intent intent = mSignInClient.getSignInIntent();
//        startActivityForResult(intent, RC_SIGN_IN);
//    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode != QUESTIONNAIRE_REQUEST) {
            switch (resultCode) {
                case RESULT_OK:
                    String startTime = data.getExtras().getString("StartTime");
                    String endTime = data.getExtras().getString("EndTime");
                    Log.d(TAG, startTime + " ~ " + endTime);
                    break;
                default:
                    break;
            }
        }
//        if (requestCode == RC_SIGN_IN) {
//            Task<GoogleSignInAccount> task =
//                    GoogleSignIn.getSignedInAccountFromIntent(data);
//            if (task.isSuccessful()) {
//                // Sign in succeeded, proceed with account
//                GoogleSignInAccount acct = task.getResult();
//                Log.d(TAG, "Sign in success");
//            } else {
//                // Sign in failed, handle failure and update UI
//                // ...
//                Log.d(TAG, "Sign in failed");
//            }
//        }
    }
    public void stopService() {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        stopService(serviceIntent);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
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

    private void gotoSetting(){
        Intent Settingintent = new Intent(this, TimeActivity.class);
        startActivityForResult(Settingintent,0);
    }

    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        pref.edit().putBoolean("Question_interrupt", false).apply();
    }

    public void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
        pref.edit().putBoolean("Question_interrupt", false).apply();
    }
    public void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        pref.edit().putBoolean("Question_interrupt", false).apply();
    }
    public void onRestart() {
        super.onRestart();
        Log.d(TAG,"onRestart");
        SetTextviewContent();
    }

    public void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
        SetTextviewContent();
    }

    private String loadQuestionnaireJson(String filename) {
        // Log.d("BootCompleteReceiver","In MainActivity loadQuestionnaireJson");
        try {
            InputStream is = getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }

    }
}
