package labelingStudy.nctu.minuku.service;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import labelingStudy.nctu.minuku.DBHelper.appDatabase;
import labelingStudy.nctu.minuku.R;
import labelingStudy.nctu.minuku.Utilities.CSVHelper;
import labelingStudy.nctu.minuku.Utilities.ScheduleAndSampleManager;
import labelingStudy.nctu.minuku.Utilities.Utils;
import labelingStudy.nctu.minuku.manager.MinukuStreamManager;
import labelingStudy.nctu.minuku.model.DataRecord.NewsDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.NotificationDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.ResponseDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.SessionDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.UserDataRecord;
import labelingStudy.nctu.minuku.receiver.AlarmReceiver;
import labelingStudy.nctu.minuku.receiver.NotificationHandleReceiver;
import labelingStudy.nctu.minuku.streamgenerator.NotificationStreamGenerator;
import labelingStudy.nctu.minukucore.exception.StreamNotFoundException;
import labelingStudy.nctu.minukucore.streamgenerator.StreamGenerator;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static labelingStudy.nctu.minuku.Utilities.CSVHelper.CSV_News;
import static labelingStudy.nctu.minuku.config.Constants.ACCESSIBILITY_STREAM_GENERATOR;
import static labelingStudy.nctu.minuku.config.Constants.CANCELRECORDING;
import static labelingStudy.nctu.minuku.config.Constants.CANCEL_RECORD;
import static labelingStudy.nctu.minuku.config.Constants.CONTACT_FILE;
import static labelingStudy.nctu.minuku.config.Constants.GOOGLE_CROWDSOURCE_APP;
import static labelingStudy.nctu.minuku.config.Constants.GOOGLE_LOCAL_GUIDES;
import static labelingStudy.nctu.minuku.config.Constants.PICTURE_DIRECTORY_PATH;
import static labelingStudy.nctu.minuku.config.Constants.QUESTIONNAIRE_CHANNEL_ID;
import static labelingStudy.nctu.minuku.config.Constants.QUESTIONNAIRE_TEXT;
import static labelingStudy.nctu.minuku.config.Constants.QUESTIONNAIRE_TITLE_CONTENT;
import static labelingStudy.nctu.minuku.config.Constants.RECORDING_NOTIFICATION_ID;
import static labelingStudy.nctu.minuku.config.Constants.RECORDING_NOW;
import static labelingStudy.nctu.minuku.config.Constants.RECORDING_ONGOING_CONTENT;
import static labelingStudy.nctu.minuku.config.Constants.RECORDING_STOP_CONTENT;
import static labelingStudy.nctu.minuku.config.Constants.RECORDING_TITLE;
import static labelingStudy.nctu.minuku.config.Constants.RECORDING_TITLE_CONTENT;
import static labelingStudy.nctu.minuku.config.Constants.REMINDER_NOTIFICATION_ID;
import static labelingStudy.nctu.minuku.config.Constants.REMINDER_TEXT;
import static labelingStudy.nctu.minuku.config.Constants.REMINDER_TITLE;
import static labelingStudy.nctu.minuku.config.Constants.SKIP_CONTRIBUTE;
import static labelingStudy.nctu.minuku.config.Constants.SKIP_CONTRIBUTE_ACTION;
import static labelingStudy.nctu.minuku.config.Constants.START_RECORDING;
import static labelingStudy.nctu.minuku.config.Constants.STOPRECORDING;
import static labelingStudy.nctu.minuku.config.Constants.STOP_RECORD;
import static labelingStudy.nctu.minuku.config.SharedVariables.DELETENOTIALARM;
import static labelingStudy.nctu.minuku.config.SharedVariables.NotiIdActiveSurvey;
import static labelingStudy.nctu.minuku.config.SharedVariables.NotiIdRandomMCNotiSurvey;
import static labelingStudy.nctu.minuku.config.SharedVariables.NotiIdRandomReminder;
import static labelingStudy.nctu.minuku.config.SharedVariables.NotiIdRecord;
import static labelingStudy.nctu.minuku.config.SharedVariables.SURVEYCREATEALARM;
import static labelingStudy.nctu.minuku.config.SharedVariables.appNameForQ;
import static labelingStudy.nctu.minuku.config.SharedVariables.canFillQuestionnaire;
import static labelingStudy.nctu.minuku.config.SharedVariables.extraForQ;
import static labelingStudy.nctu.minuku.config.SharedVariables.getReadableTime;
import static labelingStudy.nctu.minuku.config.SharedVariables.ifClickedNoti;
import static labelingStudy.nctu.minuku.config.SharedVariables.ifRecordingRightNow;
import static labelingStudy.nctu.minuku.config.SharedVariables.nhandle_or_dismiss;
import static labelingStudy.nctu.minuku.config.SharedVariables.notiPack;
import static labelingStudy.nctu.minuku.config.SharedVariables.notiPackForRandom;
import static labelingStudy.nctu.minuku.config.SharedVariables.notiPostedTimeForRandom;
import static labelingStudy.nctu.minuku.config.SharedVariables.notiReason;
import static labelingStudy.nctu.minuku.config.SharedVariables.notiSubText;
import static labelingStudy.nctu.minuku.config.SharedVariables.notiText;
import static labelingStudy.nctu.minuku.config.SharedVariables.notiTextForRandom;
import static labelingStudy.nctu.minuku.config.SharedVariables.notiTickerText;
import static labelingStudy.nctu.minuku.config.SharedVariables.notiTitle;
import static labelingStudy.nctu.minuku.config.SharedVariables.notiTitleForRandom;
import static labelingStudy.nctu.minuku.config.SharedVariables.questionaireType;
import static labelingStudy.nctu.minuku.config.SharedVariables.relatedId;
import static labelingStudy.nctu.minuku.config.SharedVariables.requestCodeCancelReminder;
import static labelingStudy.nctu.minuku.config.SharedVariables.requestCodeCancelSurvey;
import static labelingStudy.nctu.minuku.config.SharedVariables.requestCodeCreateSurvey;
import static labelingStudy.nctu.minuku.config.SharedVariables.requestCodeRecordActionRecording;
import static labelingStudy.nctu.minuku.config.SharedVariables.requestCodeRecordActionStartRecord;
import static labelingStudy.nctu.minuku.config.SharedVariables.requestCodeRecordActionStopRecording;
import static labelingStudy.nctu.minuku.config.SharedVariables.requestCodeReminderActioncrowdsource;
import static labelingStudy.nctu.minuku.config.SharedVariables.requestCodeReminderActionlocalGuides;
import static labelingStudy.nctu.minuku.config.SharedVariables.requestCodeReminderActionskipContribution;
import static labelingStudy.nctu.minuku.config.SharedVariables.requestCodeSurveyNoti;
import static labelingStudy.nctu.minuku.config.SharedVariables.timeForQ;
import static labelingStudy.nctu.minuku.service.MobileCrowdsourceRecognitionService.matchAppCode;
/**
 * Created by chiaenchiang on 18/11/2018.
 */

