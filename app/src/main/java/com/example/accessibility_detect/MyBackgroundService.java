package com.example.accessibility_detect;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import labelingStudy.nctu.minuku.DBHelper.appDatabase;
import labelingStudy.nctu.minuku.Utilities.CSVHelper;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.manager.MinukuStreamManager;
import labelingStudy.nctu.minuku.model.DataRecord.UserDataRecord;

import static labelingStudy.nctu.minuku.config.Constants.DATA_SAVED_BROADCAST;
import static labelingStudy.nctu.minuku.config.Constants.QUESTIONNAIRE_TEXT;
import static labelingStudy.nctu.minuku.config.Constants.QUESTIONNAIRE_TITLE_CONTENT;
import static labelingStudy.nctu.minuku.config.Constants.REMINDER_TEXT;
import static labelingStudy.nctu.minuku.config.Constants.REMINDER_TITLE;
import static labelingStudy.nctu.minuku.config.SharedVariables.IS_ALIVE;
import static labelingStudy.nctu.minuku.config.SharedVariables.Last_Agree;
import static labelingStudy.nctu.minuku.config.SharedVariables.Last_Dialog_Time;
import static labelingStudy.nctu.minuku.config.SharedVariables.Random_session_num;
import static labelingStudy.nctu.minuku.config.SharedVariables.SCHEDULE_ALARM;
import static labelingStudy.nctu.minuku.config.SharedVariables.SERVICE_CHECKER;
import static labelingStudy.nctu.minuku.config.SharedVariables.Trigger_list;
import static labelingStudy.nctu.minuku.config.SharedVariables.getReadableTime;
import static labelingStudy.nctu.minuku.config.SharedVariables.getReadableTimeLong;

//import io.fabric.sdk.android.Fabric;

public class MyBackgroundService extends Service {
    public static final String TAG = "MyService";
    private long appStartHour;
    boolean exec = false;
    private SharedPreferences pref;
    private static int ESM_REQUEST = 0;
    private static int REMIND_REQUEST = 1;
    private Runnable runnable;
    private Handler handler;
    private long[] vibrate_effect = {100, 200, 300, 300, 500, 300, 300};
    private boolean first = true;
    private boolean setting = false;
    private boolean day_first = true;
    private long ESMremove = 900000; //50秒後問卷沒回答的話移除
    public final int ESMnumber = 10;
    public final int MinHour = 9;
    public final int MaxHour = 22;
    public final int remind_hour = 8;
    public final int Interval = 60;
    public static final int CheckInterval = 5;
    public static boolean isBackgroundServiceRunning = false;
    public static boolean isBackgroundRunnableRunning = false;
    private ScheduledExecutorService mScheduledExecutorService;
    ScheduledFuture<?> mScheduledFuture, mScheduledFutureIsAlive, mScheduledPhoneState;
    final static String CHECK_RUNNABLE_ACTION = "checkRunnable";
    final static String CONNECTIVITY_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    MinukuStreamManager streamManager;
    NotificationManager mNotificationManager;
    IntentFilter intentFilter;
    private Button esm_button;
    private Button diary_button;
    PhoneStateChecker mPhoneStateChecker;
    ShutDownReceiver shutdownReceiver;
    private BroadcastReceiver broadcastReceiver;
    NotificationManager ESM_manager;
    NotificationManager reminder_manager;
    public static final String CHANNEL_ID = "BackgroundServiceChannel";
    public static final String REMINDER_ID = "RemindChannel";
    public static final int ESM_ID = 1;
    public static final int REMIND_ID = 2;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate() executed");

//        Fabric.with(this, new Crashlytics());
        StartForeground();

