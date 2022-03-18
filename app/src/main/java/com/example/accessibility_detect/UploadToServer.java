package com.example.accessibility_detect;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import labelingStudy.nctu.minuku.Utilities.CSVHelper;
import labelingStudy.nctu.minuku.config.Constants;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.accessibility_detect.AlarmReceiver.GALLERYID;
import static com.example.accessibility_detect.AlarmReceiver.REMINDER_ID;
import static com.example.accessibility_detect.NotificationHelper.RemindBuilder;
import static com.example.accessibility_detect.NotificationHelper.mBuilder;
import static com.example.accessibility_detect.NotificationHelper.manager;
import static com.example.accessibility_detect.Utils.DATE_FORMAT_NOW_HOUR_MIN;
import static labelingStudy.nctu.minuku.Utilities.CSVHelper.CSV_Img;
import static labelingStudy.nctu.minuku.Utilities.CSVHelper.CSV_News;
import static labelingStudy.nctu.minuku.config.Constants.ALLIMG;
import static labelingStudy.nctu.minuku.config.Constants.CONTACT_FILE;
import static labelingStudy.nctu.minuku.config.Constants.PICTURE_DIRECTORY_PATH;
import static labelingStudy.nctu.minuku.config.Constants.REMINDER_TEXT;
import static labelingStudy.nctu.minuku.config.Constants.REMINDER_TITLE;
import static labelingStudy.nctu.minuku.config.Constants.URL_CHECK_IMG;
import static labelingStudy.nctu.minuku.config.Constants.URL_SAVE_CSV;
import static labelingStudy.nctu.minuku.config.Constants.URL_SAVE_IMG;
import static labelingStudy.nctu.minuku.config.Constants.URL_SAVE_TXT;
import static labelingStudy.nctu.minuku.config.SharedVariables.PHONE_STATE;
import static labelingStudy.nctu.minuku.config.SharedVariables.dumpData_finish;
import static labelingStudy.nctu.minuku.config.SharedVariables.dumpData_interval;
import static labelingStudy.nctu.minuku.config.SharedVariables.dumpData_response_number;
import static labelingStudy.nctu.minuku.config.SharedVariables.upload_btn;

//import io.fabric.sdk.android.Fabric;

public class UploadToServer extends Activity {
    private Runnable runnable;
    private boolean uploadsuccess = true;
    private int success_count = 0;
    private int total_upload = 0;
    private static int FOREID = 3;
    private static final int REMIND_ID = 2;
    private String UID = "";
    private String returnImgaeName = "0";
//    private String date ;
    private String TAG = "UploadToServer";
    NotificationManager success_manager;
    public static final String UPLOAD_SUCCESS = "UploadSuccessChannel";
    public static final int SUCCESS_ID = 9;
    private long[] vibrate_effect = {100, 200, 300, 300, 500};
    private SharedPreferences pref;
    private File root_directory;
    private ConnectivityManager mConnectivityManager;
    private NetworkInfo mNetworkInfo;
    private JSONObject jObject;
    private Button upload_button;
    private CountDownLatch latch;
    private boolean txt_exist = false;
    private File txt_file;

