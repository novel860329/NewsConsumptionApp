package labelingStudy.nctu.minuku.receiver;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import labelingStudy.nctu.minuku.service.NotificationListenService;

import static labelingStudy.nctu.minuku.config.Constants.CANCEL_RECORD;
import static labelingStudy.nctu.minuku.config.Constants.SKIP_CONTRIBUTE;
import static labelingStudy.nctu.minuku.config.Constants.STOP_RECORD;
import static labelingStudy.nctu.minuku.config.Constants.STOP_RECORDING;
import static labelingStudy.nctu.minuku.config.SharedVariables.NotiIdRandomReminder;
import static labelingStudy.nctu.minuku.config.SharedVariables.NotiIdRecord;
import static labelingStudy.nctu.minuku.config.SharedVariables.haveDeletedRecordingNoti;

/**
 * Created by chiaenchiang on 28/04/2019.
 */

public class NotificationHandleReceiver extends BroadcastReceiver {
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @SuppressLint("LongLogTag")
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("Handler Receiver", "Receive action: " + intent.getAction());
        if(intent.getAction().equals(STOP_RECORD)){
            String appName = intent.getStringExtra("appName");
            Log.d("NotificationHandleReceiver", appName);
            Intent broadCastIntent = new Intent();
            broadCastIntent.putExtra("appName",appName);
            broadCastIntent.setAction(STOP_RECORDING);
            LocalBroadcastManager.getInstance(context).sendBroadcast(broadCastIntent);
        }else if(intent.getAction().equals(CANCEL_RECORD)){
            Intent broadCastIntent = new Intent();
            broadCastIntent.setAction(STOP_RECORDING);
            LocalBroadcastManager.getInstance(context).sendBroadcast(broadCastIntent);
            NotificationListenService.cancelNotification(context,NotiIdRecord);
            haveDeletedRecordingNoti = true;
        }else if(intent.getAction().equals(SKIP_CONTRIBUTE)){  // random contribute
            NotificationListenService.cancelNotification(context,NotiIdRandomReminder);
        }

    }
}