        boolean getSettingPermission = getSharedPreferences("test", MODE_PRIVATE)
                                        .getBoolean("SettingPermission", false);
        Log.d(TAG, "SettingPermission: " + getSettingPermission);
        pref = getSharedPreferences("test",MODE_PRIVATE);
        pref.edit().putBoolean("DialogDeny", false).apply();
//        boolean iskilled = pref.getBoolean("IsKilled", false);
//        if(iskilled){
//            Intent MainIntent = new Intent(this,MainActivity.class);
//            startActivity(MainIntent);
//        }
        if(!getSettingPermission){
            SharedPreferences p = getSharedPreferences("test", MODE_PRIVATE);
            p.edit().putBoolean("SettingPermission", true).apply();

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
        isBackgroundServiceRunning = false;
        isBackgroundRunnableRunning = false;
        streamManager = MinukuStreamManager.getInstance();
        mScheduledExecutorService = Executors.newScheduledThreadPool(Constants.MAIN_THREAD_SIZE);

        intentFilter = new IntentFilter();
        intentFilter.addAction(CONNECTIVITY_ACTION);
        intentFilter.addAction(Constants.ACTION_CONNECTIVITY_CHANGE);

        for(int i = 0; i < Trigger_list.length; i++){
            Last_Agree.put(Trigger_list[i], false);
            Last_Dialog_Time.put(Trigger_list[i], 0L);
        }
        Random_session_num = (int)(Math.random()* 3 + 1);//random 1~3
        CSVHelper.storeToCSV("ESM_random_number.csv", "In background reset random number to: " + Random_session_num);
        Log.d(TAG, "Random session number = " + Random_session_num);
        //IntentFilter checkRunnableFilter = new IntentFilter(CHECK_RUNNABLE_ACTION);
        //registerReceiver(CheckRunnableReceiver, checkRunnableFilter);

        /*mPhoneStateChecker= new PhoneStateChecker();
        registerReceiver(mPhoneStateChecker,intentFilter);*/

        IntentFilter itFilter = new IntentFilter("PhoneStateChecker");
        mPhoneStateChecker = new PhoneStateChecker();
        registerReceiver(mPhoneStateChecker, itFilter); //註冊廣播接收器

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.ACTION_SHUTDOWN");
        intentFilter.addAction("android.intent.action.RECEIVE_QUICKBOOT_POWEROFF");
        intentFilter.addAction("com.htc.intent.action.QUICKBOOT_POWEROFF");
        shutdownReceiver = new ShutDownReceiver();
        registerReceiver(shutdownReceiver, intentFilter); //註冊廣播接收器

        //LocalBroadcastManager.getInstance(this).registerReceiver(CheckRunnableReceiver,checkRunnableFilter);

        //registerConnectivityNetworkMonitorForAPI21AndUp();

        // 9/14
        appDatabase db;
        db = appDatabase.getDatabase(this);
        UserDataRecord userRecord = db.userDataRecordDao().getLastRecord();

        Long nowTime = new Date().getTime();
        appStartHour = getReadableTimeLong(nowTime);

        if(userRecord == null){
            Log.d(TAG, "user data record null ");
            UserDataRecord userFirst = new UserDataRecord("NA", "", "","0",
                    "0", "9","22", 0L,
                    false, false, appStartHour, false);
            db.userDataRecordDao().insertAll(userFirst);
        }
        else{
            Log.d(TAG, "user data record Not null ");
        }

        if (!InstanceManager.isInitialized()) {
            //  CSVHelper.storeToCSV(CSVHelper.CSV_RUNNABLE_CHECK, "Going to start the runnable.");
            Log.d(TAG, "InstanceManager");
            InstanceManager.getInstance(this);
            //SessionManager.getInstance(this);
            //MobilityManager.getInstance(this);
        }
        //mScheduledExecutorService.schedule(AlarmRunnable,0,TimeUnit.SECONDS);
        handler = new Handler();
        /*runnable = new Runnable() {
            @Override
            public void run() {

                Calendar cal = Calendar.getInstance();
                //Log.d(TAG, "Min hour: " + MinHour + " Max hour: " + MaxHour);
                if ((cal.get(Calendar.HOUR_OF_DAY) == 0 && cal.get(Calendar.MINUTE) == 5 && !setting) || first) {
                    Log.d(TAG, "Set new Alarm");
                    first = false;
                    setting = true;
                    getSharedPreferences("test", MODE_PRIVATE).edit().putInt("Esm_Num", 0).apply();
                    SharedPreferences pref = getSharedPreferences("URL", MODE_PRIVATE);
                    Set<String> UrlSet = pref.getStringSet("UrlSet", new HashSet<String>());
                    List<String> TitleAndWeb = new ArrayList<String>(UrlSet);

                    TitleAndWeb.clear();

                    UrlSet = new HashSet<String>(TitleAndWeb);
                    pref.edit()
                            .putStringSet("UrlSet", UrlSet)
                            .commit();

                    Calendar start_cal = Calendar.getInstance(); //取得時間
                    start_cal.set(Calendar.HOUR_OF_DAY, MinHour);
                    start_cal.set(Calendar.MINUTE, 0);
                    start_cal.set(Calendar.SECOND, 0);

                    Calendar end_cal = Calendar.getInstance(); //取得時間
                    end_cal.set(Calendar.HOUR_OF_DAY, MaxHour);
                    end_cal.set(Calendar.MINUTE, 0);
                    end_cal.set(Calendar.SECOND, 0);

                    ArrayList<Long> ESMtimeList = random_times(start_cal, end_cal, ESMnumber, Interval, Constants.DEVICE_ID);

                    for (int i = 0; i < ESMnumber; i++) {
                        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
                        // 以日期字串組出不同的 category 以添加多個鬧鐘
                        //intent.addCategory("ID." + ESMtimeList.get(i));
                        Log.d(TAG, stampToDate(ESMtimeList.get(i)));
                        CSVHelper.storeToCSV("AlarmCreate.csv", "ESM alarm time: " + stampToDate(ESMtimeList.get(i)));
                        String AlarmTimeTag = "Alarmtime " + stampToDate(ESMtimeList.get(i));

                        intent.setAction(ESM_ALARM);
                        //intent.putExtra("time", AlarmTimeTag);

                        PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(), i, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(ALARM_SERVICE);

                        am.setExact(AlarmManager.RTC_WAKEUP, ESMtimeList.get(i), pi);       //註冊鬧鐘
                    }
                    Calendar remind_cal = Calendar.getInstance(); //取得時間
                    remind_cal.set(Calendar.HOUR_OF_DAY, remind_hour);
                    remind_cal.set(Calendar.MINUTE, 0);
                    remind_cal.set(Calendar.SECOND, 0);
                    add_reminder(getApplicationContext(), remind_cal);
                }
                if ((cal.get(Calendar.HOUR) != 0 && setting)) {
                    Log.d(TAG, "Set setting to false");
                    setting = false;
                }
                handler.postDelayed(runnable, 500);
                //Log.d(TAG, "=====================");
                //再設一個alarm來偵測Service是不是running
            }
        };*/
        //handler.postDelayed(runnable, 1000);
    }

