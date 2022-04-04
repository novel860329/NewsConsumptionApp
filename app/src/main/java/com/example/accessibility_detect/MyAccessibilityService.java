package com.example.accessibility_detect;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.loader.content.Loader;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.accessibility_detect.naturallanguage.AccessTokenLoader;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.language.v1.CloudNaturalLanguage;
import com.google.api.services.language.v1.CloudNaturalLanguageRequest;
import com.google.api.services.language.v1.CloudNaturalLanguageScopes;
import com.google.api.services.language.v1.model.AnalyzeEntitiesRequest;
import com.google.api.services.language.v1.model.AnalyzeEntitiesResponse;
import com.google.api.services.language.v1.model.AnalyzeSentimentRequest;
import com.google.api.services.language.v1.model.AnalyzeSentimentResponse;
import com.google.api.services.language.v1.model.AnnotateTextRequest;
import com.google.api.services.language.v1.model.AnnotateTextResponse;
import com.google.api.services.language.v1.model.Document;
import com.google.api.services.language.v1.model.Entity;
import com.google.api.services.language.v1.model.Features;
import com.google.api.services.language.v1.model.Sentiment;
import com.google.api.services.language.v1.model.Token;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import labelingStudy.nctu.minuku.DBHelper.appDatabase;
import labelingStudy.nctu.minuku.Utilities.CSVHelper;
import labelingStudy.nctu.minuku.Utilities.ScheduleAndSampleManager;
import labelingStudy.nctu.minuku.manager.MinukuStreamManager;
import labelingStudy.nctu.minuku.model.DataRecord.AppTimesDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.MyDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.NewsDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.SensorDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.SessionDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.UserDataRecord;
import labelingStudy.nctu.minuku.service.NotificationListenService;
import labelingStudy.nctu.minuku.streamgenerator.AccessibilityStreamGenerator;
import labelingStudy.nctu.minuku.streamgenerator.AppTimesStreamGenerator;
import labelingStudy.nctu.minuku.streamgenerator.SensorStreamGenerator;
import labelingStudy.nctu.minukucore.exception.StreamNotFoundException;
import labelingStudy.nctu.minukucore.streamgenerator.StreamGenerator;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.example.accessibility_detect.NotificationHelper.ScreenCapture_manager;
import static com.example.accessibility_detect.Utils.chrome;
import static com.example.accessibility_detect.Utils.facebook;
import static com.example.accessibility_detect.Utils.google;
import static com.example.accessibility_detect.Utils.instagram;
import static com.example.accessibility_detect.Utils.line;
import static com.example.accessibility_detect.Utils.line_mes;
import static com.example.accessibility_detect.Utils.messenger;
import static com.example.accessibility_detect.Utils.news;
import static com.example.accessibility_detect.Utils.ptt;
import static com.example.accessibility_detect.Utils.reading;
import static com.example.accessibility_detect.Utils.screen_on;
import static com.example.accessibility_detect.Utils.takeNews;
import static com.example.accessibility_detect.Utils.youtube;
import static labelingStudy.nctu.minuku.Utilities.CSVHelper.CSV_News;
import static labelingStudy.nctu.minuku.config.Constants.ACCESSIBILITY_STREAM_GENERATOR;
import static labelingStudy.nctu.minuku.config.Constants.PICTURE_DIRECTORY_PATH;
import static labelingStudy.nctu.minuku.config.Constants.RECORDING_NOTIFICATION_ID;
import static labelingStudy.nctu.minuku.config.Constants.START_RECORDING;
import static labelingStudy.nctu.minuku.config.Constants.STOP_RECORDING;
import static labelingStudy.nctu.minuku.config.SharedVariables.ESM_ALARM;
import static labelingStudy.nctu.minuku.config.SharedVariables.Last_Agree;
import static labelingStudy.nctu.minuku.config.SharedVariables.Random_session_counter;
import static labelingStudy.nctu.minuku.config.SharedVariables.Random_session_num;

//import com.crashlytics.android.Crashlytics;
//import io.fabric.sdk.android.Fabric;

public class MyAccessibilityService extends AccessibilityService{
    final String TAG = "Accessibility service";
    MinukuStreamManager streamManager;
    NotificationManager mNotificationManager;
    public static NotificationListenService notificationListenService = new NotificationListenService();
    private String MyEventText = "";
    private long[] vibrate_effect = {0, 200};
    private static String CHANNEL_ID = "AccessibilityService";
    public static final int Accessibility_ID = 6;

    private String name = "";
    private int log_counter = 0;
    Handler mMainThread = new Handler();
    private String[] NewsPack;
    private String[] NewsName;
    private String[] WebPack;
    private String[] WebEvent;
    private String[] HomePackage;
    private String[] AppPack;
    private String[] PttPack;
    public static final String CHANNEL = "AccessibilityServiceChannel";
    public static final String FOREGROUNDPREF = "ForegroundPref";
    public static final String LASTAPP = "LastApp";
    public static final String TIMESTAMP = "Timestamp";
    public static final String DEVICEID = "DeviceID";
    public static final String PACKAGENAME= "PackageName";
    public static final String ISSYSTEMAPP= "SystemApp";
    public static final String READABLE= "readable";
    public static final String EVENTTEXT = "eventtext";
    public static final String EVENTTYPE = "eventtype";
    public static final String EXTRA = "extra";
    public static final String APPLICATION_VERSION= "Application Version";
    public static final String ERROR_SHORT= "Error_Short";
    public static final String ERROR_LONG= "Error_Short";
    public static final String ERROR_CONDITION= "Error_Short";
    private UserDataRecord userRecord;
    private PowerManager mPowerManager;
    private static AccessibilityStreamGenerator accessibilityStreamGenerator;
    private static AppTimesStreamGenerator apptimesStreamGenerator;
    public static SensorStreamGenerator sensorStreamGenerator;
    ScheduledExecutorService mScheduledExecutorService;
    ScheduledFuture<?> mScheduledFuture, mScheduledFutureIsAlive, mScheduledPhoneState;
    private SessionDataRecord sessionDataRecord;
    private NewsDataRecord newsDataRecord;
    private boolean exit_room = true;
    private boolean home = false;
    private boolean home_first = true;
    private boolean random_first = true;
    private boolean watch_video = false;
    private boolean[] ScreenCaptureTransition = {false, false, false};
    private boolean line_interrupted_by_messenger = false;
    private int appear = 0;
    private SharedPreferences pref;
    private int googlebox_count = 0;
    private int googlebox_home = 0;
    private int youtube_count = 0;
    private int agree_interval;
    public static  Intent intent;
    private boolean[] state_array = {false, false, false, false, false, false, false, false, false, false};
    private String date;
    long facebook_open_time = 0L;
    long lastid;
    long phone_sessionid = 1;
    private long messenger_duration = 0;
    appDatabase db;

    Runnable runnable = new Runnable() {
        @Override
        public void run() {

            if(sensorStreamGenerator != null) {
                sensorStreamGenerator.updateStream();
            }
            Log.d(TAG, "Which one is running?");
            mMainThread.postDelayed(this, 5 * 1000);
        }
    };

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
//        AccessibilityEvent.eventTypeToString(event.getEventType()).contains("CLICK")
//        if(true){
//            AccessibilityNodeInfo nodeInfo = event.getSource();
//            dfs(nodeInfo);
//        }
//        Log.d("ABC", "------------------------");

        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                Log.d(TAG, "TYPE_VIEW_CLICKED");
                break;
            case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED:
                Log.d(TAG, "TYPE_VIEW_LONG_CLICKED");
                break;
            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
                Log.d(TAG, "TYPE_VIEW_FOCUSED");
                break;
            case AccessibilityEvent.TYPE_VIEW_SELECTED:
                Log.d(TAG, "TYPE_VIEW_SELECTED");
                break;
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                Log.d(TAG, "TYPE_VIEW_TEXT_CHANGED");
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                Log.d(TAG, "TYPE_WINDOW_STATE_CHANGED");
                break;
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                Log.d(TAG, "TYPE_NOTIFICATION_STATE_CHANGED");
                break;
//            case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_START:
//                Log.d(TAG, "TYPE_TOUCH_EXPLORATION_GESTURE_START");
//                break;
//            case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_END:
//                Log.d(TAG, "TYPE_TOUCH_EXPLORATION_GESTURE_END");
//                break;
//            case AccessibilityEvent.TYPE_VIEW_HOVER_ENTER:
//                Log.d(TAG, "TYPE_VIEW_HOVER_ENTER");
//                break;
//            case AccessibilityEvent.TYPE_VIEW_HOVER_EXIT:
//                Log.d(TAG, "TYPE_VIEW_HOVER_EXIT");
//                break;
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                Log.d(TAG, "TYPE_VIEW_SCROLLED");
                break;
            case AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED:
                Log.d(TAG, "TYPE_VIEW_TEXT_SELECTION_CHANGED");
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                Log.d(TAG, "TYPE_WINDOW_CONTENT_CHANGED");
                break;
            case AccessibilityEvent.TYPE_ANNOUNCEMENT:
                Log.d(TAG, "TYPE_ANNOUNCEMENT");
                break;
//            case AccessibilityEvent.TYPE_GESTURE_DETECTION_START:
//                Log.d(TAG, "TYPE_GESTURE_DETECTION_START");
//                break;
//            case AccessibilityEvent.TYPE_GESTURE_DETECTION_END:
//                Log.d(TAG, "TYPE_GESTURE_DETECTION_END");
//                break;
//            case AccessibilityEvent.TYPE_TOUCH_INTERACTION_START:
//                Log.d(TAG, "TYPE_TOUCH_INTERACTION_START");
//                break;
//            case AccessibilityEvent.TYPE_TOUCH_INTERACTION_END:
//                Log.d(TAG, "TYPE_TOUCH_INTERACTION_END");
//                break;
//            case AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED:
//                Log.d(TAG, "TYPE_VIEW_ACCESSIBILITY_FOCUSED");
//                break;
            case AccessibilityEvent.TYPE_WINDOWS_CHANGED:
                Log.d(TAG, "TYPE_WINDOWS_CHANGED");
                break;
//            case AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED:
//                Log.d(TAG, "TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED");
//                break;
        }

        pref.edit().putString("AccessibilityTime", ScheduleAndSampleManager.getCurrentTimeString()).apply(); // 12/14

        AccessibilityNodeInfo nodeInfo = event.getSource();
        if(nodeInfo != null)
        {
            Log.d(TAG, "NodeInfo: " + nodeInfo);
            Log.d(TAG, "NodeInfo: " + nodeInfo.getContentDescription());
        }

