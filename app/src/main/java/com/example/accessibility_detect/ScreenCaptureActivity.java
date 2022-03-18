package com.example.accessibility_detect;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import labelingStudy.nctu.minuku.DBHelper.appDatabase;
import labelingStudy.nctu.minuku.Utilities.CSVHelper;
import labelingStudy.nctu.minuku.manager.MinukuStreamManager;
import labelingStudy.nctu.minuku.model.DataRecord.AppTimesDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.SessionDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.UserDataRecord;
import labelingStudy.nctu.minuku.streamgenerator.AppTimesStreamGenerator;
import labelingStudy.nctu.minukucore.exception.StreamNotFoundException;

import static com.example.accessibility_detect.Utils.screenshotPermission;

//import com.crashlytics.android.Crashlytics;
//import io.fabric.sdk.android.Fabric;

public class ScreenCaptureActivity extends Activity {
    private String TAG = "ScreenCaptureActivity";
    MinukuStreamManager streamManager;

    private String TriggerApp = "";
    private boolean FacebookOpen = false;
    private boolean MessengerOpen = false;
    private boolean YoutubeOpen = false;
    private boolean Instagram = false;
    private boolean LineToday = false;
    private boolean LineMes = false;
    private boolean NewsApp = false;
    private boolean PTTApp = false;
    private boolean googleNews = false;
    private boolean Chrome = false;
    boolean isNoti;
    private int agree_interval;
    private Intent screenshot;
    private static AppTimesStreamGenerator apptimesStreamGenerator;
    appDatabase db;
    private SessionDataRecord sessionDataRecord;
    private SharedPreferences pref;
    private Object object;
    private String obj_key;


    protected void onCreate(final Bundle savedInstanceState){
        Log.d(TAG, "Oncreate");
        super.onCreate(savedInstanceState);
//        Fabric.with(this, new Crashlytics());
        streamManager = MinukuStreamManager.getInstance();
        pref = getSharedPreferences("test",MODE_PRIVATE);

        db = appDatabase.getDatabase(getApplicationContext());
//        UserDataRecord userDataRecord = db.userDataRecordDao().getLastRecord();

//        String time = "0";

//        if(time != null) {
//            agree_interval = Integer.parseInt(time);
//        }
//        else{
//            agree_interval = 0;
//        }
//        Log.d(TAG, "Time interval: " + agree_interval);
//        boolean ReadNews = pref.getBoolean("ReadNews",false);
        //            CSVHelper.storeToCSV("AccessibilityDetect.csv", "ScreenCaptureActivity onCreate time: " + (System.currentTimeMillis()));

        Intent intent = getIntent();
        isNoti = intent.getBooleanExtra("FromNotification", false);
//            FacebookOpen = intent.getBooleanExtra("Facebook", false);
//            MessengerOpen = intent.getBooleanExtra("Messenger", false);
//            YoutubeOpen = intent.getBooleanExtra("Youtube", false);
//            Instagram = intent.getBooleanExtra("Instagram", false);
//            LineToday = intent.getBooleanExtra("LineToday", false);
//            LineMes = intent.getBooleanExtra("LineMes", false);
//            NewsApp = intent.getBooleanExtra("News App", false);
//            PTTApp = intent.getBooleanExtra("PTT App", false);
//            googleNews = intent.getBooleanExtra("googleNews", false);
//            Chrome = intent.getBooleanExtra("Chrome", false);
        //        else
//        {
//            FacebookOpen = false;
//            MessengerOpen = false;
//            YoutubeOpen = false;
//            Instagram = false;
//            LineToday = false;
//            LineMes = false;
//            NewsApp = false;
//            PTTApp = false;
//            googleNews = false;
//            Chrome = false;
//        }
//        Log.d(TAG, "FacebookOpen: " + FacebookOpen + " " + Instagram + " " + googleNews);

//        CSVHelper.storeToCSV("AccessibilityDetect.csv", "ScreenCapture Activity Oncreate");

        try {
            this.apptimesStreamGenerator = (AppTimesStreamGenerator) MinukuStreamManager.getInstance().getStreamGeneratorFor(AppTimesDataRecord.class);
            Log.d(TAG, "Initial MyAccessibility Service");
        } catch (StreamNotFoundException e) {
            this.apptimesStreamGenerator = apptimesStreamGenerator;
            Log.d(TAG, "Initial MyAccessibility Service Failed");
        }
        requestCapturePermission();
    }