    public static String stampToDate(long timeMillis) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(timeMillis);
        return simpleDateFormat.format(date);
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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand() executed");
        CSVHelper.storeToCSV("TestService.csv", "Background service onStartCommand");

        // 6/17
        String Fixed_date = "2021-06-30";
        ArrayList<String> FileList = new ArrayList<>();
        String[] images = null;
        try{
            images = getAssets().list("esm");
            FileList = new ArrayList<String>(Arrays.asList(images));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        AssetManager assetManager = getAssets();
        Bitmap bmp = null;
        InputStream istr = null;
        for (int i = 0; i < FileList.size(); i++) { //每一張圖
            Log.d(TAG, FileList.get(i));
            File fileName = new File (getFilesDir() + "/" + FileList.get(i));
//            File fileName = new File(Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.example.accessibility_detect/" + FileList.get(i));
            try {
                istr = assetManager.open("esm/" + FileList.get(i));
                bmp = BitmapFactory.decodeStream(istr);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                FileOutputStream out = new FileOutputStream(fileName);
                if (out != null) {
                    bmp.compress(Bitmap.CompressFormat.JPEG, 70, out);
                    out.flush();
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (bmp != null) {
                    bmp.recycle();
                }
            }
        }
//        createNotificationChannel(this);

        MyAccessibilityService mobileAccessibilityService;
        mobileAccessibilityService = new MyAccessibilityService();
        Intent in = new Intent(this, MyAccessibilityService.class);
        this.startService(in);

        appDatabase db;
        db = appDatabase.getDatabase(this);
        UserDataRecord userRecord = db.userDataRecordDao().getLastRecord();
        if(userRecord != null) {
            exec = userRecord.getExec();
        }
        else{
            Log.d(TAG, "user record is null");
        }
//        exec = getSharedPreferences("test",MODE_PRIVATE).getBoolean("execute", false);

        // original
        add_ServiceChecker(this);
        updateNotificationAndStreamManagerThread();

        //sure that these command execute only once even restart
        if(!exec) {
            //4/17
            //SendESMnoti(this);
            //add_reminder(this);

            Long nowTime = new Date().getTime();
            appStartHour = getReadableTimeLong(nowTime);

            if(userRecord != null) {
                long _id = userRecord.get_id();
                db.userDataRecordDao().updateExec(_id, true);
                db.userDataRecordDao().updateAppstart(_id, appStartHour);
            }
//            getSharedPreferences("test", MODE_PRIVATE).edit().putLong("appStartHour", appStartHour).apply();


//            getSharedPreferences("test", MODE_PRIVATE).edit().putBoolean("execute", true).apply();
        }

        mMainThread = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                try {
                    //  CSVHelper.storeToCSV(CSVHelper.CSV_RUNNABLE_CHECK, "updateRun_isBackgroundServiceRunning ? "+isBackgroundServiceRunning);
                    //  CSVHelper.storeToCSV(CSVHelper.CSV_RUNNABLE_CHECK, "updateRun_isBackgroundServiceRunning ? "+isBackgroundRunnableRunning);
                    Log.d(TAG, "Stream update runnable: " + System.currentTimeMillis());
                    streamManager.updateStreamGenerators();
                } catch (Exception e) {
                    //   CSVHelper.storeToCSV(CSVHelper.CSV_RUNNABLE_CHECK, "Background, service update, stream, Exception");
                    //   CSVHelper.storeToCSV(CSVHelper.CSV_RUNNABLE_CHECK, Utils.getStackTrace(e));
                }
                Log.d("BootCompleteReceiver", "In updateStreamManagerRunnable ");

                mMainThread.postDelayed(this, 5*1000);
            }
        };
        mMainThread.post(runnable);
        //LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(CHECK_RUNNABLE_ACTION));
        return super.onStartCommand(intent, flags, startId);
    }
    private static Handler mMainThread;

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

            ESM_manager = context.getSystemService(NotificationManager.class);
            ESM_manager.createNotificationChannel(serviceChannel);

            NotificationChannel reminderChannel = new NotificationChannel(
                    REMINDER_ID,
                    "Remind Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            reminderChannel.setVibrationPattern(vibrate_effect);
            reminderChannel.enableVibration(true);

            reminder_manager = context.getSystemService(NotificationManager.class);
            reminder_manager.createNotificationChannel(reminderChannel);
        }
    }

    public void SendESMnoti(Context context)
    {
        String content = "Please fill the questionnaire";
        String ESMtime = getReadableTime(System.currentTimeMillis());//2020-04-11 14:29:35
        getSharedPreferences("test",MODE_PRIVATE).edit().putString("ESMtime", ESMtime).apply();//14:29:35
        int EsmNum = context.getSharedPreferences("test", MODE_PRIVATE).getInt("Esm_Num", 0);
        EsmNum++;
        context.getSharedPreferences("test", MODE_PRIVATE).edit().putInt("Esm_Num", EsmNum).apply();
        Intent notificationIntent = new Intent(context, ChoosePicture.class); //Intent(this, 點下去會跳到ESM class)
        PendingIntent pendingIntent = PendingIntent.getActivity(context, //類似公式的東西
                ESM_ID, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID) //設定通知要有那些屬性
                .setContentTitle(QUESTIONNAIRE_TITLE_CONTENT) // 通知的Title
                .setContentText(QUESTIONNAIRE_TEXT + "(" + EsmNum + ")")                        //通知的內容
                .setSmallIcon(R.drawable.ic_stat_name)            //通知的icon
                .setContentIntent(pendingIntent)               //點下去會跳到ESM class
                .setOngoing(false)                              //使用者滑不掉
                .setAutoCancel(true)                           //點擊之後通知消失
                .setVibrate(vibrate_effect)                    //震動模式
                .setTimeoutAfter(900000)                    //幾毫秒之後自動消失
                .setPriority(Notification.PRIORITY_HIGH)
                .build();

        ESM_manager.notify(ESM_ID, notification);                  //發送通知

    }
    public void add_reminder(Context context) {
        Log.d(TAG, "Reminder Here");
        String remind = "Remember to upload yesterday's pictures!!";

        Intent UploadServerIntent = new Intent(context, WiFireminder.class);
        PendingIntent UploadIntent = PendingIntent.getActivity(context, 300, UploadServerIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Button
        NotificationCompat.Action action2 = new NotificationCompat.Action.Builder(R.mipmap.ic_launcher, "上傳", UploadIntent).build();

        Notification reminder = new NotificationCompat.Builder(context, REMINDER_ID)
                .setContentTitle(REMINDER_TITLE)
                .setContentText(REMINDER_TEXT)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setAutoCancel(true)
                .setOngoing(true)
                .setVibrate(vibrate_effect)
                .addAction(action2)
                .setPriority(Notification.PRIORITY_HIGH)
                .build();
        reminder_manager.notify(REMIND_ID, reminder);
    }

    public static void add_ServiceChecker(Context context) {
        Log.d(TAG, "add service checker");
        //Log.d(TAG, "remind time: " + String.valueOf(cal.get(Calendar.MONTH)) + "." + String.valueOf(cal.get(Calendar.DATE)) + " " + String.valueOf(cal.get(Calendar.HOUR_OF_DAY)) + ":" + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND));
        try {
            Thread.sleep(200);
        } catch (Exception e) {

        }
        Intent intent = new Intent(context, AlarmReceiver.class);
        // 以日期字串組出不同的 category 以添加多個鬧鐘
        //intent.addCategory("ID." + String.valueOf(cal.get(Calendar.MONTH)) + "." + String.valueOf(cal.get(Calendar.DATE)) + "-" + String.valueOf((cal.get(Calendar.HOUR_OF_DAY) )) + "." + String.valueOf(cal.get(Calendar.MINUTE)) + "." + String.valueOf(cal.get(Calendar.SECOND)));
        //String AlarmTimeTag = "Alarmtime " + String.valueOf(cal.get(Calendar.HOUR_OF_DAY)) + ":" + String.valueOf(cal.get(Calendar.MINUTE)) + ":" + String.valueOf(cal.get(Calendar.SECOND));

        intent.setAction(SERVICE_CHECKER);
        //intent.putExtra("time", AlarmTimeTag);

        PendingIntent pi = PendingIntent.getBroadcast(context, 20, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        long time_fired = System.currentTimeMillis() + CheckInterval * 1000 * 60;
        Log.d(TAG, "Service Checker fire time: " + stampToDate(time_fired));
        CSVHelper.storeToCSV("AlarmCreate.csv", "ServiceChecker alarm time: " + stampToDate(time_fired));
        am.setExact(AlarmManager.RTC_WAKEUP, time_fired, pi);       //註冊鬧鐘
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    Runnable updateStreamManagerRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                //  CSVHelper.storeToCSV(CSVHelper.CSV_RUNNABLE_CHECK, "updateRun_isBackgroundServiceRunning ? "+isBackgroundServiceRunning);
                //  CSVHelper.storeToCSV(CSVHelper.CSV_RUNNABLE_CHECK, "updateRun_isBackgroundServiceRunning ? "+isBackgroundRunnableRunning);
                Log.d(TAG, "Stream update runnable: " + System.currentTimeMillis());
                streamManager.updateStreamGenerators();
            } catch (Exception e) {
                //   CSVHelper.storeToCSV(CSVHelper.CSV_RUNNABLE_CHECK, "Background, service update, stream, Exception");
                //   CSVHelper.storeToCSV(CSVHelper.CSV_RUNNABLE_CHECK, Utils.getStackTrace(e));
            }
            Log.d("BootCompleteReceiver", "In updateStreamManagerRunnable ");
        }
    };

    private void updateNotificationAndStreamManagerThread() {


        /*mScheduledFuture = mScheduledExecutorService.scheduleAtFixedRate(//每一分鐘偵測一次phone state並更新到room database
                updateStreamManagerRunnable,
                Constants.STREAM_UPDATE_DELAY,
                Constants.STREAM_UPDATE_FREQUENCY,
                TimeUnit.SECONDS);*/

        try
        {
            Thread.sleep(200);
            Intent Isalive_intent = new Intent(this, AlarmReceiver.class);
            Isalive_intent.setAction(IS_ALIVE );

            PendingIntent Isalive_pi = PendingIntent.getBroadcast(this, 200, Isalive_intent, PendingIntent.FLAG_CANCEL_CURRENT);

            AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);

            am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),30*60*1000,Isalive_pi);
        }
        catch(Exception e)
        {

        }

        try
        {
            Thread.sleep(200);
            Intent ESMalarm_intent = new Intent(this, AlarmReceiver.class);
            ESMalarm_intent.setAction(SCHEDULE_ALARM);

            PendingIntent ESMalarm_pi = PendingIntent.getBroadcast(this, 300, ESMalarm_intent, PendingIntent.FLAG_CANCEL_CURRENT);

            Calendar cal = Calendar.getInstance(); //取得時間

            if(!exec) cal.add(cal.DAY_OF_MONTH,0);
            else cal.add(cal.DAY_OF_MONTH,0);

            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 5);
            AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);

            Log.d(TAG, "Set schedule alarm date: " + cal);
            am.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),24*60*60*1000,ESMalarm_pi);
        }
        catch(Exception e)
        {

        }
    }

    /*BroadcastReceiver CheckRunnableReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(CHECK_RUNNABLE_ACTION)) {

                Log.d(TAG, "[check runnable] going to check if the runnable is running");

                //  CSVHelper.storeToCSV(CSVHelper.CSV_RUNNABLE_CHECK, "going to check if the runnable is running");
                //  CSVHelper.storeToCSV(CSVHelper.CSV_RUNNABLE_CHECK, "is the runnable running ? " + isBackgroundRunnableRunning);

                if (!isBackgroundRunnableRunning) {

                    Log.d(TAG, "[check runnable] the runnable is not running, going to restart it.");

                    //    CSVHelper.storeToCSV(CSVHelper.CSV_RUNNABLE_CHECK, "the runnable is not running, going to restart it");

                    updateNotificationAndStreamManagerThread();

                    Log.d(TAG, "[check runnable] the runnable is restarted.");

                    //    CSVHelper.storeToCSV(CSVHelper.CSV_RUNNABLE_CHECK, "the runnable is restarted");
                }

                PendingIntent pi = PendingIntent.getBroadcast(MyBackgroundService.this, 50, new Intent(CHECK_RUNNABLE_ACTION), 0);

                AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    alarm.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            System.currentTimeMillis() + Constants.PROMPT_SERVICE_REPEAT_MILLISECONDS,
                            pi);
                } else {

                    alarm.set(
                            AlarmManager.RTC_WAKEUP,
                            System.currentTimeMillis() + Constants.PROMPT_SERVICE_REPEAT_MILLISECONDS,
                            pi
                    );
                }


            }
            //   Log.d("BootCompleteReceiver","In Background CheckRunnableReceiver");
        }
    };*/

    private void registerConnectivityNetworkMonitorForAPI21AndUp() {

        Log.d("BootCompleteReceiver", "In Background registerConnectivityNetworkMonitorForAPI21AndUp");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //loading the result page again
                //loadPages();
            }
        };
        Log.d(TAG, "register networkstate checker");
        registerReceiver(broadcastReceiver, new IntentFilter(DATA_SAVED_BROADCAST));
        NetworkRequest.Builder builder = new NetworkRequest.Builder();

        connectivityManager.registerNetworkCallback(
                builder.build(),
                new ConnectivityManager.NetworkCallback() {

                    @Override
                    public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                        sendBroadcast(
                                getConnectivityIntent("onCapabilitiesChanged : " + networkCapabilities.toString())
                        );
                    }
                }
        );
    }

    private Intent getConnectivityIntent(String message) {

        Intent intent = new Intent();

        intent.setAction(Constants.ACTION_CONNECTIVITY_CHANGE);

        intent.putExtra("message", message);

        Log.d("BootCompleteReceiver", "In Background getConnectivityIntent");
        return intent;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        CSVHelper.storeToCSV("TestService.csv", "Background service onDestroy");
        Log.d(TAG, "onDestroy");
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.cancel(1);
        mNotificationManager.cancel(2);
        mNotificationManager.cancel(3);
        mNotificationManager.cancel(9);
        //mScheduledFuture.cancel(true);
        //mScheduledPhoneState.cancel(true);
        //mScheduledFutureIsAlive.cancel(true);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mPhoneStateChecker);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(shutdownReceiver);
        //LocalBroadcastManager.getInstance(this).unregisterReceiver(CheckRunnableReceiver);
        sendBroadcastToStartService();
    }
    private void sendBroadcastToStartService(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            sendBroadcast(new Intent(this, RestartReceiver.class).setAction(Constants.CHECK_SERVICE_ACTION));
        } else {
            Intent checkServiceIntent = new Intent(Constants.CHECK_SERVICE_ACTION);
            sendBroadcast(checkServiceIntent);
        }
        Log.d("BootCompleteReceiver","In Background sendBroadcastToStartService");
    }

    private void StartForeground()
    {
        Log.d(TAG,"Foreground Service start");
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
    }
}

