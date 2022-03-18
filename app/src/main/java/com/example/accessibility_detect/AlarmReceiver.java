package com.example.accessibility_detect;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.Button;

import androidx.core.app.NotificationCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.accessibility_detect.diarys.QuestionActivity_diary;
import com.example.accessibility_detect.questions.QuestionActivity;
import com.example.accessibility_detect.questions_test.QuestionActivity_test;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import labelingStudy.nctu.minuku.DBHelper.appDatabase;
import labelingStudy.nctu.minuku.Utilities.CSVHelper;
import labelingStudy.nctu.minuku.Utilities.ScheduleAndSampleManager;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.model.DataRecord.FinalAnswerDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.UserDataRecord;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.NOTIFICATION_SERVICE;
import static com.example.accessibility_detect.MyBackgroundService.CheckInterval;
import static com.example.accessibility_detect.MyBackgroundService.stampToDate;
import static com.example.accessibility_detect.NotificationHelper.RemindBuilder;
import static labelingStudy.nctu.minuku.Utilities.CSVHelper.CSV_Diary;
import static labelingStudy.nctu.minuku.config.Constants.DIARY_TEXT;
import static labelingStudy.nctu.minuku.config.Constants.DIARY_TITLE_CONTENT;
import static labelingStudy.nctu.minuku.config.Constants.MY_SOCKET_TIMEOUT_MS;
import static labelingStudy.nctu.minuku.config.Constants.QUESTIONNAIRE_TEXT;
import static labelingStudy.nctu.minuku.config.Constants.QUESTIONNAIRE_TITLE_CONTENT;
import static labelingStudy.nctu.minuku.config.Constants.REMINDER_TEXT;
import static labelingStudy.nctu.minuku.config.Constants.REMINDER_TITLE;
import static labelingStudy.nctu.minuku.config.Constants.URL_SAVE_ISALIVE;
import static labelingStudy.nctu.minuku.config.SharedVariables.CLEAN_DIARYNOTI;
import static labelingStudy.nctu.minuku.config.SharedVariables.CanFillDiary;
import static labelingStudy.nctu.minuku.config.SharedVariables.CanFillEsm;
import static labelingStudy.nctu.minuku.config.SharedVariables.DAIRY_ALARM;
import static labelingStudy.nctu.minuku.config.SharedVariables.ESMTEST_ALARM;
import static labelingStudy.nctu.minuku.config.SharedVariables.ESM_ALARM;
import static labelingStudy.nctu.minuku.config.SharedVariables.IS_ALIVE;
import static labelingStudy.nctu.minuku.config.SharedVariables.PHONE_STATE;
import static labelingStudy.nctu.minuku.config.SharedVariables.REMINDER;
import static labelingStudy.nctu.minuku.config.SharedVariables.ReadableTimeAddHour;
import static labelingStudy.nctu.minuku.config.SharedVariables.SCHEDULE_ALARM;
import static labelingStudy.nctu.minuku.config.SharedVariables.SERVICE_CHECKER;
import static labelingStudy.nctu.minuku.config.SharedVariables.getReadableTime;
import static labelingStudy.nctu.minuku.config.SharedVariables.isAlarmReceiverFirst;
import static labelingStudy.nctu.minuku.config.SharedVariables.isAlarmReceiverFirstDiary;
import static labelingStudy.nctu.minuku.config.SharedVariables.timeForQ;

//import io.fabric.sdk.android.Fabric;

