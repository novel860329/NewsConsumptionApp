package labelingStudy.nctu.minuku.receiver;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Environment;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import labelingStudy.nctu.minuku.DBHelper.appDatabase;
import labelingStudy.nctu.minuku.Utilities.CSVHelper;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.service.NotificationListenService;

import static labelingStudy.nctu.minuku.config.Constants.VIDEO_DIRECTORY_PATH;
import static labelingStudy.nctu.minuku.config.SharedVariables.DELETENOTIALARM;
import static labelingStudy.nctu.minuku.config.SharedVariables.NSHasPulledDown;
import static labelingStudy.nctu.minuku.config.SharedVariables.NotiIdRandomMCNotiSurvey;
import static labelingStudy.nctu.minuku.config.SharedVariables.RANDOMALARMASREMINDER;
import static labelingStudy.nctu.minuku.config.SharedVariables.RESET;
import static labelingStudy.nctu.minuku.config.SharedVariables.SURVEYCREATEALARM;
import static labelingStudy.nctu.minuku.config.SharedVariables.canFillQuestionnaire;
import static labelingStudy.nctu.minuku.config.SharedVariables.crowdsource;
import static labelingStudy.nctu.minuku.config.SharedVariables.dayCount;
import static labelingStudy.nctu.minuku.config.SharedVariables.dayCountString;
import static labelingStudy.nctu.minuku.config.SharedVariables.extraForQ;
import static labelingStudy.nctu.minuku.config.SharedVariables.getReadableTime;
import static labelingStudy.nctu.minuku.config.SharedVariables.map;
import static labelingStudy.nctu.minuku.config.SharedVariables.pullcontent;
import static labelingStudy.nctu.minuku.config.SharedVariables.reminderSize;
import static labelingStudy.nctu.minuku.config.SharedVariables.requestCodeCancelSurvey;
import static labelingStudy.nctu.minuku.config.SharedVariables.requestCodeCreateSurvey;
import static labelingStudy.nctu.minuku.config.SharedVariables.requestCodeRandomReminderAlarm;
import static labelingStudy.nctu.minuku.config.SharedVariables.requestCodeRecordActionRecording;
import static labelingStudy.nctu.minuku.config.SharedVariables.requestCodeRecordActionStartRecord;
import static labelingStudy.nctu.minuku.config.SharedVariables.requestCodeRecordActionStopRecording;
import static labelingStudy.nctu.minuku.config.SharedVariables.requestCodeReminderActioncrowdsource;
import static labelingStudy.nctu.minuku.config.SharedVariables.requestCodeReminderActionlocalGuides;
import static labelingStudy.nctu.minuku.config.SharedVariables.requestCodeReminderActionskipContribution;
import static labelingStudy.nctu.minuku.config.SharedVariables.requestCodeSurveyNoti;
import static labelingStudy.nctu.minuku.config.SharedVariables.resetFire;
import static labelingStudy.nctu.minuku.config.SharedVariables.todayMCount;
import static labelingStudy.nctu.minuku.config.SharedVariables.visitedApp;
import static labelingStudy.nctu.minuku.service.NotificationListenService.haveSentMCNoti;