    public void requestCapturePermission(){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        screenshot = new Intent(getApplicationContext(),ScreenCapture.class);
        String Trigger = pref.getString("Trigger", "");
        Log.d(TAG, "New Trigger method: " + Trigger);
        if(Trigger.equals("Facebook")){
            TriggerApp = "Facebook";
        }
        else if(Trigger.equals("LineToday")){
            TriggerApp = "LineToday";
        }
        else if(Trigger.equals("NewsApp")){
            TriggerApp = "NewsApp";
        }
        else if(Trigger.equals("googleNews")){
            TriggerApp = "googleNews";
        }
        else if(Trigger.equals("LineMes")){
            TriggerApp = "LineMes";
        }
        else if(Trigger.equals("Youtube")){
            TriggerApp = "Youtube";
        }
        else if(Trigger.equals("Instagram")){
            TriggerApp = "Instagram";
        }
        else if(Trigger.equals("PTT")){
            TriggerApp = "PTT";
        }
        else if(Trigger.equals("Messenger")){
            TriggerApp = "Messenger";
        }
        else if(Trigger.equals("Chrome")){
            TriggerApp = "Chrome";
        }
        else{
            TriggerApp = "";
        }

        if(!TriggerApp.equals("")){
            pref.edit()
                    .putString("TriggerApp", TriggerApp)
                    .apply();

//            CSVHelper.storeToCSV("AccessibilityDetect.csv", "ScreenCapture Activity Trigger app is " + TriggerApp);

            MediaProjectionManager mediaProjectionManager = (MediaProjectionManager)
                    getSystemService(Context.MEDIA_PROJECTION_SERVICE);

            long lastid = pref.getLong("SessionID", 0);
//            long last_dialog_time = pref.getLong("last_dialog_time",0);
//            long now_dialog_time = System.currentTimeMillis();
//            boolean last_agree = pref.getBoolean("last_agree",true);
//            boolean pop_up = Dialog_pop(last_dialog_time, now_dialog_time);//有沒有超過interval
//            CSVHelper.storeToCSV("AccessibilityDetect.csv", "Session id is " + lastid);
            if (lastid != -1) {
//                Last_Dialog_Time.put("Base", 0L);
//                long last_dialog_time = 0L;
//                long now_dialog_time = System.currentTimeMillis();
//                boolean last_agree = false;
//                if (Last_Dialog_Time != null) {
//                    if (Last_Dialog_Time.containsKey(TriggerApp)) {
//                        last_dialog_time = Last_Dialog_Time.get(TriggerApp);
//                    }
//                }
//                CSVHelper.storeToCSV("ScreenCaptureActivity.csv", "Last_Dialog_Time: " + Last_Dialog_Time);
//
//                Last_Agree.put("Base", false);
//                if (Last_Agree != null) {
//                    if (Last_Agree.containsKey(TriggerApp)) {
//                        last_agree = Last_Agree.get(TriggerApp);
//                    }
//                }
//                CSVHelper.storeToCSV("ScreenCaptureActivity.csv", "Last_Agree: " + Last_Agree);

//                boolean pop_up = Dialog_pop(last_dialog_time, now_dialog_time);//有沒有超過interval
//                CSVHelper.storeToCSV("ScreenCaptureActivity.csv", last_dialog_time + " | " + now_dialog_time + " | " + pop_up + " | " + last_agree);
//                Log.d(TAG, last_dialog_time + " | " + now_dialog_time + " | " + pop_up + " | " + last_agree);
//                if (isNoti) pop_up = true;

//                CSVHelper.storeToCSV("AccessibilityDetect.csv", "ScreenCapture Activity dialog pop up is " + pop_up);

                try {
                    boolean DialogDeny = false;

                    UserDataRecord userRecord = db.userDataRecordDao().getLastRecord();

                    if(userRecord != null)
                    {
                        DialogDeny = userRecord.getDialogDeny();
                    }

//                    boolean DialogDeny = pref.getBoolean("DialogDeny", false);
                    if (DialogDeny) {//問過而且說同意(1)
                        CSVHelper.storeToCSV("ScreenCaptureActivity.csv", "You have agreed");
                        Log.d(TAG, "Deny Dialog and start screenshot");
//                        String permission_uri = pref.getString("screenshotPermission", null);
//                        String permission_extras = pref.getString("permission_extras", null);
//                        Log.d(TAG, "permission_uri: " + permission_uri);
                        try {
//                            Intent permission = Intent.parseUri(permission_uri, 0);
//                            permission.putExtra(obj_key, object.toString());
                            if (screenshotPermission != null) {
//                                permission.putExtra(permission_extras
                                ScreenCapture.setResultIntent(screenshotPermission);
                                GotoScreenShot();
                                this.finish();
                            }
                            else{
                                Intent it = mediaProjectionManager.createScreenCaptureIntent();
                                startActivityForResult(it, 1);
                            }
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    else{//沒問過(0)
//                    CSVHelper.storeToCSV("ScreenCaptureActivity.csv", "You have not agreed");
//                        CSVHelper.storeToCSV("AccessibilityDetect.csv", "ScreenCapture Activity start activity for result");
                        Log.d(TAG, "Accept Dialog and start screenshot");
                        Intent it = mediaProjectionManager.createScreenCaptureIntent();
                        Log.d(TAG, "startActivityForResult");
                        startActivityForResult(it, 1);
                    }
//                    else {//問過但不同意(2)
//                        CSVHelper.storeToCSV("ScreenCaptureActivity.csv", "hasPermission Screenshot but disagree");
//                        this.finish();
//                    }
//                    CSVHelper.storeToCSV("Dialog.csv","Dialog Time: " + System.currentTimeMillis() + " After agree dialog " + TriggerApp);
                } catch (final RuntimeException ignored) {
                    Log.d(TAG, "not hasPermission Screenshot exception");
                    Intent it = mediaProjectionManager.createScreenCaptureIntent();
                    startActivityForResult(it, 1);
                }
            }
        }
        else
        {
            stopService(screenshot);
            Log.d(TAG, "Stop ScreenShotService");
            this.finish();
        }

//        if( FacebookOpen || LineToday || NewsApp || googleNews || LineMes || PTTApp || YoutubeOpen
//                || Instagram || MessengerOpen || Chrome)
//        {
//            if(FacebookOpen){
//                TriggerApp = "Facebook";
//            }
//            else if(LineToday){
//                TriggerApp = "LineToday";
//            }
//            else if(NewsApp){
//                TriggerApp = "NewsApp";
//            }
//            else if(googleNews){
//                TriggerApp = "googleNews";
//            }
//            else if(LineMes){
//                TriggerApp = "LineMes";
//            }
//            else if(YoutubeOpen){
//                TriggerApp = "Youtube";
//            }
//            else if(Instagram){
//                TriggerApp = "Instagram";
//            }
//            else if(PTTApp){
//                TriggerApp = "PTT";
//            }
//            else if(MessengerOpen){
//                TriggerApp = "Messenger";
//            }
//            else if(Chrome){
//                TriggerApp = "Chrome";
//            }
//
//            pref = getSharedPreferences("test", MODE_PRIVATE);
//            pref.edit()
//                    .putString("TriggerApp", TriggerApp)
//                    .apply();
//
//            CSVHelper.storeToCSV("AccessibilityDetect.csv", "ScreenCapture Activity Trigger app is " + TriggerApp);
//
//            MediaProjectionManager mediaProjectionManager = (MediaProjectionManager)
//                    getSystemService(Context.MEDIA_PROJECTION_SERVICE);
//
//            long lastid = pref.getLong("SessionID", 0);
////            long last_dialog_time = pref.getLong("last_dialog_time",0);
////            long now_dialog_time = System.currentTimeMillis();
////            boolean last_agree = pref.getBoolean("last_agree",true);
////            boolean pop_up = Dialog_pop(last_dialog_time, now_dialog_time);//有沒有超過interval
//            CSVHelper.storeToCSV("AccessibilityDetect.csv", "Session id is " + lastid);
//            if (lastid != -1) {
////                Last_Dialog_Time.put("Base", 0L);
////                long last_dialog_time = 0L;
////                long now_dialog_time = System.currentTimeMillis();
////                boolean last_agree = false;
////                if (Last_Dialog_Time != null) {
////                    if (Last_Dialog_Time.containsKey(TriggerApp)) {
////                        last_dialog_time = Last_Dialog_Time.get(TriggerApp);
////                    }
////                }
////                CSVHelper.storeToCSV("ScreenCaptureActivity.csv", "Last_Dialog_Time: " + Last_Dialog_Time);
////
////                Last_Agree.put("Base", false);
////                if (Last_Agree != null) {
////                    if (Last_Agree.containsKey(TriggerApp)) {
////                        last_agree = Last_Agree.get(TriggerApp);
////                    }
////                }
////                CSVHelper.storeToCSV("ScreenCaptureActivity.csv", "Last_Agree: " + Last_Agree);
//
////                boolean pop_up = Dialog_pop(last_dialog_time, now_dialog_time);//有沒有超過interval
////                CSVHelper.storeToCSV("ScreenCaptureActivity.csv", last_dialog_time + " | " + now_dialog_time + " | " + pop_up + " | " + last_agree);
////                Log.d(TAG, last_dialog_time + " | " + now_dialog_time + " | " + pop_up + " | " + last_agree);
////                if (isNoti) pop_up = true;
//
////                CSVHelper.storeToCSV("AccessibilityDetect.csv", "ScreenCapture Activity dialog pop up is " + pop_up);
//
//                try {
//
////                    if (!pop_up && last_agree) {//問過而且說同意(1)
////                        CSVHelper.storeToCSV("ScreenCaptureActivity.csv", "You have agreed");
////                        if (screenshotPermission != null) {
////                            ScreenCapture.setResultIntent(screenshotPermission);
////                        }
////                        GotoScreenShot();
////                        this.finish();
////                    }
//                    if (true) {//沒問過(0)
////                    CSVHelper.storeToCSV("ScreenCaptureActivity.csv", "You have not agreed");
//                        CSVHelper.storeToCSV("AccessibilityDetect.csv", "ScreenCapture Activity start activity for result");
//
//                        Intent it = mediaProjectionManager.createScreenCaptureIntent();
//                        startActivityForResult(it, 1);
//                    } else {//問過但不同意(2)
//                        CSVHelper.storeToCSV("ScreenCaptureActivity.csv", "hasPermission Screenshot but disagree");
//                        this.finish();
//                    }
////                    CSVHelper.storeToCSV("Dialog.csv","Dialog Time: " + System.currentTimeMillis() + " After agree dialog " + TriggerApp);
//                } catch (final RuntimeException ignored) {
//                    Log.d(TAG, "not hasPermission Screenshot exception");
//                    Intent it = mediaProjectionManager.createScreenCaptureIntent();
//                    startActivityForResult(it, 1);
//                }
//            }
//        }
//        else
//        {
//            stopService(screenshot);
//            Log.d(TAG, "Stop ScreenShotService");
//            this.finish();
//        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent it) {
        super.onActivityResult(requestCode, resultCode, it);
//        CSVHelper.storeToCSV("AccessibilityDetect.csv", "ScreenCaptureActivity onActivityResult time: " + (System.currentTimeMillis()));

        String Trigger = pref.getString("Trigger", "");
        Log.d(TAG, "New Trigger method: " + Trigger);
        if(!Trigger.equals("")) {
            pref.edit()
                    .putString("TriggerApp", Trigger)
                    .apply();
        }

        Log.d(TAG,"result code: " + resultCode);
//        Last_Dialog_Time.put(TriggerApp, System.currentTimeMillis());
//        pref.edit().putLong("last_dialog_time",System.currentTimeMillis()).apply();
        switch (requestCode) {
            case 0:
                if(resultCode == Activity.RESULT_CANCELED)
                {
//                    CSVHelper.storeToCSV("ScreenCaptureActivity.csv","RESULT_CANCELED");
//                    screenshotPermission = null;
                    Utils.cancel = true;
//                    Last_Agree.put(TriggerApp, false);
//                    CSVHelper.storeToCSV("AccessibilityDetect.csv", "ScreenCapture Activity cancel is set to " + Utils.cancel);
                    setAppTimesDatabase(true);
//                    pref.edit().putBoolean("last_agree",false).apply();
//                    TriggerApp_permission.put(TriggerApp, 2);
                }
            case 1:
                if (resultCode == RESULT_OK && it != null) {
//                    CSVHelper.storeToCSV("ScreenCaptureActivity.csv","RESULT_OK");

//                    setScreenshotPermission(it);
                    screenshotPermission = it;
                    ScreenCapture.setResultIntent(screenshotPermission);
//                    Last_Agree.put(TriggerApp, true);
                    setAppTimesDatabase(false);
//                    pref.edit().putBoolean("last_agree",true).apply();
                    GotoScreenShot();
                }
                else if(resultCode == Activity.RESULT_CANCELED)
                {
//                    CSVHelper.storeToCSV("ScreenCaptureActivity.csv","RESULT_CANCELED");
//                    screenshotPermission = null;
                    Utils.cancel = true;
//                    Last_Agree.put(TriggerApp, false);
//                    CSVHelper.storeToCSV("AccessibilityDetect.csv", "ScreenCapture Activity cancel is set to " + Utils.cancel);
                    setAppTimesDatabase(true);
//                    pref.edit().putBoolean("last_agree",false).apply();
//                    TriggerApp_permission.put(TriggerApp, 2);
                }
                break;
        }
        this.finish();
    }

    void setAppTimesDatabase(boolean result_cancel)
    {
        if(apptimesStreamGenerator!=null) {
            String Trigger = pref.getString("Trigger", "");
            if(result_cancel) {
                if (Trigger.equals("Facebook")) {
                    apptimesStreamGenerator.updateapptimes("FacebookScreen");
                } else if (Trigger.equals("LineToday")) {
                    apptimesStreamGenerator.updateapptimes("LinetodayScreen");
                } else if (Trigger.equals("NewsApp")) {
                    apptimesStreamGenerator.updateapptimes("NewsappScreen");
                } else if (Trigger.equals("googleNews")) {
                    apptimesStreamGenerator.updateapptimes("GooglenowScreen");
                } else if (Trigger.equals("Instagram")) {
                    apptimesStreamGenerator.updateapptimes("InstagramScreen");
                } else if (Trigger.equals("Youtube")) {
                    apptimesStreamGenerator.updateapptimes("YoutubeScreen");
                } else if (Trigger.equals("Chrome")) {
                    apptimesStreamGenerator.updateapptimes("ChromeScreen");
                }
            }
            else{
                if (Trigger.equals("Facebook")) {
                    apptimesStreamGenerator.updateapptimes("FacebookOpen");
                } else if (Trigger.equals("LineToday")) {
                    apptimesStreamGenerator.updateapptimes("LinetodayOpen");
                } else if (Trigger.equals("NewsApp")) {
                    apptimesStreamGenerator.updateapptimes("NewsappOpen");
                } else if (Trigger.equals("googleNews")) {
                    apptimesStreamGenerator.updateapptimes("GooglenowOpen");
                }  else if (Trigger.equals("Instagram")) {
                    apptimesStreamGenerator.updateapptimes("InstagramOpen");
                } else if (Trigger.equals("Youtube")) {
                    apptimesStreamGenerator.updateapptimes("YoutubeOpen");
                } else if (Trigger.equals("Chrome")) {
                    apptimesStreamGenerator.updateapptimes("ChromeOpen");
                }
            }
        }
    }
    protected void onDestroy()
    {
        super.onDestroy();
        Log.d(TAG,"onDestroy");
    }
    protected void setScreenshotPermission(final Intent permissionIntent) {
        Log.d(TAG, "setScreenshotPermission");
        screenshotPermission = permissionIntent;
//        String screenshotPermission_uri = permissionIntent.toUri(0);
//        Bundle temp = permissionIntent.getExtras();
//        for(String key : temp.keySet()){
//            object = temp.get(key);   //later parse it as per your required type
//            obj_key = key;
//            Log.d(TAG, key + " " + object.toString());
//            break;
//        }
//        String permission_extras = permissionIntent.getExtras().toString();
//        pref.edit().putString("permission_extras", permission_extras).apply();
//        pref.edit().putString("screenshotPermission", screenshotPermission_uri).apply();
        CSVHelper.storeToCSV("ScreenCaptureActivity.csv","setScreenshotPermission: " + screenshotPermission);
    }
    private void GotoScreenShot(){
        Log.d(TAG, "GotoScreenShot");
//        TriggerApp_permission.put(TriggerApp, 1);
        stopService(screenshot);
        startService(screenshot);
//        FirstscreenshotPermission = false;


//        sessionDataRecord = new SessionDataRecord(ScheduleAndSampleManager.getCurrentTimeString(), "NA", "Image", TriggerApp);
//        db.SessionDataRecordDao().insertAll(sessionDataRecord);
//        getSharedPreferences("test", MODE_PRIVATE).edit().putLong("SessionID", db.SessionDataRecordDao().getLastRecord().get_id()).apply();
        Log.d("BootCompleteReceiver", "In updateStreamManagerRunnable ");
    }
    public boolean Dialog_pop(long last, long now){
        if(now - last < agree_interval*60*60*1000){
            return false;
        }
        else{
            return true;
        }
    }
//    private boolean hasScreenshotPermission(String trigger){
//        boolean permission = false;
//        try {
//            if (TriggerApp_permission.get(trigger)){
//                permission = true;
//            }
//        }
//        catch(NullPointerException e){
//            e.printStackTrace();
//        }
//        Log.d(TAG, trigger + " hasScreenshotPermission: " + permission);
//        return permission;
//    }
}