public class AlarmReceiver extends BroadcastReceiver{
    private static String TAG = "AlarmReceiver";
    private static appDatabase db;
    private SharedPreferences pref;
    private long[] vibrate_effect = {100, 200, 300, 300, 500, 300, 300};
    NotificationManager ESM_manager;
    NotificationManager reminder_manager;
    NotificationManager diary_manager;
    NotificationManager mNotificationManager;
    Notification reminder;
    Context mContext;
    private static int REMIND_REQUEST = 1;
    private static int DIARY_REQUEST = 6;
    private static int DIARY_CLEAN_REQUEST = 7;
    public final int Interval = 60;
    public static final String CHANNEL_ID = "BackgroundServiceChannel";
    public static final String REMINDER_ID = "RemindChannel";
    public static final String DAIRYCHANNEL_ID = "DiaryChannel";
    public static final int ESM_ID = 1;
    public static final int REMIND_ID = 2;
    public static final int DIARY_ID = 4;
    public static final int GALLERYID = 200;
    private Button esm_button;
    private Button diary_button;
    private static UserDataRecord userRecord;
    public void onReceive(Context context, Intent intent)
    {
//        Fabric.with(context, new Crashlytics());
        pref = context.getSharedPreferences("test", MODE_PRIVATE);
        mNotificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        createNotificationChannel(context);
        String action = intent.getAction();
        db = appDatabase.getDatabase(context);
        userRecord = db.userDataRecordDao().getLastRecord();

        if(action.equals(ESM_ALARM))
        {
            Log.d(TAG, "Alarm Activate");
            SendESMnoti(context);//發問卷通知
        }
        if(action.equals(REMINDER))
        {
            Log.d(TAG, "Reminder");
            remindupload(context);//發上傳通知
        }
        if(action.equals(SERVICE_CHECKER))
        {
            Log.d(TAG, "Checker");
            isServiceAlive(context);
        }
        if(action.equals(PHONE_STATE))
        {
            CallPhoneStateChecker(context);
        }
        if(action.equals(IS_ALIVE))
        {
            CallIsAlive(context);
        }
        if(action.equals(SCHEDULE_ALARM)) {
            CallScheduleAlarm(context);
        }
        if(action.equals(DAIRY_ALARM)){
            SendDiaryNoti(context);
        }
        if(action.equals(CLEAN_DIARYNOTI)){
            RemoveDiaryNoti();
        }
        if(action.equals(ESMTEST_ALARM))
        {
            Log.d(TAG, "Alarm Activate");
            SendTestESMnoti(context);//發問卷通知
        }
    }
    private void createNotificationChannel(Context context) {
        Log.d(TAG, "createNotificationChannel");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Background Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            serviceChannel.setVibrationPattern(vibrate_effect);
            serviceChannel.enableVibration(true);

            ESM_manager =  (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            ESM_manager.createNotificationChannel(serviceChannel);

            NotificationChannel reminderChannel = new NotificationChannel(
                    REMINDER_ID,
                    "Remind Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            reminderChannel.setVibrationPattern(vibrate_effect);
            reminderChannel.enableVibration(true);

            reminder_manager =  (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            reminder_manager.createNotificationChannel(reminderChannel);

            NotificationChannel dairyChannel = new NotificationChannel(
                    DAIRYCHANNEL_ID,
                    "Dairy Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            dairyChannel.setVibrationPattern(vibrate_effect);
            dairyChannel.enableVibration(true);

            diary_manager =  (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            diary_manager.createNotificationChannel(dairyChannel);
        }
        else{
            ESM_manager =  (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            reminder_manager =  (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            diary_manager =  (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
    }

    public void RemoveDiaryNoti()
    {
        UserDataRecord userRecord = db.userDataRecordDao().getLastRecord();
        if(userRecord != null) {
            db.userDataRecordDao().updateCanFillDiary(userRecord.get_id(), false);
        }
        CSVHelper.storeToCSV(CSV_Diary, "remove diary noti");
        CanFillDiary = false;
        Log.d(TAG, "remove diary noti");
        mNotificationManager.cancel(DIARY_ID);
    }

    public void SendESMnoti(Context context)
    {
        timeForQ = getReadableTime(System.currentTimeMillis());
        UserDataRecord userRecord = db.userDataRecordDao().getLastRecord();
        if(userRecord != null) {
            db.userDataRecordDao().updateCanFillESM(userRecord.get_id(), true);
        }
        CanFillEsm = true;
        Log.d(TAG, "Send ESM");
        String ESMtime = getReadableTime(System.currentTimeMillis());//2020-04-11 14:29:35
        CSVHelper.storeToCSV("AlarmCreate.csv", "send ESM time: " + ESMtime);
        Log.d(TAG, "TTime" + ESMtime);
//        pref.edit().putLong("Now_Esm_Time", System.currentTimeMillis()).apply();
        pref.edit().putLong("ESM_send", System.currentTimeMillis()).apply();
//        if(pref.getBoolean("IsDestro", true) || isFinish.equals("1")){
//            pref.edit().putLong("ESM_SendDestroy", System.currentTimeMillis()).apply();
//        }
        pref.edit().putString("ESMtime", ESMtime).apply();//14:29:35
        pref.edit().putBoolean("NewEsm", true).apply();

        int EsmNum = 0;
        int questionnaireID = 0;
        String TodayResponseSet = "";
        String[] TodayResponse = {};
        String[] TotalEsmResponse = {};
        if(userRecord != null){
            long _id = userRecord.get_id();
            String Tesm_str = userRecord.getTotal_ESM_number();
            TodayResponseSet = userRecord.getESM_number();
            TodayResponse = TodayResponseSet.split(",");
            TotalEsmResponse = Tesm_str.split(",");
            EsmNum = Integer.parseInt(TotalEsmResponse[TotalEsmResponse.length - 1]);
            questionnaireID = userRecord.getquestionnaireID();
            Log.d("QuestionActivity","questionnaireID: " + questionnaireID);
            db.userDataRecordDao().updateQuestionnaireID(_id, questionnaireID + 1);
            pref.edit().putInt("ESMID", questionnaireID + 1).apply();
        }
        Log.d(TAG, "ESM number = " + EsmNum + " ESM response size = " + TotalEsmResponse.length);
//        int EsmNum = pref.getInt("Esm_Num", 0);//今天有發幾封
        EsmNum++;

        if(userRecord != null){
            String update_esm = "";
            TotalEsmResponse[TotalEsmResponse.length - 1] = String.valueOf(EsmNum);
            for(int i = 0; i < TotalEsmResponse.length; i++){
                if(!TotalEsmResponse[i].equals("")){
                    update_esm = update_esm + TotalEsmResponse[i] + ",";
                }
            }
            long _id = userRecord.get_id();
            db.userDataRecordDao().updateTotalESM(_id, update_esm);
            Log.d(TAG, "ESM number = " + update_esm);
        }
//        pref.edit().putInt("Esm_Num", EsmNum).apply();

        // 12/16紀錄ESM的第三種狀態(沒回答消失)
        int esmNum = 0;
        for(int i = 0; i < TodayResponse.length; i++){
            esmNum += Integer.parseInt(TodayResponse[i]);
        }

        int Total_ESM = 0;
        for(int i = 0; i < TotalEsmResponse.length; i++){
            Total_ESM += Integer.parseInt(TotalEsmResponse[i]);
        }

        int ESMID = pref.getInt("ESMID", 0);
        FinalAnswerDataRecord finalAnswerDataRecord = new FinalAnswerDataRecord();
        finalAnswerDataRecord.setGenerateTime(pref.getLong("ESM_send", 0));
        finalAnswerDataRecord.setRespondTime(0L); //點進問卷的時間
        finalAnswerDataRecord.setSubmitTime(0L);// onDestroy時間
        finalAnswerDataRecord.setisFinish("0");
        finalAnswerDataRecord.setQuesType("ESM");
        finalAnswerDataRecord.setreplyCount(String.valueOf(esmNum));
        finalAnswerDataRecord.settotalCount(String.valueOf(Total_ESM));
        finalAnswerDataRecord.setAnswerChoicePos("0");
        finalAnswerDataRecord.setAnswerChoiceState("1");
        finalAnswerDataRecord.setanswerId(String.valueOf(0));
        finalAnswerDataRecord.setdetectedTime(getReadableTime(System.currentTimeMillis()));
        finalAnswerDataRecord.setQuestionId("0");
        finalAnswerDataRecord.setsyncStatus(0);
        finalAnswerDataRecord.setRelatedId(ESMID);
        finalAnswerDataRecord.setAnswerChoice("");
        finalAnswerDataRecord.setcreationIme(new Date().getTime());
        db.finalAnswerDao().insertAll(finalAnswerDataRecord);
        /////////

        //pref.edit().putBoolean("Diary",false).apply();
        Intent notificationIntent = new Intent(context, QuestionActivity.class); //Intent(this, 點下去會跳到ESM class)
        notificationIntent.putExtra("json_questions", loadQuestionnaireJson("questions_example.json", context));
//        notificationIntent.putExtra("relatedIdForQ", RelatedID);
        notificationIntent.putExtra("notiId",String.valueOf(ESM_ID));

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, //類似公式的東西
                ESM_ID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder;
        Notification notification;
        builder = new NotificationCompat.Builder(context, CHANNEL_ID); //設定通知要有那些屬性
        builder .setContentTitle(QUESTIONNAIRE_TITLE_CONTENT) // 通知的Title
                .setContentText(QUESTIONNAIRE_TEXT + "(" + EsmNum + ")")                        //通知的內容
                .setSmallIcon(R.drawable.ic_stat_name)            //通知的icon
                .setContentIntent(pendingIntent)               //點下去會跳到ESM class
                //.setOngoing(true)                              //使用者滑不掉
                .setAutoCancel(true)                           //點擊之後通知消失
                .setVibrate(vibrate_effect)                   //震動模式
                .setTimeoutAfter(900000)                    //幾毫秒之後自動消失
                .setPriority(Notification.PRIORITY_MAX)
                .build();
        notification = builder.build();
        try{
            Thread.sleep(10);
        }catch(Exception e){

        }
        ESM_manager.notify(ESM_ID, notification);                  //發送通知
    }

    public void SendTestESMnoti(Context context)
    {
        timeForQ = getReadableTime(System.currentTimeMillis());
        UserDataRecord userRecord = db.userDataRecordDao().getLastRecord();
        if(userRecord != null) {
            db.userDataRecordDao().updateCanFillESM(userRecord.get_id(), true);
        }
        CanFillEsm = true;
        Log.d(TAG, "Send ESM");
        String ESMtime = getReadableTime(System.currentTimeMillis());//2020-04-11 14:29:35
        CSVHelper.storeToCSV("AlarmCreate.csv", "send ESM time: " + ESMtime);
        Log.d(TAG, "TTime" + ESMtime);
//        pref.edit().putLong("Now_Esm_Time", System.currentTimeMillis()).apply();
        pref.edit().putLong("ESM_send", System.currentTimeMillis()).apply();
//        if(pref.getBoolean("IsDestro", true) || isFinish.equals("1")){
//            pref.edit().putLong("ESM_SendDestroy", System.currentTimeMillis()).apply();
//        }
        pref.edit().putString("ESMtime", ESMtime).apply();//14:29:35
        pref.edit().putBoolean("NewEsm", true).apply();
        int questionnaireID = 0;
        int EsmNum = 0;
        String TodayResponseSet = "";
        String[] TodayResponse = {};
        String[] TotalEsmResponse = {};
        if(userRecord != null){
            long _id = userRecord.get_id();
            String Tesm_str = userRecord.getTotal_ESM_number();
            TodayResponseSet = userRecord.getESM_number();
            TodayResponse = TodayResponseSet.split(",");
            TotalEsmResponse = Tesm_str.split(",");
            EsmNum = Integer.parseInt(TotalEsmResponse[TotalEsmResponse.length - 1]);
//            questionnaireID = userRecord.getquestionnaireID();
//            Log.d("QuestionActivity","questionnaireID: " + questionnaireID);
//            db.userDataRecordDao().updateQuestionnaireID(_id, questionnaireID + 1);
//            pref.edit().putInt("ESMID", questionnaireID + 1).apply();
        }
//        Log.d(TAG, "ESM number = " + EsmNum + " ESM response size = " + TotalEsmResponse.length);
//        int EsmNum = pref.getInt("Esm_Num", 0);//今天有發幾封
//        EsmNum++;

//        if(userRecord != null){
//            String update_esm = "";
//            TotalEsmResponse[TotalEsmResponse.length - 1] = String.valueOf(EsmNum);
//            for(int i = 0; i < TotalEsmResponse.length; i++){
//                if(!TotalEsmResponse[i].equals("")){
//                    update_esm = update_esm + TotalEsmResponse[i] + ",";
//                }
//            }
//            long _id = userRecord.get_id();
//            db.userDataRecordDao().updateTotalESM(_id, update_esm);
//            Log.d(TAG, "ESM number = " + update_esm);
//        }
//        pref.edit().putInt("Esm_Num", EsmNum).apply();

//         12/16紀錄ESM的第三種狀態(沒回答消失)
        int esmNum = 0;
        for(int i = 0; i < TodayResponse.length; i++){
            esmNum += Integer.parseInt(TodayResponse[i]);
        }

        int Total_ESM = 0;
        for(int i = 0; i < TotalEsmResponse.length; i++){
            Total_ESM += Integer.parseInt(TotalEsmResponse[i]);
        }

//        int ESMID = pref.getInt("ESMID", 0);

        FinalAnswerDataRecord finalAnswerDataRecord = new FinalAnswerDataRecord();
        finalAnswerDataRecord.setGenerateTime(pref.getLong("ESM_send", 0));
        finalAnswerDataRecord.setRespondTime(0L); //點進問卷的時間
        finalAnswerDataRecord.setSubmitTime(0L);// onDestroy時間
        finalAnswerDataRecord.setisFinish("0");
        finalAnswerDataRecord.setQuesType("ESM");
        finalAnswerDataRecord.setreplyCount(String.valueOf(esmNum));
        finalAnswerDataRecord.settotalCount(String.valueOf(Total_ESM));
        finalAnswerDataRecord.setAnswerChoicePos("0");
        finalAnswerDataRecord.setAnswerChoiceState("1");
        finalAnswerDataRecord.setanswerId(String.valueOf(0));
        finalAnswerDataRecord.setdetectedTime(getReadableTime(System.currentTimeMillis()));
        finalAnswerDataRecord.setQuestionId("0");
        finalAnswerDataRecord.setsyncStatus(0);
        finalAnswerDataRecord.setRelatedId(-1);
        finalAnswerDataRecord.setAnswerChoice("");
        finalAnswerDataRecord.setcreationIme(new Date().getTime());
        db.finalAnswerDao().insertAll(finalAnswerDataRecord);
        /////////

        //pref.edit().putBoolean("Diary",false).apply();


        Intent notificationIntent = new Intent(context, QuestionActivity_test.class); //Intent(this, 點下去會跳到ESM class)
        notificationIntent.putExtra("json_questions", loadQuestionnaireJson("questions_example.json", context));
//        notificationIntent.putExtra("relatedIdForQ", RelatedID);
        notificationIntent.putExtra("notiId",String.valueOf(ESM_ID));

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, //類似公式的東西
                ESM_ID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder;
        Notification notification;
        builder = new NotificationCompat.Builder(context, CHANNEL_ID); //設定通知要有那些屬性
        builder .setContentTitle(QUESTIONNAIRE_TITLE_CONTENT) // 通知的Title
                .setContentText(QUESTIONNAIRE_TEXT + "(" + EsmNum + ")")                        //通知的內容
                .setSmallIcon(R.drawable.ic_stat_name)            //通知的icon
                .setContentIntent(pendingIntent)               //點下去會跳到ESM class
                //.setOngoing(true)                              //使用者滑不掉
                .setAutoCancel(true)                           //點擊之後通知消失
                .setVibrate(vibrate_effect)                    //震動模式
                .setTimeoutAfter(900000)                    //幾毫秒之後自動消失
                .setPriority(Notification.PRIORITY_MAX)
                .build();
        notification = builder.build();
//        try{
//            Thread.sleep(1000);
//        }catch(Exception e){
//
//        }
        ESM_manager.notify(ESM_ID, notification);                  //發送通知
    }

    public void remindupload(Context context)
    {
        Log.d(TAG, "Reminder Here");
        String remind = "Remember to upload yesterday's pictures!!";
        CSVHelper.storeToCSV("AlarmCreate.csv", "send reminder time: " + getReadableTime(System.currentTimeMillis()));

        Intent UploadServerIntent = new Intent(context, WiFireminder.class);
        PendingIntent UploadIntent = PendingIntent.getActivity(context, 300, UploadServerIntent, FLAG_UPDATE_CURRENT);

        PendingIntent Gallerypending;
        Intent GalleryIntent = context.getPackageManager().getLaunchIntentForPackage("com.google.android.apps.photos");
        if(GalleryIntent != null) {
            GalleryIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            Gallerypending = PendingIntent.getActivity(context, GALLERYID, GalleryIntent, 0);

            //Button
            NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.mipmap.ic_launcher, "相簿", Gallerypending).build();
            NotificationCompat.Action action2 = new NotificationCompat.Action.Builder(R.mipmap.ic_launcher, "上傳", UploadIntent).build();

            RemindBuilder = new NotificationCompat.Builder(context, REMINDER_ID)
                    .setContentTitle(REMINDER_TITLE)
                    .setContentText(REMINDER_TEXT)
                    .setContentIntent(Gallerypending)
                    .setSmallIcon(R.drawable.ic_stat_name)
                    .setAutoCancel(true)
                    .setOngoing(true)
                    .setVibrate(vibrate_effect)
                    .setPriority(Notification.PRIORITY_MAX)
                    .addAction(action2)
                    .addAction(action);
        }
        else{
            NotificationCompat.Action action2 = new NotificationCompat.Action.Builder(R.mipmap.ic_launcher, "上傳", UploadIntent).build();

            RemindBuilder = new NotificationCompat.Builder(context, REMINDER_ID)
                    .setContentTitle(REMINDER_TITLE)
                    .setContentText(REMINDER_TEXT)
                    .setSmallIcon(R.drawable.ic_stat_name)
                    .setAutoCancel(true)
                    .setOngoing(true)
                    .setVibrate(vibrate_effect)
                    .setPriority(Notification.PRIORITY_MAX)
                    .addAction(action2);
        }



        reminder = RemindBuilder.build();
        reminder_manager.notify(REMIND_ID, reminder);
    }

    public void SendDiaryNoti(Context context)
    {
        Log.d(TAG, "Diary Here");
        UserDataRecord userRecord = db.userDataRecordDao().getLastRecord();
        if(userRecord != null) {
            db.userDataRecordDao().updateCanFillDiary(userRecord.get_id(), true);
        }
        CanFillDiary = true;
        CSVHelper.storeToCSV("AlarmCreate.csv", "send diary time: " + getReadableTime(System.currentTimeMillis()));
//        try{
//            Thread.sleep(100);
//            diary_button = MainActivity.diary_button;
//            diary_button.setEnabled(true);
//        }catch(Exception e){
//            e.printStackTrace();
//        }
        int DiaryNum = 0;
        int DiaryClick = 0;
        int questionnaireID = 0, DiaryID = 0;
        if(userRecord != null){
            String Tdiary_str = userRecord.getTotalDiary_number();
            DiaryNum = Integer.parseInt(Tdiary_str);
            DiaryClick = Integer.valueOf(userRecord.getDiary_number());

            long app_start = userRecord.getApp_start();
            double day_interval = DateInterval(app_start);
            int Total_Diary = Integer.valueOf(Tdiary_str);

            Log.d(TAG, "Diary number = " + DiaryNum);

//        int DiaryNum = pref.getInt("Diary_Num", 0);
            if((int)(day_interval + 1) > Total_Diary)
                DiaryNum++;
//        pref.edit().putInt("Diary_Num", DiaryNum).apply();


            long _id = userRecord.get_id();
            db.userDataRecordDao().updateTotalDiary(_id, String.valueOf(DiaryNum));
            questionnaireID = userRecord.getquestionnaireID();
            Log.d("QuestionActivity","questionnaireID: " + questionnaireID);
            db.userDataRecordDao().updateQuestionnaireID(_id, questionnaireID + 1);
            pref.edit().putInt("DiaryID", questionnaireID + 1).apply();
        }

        pref.edit().putLong("Now_Diary_Time", System.currentTimeMillis()).apply();
        if(pref.getBoolean("IsDiaryDestroy", true)){
            pref.edit().putLong("Diary_SendDestroy", System.currentTimeMillis()).apply();
        }
        pref.edit().putBoolean("NewDiary", true).apply();
        DiaryID = pref.getInt("DiaryID", 0);
        //pref.edit().putBoolean("Diary",true).apply();

        FinalAnswerDataRecord finalAnswerDataRecord = new FinalAnswerDataRecord();
        finalAnswerDataRecord.setGenerateTime(pref.getLong("Now_Diary_Time", 0));
        finalAnswerDataRecord.setRespondTime(0L); //點進問卷的時間
        finalAnswerDataRecord.setSubmitTime(0L);// onDestroy時間
        finalAnswerDataRecord.setisFinish("0");
        finalAnswerDataRecord.setQuesType("Diary");
        finalAnswerDataRecord.setreplyCount(String.valueOf(DiaryClick));
        finalAnswerDataRecord.settotalCount(String.valueOf(DiaryNum));
        finalAnswerDataRecord.setAnswerChoicePos("0");
        finalAnswerDataRecord.setAnswerChoiceState("1");
        finalAnswerDataRecord.setanswerId(String.valueOf(0));
        finalAnswerDataRecord.setdetectedTime(getReadableTime(System.currentTimeMillis()));
        finalAnswerDataRecord.setQuestionId("0");
        finalAnswerDataRecord.setsyncStatus(0);
        finalAnswerDataRecord.setRelatedId(DiaryID);
        finalAnswerDataRecord.setAnswerChoice("");
        finalAnswerDataRecord.setcreationIme(new Date().getTime());
        db.finalAnswerDao().insertAll(finalAnswerDataRecord);
        /////////

//        Intent notificationIntent = new Intent(context, ChoosePicture.class); //Intent(this, 點下去會跳到ESM class)
        Intent notificationIntent = new Intent(context, QuestionActivity_diary.class); //Intent(this, 點下去會跳到ESM class)
        notificationIntent.putExtra("json_questions", loadQuestionnaireJson("diarys_example.json", context));
//        notificationIntent.putExtra("relatedIdForQ", RelatedID);
        notificationIntent.putExtra("notiId",String.valueOf(DIARY_ID));

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, //類似公式的東西
                DIARY_ID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification;
        notification = new NotificationCompat.Builder(context, DAIRYCHANNEL_ID) //設定通知要有那些屬性
                .setContentTitle(DIARY_TITLE_CONTENT) // 通知的Title
                .setContentText(DIARY_TEXT )                        //通知的內容
                .setSmallIcon(R.drawable.ic_stat_name)            //通知的icon
                .setContentIntent(pendingIntent)               //點下去會跳到ESM class
                .setOngoing(true)                              //使用者滑不掉
                .setAutoCancel(true)                           //點擊之後通知消失
                .setVibrate(vibrate_effect)                    //震動模式
                .setPriority(Notification.PRIORITY_MAX)
                .build();
        diary_manager.notify(DIARY_ID, notification);

        Calendar cal = Calendar.getInstance();
        String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
        String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));

//        pref.edit().putString("ShotDown_SendDiary", month + "/" + day).apply();
    }

    public void isServiceAlive(Context context)
    {
        Log.d(TAG, "Background service running: " + isMyServiceRunning(com.example.accessibility_detect.MyBackgroundService.class, context));
        if(!isMyServiceRunning(com.example.accessibility_detect.MyBackgroundService.class, context))
        {
//            CSVHelper.storeToCSV("AlarmCreate.csv", "isServiceAlive");
            mNotificationManager.cancel(1);
            mNotificationManager.cancel(2);
            mNotificationManager.cancel(3);
            mNotificationManager.cancel(4);
            mNotificationManager.cancel(9);
            if(userRecord != null){
                db.userDataRecordDao().updateIsKilled(userRecord.get_id(), true);
            }
//            userRecord.setIsKilled(true);
            Log.d(TAG, "is killed is true");
//            pref.edit().putBoolean("IsKilled", true).apply();
            context.startService(new Intent(context, MyBackgroundService.class));
        }
        add_ServiceChecker(context);
    }

    public void add_ServiceChecker(Context context)
    {
        Log.d(TAG, "add service checker");
        //Log.d(TAG, "remind time: " + String.valueOf(cal.get(Calendar.MONTH)) + "." + String.valueOf(cal.get(Calendar.DATE)) + " " + String.valueOf(cal.get(Calendar.HOUR_OF_DAY)) + ":" + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND));
        try
        {
            Thread.sleep(200);
        }
        catch(Exception e)
        {

        }
        boolean isdiary = false;
        boolean send = false;
        int MaxHour = 22;
        int MinHour = 9;
        long _id = 0;
        /* 3/17 */
        if(userRecord != null) {
            isdiary = userRecord.getDiaryClick();
            send = userRecord.getDiarySend();
//            send = pref.getBoolean("isDiarySend", false);
            MaxHour = Integer.parseInt(userRecord.getquestionnaire_endTime());
            MinHour = Integer.parseInt(userRecord.getquestionnaire_startTime());
            _id = userRecord.get_id();
        }
        Calendar cal = Calendar.getInstance();
        int Now_hour = cal.get(Calendar.HOUR_OF_DAY);
        if(MaxHour < MinHour){
            if(Now_hour >= (MaxHour - 1) && Now_hour < MinHour && !send){
                db.userDataRecordDao().updateDiarySend(_id, true);
//                pref.edit().putBoolean("isDiarySend", true).apply();
                add_reminder(context, cal);
                CSVHelper.storeToCSV("AlarmCreate.csv","reminder cal: " + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.DAY_OF_MONTH) + " " + cal.get(Calendar.HOUR_OF_DAY));
                add_dairy(context, cal);
                CSVHelper.storeToCSV("AlarmCreate.csv","diary cal: " + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.DAY_OF_MONTH) + " " + cal.get(Calendar.HOUR_OF_DAY));
            }
            else if((Now_hour >= (MaxHour - 1) && Now_hour < MinHour) && send && !isdiary){
                db.userDataRecordDao().updateDiarySend(_id, true);
//                pref.edit().putBoolean("isDiarySend", true).apply();
                UserDataRecord userRecord = db.userDataRecordDao().getLastRecord();
                if(userRecord != null) {
                    db.userDataRecordDao().updateCanFillDiary(userRecord.get_id(), true);
                }
                CanFillDiary = true;
                CSVHelper.storeToCSV("AlarmCreate.csv","complement diary cal: " + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.DAY_OF_MONTH) + " " + cal.get(Calendar.HOUR_OF_DAY));
            }
            if(Now_hour >= MinHour || Now_hour < (MaxHour - 1)){
                db.userDataRecordDao().updateDiarySend(_id, false);
//                pref.edit().putBoolean("isDiarySend", false).apply();
            }
        }
        if(MaxHour > MinHour) {
            if (Now_hour >= (MaxHour - 1) && !send) {
                db.userDataRecordDao().updateDiarySend(_id, true);
//                pref.edit().putBoolean("isDiarySend", true).apply();
                add_reminder(context, cal);
                CSVHelper.storeToCSV("AlarmCreate.csv","reminder cal: " + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.DAY_OF_MONTH) + " " + cal.get(Calendar.HOUR_OF_DAY));
                add_dairy(context, cal);
                CSVHelper.storeToCSV("AlarmCreate.csv", "diary cal: " + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.DAY_OF_MONTH) + " " + cal.get(Calendar.HOUR_OF_DAY));
            }
            else if((Now_hour >= (MaxHour - 1) || Now_hour < MinHour) && send && !isdiary){
                db.userDataRecordDao().updateDiarySend(_id, true);
//                pref.edit().putBoolean("isDiarySend", true).apply();
                UserDataRecord userRecord = db.userDataRecordDao().getLastRecord();
                if(userRecord != null) {
                    db.userDataRecordDao().updateCanFillDiary(userRecord.get_id(), true);
                }
                CanFillDiary = true;
                CSVHelper.storeToCSV("AlarmCreate.csv","complement diary cal: " + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.DAY_OF_MONTH) + " " + cal.get(Calendar.HOUR_OF_DAY));
            }
            if(Now_hour >= MinHour && Now_hour < (MaxHour - 1)){
                db.userDataRecordDao().updateDiarySend(_id, false);
//                pref.edit().putBoolean("isDiarySend", false).apply();
            }
        }
        CSVHelper.storeToCSV("AlarmCreate.csv","Diary send: " + send + " " + isdiary + " , " + MinHour + "," + MaxHour + "," + Now_hour);