//        String last_active = getSharedPreferences("test", MODE_PRIVATE)
//                .getString("ForegroundApp", "");
        intent = new Intent(getApplicationContext(), ScreenCaptureActivity.class);

        String now_active = printForegroundTask();
        screen_on = getScreenStatus();

        //Log.d(TAG, "Screen Status:" + screen_on);
        boolean screenshot = isMyServiceRunning(com.example.accessibility_detect.ScreenCapture.class);
        String time = pref.getString("agree_dialog_time","0");

        if(time != null) {
            agree_interval = Integer.parseInt(time);
        }
        else{
            agree_interval = 0;
        }
        MyEventText = "NA";

        if(event.getPackageName() != null) {
            home = ((getEventText(event).contains("螢幕") && !getEventText(event).contains("全螢幕模式")) || getEventText(event).contains("主畫面") || getEventText(event).matches("第.*頁，共.*頁")
                        || getEventText(event).contains("預設頁面") || getEventText(event).contains("桌面") || !screen_on);
            if(event.getPackageName().equals("com.android.systemui"))home = false;
//            Log.d(TAG, "screen on: " + screen_on + " random first: " + random_first);
            if(!screen_on){
                home = true;
                if(random_first) {
                    Random_session_num = (int) (Math.random() * 3 + 1);
                    CSVHelper.storeToCSV("ESM_random_number.csv", "because screen is off, reset random number to: " + Random_session_num);
                    Random_session_counter = 0;
//                    pref.edit().putLong("Phone_SessionID", -1).apply();
//                    Log.d(TAG, "Phone session id: -1");
                    random_first = false;
                }
            }
            else{
                if(!random_first) {
//                    phone_sessionid = pref.getLong("Phone_SessionID", 1);
//                    phone_sessionid++;
//                    pref.edit().putLong("Phone_SessionID", phone_sessionid).apply();
//                    Log.d(TAG, "Phone session id: " + phone_sessionid);
                    random_first = true;
                }
            }
            if(home) CSVHelper.storeToCSV("AccessibilityDetect.csv",  "Home event: " + getEventText(event));

            int EventType = event.getEventType();

            String EventText = getEventText(event);
            String EventPackage = event.getPackageName().toString();
            Log.d(TAG, "The package name is: " + EventPackage);
            Log.d(TAG, "The event message is: " + EventText);
//            getUserInput(EventType, EventText, EventPackage, event);

//            Log.d(TAG, "Facebook is : " + String.valueOf(facebook));
//
//            Log.d(TAG, "Messenger is : " + String.valueOf(messenger));
//
//            for(int i = 0; i < 10; i++){
//                Log.d(TAG, "State Array: " + state_array[i]);
//            }
            StopScreenshotDetecter(screenshot, EventType, EventText, EventPackage, now_active, nodeInfo);

            IsGoogleRunning(EventType, EventText, EventPackage, intent, screenshot, now_active);
            Log.d(TAG, "Google now: " + google);

            IsFacebookRunning(EventType, EventText, EventPackage, intent, screenshot);

            IsLineMesRunning(EventType, EventText, EventPackage, nodeInfo, screenshot, now_active);
            //Log.d(TAG, "Line is : " + String.valueOf(line));

            Log.d(TAG, "Chrome: " + chrome);
            IsWebBrowserRunning(EventType, EventText, EventPackage, intent, screenshot, now_active);

            IsInstagramRunning(EventType, EventText, EventPackage, intent, screenshot);
            //Log.d(TAG, "Instagram is : " + String.valueOf(instagram));

            IsYoutubeRunning(EventType, EventText, EventPackage, intent, screenshot);
            //Log.d(TAG, "Youtube is : " + String.valueOf(youtube));

            IsNewsAppRunning(EventType, EventText, EventPackage, now_active, intent, screenshot);
            //Log.d(TAG, "NewsApp is : " + String.valueOf(news));

            IsLineRunning(EventType, EventText, EventPackage, intent, screenshot);
            //Log.d(TAG, "Today is : " + String.valueOf(line));

            IsPTTAppRunning(EventType, EventText, EventPackage, now_active, screenshot, event);
            //Log.d(TAG, "PTTApp is : " + String.valueOf(ptt));

            Log.d(TAG, "My Event is : " + MyEventText);

            IsMessengerRunning(EventType, EventText, EventPackage, nodeInfo, now_active, screenshot);

            Log.d(TAG, "Messenger is : " + String.valueOf(messenger));


            String extra = "";
            if(nodeInfo != null)
            {
                if(nodeInfo.getContentDescription() != null)
                {
                    extra = nodeInfo.getContentDescription().toString();
                }
            }
            DetectLastForeground(EventType, EventText, EventPackage, extra);

            SaveLineLastSelected(EventType, EventText, EventPackage, now_active);


            pref.edit()
                    .putString("ForegroundApp", now_active)
                    .apply();
        }
        if(nodeInfo != null) {
            nodeInfo.recycle();
        }
    }

    public void dfs(AccessibilityNodeInfo info){
        if(info == null)
            return;
        if(info.getText() != null && info.getText().length() > 0){
            if(info.getText().toString().contains("新聞") && info.getText().toString().length() < 30)
            {
                Log.d(TAG, "DFS news: " + info.getText().toString());
//                    URLtemp.add(EventText);
//                if(apptimesStreamGenerator!=null) {
//                    apptimesStreamGenerator.updateapptimes("PTTtitle");
//                }
                WriteToFile(info.getText().toString(), "PTT");
                Log.d(TAG, "News In PTT: " + info.getText().toString());
            }
            Log.d(TAG, "dfs: " + info.getText() + " class: "+info.getClassName());
        }

        for(int i=0;i<info.getChildCount();i++){
            AccessibilityNodeInfo child = info.getChild(i);
            dfs(child);
            if(child != null){
                child.recycle();
            }
        }
    }

    int lastEvent = 0;
    boolean isTyping = false;
    int eventCounter = 0;
    String UserInput = "";
    int finishCount = 0;

    private void getUserInput(int EventType, String EventText, String EventPackage, AccessibilityEvent e){
//        UserInput = "";

        if(Arrays.asList(AppPack).contains(EventPackage) || Arrays.asList(WebPack).contains(EventPackage) || Arrays.asList(NewsPack).contains(EventPackage)) {
            Log.d(TAG, "get user input: " + isTyping + " " + eventCounter);

            if (EventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
                Log.d("Semantic", "Source: " + e.getSource());
                Log.d("Semantic", "Class Name: " + e.getClassName());
                Log.d("Semantic", "Event Time: " + e.getEventTime());
                Log.d("Semantic", "Text: " + e.getText());
                Log.d("Semantic", "Before Text: " + e.getBeforeText());
                Log.d("Semantic", "From Index: " + e.getFromIndex());
                Log.d("Semantic", "Add Count: " + e.getAddedCount());
                Log.d("Semantic", "Remove Count: " + e.getRemovedCount());
                StringBuilder sb = new StringBuilder();
                for (CharSequence s : e.getText()) {
                    sb.append(s);
                }
                EventText =  sb.toString();

                if(e.getBeforeText().equals("")){
                    Log.d("Semantic", "Store: " + UserInput);
                    if(!UserInput.equals("")){
//                        CSVHelper.storeToCSV("Semantic.csv", UserInput);
                        prepareApi();
                        analyzeSentiment(UserInput);
//                        analyzeEntities(UserInput);
//                        analyzeSyntax(UserInput);
                    }
                }
                if(e.getRemovedCount() == 0){
                    UserInput = EventText;
                    Log.d("Semantic", "This is input: " + UserInput);
                }
                Log.d("Semantic", "----------------------");
//                UserInput = EventText;
//                isTyping = true;
//                if (EventPackage.equals("com.facebook.orca") || EventPackage.equals("jp.naver.line.android") || EventPackage.equals("com.instagram.android")) {
//                    finishCount = 5;
//                }
//                else if(EventPackage.equals("com.facebook.katana")){
//                    finishCount = 30;
//                }
//                else if(EventPackage.equals("com.google.android.youtube") || Arrays.asList(Utils.PTTPack).contains(EventPackage) ){
//                    finishCount = 7;
//                }
//                else{
//                    finishCount = 40;
//                }
            }
//            if (isTyping) {
//                if (EventType != AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
//                    eventCounter++;
//                } else {
//                    eventCounter = 0;
//                }
//            }
//            if (eventCounter > finishCount) {
//                isTyping = false;
//                eventCounter = 0;
//                if(UserInput.length() > 2) {
//                    CSVHelper.storeToCSV("Semantic.csv", UserInput);
//                    Log.d(TAG, "Semantic: " + UserInput);
//                }
//            }

            lastEvent = EventType;
        }
    }

    private void IsFacebookRunning(int EventType, String EventText, String EventPackage, Intent service_intent, boolean screenshot)
    {
        Log.d(TAG, "Cancel: " + Utils.cancel);
        Log.d(TAG, "Screen shot: " + screenshot);
        if (true) { // !screenshot 1/18
            if ((EventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || EventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED)
                    && EventPackage.equals("com.facebook.katana") && EventText.equals("Facebook") && !facebook && !Utils.cancel) {
                clean_prevApp(state_array, 1);
                state_array[1] = true;
                facebook = true;
                MyEventText = "Facebook is open";
                Log.d(TAG, "Facebook is open!!");
//                if (apptimesStreamGenerator!=null)
//                {
//                    apptimesStreamGenerator.updateapptimes("FacebookOpen");
//                }

//                CSVHelper.storeToCSV("AccessibilityDetect.csv", "In facebook screen shot trigger, facebook flag is " + facebook);

                storeSession("Facebook", "Image");
                NewsRelated(true);

//                CSVHelper.storeToCSV("Dialog.csv" ,"Dialog Time: " + System.currentTimeMillis() + " In accessibility service(facebook)");
//                service_intent.putExtra("Facebook", facebook);
                pref.edit().putString("Trigger", "Facebook").apply();
                service_intent.putExtra("FromNotification", false);
                service_intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
//                CSVHelper.storeToCSV("AccessibilityDetect.csv", "facebook call startActivity time: " + (System.currentTimeMillis()));
//                try {
//                    Thread.sleep(200);
//                } catch (Exception e) {
//
//                }
                Log.d("ScreenCaptureActivity", "Facebook is open!!");
                startActivity(service_intent);

                SendCaptureNoti("Facebook", facebook, service_intent, screenshot);

//                CSVHelper.storeToCSV("AccessibilityDetect.csv", "Send facebook screen shot notification");
            }
        }
        //optimization
//            else if (screenshot)
//            {
//                if(facebook) {
//                    //返回桌面
//                    if (EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
//                            && home) {
//                        init_application();
//                        service_intent.putExtra("Facebook", facebook);
//                        stopService(new Intent(getApplicationContext(), ScreenCapture.class));
//                        MyEventText = "Facebook is close";
//                        Log.d(TAG, "Facebook is close!!");
//                        NewsRelated(false);
//                    }
//                    //案通知欄裡面的訊息，跳到別的app
//                    else if(EventType == AccessibilityEvent.TYPE_VIEW_CLICKED
//                            && (Arrays.asList(Utils.HomePackage).contains(EventPackage)
//                            || EventPackage.equals("com.android.systemui"))
//                            && !Arrays.asList(Utils.Permission_ui).contains(EventText))
//                    {
//                        init_application();
//                        service_intent.putExtra("Facebook", facebook);
//                        stopService(new Intent(getApplicationContext(), ScreenCapture.class));
//                        Log.d(TAG, "Facebook is close!!");
//                        MyEventText = "Facebook is close";
//                        NewsRelated(false);
//                    }
//                }
//            }
//            if(Utils.cancel && facebook)
//            {
//                facebook = true;
//                if (EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
//                        && home) {
//                    Log.d(TAG, "facebook Initial");
//                    MyEventText = "Facebook is close";
//                    Utils.cancel = false;
//                    init_application();
//                    NewsRelated(false);
//                }
//                //案通知欄裡面的訊息，跳到別的app
//                else if(EventType == AccessibilityEvent.TYPE_VIEW_CLICKED
//                        && (Arrays.asList(Utils.HomePackage).contains(EventPackage)
//                        || EventPackage.equals("com.android.systemui"))
//                        && !Arrays.asList(Utils.Permission_ui).contains(EventText))
//                {
//                    Log.d(TAG, "facebook Initial");
//                    MyEventText = "Facebook is close";
//                    Utils.cancel = false;
//                    NewsRelated(false);
//                    init_application();
//                }
//            }
    }
    private void IsMessengerRunning(int EventType, String EventText, String EventPackage, AccessibilityNodeInfo nodeInfo, String now_active, boolean screenshot)
    {
//        boolean change_app = false;
        if(ScreenCaptureTransition[0] && !screenshot){
            storeSession("Messenger", "Url");
            NewsRelated(true);
            ScreenCaptureTransition[0] = false;
        }
//        if(!now_active.equals("com.facebook.orca") && EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
//                && !now_active.equals("android") && !now_active.equals("com.android.systemui") && !now_active.equals("com.example.accessibility_detect")){
//            change_app = true;
//        }
        if (!messenger) {
            if ((EventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || EventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED)
                    && (EventText.equals("Messenger") || (EventPackage.equals("com.facebook.orca")))) {
//                try {
//                    Thread.sleep(500);
//                } catch (Exception e) {
//
//                }
                clean_prevApp(state_array, 9);
                state_array[9] = true;
                messenger = true;
                ScreenCaptureTransition[0] = true;
                MyEventText = "Messenger is open";
                messenger_duration = System.currentTimeMillis();
                Log.d(TAG, "Messenger is open!!");
//                CSVHelper.storeToCSV("AccessibilityDetect.csv", "messenger is open");
//                CSVHelper.storeToCSV("AccessibilityDetect.csv", "In messenger not running scope, messenger flag is " + messenger);
                if(screenshot){
                    stopService(new Intent(getApplicationContext(), ScreenCapture.class));
                }
            }
        }
        else if (messenger)
        {
//                if(nodeInfo!=null) {
//                    if (nodeInfo.getContentDescription() != null) {
//                        if(nodeInfo.getContentDescription().equals("用戶")){
//                            if(!name.equals("")) {
//                                WriteToFile("Test","Messenger");
//                            }
//                            else{
//                                URLtemp.clear();
//                            }
//                        }
//                    }
//                }
//            if(false){
//                if(nodeInfo!=null) {
//                    if (nodeInfo.getContentDescription() == null) {
//                        messenger = false;
//                        lastid = pref.getLong("SessionID", 0);
//                        db.SessionDataRecordDao().updateSession(lastid, ScheduleAndSampleManager.getCurrentTimeString(), System.currentTimeMillis());
//                        MyEventText = "Messenger is close";
////                            if(!name.equals("")) {
////                                WriteToFile("Test","Messenger");
////                            }
//                        NewsRelated(false);
//                        Log.d(TAG, "Messenger is close!!");
//                    }
//                    //take_next = false;
//                }
//            }
//                //返回桌面
//             else
            boolean change_app = false;
            if(!now_active.equals("com.facebook.orca") && EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
                    && !now_active.equals("android") && !now_active.equals("com.android.systemui") && !now_active.equals("com.example.accessibility_detect")
                    && !EventPackage.equals("com.facebook.orca") && !EventPackage.equals("android") && EventPackage.equals(now_active)
                    && !EventPackage.equals("com.android.systemui") && !EventPackage.equals("com.example.accessibility_detect")){
                change_app = true;
            }
            boolean question_interrupt = pref.getBoolean("Question_interrupt",false);
            if (EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
                        && (home || question_interrupt || change_app)) {
//                CSVHelper.storeToCSV("AccessibilityDetect.csv",  "Messenger is close(home condition)");
                pref.edit().putBoolean("Question_interrupt",false).apply();
                    init_application();
                    messenger = false;
                    state_array[9] = false;
                    MyEventText = "Messenger is close";
                    lastid = pref.getLong("SessionID", 0);
                    db.SessionDataRecordDao().updateSession(lastid, ScheduleAndSampleManager.getCurrentTimeString(), System.currentTimeMillis());
//                        if(!name.equals("")) {
//                            WriteToFile("Test","Messenger");
//                        }
                    NewsRelated(false);
//                    CSVHelper.storeToCSV("AccessibilityDetect.csv", "In messenger running scope, messenger flag is " + messenger + " ( " + home +", )");

                    long now = System.currentTimeMillis();
                    if(now - messenger_duration >= 10 *1000){
                        ESMjump("Messenger");
                    }

                    Log.d(TAG, "Messenger is close!!");
                }
                //案通知欄裡面的訊息，跳到別的app
            else if(
                    false
//                        EventType == AccessibilityEvent.TYPE_VIEW_CLICKED
//                        && (Arrays.asList(HomePackage).contains(EventPackage)
//                        || EventPackage.equals("com.android.systemui"))
//                        && !Arrays.asList(Utils.Permission_ui).contains(EventText)
            )
                {
//                    CSVHelper.storeToCSV("AccessibilityDetect.csv",  "Messenger is close(noti condition)");

                    init_application();
                    messenger = false;
                    state_array[9] = false;
                    lastid = pref.getLong("SessionID", 0);
                    db.SessionDataRecordDao().updateSession(lastid, ScheduleAndSampleManager.getCurrentTimeString(), System.currentTimeMillis());
                    MyEventText = "Messenger is close";
//                        if(!name.equals("")) {
//                            WriteToFile("Test","Messenger");
//                        }
                    NewsRelated(false);
                    Log.d(TAG, "Messenger is close!!");
                }
//                else if(EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED  && !now_active.equals("com.facebook.orca")){
//                    if(Arrays.asList(AppPack).contains(now_active) || Arrays.asList(NewsPack).contains(now_active)
//                            || Arrays.asList(PTTPack).contains(now_active)) {
//                        init_application();
//                        messenger = false;
//                        MyEventText = "Messenger is close";
//                        lastid = pref.getLong("SessionID", 0);
//                        db.SessionDataRecordDao().updateSession(lastid, ScheduleAndSampleManager.getCurrentTimeString(), System.currentTimeMillis());
//                        //                        if(!name.equals("")) {
//                        //                            WriteToFile("Test","Messenger");
//                        //                        }
//                        NewsRelated(false);
//                        Log.d(TAG, "Messenger is close!!");
//                    }
//                }
        }
        if(EventPackage.equals("com.facebook.orca") && messenger)
        {
            if(nodeInfo != null)
            {
                String content = "";
                if(nodeInfo.getContentDescription() != null){
                    content = nodeInfo.getContentDescription().toString();
                }
                Log.d(TAG, "nodeInfo.getContentDescription(): " + content);
                String url = "";
//                    if(name.equals(""))
//                    {
//                        getMessengerName(nodeInfo);//看有沒有抓到名字
//                    }
                if(content.contains("http:"))
                {
                    String[] split_str = content.split("http:");
                    url = "http:" + split_str[1];
                    SharedPreferences pref = getSharedPreferences("URL", MODE_PRIVATE);
                    Set<String> UrlSet = pref.getStringSet("UrlSet", new HashSet<String>());
                    List<String> TitleAndWeb = new ArrayList<String>(UrlSet);

                    if(!TitleAndWeb.contains(url)) {
//                            URLtemp.add(url);
                        Log.d(TAG, "MessengerURL: " + url); //網址，要傳到後端
                        MyEventText = "Get Messenger URL";
//                        if (apptimesStreamGenerator != null) {
//                            apptimesStreamGenerator.updateapptimes("MessengerURL");
//                        }
                        WriteToFile(url, "Messenger");
                    }
                }
                else if(content.contains("https:"))
                {
                    String[] split_str = content.split("https:");
                    url = "https:" + split_str[1];
                    SharedPreferences pref = getSharedPreferences("URL", MODE_PRIVATE);
                    Set<String> UrlSet = pref.getStringSet("UrlSet", new HashSet<String>());
                    List<String> TitleAndWeb = new ArrayList<String>(UrlSet);

                    if(!TitleAndWeb.contains(url)) {
//                            URLtemp.add(url);
                        Log.d(TAG, "MessengerURL: " + url); //網址，要傳到後端
                        MyEventText = "Get Messenger URL";
//                        if (apptimesStreamGenerator != null) {
//                            apptimesStreamGenerator.updateapptimes("MessengerURL");
//                        }
                        WriteToFile(url, "Messenger");
                    }
                }
            }
        }
            /*if(Utils.cancel && messenger)
            {
                messenger = true;
                if (event.getEventType() != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
                        && ((getEventText(event).indexOf("螢幕") >= 0 || getEventText(event).indexOf("主畫面") >= 0
                        || getEventText(event).indexOf("預設頁面") >= 0))) {
                    Log.d(TAG, "messenger Initial");
                    Utils.cancel = false;
                    init_application();
                }
                //案通知欄裡面的訊息，跳到別的app
                else if(event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED
                        && (Arrays.asList(Utils.HomePackage).contains(event.getPackageName().toString())
                        || event.getPackageName().toString().equals("com.android.systemui"))
                        && !Arrays.asList(Utils.Permission_ui).contains(getEventText(event)))
                {
                    Log.d(TAG, "messenger Initial");
                    Utils.cancel = false;
                    init_application();
                }
            }*/
    }
    private void IsYoutubeRunning(int EventType, String EventText, String EventPackage, Intent service_intent, boolean screenshot)
    {
        /*if(event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED
            && event.getPackageName().toString().equals("com.google.android.youtube")
            && now_active.equals("com.google.android.youtube")
            && getEventText(event).equals("前往頻道動作選單"))
        {
            if(nodeInfo != null && nodeInfo.getContentDescription() != null)
            {
                WriteToFile(nodeInfo.getContentDescription().toString());
                youtube_screen = true;
                //Log.d(TAG, "GG: " + nodeInfo.getContentDescription().toString()); //點了甚麼影片，要傳到後端
            }
        }*/
        Log.d(TAG, "is watch video: " + watch_video);
        Log.d(TAG, "youtube: " + youtube);
        if (EventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                && EventPackage.equals("com.google.android.youtube") && EventText.equals("YouTube")
                && !youtube && !Utils.cancel ) { // !watch_video 1/4
            clean_prevApp(state_array, 5);
            state_array[5] = true;
            youtube = true;
            Log.d(TAG, "Youtube is open!!");
            MyEventText = "Youtube is open";
            storeSession("Youtube", "Image");
            NewsRelated(true);

//            CSVHelper.storeToCSV("AccessibilityDetect.csv", "In youtube screen shot trigger, youtube flag is " + youtube);

//            service_intent.putExtra("Youtube", youtube);
            pref.edit().putString("Trigger", "Youtube").apply();
            service_intent.putExtra("FromNotification", false);
            service_intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
//                    try {
//                        Thread.sleep(500);
//                    } catch (Exception e) {
//
//                    }
            startActivity(service_intent);

            SendCaptureNoti("Youtube", youtube, service_intent, screenshot);

//            CSVHelper.storeToCSV("AccessibilityDetect.csv", "Send youtube screen shot notification");
        }
        else if (EventType == AccessibilityEvent.TYPE_VIEW_SCROLLED
                && EventPackage.equals("com.google.android.youtube")
                && !Utils.cancel && !youtube) { // watch_video 1/4
            youtube_count++;
            if(youtube_count >= 3) {
                watch_video = false;
                youtube_count = 0;
//                clean_prevApp(state_array, 5);
//                state_array[5] = true;
//                youtube = true;
                Log.d(TAG, "Youtube is open!!");
//                MyEventText = "Youtube is open";
//                storeSession("Youtube", "Image");
                NewsRelated(true);

//                CSVHelper.storeToCSV("AccessibilityDetect.csv", "In youtube screen shot trigger, youtube flag is " + youtube);

//                service_intent.putExtra("Youtube", youtube);
                pref.edit().putString("Trigger", "Youtube").apply();

                service_intent.putExtra("FromNotification", false);
                service_intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
//                    try {
//                        Thread.sleep(500);
//                    } catch (Exception e) {
//
//                    }
                startActivity(service_intent);

                SendCaptureNoti("Youtube", youtube, service_intent, screenshot);

//                CSVHelper.storeToCSV("AccessibilityDetect.csv", "Send youtube screen shot notification");
            }
        }
        else if (EventPackage.equals("com.google.android.youtube")
                && EventType != AccessibilityEvent.TYPE_VIEW_SCROLLED
                && EventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            youtube_count = 0;
        }
            /*else if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_FOCUSED
                    && event.getPackageName().toString().equals("com.google.android.youtube")
                    && !youtube && !Utils.cancel
                    && getEventText(event).contains("暫停影片")) {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {

                }
                youtube = true;
                Log.d(TAG, "Youtube is open!!");
                service_intent.putExtra("Youtube", youtube);
                service_intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(service_intent);
            }*/
        //            else if (screenshot)
//            {
//                if(youtube) {
//                    //返回桌面
//                    if (EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
//                            && home) {
//                        init_application();
//                        service_intent.putExtra("Youtube", youtube);
//                        stopService(new Intent(getApplicationContext(), ScreenCapture.class));
//                        Log.d(TAG, "Youtube is close!!");
//                        MyEventText = "Youtube is close";
//                        NewsRelated(false);
//                    }
//                    else if(EventType == AccessibilityEvent.TYPE_VIEW_CLICKED //案通知欄裡面的訊息，跳到別的app
//                            && (Arrays.asList(Utils.HomePackage).contains(EventPackage)
//                            || EventPackage.equals("com.android.systemui"))
//                            && !Arrays.asList(Utils.Permission_ui).contains(EventText))
//                    {
//                        init_application();
//                        service_intent.putExtra("Youtube", youtube);
//                        stopService(new Intent(getApplicationContext(), ScreenCapture.class));
//                        Log.d(TAG, "Youtube is close!!");
//                        MyEventText = "Youtube is close";
//                        NewsRelated(false);
//                    }
//                    else if(EventType == AccessibilityEvent.TYPE_VIEW_CLICKED
//                            && EventPackage.equals("com.google.android.youtube")
//                            && (EventText.equals("前往頻道動作選單") || EventText.equals("動作選單")))
//                    {
//                        try {
//                            Log.d(TAG, "delay one second");
//                            Thread.sleep(2000);
//                        } catch (Exception e) {
//
//                        }
//                        init_application();
//                        service_intent.putExtra("Youtube", youtube);
//                        stopService(new Intent(getApplicationContext(), ScreenCapture.class));
//                        Log.d(TAG, "Youtube is close!!");
//                        MyEventText = "Youtube is close";
//                        NewsRelated(false);
//                        /*if(nodeInfo != null && nodeInfo.getContentDescription() != null)
//                        {
//                            WriteToFile(nodeInfo.getContentDescription().toString());
//                            Log.d(TAG, "Title: " + nodeInfo.getContentDescription().toString()); //點了甚麼影片，要傳到後端
//                        }*/
//                    }
//                }
//            }
//            if(Utils.cancel && youtube)
//            {
//                youtube = true;
//                if (EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
//                        && home) {
//                    Log.d(TAG, "youtube Initial");
//                    Utils.cancel = false;
//                    init_application();
//                    MyEventText = "Youtube is close";
//                    NewsRelated(false);
//                }
//                //案通知欄裡面的訊息，跳到別的app
//                else if(EventType == AccessibilityEvent.TYPE_VIEW_CLICKED
//                        && (Arrays.asList(Utils.HomePackage).contains(EventPackage)
//                        || EventPackage.equals("com.android.systemui"))
//                        && !Arrays.asList(Utils.Permission_ui).contains(EventText))
//                {
//                    Log.d(TAG, "youtube Initial");
//                    Utils.cancel = false;
//                    NewsRelated(false);
//                    MyEventText = "Youtube is close";
//                    init_application();
//                }
//            }
    }
    private void IsInstagramRunning(int EventType, String EventText, String EventPackage, Intent service_intent, boolean screenshot)
    {
        if (true) {
            if ((EventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || EventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED )
                    && EventPackage.equals("com.instagram.android") && EventText.equals("Instagram") && !instagram && !Utils.cancel) {
                clean_prevApp(state_array, 4);
                state_array[4] = true;
                instagram = true;
                Log.d(TAG, "Instagram is open!!");
//                CSVHelper.storeToCSV("AccessibilityDetect.csv", "instagram is open");
                MyEventText = "Instagram is open";
//
//                if(apptimesStreamGenerator!=null) {
//                    apptimesStreamGenerator.updateapptimes("InstagramOpen");
//                }

                storeSession("Instagram", "Image");
                NewsRelated(true);

//                CSVHelper.storeToCSV("AccessibilityDetect.csv", "In instagram screen shot trigger, instagram flag is " + instagram);

//                service_intent.putExtra("Instagram", instagram);
                pref.edit().putString("Trigger", "Instagram").apply();

                service_intent.putExtra("FromNotification", false);
                service_intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
//                try {
//                    Thread.sleep(200);
//                } catch (Exception e) {
//
//                }
                startActivity(service_intent);

                SendCaptureNoti("Instagram", instagram, service_intent, screenshot);

//                CSVHelper.storeToCSV("AccessibilityDetect.csv", "Send instagram screen shot notification");
            }
        }
//            else if (screenshot)
//            {
//                if(instagram) {
//                    //返回桌面
//                    if (EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
//                            && home) {
//                        init_application();
//                        service_intent.putExtra("Instagram", instagram);
//                        stopService(new Intent(getApplicationContext(), ScreenCapture.class));
//                        Log.d(TAG, "Instagram is close!!");
//                        MyEventText = "Instagram is close";
//                        NewsRelated(false);
//                    }
//                    //案通知欄裡面的訊息，跳到別的app
//                    else if(EventType == AccessibilityEvent.TYPE_VIEW_CLICKED
//                            && (Arrays.asList(Utils.HomePackage).contains(EventPackage)
//                            || EventPackage.equals("com.android.systemui"))
//                            && !Arrays.asList(Utils.Permission_ui).contains(EventText))
//                    {
//                        init_application();
//                        service_intent.putExtra("Instagram", instagram);
//                        stopService(new Intent(getApplicationContext(), ScreenCapture.class));
//                        Log.d(TAG, "Instagram is close!!");
//                        MyEventText = "Instagram is close";
//                        NewsRelated(false);
//                    }
//                }
//            }
//            if(Utils.cancel && instagram)
//            {
//                instagram = true;
//                if (EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
//                        && home) {
//                    Log.d(TAG, "Instagram Initial");
//                    Utils.cancel = false;
//                    MyEventText = "Instagram is close";
//                    init_application();
//                    NewsRelated(false);
//                }
//                //案通知欄裡面的訊息，跳到別的app
//                else if(EventType == AccessibilityEvent.TYPE_VIEW_CLICKED
//                        && (Arrays.asList(Utils.HomePackage).contains(EventPackage)
//                        || EventPackage.equals("com.android.systemui"))
//                        && !Arrays.asList(Utils.Permission_ui).contains(EventText))
//                {
//                    Log.d(TAG, "Instagram Initial");
//                    Utils.cancel = false;
//                    MyEventText = "Instagram is close";
//                    init_application();
//                    NewsRelated(false);
//                }
//            }
    }
    private void IsNewsAppRunning(int EventType, String EventText, String EventPackage, String now_active, Intent service_intent, boolean screenshot)
    {
        if (EventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                && (Arrays.asList(NewsPack).contains(now_active) && Arrays.asList(NewsPack).contains(EventPackage) )&&
                !news && !Utils.cancel ) {
            // EventText.equals("報導者") && now_active.equals("com.android.chrome")
            clean_prevApp(state_array, 6);
            state_array[6] = true;
            news = true;
//                CSVHelper.storeToCSV("AccessibilityDetect.csv", "news app is open");
            MyEventText = "News App is open";
//                if(apptimesStreamGenerator!=null) {
//                    apptimesStreamGenerator.updateapptimes("NewsappOpen");
//                }
            storeSession("NewsApp","Image");
            NewsRelated(true);

//                CSVHelper.storeToCSV("AccessibilityDetect.csv", "In news app screen shot trigger, news app flag is " + news);

//                service_intent.putExtra("News App", news);
            pref.edit().putString("Trigger", "NewsApp").apply();
            service_intent.putExtra("FromNotification", false);
            service_intent.setFlags(FLAG_ACTIVITY_NEW_TASK);

            Log.d(TAG, "News App is open!!");

            int delay_time = 2000;
            if(now_active.equals("com.ebc.news") || now_active.equals("tw.com.cw.commonwealth.cwapp")){
                delay_time = 2000;
            }
            else{
                delay_time = 1000;
            }

            try {
                Thread.sleep(delay_time);
            } catch (Exception e) {

            }
            startActivity(service_intent);

            SendCaptureNoti("News App", news, service_intent, screenshot);
//                CSVHelper.storeToCSV("AccessibilityDetect.csv", "Send news app screen shot notification");
        }
        //            else if (screenshot)
//            {
//                if(news) {
//                    if (EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
//                            && home) {
//                        init_application();
//                        service_intent.putExtra("News App", news);
//                        stopService(new Intent(getApplicationContext(), ScreenCapture.class));
//                        Log.d(TAG, "NewsApp is close!!");
//                        MyEventText = "News App is close";
//                        NewsRelated(false);
//                    }
//                    else if(EventType == AccessibilityEvent.TYPE_VIEW_CLICKED
//                            && (Arrays.asList(Utils.HomePackage).contains(EventPackage)
//                            || EventPackage.equals("com.android.systemui"))
//                            && !Arrays.asList(Utils.Permission_ui).contains(EventText))
//                    {
//                        init_application();
//                        service_intent.putExtra("News App", news);
//                        stopService(new Intent(getApplicationContext(), ScreenCapture.class));
//                        Log.d(TAG, "NewsApp is close!!");
//                        MyEventText = "News App is close";
//                        NewsRelated(false);
//                    }
//                }
//            }
//            if(Utils.cancel  && news)
//            {
//                news = true;
//                if (EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
//                        && home) {
//                    Log.d(TAG, "News app Initial");
//                    Utils.cancel = false;
//                    NewsRelated(false);
//                    MyEventText = "News App is close";
//                    init_application();
//                }
//                else if(EventType == AccessibilityEvent.TYPE_VIEW_CLICKED
//                        && (Arrays.asList(Utils.HomePackage).contains(EventPackage)
//                        || EventPackage.equals("com.android.systemui"))
//                        && !Arrays.asList(Utils.Permission_ui).contains(EventText))
//                {
//                    Log.d(TAG, "News app Initial");
//                    Utils.cancel = false;
//                    NewsRelated(false);
//                    MyEventText = "News App is close";
//                    init_application();
//                }
//            }
    }

    private void IsPTTAppRunning(int EventType, String EventText, String EventPackage, String now_active, boolean screenshot, AccessibilityEvent event)
    {//2 true
        boolean change_app = false;
//        if(true){
//            AccessibilityNodeInfo nodeInfo = event.getSource();
//            dfs(nodeInfo);
//        }
        if(ScreenCaptureTransition[1] && !screenshot){
            storeSession("PTT", "Title");
            NewsRelated(true);
            ScreenCaptureTransition[1] = false;
        }
        if(!ptt){
            if (EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
                    && Arrays.asList(PttPack).contains(EventPackage)) {
//                CSVHelper.storeToCSV("AccessibilityDetect.csv",  "PTT is open");
//                try {
//                    Thread.sleep(500);
//                } catch (Exception e) {
//
//                }
                clean_prevApp(state_array, 8);
                state_array[8] = true;
                ptt = true;
                MyEventText = "PPT is open";
                ScreenCaptureTransition[1] = true;
                Log.d(TAG, "PPT is open!!");
//                CSVHelper.storeToCSV("AccessibilityDetect.csv", "In ptt not running scope, ptt flag is " + ptt);

//                storeSession("PTT", "Title");
//                NewsRelated(true);
            }
        }
        else if(ptt) {
            if(EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED && !home
                    && EventPackage.equals("com.facebook.orca")){
//                CSVHelper.storeToCSV("AccessibilityDetect.csv",  "PTT interrupted by messenger");
                home = true;
            }
            if(!Arrays.asList(PttPack).contains(now_active) && EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
                    && !now_active.equals("android") && !now_active.equals("com.android.systemui") && !now_active.equals("com.example.accessibility_detect")
                    && !Arrays.asList(PttPack).contains(EventPackage) && !EventPackage.equals("android") && EventPackage.equals(now_active)
                    && !EventPackage.equals("com.android.systemui") && !EventPackage.equals("com.example.accessibility_detect")){
                change_app = true;
            }
            boolean question_interrupt = pref.getBoolean("Question_interrupt",false);

            if (EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
                    && (home || change_app || question_interrupt)) {
//                CSVHelper.storeToCSV("AccessibilityDetect.csv",  "PTT is close(home condition)");
                pref.edit().putBoolean("Question_interrupt",false).apply();
                state_array[8] = false;
                init_application();
                lastid = pref.getLong("SessionID", 0);
                db.SessionDataRecordDao().updateSession(lastid, ScheduleAndSampleManager.getCurrentTimeString(), System.currentTimeMillis());

                Log.d(TAG, "Ptt is close!!");
                MyEventText = "Ptt is close";
//                    WriteToFile("Test", "PTT");
                NewsRelated(false);
                ESMjump("PTT");
//                CSVHelper.storeToCSV("AccessibilityDetect.csv", "In ptt running scope, ptt flag is " + ptt + " ( " + home +"," + change_app + " )");
            }
            else{ //如果不是離開ptt的話
                if(EventPackage.equals("com.ihad.ptt")) {
                    AccessibilityNodeInfo nodeInfo = event.getSource();
                    dfs(nodeInfo);
                }
            }
//            else if(
//                    false
////                    EventType == AccessibilityEvent.TYPE_VIEW_CLICKED
////                    && (Arrays.asList(HomePackage).contains(EventPackage)
////                    || EventPackage.equals("com.android.systemui"))
////                    && !Arrays.asList(Utils.Permission_ui).contains(EventText)
//                )
//            {
//                CSVHelper.storeToCSV("AccessibilityDetect.csv",  "PTT is close(noti condition)");
//
//                init_application();
//                state_array[8] = false;
//                lastid = pref.getLong("SessionID", 0);
//                db.SessionDataRecordDao().updateSession(lastid, ScheduleAndSampleManager.getCurrentTimeString(), System.currentTimeMillis());
////                    WriteToFile("Test", "PTT");
//                Log.d(TAG, "Ptt is close!!");
//                MyEventText = "Ptt is close";
//                NewsRelated(false);
//            }
        }
        if (EventType == AccessibilityEvent.TYPE_VIEW_CLICKED && Arrays.asList(PttPack).contains(now_active)
                && Arrays.asList(PttPack).contains(EventPackage) && ptt) {
            if(EventText.contains("新聞") && !EventPackage.equals("com.ihad.ptt"))
            {
                Log.d(TAG, "Origin news: " + EventText);
//                    URLtemp.add(EventText);
//                if(apptimesStreamGenerator!=null) {
//                    apptimesStreamGenerator.updateapptimes("PTTtitle");
//                }
                WriteToFile(EventText, "PTT");
                Log.d(TAG, "News In PTT: " + EventText);
            }
        }
    }
    private void IsLineRunning(int EventType, String EventText, String EventPackage, Intent service_intent, boolean screenshot)
    {
        String today = getSharedPreferences("test", MODE_PRIVATE)
                .getString("Line", "");
        if (true) {
            if ((EventType == AccessibilityEvent.TYPE_VIEW_SELECTED &&
                    EventPackage.equals("jp.naver.line.android") && EventText.equals("TODAY")
                    && !line && !Utils.cancel)) {
//                CSVHelper.storeToCSV("AccessibilityDetect.csv", "LineToday call startActivity time: " + (System.currentTimeMillis()));

                clean_prevApp(state_array, 7);
                state_array[7] = true;
                Utils.first = false;
                line = true;
                Log.d(TAG, "LineToday is open!!");
//                CSVHelper.storeToCSV("AccessibilityDetect.csv", "Line Today is open");
                MyEventText = "LineToday is open";
//                if (apptimesStreamGenerator != null) {
//                    apptimesStreamGenerator.updateapptimes("LinetodayOpen");
//                }

                storeSession("LineToday","Image");
                NewsRelated(true);

//                CSVHelper.storeToCSV("AccessibilityDetect.csv", "In LineToday screen shot trigger, LineToday flag is " + line);

//                service_intent.putExtra("LineToday", line);
                pref.edit().putString("Trigger", "LineToday").apply();
                service_intent.putExtra("FromNotification", false);

                service_intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
//                try {
//                    Thread.sleep(500);
//                } catch (Exception e) {
//
//                }
                startActivity(service_intent);

                SendCaptureNoti("LineToday", line, service_intent, screenshot);
//                CSVHelper.storeToCSV("AccessibilityDetect.csv", "Send LineToday screen shot notification");
            }
            else if ((EventType != AccessibilityEvent.TYPE_VIEW_SELECTED
                    && EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
                    && today.equals("TODAY") && (EventPackage.equals("jp.naver.line.android"))
                    && !line && Utils.first)) {//出去再進來
//                CSVHelper.storeToCSV("AccessibilityDetect.csv", "LineToday call startActivity time: " + (System.currentTimeMillis()));


                clean_prevApp(state_array, 7);
                state_array[7] = true;
                Utils.first = false;
                line = true;
                Log.d(TAG, "LineToday is open!!");
//                CSVHelper.storeToCSV("AccessibilityDetect.csv", "Line Today is open");
                MyEventText = "LineToday is open";
//                if (apptimesStreamGenerator != null) {
//                    apptimesStreamGenerator.updateapptimes("LinetodayOpen");
//                }

                storeSession("LineToday","Image");
                NewsRelated(true);

//                CSVHelper.storeToCSV("AccessibilityDetect.csv", "In LineToday screen shot trigger, LineToday flag is " + line);

//                service_intent.putExtra("LineToday", line);
                pref.edit().putString("Trigger", "LineToday").apply();

                service_intent.putExtra("FromNotification", false);
                service_intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                try {
                    Thread.sleep(2000);
                } catch (Exception e) {

                }
                startActivity(service_intent);

                SendCaptureNoti("LineToday", line, service_intent, screenshot);
//                CSVHelper.storeToCSV("AccessibilityDetect.csv", "Send LineToday screen shot notification");
            }
        }
//            else if (screenshot) {
//                if (line) {
//                    if (home || ((EventPackage.equals("jp.naver.line.android")
//                            && (EventType == AccessibilityEvent.TYPE_VIEW_SELECTED && (EventText.equals("主頁") || EventText.equals("聊天") ||
//                            EventText.equals("貼文串") || EventText.equals("錢包")))))) {
//                        Utils.first = true;
//                        init_application();
//                        intent.putExtra("LineToday", line);
//                        stopService(new Intent(getApplicationContext(), ScreenCapture.class));
//                        Log.d(TAG, "LineToday is close!!");
//                        MyEventText = "LineToday is close";
//                        NewsRelated(false);
//                    } else if (EventType == AccessibilityEvent.TYPE_VIEW_CLICKED
//                            && (Arrays.asList(Utils.HomePackage).contains(EventPackage)
//                            || EventPackage.equals("com.android.systemui"))
//                            && !Arrays.asList(Utils.Permission_ui).contains(EventText)) {
//                        Utils.first = true;
//                        init_application();
//                        intent.putExtra("LineToday", line);
//                        stopService(new Intent(getApplicationContext(), ScreenCapture.class));
//                        Log.d(TAG, "LineToday is close!!");
//                        MyEventText = "LineToday is close";
//                        NewsRelated(false);
//                    }
//                }
//            }
//            if (Utils.cancel && line) {
//                line = true;
//                if (home || ((EventPackage.equals("jp.naver.line.android")
//                        && (EventType == AccessibilityEvent.TYPE_VIEW_SELECTED && (EventText.equals("主頁") || EventText.equals("聊天") ||
//                        EventText.equals("貼文串") || EventText.equals("錢包")))))) {
//                    Log.d(TAG, "line Initial");
//                    Utils.first = true;
//                    Utils.cancel = false;
//                    NewsRelated(false);
//                    MyEventText = "LineToday is close";
//                    init_application();
//                } else if (EventType == AccessibilityEvent.TYPE_VIEW_CLICKED
//                        && (Arrays.asList(Utils.HomePackage).contains(EventPackage)
//                        || EventPackage.equals("com.android.systemui"))
//                        && !Arrays.asList(Utils.Permission_ui).contains(EventText)) {
//                    Utils.first = true;
//                    Utils.cancel = false;
//                    NewsRelated(false);
//                    MyEventText = "LineToday is close";
//                    init_application();
//                }
//            }
    }
    private void IsGoogleRunning(int EventType, String EventText, String EventPackage, Intent service_intent, boolean screenshot, String now_active)
    {
        ApplicationInfo appInfo = null;
        ApplicationInfo nowappInfo = null;
        try {
            appInfo = this.getPackageManager().getApplicationInfo(EventPackage, 0);
            nowappInfo = this.getPackageManager().getApplicationInfo(now_active, 0);
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (true && !google) {
            //Log.d(TAG, "google first: " + Utils.google_first);
            //Log.d(TAG, "cancel: " + Utils.cancel);
            if(EventPackage.equals("com.google.android.apps.magazines") && (EventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || EventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED)
                    && (EventText.equals("為你推薦") || EventText.equals("頭條新聞") || EventText.equals("追蹤中") || EventText.equals("書報攤"))){
                clean_prevApp(state_array, 0);
                state_array[0] = true;
                googlebox_count = 0;
                googlebox_home = 0;
                Utils.google_first = false;
                google = true;
                Log.d(TAG, "GoogleNews is open!!");
                MyEventText = "GoogleNews is open";
//                if(apptimesStreamGenerator!=null) {
//                    apptimesStreamGenerator.updateapptimes("GooglenowOpen");
//                }

                storeSession("googleNews","Image");
                NewsRelated(true);

//                CSVHelper.storeToCSV("AccessibilityDetect.csv", "In googleNews screen shot trigger, googleNews flag is " + google);

//                service_intent.putExtra("googleNews", google);
                pref.edit().putString("Trigger", "googleNews").apply();

                service_intent.putExtra("FromNotification", false);

                service_intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(service_intent);

                SendCaptureNoti("googleNews", google, service_intent, screenshot);
//                CSVHelper.storeToCSV("AccessibilityDetect.csv", "Send googleNews screen shot notification");
            }
            else if ((EventPackage.equals("com.google.android.googlequicksearchbox") )
                    && !google && !Utils.cancel && Utils.google_first && EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
                googlebox_count++;
                Log.d(TAG, "googlebox_count: " + googlebox_count);
                if (googlebox_count >= 3) {
//                    try {
//                        Thread.sleep(500);
//                    } catch (Exception e) {
//
//                    }
                    clean_prevApp(state_array, 0);
                    state_array[0] = true;
                    googlebox_count = 0;
                    googlebox_home = 0;
                    Utils.google_first = false;
                    google = true;
                    reading = false;
                    Log.d(TAG, "GoogleNews is open!!");
                    CSVHelper.storeToCSV("AccessibilityDetect.csv", "google now is open");
                    MyEventText = "GoogleNews is open";
//                    if(apptimesStreamGenerator!=null) {
//                        apptimesStreamGenerator.updateapptimes("GooglenowOpen");
//                    }

                    storeSession("googleNews","Image");
                    NewsRelated(true);

//                    CSVHelper.storeToCSV("AccessibilityDetect.csv", "In googleNews screen shot trigger, googleNews flag is " + google);

//                    service_intent.putExtra("googleNews", google);
                    pref.edit().putString("Trigger", "googleNews").apply();
                    service_intent.putExtra("FromNotification", false);
                    service_intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                    startActivity(service_intent);

                    SendCaptureNoti("googleNews", google, service_intent, screenshot);
//                    CSVHelper.storeToCSV("AccessibilityDetect.csv", "Send googleNews screen shot notification");
                }
            }
            else if (!EventPackage.equals("com.google.android.googlequicksearchbox")) {
                googlebox_count = 0;
                Utils.google_first = true;
//                CSVHelper.storeToCSV("AccessibilityDetect.csv", "google search box count set to 0, first time set to true");
            }
        }
        else if ((screenshot || google) && !Utils.cancel)//screenshot
        {
            Log.d(TAG, "Google state: " + google + " " + home + " " + reading + " " + Utils.google_first);
//            CSVHelper.storeToCSV("AccessibilityDetect.csv", "Google state: " + google + " " + home + " " + reading + " " + Utils.google_first);
            if(google) {
                boolean change_app = false;
                if(appInfo != null && nowappInfo != null) {
                    if (!((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0) && !((nowappInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0)
                            && EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
                            && !EventPackage.equals("com.example.accessibility_detect") && !now_active.equals("com.example.accessibility_detect")
                            && !EventPackage.equals("com.google.android.apps.magazines") && !now_active.equals("com.google.android.apps.magazines")) {
                        change_app = true;
                    }

                }
                if((EventPackage.equals("jp.naver.line.android") || EventPackage.equals("com.facebook.orca"))
                        && EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED){
                    change_app = true;
                }
                if(EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED && !home
                        && change_app){
                    reading = false;
                    home = true;
//                    CSVHelper.storeToCSV("AccessibilityDetect.csv",  "Google now interrupted by messenger, reading set to false, home set to true");
                }
//                Log.d(TAG, "Home: " + home + " " + change_app );
                boolean question_interrupt = pref.getBoolean("Question_interrupt",false);

                if ((home || question_interrupt) && !reading) {
                    pref.edit().putBoolean("Question_interrupt", false).apply();
                    init_application();
                    state_array[0] = false;
                    Utils.google_first = true;
                    service_intent.putExtra("googleNews", google);
                    pref.edit().putString("Trigger", "").apply();

                    service_intent.putExtra("FromNotification", false);

                    CSVHelper.storeToCSV("AccessibilityDetect.csv", "In google stop screen shot, screen shot now is true, google flag is " + google + " ( " + home +"," + reading + " )");

                    notificationListenService.updateRecordingNotification(this, Accessibility_ID, "googleNews", true, intent);

//                    CSVHelper.storeToCSV("AccessibilityDetect.csv", "Send google screen shot update notification");

//                    mNotificationManager.cancel(Accessibility_ID);
                    stopService(new Intent(getApplicationContext(), ScreenCapture.class));
//                    lastid = pref.getLong("SessionID", 0);
//                    db.SessionDataRecordDao().updateSession(lastid, ScheduleAndSampleManager.getCurrentTimeString(), System.currentTimeMillis());
                    Log.d(TAG, "GoogleNews is close!!");
                    MyEventText = "GoogleNews is close";
                    endSession();
                    ESMjump("GoogleNews");
//                    NewsRelated(false);
                }
                else if(home && reading){
                    CSVHelper.storeToCSV("AccessibilityDetect.csv",  "Google now is not reading");
//                    googlebox_home++;
//                    if(googlebox_home >= 2){
//                        googlebox_home = 0;
//                        home_first = false;
//                        reading = false;
//                    }
                    home_first = false;
                    reading = false;
//                    CSVHelper.storeToCSV("AccessibilityDetect.csv", "In google stop screen shot, screen shot now is true, google reading is set to false, first time is set to false");
                }
                if(!home){
                    home_first = true;
                }
//                else if (reading && EventPackage.equals("com.google.android.googlequicksearchbox")) {
//                    googlebox_count++;
//                    Log.d(TAG, "googlebox_count in reading: " + googlebox_count);
//                    if (googlebox_count >= 3) {
//                        CSVHelper.storeToCSV("AccessibilityDetect.csv",  "Google now is not reading");
//
//                        reading = false;
//                    }
//                }
//                else if (!EventPackage.equals("com.google.android.googlequicksearchbox")) {
//                    googlebox_count = 0;
//                }
                if(
                        false
//                        EventType == AccessibilityEvent.TYPE_VIEW_CLICKED
//                        && (Arrays.asList(HomePackage).contains(EventPackage)
//                        || EventPackage.equals("com.android.systemui"))
//                        && !Arrays.asList(Utils.Permission_ui).contains(EventText)
                )
                {
//                    CSVHelper.storeToCSV("AccessibilityDetect.csv",  "Google now is close(screen shot, noti condition)");

                    init_application();
                    state_array[0] = false;
                    Utils.google_first = true;
                    reading = false;
                    service_intent.putExtra("googleNews", google);
//                    mNotificationManager.cancel(Accessibility_ID);
                    stopService(new Intent(getApplicationContext(), ScreenCapture.class));
                    lastid = pref.getLong("SessionID", 0);
                    db.SessionDataRecordDao().updateSession(lastid, ScheduleAndSampleManager.getCurrentTimeString(), System.currentTimeMillis());
                    Log.d(TAG, "GoogleNews is close!!");
                    MyEventText = "GoogleNews is close";
                    NewsRelated(false);
                }
            }
        }
        Log.d(TAG, "reading: " + reading);
        if(EventType == AccessibilityEvent.TYPE_VIEW_CLICKED && EventPackage.equals("com.google.android.googlequicksearchbox"))
        {
            reading = true;
            googlebox_home = 0;
            CSVHelper.storeToCSV("AccessibilityDetect.csv",  "Google reading is set to true");
        }
        if(Utils.cancel && google)
        {
            google = true;
//            CSVHelper.storeToCSV("AccessibilityDetect.csv", "Google state: " + google + " " + home + " " + reading + " " + Utils.google_first);

            if(EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED && !home
                    && EventPackage.equals("com.facebook.orca")){
//                CSVHelper.storeToCSV("AccessibilityDetect.csv",  "Google now interrupted by messenger");

                reading = false;
                home = true;
//                CSVHelper.storeToCSV("AccessibilityDetect.csv",  "Google now interrupted by messenger, reading set to false, home set to true");
            }
            boolean question_interrupt = pref.getBoolean("Question_interrupt",false);

            if ((home || question_interrupt) && !reading){
                CSVHelper.storeToCSV("AccessibilityDetect.csv",  "Google now is close(cancel, home condition)");
                pref.edit().putBoolean("Question_interrupt", false).apply();
                Utils.google_first = true;
                state_array[0] = false;
                Log.d(TAG, "google Initial");
                Utils.cancel = false;
                init_application();
//                CSVHelper.storeToCSV("AccessibilityDetect.csv", "In google cancel scope,  google flag is " + google + "cancel is set to false ( " + home +"," + reading + " )");

//                mNotificationManager.cancel(Accessibility_ID);
//                lastid = pref.getLong("SessionID", 0);
//                db.SessionDataRecordDao().updateSession(lastid, ScheduleAndSampleManager.getCurrentTimeString(), System.currentTimeMillis());
                MyEventText = "GoogleNews is close";
//                NewsRelated(false);
                notificationListenService.updateRecordingNotification(this, Accessibility_ID, "googleNews", true, intent);
//                CSVHelper.storeToCSV("AccessibilityDetect.csv", "Send google screen shot update notification");
                endSession();
                ESMjump("GoogleNews");
            }
            else if(home && reading)
            {
                CSVHelper.storeToCSV("AccessibilityDetect.csv",  "Google now is not reading (cancel)");
//                googlebox_home++;
//                if(googlebox_home >= 2){
//                    googlebox_home = 0;
//                    home_first = false;
//                    reading = false;
//                }
                home_first = false;
                reading = false;
//                CSVHelper.storeToCSV("AccessibilityDetect.csv", "In google cancel scope, google reading is set to false, first time is set to false");

            }
            if(!home){
                home_first = true;
            }
            if(
                    false
//                    EventType == AccessibilityEvent.TYPE_VIEW_CLICKED
//                    && (Arrays.asList(HomePackage).contains(EventPackage)
//                    || EventPackage.equals("com.android.systemui"))
//                    && !Arrays.asList(Utils.Permission_ui).contains(EventText)
            )
            {
//                CSVHelper.storeToCSV("AccessibilityDetect.csv",  "Google now is close(cancel, noti condition)");

                Utils.google_first = true;
                state_array[0] = false;
                Log.d(TAG, "google Initial");
//                mNotificationManager.cancel(Accessibility_ID);
                Utils.cancel = false;
                init_application();
//                lastid = pref.getLong("SessionID", 0);
//                db.SessionDataRecordDao().updateSession(lastid, ScheduleAndSampleManager.getCurrentTimeString(), System.currentTimeMillis());
                reading = false;
                MyEventText = "GoogleNews is close";
//                NewsRelated(false);
            }
        }
    }
    private void IsLineMesRunning(int EventType, String EventText, String EventPackage,  AccessibilityNodeInfo nodeInfo, boolean screenshot, String now_active)
    {//要找個方法不點bubble，滑一下line session要切回來
        boolean change_app = false;
        if(ScreenCaptureTransition[2] && !screenshot){
            storeSession("LineChat", "Url");
            NewsRelated(true);
            ScreenCaptureTransition[2] = false;
        }
        if (!line_mes) {
            String today = getSharedPreferences("test", MODE_PRIVATE)
                    .getString("Line", "");
            if (EventType == AccessibilityEvent.TYPE_VIEW_FOCUSED
                    && EventPackage.equals("jp.naver.line.android") && EventText.equals("文字欄位，")) {
//                try {
//                    Thread.sleep(500);
//                } catch (Exception e) {
//
//                }
                pref.edit().putString("Line", "聊天").apply();
                clean_prevApp(state_array, 2);
                state_array[2] = true;
                Utils.LineMes_first = false;
                line_mes = true;
                exit_room = true;
                Log.d(TAG, "LineMes is open!!");
//                CSVHelper.storeToCSV("AccessibilityDetect.csv", "Line mes is open");
                MyEventText = "LineMes is open";
                ScreenCaptureTransition[2] = true;
//                CSVHelper.storeToCSV("AccessibilityDetect.csv", "In line mes not running scope, line mes flag is " + line_mes);

                if(screenshot){
                    stopService(new Intent(getApplicationContext(), ScreenCapture.class));
                }
//                storeSession("LineChat","Url");
//                NewsRelated(true);
            }
            else if(line_interrupted_by_messenger && EventPackage.equals("jp.naver.line.android") && today.equals("聊天")){
                line_interrupted_by_messenger = false;
//                try {
//                    Thread.sleep(500);
//                } catch (Exception e) {
//
//                }
                pref.edit().putString("Line", "聊天").apply();
                clean_prevApp(state_array, 2);
                state_array[2] = true;
                Utils.LineMes_first = false;
                line_mes = true;
                Log.d(TAG, "LineMes is open!!");
//                CSVHelper.storeToCSV("AccessibilityDetect.csv", "Line mes is open");
                MyEventText = "LineMes is open";
                ScreenCaptureTransition[2] = true;
//                CSVHelper.storeToCSV("AccessibilityDetect.csv", "In line mes not running scope, line mes flag is " + line_mes);
                if(screenshot){
                    stopService(new Intent(getApplicationContext(), ScreenCapture.class));
                }
//                storeSession("LineChat","Url");
//                NewsRelated(true);
            }
        }
        else if (line_mes)
        {
            if(EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED && !home
                    && EventPackage.equals("com.facebook.orca")){
//                CSVHelper.storeToCSV("AccessibilityDetect.csv",  "LIne Chat interrupted by messenger");
                line_interrupted_by_messenger = true;
                home = true;
            }
            if(!now_active.equals("jp.naver.line.android") && EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
                    && !now_active.equals("android") && !now_active.equals("com.android.systemui") && !now_active.equals("com.example.accessibility_detect")
                    && !EventPackage.equals("jp.naver.line.android") && !EventPackage.equals("android") && EventPackage.equals(now_active)
                    && !EventPackage.equals("com.android.systemui") && !EventPackage.equals("com.example.accessibility_detect")){
                change_app = true;
            }
            boolean question_interrupt = pref.getBoolean("Question_interrupt",false);

            if((home || change_app || question_interrupt) && EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
//                    CSVHelper.storeToCSV("AccessibilityDetect.csv",  "Line Chat is close(home condition)");
                    pref.edit().putBoolean("Question_interrupt",false).apply();
                    Utils.LineMes_first = true;
                    exit_room = false;
                    state_array[2] = false;
                    init_application();
                    MyEventText = "LineMes is close";
//                        if(!name.equals("")) {
//                            WriteToFile("Test","Line");
//                        }
                    NewsRelated(false);
                    lastid = pref.getLong("SessionID", 0);
                    db.SessionDataRecordDao().updateSession(lastid, ScheduleAndSampleManager.getCurrentTimeString(), System.currentTimeMillis());
                    Log.d(TAG, "LineMes is close!!");
//                    ESMjump("LineMes");
//                    CSVHelper.storeToCSV("AccessibilityDetect.csv", "In line mes running scope, line mes flag is " + line_mes + " ( " + home +"," + change_app + " )");
                }
//                else if(
//                        false
////                        EventType == AccessibilityEvent.TYPE_VIEW_CLICKED
////                        && (Arrays.asList(HomePackage).contains(EventPackage)
////                        || EventPackage.equals("com.android.systemui"))
////                        && !Arrays.asList(Utils.Permission_ui).contains(EventText)
//                )
//                {
//                    CSVHelper.storeToCSV("AccessibilityDetect.csv",  "Line Chat is close(noti condition)");
//
//                    Utils.LineMes_first = true;
//                    state_array[2] = false;
//                    init_application();
//                    MyEventText = "LineMes is close";
////                        if(!name.equals("")) {
////                            WriteToFile("Test","Line");
////                        }
//                    NewsRelated(false);
//                    lastid = pref.getLong("SessionID", 0);
//                    db.SessionDataRecordDao().updateSession(lastid, ScheduleAndSampleManager.getCurrentTimeString(), System.currentTimeMillis());
//                    ESMjump("LineMes");
//                    Log.d(TAG, "LineMes is close!!");
//                }
                else if(EventPackage.equals("jp.naver.line.android")
                        && (EventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED))
                {
                    if(nodeInfo != null && nodeInfo.getContentDescription() != null)
                    {
                        if(nodeInfo.getContentDescription().toString().equals("開始聊天 按鍵"))
                        {
//                            CSVHelper.storeToCSV("AccessibilityDetect.csv",  "Line Chat is close(out chat room)");

                            Utils.LineMes_first = true;
                            exit_room = false;
                            state_array[2] = false;
                            init_application();
                            MyEventText = "LineMes is close";
//                                if(!name.equals("")) {
//                                    WriteToFile("Test","Line");
//                                }
                            NewsRelated(false);
                            lastid = pref.getLong("SessionID", 0);
                            db.SessionDataRecordDao().updateSession(lastid, ScheduleAndSampleManager.getCurrentTimeString(), System.currentTimeMillis());
                            Log.d(TAG, "LineMes is close!!");
//                            ESMjump("LineMes");
//                            CSVHelper.storeToCSV("AccessibilityDetect.csv", "In line mes running scope, line mes flag is " + line_mes + " ( " + home +"," + change_app + " )");
                        }
                    }
                }
        }
        if(!exit_room){
            if(EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED && !home
                    && EventPackage.equals("com.facebook.orca")){
//                CSVHelper.storeToCSV("AccessibilityDetect.csv",  "LIne Chat interrupted by messenger");
                line_interrupted_by_messenger = true;
                home = true;
            }
            if(!now_active.equals("jp.naver.line.android") && EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
                    && !now_active.equals("android") && !now_active.equals("com.android.systemui") && !now_active.equals("com.example.accessibility_detect")
                    && !EventPackage.equals("jp.naver.line.android") && !EventPackage.equals("android") && EventPackage.equals(now_active)
                    && !EventPackage.equals("com.android.systemui") && !EventPackage.equals("com.example.accessibility_detect")){
                change_app = true;
            }
            boolean question_interrupt = pref.getBoolean("Question_interrupt",false);

            if((home || change_app || question_interrupt ) && EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
                pref.edit().putBoolean("Question_interrupt",false).apply();
                ESMjump("LineMes");
                exit_room = true;
            }
        }
            /*if(Utils.cancel && line_mes)
            {
                line_mes = true;
                if(home && event.getEventType() != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
                    Log.d(TAG, "line Initial");
                    Utils.LineMes_first = true;
                    Utils.cancel = false;
                    init_application();
                }
                else if(event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED
                        && (Arrays.asList(Utils.HomePackage).contains(event.getPackageName().toString())
                        || event.getPackageName().toString().equals("com.android.systemui"))
                        && !Arrays.asList(Utils.Permission_ui).contains(getEventText(event)))
                {
                    Log.d(TAG, "line Initial");
                    Utils.LineMes_first = true;
                    Utils.cancel = false;
                    init_application();
                }
                else if(event.getPackageName().toString().equals("jp.naver.line.android")
                        && (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED))
                {
                    if(nodeInfo != null && nodeInfo.getContentDescription() != null)
                    {
                        if(nodeInfo.getContentDescription().toString().equals("開始聊天 按鍵"))
                        {
                            Log.d(TAG, "line Initial");
                            Utils.LineMes_first = true;
                            Utils.cancel = false;
                            init_application();
                        }
                    }
                }
            }*/
        if (EventPackage.equals("jp.naver.line.android") && line_mes) {
            try {
                if (nodeInfo != null) {
//                    if(name.equals(""))
//                    {
//                        getLineName(nodeInfo);//看有沒有抓到名字
//                    }
                    for (int i = 0; i < nodeInfo.getChildCount(); i++) {
                        if (nodeInfo.getChild(i) != null) {
                            for (int j = 0; j < nodeInfo.getChild(i).getChildCount(); j++) {
                                if (nodeInfo.getChild(i).getChild(j) != null) {
                                    for (int k = 0; k < nodeInfo.getChild(i).getChild(j).getChildCount(); k++) {
                                        if (nodeInfo.getChild(i).getChild(j).getChild(k) != null) {
                                            if (nodeInfo.getChild(i).getChild(j).getChild(k).getText() != null) {
                                                String content = nodeInfo.getChild(i).getChild(j).getChild(k).getText().toString();
                                                String url = "";
                                                if (content.contains("http:")) {
                                                    String[] split_str = content.split("http:");
                                                    url = "http:" + split_str[1];
                                                    SharedPreferences pref = getSharedPreferences("URL", MODE_PRIVATE);
                                                    Set<String> UrlSet = pref.getStringSet("UrlSet", new HashSet<String>());
                                                    List<String> TitleAndWeb = new ArrayList<String>(UrlSet);

                                                    if (!TitleAndWeb.contains(url)) {
//                                                        URLtemp.add(url);
                                                        Log.d(TAG, "LineURL: " + url); //網址，要傳到後端
                                                        MyEventText = "Get Line Message URL";
//                                                    if (apptimesStreamGenerator != null) {
//                                                        apptimesStreamGenerator.updateapptimes("LineUrl");
//                                                    }
                                                        WriteToFile(url, "LineChat");
                                                    }
                                                } else if (content.contains("https:")) {
                                                    String[] split_str = content.split("https:");
                                                    url = "https:" + split_str[1];
                                                    SharedPreferences pref = getSharedPreferences("URL", MODE_PRIVATE);
                                                    Set<String> UrlSet = pref.getStringSet("UrlSet", new HashSet<String>());
                                                    List<String> TitleAndWeb = new ArrayList<String>(UrlSet);

                                                    if (!TitleAndWeb.contains(url)) {
//                                                        URLtemp.add(url);
                                                        Log.d(TAG, "LineURL: " + url); //網址，要傳到後端
                                                        MyEventText = "Get Line Message URL";
//                                                    if (apptimesStreamGenerator != null) {
//                                                        apptimesStreamGenerator.updateapptimes("LineUrl");
//                                                    }
                                                        WriteToFile(url, "LineChat");
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            catch(NullPointerException e){
                e.printStackTrace();
            }
        }
    }
    private void IsWebBrowserRunning(int EventType, String EventText, String EventPackage, Intent service_intent, boolean screenshot, String now_active)
    {
        if (true && !google) {
            if ((EventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED || EventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)
                    && Arrays.asList(WebPack).contains(EventPackage) && Arrays.asList(WebEvent).contains(EventText) && !chrome && !Utils.cancel) {

                clean_prevApp(state_array, 3);
                state_array[3] = true;
                chrome = true;
                MyEventText = "Chrome is open";

                Log.d(TAG, "Chrome is open!!");
//                CSVHelper.storeToCSV("AccessibilityDetect.csv", "chrome is open");
//                if (apptimesStreamGenerator!=null)
//                {
//                    apptimesStreamGenerator.updateapptimes("ChromeOpen");
//                }

                storeSession("Chrome","Image");
                NewsRelated(true);

//                CSVHelper.storeToCSV("AccessibilityDetect.csv", "In chrome screen shot trigger, chrome flag is " + chrome);

//                service_intent.putExtra("Chrome", chrome);
                pref.edit().putString("Trigger", "Chrome").apply();
                service_intent.putExtra("FromNotification", false);
                service_intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
//                try {
//                    Thread.sleep(500);
//                } catch (Exception e) {
//
//                }
                startActivity(service_intent);

                SendCaptureNoti("Chrome", chrome, service_intent, screenshot);
//                CSVHelper.storeToCSV("AccessibilityDetect.csv", "Send chrome screen shot notification");
            }
        }
//        else if (screenshot)
//        {
//            if(chrome) {
//                //返回桌面
//                if ((EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
//                        && home) || (!now_active.equals("com.android.chrome") && !now_active.equals("com.example.accessibility_detect"))) {
//                        init_application();
//                        service_intent.putExtra("Chrome", chrome);
//                        stopService(new Intent(getApplicationContext(), ScreenCapture.class));
//                        MyEventText = "Chrome is close";
//                        Log.d(TAG, "Chrome is close!!");
//                        NewsRelated(false);
//                }
//                //案通知欄裡面的訊息，跳到別的app
//                else if(EventType == AccessibilityEvent.TYPE_VIEW_CLICKED
//                        && (Arrays.asList(Utils.HomePackage).contains(EventPackage)
//                        || EventPackage.equals("com.android.systemui"))
//                        && !Arrays.asList(Utils.Permission_ui).contains(EventText))
//                {
//                    init_application();
//                    service_intent.putExtra("Chrome", chrome);
//                    stopService(new Intent(getApplicationContext(), ScreenCapture.class));
//                    Log.d(TAG, "Chrome is close!!");
//                    MyEventText = "Chrome is close";
//                    NewsRelated(false);
//                }
//            }
//        }
//        if(Utils.cancel && chrome)
//        {
//            chrome = true;
//            if (EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
//                    && home) {
//                Log.d(TAG, "chrome Initial");
//                MyEventText = "Chrome is close";
//                Utils.cancel = false;
//                init_application();
//                NewsRelated(false);
//            }
//            //案通知欄裡面的訊息，跳到別的app
//            else if(EventType == AccessibilityEvent.TYPE_VIEW_CLICKED
//                    && (Arrays.asList(Utils.HomePackage).contains(EventPackage)
//                    || EventPackage.equals("com.android.systemui"))
//                    && !Arrays.asList(Utils.Permission_ui).contains(EventText))
//            {
//                Log.d(TAG, "chrome Initial");
//                MyEventText = "Chrome is close";
//                Utils.cancel = false;
//                NewsRelated(false);
//                init_application();
//            }
//        }
    }

    private void StopScreenshotDetecter(boolean screenshot, int EventType, String EventText, String EventPackage, String now_active, AccessibilityNodeInfo nodeInfo){
        String trigger_app = "";
        String ContentDescription = "";
        if(nodeInfo != null){
            ContentDescription = String.valueOf(nodeInfo.getContentDescription());
        }
        if(facebook)trigger_app = "Facebook";
        else if(youtube)trigger_app = "Youtube";
        else if(instagram)trigger_app = "Instagram";
        else if(news)trigger_app = "News App";
        else if(line)trigger_app = "LineToday";
        else if(chrome)trigger_app = "Chrome";

//        Log.d(TAG, "Big problem: " + instagram + " " + screenshot + " " + google + " " + Utils.cancel + " " + home);

        Log.d(TAG, "Trigger app: " + trigger_app);
        if(!trigger_app.equals("")) {
//            Resources resource = getResources();
//            String[] AppPackage = resource.getStringArray(labelingStudy.nctu.minuku.R.array.AppPackage);
            boolean home_condition = home;
//            Log.d(TAG, "Home condition: " + home_condition + " , home: " + home);
            boolean change_app = false; //false, 萬一screen shot == true, app flag都false
//            if(!google){
//                home_condition = ((EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
//                        && home));
//            }

            //screenshot or Utils.cancel
            if (screenshot && !google) {
                if (facebook) {
                    home_condition = ((EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
                            && home));
                    if(!now_active.equals("com.facebook.katana") && EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
                        && !now_active.equals("android") && !now_active.equals("com.android.systemui") && !now_active.equals("com.example.accessibility_detect")
                        && !EventPackage.equals("com.facebook.katana") && !EventPackage.equals("android") && EventPackage.equals(now_active)
                            && !EventPackage.equals("com.android.systemui") && !EventPackage.equals("com.example.accessibility_detect")){
                        change_app = true;
                    }

                    Log.d(TAG, "Now active : " + now_active);
//                    if(EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED && !home_condition && !now_active.equals("com.facebook.katana")){
//                        home_condition = Arrays.asList(AppPack).contains(now_active) || Arrays.asList(NewsPack).contains(now_active)
//                                || Arrays.asList(PTTPack).contains(now_active);
//                    }
//                            || (!now_active.equals("com.facebook.katana") && !now_active.equals("com.example.accessibility_detect")
//                            && !now_active.equals("android") && !now_active.equals("com.android.systemui")));
                }
                else if (chrome) {
                    home_condition = ((EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
                            && home));
                    if(!Arrays.asList(WebPack).contains(now_active) && EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
                            && !now_active.equals("android") && !now_active.equals("com.android.systemui") && !now_active.equals("com.example.accessibility_detect")
                            && !Arrays.asList(WebPack).contains(EventPackage) && !EventPackage.equals("android") && EventPackage.equals(now_active)
                            && !EventPackage.equals("com.android.systemui") && !EventPackage.equals("com.example.accessibility_detect")){
                        change_app = true;
                    }
                }
                else if (line) {
                    if(!now_active.equals("jp.naver.line.android") && EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
                            && !now_active.equals("android") && !now_active.equals("com.android.systemui") && !now_active.equals("com.example.accessibility_detect")
                            && !EventPackage.equals("jp.naver.line.android") && !EventPackage.equals("android") && EventPackage.equals(now_active)
                            && !EventPackage.equals("com.android.systemui") && !EventPackage.equals("com.example.accessibility_detect")
                    ){
                        change_app = true;
                    }
                    home_condition = (home || ((EventPackage.equals("jp.naver.line.android")
                            && (EventType == AccessibilityEvent.TYPE_VIEW_SELECTED && (EventText.equals("主頁") || EventText.equals("聊天") ||
                            EventText.equals("貼文串") || EventText.equals("錢包"))))));
//                    if(EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED && !home_condition && !now_active.equals("jp.naver.line.android")){
//                        home_condition = Arrays.asList(AppPack).contains(now_active) || Arrays.asList(NewsPack).contains(now_active)
//                                || Arrays.asList(PTTPack).contains(now_active);
//                    }
                }
                else if(instagram){
                    home_condition = ((EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
                            && home));
                    if(!now_active.equals("com.instagram.android") && EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
                            && !now_active.equals("android") && !now_active.equals("com.android.systemui") && !now_active.equals("com.example.accessibility_detect")
                            && !EventPackage.equals("com.instagram.android") && !EventPackage.equals("android") && EventPackage.equals(now_active)
                            && !EventPackage.equals("com.android.systemui") && !EventPackage.equals("com.example.accessibility_detect")
                    ){
                        change_app = true;
                    }
                }
                else if(news){
                    home_condition = ((EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
                            && EventType != AccessibilityEvent.TYPE_ANNOUNCEMENT
                            && home));
                    if(!Arrays.asList(NewsPack).contains(now_active) && EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
                            && !now_active.equals("android") && !now_active.equals("com.android.systemui") && !now_active.equals("com.example.accessibility_detect")
                            && !Arrays.asList(NewsPack).contains(EventPackage) && !EventPackage.equals("android") && EventPackage.equals(now_active)
                            && !EventPackage.equals("com.android.systemui") && !EventPackage.equals("com.example.accessibility_detect")){
                        change_app = true;
                    }
                }
                else if(youtube){
                    home_condition = ((EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
                            && home));
                    if(!now_active.equals("com.google.android.youtube") && EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
                            && !now_active.equals("android") && !now_active.equals("com.android.systemui") && !now_active.equals("com.example.accessibility_detect")
                            && !EventPackage.equals("com.google.android.youtube") && !EventPackage.equals("android") && EventPackage.equals(now_active)
                            && !EventPackage.equals("com.android.systemui") && !EventPackage.equals("com.example.accessibility_detect")){
                        change_app = true;
                    }
                }
                if(EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED && !home_condition
                        && EventPackage.equals("com.facebook.orca")){
                    home_condition = true;
                }
//                log_counter++;
//                if(log_counter == 10){
//                    CSVHelper.storeToCSV("AccessibilityDetect.csv", "In screen shot " + screenshot + " && google " + google + " , home condition is " + home + " change app is " + change_app);
//                    log_counter = 0;
//                }
                Log.d(TAG, "home condition: " + home_condition + " change app: " + change_app);

                boolean question_interrupt = pref.getBoolean("Question_interrupt",false);
                Log.d(TAG, "question_interrupt:" + question_interrupt);

                if (home_condition || change_app || question_interrupt) {

                    pref.edit().putBoolean("Question_interrupt", false).apply();

                    if(line){
                        Utils.first = true;
                    }
//                    if(home_condition)
//                        CSVHelper.storeToCSV("AccessibilityDetect.csv", trigger_app + " is close(In screen shot, home condition)");
//                    if(change_app)
//                        CSVHelper.storeToCSV("AccessibilityDetect.csv", trigger_app + " is close(In screen shot, change app)");
                    init_application();
                    if(trigger_app.equals("Facebook")){
                        state_array[1] = false;
//                        CSVHelper.storeToCSV("AccessibilityDetect.csv", "In stop screen shot detector, screen shot is true, facebook flag is " + facebook + " ( " + home_condition +"," + change_app + " )");
                        endSession();
                        ESMjump("Facebook");
                    }
                    else if(trigger_app.equals("Youtube")){
                        state_array[5] = false;
                        watch_video = false;
//                        CSVHelper.storeToCSV("AccessibilityDetect.csv", "In stop screen shot detector, screen shot is true, youtube flag is " + youtube + " ( " + home_condition +"," + change_app + " )");
                        endSession();
                        ESMjump("Youtube");
                    }
                    else if(trigger_app.equals("Instagram")){
                        state_array[4] = false;
//                        CSVHelper.storeToCSV("AccessibilityDetect.csv", "In stop screen shot detector, screen shot is true, instagram flag is " + instagram + " ( " + home_condition +"," + change_app + " )");
                        endSession();
                        ESMjump("Instagram");
                    }
                    else if(trigger_app.equals("News App")){
                        state_array[6] = false;
//                        CSVHelper.storeToCSV("AccessibilityDetect.csv", "In stop screen shot detector, screen shot is true, newsapp flag is " + news + " ( " + home_condition +"," + change_app + " )");
                        endSession();
                        ESMjump("News App");
                    }
                    else if(trigger_app.equals("LineToday")){
                        state_array[7] = false;
//                        CSVHelper.storeToCSV("AccessibilityDetect.csv", "In stop screen shot detector, screen shot is true, line today flag is " + line + " ( " + home_condition +"," + change_app + " )");
                        endSession();
                        ESMjump("LineToday");
                    }
                    else if(trigger_app.equals("Chrome")){
                        state_array[3] = false;
//                        CSVHelper.storeToCSV("AccessibilityDetect.csv", "In stop screen shot detector, screen shot is true, chrome flag is " + chrome + " ( " + home_condition +"," + change_app + " )");
                        endSession();
                        ESMjump("Chrome");
                    }

                    intent.putExtra(trigger_app, false);
                    pref.edit().putString("Trigger", "").apply();

                    notificationListenService.updateRecordingNotification(this, Accessibility_ID, trigger_app, true, intent);

//                    CSVHelper.storeToCSV("AccessibilityDetect.csv", "Send " + trigger_app + " screen shot update notification");

//                    mNotificationManager.cancel(Accessibility_ID);
                    stopService(new Intent(this, ScreenCapture.class));
                    MyEventText = trigger_app + " is close";
//                    lastid = pref.getLong("SessionID", 0);
//                    db.SessionDataRecordDao().updateSession(lastid, ScheduleAndSampleManager.getCurrentTimeString(), System.currentTimeMillis());
                    Log.d(TAG, trigger_app + " is close!!");
//                    NewsRelated(false);
                }

                // 拿掉維持一致性 1/4
//                if (trigger_app.equals("Youtube")) {
//                    Log.d(TAG, "Text:" + EventText + " Package: " + EventPackage);
//                    if ((EventType == AccessibilityEvent.TYPE_VIEW_CLICKED
//                            && EventPackage.equals("com.google.android.youtube")
//                            && (EventText.equals(" 前往頻道動作選單") || EventText.equals(" 動作選單")
//                            || EventText.equals("略過廣告") || EventText.equals("Skip ads") || EventText.equals("播放影片") || EventText.equals("結束全螢幕模式")
//                            || EventText.contains("最小化未提供字幕更多選項隱藏控制介面暫停影片上一部影片")))
//                            || (EventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
//                                    && EventPackage.equals("com.google.android.youtube")
//                                    && (ContentDescription.equals("影片播放器")))
//                            || (EventType == AccessibilityEvent.TYPE_VIEW_FOCUSED
//                            && EventPackage.equals("com.google.android.youtube")
//                            && EventText.contains("最小化未提供字幕更多選項隱藏控制介面暫停影片上一部影片"))) {
//                        watch_video = true;
//                        try {
//                            Log.d(TAG, "delay one second");
//                            Thread.sleep(2000);
//                        } catch (Exception e) {
//
//                        }
////                        init_application();
//
////                        CSVHelper.storeToCSV("AccessibilityDetect.csv", trigger_app + " is close(In screen shot, youtube case)");
//
////                        state_array[5] = false;
////                        CSVHelper.storeToCSV("AccessibilityDetect.csv", "In stop screen shot detector, screen shot is true, youtube flag is " + youtube + " ( " + home_condition +"," + change_app + " )");
//
//                        intent.putExtra(trigger_app, false);
//                        pref.edit().putString("Trigger", "").apply();
//
//                        notificationListenService.updateRecordingNotification(this, Accessibility_ID, trigger_app, true, intent);
////                        CSVHelper.storeToCSV("AccessibilityDetect.csv", "Send " + trigger_app + " screen shot update notification");
//
//                        //                        mNotificationManager.cancel(Accessibility_ID);
//
//                        stopService(new Intent(this, ScreenCapture.class));
////                        lastid = pref.getLong("SessionID", 0);
////                        db.SessionDataRecordDao().updateSession(lastid, ScheduleAndSampleManager.getCurrentTimeString(), System.currentTimeMillis());
////                        endSession();
//                        Log.d(TAG, trigger_app + " is close!!");
////                        MyEventText = trigger_app + " is close";
////                        NewsRelated(false);
//                    }
//                }
            }
            else if ((!screenshot || Utils.cancel) && (facebook || youtube || instagram || news || line || chrome)) {
                boolean cancel_condition = home;
                if (facebook) {
                    cancel_condition = (EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
                            && home);
                    if(!now_active.equals("com.facebook.katana") && EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
                            && !now_active.equals("android") && !now_active.equals("com.android.systemui") && !now_active.equals("com.example.accessibility_detect")
                            && !EventPackage.equals("com.facebook.katana") && !EventPackage.equals("android") && EventPackage.equals(now_active)
                            && !EventPackage.equals("com.android.systemui") && !EventPackage.equals("com.example.accessibility_detect")){
                        change_app = true;
                    }
//                    if(EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED && !home_condition && !now_active.equals("com.facebook.katana")){
//                        home_condition = Arrays.asList(AppPack).contains(now_active) || Arrays.asList(NewsPack).contains(now_active)
//                                || Arrays.asList(PTTPack).contains(now_active);
//                    }
//                            || (!now_active.equals("com.facebook.katana") && !now_active.equals("com.example.accessibility_detect")
//                            && !now_active.equals("android") && !now_active.equals("com.android.systemui")));
                }
                else if (chrome) {
                    cancel_condition = (EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
                            && home);
                    if(!Arrays.asList(WebPack).contains(now_active) && EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
                            && !now_active.equals("android") && !now_active.equals("com.android.systemui") && !now_active.equals("com.example.accessibility_detect")
                            && !Arrays.asList(WebPack).contains(EventPackage) && !EventPackage.equals("android") && EventPackage.equals(now_active)
                            && !EventPackage.equals("com.android.systemui") && !EventPackage.equals("com.example.accessibility_detect")){
                        change_app = true;
                        Log.d(TAG, "It seems as change app");
                    }
                }
                else if(instagram){
                    cancel_condition = (EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
                            && home);
                    if(!now_active.equals("com.instagram.android") && EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
                            && !now_active.equals("android") && !now_active.equals("com.android.systemui") && !now_active.equals("com.example.accessibility_detect")
                            && !EventPackage.equals("com.instagram.android") && !EventPackage.equals("android") && EventPackage.equals(now_active)
                            && !EventPackage.equals("com.android.systemui") && !EventPackage.equals("com.example.accessibility_detect")){
                        change_app = true;
                    }
                }
                else if(news){
                    cancel_condition = (EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
                            && home);
                    if(!Arrays.asList(NewsPack).contains(now_active) && EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
                            && !now_active.equals("android") && !now_active.equals("com.android.systemui") && !now_active.equals("com.example.accessibility_detect")
                            && !Arrays.asList(NewsPack).contains(EventPackage) && !EventPackage.equals("android") && EventPackage.equals(now_active)
                            && !EventPackage.equals("com.android.systemui") && !EventPackage.equals("com.example.accessibility_detect")){
                        change_app = true;
                    }
                }
                else if(youtube){
                    cancel_condition = (EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
                            && home);
                    if(!now_active.equals("com.google.android.youtube") && EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
                            && !now_active.equals("android") && !now_active.equals("com.android.systemui") && !now_active.equals("com.example.accessibility_detect")
                            && !EventPackage.equals("com.google.android.youtube") && !EventPackage.equals("android") && EventPackage.equals(now_active)
                            && !EventPackage.equals("com.android.systemui") && !EventPackage.equals("com.example.accessibility_detect")){
                        change_app = true;
                    }
                }
                else if (line) {
                    if(!now_active.equals("jp.naver.line.android") && EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
                            && !now_active.equals("android") && !now_active.equals("com.android.systemui") && !now_active.equals("com.example.accessibility_detect")
                            && !EventPackage.equals("jp.naver.line.android") && !EventPackage.equals("android") && EventPackage.equals(now_active)
                            && !EventPackage.equals("com.android.systemui") && !EventPackage.equals("com.example.accessibility_detect")
                    ){
                        change_app = true;
                    }
                    cancel_condition = (home || ((EventPackage.equals("jp.naver.line.android")
                            && (EventType == AccessibilityEvent.TYPE_VIEW_SELECTED && (EventText.equals("主頁") || EventText.equals("聊天") ||
                            EventText.equals("貼文串") || EventText.equals("錢包"))))));
                }
                if(EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED && !cancel_condition
                        && EventPackage.equals("com.facebook.orca")){
                    cancel_condition = true;
                }
                Log.d(TAG, "Cancel condition: " + cancel_condition);
                boolean question_interrupt = pref.getBoolean("Question_interrupt",false);
                Log.d(TAG, "Questionnaire interrupt: " + question_interrupt);
                if (cancel_condition || change_app || question_interrupt) {
                    Log.d(TAG, "cancel by home");
                    pref.edit().putBoolean("Question_interrupt", false).apply();
//                    CSVHelper.storeToCSV("AccessibilityDetect.csv", trigger_app + " is close(In cancel)");

                    if(line) Utils.first = true;
                    init_application();
                    if(trigger_app.equals("Facebook")){
                        state_array[1] = false;
//                        CSVHelper.storeToCSV("AccessibilityDetect.csv", "In stop screen shot detector, screen shot is " + screenshot + " cancel is " + Utils.cancel + ", facebook flag is " + facebook + " ( " + home_condition +"," + change_app + " )");
                        endSession();
                        ESMjump("Facebook");
                    }
                    else if(trigger_app.equals("Youtube")){
                        watch_video = false;
                        state_array[5] = false;
//                        CSVHelper.storeToCSV("AccessibilityDetect.csv", "In stop screen shot detector, screen shot is " + screenshot + " cancel is " + Utils.cancel + ", youtube flag is " + youtube + " ( " + home_condition +"," + change_app + " )");
                        endSession();
                        ESMjump("Youtube");
                    }
                    else if(trigger_app.equals("Instagram")){
                        state_array[4] = false;
//                        CSVHelper.storeToCSV("AccessibilityDetect.csv", "In stop screen shot detector, screen shot is " + screenshot + " cancel is " + Utils.cancel + ", instagram flag is " + instagram + " ( " + home_condition +"," + change_app + " )");
                        endSession();
                        ESMjump("Instagram");
                    }
                    else if(trigger_app.equals("News App")){
                        state_array[6] = false;
//                        CSVHelper.storeToCSV("AccessibilityDetect.csv", "In stop screen shot detector, screen shot is " + screenshot + " cancel is " + Utils.cancel + ", newsapp flag is " + news + " ( " + home_condition +"," + change_app + " )");
                        endSession();
                        ESMjump("News App");
                    }
                    else if(trigger_app.equals("LineToday")){
                        state_array[7] = false;
//                        CSVHelper.storeToCSV("AccessibilityDetect.csv", "In stop screen shot detector, screen shot is " + screenshot + " cancel is " + Utils.cancel + ", line today flag is " + line + " ( " + home_condition +"," + change_app + " )");
                        endSession();
                        ESMjump("LineToday");
                    }
                    else if(trigger_app.equals("Chrome")){
                        state_array[3] = false;
//                        CSVHelper.storeToCSV("AccessibilityDetect.csv", "In stop screen shot detector, screen shot is " + screenshot + " cancel is " + Utils.cancel + ", chrome flag is " + chrome + " ( " + home_condition +"," + change_app + " )");
                        endSession();
                        ESMjump("Chrome");
                    }
//                    lastid = pref.getLong("SessionID", 0);
//                    Log.d(TAG, "Last ID: " + lastid);
//                    db.SessionDataRecordDao().updateSession(lastid, ScheduleAndSampleManager.getCurrentTimeString(), System.currentTimeMillis());
                    Log.d(TAG, trigger_app + " Initial");
                    MyEventText = trigger_app + " is close";
                    Utils.cancel = false;
//                    CSVHelper.storeToCSV("AccessibilityDetect.csv", "Set cancel flag to " + Utils.cancel);

//                    mNotificationManager.cancel(Accessibility_ID);
                    notificationListenService.updateRecordingNotification(this, Accessibility_ID, trigger_app, true, intent);

//                    CSVHelper.storeToCSV("AccessibilityDetect.csv", "Send " + trigger_app + " screen shot update notification");
//                    CSVHelper.storeToCSV("AccessibilityDetect.csv", "-------------------------");

//                    NewsRelated(false);
                }
            }
        }
        Log.d(TAG, "screenshot now: " + screenshot + " !google: " + !google + " home: " + home);
        if(Utils.cancel && ((EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED && home && !google))){
//            CSVHelper.storeToCSV("AccessibilityDetect.csv", trigger_app + " is close(In cancel, home all)");
            Log.d(TAG, "Additional: " + trigger_app + " Initial");
            MyEventText = trigger_app + " is close";
            Utils.cancel = false;
            watch_video = false;
            init_application();
//            CSVHelper.storeToCSV("AccessibilityDetect.csv", "In cancel all, all flag is set to false");

            notificationListenService.updateRecordingNotification(this, Accessibility_ID, trigger_app, true, intent);
//            CSVHelper.storeToCSV("AccessibilityDetect.csv", "Send " + trigger_app + " screen shot update notification");
//            CSVHelper.storeToCSV("AccessibilityDetect.csv", "-------------------------");
//            ESMjump(trigger_app);
            endSession();
        }
        if(screenshot && !google){
            if(home && EventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED && EventType != AccessibilityEvent.TYPE_ANNOUNCEMENT){
//                CSVHelper.storeToCSV("AccessibilityDetect.csv", "screen shot now and stop and initial all");
                init_application();
                watch_video = false;

                intent.putExtra(trigger_app, false);
                pref.edit().putString("Trigger", "").apply();

                notificationListenService.updateRecordingNotification(this, Accessibility_ID, trigger_app, true, intent);

//                CSVHelper.storeToCSV("AccessibilityDetect.csv", "Send " + trigger_app + " screen shot update notification");

                stopService(new Intent(this, ScreenCapture.class));
                MyEventText = trigger_app + " is close";
//                ESMjump(trigger_app);
                endSession();
            }
        }
    }

    private void ESMjump(String TriggerApp){
//        Long lastESMtime = pref.getLong("LastESMTime", 0);
        Long now = System.currentTimeMillis();
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(now);

        UserDataRecord userDataRecord = db.userDataRecordDao().getLastRecord();
        int MinHour = 0;
        int MaxHour = 0;
        long LastEsmTime = 0L;
        long _id = 0;
        if(userDataRecord != null){
            _id = userDataRecord.get_id();
            MinHour = Integer.parseInt(userDataRecord.getquestionnaire_startTime());
            MaxHour = Integer.parseInt(userDataRecord.getquestionnaire_endTime());
            LastEsmTime = userDataRecord.getLastEsmTime();
        }

        Log.d(TAG, "min and max = " + MinHour + " " + MaxHour);

//        int MinHour = pref.getInt("MinHour", 9);
//        int MaxHour = pref.getInt("MaxHour", 22);

        Random_session_counter++;
        Log.d(TAG, "Random session number = " + Random_session_num);
        Log.d(TAG, "Random session counter = " + Random_session_counter);
        if(Random_session_counter >= Random_session_num) {
            CSVHelper.storeToCSV("ESM_random_number.csv", "counter now / random number: " + Random_session_counter + " / " + Random_session_num  + " (" + TriggerApp + ")");
            Random_session_counter = 0;
            if(MinHour < MaxHour){
                if(c.get(Calendar.HOUR_OF_DAY) >= MinHour && c.get(Calendar.HOUR_OF_DAY) < MaxHour) {
                    if (now - LastEsmTime > 60* 60 * 1000) {
                        CSVHelper.storeToCSV("ESM_random_number.csv", "ESM is delivered");
                        db.userDataRecordDao().updateLastEsmTime(_id, System.currentTimeMillis());
                        Intent intent = new Intent(this, AlarmReceiver.class);
                        intent.setAction(ESM_ALARM);
                        sendBroadcast(intent);
//                    pref.edit().putLong("LastESMTime", System.currentTimeMillis()).apply();
                        Random_session_num = (int) (Math.random() * 3 + 1);
                    }
                    else{
                        CSVHelper.storeToCSV("ESM_random_number.csv", "ESM is not delivered, not exceed one hour");
                    }
                }
                else{
                    CSVHelper.storeToCSV("ESM_random_number.csv", "ESM is not delivered, now is out of setting time");
                }
            }
            else if(MaxHour < MinHour){ // min = 12, max = 3
                if(c.get(Calendar.HOUR_OF_DAY) >= MinHour || c.get(Calendar.HOUR_OF_DAY) < MaxHour){
                    if (now - LastEsmTime > 60* 60 * 1000) {
                        CSVHelper.storeToCSV("ESM_random_number.csv", "ESM is delivered");
                        db.userDataRecordDao().updateLastEsmTime(_id, System.currentTimeMillis());
                        Intent intent = new Intent(this, AlarmReceiver.class);
                        intent.setAction(ESM_ALARM);
                        sendBroadcast(intent);
//                    pref.edit().putLong("LastESMTime", System.currentTimeMillis()).apply();
                        Random_session_num = (int) (Math.random() * 3 + 1);
                    }
                    else{
                        CSVHelper.storeToCSV("ESM_random_number.csv", "ESM is not delivered, not exceed one hour");
                    }
                }
                else{
                    CSVHelper.storeToCSV("ESM_random_number.csv", "ESM is not delivered, now is out of setting time");
                }
            }
//            if(c.get(Calendar.HOUR_OF_DAY) >= MinHour && c.get(Calendar.HOUR_OF_DAY) < MaxHour) {
//                if (now - LastEsmTime > 60* 60 * 1000) {
//                    CSVHelper.storeToCSV("ESM_random_number.csv", "ESM is delivered");
//                    db.userDataRecordDao().updateLastEsmTime(_id, System.currentTimeMillis());
//                    Intent intent = new Intent(this, AlarmReceiver.class);
//                    intent.setAction(ESM_ALARM);
//                    sendBroadcast(intent);
////                    pref.edit().putLong("LastESMTime", System.currentTimeMillis()).apply();
//                    Random_session_num = (int) (Math.random() * 3 + 1);
//                }
//                else{
//                    CSVHelper.storeToCSV("ESM_random_number.csv", "ESM is not delivered, not exceed one hour");
//                }
//            }
//            else{
//                CSVHelper.storeToCSV("ESM_random_number.csv", "ESM is not delivered, now is out of setting time");
//            }
            CSVHelper.storeToCSV("ESM_random_number.csv", "Reset random number to: " + Random_session_num);
        }
        else{
            CSVHelper.storeToCSV("ESM_random_number.csv", "counter now / random number: " + Random_session_counter + " / " + Random_session_num + " (" + TriggerApp + ")");
        }
    }
    private void clean_prevApp(boolean[] now_state, int index){
        for(int i = 0; i < now_state.length; i++){
//            CSVHelper.storeToCSV("AccessibilityDetect.csv", i + " " + now_state[i]);
            if(now_state[i]) {
                switch (i) {
                    case 0: //googleNews
//                        init_application();
                        google = false;
                        Utils.google_first = true;
                        googlebox_count = 0;
                        googlebox_home = 0;
                        reading = false;
                        if(Utils.cancel){
                            Utils.cancel = false;
//                            CSVHelper.storeToCSV("AccessibilityDetect.csv", "In google cleaned, set cancel flag to " + Utils.cancel);
                        }
                        else{
                            intent.putExtra("googleNews", true);
                            pref.edit().putString("Trigger", "").apply();

                            stopService(new Intent(this, ScreenCapture.class));
                        }
//                        CSVHelper.storeToCSV("AccessibilityDetect.csv", "In google cleaned, set google flag to " + google);

                        MyEventText = "googleNews" + " is close";
//                        CSVHelper.storeToCSV("AccessibilityDetect.csv", "googleNews is cleaned");
                        notificationListenService.updateRecordingNotification(this, Accessibility_ID, "Facebook", true, intent);
//                        CSVHelper.storeToCSV("AccessibilityDetect.csv", "Send google screen shot update notification");
                        endSession();
                        ESMjump("googleNews");
//                        lastid = pref.getLong("SessionID", 0);
//                        db.SessionDataRecordDao().updateSession(lastid, ScheduleAndSampleManager.getCurrentTimeString(), System.currentTimeMillis());
                        Log.d(TAG, "googleNews" + " is close!!");
//                        NewsRelated(false);
                        break;
                    case 1: //Facebook
//                        init_application();
                        facebook = false;
                        if(Utils.cancel){
                            Utils.cancel = false;
//                            CSVHelper.storeToCSV("AccessibilityDetect.csv", "In facebook cleaned, set cancel flag to " + Utils.cancel);
                        }
                        else{
                            intent.putExtra("Facebook", false);
                            pref.edit().putString("Trigger", "").apply();

                            stopService(new Intent(this, ScreenCapture.class));
                        }
//                        CSVHelper.storeToCSV("AccessibilityDetect.csv", "In facebook cleaned, set facebook flag to " + facebook);

                        MyEventText = "Facebook" + " is close";
//                        CSVHelper.storeToCSV("AccessibilityDetect.csv", "Facebook is cleaned");
                        notificationListenService.updateRecordingNotification(this, Accessibility_ID, "Facebook", true, intent);
//                        CSVHelper.storeToCSV("AccessibilityDetect.csv", "Send Facebook screen shot update notification");

//                        lastid = pref.getLong("SessionID", 0);
//                        db.SessionDataRecordDao().updateSession(lastid, ScheduleAndSampleManager.getCurrentTimeString(), System.currentTimeMillis());
                        Log.d(TAG, "Facebook" + " is close!!");
                        endSession();
                        ESMjump("Facebook");
//                        NewsRelated(false);
                        break;
                    case 2: // LineMes
                        Utils.LineMes_first = true;
                        line_mes = false;
//                        init_application();
                        MyEventText = "LineMes is close";
                        NewsRelated(false);
                        lastid = pref.getLong("SessionID", 0);
//                        CSVHelper.storeToCSV("AccessibilityDetect.csv", "Line Mes is cleaned");
                        db.SessionDataRecordDao().updateSession(lastid, ScheduleAndSampleManager.getCurrentTimeString(), System.currentTimeMillis());
                        CSVHelper.storeToCSV("MyDataRecord.csv", "LineMes is close!!");
                        ESMjump("LineMes");
//                        CSVHelper.storeToCSV("AccessibilityDetect.csv", "In line mes cleaned, set line mes flag to false");
                    case 3: //web
                        init_application();
                        chrome = false;
                        if(Utils.cancel){
                            Utils.cancel = false;
//                            CSVHelper.storeToCSV("AccessibilityDetect.csv", "In chrome cleaned, set cancel flag to " + Utils.cancel);
                        }
                        else{
                            intent.putExtra("Chrome", false);
                            pref.edit().putString("Trigger", "").apply();

                            stopService(new Intent(this, ScreenCapture.class));
                        }
//                        CSVHelper.storeToCSV("AccessibilityDetect.csv", "In chrome cleaned, set chrome flag to " + chrome);

                        MyEventText = "Chrome" + " is close";
//                        CSVHelper.storeToCSV("AccessibilityDetect.csv", "Chrome is cleaned");
                        notificationListenService.updateRecordingNotification(this, Accessibility_ID, "Chrome", true, intent);
//                        CSVHelper.storeToCSV("AccessibilityDetect.csv", "Send chrome screen shot update notification");
                        endSession();
                        ESMjump("Chrome");
//                        lastid = pref.getLong("SessionID", 0);
//                        db.SessionDataRecordDao().updateSession(lastid, ScheduleAndSampleManager.getCurrentTimeString(), System.currentTimeMillis());
                        Log.d(TAG, "Chrome" + " is close!!");
//                        NewsRelated(false);
                    case 4: // instagram
//                        init_application();
                        instagram = false;
                        if(Utils.cancel){
                            Utils.cancel = false;
//                            CSVHelper.storeToCSV("AccessibilityDetect.csv", "In instagram cleaned, set cancel flag to " + Utils.cancel);
                        }
                        else{
                            intent.putExtra("Instagram", false);
                            pref.edit().putString("Trigger", "").apply();

                            stopService(new Intent(this, ScreenCapture.class));
                        }
//                        CSVHelper.storeToCSV("AccessibilityDetect.csv", "In instagram cleaned, set instagram flag to " + instagram);

                        MyEventText = "Instagram" + " is close";
//                        CSVHelper.storeToCSV("AccessibilityDetect.csv", "Instagram is cleaned");
                        notificationListenService.updateRecordingNotification(this, Accessibility_ID, "Instagram", true, intent);
//                        CSVHelper.storeToCSV("AccessibilityDetect.csv", "Send Instagram screen shot update notification");
                        endSession();
                        ESMjump("Instagram");
//                        lastid = pref.getLong("SessionID", 0);
//                        db.SessionDataRecordDao().updateSession(lastid, ScheduleAndSampleManager.getCurrentTimeString(), System.currentTimeMillis());
                        Log.d(TAG, "Instagram" + " is close!!");
//                        NewsRelated(false);
                    case 5: // youtube
//                        init_application();
                        youtube = false;
                        watch_video = false;
                        if(Utils.cancel){
                            Utils.cancel = false;
//                            CSVHelper.storeToCSV("AccessibilityDetect.csv", "In youtube cleaned, set cancel flag to " + Utils.cancel);
                        }
                        else{
                            intent.putExtra("Youtube", false);
                            pref.edit().putString("Trigger", "").apply();

                            stopService(new Intent(this, ScreenCapture.class));
                        }
//                        CSVHelper.storeToCSV("AccessibilityDetect.csv", "In youtube cleaned, set youtube flag to " + youtube);

                        MyEventText = "Youtube" + " is close";
//                        CSVHelper.storeToCSV("AccessibilityDetect.csv", "Youtube is cleaned");
                        notificationListenService.updateRecordingNotification(this, Accessibility_ID, "Youtube", true, intent);
//                        CSVHelper.storeToCSV("AccessibilityDetect.csv", "Send youtube screen shot update notification");
                        endSession();
                        ESMjump("Youtube");
//                        lastid = pref.getLong("SessionID", 0);
//                        db.SessionDataRecordDao().updateSession(lastid, ScheduleAndSampleManager.getCurrentTimeString(), System.currentTimeMillis());
                        Log.d(TAG, "Youtube" + " is close!!");
//                        NewsRelated(false);
                    case 6: //NewsApp
                        news = false;
//                        init_application();
                        if(Utils.cancel){
                            Utils.cancel = false;
//                            CSVHelper.storeToCSV("AccessibilityDetect.csv", "In news app cleaned, set cancel flag to " + Utils.cancel);
                        }
                        else{
                            intent.putExtra("News App", false);
                            pref.edit().putString("Trigger", "").apply();

                            stopService(new Intent(getApplicationContext(), ScreenCapture.class));
                        }
//                        CSVHelper.storeToCSV("AccessibilityDetect.csv", "In news app cleaned, set news app flag to " + news);
                        MyEventText = "News App" + " is close";
//                        CSVHelper.storeToCSV("AccessibilityDetect.csv", "News App is cleaned");
                        notificationListenService.updateRecordingNotification(this, Accessibility_ID, "News App", true, intent);
//                        CSVHelper.storeToCSV("AccessibilityDetect.csv", "Send news app screen shot update notification");
                        endSession();
                        ESMjump("News App");
                        //                        lastid = pref.getLong("SessionID", 0);
//                        db.SessionDataRecordDao().updateSession(lastid, ScheduleAndSampleManager.getCurrentTimeString(), System.currentTimeMillis());
                        Log.d(TAG, "News App" + " is close!!");
//                        NewsRelated(false);
                    case 7: //Line Today
                        Utils.first = true;
                        line  = false;
//                        init_application();
                        if(Utils.cancel){
                            Utils.cancel = false;
//                            CSVHelper.storeToCSV("AccessibilityDetect.csv", "In line today cleaned, set cancel flag to " + Utils.cancel);
                        }
                        else{
                            intent.putExtra("LineToday", false);
                            pref.edit().putString("Trigger", "").apply();

                            stopService(new Intent(getApplicationContext(), ScreenCapture.class));
                        }
//                        CSVHelper.storeToCSV("AccessibilityDetect.csv", "In line today cleaned, set line today flag to " + line);
//                        CSVHelper.storeToCSV("MyDataRecord.csv", "LineToday is close!!");

                        MyEventText = "LineToday" + " is close";
//                        CSVHelper.storeToCSV("AccessibilityDetect.csv", "LineToday is cleaned");
                        notificationListenService.updateRecordingNotification(this, Accessibility_ID, "LineToday", true, intent);
//                        CSVHelper.storeToCSV("AccessibilityDetect.csv", "Send line today screen shot update notification");
                        endSession();
                        ESMjump("LineToday");
                        //                        lastid = pref.getLong("SessionID", 0);
//                        db.SessionDataRecordDao().updateSession(lastid, ScheduleAndSampleManager.getCurrentTimeString(), System.currentTimeMillis());
                        Log.d(TAG, "LineToday" + " is close!!");
//                        NewsRelated(false);
                    case 8: // PTT
                        ptt = false;
//                        init_application();
                        lastid = pref.getLong("SessionID", 0);
                        db.SessionDataRecordDao().updateSession(lastid, ScheduleAndSampleManager.getCurrentTimeString(), System.currentTimeMillis());
//                        CSVHelper.storeToCSV("AccessibilityDetect.csv", "PTT is cleaned");
                        Log.d(TAG, "Ptt is close!!");
                        MyEventText = "Ptt is close";
                        NewsRelated(false);
                        ESMjump("PTT");
//                        CSVHelper.storeToCSV("AccessibilityDetect.csv", "In ptt cleaned, set ptt flag to false");
                    case 9:// messenger
//                        init_application();
                        messenger = false;
                        MyEventText = "Messenger is close";
//                        CSVHelper.storeToCSV("AccessibilityDetect.csv", "Messenger is cleaned");
                        lastid = pref.getLong("SessionID", 0);
                        db.SessionDataRecordDao().updateSession(lastid, ScheduleAndSampleManager.getCurrentTimeString(), System.currentTimeMillis());
                        NewsRelated(false);
                        Log.d(TAG, "Messenger is close!!");
                        long now = System.currentTimeMillis();
                        if(now - messenger_duration >= 5 *1000){
                            ESMjump("Messenger");
                        }
//                        CSVHelper.storeToCSV("AccessibilityDetect.csv", "In messenger cleaned, set messenger flag to false");
                }
                now_state[i] = false;
            }
//            CSVHelper.storeToCSV("AccessibilityDetect.csv", i + " " + now_state[i]);
        }
    }
    private void SaveLineLastSelected(int EventType, String EventText, String EventPackage, String now_active)
    {
//        pref = getSharedPreferences("test", MODE_PRIVATE);
        if (EventPackage.equals("jp.naver.line.android")
                && (EventType == AccessibilityEvent.TYPE_VIEW_SELECTED && (EventText.equals("主頁") ||
                EventText.equals("貼文串") || EventText.equals("錢包") || EventText.equals("TODAY")
                || EventText.equals("聊天")))) {
            Log.d(TAG, "Last is not Today");
            if(!EventText.equals("TODAY") && !EventText.equals("聊天")) {
                Utils.first = true;
                Utils.LineMes_first = true;
            }
            pref.edit()
                    .putString("Line", EventText)
                    .apply();
        }
//        if (EventPackage.equals("jp.naver.line.android")
//                && (EventType == AccessibilityEvent.TYPE_VIEW_SELECTED && (EventText.equals("TODAY")
//                || EventText.equals("聊天")))) {
//            Log.d(TAG, "Last is Today");
//            pref.edit()
//                    .putString("Line", EventText)
//                    .apply();
//        }
    }
    public boolean Dialog_pop(long last, long now){
        if(now - last < agree_interval*60*60*1000){
            return false;
        }
        else{
            return true;
        }
    }
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        CSVHelper.storeToCSV("TestService.csv", "Accessibility service connected");
        streamManager = MinukuStreamManager.getInstance();
//        Fabric.with(this, new Crashlytics());
        Toast.makeText(getApplicationContext(),"已連接",Toast.LENGTH_SHORT).show();

        pref = getSharedPreferences("test", MODE_PRIVATE);
        SharedPreferences pref = getSharedPreferences("test", MODE_PRIVATE);
        pref.edit()
                .putBoolean("LineToday", false)
                .apply();
        Resources res = getResources();
        NewsPack = res.getStringArray(labelingStudy.nctu.minuku.R.array.NewsPack);
        NewsName = res.getStringArray(labelingStudy.nctu.minuku.R.array.NewsName);
        HomePackage = res.getStringArray(labelingStudy.nctu.minuku.R.array.HomePackage);
        AppPack = res.getStringArray(labelingStudy.nctu.minuku.R.array.AppPackage);
        WebPack = res.getStringArray(labelingStudy.nctu.minuku.R.array.WebPackage);
        WebEvent = res.getStringArray(labelingStudy.nctu.minuku.R.array.WebText);
        PttPack = res.getStringArray(labelingStudy.nctu.minuku.R.array.PttPackage);


//        List<PackageInfo> packages = getPackageManager().getInstalledPackages(0);
//        for (PackageInfo packageInfo : packages) {
//            Log.d(TAG, "ALL Package :" + packageInfo.packageName);
//            if(isSystemPackage(packageInfo)){
//                Log.d(TAG, "System package");
//                SystemPackage.add(packageInfo.packageName);
//            }
//        }
//        String[] eventText = res.getStringArray(labelingStudy.nctu.minuku.R.array.NewsName);
//        createNotificationChannel(this);
        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        IntentFilter filter = new IntentFilter();
        filter.addAction(START_RECORDING);
        filter.addAction(STOP_RECORDING);
        LocalBroadcastManager.getInstance(this).registerReceiver(Recording, filter);

        List<String> UrlList = new ArrayList<>();
        Set<String> UrlSet = new HashSet<String>(UrlList);
        pref = getSharedPreferences("URL", MODE_PRIVATE);
        pref.edit()
                .putStringSet("UrlSet", UrlSet)
                .apply();

        mPowerManager = (PowerManager)getSystemService(POWER_SERVICE);
        db = appDatabase.getDatabase(getApplicationContext());
        userRecord = db.userDataRecordDao().getLastRecord();
        Log.d(TAG, "onServiceConnected");

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();

//        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED |
//                            AccessibilityEvent.TYPE_WINDOWS_CHANGED |
//                            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED |
//                            AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED |
//                            AccessibilityEvent.TYPE_VIEW_CLICKED |
//                            AccessibilityEvent.TYPE_VIEW_SCROLLED |
//                            AccessibilityEvent.TYPE_VIEW_SELECTED |
//                            AccessibilityEvent.TYPE_VIEW_FOCUSED|
//                            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED |
//                            AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED |
//                            AccessibilityEvent.TYPE_ANNOUNCEMENT |
//                            AccessibilityEvent.TYPE_VIEW_LONG_CLICKED;
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.flags = AccessibilityServiceInfo.DEFAULT;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        /*mScheduledExecutorService = Executors.newScheduledThreadPool(Constants.MAIN_THREAD_SIZE);
        mScheduledFuture = mScheduledExecutorService.scheduleAtFixedRate(
                uploadAppUsageRunnable,
                Constants.STREAM_UPDATE_DELAY,
                Constants.STREAM_UPDATE_FREQUENCY,
                TimeUnit.SECONDS);*/
        setServiceInfo(info);
    }

    // NLP api
    private static final int LOADER_ACCESS_TOKEN = 1;
    AccessTokenLoader mAccessTokenLoader;
    private GoogleCredential mCredential;
    private Thread mThread;
    private final BlockingQueue<CloudNaturalLanguageRequest<? extends GenericJson>> mRequests
            = new ArrayBlockingQueue<>(3);
//    private LoaderManager.LoaderCallbacks<List<String>>
//            mLoaderCallbacks = new LoaderManager.LoaderCallbacks<String>() {
//        @Override
//        public Loader<String> onCreateLoader(int id, Bundle args) {
//            return new AccessTokenLoader(MyAccessibilityService.this);
//        }
//
//        @Override
//        public void onLoadFinished(Loader<String> loader, String token) {
//            setAccessToken(token);
//        }
//
//        @Override
//        public void onLoaderReset(Loader<String> loader) {
//        }
//    };
    Loader.OnLoadCompleteListener<String> mLoaderListener = new Loader.OnLoadCompleteListener<String>() {
        @Override
        public void onLoadComplete(Loader<String> loader, String token) {
            Log.d(TAG, "Load complete: " + token);
            setAccessToken(token);
        }
    };
    private CloudNaturalLanguage mApi = new CloudNaturalLanguage.Builder(
            new NetHttpTransport(),
            JacksonFactory.getDefaultInstance(),
            new HttpRequestInitializer() {
                @Override
                public void initialize(HttpRequest request) throws IOException {
                    mCredential.initialize(request);
                }
            }).build();
    public void analyzeSentiment(String text) {
        try {
            mRequests.add(mApi
                    .documents()
                    .analyzeSentiment(new AnalyzeSentimentRequest()
                            .setDocument(new Document()
                                    .setContent(text)
                                    .setType("PLAIN_TEXT"))));
        } catch (IOException e) {
            Log.e(TAG, "Failed to create analyze request.", e);
        }
    }
    public void analyzeEntities(String text) {
        try {
            // Create a new entities API call request and add it to the task queue
            mRequests.add(mApi
                    .documents()
                    .analyzeEntities(new AnalyzeEntitiesRequest()
                            .setDocument(new Document()
                                    .setContent(text)
                                    .setType("PLAIN_TEXT"))));
        } catch (IOException e) {
            Log.e(TAG, "Failed to create analyze request.", e);
        }
    }
    public void analyzeSyntax(String text) {
        try {
            mRequests.add(mApi
                    .documents()
                    .annotateText(new AnnotateTextRequest()
                            .setDocument(new Document()
                                    .setContent(text)
                                    .setType("PLAIN_TEXT"))
                            .setFeatures(new Features()
                                    .setExtractSyntax(true))));
        } catch (IOException e) {
            Log.e(TAG, "Failed to create analyze request.", e);
        }
    }

    private void prepareApi() {
        // Initiate token refresh
        Log.d(TAG, "Prepare api");
        mAccessTokenLoader = new AccessTokenLoader(this);
        mAccessTokenLoader.registerListener(LOADER_ACCESS_TOKEN, mLoaderListener);
        mAccessTokenLoader.startLoading();
    }
    public void setAccessToken(String token) {
        mCredential = new GoogleCredential()
                .setAccessToken(token)
                .createScoped(CloudNaturalLanguageScopes.all());
        startWorkerThread();
    }
    private void startWorkerThread() {
        if (mThread != null) {
            return;
        }
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (mThread == null) {
                        break;
                    }
                    try {
                        // API calls are executed here in this worker thread
                        deliverResponse(mRequests.take().execute());
                    } catch (InterruptedException e) {
                        Log.e(TAG, "Interrupted.", e);
                        break;
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to execute a request.", e);
                    }
                }
            }
        });
        mThread.start();
    }

    private void deliverResponse(final GenericJson response) {
        Log.d(TAG, "Generic Response --> " + response);
        if (response instanceof AnalyzeSentimentResponse) {
            Sentiment sentiment = ((AnalyzeSentimentResponse) response).getDocumentSentiment();
            CSVHelper.storeToCSV("Semantic.csv", UserInput + " (" + sentiment.getMagnitude() + " " + sentiment.getScore() + ")");
        }
        else if (response instanceof AnnotateTextResponse) {
            final List<Token> tokens = ((AnnotateTextResponse) response).getTokens();
            final int size = tokens.size();
//            final TokenInfo[] array = new TokenInfo[size];
            for (int i = 0; i < size; i++) {
                Log.d(TAG, "Text: " + tokens.get(i).getText().getContent());
//                array[i] = new TokenInfo(tokens.get(i));
            }
        }
        else if (response instanceof AnalyzeEntitiesResponse) {
            final List<Entity> entities = ((AnalyzeEntitiesResponse) response).getEntities();
            final int size = entities.size();
            for (int i = 0; i < size; i++) {
                Log.d(TAG, "Salience: " + entities.get(i).getSalience());
//                array[i] = new EntityInfo(entities.get(i));
            }
        }
//        Toast.makeText(MyAccessibilityService.this, "Response Recieved from Cloud NLP API", Toast.LENGTH_SHORT).show();
    }
    //NLP api

    public void runPhoneStatusMainThread(boolean start){

        labelingStudy.nctu.minuku.logger.Log.d(TAG, "runSensorMainThread") ;

        if(start) {
            Log.d(TAG, "Start Thread");
            mMainThread.post(runnable);
        }
        else{
            Log.d(TAG, "Stop Thread");
            mMainThread.removeCallbacks(runnable);
        }
    }

    public MyAccessibilityService() {
        super();
        try {
            this.accessibilityStreamGenerator = (AccessibilityStreamGenerator) MinukuStreamManager.getInstance().getStreamGeneratorFor(MyDataRecord.class);
            this.apptimesStreamGenerator = (AppTimesStreamGenerator) MinukuStreamManager.getInstance().getStreamGeneratorFor(AppTimesDataRecord.class);
            this.sensorStreamGenerator = (SensorStreamGenerator) MinukuStreamManager.getInstance().getStreamGeneratorFor(SensorDataRecord.class);

            //apptimesStreamGenerator.setLatestInAppAction(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
            //apptimesStreamGenerator.updateStream();
            Log.d(TAG, "Initial MyAccessibility Service");
        } catch (StreamNotFoundException e) {
            Log.d(TAG, "Initial MyAccessibility Service Failed");
        }
    }

    private String getEventText(AccessibilityEvent event) {
        StringBuilder sb = new StringBuilder();
        for (CharSequence s : event.getText()) {
            sb.append(s);
        }
        return sb.toString();
    }

    private String printForegroundTask() {
        String currentApp = "NULL";
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager usm = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,  time - 1000*1000, time);
            if (appList != null && appList.size() > 0) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                for (UsageStats usageStats : appList) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (mySortedMap != null && !mySortedMap.isEmpty()) {
                    currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                }
            }
        } else {
            ActivityManager am = (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> tasks = am.getRunningAppProcesses();
            currentApp = tasks.get(0).processName;
        }
        Log.e(TAG, "Current App in foreground is: " + currentApp);
        return currentApp;
    }
    @Override
    public void onInterrupt() {
        Log.d(TAG, "onInterrupt");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        CSVHelper.storeToCSV("TestService.csv", "Accessibility service onDestroy");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (mAccessTokenLoader != null) {
            mAccessTokenLoader.unregisterListener(mLoaderListener);
            mAccessTokenLoader.cancelLoad();
            mAccessTokenLoader.stopLoading();
        }
        return super.onUnbind(intent);
    }

    public static void init_application()
    {
        facebook = false;
        google = false;
        news = false;
        line = false;
        line_mes = false;
        instagram = false;
        youtube = false;
        ptt = false;
        takeNews = false;
        messenger = false;
        chrome = false;
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
    private void WriteToFile(String Text, String tag)
    {
        appear = 0;
        date = Utils.getTimeString(Utils.DATE_FORMAT_NOW_DAY);

        SharedPreferences pref = getSharedPreferences("URL", MODE_PRIVATE);
        Set<String> UrlSet = pref.getStringSet("UrlSet", new HashSet<String>());
        List<String> TitleAndWeb = new ArrayList<String>(UrlSet);

//        for(int i = 0; i < URLtemp.size(); i++)
        {
            if (!TitleAndWeb.contains(Text)) {
                TitleAndWeb.add(Text);
                try {
                    UrlSet = new HashSet<String>(TitleAndWeb);
                    pref.edit()
                            .putStringSet("UrlSet", UrlSet)
                            .apply();
                    File directory = new File(Environment.getExternalStorageDirectory().getPath() + PICTURE_DIRECTORY_PATH + date);

                    if (!directory.exists() || !directory.isDirectory()) {
                        directory.mkdirs();
                    }

                    String dataType = "Null";
                    String appName = tag;


                    long sessionID = getSharedPreferences("test", MODE_PRIVATE).getLong("SessionID", 0);
                    String fileName = "";

                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                    String date = ScheduleAndSampleManager.getTimeString(System.currentTimeMillis(), df);
                    String filePath = Environment.getExternalStorageDirectory() + PICTURE_DIRECTORY_PATH + date + "/" + fileName;
                    String content = Text;
                    Log.d(TAG, "news data record: " + sessionID + " " + fileName + " " + filePath + " " + content);
                    if (tag.equals("Messenger")) {
                        CSVHelper.storeUrlCSV(CSV_News, "Messenger", content);
                        getSharedPreferences("test", MODE_PRIVATE).edit().putString("AccessibilityUrl", "Messenger " +  content).apply();
//                        UpdateAllStreamGenerator();
                        newsDataRecord = new NewsDataRecord(sessionID, fileName, filePath, content);
                        dataType = "Url";
                    } else if (tag.equals("LineChat")) {
                        CSVHelper.storeUrlCSV(CSV_News, "Line", content);
                        getSharedPreferences("test", MODE_PRIVATE).edit().putString("AccessibilityUrl", "Line " + content).apply();
//                        UpdateAllStreamGenerator();
                        newsDataRecord = new NewsDataRecord(sessionID, fileName, filePath, content);
                        dataType = "Url";
                    } else if (tag.equals("PTT")) {
                        CSVHelper.storeUrlCSV(CSV_News, "PTT", content);
                        getSharedPreferences("test", MODE_PRIVATE).edit().putString("AccessibilityUrl", "PTT " + content).apply();
//                        UpdateAllStreamGenerator();
                        newsDataRecord = new NewsDataRecord(sessionID, fileName, filePath, content);
                        dataType = "Title";
                    }

                    db.NewsDataRecordDao().insertAll(newsDataRecord);

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
//        URLtemp.clear();
//        name = "";
    }

    private void DetectLastForeground(int EventType, String EventText, String EventPackage, String extra) {
        //Fixed: Window State Changed from the same application (showing keyboard within an app) should be ignored
        //boolean same_app = false;

        //String ForegroundNow = printForegroundTask();
        String Deviceid = getSharedPreferences("test", MODE_PRIVATE).getString("UserID", "");
//        String text = "";
//        String type = "";
//        String packageName = "";
        String Eventtype = EventTypeIwant(EventType);
//        if(Eventtype.equals("TYPE_VIEW_SCROLLED") && event_scroll){
//            Eventtype = "NA";
//        }
//        if(Eventtype.equals("TYPE_VIEW_SCROLLED") && !event_scroll){
//            event_scroll = true;
//        }
        if (!Eventtype.equals("NA") || !MyEventText.equals("NA")) {
//            if(!Eventtype.equals("TYPE_VIEW_SCROLLED"))
//            {
//                event_scroll = false;
//            }
//            type = Eventtype;
//            text = getEventText(event);
//            packageName = event.getPackageName().toString();
//            if (event.getContentDescription() != null) {
//                extra = event.getContentDescription().toString();
//                // Log.d(TAG,"extra : "+ extra);
//            }

            /*PackageInfo pkgInfo;
            try {
                pkgInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA);
            } catch (PackageManager.NameNotFoundException | NullPointerException | Resources.NotFoundException e) {
                pkgInfo = null;
            }*/

            if(isOurTarget(EventPackage))
            {
//                JSONObject jobject = new JSONObject();
                try {
//                    JSONObject appobject = new JSONObject();
//                    appobject.put(TIMESTAMP, System.currentTimeMillis());
//                    appobject.put(READABLE, getReadableTimeLong(System.currentTimeMillis()));
//                    appobject.put(DEVICEID, Deviceid);
//                    appobject.put(PACKAGENAME, packageName);
//                    appobject.put("MyEventText", MyEventText);
//                    appobject.put(EVENTTEXT, text);
//                    appobject.put(EVENTTYPE, type);
//                    appobject.put(EXTRA, extra);
//                    jobject.put("myAccessibility", appobject.toString());
                    if (accessibilityStreamGenerator != null) {
                        Log.d(TAG, "accessibilityStreamGenerator not null");
                        int index = Arrays.asList(NewsPack).indexOf(EventPackage);
                        String NewsApp_Name;
                        if(index >= 0){
                            NewsApp_Name = NewsName[index];
                        }
                        else{
                            NewsApp_Name = "";
                        }
                        Log.d(TAG, "News name: " + NewsApp_Name);
                        accessibilityStreamGenerator.setLatestInAppAction(System.currentTimeMillis(), Deviceid, EventPackage, EventText, Eventtype, MyEventText, extra, NewsApp_Name);
                        accessibilityStreamGenerator.updateStream();
                    }
//                    CSVHelper.storeAccessibilityCSV("CheckStoreAccess.csv", packageName, text, type, extra);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean isOurTarget(String pkgName){
        if(Arrays.asList(AppPack).contains(pkgName) || Arrays.asList(WebPack).contains(pkgName)
                || Arrays.asList(NewsPack).contains(pkgName) || Arrays.asList(PttPack).contains(pkgName)){
            return true;
        }
        return false;
    }
    public static boolean isSystemPackage(PackageInfo pkgInfo) {
        return pkgInfo != null && ((pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1);
    }

    public static String EventTypeIwant(int EventType)
    {
        String type = "NA";
        switch (EventType) {
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                type = "TYPE_VIEW_CLICKED";
                break;
            case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED:
                type = "TYPE_VIEW_LONG_CLICKED";
                break;
            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
                type = "TYPE_VIEW_FOCUSED";
                break;
            case AccessibilityEvent.TYPE_VIEW_SELECTED:
                type = "TYPE_VIEW_SELECTED";
                break;
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                type = "TYPE_VIEW_TEXT_CHANGED";
                break;
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                type = "TYPE_NOTIFICATION_STATE_CHANGED";
                break;
            case AccessibilityEvent.TYPE_ANNOUNCEMENT:
                type = "TYPE_ANNOUNCEMENT";
                break;
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                type = "TYPE_VIEW_SCROLLED";
                break;
            case AccessibilityEvent.TYPE_WINDOWS_CHANGED:
                type = "TYPE_WINDOWS_CHANGED";
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                type = "TYPE_WINDOW_STATE_CHANGED";
                break;
//            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
//                type = "YPE_WINDOW_CONTENT_CHANGED";
//                break;
        }
        return type;
    }
    public boolean getScreenStatus() {
        boolean screenOn = false;
        //use isInteractive after api 20
        if (mPowerManager.isInteractive()) screenOn = true;
        else screenOn = false;

        return screenOn;
    }

    public void NewsRelated(boolean open)
    {
        getSharedPreferences("test", MODE_PRIVATE).edit().putBoolean("ReadNews",open).apply();
        runPhoneStatusMainThread(open);
        try {
            Log.d(TAG, "Stream update runnable: " + System.currentTimeMillis());
//            streamManager.updateStreamGenerators();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createNotificationChannel(Context context) {
        Log.d(TAG, "createNotificationChannel");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    RECORDING_NOTIFICATION_ID,
                    "Accessibility Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            serviceChannel.setVibrationPattern(vibrate_effect);
            serviceChannel.enableVibration(true);

            ScreenCapture_manager =  (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            ScreenCapture_manager.createNotificationChannel(serviceChannel);
        }
    }
    public void SendCaptureNoti(String appName, boolean appFlag, Intent ScreenShotIntent, boolean screenshot){

        if(!screenshot){
            notificationListenService.startRecordingNotification(this, Accessibility_ID, appName, appFlag, ScreenShotIntent);
//            long last_dialog_time = 0L;
//            long now_dialog_time = System.currentTimeMillis();
//            boolean last_agree = false;
//            if(Last_Dialog_Time != null){
//                if(Last_Dialog_Time.containsKey(appName)) {
//                    last_dialog_time = Last_Dialog_Time.get(appName);
//                }
//            }
//            if(Last_Agree != null) {
//                if (Last_Agree.containsKey(appName)) {
//                    last_agree = Last_Agree.get(appName);
//                }
//            }
//            boolean pop_up = Dialog_pop(last_dialog_time, now_dialog_time);//有沒有超過interval
//            CSVHelper.storeToCSV("AccessibilityDetect.csv", "pop up: " + pop_up + " last agree: " + last_agree + " in " + appName);
////            Log.d(TAG, "ATesting: " + pop_up + " " + last_agree);
//            if (!pop_up && last_agree){
//                ScreenShotIntent.putExtra(appName, true);
//                ScreenShotIntent.putExtra("FromNotification", false);
//
//                ScreenShotIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
//                startActivity(ScreenShotIntent);
//            }

        }
//        if(screenshot && (notificationListenService.ifRecordingTransit(this,RECORDING_TITLE_CONTENT)
//                || notificationListenService.ifRecordingTransit(this,RECORDING_STOP_CONTENT) ) ){
//            notificationListenService.updateRecordingNotification(this, NotiIdRecord);
//            Log.d(TAG,"recording : ");
//        }
//        if(((!screenshot||stopRecordReceive) && notificationListenService.ifRecordingTransit(this,RECORDING_ONGOING_CONTENT))){
//            notificationListenService.stopRecordingNotification(this, NotiIdRecord);
//
//        }
//        if(!screenshot) {
//            ScreenShotIntent.putExtra(appName, appFlag);
//            ScreenShotIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
//            PendingIntent pendingIntent = PendingIntent.getActivity(this, //類似公式的東西
//                    Accessibility_ID, ScreenShotIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//            NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.mipmap.ic_launcher, "開始截圖", pendingIntent).build();
//
//            Notification notification;
//            AccessibilityBuilder = new NotificationCompat.Builder(this, RECORDING_NOTIFICATION_ID) //設定通知要有那些屬性
//                    .setContentTitle(SCRRENSHOT_TITLE_CONTENT) // 通知的Title
//                    .setContentText(SCRRENSHOT_TEXT)                        //通知的內容
//                    .setSmallIcon(R.drawable.ic_stat_name)            //通知的icon
////                    .setOngoing(true)                            //使用者滑不掉
//                    .setAutoCancel(false)           //點擊之後通知消失
//                    .setOnlyAlertOnce(true)
//                    .setVibrate(vibrate_effect)//震動模式
//                    .addAction(action);
//            ScreenCapture_manager.notify(Accessibility_ID, AccessibilityBuilder.build());
//        }
    }
    public void getMessengerName(AccessibilityNodeInfo nodeInfo){
            if (nodeInfo != null) {
//                readAllFreq();
                if(nodeInfo.getClassName() != null) {
                    if (nodeInfo.getClassName().equals("android.widget.FrameLayout")) {
                        for (int i = 0; i < nodeInfo.getChildCount(); i++) {
                            if (nodeInfo.getChild(i) != null) {
                                if(nodeInfo.getChild(i).getClassName() != null) {
                                    if(nodeInfo.getChild(i).getClassName().equals("android.view.ViewGroup")) {
                                        for (int j = 0; j < nodeInfo.getChild(i).getChildCount(); j++) {
                                            if (nodeInfo.getChild(i).getChild(j) != null) {
                                                if (nodeInfo.getChild(i).getChild(j).getChildCount() >= 2) {
                                                    if (nodeInfo.getChild(i).getChild(j).getClassName() != null && nodeInfo.getChild(i).getChild(j).getContentDescription() != null) {
                                                        if (nodeInfo.getChild(i).getChild(j).getClassName().equals("android.widget.Button")) {
                                                            appear++;
                                                            if(appear == 1){
                                                                name = "";
                                                            }
                                                            else{
                                                                name = nodeInfo.getChild(i).getChild(j).getContentDescription().toString().split(",")[0];
                                                                Log.d(TAG, "Target: " + name);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if(nodeInfo.getContentDescription() != null){
                    Log.d(TAG, "try to get name");
                    if (nodeInfo.getChildCount() == 0){
                        String str = nodeInfo.getContentDescription().toString();
                        if(str.contains("的聊天大頭貼")){
                            int index = str.indexOf("的聊天大頭貼");
                            String target = str.substring(0, index);
                            name = target;
                            Log.d(TAG, "Target: " + target);
                        }
                    }
                }
                //拿取聊天室box的名字
//                if (event.getPackageName().equals("com.facebook.orca")) {
//
//                    if(log_write == true) {
//                        if(log_count == 15) {
//
//                            StringBuilder writer = new StringBuilder();
//                            long time = System.currentTimeMillis();
//                            String time_readable = getReadableTime(time);//"yyyy-MM-dd HH:mm:ss"
//
//                            String content = fb_send_count + content_temp;
//                            writer.append(content + "\n");
//                            WriteToFile(writer, "DebugLog_send");
//
//                            content_temp = "";
//                            log_count = 0;
//                            log_write = false;
//                        }
//
//
//                        content_temp = content_temp + "," + event.getEventType() + ":" + nodeInfo.getContentDescription();
//                        log_count = log_count + 1;
//
//                    }
//
//                    if (SendAndFindName == 0) {
//                        //打完字而且發送出去
//                        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED
//                                && getEventText(event).contains("輸入訊息…")) {
//                            if (TempSend.equals("")) {
//                                Log.d(TAG, "--------------------------------");
//                                SendAndFindName = 1;
//                            } else {
//                                SendTo = TempSend;
//                                Log.d(TAG, "In else");
//                            }
//                        }
//                    } else {
//                        log_write = true;
//                        Rect BoundInScreen = new Rect();
//                        nodeInfo.getBoundsInScreen(BoundInScreen);
//                        //Log.d(TAG, "Top: " + BoundInScreen.top);
//                        //Log.d(TAG, "Left: " + BoundInScreen.left);
//                        if (count == 14) {
//                            if (BoundInScreen.top != 0 && BoundInScreen.left != 0) {
//                                if (BoundInScreen.top < min_top) {
//                                    min_top = BoundInScreen.top;
//                                    if (nodeInfo.getContentDescription() != null) {
//                                        TempSend = nodeInfo.getContentDescription().toString();
//                                    }
//                                }
//                            }
//
//                            fb_send_count = fb_send_count + 1;
//                            StringBuilder writer = new StringBuilder();
//                            long time = System.currentTimeMillis();
//                            String time_readable = getReadableTime(time);//"yyyy-MM-dd HH:mm:ss"
//
//                            String alias = TempSend.replaceAll(",", " ");
//                            String content = time + "," + time_readable + "," + "send" + "," + alias + "," +  fb_send_count;
//                            writer.append(content + "\n");
//                            WriteToFile(writer, "DebugLog");
//
//
//                            min_top = 10000;
//                            SendAndFindName = 0;
//                            count = 0;
//                            top_location_list.clear();
//                            left_location_list.clear();
//                            Log.d(TAG, "Send To: " + TempSend);
//                            long timestamp = System.currentTimeMillis();
//                            String Timestamp = String.valueOf(timestamp);
//                            String post_date = DateFormat.format("yyyy-MM-dd HH:mm:ss", timestamp).toString();
//                            String packageName = event.getPackageName().toString();
//                            String message = getSharedPreferences("NotiRecord", MODE_PRIVATE).getString("message", "NA");
//                            //Utils.writeCsv(Timestamp, post_date, packageName, TempSend, message, "NA", "NA", "Send notification");
//
//                            String Receiver = TempSend;
//                            TempSend = "";
//                            lastInteractWith = Receiver;
//                            Log.d(TAG, "LastInteract:" + lastInteractWith);
//                            getSharedPreferences("NotiSpec",MODE_PRIVATE).edit().putString("LastInteract",lastInteractWith).apply();
//
//                            Integer current_time = Math.toIntExact((System.currentTimeMillis() / 1000) + 3600 * 8);
//                            lastInteractTime = current_time;
//                            getSharedPreferences("NotiSpec",MODE_PRIVATE).edit().putLong("LastInteractTime", lastInteractTime).apply();
//                            if (global_freq.containsKey(Receiver)) {
//                                global_freq.put(Receiver, global_freq.get(Receiver) + 1);
//                            } else {
//                                global_freq.put(Receiver, 1);
//                            }
//                            Log.d(TAG, "----------------------global freq------------------------------");
//                            for (Object key : global_freq.keySet()) {
//                                Log.d(TAG, key + " : " + global_freq.get(key));
//                            }
//                            //daily frequency
//                            day_count = day_count + 1;
//                            if (day_freq.isEmpty()) {
//                                day_freq.put(Receiver, 1);
//                            } else {
//                                if (day_freq.containsKey(Receiver)) {
//                                    day_freq.put(Receiver, day_freq.get(Receiver) + 1);
//                                } else {
//                                    day_freq.put(Receiver, 1);
//                                }
//                            }
//                            Log.d(TAG,"Testing: " + ListContainString(favorite_list,Receiver));
//                            if (ListContainString(favorite_list,Receiver)) {
//                                fav_count = fav_count + 1;
//                                if (favorite_freq.isEmpty()) {
//                                    favorite_freq.put(Receiver, 1);
//                                } else {
//                                    if (favorite_freq.containsKey(Receiver)) {
//                                        favorite_freq.put(Receiver, favorite_freq.get(Receiver) + 1);
//                                    } else {
//                                        favorite_freq.put(Receiver, 1);
//                                    }
//                                }
//                            }
//                            else {
//                                nyt_count = nyt_count + 1;
//                                if (notYouThink_freq.isEmpty()) {
//                                    notYouThink_freq.put(Receiver, 1);
//                                } else {
//                                    if (notYouThink_freq.containsKey(Receiver)) {
//                                        notYouThink_freq.put(Receiver, notYouThink_freq.get(Receiver) + 1);
//                                    } else {
//                                        notYouThink_freq.put(Receiver, 1);
//                                    }
//                                }
//                            }
//
//
//
//
//                            Log.d(TAG, "----------------------daily freq------------------------------");
//                            for (Object key : day_freq.keySet()) {
//                                Log.d(TAG, key + " : " + day_freq.get(key));
//                            }
//
//                            if (ten_mins_list.size() == 0) {
//                                ten_mins_list.put(Receiver, current_time);
//                                ten_mins_freq.put(Receiver, 1);
//                            } else {
//                                if (ten_mins_list.containsKey(Receiver)) {
//                                    ten_mins_freq.put(Receiver, ten_mins_freq.get(Receiver) + 1);
//                                } else {
//                                    ten_mins_freq.put(Receiver, 1);
//                                }
//                                ten_mins_list.put(Receiver, current_time);
//                                ten_mins_list = (HashMap<String, Integer>) sortByComparator(ten_mins_list, true);
//                                Log.d(TAG, "----------------------new list------------------------------");
//                                for (Object key : ten_mins_list.keySet()) {
//                                    Log.d(TAG, key + " : " + ten_mins_list.get(key));
//                                }
//                            }
//                            Log.d(TAG, "-------------------------after remove-----------------------------");
//                            for (Object key : ten_mins_list.keySet()) {
//                                Log.d(TAG, key + " : " + ten_mins_list.get(key));
//                            }
//
//                            for (Object key : ten_mins_list.keySet()) {
//                                Log.d(TAG, key + " : " + ten_mins_freq.get(key));
//                            }
//                            if (State == 0) {
//                                State = 1;
//                            }
////                                if (State == 0) {
////                                    State = 3;
////                                } else if (State == 2) {
////                                    State = 3;
////                                } else if (State == 3) {
////                                    if (ten_mins_list.size() >= 2) {
////                                        State = 1;
////                                    }
////                                } else if (State == 4) {
////                                    State = 3;
////                                }
//                        } else {
//                            count = count + 1;
//                            if (BoundInScreen.top != 0 && BoundInScreen.left != 0) {
//                                if (BoundInScreen.top < min_top) {
//                                    min_top = BoundInScreen.top;
//                                    if (nodeInfo.getContentDescription() != null) {
//                                        TempSend = nodeInfo.getContentDescription().toString();
//                                    }
//                                }
//                            }
//
//                        }
//                    }
//                }
//                else if (event.getPackageName().equals("jp.naver.line.android")) {
//                    String message = getEventText(event);
//                    //使用者進入誰的聊天室
//                    TempSend = getLineName(nodeInfo);
//                    if(!TempSend.equals("")) {
//                        pref.edit().putString("TempSend", TempSend).apply();
//                        Log.d(TAG, "temp send: " + TempSend);
//                    }
//                    /*int[] index = {message.indexOf("下午"), message.indexOf("上午"), message.indexOf("昨天"), FindDateString(message)};
//                    for (int i = 0; i < index.length; i++) {
//                        if (index[i] >= 0) {
//                            //TempSend = message.substring(0, index[i]);
//                            Log.d(TAG, "temp send: " + TempSend);
//                            break;
//                        }
//                    }*/
//
//                    if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED
//                            && !getEventText(event).equals("")) {
//                        Send_message = getEventText(event);
//                    }
//                    //看EditBox有沒有字在裡面 or 讚的圖示變成發送的圖示
//                    if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED
//                            && !getEventText(event).equals("") && !typing) {
//                        //打字中....
//                        typing = true;
//                    } else if (nodeInfo.getContentDescription() != null) {
//                        if (nodeInfo.getContentDescription().equals("傳送")) {
//                            typing = true;
//                        }
//                    }
//
//                    if(nodeInfo.getContentDescription() != null) {
//                        //按下發送
//                        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED
//                                && nodeInfo.getContentDescription().equals("錄音")) {
//
//                            StringBuilder writer = new StringBuilder();
//                            long time = System.currentTimeMillis();
//                            String time_readable = getReadableTime(time);//"yyyy-MM-dd HH:mm:ss"
//
//                            SendTo = pref.getString("TempSend", "");
//                            String alias = SendTo.replaceAll(",", " ");
//                            String content = time + "," + time_readable + "," + "send" + "," + alias;
//                            writer.append(content + "\n");
//                            WriteToFile(writer, "DebugLog");
//
//
//                            typing = false;
//                            long timestamp = System.currentTimeMillis();
//                            String Timestamp = String.valueOf(timestamp);
//                            String post_date = DateFormat.format("yyyy-MM-dd HH:mm:ss", timestamp).toString();
//                            String packageName = event.getPackageName().toString();
//                            //Utils.writeCsv(Timestamp, post_date, packageName, SendTo, Send_message, "NA", "NA", "Send notification");
//                            String Receiver = SendTo;
//                            Integer current_time = Math.toIntExact((System.currentTimeMillis() / 1000) + 3600 * 8);
//                            lastInteractTime = current_time;
//                            getSharedPreferences("NotiSpec",MODE_PRIVATE).edit().putLong("LastInteractTime", lastInteractTime).apply();
//                            if (global_freq.containsKey(Receiver)) {
//                                global_freq.put(Receiver, global_freq.get(Receiver) + 1);
//                            } else {
//                                global_freq.put(Receiver, 1);
//                            }
//                            Log.d(TAG, "----------------------global freq------------------------------");
////                            for (Object key : global_freq.keySet()) {
////                                Log.d(TAG, key + " : " + global_freq.get(key));
////                            }
//                            //daily frequency
//                            day_count = day_count + 1;
//                            if (day_freq.isEmpty()) {
//                                day_freq.put(Receiver, 1);
//                            } else {
//                                if (day_freq.containsKey(Receiver)) {
//                                    day_freq.put(Receiver, day_freq.get(Receiver) + 1);
//                                } else {
//                                    day_freq.put(Receiver, 1);
//                                }
//                            }
////                            Log.d(TAG,"Testing: " + ListContainString(favorite_list,currentTitle));
//                            if (ListContainString(favorite_list,Receiver)) {
//                                fav_count = fav_count + 1;
//                                if (favorite_freq.isEmpty()) {
//                                    favorite_freq.put(Receiver, 1);
//                                } else {
//                                    if (favorite_freq.containsKey(Receiver)) {
//                                        favorite_freq.put(Receiver, favorite_freq.get(Receiver) + 1);
//                                    } else {
//                                        favorite_freq.put(Receiver, 1);
//                                    }
//                                }
//                            }
//                            else {
//                                nyt_count = nyt_count + 1;
//                                if (notYouThink_freq.isEmpty()) {
//                                    notYouThink_freq.put(Receiver, 1);
//                                } else {
//                                    if (notYouThink_freq.containsKey(Receiver)) {
//                                        notYouThink_freq.put(Receiver, notYouThink_freq.get(Receiver) + 1);
//                                    } else {
//                                        notYouThink_freq.put(Receiver, 1);
//                                    }
//                                }
//                            }
//
////                            Log.d(TAG, "----------------------daily freq------------------------------");
////                            for (Object key : day_freq.keySet()) {
////                                Log.d(TAG, key + " : " + day_freq.get(key));
////                            }
//
//                            if (ten_mins_list.size() == 0) {
//                                ten_mins_list.put(Receiver, current_time);
//                                ten_mins_freq.put(Receiver, 1);
//                            } else {
//                                if (ten_mins_list.containsKey(Receiver)) {
//                                    ten_mins_freq.put(Receiver, ten_mins_freq.get(Receiver) + 1);
//                                } else {
//                                    ten_mins_freq.put(Receiver, 1);
//                                }
//                                ten_mins_list.put(Receiver, current_time);
//                                ten_mins_list = (HashMap<String, Integer>) sortByComparator(ten_mins_list, true);
////                                Log.d(TAG, "----------------------new list------------------------------");
////                                for (Object key : ten_mins_list.keySet()) {
////                                    Log.d(TAG, key + " : " + ten_mins_list.get(key));
////                                }
//                            }
//////                            Log.d(TAG, "-------------------------after remove-----------------------------");
////                            for (Object key : ten_mins_list.keySet()) {
////                                Log.d(TAG, key + " : " + ten_mins_list.get(key));
////                            }
////
////                            for (Object key : ten_mins_list.keySet()) {
////                                Log.d(TAG, key + " : " + ten_mins_freq.get(key));
////                            }
//                            if (State == 0) {
//                                State = 1;
//                            }
//
//                            Log.d(TAG, "Send To: " + SendTo);
//                            getSharedPreferences("NotiSpec",MODE_PRIVATE).edit().putString("LastInteract",SendTo).apply();
//                        }
//                    }
//                }
//                storeAllFreq();
            }
    }
    public void getLineName(AccessibilityNodeInfo nodeInfo){
        if(nodeInfo.getClassName() != null) {
            if (nodeInfo.getClassName().equals("android.widget.RelativeLayout")) {
                for (int i = 0; i < nodeInfo.getChildCount(); i++) {
                    if (nodeInfo.getChild(i) != null) {
//                        Log.d(TAG, "NodeInfo child class: " + nodeInfo.getChild(i).getClassName());
//                        Log.d(TAG, "NodeInfo child text: " + nodeInfo.getChild(i).getText());
//                        Log.d(TAG, "NodeInfo child ContentDescription: " + nodeInfo.getChild(i).getContentDescription());
                        if(nodeInfo.getChild(i).getClassName() != null && nodeInfo.getChild(i).getContentDescription() != null) {
                            if (nodeInfo.getChild(i).getClassName().equals("android.widget.LinearLayout")
                                    && nodeInfo.getChild(i).getContentDescription().equals("撥號")) {
                                for (int j = 0; j < nodeInfo.getChild(i).getChildCount(); j++) {
                                    if (nodeInfo.getChild(i).getChild(j) != null) {
//                                        Log.d(TAG, "NodeInfo childchild class: " + nodeInfo.getChild(i).getChild(j).getClassName());
//                                        Log.d(TAG, "NodeInfo childchild text: " + nodeInfo.getChild(i).getChild(j).getText());
//                                        Log.d(TAG, "NodeInfo childchild ContentDescription: " + nodeInfo.getChild(i).getChild(j).getContentDescription());
                                        if(nodeInfo.getChild(i).getChild(j).getClassName() != null) {
                                            if(nodeInfo.getChild(i).getChild(j).getClassName().equals("android.widget.LinearLayout")) {
                                                for (int k = 0; k < nodeInfo.getChild(i).getChild(j).getChildCount(); k++) {
                                                    if (nodeInfo.getChild(i).getChild(j).getChild(k) != null) {
//                                                        Log.d(TAG, "NodeInfo childchildchild class: " + nodeInfo.getChild(i).getChild(j).getChild(k).getClassName());
//                                                        Log.d(TAG, "NodeInfo childchildchild text: " + nodeInfo.getChild(i).getChild(j).getChild(k).getText());
//                                                        Log.d(TAG, "NodeInfo childchildchild ContentDescription: " + nodeInfo.getChild(i).getChild(j).getChild(k).getContentDescription());
                                                        if(nodeInfo.getChild(i).getChild(j).getChild(k).getClassName() != null
                                                                && nodeInfo.getChild(i).getChild(j).getChild(k).getText() != null){
                                                            if(nodeInfo.getChild(i).getChild(j).getChild(k).getClassName().equals("android.widget.TextView")){
                                                                name = nodeInfo.getChild(i).getChild(j).getChild(k).getText().toString();
                                                                Log.d(TAG, "Target: " + name);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    public void storeSession(String appname, String data_type){
//        try{
//            Thread.sleep(500);
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//        CSVHelper.storeToCSV("AccessibilityDetect.csv", appname + " is open");
        long phone_session_id = 1;
        UserDataRecord userRecord = db.userDataRecordDao().getLastRecord();
        if(userRecord!=null) {
            phone_session_id = userRecord.getPhoneSession();
            Log.d("Test", "userRecord is not null");
        }
        Log.d("Test", "phone session id is: " + phone_session_id);
        sessionDataRecord = new SessionDataRecord(ScheduleAndSampleManager.getCurrentTimeString(), "NA", data_type, appname, phone_session_id);
        db.SessionDataRecordDao().insertAll(sessionDataRecord);
        pref.edit().putLong("SessionID", db.SessionDataRecordDao().getLastRecord().get_id()).apply();
    }
    public void endSession(){
        lastid = pref.getLong("SessionID", 0);
        db.SessionDataRecordDao().updateSession(lastid, ScheduleAndSampleManager.getCurrentTimeString(), System.currentTimeMillis());
        NewsRelated(false);
    }

    private BroadcastReceiver Recording = new BroadcastReceiver() {

        @SuppressLint("LongLogTag")
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("FloatingActionButtonService","in on receive");
            Log.d("FloatingActionButtonService","intent :"+intent.getAction().toString());

            if(intent.getAction().equals(STOP_RECORDING)){
                Log.d("FloatingActionButtonService","stop in activty ");
                String appName = intent.getStringExtra("appName");

                Last_Agree.put(appName, false);

                Log.d("FloatingActionButtonService",appName);
//                Last_Dialog_Time.put(appName, System.currentTimeMillis());
//                Last_Agree.put(appName, false);
                stopService(new Intent(getApplicationContext(), ScreenCapture.class));
//                endSession();
            }
            else if(intent.getAction().equals(START_RECORDING)){
                Log.d("checkBroadcast","receive start recording");
            }
        }
    };

}
