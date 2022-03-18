package com.example.accessibility_detect;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

//import com.crashlytics.android.Crashlytics;
//
//import io.fabric.sdk.android.Fabric;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.service.NotificationListenService;

import static com.example.accessibility_detect.NotificationHelper.input;
import static com.example.accessibility_detect.NotificationHelper.mBuilder;
import static com.example.accessibility_detect.NotificationHelper.manager;

public class ForegroundService extends Service {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    private static String TAG = "Foreground";
    public static NotificationListenService notificationListenService = new NotificationListenService();
    private static int FOREID = 3;
    private static int UPLOADID = 100;
    public static int GALLERYID = 200;
    private static int ESM_REQUEST = 300;
    private static int REMIND_REQUEST = 400;
    private Notification notification;
    private SharedPreferences pref;

    @Override
    public void onCreate() {
        super.onCreate();
//        Fabric.with(this, new Crashlytics());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"Foreground Service on start command");

        try {
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            Constants.DEVICE_ID = tm.getDeviceId();
            if(Constants.DEVICE_ID  == null){
                Constants.DEVICE_ID = Settings.Secure.getString(
                        this.getContentResolver(),
                        Settings.Secure.ANDROID_ID);
            }
            pref = getSharedPreferences("test", MODE_PRIVATE);
            pref.edit()
                    .putString("UserID", Constants.DEVICE_ID)
                    .apply();
            Log.d(TAG, "userID = " + Constants.DEVICE_ID);
        }
        catch(SecurityException e)
        {
            e.printStackTrace();
        }
        input = Constants.DEVICE_ID;
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                FOREID, notificationIntent, 0);

//        Intent GalleryIntent = getApplicationContext().getPackageManager().getLaunchIntentForPackage("com.simplemobiletools.gallery");
//        if(GalleryIntent == null){
//            GalleryIntent = getApplicationContext().getPackageManager().getLaunchIntentForPackage("com.google.android.apps.photos");
//        }
//        else{
//            GalleryIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        }
        PendingIntent Gallerypending;
        Intent GalleryIntent = this.getPackageManager().getLaunchIntentForPackage("com.google.android.apps.photos");
        if(GalleryIntent != null) {
            GalleryIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            Gallerypending = PendingIntent.getActivity(this, GALLERYID, GalleryIntent, 0);

            Intent UploadServerIntent = new Intent(this, WiFireminder.class);
            PendingIntent UploadIntent = PendingIntent.getActivity(this,  UPLOADID, UploadServerIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            //Button
            NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.mipmap.ic_launcher, "相簿", Gallerypending).build();
            NotificationCompat.Action action2 = new NotificationCompat.Action.Builder(R.mipmap.ic_launcher, "上傳", UploadIntent).build();

            mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("News Consumption 正在背景執行")
                    .setContentText("您的ID是: " + Constants.DEVICE_ID)
                    .setSmallIcon(R.drawable.ic_stat_name)
                    .setPriority(Notification.PRIORITY_MAX)
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)                              //使用者滑不掉
                    .setAutoCancel(false)                           //點擊之後通知不消失
                    .setVibrate(new long[]{ 0 })
                    .addAction(action);
        }
        else{
            Intent UploadServerIntent = new Intent(this, WiFireminder.class);
            PendingIntent UploadIntent = PendingIntent.getActivity(this,  UPLOADID, UploadServerIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            //Button
            NotificationCompat.Action action2 = new NotificationCompat.Action.Builder(R.mipmap.ic_launcher, "上傳", UploadIntent).build();

            mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("News Consumption 正在背景執行")
                    .setContentText("您的ID是: " + Constants.DEVICE_ID)
                    .setSmallIcon(R.drawable.ic_stat_name)
                    .setPriority(Notification.PRIORITY_MAX)
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)                              //使用者滑不掉
                    .setAutoCancel(false)                           //點擊之後通知不消失
                    .setVibrate(new long[]{ 0 });
        }



        notification = mBuilder.build();

//        Notification notification = notificationListenService.getRecordingNotification(this,RECORDING_ONGOING_CONTENT, "Initial", false, null);

        startForeground(FOREID, notification);

        //do heavy work on a background thread


        //stopSelf();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        Log.d(TAG, "createNotificationChannel");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            serviceChannel.setVibrationPattern(new long[]{ 0 });
            serviceChannel.setSound(null, null);
            serviceChannel.enableVibration(true);

            manager =  (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(serviceChannel);
        }
        else{
            manager =  (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        }
    }

}
