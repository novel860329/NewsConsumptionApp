package com.example.accessibility_detect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Calendar;

import labelingStudy.nctu.minuku.DBHelper.appDatabase;
import labelingStudy.nctu.minuku.Utilities.CSVHelper;
import labelingStudy.nctu.minuku.model.DataRecord.UserDataRecord;

import static android.content.Context.MODE_PRIVATE;

public class BootUpReceiver extends BroadcastReceiver {
    private SharedPreferences pref;
    private appDatabase db;
    UserDataRecord userRecord;
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
//        throw new UnsupportedOperationException("Not yet implemented");
        pref = context.getSharedPreferences("test", MODE_PRIVATE);
        db = appDatabase.getDatabase(context);
        userRecord = db.userDataRecordDao().getLastRecord();
        String intent_action = intent.getAction();
        CSVHelper.storeToCSV("AlarmCreate.csv", "intent_action: " + intent_action);
        /* 同一個接收者可以收多個不同行為的廣播，所以可以判斷收進來的行為為何，再做不同的動作 */
//        if (intent_action.equals("android.intent.action.ACTION_SHUTDOWN") ||
//                intent_action.equals("android.intent.action.QUICKBOOT_POWEROFF")) {
//            pref.edit().putInt("BootUpReceiver", 5).apply();
//            CSVHelper.storeToCSV("AlarmCreate.csv", "BootUpReceiver action shotdown" + pref.getInt("BootUpReceiver", 0));
//        }
        if (intent_action.equals("android.intent.action.BOOT_COMPLETED")) {
            /* 收到廣播後要做的事 */
            Calendar cal = Calendar.getInstance();
            int day = cal.get(Calendar.DAY_OF_MONTH); //開機日期
            int ShutDown_Day = pref.getInt("ShutDown_Day", 0); //關機日期

            CSVHelper.storeToCSV("AlarmCreate.csv", "BootUpReceiver boot complete " + pref.getInt("ShutDown_Day", 0));
            if(userRecord != null){
                if(day == ShutDown_Day) { //沒有跨天
                    db.userDataRecordDao().updateIsKilled(userRecord.get_id(), true);
                }
            }
//            userRecord.setIsKilled(true);
            Log.d("BootReceive", "is killed is true");
//            pref.edit().putBoolean("IsKilled", true).apply();
            context.startService(new Intent(context, MyBackgroundService.class));
        }
    }
}