        Intent intent = new Intent(context, AlarmReceiver.class);
        // 以日期字串組出不同的 category 以添加多個鬧鐘
        //intent.addCategory("ID." + String.valueOf(cal.get(Calendar.MONTH)) + "." + String.valueOf(cal.get(Calendar.DATE)) + "-" + String.valueOf((cal.get(Calendar.HOUR_OF_DAY) )) + "." + String.valueOf(cal.get(Calendar.MINUTE)) + "." + String.valueOf(cal.get(Calendar.SECOND)));
        //String AlarmTimeTag = "Alarmtime " + String.valueOf(cal.get(Calendar.HOUR_OF_DAY)) + ":" + String.valueOf(cal.get(Calendar.MINUTE)) + ":" + String.valueOf(cal.get(Calendar.SECOND));

        intent.setAction(SERVICE_CHECKER);
        //intent.putExtra("time", AlarmTimeTag);

        PendingIntent pi = PendingIntent.getBroadcast(context, 20, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        long time_fired = System.currentTimeMillis() + CheckInterval*1000*60;
        Log.d(TAG, "Service Checker fire time: " + stampToDate(time_fired));
        CSVHelper.storeToCSV("AlarmCreate.csv", "ServiceChecker alarm time: " + stampToDate(time_fired));
        am.setExact(AlarmManager.RTC_WAKEUP, time_fired, pi);       //註冊鬧鐘
    }