    protected void onCreate(final Bundle savedInstanceState) {
        CSVHelper.storeToCSV(CSV_Img, "Oncreate");
        super.onCreate(savedInstanceState);
//        Fabric.with(this, new Crashlytics());
        pref = getSharedPreferences("test",MODE_PRIVATE);
        success_count = 0;
        total_upload = 0;
        mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        if(mNetworkInfo != null)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                new CheckFileAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            else
                new CheckFileAsync().execute();
        }
        else
        {
            Toast.makeText(getApplicationContext(),
                    "請連接網路後再上傳一次",
                    Toast.LENGTH_LONG).show();
        }
        this.finish();
    }

    private class CheckFileAsync extends AsyncTask<Void, Integer, Integer> {
        List<String> root = new ArrayList<String>();
        List<String> image = new ArrayList<String>();
        boolean exist = false;
        JSONObject objMainList = new JSONObject();
        JSONArray arrForUser = new JSONArray();
        OutputStream os = null;
        InputStream is = null;

        protected Integer doInBackground(Void... params) {
            CSVHelper.storeToCSV(CSV_Img, "check img doInBackground");
            //上船背景資料

            if(exist) {
                try {
                    objMainList.put(UID, arrForUser);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //讀所有還在手機裡面的照片
                for (int i = 0; i < root.size(); i++) {
                    image = getFilesAllName(root.get(i));
//                    CSVHelper.storeToCSV(CSV_Img, "img file: " + root.get(i));
                    // 可優化: 在這裡判斷這個資料夾是不是今天
                    if(image != null) {
                        Log.d(TAG, "img file size: " + image.size());
//                        CSVHelper.storeToCSV(CSV_Img, "img file size: " + image.size());
                        for (int j = 0; j < image.size(); j++) {
                            if (image.get(j).substring(image.get(j).length() - 3).equals("jpg") && !image.get(j).contains("trashed")) {
                                String[] temp = image.get(j).split("/");
                                try {
                                    arrForUser.put(temp[temp.length - 2] + "-" + temp[temp.length - 1]);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    try {
                        objMainList.put(UID, arrForUser);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Log.d(TAG, objMainList.toString());

//                CSVHelper.storeToCSV(CSV_Img, "connect to flask server");
                //connect flask
                HttpURLConnection conn = null;
                try {
                    //constants
                    URL url = new URL(URL_CHECK_IMG + UID);
                    String message = objMainList.toString();

                    conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(300000 /*milliseconds*/);
                    conn.setConnectTimeout(20000 /* milliseconds */);
                    conn.setRequestMethod("POST");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setFixedLengthStreamingMode(message.getBytes().length);

                    //make some HTTP header nicety
                    conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                    conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
                    conn.setRequestProperty("Connection", "keep-alive");

                    //open
                    conn.connect();

                    //setup send
                    os = new BufferedOutputStream(conn.getOutputStream());
                    os.write(message.getBytes());
                    //clean up
                    os.flush();

                    //do somehting with response
                    is = conn.getInputStream();
                    int ch;
                    StringBuffer b = new StringBuffer();
                    while ((ch = is.read()) != -1) {
                        b.append((char) ch);
                    }
                    jObject = new JSONObject(b.toString());
//                    if(jObject.getString("NotInDB") != null){
//                        CSVHelper.storeToCSV(CSV_Img, "NotInDB is not null");
//                    }
//                    if(jObject.getString("InDB") != null){
//                        CSVHelper.storeToCSV(CSV_Img, "InDB is not null");
//                    }
//                    CSVHelper.storeToCSV(CSV_Img, "jObject: " + jObject.toString());
                    Log.d(TAG, "jObject:" + jObject.toString());//json object: {"NotInDB":[...], "InDB":[...]}
                }  catch (JSONException e){
//                    CSVHelper.storeToCSV(CSV_Img, "JSONException");
                    e.printStackTrace();
                } catch (java.net.SocketTimeoutException e){
                    CSVHelper.storeToCSV(CSV_Img, "SocketTimeoutException");
                } catch (IOException e) {
                    CSVHelper.storeToCSV(CSV_Img, "IOException");
                    e.printStackTrace();
                }
                finally{
                    conn.disconnect();
                }
            }
            Log.d(TAG, "Finish Check File doInBackground");
            return null;
        }

        protected void onPreExecute() {
            super.onPreExecute();

            Intent phonestate_intent = new Intent(getApplicationContext(), AlarmReceiver.class);
            phonestate_intent.setAction(PHONE_STATE);
            sendBroadcast(phonestate_intent);
            Log.d(TAG, "Phone State Checker start!!!");

            CSVHelper.storeToCSV(CSV_Img, "check img onPreExecute");

            try {
                upload_button = MainActivity.upload_button;
                upload_btn = false;
                upload_button.setEnabled(upload_btn);
            }catch(Exception e){
                Log.d(TAG, "Cannot find upload button");
            }

            Log.d(TAG, "Check File onPreExecute");
            root_directory = new File(Environment.getExternalStorageDirectory().getPath() + PICTURE_DIRECTORY_PATH);
            if(root_directory.exists())
            {
                root = getFilesAllName(root_directory.toString());
                UID = pref.getString("UserID", Constants.DEVICE_ID);
                exist = true;
            }
            else
            {
                exist = false;
            }

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancel(REMIND_ID);

            //RemindBuilder.
            mBuilder.setProgress(0, 0, true);
            mBuilder.setContentTitle("News Consumption 正在檢查資料");
            mBuilder.setContentText("檢查中...");
            mBuilder.setOnlyAlertOnce(true);
            manager.notify(FOREID, mBuilder.build());

//            RemindBuilder.mActions.clear();
//            RemindBuilder.setContentTitle("News Consumption 正在上傳資料");
//            RemindBuilder.setContentText("請耐心等候.....");
//            RemindBuilder.setDefaults(NotificationManager.IMPORTANCE_MIN);
//            Intent GalleryIntent = UploadToServer.this.getPackageManager().getLaunchIntentForPackage("com.google.android.apps.photos");
//            if(GalleryIntent != null) {
//                GalleryIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//            }
//            PendingIntent Gallerypending = PendingIntent.getActivity(UploadToServer.this, GALLERYID, GalleryIntent, 0);
//
//            //Button
//            NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.mipmap.ic_launcher, "相簿", Gallerypending).build();
//            RemindBuilder.addAction(action);
//            manager.notify(REMIND_ID, RemindBuilder.build());
        }

        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            // Removes the progress bar
            CSVHelper.storeToCSV(CSV_Img, "check img onPostExecute");
            mBuilder.setContentTitle("News Consumption 上傳準備中, 請稍後");
            mBuilder.setContentText("請耐心等待，謝謝您的合作");
            mBuilder.setOnlyAlertOnce(true);
            manager.notify(FOREID, mBuilder.build());
            try
            {
                Thread.sleep(500);
                is.close();
                os.close();
            }
            catch (Exception e)
            {

            }
            /*String id = getSharedPreferences("test", MODE_PRIVATE)
                    .getString("UserID", "");
            mBuilder.setProgress(0, 0, false);
            mBuilder.setContentTitle("News Consumption 正在背景執行");
            mBuilder.setContentText("您的ID是: " + id);
            manager.notify(FOREID, mBuilder.build());*/

            new UploadFileAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        protected void onProgressUpdate(Integer... values) {
            // Update progress
            super.onProgressUpdate(values);
        }
    }

    private class UploadFileAsync extends AsyncTask<Void, Integer, Integer> {
        int arr_size ;

        List<String> root = new ArrayList<String>();
        List<String> image = new ArrayList<String>();
        List<String> fileall = new ArrayList<String>();
        List<String> imgNotInDB = new ArrayList<String>();
        List<String> imgInDB = new ArrayList<String>();
        boolean upload_exist = false;
        boolean delete_exist = false;
        boolean lastpart = false;
        int SuccessNum = 0;

        protected Integer doInBackground(Void... params) {
            CSVHelper.storeToCSV(CSV_Img, "upload img onInBackground");
            root_directory = new File(Environment.getExternalStorageDirectory().getPath() + PICTURE_DIRECTORY_PATH);
            publishProgress(0);
            if(root_directory.exists()) {//船CSV黨
                try {
                    String nowDate = Utils.getTimeString(Utils.DATE_FORMAT_NOW_DAY);
                    root = getFilesAllName(root_directory.toString());
                    for (int i = 0; i < root.size(); i++) {
                        String[] root_split = root.get(i).split("/");
                        Log.d(TAG, "URL CSV: " + root_split[root_split.length - 1] + " " + nowDate);
//                        CSVHelper.storeToCSV(CSV_Img, "find file: " + root_split[root_split.length - 1] + " " + nowDate);
                        if(!root_split[root_split.length - 1].equals(nowDate)) {
                            fileall = getFilesAllName(root.get(i));
                            if(fileall != null) {
//                                CSVHelper.storeToCSV(CSV_Img, "file size: " + fileall.size());
                                for (int j = 0; j < fileall.size(); j++) {
                                    if (fileall.get(j).substring(fileall.get(j).length() - 3).equals("csv")) {
                                        CSVHelper.storeToCSV(CSV_Img, "send NewsUrl.csv");
                                        String[] temp = fileall.get(j).split("/");
                                        String date = temp[temp.length - 2];
                                        String postUrl = URL_SAVE_CSV;
                                        File f = new File(fileall.get(j));
                                        RequestBody postBodyImage = new MultipartBody.Builder()
                                                .setType(MultipartBody.FORM)
                                                .addFormDataPart("UrlCsv", CSV_News, RequestBody.create(MediaType.parse("text/csv"), f))
                                                .addFormDataPart("date", date)
                                                .addFormDataPart("userid", UID)
                                                .build();
                                        postTxtRequest(postUrl, postBodyImage, f);
                                    } else if (fileall.get(j).substring(fileall.get(j).length() - 3).equals("txt")) {
                                        CSVHelper.storeToCSV(CSV_Img, "send AllImg.txt");
                                        String[] temp = fileall.get(j).split("/");
                                        String date = temp[temp.length - 2];
                                        String postUrl = URL_SAVE_TXT;
                                        File f = new File(fileall.get(j));
                                        RequestBody postBodyImage = new MultipartBody.Builder()
                                                .setType(MultipartBody.FORM)
                                                .addFormDataPart("Txt", ALLIMG, RequestBody.create(MediaType.parse("text/plain"), f))
                                                .addFormDataPart("date", date)
                                                .addFormDataPart("userid", UID)
                                                .build();
                                        postTxtRequest(postUrl, postBodyImage, f);
                                    }
                                }
                            }
                        }
                    }
                    CSVHelper.storeToCSV(CSV_Img, "upload csv file success");
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                    uploadsuccess = false;
//                    return null;
                }
            }

            String nowDate = Utils.getTimeString(Utils.DATE_FORMAT_NOW_DAY);
//            dumpData_finish = false;
            double time_now = System.currentTimeMillis();
            while(!dumpData_finish){
                if(System.currentTimeMillis() - time_now > 60*60*1000){
                    break;
                }
                try{
                    Thread.sleep(500);
                }
                catch(Exception e){
                    e.printStackTrace();
                }
                Log.d(TAG, "In while loop, dumpData_response_number: " + dumpData_response_number);
                publishProgress(dumpData_response_number * 10);
            }
            CSVHelper.storeToCSV(CSV_Img,"Start upload img");

            int progress = 0;
            if(upload_exist)//上傳還不在database的照片
            {
                int i;
                Bitmap bitmap = null;
                //MultipartBody.Builder multipartBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
//                CSVHelper.storeToCSV(CSV_Img, "upload image");
                String postUrl = URL_SAVE_IMG;
//                CSVHelper.storeToCSV(CSV_Img, "imgNotInDB size: " + imgNotInDB.size());
                for (i = 0; i < imgNotInDB.size(); i++) { //考慮會不會有upload在ImgNotInDB裡面
                    try {
//                        CSVHelper.storeToCSV(CSV_Img,  "Upload img: " + imgNotInDB.get(i));
                        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_NOW_HOUR_MIN);
                        String[] imgName_split = imgNotInDB.get(i).split("-");
                        if (!imgNotInDB.get(i).contains("trashed")) {
                            if (!nowDate.equals(imgName_split[0] + "-" + imgName_split[1] + "-" + imgName_split[2])) {
                                String imgDateFormat = "";
                                String dir = imgNotInDB.get(i).substring(0, 10);
                                String img = "";// 10/7
//                            String[] imgName_split = imgNotInDB.get(i).split("-");
                                String session = "";
                                String trigger = "";
                                String ImagePath;

                                if (imgNotInDB.get(i).contains("Upload")) {
                                    imgDateFormat = imgName_split[0] + "-" + imgName_split[1] + "-" + imgName_split[2] + "-" +
                                            imgName_split[4] + "-" + imgName_split[5] + "-" + imgName_split[6]; // 9/23
                                    img = imgNotInDB.get(i).substring(11, 26);// 10/7
                                    session = imgName_split[7];
                                    Log.d(TAG, "upload img name: " + imgNotInDB.get(i));
                                    if (imgNotInDB.get(i).contains("crop")) {
                                        trigger = imgName_split[10].split("\\.")[0];// 9/23
                                        ImagePath = Environment.getExternalStorageDirectory().getPath() + PICTURE_DIRECTORY_PATH + dir + "/" + img + "-" + session + "-"
                                                + "ESM" + "-" + "crop" + "-" + trigger + ".jpg";
                                    } else if (imgNotInDB.get(i).contains("ESM")) {
                                        trigger = imgName_split[9].split("\\.")[0];// 9/23
                                        ImagePath = Environment.getExternalStorageDirectory().getPath() + PICTURE_DIRECTORY_PATH + dir + "/" + img + "-" + session + "-"
                                                + "ESM" + "-" + trigger + ".jpg";
                                    } else {
                                        trigger = imgName_split[8].split("\\.")[0];// 9/23
                                        ImagePath = Environment.getExternalStorageDirectory().getPath() + PICTURE_DIRECTORY_PATH + dir + "/" + img + "-" + session + "-"
                                                + trigger + ".jpg";
                                    }
                                }
                                else {
                                    imgDateFormat = imgName_split[0] + "-" + imgName_split[1] + "-" + imgName_split[2] + "-" +
                                            imgName_split[3] + "-" + imgName_split[4] + "-" + imgName_split[5]; // 9/23
                                    img = imgNotInDB.get(i).substring(11, 19);// 10/7
                                    session = imgName_split[6];
                                    if (imgNotInDB.get(i).contains("crop")) {
                                        trigger = imgName_split[9].split("\\.")[0];// 9/23
                                        ImagePath = Environment.getExternalStorageDirectory().getPath() + PICTURE_DIRECTORY_PATH + dir + "/" + img + "-" + session + "-"
                                                + "ESM" + "-" + "crop" + "-" + trigger + ".jpg";
                                    } else if (imgNotInDB.get(i).contains("ESM")) {
                                        trigger = imgName_split[8].split("\\.")[0];// 9/23
                                        ImagePath = Environment.getExternalStorageDirectory().getPath() + PICTURE_DIRECTORY_PATH + dir + "/" + img + "-" + session + "-"
                                                + "ESM" + "-" + trigger + ".jpg";
                                    } else {
                                        trigger = imgName_split[7].split("\\.")[0];// 9/23
                                        ImagePath = Environment.getExternalStorageDirectory().getPath() + PICTURE_DIRECTORY_PATH + dir + "/" + img + "-" + session + "-"
                                                + trigger + ".jpg";
                                    }
                                }

//                                CSVHelper.storeToCSV(CSV_Img,  "parse img success: " + imgNotInDB.get(i));
                                Log.d(TAG, "session: " + session);

                                Log.d(TAG, "trigger: " + trigger);
                                Log.d(TAG, "Need upload: " + imgDateFormat);
                                Date date = null;
                                try {
                                    date = dateFormat.parse(imgDateFormat);
                                } catch (ParseException e) {
                                    e.printStackTrace();
//                                    CSVHelper.storeToCSV(CSV_Img, "Date parse error: " + imgDateFormat);
//                                uploadsuccess = false;
//                                return null;
                                }
                                long timestamp = 0L;

                                if (date != null) {
                                    timestamp = date.getTime();
                                }
//                        Log.d(TAG, "image name " + img);

                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                BitmapFactory.Options options = new BitmapFactory.Options();
                                options.inPreferredConfig = Bitmap.Config.RGB_565;

                                // Read BitMap by file path
                                bitmap = BitmapFactory.decodeFile(ImagePath, options);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                                try {
                                    byte[] byteArray = stream.toByteArray();
//                                    CSVHelper.storeToCSV(CSV_Img, "upload: " + imgNotInDB.get(i));
                                    MultipartBody.Builder multipartBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
//                            multipartBody.addFormDataPart("image" + i, imgNotInDB.get(i), RequestBody.create(MediaType.parse("image/*jpg"), byteArray))
//                                    .addFormDataPart("filename" + i, imgNotInDB.get(i))
//                                    .addFormDataPart("timestamp" + i, String.valueOf(timestamp))
//                                    .addFormDataPart("date" + i, dir)
//                                    .addFormDataPart("time" + i, img)
//                                    .addFormDataPart("trigger" + i, trigger);
                                    total_upload++;

                                    multipartBody.addFormDataPart("image", imgNotInDB.get(i), RequestBody.create(MediaType.parse("image/*jpg"), byteArray))
                                            .addFormDataPart("filename", imgNotInDB.get(i))
                                            .addFormDataPart("timestamp", String.valueOf(timestamp))
                                            .addFormDataPart("date", dir);
//                                        .addFormDataPart("time", img)
//                                        .addFormDataPart("trigger", trigger)
//                                        .addFormDataPart("session_id", session);
                                    RequestBody postBodyImage = multipartBody.addFormDataPart("userid", UID).build();
                                    postRequest(postUrl, postBodyImage);
                                    Thread.sleep(400);
                                } catch (OutOfMemoryError e) {
                                    break;
                                } finally {
                                    if (bitmap != null) {
                                        bitmap.recycle();
                                    }
                                }
                            }
                        }
                        //postRequest(postUrl, postBodyImage);
                    }
                    catch (Exception e) {
//                        CSVHelper.storeToCSV(CSV_Img, "Maybe parse error");
                        e.printStackTrace();
//                    uploadsuccess = false;
//                    return null;
                    }
                    if (i % 50 == 0) {
                        publishProgress(dumpData_interval * 10 + i);
//                            progress = i;
                    }
                }
                CSVHelper.storeToCSV("UploadToServer.csv", "postRequest ( " + imgNotInDB.size() + " )");
//                    RequestBody postBodyImage = multipartBody.addFormDataPart("userid", UID)
//                            .addFormDataPart("ImageCount", String.valueOf(imgNotInDB.size()))
//                            .build();
//                    postRequest(postUrl, postBodyImage);

//                    lastpart = true;
//                    try{
//                        Thread.sleep(100);
//                    }catch(Exception e){
//                        e.printStackTrace();
//                    }
//                    mBuilder.setProgress(0, 0, true);
//                    mBuilder.setContentTitle("News Consumption 上傳中.....");
//                    mBuilder.setContentText("請勿中斷網路連線");
//                    manager.notify(FOREID, mBuilder.build());
//
//                    latch.await();
                if(bitmap != null){
                    bitmap.recycle();
                }
            }
//            CSVHelper.storeToCSV("DumpData.csv", "dump data number: " + dumpDataResponse + " " + dumpDataNumber);
//            while(dumpDataResponse < dumpDataNumber ){
//                Log.d(TAG, "dump data number: " + dumpDataResponse + " " + dumpDataNumber);
//                try {
//                    Thread.sleep(500);
//                }
//                catch(Exception e){
//                    e.printStackTrace();
//                }
//                //if(dumpDataResponse跟最後紀錄的數字不一樣)publishProgress(dumpDataResponse)
//            }//等到dump data有道一定數量才代表完成
//
//            CSVHelper.storeToCSV("DumpData.csv", "Finish Upload File doInBackground " + progress);
            if(delete_exist)//刪除已經在database的照片
            {
                try {
                    CSVHelper.storeToCSV(CSV_Img, "delete file");
//                    if(imgInDB != null)CSVHelper.storeToCSV(CSV_Img, "imgInDB size: " + imgInDB.size());
                    for (int i = 0; i < imgInDB.size(); i++) {
                        String dir = imgInDB.get(i).substring(0, 10);//2020-06-04
                        String img = imgInDB.get(i).substring(11);//Upload-09-04-40-2-facebook.jpg // 10/7
                        String ImagePath = Environment.getExternalStorageDirectory().getPath() + PICTURE_DIRECTORY_PATH + dir + "/" + img;
                        String FilePath = Environment.getExternalStorageDirectory().getPath() + PICTURE_DIRECTORY_PATH + dir;
                        File imgfile = new File(ImagePath);
                        File dirfile = new File(FilePath);
                        boolean flag = NotTodayFile(dir);
                        if(flag) {
                            imgfile.delete();
                        }
                        File[] contents = dirfile.listFiles();
                        if(contents.length == 0 || contents == null)
                        {
//                            CSVHelper.storeToCSV(CSV_Img, "delete file path: " + FilePath);
                            dirfile.delete();
                        }
                    }
                }
                catch(Exception e)
                {
                    CSVHelper.storeToCSV(CSV_Img, e.getMessage());
                    e.printStackTrace();
                }
            }
            Log.d(TAG, "Finish Upload File doInBackground " + (imgNotInDB.size() - 1));
//            publishProgress(imgNotInDB.size() - 1);
//            publishProgress(progress + 50);

//            try{
//                Thread.sleep(3000);
//            }
//            catch(Exception e){
//                Log.d(TAG, "Cannot find upload button");
//            }

//            for(int i = 1; i <= 6; i++) {
//                try {
//                    Thread.sleep(60000);
//                    publishProgress(arr_size + i * 50);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }

            /*if(image_directory.exists())
            {
                for(int i = 0; i < image.size(); i++)
                {
                    String[] picture_name = image.get(i).split("/");
                    File file_temp = new File(image.get(i));
                    Uri file = Uri.fromFile(new File(image.get(i)));
                    StorageReference riversRef = storageReference.child(userID + "/" + date + "/" + picture_name[picture_name.length - 1]);
                    UploadTask uploadTask = riversRef.putFile(file);

                    // Register observers to listen for when the download is done or if it fails
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            Log.d(TAG, "Upload failure");
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc
                            Log.d(TAG, "Upload success: " + SuccessNum + " / " + arr_size);
                            publishProgress(SuccessNum);
                            file_temp.delete();
                            SuccessNum++;
                            if (image_directory.exists()) {
                                image_directory.delete();
                            }
                        }
                    });
                }
            }
            if(txt_directory.exists())
            {
                //UploadTxt(txt_directory);
            }*/
            /*if(dir_exist) {
                TitleAndWeb.clear();

                for (int i = 0; i < image.size(); i++) {
                    // Sets the progress indicator completion percentage
                    publishProgress(i);
                    Log.d(TAG, "Upload success: " + i + " / " + arr_size);
                    try {
                        // Sleep for 5 seconds
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        Log.d("TAG", "sleep failure");
                    }
                }
            }*/
            return null;
        }

        protected void onPreExecute() {
            super.onPreExecute();
            CSVHelper.storeToCSV(CSV_Img, "upload img onPreExecute");
            Log.d(TAG, "In onPreExecute, Dump data interval: " + dumpData_interval);

            mBuilder.setProgress(arr_size + dumpData_interval * 10 , 0, false);
            mBuilder.setContentTitle("News Consumption 上傳準備中, 請稍後");
            mBuilder.setContentText("請耐心等待，謝謝您的合作");
            mBuilder.setOnlyAlertOnce(true);
            manager.notify(FOREID, mBuilder.build());
            uploadsuccess = true;
            lastpart = false;
            createNotificationChannel(getApplicationContext());
//            determineDate();
            /*txt_file = new File(Environment.getExternalStorageDirectory().getPath() + PICTURE_DIRECTORY_PATH + date + ".txt");
            if(txt_file.exists())
            {
                txt_exist = true;
            }
            else
            {
                txt_exist = true;
            }*/

            try {
                JSONArray jArray = jObject.getJSONArray("NotInDB");

                if(jArray != null) {
                    CSVHelper.storeToCSV(CSV_Img, "NotInDB length is: " + jArray.length());
                    Log.d(TAG, "jArray: " + jArray.length());
                    if(jArray.length() > 0) {
                        upload_exist = true;
                    }
                    else{
                        upload_exist = false;
                    }
                    for (int i = 0; i < jArray.length(); i++) {
                        imgNotInDB.add(jArray.getString(i));
                    }
                    Collections.sort(imgNotInDB);
                    arr_size = imgNotInDB.size() - 1;
                }
                else
                {
                    arr_size = 0;
                    upload_exist = false;
                }

                jArray = jObject.getJSONArray("InDB");

                if(jArray != null) {
                    CSVHelper.storeToCSV(CSV_Img, "InDB length is: " + jArray.length());
                    Log.d(TAG, "jArray: " + jArray.length());
                    if(jArray.length() > 0) {
                        delete_exist = true;
                    }
                    else{
                        delete_exist = false;
                    }
                    for (int i = 0; i < jArray.length(); i++) {
                        imgInDB.add(jArray.getString(i));
                    }
                    Collections.sort(imgInDB);
                }
                else
                {
                    delete_exist = false;
                }
            }
            catch(JSONException e)
            {
                e.printStackTrace();
                CSVHelper.storeToCSV(CSV_Img, "Upload Async JsonException");
                uploadsuccess = false;
            }
            catch(NullPointerException e)
            {
                e.printStackTrace();
                CSVHelper.storeToCSV(CSV_Img, "Upload Async NullException");
                uploadsuccess = false;
            }

            /*determineDate();
            image_directory = new File(Environment.getExternalStorageDirectory().getPath() + PICTURE_DIRECTORY_PATH + date);
            txt_directory = new File(Environment.getExternalStorageDirectory().getPath() + PICTURE_DIRECTORY_PATH + date + ".txt");image = getFilesAllName(image_directory.toString());
            if(image_directory.exists())
            {
                dir_exist = true;
                arr_size = image.size() - 1;
            }
            else
            {
                dir_exist = false;
                arr_size = 0;
            }*/
        }

        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            CSVHelper.storeToCSV(CSV_Img, "upload img onPostExecute");

            // Removes the progress bar
            try{
                Thread.sleep(1000);
                upload_button.setEnabled(true);
            }
            catch(Exception e){
                Log.d(TAG, "Cannot find upload button");
            }


            String id = getSharedPreferences("test", MODE_PRIVATE)
                    .getString("UserID", "");

            mBuilder.setProgress(0, 0, false);
            mBuilder.setContentTitle("News Consumption 正在背景執行");
            mBuilder.setOnlyAlertOnce(true);
            mBuilder.setContentText("您的ID是: " + id);
            manager.notify(FOREID, mBuilder.build());

            String NotiContent = "";
            int success_threshold = total_upload / 2;
//            CSVHelper.storeToCSV(CSV_Img, "1/24 add: " + success_threshold + " " + success_count + " " + total_upload);
            if(uploadsuccess && (success_count >= success_threshold))
            {
                NotiContent = "上傳成功，謝謝您的配合";
                NotificationManager notificationManager = (NotificationManager) UploadToServer.this.getSystemService(NOTIFICATION_SERVICE);
                notificationManager.cancel(REMIND_ID);
                pref.edit().putBoolean("UploadClick", true).apply();
                Log.d(TAG, "PostExecute");
                AlarmReceiver.cancel_reminder(UploadToServer.this);
            }
            else{
//                RemindBuilder.mActions.clear();
                Intent UploadServerIntent = new Intent(UploadToServer.this, WiFireminder.class);
                PendingIntent UploadIntent = PendingIntent.getActivity(UploadToServer.this, 300, UploadServerIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                Intent GalleryIntent = UploadToServer.this.getPackageManager().getLaunchIntentForPackage("com.google.android.apps.photos");
                if(GalleryIntent != null) {
                    GalleryIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                }
                PendingIntent Gallerypending = PendingIntent.getActivity(UploadToServer.this, GALLERYID, GalleryIntent, 0);

                //Button
                NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.mipmap.ic_launcher, "相簿", Gallerypending).build();
                NotificationCompat.Action action2 = new NotificationCompat.Action.Builder(R.mipmap.ic_launcher, "上傳", UploadIntent).build();

                RemindBuilder = new NotificationCompat.Builder(UploadToServer.this, REMINDER_ID)
                        .setContentTitle(REMINDER_TITLE)
                        .setContentText(REMINDER_TEXT)
                        .setSmallIcon(R.drawable.ic_stat_name)
                        .setAutoCancel(true)
                        .setOngoing(true)
                        .setVibrate(vibrate_effect)
                        .setPriority(Notification.PRIORITY_MAX)
                        .addAction(action2)
                        .addAction(action);
                manager.notify(REMIND_ID, RemindBuilder.build());
                NotiContent = "上傳失敗，請檢查網路是否穩定";
            }

            //new cmd
            Notification notification = new NotificationCompat.Builder(getApplicationContext(), UPLOAD_SUCCESS) //設定通知要有那些屬性
                    .setContentTitle("News Consumption") // 通知的Title
                    .setContentText(NotiContent)                        //通知的內容
                    .setSmallIcon(R.drawable.ic_stat_name)            //通知的icon
                    .setOngoing(false)
                    .setPriority(Notification.PRIORITY_MAX)
                    .setVibrate(vibrate_effect)//震動模式
                    .build();

            success_manager.notify(SUCCESS_ID, notification);                  //發送通知

            upload_btn = true;
            upload_button.setEnabled(upload_btn);
        }

        protected void onProgressUpdate(Integer... values) {
            // Update progress
            super.onProgressUpdate(values);
            Log.d(TAG, "In onProgressUpdate, Dump data interval: " + dumpData_interval);
            Log.d(TAG, "In onProgressUpdate, value: " + values[0]);
            mBuilder.setProgress(arr_size + dumpData_interval * 10 , values[0], false);
            mBuilder.setContentTitle("正在上傳資料, 請勿關閉網路連線");
            mBuilder.setContentText("上傳中.....");
            mBuilder.setOnlyAlertOnce(true);
            manager.notify(FOREID, mBuilder.build());
        }
    }

    public boolean NotTodayFile(String file_date){ //2020-12-18
        String[] file_split = file_date.split("-");
        Calendar now_cal = Calendar.getInstance();
        Calendar file_cal = Calendar.getInstance();
        file_cal.set(Integer.parseInt(file_split[0]), Integer.parseInt(file_split[1]) - 1, Integer.parseInt(file_split[2])); // 11
        // 0 1 2 3 4 5 ... 11
        // 1 2 3 4 5 6 ... 12
        now_cal.add(Calendar.DAY_OF_MONTH, -1); // 2020-12-27
//        Log.d(TAG, "Now cal: " + now_cal);
//        Log.d(TAG, "file cal: " + file_cal);
        long aDayInMilliSecond = 60 * 60 * 24; //一天的ms
        long dayDiff = (now_cal.getTimeInMillis()/1000 - file_cal.getTimeInMillis()/1000)/aDayInMilliSecond; // 18 >> 0, 17 >> 1, 16 >> 2
//        CSVHelper.storeToCSV(CSV_Img,now_cal.getTimeInMillis()/1000 + " " + file_cal.getTimeInMillis()/1000);
//        CSVHelper.storeToCSV(CSV_Img, "now, file, diff= " + (now_cal.get(Calendar.MONTH) + 1) + "/" + now_cal.get(Calendar.DAY_OF_MONTH) + " , " + file_split[1] + "/" + file_split[2] + " , " + dayDiff);
//        Log.d(TAG, "Day diff: " + dayDiff);
        if(dayDiff > 1){
            return true;
        }
        else{
            return false;
        }
//        if(now_cal.get(Calendar.YEAR) != Integer.parseInt(file_split[0]) || now_cal.get(Calendar.MONTH) + 1 != Integer.parseInt(file_split[1])
//            || now_cal.get(Calendar.DAY_OF_MONTH) != Integer.parseInt(file_split[2])){
//            return true;
//        }
    }
    public static List<String> getFilesAllName(String path) {
        File file = new File(path);
        File[] files = file.listFiles();
        if (files == null) {
            Log.e("error", "Empty Directory");
            return null;
        }
        List<String> s = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            String[] file_split = files[i].getName().split("/");
            if(!file_split[file_split.length - 1].equals(CONTACT_FILE)) {
                s.add(files[i].getAbsolutePath());
            }
        }
        Collections.sort(s);
        return s;
    }

    void postRequest(String postUrl, RequestBody postBody) {
        Request request = new Request.Builder()
                .url(postUrl)
                .post(postBody)
                .build();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.MINUTES)
                .writeTimeout(5, TimeUnit.MINUTES)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Cancel the post on failure.
                call.cancel();
                e.printStackTrace();
//                CSVHelper.storeToCSV(CSV_Img, "img upload error: ");
//                latch.countDown();
                success_count++;
                Log.d(TAG, "Connect server failed");
//                uploadsuccess = false;
                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                /*runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "Connect server failed");
                    }
                });*/
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
//                Log.d(TAG, response.body().string());
                String responseName = response.body().string();
                Log.d(TAG, "Response name: " + responseName);
                if(!responseName.contains("Upload")){
                    if(!responseName.equals("0")) {
//                        CSVHelper.storeToCSV(CSV_Img, responseName + " upload success");
                        success_count++;
                        updateImgName(responseName);
                    }
                    else{
                        CSVHelper.storeToCSV(CSV_Img, responseName + " equal 0");
                    }
                }
                else{
                    success_count++;
//                    CSVHelper.storeToCSV(CSV_Img, responseName + " contain upload");
                }
//                returnImgaeName = pref.getString("ReturnImageName", "0");
////                Log.d(TAG, responseName + " v.s. " + returnImgaeName);
//                if(responseName.compareTo(returnImgaeName) > 0){
//                    pref.edit().putString("ReturnImageName", responseName).apply();
//                }
//                CSVHelper.storeToCSV("UploadToServer.csv", "Upload success");
//                latch.countDown();
                response.close();
                /*if(response.body().string().equals("UrlTxt upload success") && txt_exist)
                {
                    txt_file.delete();
                }*/
                /*runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    }
                });*/
            }
        });
    }

    void postTxtRequest(String postUrl, RequestBody postBody, File f) {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(postUrl)
                .post(postBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Cancel the post on failure.
                call.cancel();
                CSVHelper.storeToCSV(CSV_Img, "Txt request failed");
                Log.d(TAG, "Connect server failed");
                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                /*runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "Connect server failed");
                    }
                });*/
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                String responseStr = response.body().string();
                if(responseStr.equals("UrlCsv upload success"))
                {
//                    CSVHelper.storeToCSV(CSV_Img, "UrlCsv upload success");
                    f.delete();
                }
                if(responseStr.equals("Txt upload success"))
                {
//                    CSVHelper.storeToCSV(CSV_Img, "Txt upload success");
                    f.delete();
                }
                response.close();
                /*runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    }
                });*/
            }
        });
    }

    public void updateImgName(String responseName){
//        File root_directory = new File(Environment.getExternalStorageDirectory().getPath() + PICTURE_DIRECTORY_PATH);
        try {
            String dir = responseName.substring(0, 10); //2020-09-15
            String fileName = responseName.substring(11);
//
//            Log.d(TAG, "dir: " + dir);
//            Log.d(TAG, "file name: " + fileName);
            String FilePath = Environment.getExternalStorageDirectory().getPath() + PICTURE_DIRECTORY_PATH + dir;
            File imgfile = new File(FilePath, fileName);
            Log.d(TAG, "imgfile: " + imgfile);
            if(imgfile == null)Log.d(TAG, "imgfile is null");

            String newname = "Upload-" + fileName;
            Log.d(TAG, "new name: " + newname);
            File to = new File(FilePath, newname);
            boolean flag = imgfile.renameTo(to);
            Log.d(TAG, "Rename success: " + flag);
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
    /*public void UploadImage(File f)
    {
        List<String> image= getFilesAllName(f.toString());
        for(int i = 0; i < image.size(); i++)
        {
            String[] picture_name = image.get(i).split("/");
            File file_temp = new File(image.get(i));
            Uri file = Uri.fromFile(new File(image.get(i)));
            StorageReference riversRef = storageReference.child(userID + "/" + date + "/" + picture_name[picture_name.length - 1]);
            UploadTask uploadTask = riversRef.putFile(file);

            // Register observers to listen for when the download is done or if it fails
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    Log.d(TAG, "Upload failure");
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    Log.d(TAG, "Upload success");

                }
            });
            file_temp.delete();
        }
        if (f.exists()) {
            f.delete();
        }
    }*/
    /*public void UploadTxt(File f)
    {
        Uri txt_file = Uri.fromFile(new File(f.toString()));
        StorageReference riversRef = storageReference.child(userID + "/" + date + ".txt");
        UploadTask txt_uploadTask = riversRef.putFile(txt_file);
        // Register observers to listen for when the download is done or if it fails
        txt_uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Log.d(TAG, "Upload failure");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                Log.d(TAG, "Upload success");
            }
        });
        if (f.exists()) {
            f.delete();
        }
    }*/

//    public void determineDate()
//    {
//        date = getOldDate(0);
//    }
    public static String getOldDate(int distanceDay) {
        SimpleDateFormat dft = new SimpleDateFormat("yyyy-MM-dd");
        Date beginDate = new Date();
        Calendar date = Calendar.getInstance();
        date.setTime(beginDate);
        date.set(Calendar.DATE, date.get(Calendar.DATE) + distanceDay);
        Date endDate = null;
        try {
            endDate = dft.parse(dft.format(date.getTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dft.format(endDate);
    }

    private void createNotificationChannel(Context context) {
        Log.d(TAG, "createNotificationChannel");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    UPLOAD_SUCCESS,
                    "Upload Success Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            serviceChannel.setVibrationPattern(vibrate_effect);
            serviceChannel.enableVibration(true);
            success_manager =  (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            success_manager.createNotificationChannel(serviceChannel);
        }
        else{
            success_manager =  (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
    }
}

