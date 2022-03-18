package com.example.accessibility_detect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationClickReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //todo 跳转之前要处理的逻辑
        Log.d("Receiver", "Notification receiver");

        Intent newIntent = new Intent(context, UploadToServer.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(newIntent);
    }

    /*public static void collapseStatusBar(Context context){
        try
        {
            Object statusBarManager = context.getSystemService();
            Method collapse;
            if (Build.VERSION.SDK_INT <= 16) {
                collapse = statusBarManager.getClass().getMethod("collapse");
            }
            else
            {
                collapse = statusBarManager.getClass().getMethod("collapsePanels");
            }
            collapse.invoke(statusBarManager);
        }
        catch(Exception localException)
        {
            localException.printStackTrace();
        }
    }*/
}