    private boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void CallPhoneStateChecker(Context context)
    {
        CSVHelper.storeToCSV("AlarmCreate.csv", "phone state checker time: " + getReadableTime(System.currentTimeMillis()));
        try {
            Log.d(TAG, "Phone state runnable: " + System.currentTimeMillis());
//            CSVHelper.storeToCSV("AlarmCreate.csv", "PhoneState Alarm time: " + stampToDate(System.currentTimeMillis()));

            Intent it = new Intent("PhoneStateChecker");
            context.sendBroadcast(it);
            //  CSVHelper.storeToCSV(CSVHelper.CSV_RUNNABLE_CHECK, "updateRun_isBackgroundServiceRunning ? "+isBackgroundServiceRunning);
            //  CSVHelper.storeToCSV(CSVHelper.CSV_RUNNABLE_CHECK, "updateRun_isBackgroundServiceRunning ? "+isBackgroundRunnableRunning);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG,"Cannot send broadcast to phone state checker");
            //   CSVHelper.storeToCSV(CSVHelper.CSV_RUNNABLE_CHECK, "Background, service update, stream, Exception");
            //   CSVHelper.storeToCSV(CSVHelper.CSV_RUNNABLE_CHECK, Utils.getStackTrace(e));
        }
        Log.d("BootCompleteReceiver", "In updateStreamManagerRunnable ");
    }

    private void CallIsAlive(Context context)
    {
        mContext = context;

        Log.d(TAG, "IsAlive runnable: " + System.currentTimeMillis());

        CSVHelper.storeToCSV(CSVHelper.CSV_CHECK_ISALIVE, "sendingIsAliveData");

        Constants.DEVICE_ID = pref.getString("UserID", Constants.DEVICE_ID);

        if(Constants.DEVICE_ID != null) {
            if (Constants.DEVICE_ID.length() > 5) {
                sendToAWS task = new sendToAWS();
                task.execute();
                //sendingIsAliveData();
            }
        }
        Log.d("BootCompleteReceiver", "In isAliveRunnable ");
    }
    class sendToAWS extends AsyncTask<Void,Void,Void> {