/**
 * Created by chiaenchiang on 20/11/2018.
 */

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class AlarmReceiver extends BroadcastReceiver {
    public String TAG  = "AlarmReceiver";
    appDatabase db;
    int  countDown;
    public NotificationListenService notificationListenService = new NotificationListenService();
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onReceive(Context context, Intent intent) {
        db = appDatabase.getDatabase(context);
        String action = intent.getAction();
        SharedPreferences  sharedPrefs = context.getSharedPreferences(Constants.sharedPrefString, context.MODE_PRIVATE);
       // Integer alarmNumber = intent.getIntExtra("alarmNumber",-1);
        Integer notiNumber = intent.getIntExtra("notiNumber",-1);
        String appName = intent.getStringExtra("appName");
         if(action.equals(RESET)){
             Log.d("AlarmHelper", "AlarmReceiver set reset ");
             todayMCount = 0;
             //todayNCount = 0;

//            pref.edit().putInt("todayMCount", 0).apply();
//            pref.edit().putInt("todayNCount", 0).apply();
//            int dayCount = pref.getInt("dayCount",0);
             dayCount+=1;
             sharedPrefs.edit().putInt(dayCountString,dayCount).commit();
//            pref.edit().putInt("dayCount", dayCount).apply();
             deleteAllSyncVideo();
             deleteAllSyncData();
            // 整點叫你
    //        AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
    //        am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
//            Toast.makeText(context,"整點歸零 delete all sync data",Toast.LENGTH_LONG).show();
//            JSONObject object = new JSONObject();
//
//             try {
//                 object.put("resetReceive", SharedVariables.getReadableTime(new Date().getTime()));
//             } catch (JSONException e) {
//                 e.printStackTrace();
//             }
//             CSVHelper.storeToCSV("CheckAlarm.csv",object.toString());
             resetFire = true;
             haveSentMCNoti.clear();
            // cancel all pendingIntent
             cancelAllPendingIntent(context);

             // 重新random

        }
//         else if(action.equals(SURVEYALARM)){
//             Log.d("AlarmHelper", "AlarmReceiver set survey ");
//             canSentNotiMC = true;
//             canSentNotiMCNoti = true;
//             canSentReminder = true;
//             NSHasPulledDown = false;
//             pullcontent.clear();
//
//
//             //Toast.makeText(context,"可送noti",Toast.LENGTH_LONG).show();
//            // JSONObject object = new JSONObject();
//
////             try {
////                 object.put("surveyReceive",SharedVariables.getReadableTime(new Date().getTime()));
////             } catch (JSONException e) {
////                 e.printStackTrace();
////             }
////             CSVHelper.storeToCSV("CheckAlarm.csv",object.toString());
//             if(alarmNumber%2==0){
//                 canSentNoti = true;
//             }
//             if(alarmNumber == 0){
//                // CSVHelper.storeToCSV("CheckAlarm.csv","survey1Fire set true");
//                 survey1Fire = true;
//             }else if(alarmNumber == 1){
//                // CSVHelper.storeToCSV("CheckAlarm.csv","survey2Fire set true");
//                 survey2Fire = true ;
//             }else if(alarmNumber == 2){
//                // CSVHelper.storeToCSV("CheckAlarm.csv","survey3Fire set true");
//                 survey3Fire = true;
//             }else if(alarmNumber == 3){
//                 survey4Fire = true;
//               //  CSVHelper.storeToCSV("CheckAlarm.csv","survey4Fire set true");
//             }else if(alarmNumber == 4){
//                 survey5Fire = true;
//               //  CSVHelper.storeToCSV("CheckAlarm.csv","survey5Fire set true");
//             }else if(alarmNumber == 5){
//                 survey6Fire = true;
//               //  CSVHelper.storeToCSV("CheckAlarm.csv","survey6Fire set true");
//             }else if(alarmNumber == 6){
//                 survey7Fire = true;
//              //   CSVHelper.storeToCSV("CheckAlarm.csv","survey7Fire set true");
//             }else if(alarmNumber == 7){
//                 survey8Fire = true;
//               //  CSVHelper.storeToCSV("CheckAlarm.csv","survey8Fire set true");
//             }else if(alarmNumber == 8){
//                 survey9Fire = true ;
//                // CSVHelper.storeToCSV("CheckAlarm.csv","survey8Fire set true");
//             }else if(alarmNumber == 9){
//                 survey10Fire = true;
//                 canSentNoti = false;
//                 canSentNotiMC = false;
//                 canSentNotiMCNoti = false;
//                 canSentReminder = false;
//             //    CSVHelper.storeToCSV("CheckAlarm.csv","survey8Fire set true");
//             }
//
//             if(alarmNumber>=0 && alarmNumber<9){
//                 createRandomAlarm(context,alarmNumber);
//             }
//        }
//        else if(action.equals(RANDOMSURVEYALARM)){
//             NSHasPulledDown = false;
//             pullcontent.clear();
//           // if(canSentNoti) {
//                extraForQ = notiTitleForRandom + " " + notiTextForRandom;
//                if(!notificationListenService.checkAnyNotiExist(context)) {
//                    haveSentMCNoti.clear();
//                    notificationListenService.createNotification(context,notiPackForRandom,notiPostedTimeForRandom,1, NotiIdRandomSurvey,QUESTIONNAIRE_TITLE_RANDOM_NOTI);
////                    if(notiPostedTimeForRandom!= Long.valueOf(0) && QUESTIONNAIRE_TITLE_RANDOM_NOTI != "")
////                       //s notificationListenService.createNotification(context,notiPackForRandom,notiPostedTimeForRandom,1, NotiIdRandomSurvey,QUESTIONNAIRE_TITLE_RANDOM_NOTI);
////                   // triggerNotifications(notiPackForRandom, notiPostedTimeForRandom, 1, context);
////                    CSVHelper.storeToCSV("randomAlarm.csv", "receive time" + getReadableTime(System.currentTimeMillis()));
////                    CSVHelper.storeToCSV("randomAlarm.csv", "receive content" + notiPackForRandom + " " + notiPostedTimeForRandom + " " + notiTitleForRandom + " " + notiTextForRandom);
////                    canSentNoti = false;
//                }
//
//           // }
//         }
        else if(action.equals(RANDOMALARMASREMINDER)){
           //  notificationListenService.createNotificationAsReminders(context);
//             if(canSentReminder ) {
                 NSHasPulledDown = false;
                 pullcontent.clear();
                 if(!(visitedApp.equals(map)||visitedApp.equals(crowdsource)) && notificationListenService.checkIfReminderNeeded()){
                     // 需要提醒，且並沒有正在執行群眾外包工作
                     notificationListenService.createNotificationAsReminders(context);
                 }
//                    canSentReminder = false;
//             }
         }
        else if (action.equals(DELETENOTIALARM)){
            cancelNotification(context,notiNumber);
             canFillQuestionnaire = false;
             //canSentReminder = false;
             //  看是否開著兩種 app
//             if(!(visitedApp.equals(map)||visitedApp.equals(crowdsource))) {
//                 // 存之前的資訊在sharepreference
//                 cancelNotification(context, notiNumber);
//             }

         }
         else if (action.equals(SURVEYCREATEALARM)){
             canFillQuestionnaire = true;
             Calendar rightNow = Calendar.getInstance();
             int hour = rightNow.get(Calendar.HOUR_OF_DAY);
             int minute = rightNow.get(Calendar.MINUTE);
             int target_hour = hour;
             int target_min ;
             if(minute - 10 >= 0){
                 target_min = minute - 10 ;
             }else{
                 target_hour -= 1 ;
                 target_min = minute - 10 + 60 ;
             }
             Calendar when = Calendar.getInstance();
             when.setTimeInMillis(System.currentTimeMillis());

             when.set(Calendar.HOUR_OF_DAY,target_hour);  // (8+0*2)8 (8+1*2)10 (8+2*2)12 (8+3*2)14 (8+4*2)16 (8+5*2)18 (8+6*2)10 (8+7*2)22
             when.set(Calendar.MINUTE, target_min);
             when.set(Calendar.SECOND, 0);
             when.set(Calendar.MILLISECOND, 0);

             notificationListenService.createNotification(context,appName,when.getTimeInMillis() , 0, NotiIdRandomMCNotiSurvey, extraForQ);
         }


    }

    public void  cancelNotification(Context context,int noti_id){

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        StatusBarNotification[] notifications =
                new StatusBarNotification[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            notifications = notificationManager.getActiveNotifications();
        }
        for (StatusBarNotification notification : notifications) {
            if (notification.getId() == noti_id) {
                notificationManager.cancel(noti_id);
                CSVHelper.storeToCSV("noti_cancel.csv","15 timesup : "+noti_id+" : "+getReadableTime(new Date().getTime()));
                // Do something.
                return;
            }
        }


    }
    // 90 - 120
    void cancelAllPendingIntent(Context context){
        int[] noActionRequestCode = {
                requestCodeSurveyNoti,requestCodeReminderActionlocalGuides,requestCodeReminderActioncrowdsource, requestCodeReminderActionskipContribution,
                requestCodeRecordActionStartRecord,requestCodeRecordActionRecording, requestCodeRecordActionStopRecording
        };
        cancelPendingIntent(context,requestCodeCancelSurvey,DELETENOTIALARM);
        cancelPendingIntent(context,requestCodeCreateSurvey,SURVEYCREATEALARM);
        for(int i = 0 ; i < reminderSize ; i++){
            cancelPendingIntent(context,requestCodeRandomReminderAlarm+i,RANDOMALARMASREMINDER);
        }

        for(int i = 0 ; i < noActionRequestCode.length ; i++ ){
            cancelPendingIntent(context,noActionRequestCode[i],"");
        }
    }

    void cancelPendingIntent(Context context, int requestCode, String action){
        Intent intent = new Intent(context, AlarmReceiver.class);
        if(!action.equals("")){
            intent.setAction(action);
        }
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingIntent);
        pendingIntent.cancel();
    }