@SuppressLint("OverrideAbstract")
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class NotificationListenService extends NotificationListenerService {
    private static final String TAG = "NotificationListener";
    UserDataRecord userDataRecord;
    //private NotificationManager mManager;
    //    private String deviceId;
    private String title;
    private String text;
    private String subText;
    private String tickerText;
    public String pack;
    private String date;
    int notificationCode ;
    Long postedTime;
    String haveShownApp = "";
    int countDown;
    int countDownForNoti;
    String lastTimePostContent ="";
    String lastTimeRemovedContent = "";
    int noti_id;
    private appDatabase db;
    private SessionDataRecord sessionDataRecord;
    private NewsDataRecord newsDataRecord;
    public static ArrayList<String> haveSentMCNoti = new ArrayList<>();
    public static int GALLERYID = 200;

    //    private String app;
//    private Boolean send_form;
//    private String  last_title;
//    private Boolean skip_form;
//    private Intent intent;

    // JSONObject dataInJson = new JSONObject();
    private static NotificationStreamGenerator notificationStreamGenerator;
    public NotificationListenService(NotificationStreamGenerator notiStreamGenerator){
        try {
            Log.d(TAG,"call notificationlistener service2");
            this.notificationStreamGenerator = (NotificationStreamGenerator) MinukuStreamManager.getInstance().getStreamGeneratorFor(NotificationDataRecord.class);
        } catch (StreamNotFoundException e) {
            this.notificationStreamGenerator = notiStreamGenerator;
            Log.d(TAG,"call notificationlistener service3");}
    }

    public NotificationListenService(){
        super();
    }



    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void setRandomNotiAndSentNoti(){
        StatusBarNotification[]sbn = this.getActiveNotifications();
        if(sbn!=null) {
//            for(StatusBarNotification s : sbn){
//                Log.d("checkRandomNoti","packageName : "+s.getPackageName());
//                Notification noti = s.getNotification();
//                Log.d("checkRandomNoti","title : "+noti.extras.get("android.title").toString());
//                Log.d("checkRandomNoti","text : "+noti.extras.get("android.text").toString());
//
//            }
            int minNum = 0;
            int maxNum = sbn.length - 1;
            int random = new Random().nextInt((maxNum - minNum) + 1) + minNum;
            Notification notification = sbn[random].getNotification();
            StatusBarNotification theMostCurrent = sbn[random];
            //JSONObject obj = new JSONObject();
            int i;
            for (i = 0 ; i < 200 ; i++){  //最多只找
               if(notification.extras.get("android.title") == null || notification.extras.get("android.text") ==null){
//                   CSVHelper.storeToCSV("randomAlarm.csv","title text null new random :"+random);
                   random = new Random().nextInt((maxNum - minNum) + 1) + minNum;
               }else if(!ifTargetApp(MobileCrowdsourceRecognitionService.matchAppCode(sbn[random].getPackageName()))){
//                   CSVHelper.storeToCSV("randomAlarm.csv","random not target pack : "+MobileCrowdsourceRecognitionService.matchAppCode(sbn[random].getPackageName()));
                   random = new Random().nextInt((maxNum - minNum) + 1) + minNum;
                 //  CSVHelper.storeToCSV("randomAlarm.csv","new random :"+random);
               }else
                   break;
                notification = sbn[random].getNotification();
                if(sbn[random].getPostTime()>theMostCurrent.getPostTime())
                    theMostCurrent = sbn[random];

            }
            if(i == 200)
                notification = theMostCurrent.getNotification();
            else
                notification = sbn[random].getNotification();

            notiTitleForRandom = notification.extras.get("android.title") != null? notification.extras.get("android.title").toString() :"";
            notiPackForRandom = sbn[random].getPackageName()!=null? sbn[random].getPackageName() : " ";
            notiTextForRandom = notification.extras.get("android.text")!=null? notification.extras.get("android.text").toString():"";
            notiPostedTimeForRandom = sbn[random].getPostTime();


//            try {
//                obj.put("randomAlarm_set_pack",notiPackForRandom);
//                obj.put("randomAlarm_set_title",notiTitleForRandom);
//                obj.put("randomAlarm_set_text ",notiTextForRandom);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            CSVHelper.storeToCSV("randomAlarm.csv",obj.toString());

        }else{
            Log.d("checkRandomNoti","null");
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Received start id ");
        db = appDatabase.getDatabase(getApplicationContext());
//        userDataRecord = db.userDataRecordDao().getLastRecord();
        // Crashlytics.log(Log.INFO, "SilentModeService", "Requested new filter. StartId: " + startId + ": " + intent);

        return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Notification bind");

        return super.onBind(intent);
    }


    //
//    @Override
//    public StatusBarNotification[] getActiveNotifications() {
//        return NotificationListenService.this.getActiveNotifications();
//    }


    @SuppressLint("NewApi")
    public void getMCNoti(){
        Context context = getApplicationContext();
        StatusBarNotification[]sbn = this.getActiveNotifications();
        if(sbn!=null) {
            for(StatusBarNotification s : sbn){
                Log.d("checkRandomNoti","packageName : "+s.getPackageName());
                Notification noti = s.getNotification();
                String title = "";
                String text = "";
                if (noti.extras.get("android.title") != null ) {
                    title = noti.extras.get("android.title").toString();
                }
                if (noti.extras.get("android.text") != null ) {
                    text = noti.extras.get("android.text").toString();
                }
                if((matchAppCode(s.getPackageName())==18 && title.equals(QUESTIONNAIRE_TITLE_CONTENT))||(matchAppCode(s.getPackageName())==18 && title.equals(REMINDER_TITLE))){
                        boolean ifMCNoti = MobileCrowdsourceRecognitionService.ifNotiTitleMCTask(this, title + text);
                        if (ifMCNoti) {
                            // for random MC notification
                           // if (canSentNotiMCNoti) {
                                String label = s.getPostTime() + title;
                                if (!haveSentMCNoti.contains(label)) {
                                    //CSVHelper.storeToCSV("MCNoti_cancel.csv", "send because over15minutes : " + getReadableTime(s.getPostTime()));
                                    extraForQ = title + " " + text;
                                    if (!checkAnyNotiExist(context,NotiIdActiveSurvey)) {
                                       // CSVHelper.storeToCSV("MCNoti_cancel.csv", "NoOtherNoti : " + extraForQ);
                                        setAfterMinutesAlarm(context,MobileCrowdsourceRecognitionService.matchAppName(matchAppCode(s.getPackageName())),15,requestCodeCreateSurvey,SURVEYCREATEALARM,NotiIdRandomMCNotiSurvey);
                                        haveSentMCNoti.add(label);
                                    }
                                }


                        }

                }
            }
        }
    }





    @TargetApi(Build.VERSION_CODES.KITKAT)
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public boolean checkIfReminderNeeded(){
        StatusBarNotification[]sbn = this.getActiveNotifications();
        if(sbn!=null) {
            for (StatusBarNotification s : sbn) {
                if(matchAppCode(s.getPackageName())==4||matchAppCode(s.getPackageName())==5){
                    Long now = new Date().getTime();
                    Notification noti = s.getNotification();
                    String title = "";
                    String text = "";
                    if (noti.extras.get("android.title") != null ) {
                        title = noti.extras.get("android.title").toString();
                    }
                    if (noti.extras.get("android.text") != null ) {
                        text = noti.extras.get("android.text").toString();
                    }
                    boolean ifMCNoti = MobileCrowdsourceRecognitionService.ifNotiTitleMCTask(this,title+text);
                    if(ifMCNoti) {
                        if ((now - s.getPostTime() < 60 * 1000 * 60) ) { //如果有小於60分鐘的MC notification 就不用在送reminder
                             return false;
                        }
                    }
                }
            }
        }
        return true;
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        getSharedPreferences("test", MODE_PRIVATE).edit().
                putString("NotificationTime", ScheduleAndSampleManager.getCurrentTimeString()).apply(); // 12/14

        Log.d(TAG, "in posted");
        //setRandomNotiAndSentNoti();
        //getMCNoti();

        //Log.d(TAG, "Notification received: " + sbn.getPackageName() + ":" + sbn.getNotification().tickerText);


        Notification notification = sbn.getNotification();


        try {
            title = notification.extras.get("android.title").toString();
        } catch (Exception e) {
            title = "";
        }
        try {
            text = notification.extras.get("android.text").toString();
        } catch (Exception e) {
            text = "";
        }

        try {
            subText = notification.extras.get("android.subText").toString();
        } catch (Exception e) {
            subText = "";
        }

        try {
            tickerText = notification.tickerText.toString();
        } catch (Exception e) {
            tickerText = "";
        }
        try {
            pack = sbn.getPackageName();
        } catch (Exception e) {
            pack = "";
        }
        String extra = "";
        for (String key : notification.extras.keySet()) {
            Object value = notification.extras.get(key);
            try {
               // Log.d(TAG, String.format("%s %s (%s)", key,
               //         value.toString(), value.getClass().getName()));
                extra += value.toString();
            } catch (Exception e) {

            }
        }
       // notificationCode = MobileCrowdsourceRecognitionService.matchAppCode(sbn.getPackageName());
      //  String postedNotiInfo = title + " " + text + " " + subText + " " + tickerText + " " + extra;
        Log.d(TAG, "Noti test: " + title + " " + text + " " + subText + " " + tickerText + " " + extra);
        String finalContent = title + " " + text + " " + subText + " " + tickerText;

        //去除括號造成的錯誤
        try {
            finalContent = finalContent.replaceAll("[(|)|*|/|?|\\[|\\]|{|}|+|\\-]", "");
        }
        catch (Exception e){
            finalContent = "";
        }
        Log.d(TAG, "final content: " + finalContent);
//        if(lastTimePostContent !=null){
//            if(finalContent.contains(lastTimePostContent))
//                finalContent = finalContent.replaceAll(lastTimePostContent,"");
//        }
        //可能這一行造成錯誤
        try {
            if (/*!lastTimePostContent.equals(finalContent) && */finalContent.length() != 0 && text.length() != 0) {
//                boolean alreadyInDB = false;
//                Cursor transCursor = null;
//                try {
//                    transCursor = db.notificationDataRecordDao().getUnsyncedDataAll(0);
//                    int rows = transCursor.getCount();
//                    if (rows != 0) {
//                        transCursor.moveToFirst();
//                        for (int i = 0; i < rows; i++) {
//                            String title_col = transCursor.getString(2);
//                            String n_text_col = transCursor.getString(3);
//                            if(title_col.equals(title) && n_text_col.equals(text)){
//                                alreadyInDB = true;
//                                break;
//                            }
//                        }
//                    }
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//                finally {
//                    if(transCursor != null){
//                        transCursor.close();
//                    }
//                }
                Resources res = getResources();
                String[] NewsPack = res.getStringArray(R.array.NewsPack);
                String[] eventText = res.getStringArray(R.array.NewsName);
//                CSVHelper.storeNotiCSV("NotificationService.csv", title, text, subText, tickerText, pack, MatchReason.POST);
                if (Arrays.asList(NewsPack).contains(pack)) {
                    String app_name = "";
                    for(int i = 0; i < NewsPack.length; i++){
                        if(NewsPack[i].equals(pack)){
                            app_name = eventText[i];
                        }
                    }
                    storeToNotificationDataRecord(title, text, subText, tickerText, app_name, MatchReason.POST, -1);
                    CSVHelper.storeUrlCSV(CSV_News, app_name, title + " " + text);
                    getSharedPreferences("test", MODE_PRIVATE).edit().putString("NotificationUrl", app_name + " " + title + " " + text).apply();
//                    UpdateAllStreamGenerator();
                    /*String dataType = "Title";
                sessionDataRecord = new SessionDataRecord(getReadableTime(new Date().getTime()), "NA", dataType);
                db.SessionDataRecordDao().insertAll(sessionDataRecord);

                long sessionID = db.SessionDataRecordDao().getLastRecord().get_id();

                String appName = pack;
                String fileName = CSV_News;

                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                String date = ScheduleAndSampleManager.getTimeString(System.currentTimeMillis(),df);
                String filePath = Environment.getExternalStorageDirectory() + "/Pictures/News_Consumption/" + date + "/" + fileName;
                String content = title + " " + text;
                newsDataRecord = new NewsDataRecord(sessionID, appName, fileName, filePath, content);
                db.NewsDataRecordDao().insertAll(newsDataRecord);*/
                }
                else if((pack.equals("com.facebook.orca") || pack.equals("jp.naver.line.android"))){
                    Resources resource = getResources();
                    String[] ChatRobot = resource.getStringArray(R.array.ChatRobot);
//                    if(!text.contains("傳達了") && !title.contains("傳達了")) {
//                        if (pack.equals("com.facebook.orca")) {
//                            app_test = "Messenger";
//                            int c = 0;
//                            for (int i = 0; i < text.length(); i++) {
//                                if (text.charAt(i) == '：') {
//                                    c++;
//                                }
//                                if (c >= 1) break;
//                            }
//                            if (c >= 1) { //群組
//                                Log.d(TAG, "This is messenger group");
//                                name_test = text.split("：")[0];
//                            } else {
//                                Log.d(TAG, "This is not messenger group");
//                                name_test = title;
//                            }
//                            Log.d(TAG, "Name is " + name_test);
//                        }
//                        else if (pack.equals("jp.naver.line.android")) {
//                            app_test = "Line";
//                            if (title.contains("：")) { //群組
//                                Log.d(TAG, "This is line group");
//                                name_test = title.split("：")[1];
//                            } else {
//                                Log.d(TAG, "This is not the line group");
//                                name_test = title;
//                            }
//                        }
//
//                        SenderID_test = GetSenderID(name_test);
//                    }
                    if(text.length() > 5) {
                        if (text.contains("http:") || text.contains("https:")) {
                            String app_test = "";
                            String name_test = "";
                            int SenderID_test = -1;
                            if (pack.equals("com.facebook.orca")) {
                                app_test = "Messenger";
                                int c = 0;
                                for (int i = 0; i < text.length(); i++) {
                                    if (text.charAt(i) == '：') {
                                        c++;
                                    }
                                    if (c >= 1) break;
                                }
                                if (c >= 1) { //群組
                                    Log.d(TAG, "This is messenger group");
                                    name_test = text.split("：")[0];
                                } else {
                                    Log.d(TAG, "This is not messenger group");
                                    name_test = title;
                                }
                                Log.d(TAG, "Name is " + name_test);
                            }
                            else if (pack.equals("jp.naver.line.android")) {
                                app_test = "Line";
                                if (title.contains("：")) { //群組
                                    Log.d(TAG, "This is line group");
                                    name_test = title.split("：")[1];
                                } else {
                                    Log.d(TAG, "This is not the line group");
                                    name_test = title;
                                }
                            }

//                            SenderID_test = GetSenderID(name_test);

                            storeToNotificationDataRecord(name_test, text, subText, tickerText, app_test, MatchReason.POST, -1);

                            CSVHelper.storeUrlCSV(CSV_News, app_test + " (notification)", name_test + " " + text);
                            getSharedPreferences("test", MODE_PRIVATE).edit().putString("NotificationUrl", app_test + "(notification)" + " " + name_test + " " + text).apply();//
//                            UpdateAllStreamGenerator();
//                            WriteToFile(text, title);
                        }
                        else if (Arrays.asList(ChatRobot).contains(title)) {
                            String app = "未知來源";
                            String name = "";
                            if(pack.equals("com.facebook.orca")){
                                app = "Messenger";
                                int c = 0;
                                for(int i = 0; i < text.length(); i++){
                                    if(text.charAt(i) == '：'){
                                        c++;
                                    }
                                    if(c >= 1)break;
                                }
                                if(c >= 1){ //群組
                                    Log.d(TAG, "This is messenger group");
                                    name = text.split("：")[0];
                                }
                                else{
                                    Log.d(TAG, "This is not messenger group");
                                    name = title;
                                }
                                Log.d(TAG, "Name is " + name);
                            }
                            else if(pack.equals("jp.naver.line.android")){
                                app = "Line";
                                if(title.contains("：")){ //群組
                                    name = title.split("：")[1];
                                }
                                else{
                                    name = title;
                                }
                            }

//                            int SenderID = GetSenderID(name);

                            storeToNotificationDataRecord(name, text, subText, tickerText, app, MatchReason.POST, -1);

                            CSVHelper.storeUrlCSV(CSV_News, app + " (notification)", name + " " + text);
                            getSharedPreferences("test", MODE_PRIVATE).edit().putString("NotificationUrl", app + "(notification)" + " " + name + " " + text).apply();
//                            UpdateAllStreamGenerator();
                        }
                    }
                    else if (Arrays.asList(ChatRobot).contains(title)) {
                        String app = "未知來源";
                        String name = "";
                        if(pack.equals("com.facebook.orca")){
                            app = "Messenger";
                            int c = 0;
                            for(int i = 0; i < text.length(); i++){
                                if(text.charAt(i) == '：'){
                                    c++;
                                }
                                if(c >= 1)break;
                            }
                            if(c >= 1){ //群組
                                Log.d(TAG, "This is messenger group");
                                name = text.split("：")[0];
                            }
                            else{
                                Log.d(TAG, "This is not messenger group");
                                name = title;
                            }
                            Log.d(TAG, "Name is " + name);
                        }
                        else if(pack.equals("jp.naver.line.android")){
                            app = "Line";
                            if(title.contains("：")){ //群組
                                name = title.split("：")[1];
                            }
                            else{
                                name = title;
                            }
                        }

//                        int SenderID = GetSenderID(name);

                        storeToNotificationDataRecord(name, text, subText, tickerText, app, MatchReason.POST, -1);

                        CSVHelper.storeUrlCSV(CSV_News, app + " (notification)", name + " " + text);
                        getSharedPreferences("test", MODE_PRIVATE).edit().putString("NotificationUrl", app + "(notification)" + " " + name + " " + text).apply();
//                        UpdateAllStreamGenerator();
                    }
//                    CSVHelper.storeUrlCSV(CSV_News, pack, title + " " + text);
                }
//                else if (pack.equals("com.example.accessibility_detect")) {
//                    storeToNotificationDataRecord(title, text, subText, tickerText, "NewsConsumption", MatchReason.POST, -1);
////                    CSVHelper.storeToCSV("NotiPost.csv", "Noti Post: " + title + " " + text);
//                }
                lastTimePostContent = finalContent;
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
//        if (notificationCode != 20 && notificationCode != 6 && notificationCode != 7 && notificationCode != 8 && notificationCode != 9 && notificationCode != 17 && notificationCode != 16)  // 去掉不想看的
//            notiList.addElement(notificationCode);
        // 判斷是否為map or mobile crowdosurce 且判斷是否螢幕亮著
//        if((text.contains("測試測試"))) {
//            createNotificationAsReminders(this);
//        }
//        if ((notificationCode == 4) || (notificationCode == 5)) {
//            Boolean is_mobile_crowdsource_task = MobileCrowdsourceRecognitionService.ifMobileCrowdsourceTask(this, postedNotiInfo);
//             if(is_mobile_crowdsource_task) {
////                 if(countDown == -1 ) {// 一開始
////
////                 }
//                 if(!alarmExist()) {
//                     NotiInfoForQ = title +"::"+ text+"::"+subText;
//                     notiPostedTimeForMC = System.currentTimeMillis();
//                     haveShownApp = matchAppName(notificationCode);
//                     setAlarm();
//                 }
////                repeatTask rep = new repeatTask();
////                rep.startRepeatingTask();
//                // checkifNoticeinTenMinutes
////                triggerNotifications(pack, "noti_true_user_false");
//            }
//        }
//        if((text.contains("測試測試"))) {
//            triggerNotifications(visitedApp,"" , 0,"");
//        }
////        if((text.contains("測試測試1"))) {
////            triggerNotifications(visitedApp,"" , 1,"");
////        }
//        if((text.contains("測試測試2"))) {
//            triggerNotifications(visitedApp,"" , 2," 地圖：準備好分享更多資訊了嗎？ 請在Google地圖上為 Go eat Tapas Dining 分享資訊");
//        }



            // }else{

            // }
//
//        }


//        Calendar c = Calendar.getInstance();
//        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        String formattedDate = df.format(c.getTime());






//        Intent resultIntent = new Intent(Intent.ACTION_VIEW);
//        try {
//            resultIntent.setData(Uri.parse("https://nctucommunication.qualtrics.com/jfe/form/SV_78KPI6cbgtRFHp3?app="+app+"&title="+ URLEncoder.encode(title, "UTF-8") +"&text="+URLEncoder.encode(text, "UTF-8")+"&created_at="+unixTime*1000+"&user="+deviceId+"&time="+URLEncoder.encode(formattedDate, "UTF-8")));
//        } catch (java.io.UnsupportedEncodingException e){
//            resultIntent.setData(Uri.parse("https://nctucommunication.qualtrics.com/jfe/form/SV_78KPI6cbgtRFHp3?app="+app+"&title="+title+"&text="+text+"&created_at="+unixTime*1000+"&user="+deviceId+"&time="+formattedDate));
//        }
//
//        Intent notificationIntent = new Intent(getApplicationContext(),  ResultActivity.class);
//        try{
//            notificationIntent.putExtra("URL", "https://nctucommunication.qualtrics.com/jfe/form/SV_78KPI6cbgtRFHp3?app="+app+"&title="+ URLEncoder.encode(title, "UTF-8") +"&text="+URLEncoder.encode(text, "UTF-8")+"&created_at="+unixTime*1000+"&user="+deviceId+"&time="+URLEncoder.encode(formattedDate, "UTF-8"));
//
//        } catch (java.io.UnsupportedEncodingException e){
//            notificationIntent.putExtra("URL", "https://nctucommunication.qualtrics.com/jfe/form/SV_78KPI6cbgtRFHp3?app="+app+"&title="+title+"&text="+text+"&created_at="+unixTime*1000+"&user="+deviceId+"&time="+formattedDate);
//        }
//        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
//                | Intent.FLAG_ACTIVITY_CLEAR_TASK);


//        Long last_form_notification_sent_time = getSharedPreferences("edu.nctu.minuku", MODE_PRIVATE)
//                .getLong("last_form_notification_sent_time", 1);
//
//
//
////        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
////                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
////
////        PendingIntent formIntent = PendingIntent.getActivity(this, UUID.randomUUID().hashCode(), notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
//
//        Intent snoozeIntent = new Intent(this, labelingStudy.nctu.minuku.receiver.SnoozeReceiver.class);
//        snoozeIntent.setAction("ACTION_SNOOZE");
//
//        PendingIntent btPendingIntent = PendingIntent.getBroadcast(this, UUID.randomUUID().hashCode(), snoozeIntent,0);
//
//
//        mManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
//
//
//        // notification channel
//        int notifyID = 1;
//        String CHANNEL_ID = "my_channel_01";// The id of the channel.
//        CharSequence name = "firstChannel";// The user-visible name of the channel.
//        int importance = NotificationManager.IMPORTANCE_HIGH;
//        @SuppressLint({"NewApi", "LocalSuppress"}) NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            mManager.createNotificationChannel(mChannel);
//        }
//
//
//
//
//
//        Log.d(TAG,"notificaitonCode : "+notificationCode);
//
//        if(notificationCode == InterceptedNotificationCode.OTHER_KIND_OF_NOTIFICATION){
//            Log.d(TAG,"inOtherKind");
//            if((text.contains("測試測試"))) {
//                pref.edit()
//                        .putLong("state_notification_sent_esm", System.currentTimeMillis() / 1000L)
//                        .apply();
////                try {
////                    SQLiteDatabase db = DBManager.getInstance().openDatabase();
////                    values.put(DBHelper.TIME, new Date().getTime());
////                    values.put(DBHelper.title_col, title);
////                    values.put(DBHelper.n_text_col, text);
////                    values.put(DBHelper.subText_col, subText);
////                    values.put(DBHelper.tickerText_col, tickerText);
////                    values.put(DBHelper.app_col, sbn.getPackageName());
////                    values.put(DBHelper.sendForm_col, Boolean.TRUE);
////                    values.put(DBHelper.longitude_col, (float)LocationStreamGenerator.longitude.get());
////                    values.put(DBHelper.latitude_col, (float)LocationStreamGenerator.latitude.get());
////
////                    db.insert(DBHelper.notification_table, null, values);
////
////                } catch (NullPointerException e) {
////                    e.printStackTrace();
//////                    Amplitude.getInstance().logEvent("SAVE_NOTIFICATION_FAILED");
////                } finally {
////                    values.clear();
////                    DBManager.getInstance().closeDatabase();
////                }
////                Amplitude.getInstance().logEvent("SUCCESS_SEND_FORM");
//                pref.edit()
//                        .putLong("last_form_notification_sent_time", unixTime)
//                        .apply();
//                String type = pref.getString("type","NA");
//                Log.d(TAG,"type : "+type);
//
//
//
//
//                Log.d(TAG,"ready to sent questionnaire");
//
//
//                Intent nIntent = new Intent(Intent.ACTION_VIEW);
//
//
//
//                app = "googleMap";
//                nIntent.setData(Uri.parse("https://nctucommunication.qualtrics.com/jfe/form/SV_ezVodMgyxCpbe7j?app="+app));
//                PendingIntent contentIntent = PendingIntent.getActivity(this, 0, nIntent, 0);
//// Create a notification and set the notification channel.
//                Notification noti = new Notification.Builder(this)
//                        .setContentTitle("New Message")
//                        .setContentText("請填寫問卷")
//                        .setSmallIcon(R.drawable.self_reflection)
//                       // .setWhen(System.currentTimeMillis()+5000)
//                        .setContentIntent(contentIntent)
//                        .setChannelId(CHANNEL_ID)
//                        .setAutoCancel(true)
//                        .setOngoing(true)
//
//                        .build();
//                mManager.notify(notifyID , noti);
//
//
//
//                new Thread(
//                        new Runnable() {
//                            @Override
//                            public void run() {
//                                try {
//                                    Thread.sleep(600*1000);
//                                } catch (InterruptedException e) {
//                                    Log.d(TAG, "sleep failure");
//                                }
//
//                                mManager.cancel(0);
//                            }
//                        }
//                ).start();
//
//                Handler h = new Handler();
//                long delayInMilliseconds = 600*1000;
//                h.postDelayed(new Runnable() {
//                    public void run() {
//                        mManager.cancel(0);
//                    }
//                }, delayInMilliseconds);
//            }
//
//
//
//        }
    }
    int GetSenderID(String name){
        int SenderID = -1;
        UserDataRecord userDataRecord = db.userDataRecordDao().getLastRecord();
        String Sender = "";
        if(userDataRecord != null){
            Sender = userDataRecord.getSenderList();
            ArrayList<String> SenderList = new ArrayList<String>(Arrays.asList(Sender.split(",")));

            int space_index = SenderList.indexOf("");
            if(space_index != -1) {
                SenderList.remove(space_index);
            }

            if(SenderList.contains(name)){
                Log.d(TAG, "Sender list contain " + name);
                SenderID = SenderList.indexOf(name);
            }
            else{
                Log.d(TAG, "Sender list didn't contain " + name);
                SenderList.add(name);
                SenderID = SenderList.size() - 1;
                StoreToTxt(name, String.valueOf(SenderID));
            }

            Log.d(TAG, "Sender ID: " + SenderID);
            Log.d(TAG, "Sender list: ");
            Log.d(TAG, SenderList.toString());
//            for(int i = 0; i < SenderList.size(); i++){
//                Log.d(TAG, SenderList.get(i));
//            }
            long _id = userDataRecord.get_id();
            String[] SenderArray = SenderList.toArray(new String[0]);

            StringBuilder SenderBuilder = new StringBuilder();
            for(int i = 0; i < SenderArray.length; i++){
                SenderBuilder.append(SenderArray[i] + ",");
            }
            Log.d(TAG, "Sender Builder: " + SenderBuilder.toString());
            db.userDataRecordDao().updateSenderList(_id, SenderBuilder.toString());
//            userDataRecord.setSenderList(SenderList);
        }
        return SenderID;
    }
    void StoreToTxt(String sender, String senderID){
        try {
            FileWriter fw = new FileWriter(Environment.getExternalStorageDirectory().getPath() + PICTURE_DIRECTORY_PATH + CONTACT_FILE, true);
            BufferedWriter bw = new BufferedWriter(fw); //將BufferedWeiter與FileWrite物件做連結
            bw.write(sender + " " + senderID);
            bw.newLine();
            bw.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    private void UpdateAllStreamGenerator(){
        for(StreamGenerator streamGenerator: MinukuStreamManager.mRegisteredStreamGenerators.values()) {
            Log.d(TAG, "Stream generator : " + streamGenerator.getClass() + " \n" +
                    "Update frequency: " + streamGenerator.getUpdateFrequency());
            if(streamGenerator.getUpdateFrequency() == -1) {
//                continue;
            }
            if(true) {
                Log.d(TAG, "Calling update stream generator for " + streamGenerator.getClass().toString());
                if(!streamGenerator.getClass().toString().equals(ACCESSIBILITY_STREAM_GENERATOR))
                {
                    Log.d(TAG, "In Filter");
                    try{
                        streamGenerator.updateStream();
                    }catch (Exception e){

                        //  CSVHelper.storeToCSV(CSVHelper.CSV_CHECK_SESSION, "Class : "+streamGenerator.getClass().getName());
                        //  CSVHelper.storeToCSV(CSVHelper.CSV_CHECK_SESSION, Utils.getStackTrace(e));
                    }
                }
            }
        }
    }
    private void WriteToFile(String Text, String name)
    {
        date = Utils.getTimeString(Utils.DATE_FORMAT_NOW_DAY);

        SharedPreferences pref = getSharedPreferences("URL", MODE_PRIVATE);
        Set<String> UrlSet = pref.getStringSet("UrlSet", new HashSet<String>());
        List<String> TitleAndWeb = new ArrayList<String>(UrlSet);

        {
            if (!TitleAndWeb.contains(Text)) {
                TitleAndWeb.add(Text);
                try {
                    UrlSet = new HashSet<String>(TitleAndWeb);
                    pref.edit()
                            .putStringSet("UrlSet", UrlSet)
                            .apply();
//                    File directory = new File(Environment.getExternalStorageDirectory().getPath() + PICTURE_DIRECTORY_PATH + date);

//                    if (!directory.exists() || !directory.isDirectory()) {
//                        directory.mkdirs();
//                    }

//                    String dataType = "Null";

                    //long sessionID = getSharedPreferences("test", MODE_PRIVATE).getLong("SessionID", 0);
//                    String fileName = CSV_News;

//                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
//                    String date = ScheduleAndSampleManager.getTimeString(System.currentTimeMillis(), df);
//                    String filePath = Environment.getExternalStorageDirectory() + PICTURE_DIRECTORY_PATH + date + "/" + fileName;
//                    String content = text;
//                    Log.d(TAG, "news data record: " + sessionID + " " + name + " " + filePath + " " + content);
//                    CSVHelper.storeUrlCSV(CSV_News, "Noti", content + " " + name);
                /*FileWriter fw = new FileWriter(Environment.getExternalStorageDirectory().getPath() + "PICTURE_DIRECTORY_PATH" + date + "/MesUrl.txt", true);
                BufferedWriter bw = new BufferedWriter(fw); //將BufferedWeiter與FileWrite物件做連結
                bw.write(Text);
                bw.newLine();
                bw.close();*/
                /*for(int i = 0; i < TitleAndWeb.size(); i++)
                {
                    Log.d(TAG, TitleAndWeb.get(i));
                }*/
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
//    public boolean alarmExist(){
//        Intent checkIfNotiAlarmIntent = new Intent(this, AlarmReceiver.class);
//        checkIfNotiAlarmIntent.setAction(SURVEYALARM);
//        boolean alarmUp = (PendingIntent.getBroadcast(this, 26,   // 11 12 13 14 15 16 17 18 19 ...21
//                checkIfNotiAlarmIntent,
//                PendingIntent.FLAG_NO_CREATE) != null);
//        if(alarmUp)return true;
//        else return false;
//    }

//    public void setAlarm(){
//        Calendar currentTime = Calendar.getInstance();
//        int currentHourIn24Format = currentTime.get(Calendar.HOUR_OF_DAY);
//        int currentMinInFormat = currentTime.get(Calendar.MINUTE);
//        if(currentHourIn24Format<22) {
//            if(currentMinInFormat+15<60){
//                currentMinInFormat = currentMinInFormat+15;
//            }else{
//                currentHourIn24Format+=1;
//                currentMinInFormat = currentMinInFormat+15-60;
//            }
//
//            Calendar when = Calendar.getInstance();
//            when.setTimeInMillis(System.currentTimeMillis());
//
//            when.set(Calendar.HOUR_OF_DAY, currentHourIn24Format);  // (8+0*2)8 (8+1*2)10 (8+2*2)12 (8+3*2)14 (8+4*2)16 (8+5*2)18 (8+6*2)10 (8+7*2)22
//            when.set(Calendar.MINUTE, currentMinInFormat);
//            when.set(Calendar.SECOND, 0);
//            when.set(Calendar.MILLISECOND, 0);
//            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//            Intent intent = new Intent(this, AlarmReceiver.class);
//            intent.setAction(MCNOTIALAEM);
//            PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 26,intent,PendingIntent.FLAG_UPDATE_CURRENT);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
//                am.setAlarmClock(new AlarmManager.AlarmClockInfo(when.getTimeInMillis(),alarmIntent),alarmIntent);
//            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
//                am.setExact(AlarmManager.RTC, when.getTimeInMillis(), alarmIntent);
//            else
//                am.set(AlarmManager.RTC, when.getTimeInMillis(), alarmIntent);
//
//        }
//
//    }

    public void storeToNotificationDataRecord(String title,String text,String subText,String tickerText,String pack,String reason, int SenderID){
        try {
            this.notificationStreamGenerator = (NotificationStreamGenerator) MinukuStreamManager.getInstance().getStreamGeneratorFor(NotificationDataRecord.class);
        } catch (StreamNotFoundException e) {
            e.printStackTrace();
        }

        try {
            notificationStreamGenerator.setNotificationDataRecord(title, text, subText, tickerText, pack, -1,reason, SenderID);
            notificationStreamGenerator.updateStream();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn, NotificationListenerService.RankingMap rankingMap, int reason) {
        String reasonNotiRemoved ="";
        String channleId = "";
        reasonNotiRemoved = checkWhichReason(reason);
        if(reasonNotiRemoved.equals(MatchReason.REASON_CLICK)){
            ifClickedNoti = true;
        }

        if(sbn != null) {
            Log.d(TAG, "Notification handle or dismiss: " + sbn.getPackageName() + ":" + sbn.getNotification().tickerText);
            notificationCode = MobileCrowdsourceRecognitionService.matchAppCode(sbn.getPackageName());
            channleId = sbn.getNotification().getChannelId();

            Resources res = getResources();
            String[] NewsPack = res.getStringArray(R.array.NewsPack);
            String[] eventText = res.getStringArray(R.array.NewsName);
            getRemovedNotiInfo(sbn);
//        CSVHelper.storeNotiCSV("NotificationService.csv", notiTitle, notiText, notiSubText, notiTickerText, notiPack, reasonNotiRemoved);
            if (Arrays.asList(NewsPack).contains(sbn.getPackageName()) || sbn.getPackageName().equals("com.example.accessibility_detect")
                    || notiPack.equals("com.facebook.orca") || notiPack.equals("jp.naver.line.android")) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    String finalContent = notiTitle + " " + text + " " + subText + " " + tickerText;

                    //去除括號造成的錯誤
                    try {
                        finalContent = finalContent.replaceAll("[(|)|*|/|?|\\[|\\]|{|}|+|\\-]", "");
                    } catch (Exception e) {
                        finalContent = "";
                    }
                    Log.d(TAG, "final content: " + finalContent);
//                if(lastTimeRemovedContent !=null){
//                    if(finalContent.contains(lastTimeRemovedContent)){
//                        try{
//                            finalContent = finalContent.replaceAll(lastTimeRemovedContent,"");
//                        }catch(PatternSyntaxException e){
//                            finalContent = finalContent;
//                        }
//                    }
//
//                }
                    if (/*!lastTimeRemovedContent.equals(finalContent) && */finalContent.length() != 0 && notiText.length() != 0) {
                        if (Arrays.asList(NewsPack).contains(notiPack)) {
                            String app_name = "";
                            for (int i = 0; i < NewsPack.length; i++) {
                                if (NewsPack[i].equals(notiPack)) {
                                    app_name = eventText[i];
                                }
                            }
                            storeToNotificationDataRecord(notiTitle, notiText, notiSubText, notiTickerText, app_name, reasonNotiRemoved, -1);
                            CSVHelper.storeToCSV("NotiCancel.csv", "Noti Remove: " + notiTitle + " " + notiText);
                        } else if ((notiPack.equals("com.facebook.orca") || notiPack.equals("jp.naver.line.android"))) {
                            Resources resource = getResources();
                            String[] ChatRobot = resource.getStringArray(R.array.ChatRobot);
                            if (notiText.length() > 5) {
                                if (notiText.contains("http:") || notiText.contains("https:")) {
                                    String app = "";
                                    String name = "";
                                    if (notiPack.equals("com.facebook.orca")) {
                                        app = "Messenger";
                                        int c = 0;
                                        for (int i = 0; i < notiText.length(); i++) {
                                            if (notiText.charAt(i) == '：') {
                                                c++;
                                            }
                                            if (c >= 1) break;
                                        }
                                        if (c >= 1) { //群組
                                            Log.d(TAG, "This is messenger group");
                                            name = notiText.split("：")[0];
                                        } else {
                                            Log.d(TAG, "This is not messenger group");
                                            name = notiTitle;
                                        }
                                        Log.d(TAG, "Name is " + name);
                                    } else if (notiPack.equals("jp.naver.line.android")) {
                                        app = "Line";
                                        if (notiTitle.contains("：")) { //群組
                                            Log.d(TAG, "This is line group");
                                            name = notiTitle.split("：")[1];
                                        } else {
                                            Log.d(TAG, "This is not line group");
                                            name = notiTitle;
                                        }
                                    }

//                                int SenderID = GetSenderID(name);
                                    storeToNotificationDataRecord(name, notiText, notiSubText, notiTickerText, app, reasonNotiRemoved, -1);

                                    CSVHelper.storeToCSV("NotiCancel.csv", "Noti Cancel: " + name + " " + notiText);
//                            WriteToFile(text, title);
                                } else if (Arrays.asList(ChatRobot).contains(notiTitle)) {
                                    String app = "未知來源";
                                    String name = "";
                                    if (notiPack.equals("com.facebook.orca")) {
                                        app = "Messenger";
                                        int c = 0;
                                        for (int i = 0; i < notiText.length(); i++) {
                                            if (notiText.charAt(i) == '：') {
                                                c++;
                                            }
                                            if (c >= 1) break;
                                        }
                                        if (c >= 1) { //群組
                                            Log.d(TAG, "This is messenger group");
                                            name = notiText.split("：")[0];
                                        } else {
                                            Log.d(TAG, "This is messenger group");
                                            name = notiTitle;
                                        }
                                        Log.d(TAG, "Name is " + name);
                                    } else if (notiPack.equals("jp.naver.line.android")) {
                                        app = "Line";
                                        if (notiTitle.contains("：")) { //群組
                                            name = notiTitle.split("：")[1];
                                        } else {
                                            name = notiTitle;
                                        }
                                    }

//                                int SenderID = GetSenderID(name);
                                    storeToNotificationDataRecord(name, notiText, notiSubText, notiTickerText, app, reasonNotiRemoved, -1);//存群組名還是人名?
                                    CSVHelper.storeToCSV("NotiCancel.csv", "Noti Cancel: " + name + " " + notiText);
                                }
                            } else if (Arrays.asList(ChatRobot).contains(notiTitle)) {
                                String app = "未知來源";
                                String name = "";
                                if (notiPack.equals("com.facebook.orca")) {
                                    app = "Messenger";
                                    int c = 0;
                                    for (int i = 0; i < notiText.length(); i++) {
                                        if (notiText.charAt(i) == '：') {
                                            c++;
                                        }
                                        if (c >= 1) break;
                                    }
                                    if (c >= 1) { //群組
                                        Log.d(TAG, "This is messenger group");
                                        name = notiText.split("：")[0];
                                    } else {
                                        Log.d(TAG, "This is messenger group");
                                        name = notiTitle;
                                    }
                                    Log.d(TAG, "Name is " + name);
                                } else if (notiPack.equals("jp.naver.line.android")) {
                                    app = "Line";
                                    if (notiTitle.contains("：")) { //群組
                                        name = notiTitle.split("：")[1];
                                    } else {
                                        name = notiTitle;
                                    }
                                }

//                            int SenderID = GetSenderID(name);
                                storeToNotificationDataRecord(name, notiText, notiSubText, notiTickerText, app, reasonNotiRemoved, -1);

                                CSVHelper.storeToCSV("NotiCancel.csv", "Noti Cancel: " + notiTitle + " " + notiText);
                            }
//                    CSVHelper.storeUrlCSV(CSV_News, pack, title + " " + text);
                        }
//                    else if (notiPack.equals("com.example.accessibility_detect")) {
//                        storeToNotificationDataRecord(notiTitle, notiText, notiSubText, notiTickerText, "NewsConsumption", reasonNotiRemoved, -1);
//                        CSVHelper.storeToCSV("NotiCancel.csv", "Noti Cancel: " + notiTitle + " " + notiText);
//                    }
                        lastTimeRemovedContent = finalContent;
                    }
                    notiReason = reasonNotiRemoved;
                }
                nhandle_or_dismiss = notificationCode;
            }
        /*else if(notificationCode == 9){ // 此app發出的notification
            String title =" ";
            try {
                title = sbn.getNotification().extras.get("android.title").toString();
            } catch (Exception e) {
                title = "";
            }
            if(channleId.equals(RECORDING_NOTIFICATION_ID)){
                if(title.equals(RECORDING_ONGOING_CONTENT)){ // 錄製一段時間 刪除通知
                    Intent stopRecordingIntent = new Intent(this, NotificationHandleReceiver.class);
                    stopRecordingIntent.setAction(STOP_RECORD);
                    sendBroadcast(stopRecordingIntent);
                }
                haveDeletedRecordingNoti = true;
            }
//            else if(channleId.equals(REMINDER_NOTIFICATION_ID)){
//
//            }

        }*/
            else {
                nhandle_or_dismiss = -1;
            }
        }

    }

    public boolean ifTargetApp(int noti){
        if(noti==18){
            return true;
        }else
            return false;
    }





    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String getRemovedNotiInfo(StatusBarNotification sbn){
        String title,text,subText,tickerText,pack = " ";
        try {
            title = sbn.getNotification().extras.get("android.title").toString();
        } catch (Exception e){
            title = "";
        }
        try {
            text = sbn.getNotification().extras.get("android.text").toString();
        } catch (Exception e){
            text = "";
        }

        try {
            subText = sbn.getNotification().extras.get("android.subText").toString();
        } catch (Exception e){
            subText = "";
        }

        try {
            tickerText = sbn.getNotification().tickerText.toString();
        } catch (Exception e){
            tickerText = "";
        }
        try {
            pack = sbn.getPackageName();
        } catch (Exception e){
            pack = "";
        }
        notiTitle = title;
        notiText = text;
        notiSubText = subText;
        notiTickerText = tickerText;
        notiPack = pack;




        String allString = title+" "+text+" "+subText+" "+tickerText+" "+pack;
        return allString;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static boolean checkAnyNotiExist(Context context, int noti_id){

        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        StatusBarNotification[] notifications =
                new StatusBarNotification[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            notifications = notificationManager.getActiveNotifications();
        }
        for (StatusBarNotification notification : notifications) {
            if (notification.getId() == noti_id) {
                Log.d(TAG, "checkAnyNotiExist true NotiIdActiveSurvey");
                return true;
            }
        }
        Log.d(TAG, "checkAnyNotiExist false");
        return false;
    }
//    public static boolean checkNotiExist(Context context , int noti_id){
//        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
//        StatusBarNotification[] notifications =
//                new StatusBarNotification[0];
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
//            notifications = notificationManager.getActiveNotifications();
//        }
//        for (StatusBarNotification notification : notifications) {
//            if(notification.getId() == noti_id){
//                return true;
//            }
//        }
//
//        return false;
//    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public boolean checkRecordingNotiExist(Context context){

        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        StatusBarNotification[] notifications =
                new StatusBarNotification[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            notifications = notificationManager.getActiveNotifications();
        }
        for (StatusBarNotification notification : notifications) {
            if (notification.getId() == NotiIdRecord) {
                return true;
            }
        }

        return false;
    }
    public boolean checkIfReminderExist(Context context){
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        StatusBarNotification[] notifications =
                new StatusBarNotification[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            notifications = notificationManager.getActiveNotifications();
        }
        for (StatusBarNotification notification : notifications) {
            if (notification.getId() == NotiIdRandomReminder) {
                return true;
            }
        }
        return false;
    }

    public Long leftTimeOfNoti(Context context , long expire_minutes, int which_noti_id){
        Long now = new Date().getTime();
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        StatusBarNotification[] notifications =
                new StatusBarNotification[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            notifications = notificationManager.getActiveNotifications();
        }
        for (StatusBarNotification notification : notifications) {
            int notiId = notification.getId();
            if (notiId == which_noti_id ) { //|| notiId== NotiIdRandomSurvey
                return expire_minutes*1000*60 - (now - notification.getPostTime());

            }
        }
        return Long.valueOf(0);
    }




    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public boolean ifRecordingTransit(Context context,String condition){

        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        StatusBarNotification[] notifications =
                new StatusBarNotification[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            notifications = notificationManager.getActiveNotifications();
        }
        for (StatusBarNotification notification : notifications) {
            if (notification.getId() == NotiIdRecord) {
                String title = notification.getNotification().extras.getString("android.title");
                if(title.equals(condition))
                    return true;
            }
        }
        return false;
    }

    public void createNotificationAsReminders(Context context){
        String id = REMINDER_NOTIFICATION_ID;
        NotificationManager notifManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent localGuidesIntent = new Intent();
        localGuidesIntent.setPackage("com.google.android.gms.maps");
        localGuidesIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        Intent crowdsourceIntent = context.getPackageManager().getLaunchIntentForPackage("com.google.android.apps.village.boond");
        if(crowdsourceIntent==null){
            crowdsourceIntent = new Intent(Intent.ACTION_VIEW);
            crowdsourceIntent.setData(Uri.parse("market://details?id=" + "com.google.android.apps.village.boond"));
        }
        //crowdsourceIntent.setPackage("com.google.android.apps.village.boond");
        crowdsourceIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        localGuidesIntent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/maps/contrib/"));
        localGuidesIntent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");

//        crowdsourceIntent = new Intent(android.content.Intent.ACTION_VIEW,
//                Uri.parse("https://crowdsource.google.com"));
        //crowdsourceIntent.setPackage("com.google.android.apps.village.boond");

        Intent  skipContributeIntent = new Intent(context, NotificationHandleReceiver.class);
        skipContributeIntent.setAction(SKIP_CONTRIBUTE);

        PendingIntent localGuidesPendingIntent = null;
        PendingIntent crowdsourcePendingIntent = null;
        PendingIntent skipContributePendingIntent = null;
        localGuidesPendingIntent =  PendingIntent.getActivity(context, requestCodeReminderActionlocalGuides, localGuidesIntent,  PendingIntent.FLAG_CANCEL_CURRENT);
        crowdsourcePendingIntent =  PendingIntent.getActivity(context, requestCodeReminderActioncrowdsource, crowdsourceIntent,  PendingIntent.FLAG_CANCEL_CURRENT);
        skipContributePendingIntent = PendingIntent.getBroadcast(context, requestCodeReminderActionskipContribution,
                skipContributeIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Builder builder;

        NotificationCompat.Action localGuidesAction =
                new NotificationCompat.Action.Builder(R.drawable.ic_radio_button_checked_black_24dp,
                        GOOGLE_LOCAL_GUIDES, localGuidesPendingIntent)
                        .build();

        NotificationCompat.Action crowdsourceAction =
                new NotificationCompat.Action.Builder(R.drawable.ic_highlight_off_black_24dp,
                        GOOGLE_CROWDSOURCE_APP, crowdsourcePendingIntent)
                        .build();

        NotificationCompat.Action skipCrowdsourceAction =
                new NotificationCompat.Action.Builder(R.drawable.ic_highlight_off_black_24dp,
                        SKIP_CONTRIBUTE_ACTION, skipContributePendingIntent)
                        .build();


        if (notifManager == null) {
            notifManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = notifManager.getNotificationChannel(id);
            if (mChannel == null) {
                mChannel = new NotificationChannel(id, REMINDER_TITLE, importance);
                mChannel.enableVibration(true);
                mChannel.setVibrationPattern(new long[] {0, 300});
                //mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                notifManager.createNotificationChannel(mChannel);
            }
            builder = new NotificationCompat.Builder(context, id);
            builder.setContentTitle(REMINDER_TITLE)
                    .setSmallIcon(R.drawable.hand_shake_noti)   // required
                    .setContentText(REMINDER_TEXT) // required
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setTicker(REMINDER_TITLE)
                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                    .setContentIntent(null)
                    .setAutoCancel(true)
                    .setOnlyAlertOnce(true) ;


        }
        else {
            builder = new NotificationCompat.Builder(context, id);
            builder.setContentTitle(REMINDER_TITLE)                            // required
                    .setSmallIcon(R.drawable.hand_shake_noti)   // required
                    .setContentText(REMINDER_TEXT) // required
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setTicker(REMINDER_TITLE)
                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                    .setContentIntent(null)
                    .setOnlyAlertOnce(true)
                    .setAutoCancel(true);
        }
        builder.setPriority(Notification.PRIORITY_HIGH);


        Log.d(TAG,"recording title : "+ REMINDER_TITLE);
        builder.addAction(localGuidesAction);
        builder.addAction(crowdsourceAction);
        builder.addAction(skipCrowdsourceAction);
        Notification notification = builder.build();

        notification.tickerView = null;
        Notification notificationBuilder = builder.build();
        notifManager.notify(NotiIdRandomReminder, notificationBuilder);

        extraForQ = REMINDER_TITLE + " " + REMINDER_TEXT;

        setAfterMinutesAlarm(context,"",30,requestCodeCancelReminder, DELETENOTIALARM,NotiIdRandomReminder);  // delete reminder


    }



    public void createNotification(Context context, String app, long time, int questionType, final int NOTIFY_ID, String title) {
//        final int NOTIFY_ID = 100; // ID of notification
        NotificationManager notifManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        String id = QUESTIONNAIRE_CHANNEL_ID;// default_channel_id
//        String title = QUESTIONNAIRE_TITLE_MC; // Default Channel
        String  aMessage = QUESTIONNAIRE_TITLE_CONTENT;
        noti_id = NOTIFY_ID;

        appNameForQ = app;
        questionaireType = questionType;
        canFillQuestionnaire = true;
        timeForQ = getReadableTime(time);;


        Intent intent = null;
        PendingIntent pendingIntent;
        NotificationCompat.Builder builder;
        try {
            intent = new Intent(context,Class.forName("mobilecrowdsourceStudy.nctu.minuku_2.questions.QuestionActivity"));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        intent.putExtra("json_questions", loadQuestionnaireJson(context,"questions_example.json"));
        intent.putExtra("notidId",noti_id);

        intent.putExtra("extraInfo",extraForQ);
        intent.putExtra("relatedIdForQ",relatedId);
        //pageRecord.clear();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = notifManager.getNotificationChannel(id);
            if (mChannel == null) {
                mChannel = new NotificationChannel(id, title, importance);
                mChannel.enableVibration(true);
                mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                notifManager.createNotificationChannel(mChannel);
            }
            builder = new NotificationCompat.Builder(context, id);

//            try {
//                intent = new Intent(context,Class.forName("mobilecrowdsourceStudy.nctu.minuku_2.MainActivity"));
//                Log.d("BootCompleteReceiver","In NotificationListener createNotification");
//            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
//               // Log.d("BootCompleteReceiver","In bootComplete BackgroundService is not ok");
//
//            }
            // intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            pendingIntent = PendingIntent.getActivity(context, requestCodeSurveyNoti, intent, 0);
            builder.setContentTitle(aMessage)                            // required
                    .setSmallIcon(R.drawable.hand_shake_noti)   // required
                    .setContentText(QUESTIONNAIRE_TEXT) // required
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setContentIntent(pendingIntent)
                    .setTicker(aMessage)
                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        }
        else {
            builder = new NotificationCompat.Builder(context, id);
//            try {
//                intent = new Intent(context,Class.forName("mobilecrowdsourceStudy.nctu.minuku_2.MainActivity"));
//            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
//            }
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            pendingIntent = PendingIntent.getActivity(context, requestCodeSurveyNoti, intent,  PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentTitle(aMessage)                            // required
                    .setSmallIcon(R.drawable.hand_shake_noti)   // required
                    .setContentText(QUESTIONNAIRE_TEXT) // required
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setContentIntent(pendingIntent)
                    .setTicker(aMessage)
                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                    .setPriority(Notification.PRIORITY_HIGH);
        }
        Notification notification = builder.build();
        notifManager.notify(NOTIFY_ID, notification);


        ResponseDataRecord responseDataRecord = new ResponseDataRecord(getReadableTime(new Date().getTime()),relatedId,questionType);
        appDatabase db = appDatabase.getDatabase(context);
        db.repsonseDataRecordDao().insertAll(responseDataRecord);
//        CSVHelper.storeToCSV("response.csv","original : "+responseDataRecord.toString());
        if(noti_id == NotiIdActiveSurvey){
            CSVHelper.storeToCSV("noti_cancel.csv","startNotiTime : "+getReadableTime(new Date().getTime()));
        }
//        else if(noti_id == NotiIdRandomSurvey) {
//            CSVHelper.storeToCSV("randomAlarm.csv","startNotiTime : "+getReadableTime(new Date().getTime()));
//        }
        else if(noti_id == NotiIdRandomMCNotiSurvey){
            CSVHelper.storeToCSV("MCNoti_cancel.csv","startNotiTime : "+getReadableTime(new Date().getTime()));
        }


//        CountDownTask countDownTask = new CountDownTask();
//        countDownTask.startRepeatingTask(responseDataRecord);
//        new Thread(
//                new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            Thread.sleep(600*1000);
//                        } catch (InterruptedException e) {
//                            Log.d(TAG, "sleep failure");
//                        }
//                        relatedId++;
//                        notifManager.cancel(noti_id);
//                        canFillQuestionnaire = false;
//                        CSVHelper.storeToCSV("wipeNoti.csv","selfCancelNotiTime : "+noti_id +getReadableTime(new Date().getTime()));
//
//                    }
//                }
//        ).start();
        setAfterMinutesAlarm(context,"",15,requestCodeCancelSurvey, DELETENOTIALARM,noti_id);

    }

    public void setAfterMinutesAlarm(Context context,String appName,int speciMinutes ,int requestCode, String action,int noti_id){
        Calendar rightNow = Calendar.getInstance();
        int hour = rightNow.get(Calendar.HOUR_OF_DAY);
        int minute = rightNow.get(Calendar.MINUTE);
        int target_hour = hour;
        int target_min ;
        if(minute + speciMinutes >= 60){
            target_hour+=1;
            target_min = minute + speciMinutes - 60;
        }else{
            target_min = minute + speciMinutes;
        }
        Calendar when = Calendar.getInstance();
        when.setTimeInMillis(System.currentTimeMillis());

        when.set(Calendar.HOUR_OF_DAY,target_hour);  // (8+0*2)8 (8+1*2)10 (8+2*2)12 (8+3*2)14 (8+4*2)16 (8+5*2)18 (8+6*2)10 (8+7*2)22
        when.set(Calendar.MINUTE, target_min);
        when.set(Calendar.SECOND, 0);
        when.set(Calendar.MILLISECOND, 0);
        if(noti_id == NotiIdActiveSurvey){
            CSVHelper.storeToCSV("noti_cancel.csv", "cancel target_hour" + target_hour);
            CSVHelper.storeToCSV("noti_cancel.csv", "cancel target_min" + target_min);
        }
//        else if(noti_id == NotiIdRandomSurvey) {
//            CSVHelper.storeToCSV("randomAlarm.csv", "cancel target_hour" + target_hour);
//            CSVHelper.storeToCSV("randomAlarm.csv", "cancel target_min" + target_min);
//        }
        else if(noti_id == NotiIdRandomMCNotiSurvey){
            CSVHelper.storeToCSV("MCNoti_cancel.csv", "cancel target_hour" + target_hour);
            CSVHelper.storeToCSV("MCNoti_cancel.csv", "cancel target_min" + target_min);
        }
//        if(noti_id!=NotiIdRandomReminder) // not reminder
//            setAlarm(context, when,requestCodeCancelSurvey,DELETENOTIALARM,noti_id); // 11 12 13 14 15 16 17 18
//        else
//            setAlarm(context, when,requestCodeCancelReminder,DELETENOTIALARM,noti_id);



        AlarmManager am = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("notiNumber",noti_id);
        intent.putExtra("appName",appName);
        intent.setAction(action);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, requestCode,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        JSONObject object = new JSONObject();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            object.put("set delete setAlarm",action);
            object.put("when",sdf.format(when.getTime()));
            object.put("requestCode",requestCode).toString();

        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(noti_id == NotiIdActiveSurvey){
            CSVHelper.storeToCSV("noti_cancel.csv", object.toString());
        }
//        else if(noti_id == NotiIdRandomSurvey) {
//            CSVHelper.storeToCSV("randomAlarm.csv", object.toString());
//        }
        else if(noti_id == NotiIdRandomMCNotiSurvey){
            CSVHelper.storeToCSV("MCNoti_cancel.csv", object.toString());
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            am.setAlarmClock(new AlarmManager.AlarmClockInfo(when.getTimeInMillis(),alarmIntent),alarmIntent);
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            am.setExact(AlarmManager.RTC, when.getTimeInMillis(), alarmIntent);
        else
            am.set(AlarmManager.RTC, when.getTimeInMillis(), alarmIntent);

        Log.i(TAG, "Alarm set " + sdf.format(when.getTime()));
    }

//    public void setAlarm(Context context, Calendar when,int requestCode, String action,int noti_id){
//        AlarmManager am = (AlarmManager) context
//                .getSystemService(Context.ALARM_SERVICE);
//        Intent intent = new Intent(context, AlarmReceiver.class);
//        intent.putExtra("notiNumber",noti_id);
//        intent.setAction(action);
//        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, requestCode,intent,PendingIntent.FLAG_UPDATE_CURRENT);
//        JSONObject object = new JSONObject();
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        try {
//            object.put("set delete setAlarm",action);
//            object.put("when",sdf.format(when.getTime()));
//            object.put("requestCode",requestCode).toString();
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        if(noti_id == NotiIdActiveSurvey){
//        }else if(noti_id == NotiIdRandomSurvey) {
//            CSVHelper.storeToCSV("randomAlarm.csv", object.toString());
//        }else if(noti_id == NotiIdRandomMCNotiSurvey){
//            CSVHelper.storeToCSV("MCNoti_cancel.csv", object.toString());
//        }
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
//            am.setAlarmClock(new AlarmManager.AlarmClockInfo(when.getTimeInMillis(),alarmIntent),alarmIntent);
//        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
//            am.setExact(AlarmManager.RTC, when.getTimeInMillis(), alarmIntent);
//        else
//            am.set(AlarmManager.RTC, when.getTimeInMillis(), alarmIntent);
//
//        Log.i(TAG, "Alarm set " + sdf.format(when.getTime()));
//
//    }

    public void startRecordingNotification(Context context, int NOTIFY_ID, String appName, boolean appFlag, Intent ScreenShotIntent){
        Log.d(TAG, "startRecordingNotification");
//        CSVHelper.storeToCSV("AccessibilityDetect.csv", appName + " start notification");
        ifRecordingRightNow = true;
        Notification notification=getRecordingNotification(context,RECORDING_TITLE_CONTENT, appName, appFlag, ScreenShotIntent);
        NotificationManager notifManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.notify(NOTIFY_ID,notification);
    }

    public void updateRecordingNotification(Context context,int NOTIFY_ID, String appName, boolean appFlag, Intent ScreenShotIntent){
        Log.d(TAG, "updateRecordingNotification");
//        CSVHelper.storeToCSV("AccessibilityDetect.csv", appName + " update notification");
        ifRecordingRightNow = false;
        Notification notification=getRecordingNotification(context,RECORDING_ONGOING_CONTENT, appName, appFlag, ScreenShotIntent);
        NotificationManager notifManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
//        startForeground(NOTIFY_ID,notification);

        notifManager.notify(NOTIFY_ID,notification);
    }
    public void stopRecordingNotification(Context context, int NOTIFY_ID, String appName, boolean appFlag, Intent ScreenShotIntent){
        Log.d(TAG, "stopRecordingNotification");
//        CSVHelper.storeToCSV("AccessibilityDetect.csv", appName + " stop notification");
        Notification notification=getRecordingNotification(context,RECORDING_STOP_CONTENT, appName, appFlag, ScreenShotIntent);
        NotificationManager notifManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
//        startForeground(NOTIFY_ID,notification);
        notifManager.notify(NOTIFY_ID,notification);
    }
    public static void cancelNotification(Context context,int noti_id){

        NotificationManager notifManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        StatusBarNotification[] notifications =
                new StatusBarNotification[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            notifications = notifManager.getActiveNotifications();
        }
        for (StatusBarNotification notification : notifications) {
            if(notification.getId() == noti_id) {
                notifManager.cancel(noti_id);
                if(questionaireType==0){
                    CSVHelper.storeToCSV("noti_cancel.csv",noti_id +" : "+getReadableTime(new Date().getTime()));
                }
//                else if(noti_id == NotiIdRandomSurvey && questionaireType==1){
//                    CSVHelper.storeToCSV("randomAlarm.csv","finish questionnaire without click notification : "+getReadableTime(new Date().getTime()));
//                }
//                else if(noti_id == NotiIdRandomMCNotiSurvey && questionaireType==2){
//                    CSVHelper.storeToCSV("MCNoti_cancel.csv","finish questionnaire without click notification : "+getReadableTime(new Date().getTime()));
//                }
            }
        }


    }
    public Notification getRecordingNotification(Context context, String content, String appName, boolean appFlag, Intent ScreenShotIntent){
        NotificationManager notifManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        String id = RECORDING_NOTIFICATION_ID;
        PendingIntent startRecordingPendingIntent = null;
        PendingIntent stopRecordingPendingIntent = null;
        PendingIntent cancelRecordingPendingIntent = null;

        Log.d(TAG, "app: " + appName + " flag: " + appFlag);
        if(ScreenShotIntent != null) {
            ScreenShotIntent.putExtra(appName, appFlag);
            ScreenShotIntent.putExtra("FromNotification", true);
            ScreenShotIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
            startRecordingPendingIntent = PendingIntent.getActivity(context, requestCodeRecordActionStartRecord, ScreenShotIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        }
        // stop
        Intent stopRecordingIntent = new Intent(context, NotificationHandleReceiver.class);
        stopRecordingIntent.putExtra("appName", appName);
        stopRecordingIntent.setAction(STOP_RECORD);
        stopRecordingPendingIntent =  PendingIntent.getBroadcast(context, requestCodeRecordActionRecording, stopRecordingIntent,  PendingIntent.FLAG_CANCEL_CURRENT);

        //google gallery
//        Intent GalleryIntent = getPackageManager().getLaunchIntentForPackage("com.google.android.apps.photos");
//        if(GalleryIntent != null) {
//            GalleryIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        }
//        PendingIntent Gallerypending = PendingIntent.getActivity(this, GALLERYID, GalleryIntent, 0);

        // cancel
        Intent  deleteRecordingIntent = new Intent(context, NotificationHandleReceiver.class);
        deleteRecordingIntent.setAction(CANCEL_RECORD);
        cancelRecordingPendingIntent = PendingIntent.getBroadcast(context, requestCodeRecordActionStopRecording,
                    deleteRecordingIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder;

        NotificationCompat.Action startRecordingAction =
                new NotificationCompat.Action.Builder(R.drawable.ic_stat_name,
                        RECORDING_NOW, startRecordingPendingIntent)
                        .build();

        NotificationCompat.Action stopRecordingAction =
                new NotificationCompat.Action.Builder(R.drawable.ic_stat_name,
                        STOPRECORDING, stopRecordingPendingIntent)
                        .build();

//        NotificationCompat.Action GalleryAction =
//                new NotificationCompat.Action.Builder(R.drawable.ic_stat_name,
//                        "相簿", Gallerypending).build();


        NotificationCompat.Action cancelRecordingAction =
                new NotificationCompat.Action.Builder(R.drawable.ic_stat_name,
                        CANCELRECORDING, cancelRecordingPendingIntent)
                        .build();

        if (notifManager == null) {
            notifManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = notifManager.getNotificationChannel(id);
            if (mChannel == null) {
                mChannel = new NotificationChannel(id, "Accessibility Service Channel", importance);
                mChannel.enableVibration(true);
                mChannel.setVibrationPattern(new long[] {0, 50});
                //mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                notifManager.createNotificationChannel(mChannel);
            }
            builder = new NotificationCompat.Builder(context, id);


            builder.setContentTitle(RECORDING_TITLE)                             // required
                    .setSmallIcon(R.drawable.ic_stat_name)   // required
                    .setContentText(content + " ("+ appName + ")") // required
                    .setContentIntent(null)
                    .setOnlyAlertOnce(true)
                    .setAutoCancel(false)
//                    .addAction(startRecordingAction)
//                    .addAction(stopRecordingAction)
                    //.setTicker(null)
                    //.setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                    .setVibrate(new long[] {0, 50});
        }
        else {
            builder = new NotificationCompat.Builder(context, id);
            builder.setContentTitle(RECORDING_TITLE)                            // required
                    .setSmallIcon(R.drawable.ic_stat_name)   // required
                    .setContentText(content + " ("+ appName + ")") // required
                    .setContentIntent(null)
                    .setOnlyAlertOnce(true)
//                    .addAction(startRecordingAction)
//                    .addAction(stopRecordingAction)
                    .setVibrate(new long[] {0, 50})
                    .setAutoCancel(false)
//                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                    .setPriority(Notification.PRIORITY_MAX);
        }
        Log.d(TAG,"recording title : "+ content);
        if(content.equals(RECORDING_STOP_CONTENT)){
            builder.addAction(stopRecordingAction);
        }else if(content.equals(RECORDING_TITLE_CONTENT)){
            builder.addAction(startRecordingAction);
        }
//        builder.addAction(GalleryAction);
//        builder.addAction(cancelRecordingAction);
        Notification notification = builder.build();

//        notification.tickerView = null;
//        recordNotificationChanging = true;
        return notification;
    }

    public boolean pendingIntentExist(Context context,Intent intent,int requestCode){
        boolean alarmUp = (PendingIntent.getBroadcast(context, requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE) != null);

       return alarmUp;
    }
    public void cancelRecordNotiPendingIntent(Context context){
            Intent recordIntent = new Intent(context, BackgroundScreeenRecorderActivity.class);
            recordIntent.setAction(START_RECORDING);
            Intent stopRecordingIntent = new Intent(context, NotificationHandleReceiver.class);
            stopRecordingIntent.setAction(STOP_RECORD);
            Intent cancelRecordingIntent = new Intent(context, NotificationHandleReceiver.class);
            cancelRecordingIntent.setAction(CANCEL_RECORD);

            if(pendingIntentExist(context,recordIntent,requestCodeRecordActionStartRecord)){
                PendingIntent.getActivity(context, requestCodeRecordActionStartRecord, recordIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT).cancel();
            }else if (pendingIntentExist(context,stopRecordingIntent,requestCodeRecordActionRecording)){
                PendingIntent.getBroadcast(context, requestCodeRecordActionRecording, stopRecordingIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT).cancel();
            }else if(pendingIntentExist(context,cancelRecordingIntent,requestCodeRecordActionStopRecording)){
                PendingIntent.getBroadcast(context, requestCodeRecordActionStopRecording, cancelRecordingIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT).cancel();
            }
    }





//    public class CountDownTask {
//        Handler mHandler = new Handler();
//        appDatabase db = appDatabase.getDatabase(getApplicationContext());
//        int interval = 1000; // 1000 * 30
//        ResponseDataRecord responseDR;
//        Runnable mHandlerTask = new Runnable() {
//            @RequiresApi(api = Build.VERSION_CODES.O)
//            @Override
//            public void run() {
//                if (countDownForNoti > 0) {
//                    Log.d(TAG,"CountDown in if = "+countDownForNoti);
//                    countDownForNoti -= interval;
//                    mHandler.postDelayed(mHandlerTask, interval);
//                    //已經handle 而且超過十分鐘 停止偵測
//                    if(ifComplete) {
//                        responseDR.setStartAnswerTime(getReadableTime(startAnswerTimeLong));
//                        responseDR.setFinishedTime(getReadableTime(finishAnswerTime));
//                        responseDR.setIfComplete(true);
//                        db.repsonseDataRecordDao().insertAll(responseDR);
//                        stopRepeatingTask();
//                    }
//
//                }else{   //十分鐘之後沒有按
//                    db.repsonseDataRecordDao().insertAll(responseDR);
//                    stopRepeatingTask();
//
//                }
//            }
//        };
//
//        void startRepeatingTask(ResponseDataRecord rs) {
//            responseDR = rs;
//            countDownForNoti = 10*60*1000;//ten minutes // 10*60*1000
//            mHandlerTask.run();
//        }
//
//        void stopRepeatingTask() {
//            NotificationManager mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//            relatedId++;
//            mManager.cancel(102);
//            canFillQuestionnaire = false;
//            startAnswerTimeLong = Long.valueOf(0);
//            finishAnswerTime = Long.valueOf(0);
//            ifComplete = false;
//            mHandler.removeCallbacks(mHandlerTask);
//            countDownForNoti = -1;
//        }
//    }





//    private void createNotificationChannel(String channelName, String channelID, int channelImportance) {
//        // Create the NotificationChannel, but only on API 26+ because
//        // the NotificationChannel class is new and not in the support library
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            CharSequence name = channelName;
//            int importance = channelImportance;
//            NotificationChannel channel = new NotificationChannel(channelID, name, importance);
//            // Register the channel with the system; you can't change the importance
//            // or other notification behaviors after this
//            NotificationManager notificationManager = getSystemService(NotificationManager.class);
//            notificationManager.createNotificationChannel(channel);
//        }
//    }

//    public void saveArrayList(ArrayList<String> list, String key, SharedPreferences prefs){
//
//        SharedPreferences.Editor editor = prefs.edit();
//        Gson gson = new Gson();
//        String json = gson.toJson(list);
//        editor.putString(key, json);
//        editor.apply();     // This line is IMPORTANT !!!
//    }
private String loadQuestionnaireJson(Context context, String filename) {
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
    public static final class MatchReason {
        public static final String POST = "POST";
        public static final String REASON_APP_CANCEL = "REASON_APP_CANCEL";
        public static final String REASON_APP_CANCEL_ALL = "REASON_APP_CANCEL_ALL";
        public static final String REASON_CANCEL = "REASON_CANCEL";
        public static final String REASON_CANCEL_ALL = "REASON_CANCEL_ALL";
        public static final String REASON_CHANNEL_BANNED = "REASON_CHANNEL_BANNED";
        public static final String REASON_CLICK = "REASON_CLICK";
        public static final String REASON_ERROR = "REASON_ERROR";
        public static final String REASON_GROUP_OPTIMIZATION = "REASON_GROUP_OPTIMIZATION";
        public static final String REASON_GROUP_SUMMARY_CANCELED = "REASON_GROUP_SUMMARY_CANCELED";
        public static final String REASON_LISTENER_CANCEL = "REASON_LISTENER_CANCEL";
        public static final String REASON_LISTENER_CANCEL_ALL = "REASON_LISTENER_CANCEL_ALL";
        public static final String REASON_PACKAGE_BANNED = "REASON_PACKAGE_BANNED";
        public static final String REASON_PACKAGE_CHANGED = "REASON_PACKAGE_CHANGED";
        public static final String REASON_PACKAGE_SUSPENDED = "REASON_PACKAGE_SUSPENDED";
        public static final String REASON_PROFILE_TURNED_OFF = "REASON_PROFILE_TURNED_OFF";
        public static final String REASON_SNOOZED = "REASON_SNOOZED";
        public static final String REASON_TIMEOUT = "REASON_TIMEOUT";
        public static final String REASON_UNAUTOBUNDLED = "REASON_UNAUTOBUNDLED";
        public static final String REASON_USER_STOPPED = "REASON_USER_STOPPED";
        public static final String SUPPRESSED_EFFECT_SCREEN_OFF = "SUPPRESSED_EFFECT_SCREEN_OFF ";
        public static final String SUPPRESSED_EFFECT_SCREEN_ON = "SUPPRESSED_EFFECT_SCREEN_ON ";

    }
    public static String checkWhichReason(int reason){
        if(reason == REASON_APP_CANCEL){
            return MatchReason.REASON_APP_CANCEL;
        }else if(reason ==REASON_APP_CANCEL_ALL){
            return MatchReason.REASON_APP_CANCEL_ALL;
        }else if(reason ==REASON_CANCEL){
            return MatchReason.REASON_CANCEL;
        }else if(reason ==REASON_CANCEL_ALL){
            return MatchReason.REASON_CANCEL_ALL;
        }else if(reason == REASON_CHANNEL_BANNED){
            return MatchReason.REASON_CHANNEL_BANNED;
        }else if(reason ==REASON_CLICK){
            return MatchReason.REASON_CLICK;
        }else if(reason ==REASON_ERROR){
            return MatchReason.REASON_ERROR;
        }else if(reason == REASON_GROUP_OPTIMIZATION){
            return MatchReason.REASON_GROUP_OPTIMIZATION;
        }else if(reason == REASON_GROUP_SUMMARY_CANCELED){
            return MatchReason.REASON_GROUP_SUMMARY_CANCELED;
        } else if (reason == REASON_LISTENER_CANCEL) {
            return MatchReason.REASON_LISTENER_CANCEL;
        }else if(reason == REASON_LISTENER_CANCEL_ALL){
            return MatchReason.REASON_LISTENER_CANCEL_ALL;
        }else if(reason == REASON_PACKAGE_BANNED){
            return MatchReason.REASON_PACKAGE_BANNED;
        }else if (reason == REASON_PACKAGE_CHANGED) {
            return MatchReason.REASON_PACKAGE_CHANGED;
        }else if(reason == REASON_PACKAGE_SUSPENDED){
            return MatchReason.REASON_PACKAGE_SUSPENDED;
        }else if(reason == 	REASON_PROFILE_TURNED_OFF){
            return MatchReason.REASON_PROFILE_TURNED_OFF;
        }else if (reason == REASON_SNOOZED) {
            return MatchReason.REASON_SNOOZED;
        } else if (reason == REASON_TIMEOUT) {
            return MatchReason.REASON_TIMEOUT;
        } else if(reason == REASON_UNAUTOBUNDLED){
            return MatchReason.REASON_UNAUTOBUNDLED;
        }else if(reason == 	REASON_USER_STOPPED){
            return MatchReason.REASON_USER_STOPPED;
        }else if (reason == SUPPRESSED_EFFECT_SCREEN_OFF) {
            return MatchReason.SUPPRESSED_EFFECT_SCREEN_OFF;
        } else if (reason == SUPPRESSED_EFFECT_SCREEN_ON) {
            return MatchReason.SUPPRESSED_EFFECT_SCREEN_ON;
        }else {
            return "OTHER";
        }
    }

}