        @androidx.annotation.RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected Void doInBackground(Void ...voids) {
            sendingIsAliveData();
            // Log.d("BootCompleteReceiver","In Background createNotificationChannel");
            return null;
        }
    }
    private void sendingIsAliveData() {
        //making isAlive
        JSONObject dataInJson = new JSONObject();

        String TodayResponseSet = "";
        String TotalResponseSet = "";
        if(userRecord != null) {
            TodayResponseSet = userRecord.getESM_number();
            TotalResponseSet = userRecord.getTotal_ESM_number();
        }

//        String TodayResponseSet = pref.getString("TodayResponse", "");
        Log.d(TAG, TodayResponseSet);
        String[] TodayResponse = TodayResponseSet.split(",");

//        String TotalResponseSet = pref.getString("TotalResponse", "");
        Log.d(TAG, TotalResponseSet);
        String[] TotalResponse = TotalResponseSet.split(",");

        int esmTNum = 0;
        int esmNum = 0;

        Long appstart = 0L;
        if(userRecord != null){
            appstart = userRecord.getApp_start();
        }
//        String result = "";
        JSONArray multiRows = new JSONArray();
        List<String> Date = new ArrayList<>();
        for(int i = 0; i < TotalResponse.length; i++){
            Log.d(TAG, "i: " + i);
            JSONObject oneRow = new JSONObject();
            Date.add(DateConverter(appstart));
            appstart = ReadableTimeAddHour(appstart, 24);
            try{
                oneRow.put("Date", Date.get(i));
                oneRow.put("Today_Response", TodayResponse[i]);
                oneRow.put("Total_count", TotalResponse[i]);
                multiRows.put(oneRow);
            }catch(JSONException e){
                e.printStackTrace();
            }
            if(!TodayResponse[i].equals(""))
                esmNum += Integer.parseInt(TodayResponse[i]);
            if(!TotalResponse[i].equals(""))
                esmTNum += Integer.parseInt(TotalResponse[i]);
//            result = "已回答/總共: " + TodayResponse[i] + "/" + TotalResponse[i];
        }

        Log.d(TAG, "MultiRow: " + multiRows);
//        for(int i = 0; i < TotalResponse.length; i++){
//            if(!TodayResponse[i].equals(""))
//                esmNum += Integer.parseInt(TodayResponse[i]);
//            if(!TotalResponse[i].equals(""))
//                esmTNum += Integer.parseInt(TotalResponse[i]);
//        }
//        int ResponseSize = TotalResponse.length + 1;
//        int i = 0;
//        if( ResponseSize == 1)i = 0;
//        if( ResponseSize > 1)i = 1;
//        for(; i < ResponseSize; i++){
//            if(i == (ResponseSize - 1)){
//                String TodayRCount = String.valueOf(pref.getInt(todayMCountString, 0));
//                String TodayCount = String.valueOf(pref.getInt("Esm_Num", 0));
//                esmNum += Integer.parseInt(TodayRCount);
//                esmTNum += Integer.parseInt(TodayCount);
//            }
//            else {
//                Log.d(TAG, "In Else");
//                esmNum += Integer.parseInt(TodayResponse[i]);
//                esmTNum += Integer.parseInt(TotalResponse[i]);
//            }
//        }
        int d = 0;
        int DiaryClick = 0;
        if(userRecord != null) {
            d = Integer.valueOf(userRecord.getTotalDiary_number());
            DiaryClick = Integer.valueOf(userRecord.getDiary_number());
        }
//        int d = pref.getInt("Diary_Num", 0);//日誌通知數目
//        int DiaryClick = pref.getInt("Diary_click", 0);

        try {
            String currentTimeString = ScheduleAndSampleManager.getCurrentTimeString();
            String AccessibilityTime = pref.getString("AccessibilityTime", "0");
            String NotificationTime = pref.getString("NotificationTime", "0");
            dataInJson.put("Diary_response", DiaryClick + "/" + d);
            dataInJson.put("ESM_response", esmNum + "/" + esmTNum);
            dataInJson.put("ESM_Detail", multiRows);
            dataInJson.put("time", System.currentTimeMillis());
            dataInJson.put("timeString", currentTimeString);
            dataInJson.put("AccessibilityTime", AccessibilityTime);
            dataInJson.put("NotificationTime", NotificationTime);
            dataInJson.put("device_id", Constants.DEVICE_ID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "data in json: " + dataInJson);
        saveData(dataInJson);
        // Log.d("BootCompleteReceiver","In Background sendingIsAliveData");
    }

    public String DateConverter(Long time){
        Long year = time/1000000;
        time = time % 1000000;
        Long month = time/10000;
        time = time % 10000;
        Long day = time / 100;
        return year + "/" + month + "/" + day;
    }

    private void saveData(JSONObject multipleRows) {

        CSVHelper.storeToCSV(CSVHelper.CSV_CHECK_ISALIVE, "data : " + multipleRows.toString());
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, URL_SAVE_ISALIVE, multipleRows, new Response.Listener<JSONObject>() {
            @androidx.annotation.RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onResponse(JSONObject response) {

                if (response != null) {
                    String device_id = "";
                    try {
                        device_id = response.getString("device_id");
                        Log.d(TAG, "is alive id : " + device_id);
//                        String access_time = response.getString("Accessibility");
//                        Log.d(TAG,"access_time : "+access_time);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //  Log.d(TAG,"device Id : "+device_id.toString());
                    //  Log.d(TAG,"device Id constant : "+Constants.DEVICE_ID);

                }
//                //TODO: handle success

//                try {
//                    JSONObject obj = response;
//                    Log.d(TAG," repsonse : "+response.toString());
//
//                    if (obj.getString("error")=="false") {  //沒有錯誤
//                        //updating the status in sqlite
//                        Log.d(TAG," repsonse : error = false");
//
//                        db.accessibilityDataRecordDao().updateDataStatus(creationTime.get(0), DATA_SYNCED_WITH_SERVER);
//
//                        //sending the broadcast to refresh the list
//                        context.sendBroadcast(new Intent(DATA_SAVED_BROADCAST));
//                    }else{
//                        db.accessibilityDataRecordDao().updateDataStatus(creationTime.get(0), DATA_NOT_SYNCED_WITH_SERVER);
//
//                    }
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }

                // refreshAllContent(60*1000*10); // TODO 10min->1hr
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                //TODO: handle failure
            }
        });
        jsonRequest.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                0,  //0
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


        VolleySingleton.getInstance(mContext).addToRequestQueue(jsonRequest);
        // Log.d("BootCompleteReceiver","In Background saveData");
    }