//    public static void createRandomAlarm(Context context,int alarmNumber){
//        int minNum = 30;
//        int maxNum = 60;
//        int random = new Random().nextInt((maxNum - minNum) + 1) + minNum;
//        int random_for_reminder = new Random().nextInt((maxNum - minNum) + 1) + minNum;
//        int target_minute ;
//        int target_hour ;
//        int target_minute_for_reminder;
//        int target_hour_for_reminder;
//        if(min_range_1_5[alarmNumber]+random >= 60){
//            target_minute = min_range_1_5[alarmNumber]+random - 60;
//            target_hour = hour_range_1_5[alarmNumber]+1;
//            target_hour = target_hour>=24? target_hour-24 : target_hour;
//        }else{
//            target_minute = min_range_1_5[alarmNumber]+random;
//            target_hour = hour_range_1_5[alarmNumber];
//        }
//        if(min_range_1_5[alarmNumber]+random_for_reminder >= 60){
//            target_minute_for_reminder = min_range_1_5[alarmNumber]+random_for_reminder - 60;
//            target_hour_for_reminder = hour_range_1_5[alarmNumber]+1;
//            target_hour_for_reminder = target_hour_for_reminder>=24? target_hour_for_reminder-24 : target_hour_for_reminder;
//        }else{
//            target_minute_for_reminder = min_range_1_5[alarmNumber]+random_for_reminder;
//            target_hour_for_reminder = hour_range_1_5[alarmNumber];
//        }
//
//
//        Calendar currentTime = Calendar.getInstance();
//        Calendar when = Calendar.getInstance();
//        when.setTimeInMillis(System.currentTimeMillis());
//
//
//        when.set(Calendar.HOUR_OF_DAY,target_hour);  // (8+0*2)8 (8+1*2)10 (8+2*2)12 (8+3*2)14 (8+4*2)16 (8+5*2)18 (8+6*2)10 (8+7*2)22
//        when.set(Calendar.MINUTE, target_minute);
//        when.set(Calendar.SECOND, 0);
//        when.set(Calendar.MILLISECOND, 0);
//        CSVHelper.storeToCSV("randomAlarm.csv","random "+random);
//        CSVHelper.storeToCSV("randomAlarm.csv","now hour_range_1_5 "+ hour_range_1_5[alarmNumber]);
//        CSVHelper.storeToCSV("randomAlarm.csv","now min_range_1_5 "+ min_range_1_5[alarmNumber]);
//        CSVHelper.storeToCSV("randomAlarm.csv","new Random hour_range_1_5 "+(target_hour));
//        CSVHelper.storeToCSV("randomAlarm.csv","new Random min_range_1_5 "+(target_minute));
//
//        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        Intent intent = new Intent(context, AlarmReceiver.class);
//        intent.setAction(RANDOMSURVEYALARM);
//        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, requestCodeRandomSurveyAlarm,intent,PendingIntent.FLAG_UPDATE_CURRENT);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
//            am.setAlarmClock(new AlarmManager.AlarmClockInfo(when.getTimeInMillis(),alarmIntent),alarmIntent);
//        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
//            am.setExact(AlarmManager.RTC, when.getTimeInMillis(), alarmIntent);
//        else
//            am.set(AlarmManager.RTC, when.getTimeInMillis(), alarmIntent);
//
//
//        // for reminder
//        Calendar when_for_reminder = Calendar.getInstance();
//        when_for_reminder.setTimeInMillis(System.currentTimeMillis());
//
//        when_for_reminder.set(Calendar.HOUR_OF_DAY,target_hour_for_reminder);  // (8+0*2)8 (8+1*2)10 (8+2*2)12 (8+3*2)14 (8+4*2)16 (8+5*2)18 (8+6*2)10 (8+7*2)22
//        when_for_reminder.set(Calendar.MINUTE, target_minute_for_reminder);
//        when_for_reminder.set(Calendar.SECOND, 0);
//        when_for_reminder.set(Calendar.MILLISECOND, 0);
//
//        Intent intent_for_reminder = new Intent(context, AlarmReceiver.class);
//        intent_for_reminder.setAction(RANDOMALARMASREMINDER);
//        PendingIntent alarmIntent_for_reminder = PendingIntent.getBroadcast(context, requestCodeRandomReminderAlarm,intent_for_reminder,PendingIntent.FLAG_UPDATE_CURRENT);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
//            am.setAlarmClock(new AlarmManager.AlarmClockInfo(when_for_reminder.getTimeInMillis(),alarmIntent_for_reminder),alarmIntent_for_reminder);
//        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
//            am.setExact(AlarmManager.RTC, when_for_reminder.getTimeInMillis(), alarmIntent_for_reminder);
//        else
//            am.set(AlarmManager.RTC, when_for_reminder.getTimeInMillis(), alarmIntent_for_reminder);
//
//
//        nextTimeRadomAlarmNumber = alarmNumber +1;
//        nextTimeRadomAlarmNumber = nextTimeRadomAlarmNumber>=10? 0 : nextTimeRadomAlarmNumber;
//
//
//    }

    public void deleteAllSyncData(){
        db.accessibilityDataRecordDao().deleteSyncData(1);
        db.AppTimesDataRecordDao().deleteSyncData(1);
        db.transportationModeDataRecordDao().deleteSyncData(1);
        db.locationDataRecordDao().deleteSyncData(1);
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

        db.videoDataRecordDao().deleteSyncData(1);
    }
    public void deleteAllSyncVideo() {

        Cursor transCursor = db.videoDataRecordDao().getSyncVideoData(1);
        int rows = transCursor.getCount();

        if (rows != 0) {
            transCursor.moveToFirst();
            for (int i = 0; i < rows; i++) {
                String fileName = transCursor.getString(3);
                File file = new File(Environment.getExternalStorageDirectory()+VIDEO_DIRECTORY_PATH,
                        fileName);
                file.delete();
                boolean deleted = file.delete();
                if(deleted){
                    Log.d("AlarmHelper", "fileName : "+ fileName +"has been deleted");
                    CSVHelper.storeToCSV("FileDelete.csv","fileName : "+ fileName +"has been deleted");
                }else{
                    Log.d(TAG,"file: "+fileName+" has not been deleted");
                    CSVHelper.storeToCSV("FileDelete.csv","fileName : "+ fileName +"has not been deleted");
                }
                transCursor.moveToNext();
            }
        }
    }


}
