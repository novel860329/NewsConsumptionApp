package com.example.accessibility_detect;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

//import com.crashlytics.android.Crashlytics;

//import io.fabric.sdk.android.Fabric;

public class WiFireminder extends Activity {
    private String TAG = "WiFiremind";
    private boolean skipMessage;
    private AlertDialog.Builder alert;
    public CheckBox UseWiFi;
    private int option = 1;
    private static ConnectivityManager mConnectivityManager;

    protected void onCreate(final Bundle savedInstanceState) {
        Log.d(TAG, "Oncreate");
        super.onCreate(savedInstanceState);
//        Fabric.with(this, new Crashlytics());
        //myDialog(this);
        mConnectivityManager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);

        Dialogbox(this);
        if (!isCheckboxStateEnabled()) { //!isCheckboxStateEnabled() 12/16
            alert.show();
        }
        else {
            if(isWifiConnected()) {
                Intent newIntent = new Intent(this, UploadToServer.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.startActivity(newIntent);
            }else{
                Toast.makeText(getApplicationContext(),
                        "請連接Wifi後再上傳一次", Toast.LENGTH_LONG).show();
            }
            /*SharedPreferences settings = getSharedPreferences("showit", 0);
            String internet = settings.getString("Internet", "NA");

            if(internet.equals("Wifi"))
            {
                if(isWifiConnected()) {
                    Intent newIntent = new Intent(this, UploadToServer.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    this.startActivity(newIntent);
                }else{
                    Toast.makeText(getApplicationContext(),
                            "請連接Wifi", Toast.LENGTH_LONG).show();
                }
            }
            if(internet.equals("Mobile"))
            {
                Intent newIntent = new Intent(this, UploadToServer.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.startActivity(newIntent);
            }*/
            this.finish();
        }
        //
    }
    public void Dialogbox(Context context) {
        alert = new AlertDialog.Builder(context);
        alert.setOnCancelListener(dialog -> WiFireminder.this.finish());
        View checkboxLayout = View.inflate(context, R.layout.checkbox, null);
        UseWiFi = (CheckBox) checkboxLayout.findViewById(R.id.usewifi);
        alert.setView(checkboxLayout);
        alert.setTitle("上傳大量圖片");
        alert.setIcon(android.R.drawable.ic_dialog_alert);
        alert.setMessage("將上傳大量資料，如要避免支付超額數據用量費用，請只使用Wi-Fi上傳。");

        alert.setNegativeButton("使用Wifi上傳", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                boolean checkBoxResult = false;

                SharedPreferences settings = getSharedPreferences("showit", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("Internet", "Wifi");
                editor.apply();

                if (UseWiFi.isChecked())
                    checkBoxResult = true;

                if(checkBoxResult)
                {
                    Log.d(TAG, "checkbox is true and cancel");
                }
                else
                {
                    Log.d(TAG, "checkbox is false and cancel");
                }
                setCheckboxState(checkBoxResult);

                if(isWifiConnected())
                {
                    Log.d(TAG, "Wifi Connected in Dialog");
                    Intent newIntent = new Intent(context, UploadToServer.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(newIntent);
                }
                else
                {
                    Toast.makeText(getApplicationContext(),
                            "請連接Wifi後再上傳一次", Toast.LENGTH_LONG).show();
                }
                finish();
                //WiFireminder.this.finish();
            }
        });

        alert.setPositiveButton("使用任何網路上傳", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                boolean checkBoxResult = false;

                SharedPreferences settings = getSharedPreferences("showit", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("Internet", "Mobile");
                editor.apply();

                if (UseWiFi.isChecked())
                    checkBoxResult = true;

                //setCheckboxState(checkBoxResult);
                if(checkBoxResult)
                {
                    Log.d(TAG, "checkbox is true and continue");
                }
                else
                {
                    Log.d(TAG, "checkbox is false and continue");
                }
                setCheckboxState(checkBoxResult);
                Intent newIntent = new Intent(context, UploadToServer.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(newIntent);
                finish();
                //WiFireminder.this.finish();
                // 關閉對話框
            }
        });
    }
    public void setCheckboxState(boolean chk) {
        // 記錄勾選方塊是否被打勾
        SharedPreferences settings = getSharedPreferences("showit", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("skipMessage", chk); // 12/16
        editor.apply();
    }

    public boolean isCheckboxStateEnabled() {
        // 讀取勾選方塊是否被打勾,預設值是未打勾(fasle)
        SharedPreferences settings = getSharedPreferences("showit", 0);
        skipMessage = settings.getBoolean("skipMessage", false);

        return skipMessage;
    }

    public boolean isWifiConnected()
    {
        boolean connect = false;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Network[] networks = mConnectivityManager.getAllNetworks();

            for (Network network : networks) {
                if(network == null){
                    break;
                }
                NetworkCapabilities nc = mConnectivityManager.getNetworkCapabilities(network);
                if(nc!=null){
                    if(nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
                        connect = true;
                    else
                        connect = false;
                }
            }
        }
        Log.d(TAG, "Wifi connecte: " + connect);
        return connect;
    }

    private void myDialog(Context context)
    {
        View viewDialogBroadcast;
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();

        dialogBuilder.setTitle("上傳大量圖片")
                .setView(R.layout.wifi_reminder_dialog);

        viewDialogBroadcast = (LinearLayout) inflater.inflate(R.layout.wifi_reminder_dialog, null);

        final RadioGroup groupBroadcast = (RadioGroup) viewDialogBroadcast.findViewById(R.id.groupBroadcast);
        final RadioButton UseAny = (RadioButton) viewDialogBroadcast.findViewById(R.id.any);
        final RadioButton Wifi = (RadioButton) viewDialogBroadcast.findViewById(R.id.wifi);
        groupBroadcast.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId==UseAny.getId()){
                    Log.d(TAG, "This is 1");
                    option = 1;
                }
                else if (checkedId==Wifi.getId()) {
                    Log.d(TAG, "This is 2");
                    option = 2;
                }
            }
        });
        alert = new AlertDialog.Builder(this)
                .setView(viewDialogBroadcast)
                .setTitle("即將上傳大量圖片")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setNegativeButton("上傳", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(option == 1){
                            Intent newIntent = new Intent(context, UploadToServer.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(newIntent);
                        }
                        else{
                            if(isWifiConnected())
                            {
                                Log.d(TAG, "Wifi Connected in Dialog");
                                Intent newIntent = new Intent(context, UploadToServer.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(newIntent);
                            }
                            else
                            {
                                Toast.makeText(getApplicationContext(),
                                        "請連接Wifi", Toast.LENGTH_LONG).show();
                            }
                        }
                        finish();
                    }
                })
                .setPositiveButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        alert.setOnCancelListener(dialog -> WiFireminder.this.finish());
        alert.create().show();
    }
}