    private double DateInterval(long time){
        int Year = (int)time/1000000;
        time = time % 1000000;

        int Month = (int)time/10000;
        time = time % 10000;

        int Day = (int)time/100;
        time = time % 100;

        int Hour = (int)time;

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, Year);
        cal.set(Calendar.MONTH, Month - 1);
        cal.set(Calendar.DAY_OF_MONTH, Day);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        Calendar cal2 = Calendar.getInstance();
        return ((cal2.getTimeInMillis() - cal.getTimeInMillis()) / (24 * 60 * 60 * 1000.0));
    }

    private double SetCalendar(int month, int day){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return cal.getTimeInMillis();
    }
    private void CallScheduleAlarm(Context context)
    {
        Log.d(TAG,"Alarm runnable: " + System.currentTimeMillis());
//        boolean iskilled = pref.getBoolean("IsKilled", false);
        boolean iskilled = userRecord.getIsKilled();
        boolean isupload = pref.getBoolean("UploadClick", false);
//        boolean isdiary = pref.getBoolean("DiaryClick", false);
        boolean isdiary = userRecord.getDiaryClick();

//        CanFillDiary = false;
        clean_dairyNoti(context);
//        mNotificationManager.cancel(DIARY_ID); QQ

        int MaxHour = Integer.parseInt(userRecord.getquestionnaire_endTime());
        int MinHour = Integer.parseInt(userRecord.getquestionnaire_startTime());
//        int diaryHR = Integer.parseInt(userRecord.getquestionnaire_endTime()) - 1;
//        int diaryHR = pref.getInt("MaxHour", 22) - 1;
        Log.d(TAG, "New diary time: " + (MaxHour - 1));

//        boolean send = pref.getBoolean("isDiarySend", false);
//        Calendar cal = Calendar.getInstance();
//        int Now_hour = cal.get(Calendar.HOUR_OF_DAY);
//        if(MaxHour > MinHour) {
//            if (Now_hour >= (MaxHour - 1) && send && !isdiary) {
//                FillDiary = true;
//            }
//        }

        if(!iskilled) {
            CSVHelper.storeToCSV("AlarmCreate.csv", "Normal reset alarm");
//            for(int i = 0; i < TriggerApp_array.length; i++){ //初始化permission(第一次)
//                TriggerApp_permission.put(TriggerApp_array[i], 0);
//            }
            deleteAllSyncData();

//            String TodayRCount = String.valueOf(pref.getInt(todayMCountString, 0));
//            String TodayCount = String.valueOf(pref.getInt("Esm_Num", 0));

            String TodayResponse = "";
            String TotalResponse = "";
            if(userRecord != null){
                CSVHelper.storeToCSV("AlarmCreate.csv", "Update Esm number");
                long app_start = userRecord.getApp_start();
                double day_interval = DateInterval(app_start);

                String esm_str = userRecord.getESM_number();
                String Tesm_str = userRecord.getTotal_ESM_number();

                String[] esmNum = esm_str.split(",");
                int l = 0;
                Log.d(TAG, "esmNum len: " + esmNum.length);
                if(esmNum.length == 1){
                    for(int i = 0; i < esmNum.length; i++){
                        Log.d(TAG, "esmNum: " + esmNum[i]);
                    }
                    if(!esmNum[0].equals("")){
                        Log.d(TAG, "!esmNum[0].equals(\"\")");
                        l = esmNum.length;
                    }
                }
                else{
                    l = esmNum.length;
                }
                Log.d(TAG, "day number: " + (int)(day_interval + 1) + " " + "Esm len: " + l);
                if((int)(day_interval + 1) != l){
                    TodayResponse = esm_str + "0,";
                    TotalResponse = Tesm_str + "0,";
                    long _id = userRecord.get_id();
                    db.userDataRecordDao().updateESM(_id, TodayResponse);
                    db.userDataRecordDao().updateTotalESM(_id, TotalResponse);
                }
            }

//            String TodayResponse = pref.getString("TodayResponse", "");
//            if(TodayResponse.equals(""))
//                TodayResponse = TodayResponse + ",";
//            else
//                TodayResponse = TodayResponse + TodayRCount + ",";
            Log.d(TAG, "TodayResponse: " + TodayResponse);
//            pref.edit().putString("TodayResponse", TodayResponse).apply();

//            String TotalResponse = pref.getString("TotalResponse", "");
//            if(TotalResponse.equals(""))
//                TotalResponse = TotalResponse + ",";
//            else
//                TotalResponse = TotalResponse  + TodayCount + ",";
            Log.d(TAG, "TotalResponse: " + TotalResponse);
//            pref.edit().putString("TotalResponse", TotalResponse).apply();

//            pref.edit().putInt(todayMCountString, 0).apply();//今天回答幾封
//            pref.edit().putInt("Esm_Num", 0).apply();//今天出現幾封
//            pref.edit().putInt("Esm_click", 0).apply();

            SharedPreferences pref = context.getSharedPreferences("URL", MODE_PRIVATE);
            Set<String> UrlSet = pref.getStringSet("UrlSet", new HashSet<String>());
            List<String> TitleAndWeb = new ArrayList<String>(UrlSet);
            TitleAndWeb.clear();
            UrlSet = new HashSet<String>(TitleAndWeb);
            pref.edit().putStringSet("UrlSet", UrlSet).apply();

            pref = context.getSharedPreferences("test", MODE_PRIVATE);
            Set<String> DiaryPicture = pref.getStringSet("DiaryPicture", new HashSet<String>());
            List<String> Diary_List = new ArrayList<String>(DiaryPicture);
            Diary_List.clear();
            DiaryPicture = new HashSet<String>(Diary_List);
            pref.edit().putStringSet("DiaryPicture", DiaryPicture).apply();
//            Calendar start_cal = Calendar.getInstance(); //取得時間
//            start_cal.set(Calendar.HOUR_OF_DAY, MinHour);
//            start_cal.set(Calendar.MINUTE, 0);
//            start_cal.set(Calendar.SECOND, 0);
//
//            Calendar end_cal = Calendar.getInstance(); //取得時間
//            end_cal.set(Calendar.HOUR_OF_DAY, MaxHour);
//            end_cal.set(Calendar.MINUTE, 0);
//            end_cal.set(Calendar.SECOND, 0);

            //ArrayList<Long> ESMtimeList = random_times(start_cal, end_cal, MaxESM, Interval, Constants.DEVICE_ID);

            // 4/17
        /*for (int i = 0; i < ESMnumber; i++) {
            Intent in = new Intent(context, AlarmReceiver.class);
            // 以日期字串組出不同的 category 以添加多個鬧鐘
            //intent.addCategory("ID." + ESMtimeList.get(i));
            Log.d(TAG, stampToDate(ESMtimeList.get(i)));
            CSVHelper.storeToCSV("AlarmCreate.csv", "ESM alarm time: " + stampToDate(ESMtimeList.get(i)));
            String AlarmTimeTag = "Alarmtime " + stampToDate(ESMtimeList.get(i));

            in.setAction(ESM_ALARM);
            //intent.putExtra("time", AlarmTimeTag);

            PendingIntent pi = PendingIntent.getBroadcast(context, i, in, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);

            am.setExact(AlarmManager.RTC_WAKEUP, ESMtimeList.get(i), pi);       //註冊鬧鐘
        }*/

//        Calendar remind_cal = Calendar.getInstance(); //取得時間
//            if(MaxHour < MinHour){
//                remind_cal.add(Calendar.DAY_OF_MONTH, 1);
//            }
//            remind_cal.set(Calendar.HOUR_OF_DAY, MaxHour - 1);
//            remind_cal.set(Calendar.MINUTE, 5);
//            remind_cal.set(Calendar.SECOND, 0);
////            add_reminder(context, remind_cal);
//            Log.d(TAG, "reminder cal: " + (remind_cal.get(Calendar.MONTH) + 1) + "/" + remind_cal.get(Calendar.DAY_OF_MONTH) + " " + remind_cal.get(Calendar.HOUR_OF_DAY));

            if(userRecord != null) {
                long app_start = userRecord.getApp_start();
                double day_interval = DateInterval(app_start);
                int Total_Diary = Integer.valueOf(userRecord.getTotalDiary_number());
                CSVHelper.storeToCSV("AlarmCreate.csv", "day number: " + (int)(day_interval + 1) + " " + "Diary number: " + Total_Diary + " is click: " + isdiary);
//                Log.d(TAG, "day number: " + (int)(day_interval + 1) + " " + "Diary number: " + Total_Diary + " is click: " + isdiary);
                if((int)(day_interval + 1) > Total_Diary){
//                    Calendar dairy_cal = Calendar.getInstance(); //取得時間
//                    if(MaxHour < MinHour){
//                        dairy_cal.add(Calendar.DAY_OF_MONTH, 1);
//                    }
//                    dairy_cal.set(Calendar.HOUR_OF_DAY, MaxHour - 1);
//                    dairy_cal.set(Calendar.MINUTE, 0);
//                    dairy_cal.set(Calendar.SECOND, 0);
//
//                    Calendar cal = Calendar.getInstance();
//                    int Now_hour = cal.get(Calendar.HOUR_OF_DAY);
//                    if(MinHour < MaxHour){
//                        if (Now_hour < 23 && Now_hour > 2) {
//
//                        } else {
////                            add_dairy(context, dairy_cal);
//                        }
//                    }
//                    else if(MaxHour < MinHour) {
//                        if (Now_hour < 23 && Now_hour > MaxHour) {
//
//                        } else {
////                            add_dairy(context, dairy_cal);
//                        }
//                    }
//                    add_dairy(context, dairy_cal);
//                    Log.d(TAG, "diary cal: " + (dairy_cal.get(Calendar.MONTH) + 1) + "/" + dairy_cal.get(Calendar.DAY_OF_MONTH) + " " + dairy_cal.get(Calendar.HOUR_OF_DAY));
                }
                else if(!isdiary){
                    CSVHelper.storeToCSV("AlarmCreate.csv", "In not killed, not clicked");
//                    Calendar dairy_cal = Calendar.getInstance(); //取得時間
//                    if(MaxHour < MinHour){
//                        dairy_cal.add(Calendar.DAY_OF_MONTH, 1);
//                    }
//                    dairy_cal.set(Calendar.HOUR_OF_DAY, MaxHour - 1);
//                    dairy_cal.set(Calendar.MINUTE, 0);
//                    dairy_cal.set(Calendar.SECOND, 0);
////                    add_dairy(context, dairy_cal);
//                    Log.d(TAG, "diary cal: " + (dairy_cal.get(Calendar.MONTH) + 1) + "/" + dairy_cal.get(Calendar.DAY_OF_MONTH) + " " + dairy_cal.get(Calendar.HOUR_OF_DAY));
                }
            }
            else{
                CSVHelper.storeToCSV("AlarmCreate.csv", "user record is null");
//            if(!isdiary)
//            {
//                Calendar dairy_cal = Calendar.getInstance(); //取得時間
//                if(MaxHour < MinHour){
//                    dairy_cal.add(Calendar.DAY_OF_MONTH, 1);
//                }
//                dairy_cal.set(Calendar.HOUR_OF_DAY, MaxHour - 1);
//                dairy_cal.set(Calendar.MINUTE, 0);
//                dairy_cal.set(Calendar.SECOND, 0);
//                add_dairy(context, dairy_cal);

//                if(userRecord != null){
//                    long _id = userRecord.get_id();
//                    db.userDataRecordDao().updateDiaryClick(_id, false);
//                }
            }
//            }

//            }
        }
        else{
            CSVHelper.storeToCSV("AlarmCreate.csv","Killed reset alarm");
            //判斷有沒有跨天
            if(!isupload){
//                Calendar remind_cal = Calendar.getInstance(); //取得時間
//                if(MaxHour < MinHour){
//                    remind_cal.add(Calendar.DAY_OF_MONTH, 1);
//                }
//                remind_cal.set(Calendar.HOUR_OF_DAY, MaxHour - 1);
//                remind_cal.set(Calendar.MINUTE, 5);
//                remind_cal.set(Calendar.SECOND, 0);
//                add_reminder(context, remind_cal);
            }
//            Calendar cal = Calendar.getInstance();
//            int Now_Hour = cal.get(Calendar.HOUR);
//            String ShotDown_SendDiary = pref.getString("ShotDown_SendDiary", "1/1");
//            String ShotDown_ClickDiary = pref.getString("ShotDown_ClickDiary", "1/1");
//            int SendDiary_Month = Integer.valueOf(ShotDown_SendDiary.split("/")[0]);
//            int SendDiary_Day = Integer.valueOf(ShotDown_SendDiary.split("/")[1]);
//            int ClickDiary_Month = Integer.valueOf(ShotDown_ClickDiary.split("/")[0]);
//            int ClickDiary_Day = Integer.valueOf(ShotDown_ClickDiary.split("/")[1]);
//
//            double SendDiary_timestamp = SetCalendar(SendDiary_Month - 1, SendDiary_Day) / (1000 * 1000);
//            double ClickDiary_timestamp = SetCalendar(ClickDiary_Month - 1, ClickDiary_Day) / (1000 * 1000);
//            double Now_timestamp = SetCalendar(cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)) / (1000 * 1000);
//
//            Log.d(TAG, "Send Diary: " + SendDiary_Month + "/" + SendDiary_Day + " " + SendDiary_timestamp);
//            Log.d(TAG, "Click Diary: " + ClickDiary_Month + "/" + ClickDiary_Day +  " " + ClickDiary_timestamp);
//            Log.d(TAG, "Now timestamp: " + Now_timestamp);
//
//            if(MinHour < MaxHour) {
//                if ((SendDiary_timestamp == Now_timestamp && ClickDiary_timestamp < SendDiary_timestamp)
//                        || (SendDiary_timestamp < Now_timestamp && ClickDiary_timestamp > SendDiary_timestamp)
//                        || (SendDiary_timestamp < Now_timestamp && ClickDiary_timestamp == SendDiary_timestamp)) {
//                    Calendar dairy_cal = Calendar.getInstance(); //取得時間
//                    dairy_cal.set(Calendar.HOUR_OF_DAY, MaxHour - 1);
//                    dairy_cal.set(Calendar.MINUTE, 0);
//                    dairy_cal.set(Calendar.SECOND, 0);
//                    add_dairy(context, dairy_cal);
//                }
//                else if (SendDiary_timestamp < Now_timestamp && ClickDiary_timestamp < SendDiary_timestamp) {
//                    Calendar dairy_cal2 = Calendar.getInstance(); //取得時間
//                    dairy_cal2.add(Calendar.DAY_OF_MONTH, -1);
//                    dairy_cal2.set(Calendar.HOUR_OF_DAY, MaxHour - 1);
//                    dairy_cal2.set(Calendar.MINUTE, 0);
//                    dairy_cal2.set(Calendar.SECOND, 0);
//                    add_dairy2(context, dairy_cal2);
//
//                    Calendar dairy_cal = Calendar.getInstance(); //取得時間
//                    dairy_cal.set(Calendar.HOUR_OF_DAY, MaxHour - 1);
//                    dairy_cal.set(Calendar.MINUTE, 0);
//                    dairy_cal.set(Calendar.SECOND, 0);
//                    add_dairy(context, dairy_cal);
//                }
//            }
//            else{ //3/16
//                if(SendDiary_timestamp == Now_timestamp && Now_Hour < MaxHour){
//                    Calendar dairy_cal = Calendar.getInstance(); //取得時間
//                    dairy_cal.set(Calendar.HOUR_OF_DAY, MaxHour - 1);
//                    dairy_cal.set(Calendar.MINUTE, 0);
//                    dairy_cal.set(Calendar.SECOND, 0);
//                    add_dairy(context, dairy_cal);
//
//                    Calendar dairy_cal2 = Calendar.getInstance(); //取得時間
//                    if (MaxHour < MinHour) {
//                        dairy_cal2.add(Calendar.DAY_OF_MONTH, 1);
//                    }
//                    dairy_cal2.set(Calendar.HOUR_OF_DAY, MaxHour - 1);
//                    dairy_cal2.set(Calendar.MINUTE, 0);
//                    dairy_cal2.set(Calendar.SECOND, 0);
//                    add_dairy2(context, dairy_cal2);
//                }
//                else if(SendDiary_timestamp == Now_timestamp && ClickDiary_timestamp < SendDiary_timestamp && Now_Hour < MinHour){
//
//                }
//            }
            if(!isdiary){
//                Calendar dairy_cal = Calendar.getInstance(); //取得時間
//                if(MaxHour < MinHour){
//                    dairy_cal.add(Calendar.DAY_OF_MONTH, 1);
//                }
//                dairy_cal.set(Calendar.HOUR_OF_DAY, MaxHour - 1);
//                dairy_cal.set(Calendar.MINUTE, 0);
//                dairy_cal.set(Calendar.SECOND, 0);
//                add_dairy(context, dairy_cal);
            }
        }
        if(userRecord != null){
            db.userDataRecordDao().updateIsKilled(userRecord.get_id(), false);
        }
//        userRecord.setIsKilled(false);
        Log.d(TAG, "is killed is false");
//        pref.edit().putBoolean("IsKilled", false).apply();
    }
    public void deleteAllSyncData(){
        Log.d(TAG,"delete all sync data");
        db.accessibilityDataRecordDao().deleteSyncData(1);//
        db.AppTimesDataRecordDao().deleteSyncData(1);
        db.transportationModeDataRecordDao().deleteSyncData(1);
        db.locationDataRecordDao().deleteSyncData(1);//
        db.MyDataRecordDao().deleteSyncData(1);
        db.NewsDataRecordDao().deleteSyncData(1);
        db.activityRecognitionDataRecordDao().deleteSyncData(1);
        db.ringerDataRecordDao().deleteSyncData(1);
        db.batteryDataRecordDao().deleteSyncData(1);
        db.connectivityDataRecordDao().deleteSyncData(1);
        db.appUsageDataRecordDao().deleteSyncData(1);
        db.telephonyDataRecordDao().deleteSyncData(1);
        db.sensorDataRecordDao().deleteSyncData(1);
        db.notificationDataRecordDao().deleteSyncData(1);
        db.finalAnswerDao().deleteSyncData(1);//
//        db.SessionDataRecordDao().del
    }
    public static void add_reminder(Context context, Calendar cal) {
        Log.d(TAG, "remind time: " + String.valueOf(cal.get(Calendar.MONTH)) + "." + String.valueOf(cal.get(Calendar.DATE)) + " " + String.valueOf(cal.get(Calendar.HOUR_OF_DAY)) + ":" + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND));
        context.getSharedPreferences("test", MODE_PRIVATE).edit().putBoolean("UploadClick", false).apply();
        CSVHelper.storeToCSV("AlarmCreate.csv", "Reminder alarm time: " + stampToDate(cal.getTimeInMillis()));
        Intent intent = new Intent(context, AlarmReceiver.class);
        // 以日期字串組出不同的 category 以添加多個鬧鐘
        //intent.addCategory("ID." + String.valueOf(cal.get(Calendar.MONTH)) + "." + String.valueOf(cal.get(Calendar.DATE)) + "-" + String.valueOf((cal.get(Calendar.HOUR_OF_DAY) )) + "." + String.valueOf(cal.get(Calendar.MINUTE)) + "." + String.valueOf(cal.get(Calendar.SECOND)));
        //String AlarmTimeTag = "Alarmtime " + String.valueOf(cal.get(Calendar.HOUR_OF_DAY)) + ":" + String.valueOf(cal.get(Calendar.MINUTE)) + ":" + String.valueOf(cal.get(Calendar.SECOND));

        intent.setAction(REMINDER);
        //intent.putExtra("time", AlarmTimeTag);
        PendingIntent pi = null;
        if(isAlarmReceiverFirstDiary){
            pi = PendingIntent.getBroadcast(context, REMIND_REQUEST, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            isAlarmReceiverFirstDiary = false;
        }
        else{
            pi = PendingIntent.getBroadcast(context, (int)System.currentTimeMillis(), intent, PendingIntent.FLAG_CANCEL_CURRENT);
        }

        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        am.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);       //註冊鬧鐘
    }
    public static void cancel_reminder(Context context) {
//        Log.d(TAG, "diary time: " + String.valueOf(cal.get(Calendar.MONTH)) + "." + String.valueOf(cal.get(Calendar.DATE)) + " " + String.valueOf(cal.get(Calendar.HOUR_OF_DAY)) + ":" + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND));
        CSVHelper.storeToCSV("AlarmCreate.csv", "Cancel Reminder alarm time ");
        Log.d(TAG, "Cancel reminder alarm");
        Intent intent = new Intent(context, AlarmReceiver.class);
        // 以日期字串組出不同的 category 以添加多個鬧鐘
        //intent.addCategory("ID." + String.valueOf(cal.get(Calendar.MONTH)) + "." + String.valueOf(cal.get(Calendar.DATE)) + "-" + String.valueOf((cal.get(Calendar.HOUR_OF_DAY) )) + "." + String.valueOf(cal.get(Calendar.MINUTE)) + "." + String.valueOf(cal.get(Calendar.SECOND)));
        //String AlarmTimeTag = "Alarmtime " + String.valueOf(cal.get(Calendar.HOUR_OF_DAY)) + ":" + String.valueOf(cal.get(Calendar.MINUTE)) + ":" + String.valueOf(cal.get(Calendar.SECOND));

        intent.setAction(REMINDER);
        //intent.putExtra("time", AlarmTimeTag);

        PendingIntent pi = PendingIntent.getBroadcast(context, REMIND_REQUEST, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        am.cancel(pi);       //刪除鬧鐘
    }

    public static void clean_dairyNoti(Context context) {
        Log.d(TAG, "Add clean alarm");
//        Log.d(TAG, "diary time: " + String.valueOf(cal.get(Calendar.MONTH)) + "." + String.valueOf(cal.get(Calendar.DATE)) + " " + String.valueOf(cal.get(Calendar.HOUR_OF_DAY)) + ":" + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND));
        int MinHour = 9;
        if(userRecord != null)
        {
            MinHour = Integer.parseInt(userRecord.getquestionnaire_startTime());
        }
        Log.d(TAG, "clean hour: " + MinHour);

        Intent intent = new Intent(context, AlarmReceiver.class);
        // 以日期字串組出不同的 category 以添加多個鬧鐘
        //intent.addCategory("ID." + String.valueOf(cal.get(Calendar.MONTH)) + "." + String.valueOf(cal.get(Calendar.DATE)) + "-" + String.valueOf((cal.get(Calendar.HOUR_OF_DAY) )) + "." + String.valueOf(cal.get(Calendar.MINUTE)) + "." + String.valueOf(cal.get(Calendar.SECOND)));
        //String AlarmTimeTag = "Alarmtime " + String.valueOf(cal.get(Calendar.HOUR_OF_DAY)) + ":" + String.valueOf(cal.get(Calendar.MINUTE)) + ":" + String.valueOf(cal.get(Calendar.SECOND));

        Calendar cal = Calendar.getInstance(); //取得時間
        cal.set(Calendar.HOUR_OF_DAY, MinHour);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        Log.d(TAG, "clear_diary cal: " + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.DAY_OF_MONTH) + " " + cal.get(Calendar.HOUR_OF_DAY));

        intent.setAction(CLEAN_DIARYNOTI);
        //intent.putExtra("time", AlarmTimeTag);

        PendingIntent pi = PendingIntent.getBroadcast(context, DIARY_CLEAN_REQUEST, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
//        am.cancel(pi);
//        Log.d(TAG, "cancel alarm");
        am.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);       //註冊鬧鐘
    }

    public static void add_dairy(Context context, Calendar cal) {
        Log.d(TAG, "Add diary alarm");
        Log.d(TAG, "diary time: " + String.valueOf(cal.get(Calendar.MONTH)) + "." + String.valueOf(cal.get(Calendar.DATE)) + " " + String.valueOf(cal.get(Calendar.HOUR_OF_DAY)) + ":" + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND));
        if(userRecord != null)
        {
            db.userDataRecordDao().updateDiaryClick(userRecord.get_id(), false);
        }
