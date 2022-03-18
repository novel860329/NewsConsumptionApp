package com.example.accessibility_detect;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import labelingStudy.nctu.minuku.DBHelper.appDatabase;
import labelingStudy.nctu.minuku.Utilities.CSVHelper;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.model.DataRecord.UserDataRecord;

import static android.content.Context.MODE_PRIVATE;

public class RestartReceiver extends BroadcastReceiver {

    private static final String TAG = "RestartReceiver";
    private SharedPreferences pref;
    private appDatabase db;
    UserDataRecord userRecord;

    @SuppressLint("LongLogTag")
    @Override
    public void onReceive(Context context, Intent intent) {
        pref = context.getSharedPreferences("test", MODE_PRIVATE);
        db = appDatabase.getDatabase(context);
        userRecord = db.userDataRecordDao().getLastRecord();

        if (intent.getAction().equals(Constants.CHECK_SERVICE_ACTION)) {

            Log.d(TAG, "the RestarterBroadcastReceiver is going to start the BackgroundService");
            CSVHelper.storeToCSV("AlarmCreate.csv", "RestartReceiver");

            if(userRecord != null){
                db.userDataRecordDao().updateIsKilled(userRecord.get_id(), true);
            }
//            pref.edit().putBoolean("IsKilled", true).apply();

            Intent intentToStartBackground = new Intent(context, MyBackgroundService.class);

            context.startService(intentToStartBackground);
        }
    }
}