//        userRecord.setDiaryClick(false);
        Log.d(TAG, "Diary click is false");
//        context.getSharedPreferences("test", MODE_PRIVATE).edit().putBoolean("DiaryClick", false).apply();
        CSVHelper.storeToCSV("AlarmCreate.csv", "Diary alarm time: " + stampToDate(cal.getTimeInMillis()));
        Intent intent = new Intent(context, AlarmReceiver.class);
        // 以日期字串組出不同的 category 以添加多個鬧鐘
        //intent.addCategory("ID." + String.valueOf(cal.get(Calendar.MONTH)) + "." + String.valueOf(cal.get(Calendar.DATE)) + "-" + String.valueOf((cal.get(Calendar.HOUR_OF_DAY) )) + "." + String.valueOf(cal.get(Calendar.MINUTE)) + "." + String.valueOf(cal.get(Calendar.SECOND)));
        //String AlarmTimeTag = "Alarmtime " + String.valueOf(cal.get(Calendar.HOUR_OF_DAY)) + ":" + String.valueOf(cal.get(Calendar.MINUTE)) + ":" + String.valueOf(cal.get(Calendar.SECOND));
        intent.setAction(DAIRY_ALARM);
        //intent.putExtra("time", AlarmTimeTag);
        PendingIntent pi = null;
        if(true){
            Log.d(TAG, "create diary: " + DIARY_REQUEST);
            pi = PendingIntent.getBroadcast(context, DIARY_REQUEST, intent, PendingIntent.FLAG_CANCEL_CURRENT);
//            isAlarmReceiverFirst = false;
            AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            am.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);       //註冊鬧鐘
        }
        else{
//            Intent i = new Intent(context, AlarmReceiver.class);
//            i.setAction(DAIRY_ALARM);
//            Log.d(TAG, "delete request id: " + diary_requestID);
//            PendingIntent p = PendingIntent.getBroadcast(context, diary_requestID, i, PendingIntent.FLAG_CANCEL_CURRENT);
//            AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
//            am.cancel(p);       //刪除鬧鐘

//            int RequestId = (int)System.currentTimeMillis();
//            diary_requestID = RequestId;
//            Log.d(TAG, "create diary: " + RequestId);
//            pi = PendingIntent.getBroadcast(context, RequestId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
//            AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
//            am.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);       //註冊鬧鐘
//
//            Log.d(TAG, "create diary: " + RequestId + 1);
//            PendingIntent p = PendingIntent.getBroadcast(context, RequestId + 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
//            am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
//            am.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis() + 5000, p);
        }
    }
    public static void add_dairy2(Context context, Calendar cal) {
        Log.d(TAG, "Add diary alarm");
        Log.d(TAG, "diary time: " + String.valueOf(cal.get(Calendar.MONTH)) + "." + String.valueOf(cal.get(Calendar.DATE)) + " " + String.valueOf(cal.get(Calendar.HOUR_OF_DAY)) + ":" + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND));
        if(userRecord != null)
        {
            db.userDataRecordDao().updateDiaryClick(userRecord.get_id(), false);
        }
//        userRecord.setDiaryClick(false);
        Log.d(TAG, "Diary click is false");
//        context.getSharedPreferences("test", MODE_PRIVATE).edit().putBoolean("DiaryClick", false).apply();
        CSVHelper.storeToCSV("AlarmCreate.csv", "Diary alarm time: " + stampToDate(cal.getTimeInMillis()));
        Intent intent = new Intent(context, AlarmReceiver.class);
        // 以日期字串組出不同的 category 以添加多個鬧鐘
        //intent.addCategory("ID." + String.valueOf(cal.get(Calendar.MONTH)) + "." + String.valueOf(cal.get(Calendar.DATE)) + "-" + String.valueOf((cal.get(Calendar.HOUR_OF_DAY) )) + "." + String.valueOf(cal.get(Calendar.MINUTE)) + "." + String.valueOf(cal.get(Calendar.SECOND)));
        //String AlarmTimeTag = "Alarmtime " + String.valueOf(cal.get(Calendar.HOUR_OF_DAY)) + ":" + String.valueOf(cal.get(Calendar.MINUTE)) + ":" + String.valueOf(cal.get(Calendar.SECOND));

        intent.setAction(DAIRY_ALARM);
        //intent.putExtra("time", AlarmTimeTag);

        PendingIntent pi = null;
        if(isAlarmReceiverFirst){
            pi = PendingIntent.getBroadcast(context, DIARY_REQUEST, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            isAlarmReceiverFirst = false;
        }
        else{
            pi = PendingIntent.getBroadcast(context, (int)System.currentTimeMillis(), intent, PendingIntent.FLAG_CANCEL_CURRENT);
        }

        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        am.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);       //註冊鬧鐘
    }
    public static void cancel_dairy(Context context) {
//        Log.d(TAG, "diary time: " + String.valueOf(cal.get(Calendar.MONTH)) + "." + String.valueOf(cal.get(Calendar.DATE)) + " " + String.valueOf(cal.get(Calendar.HOUR_OF_DAY)) + ":" + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND));
        CSVHelper.storeToCSV("AlarmCreate.csv", "Cancel Diary alarm time ");
        Log.d(TAG, "Cancel diary alarm");
        Intent intent = new Intent(context, AlarmReceiver.class);
        // 以日期字串組出不同的 category 以添加多個鬧鐘
        //intent.addCategory("ID." + String.valueOf(cal.get(Calendar.MONTH)) + "." + String.valueOf(cal.get(Calendar.DATE)) + "-" + String.valueOf((cal.get(Calendar.HOUR_OF_DAY) )) + "." + String.valueOf(cal.get(Calendar.MINUTE)) + "." + String.valueOf(cal.get(Calendar.SECOND)));
        //String AlarmTimeTag = "Alarmtime " + String.valueOf(cal.get(Calendar.HOUR_OF_DAY)) + ":" + String.valueOf(cal.get(Calendar.MINUTE)) + ":" + String.valueOf(cal.get(Calendar.SECOND));

        intent.setAction(DAIRY_ALARM);
        //intent.putExtra("time", AlarmTimeTag);

        PendingIntent pi = PendingIntent.getBroadcast(context, DIARY_REQUEST, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        am.cancel(pi);       //刪除鬧鐘
    }
    public static ArrayList<Long> random_times(Calendar start, Calendar end, int amount, int interval_minutes, String random_seed) {
        //String seed = "hJYAe7cV";
        ArrayList<Long> randomList = new ArrayList<>();
        random_seed = String.format("%s-%d-%d", random_seed, start.get(Calendar.YEAR), start.get(Calendar.DAY_OF_YEAR));
        long random_seed_int = System.currentTimeMillis();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(random_seed.getBytes("UTF-8"));
            byte[] digest = md.digest();
            random_seed_int = (((((((digest[0] << 8 + digest[1]) << 8 + digest[2]) << 8 + digest[3]) << 8)
                    + digest[4] << 8) + digest[5] << 8) + digest[6] << 8) + digest[7];
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Random rng = new Random(random_seed_int);

        long totalInterval = end.getTimeInMillis() - start.getTimeInMillis();
        long minDifferenceMillis = interval_minutes * 60 * 1000;
        long effectiveInterval = totalInterval - minDifferenceMillis * (amount - 1);

        // Create random intervals without the minimum interval.
        while (randomList.size() < amount) {
            long random = start.getTimeInMillis() + (long) (rng.nextDouble() * effectiveInterval);
            randomList.add(random);
        }
        // Sort and add the minimum intervals between all events.
        Collections.sort(randomList);
        for (int i = 0; i < randomList.size(); i++) {
            randomList.set(i, randomList.get(i) + i * minDifferenceMillis);
        }

        return randomList;
    }
    private String loadQuestionnaireJson(String filename, Context context) {
        // Log.d("BootCompleteReceiver","In MainActivity loadQuestionnaireJson");
        try {
            InputStream is = context.getAssets().open(filename);
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
