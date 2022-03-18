package com.example.accessibility_detect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.CountDownTimer;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import labelingStudy.nctu.minuku.DBHelper.appDatabase;
import labelingStudy.nctu.minuku.Utilities.CSVHelper;
import labelingStudy.nctu.minuku.Utilities.ScheduleAndSampleManager;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.model.DataRecord.NewsDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.UserDataRecord;

import static labelingStudy.nctu.minuku.Utilities.CSVHelper.CSV_Dump;
import static labelingStudy.nctu.minuku.config.Constants.DATA_SYNCED_WITH_SERVER;
import static labelingStudy.nctu.minuku.config.Constants.MY_SOCKET_TIMEOUT_MS;
import static labelingStudy.nctu.minuku.config.Constants.URL_SAVE_DUMP;
import static labelingStudy.nctu.minuku.config.Constants.URL_SAVE_NEWS;
import static labelingStudy.nctu.minuku.config.Constants.URL_SAVE_USER;
import static labelingStudy.nctu.minuku.config.SharedVariables.ReadableTimeAddHour;
import static labelingStudy.nctu.minuku.config.SharedVariables.allMCount;
import static labelingStudy.nctu.minuku.config.SharedVariables.dumpData_finish;
import static labelingStudy.nctu.minuku.config.SharedVariables.dumpData_interval;
import static labelingStudy.nctu.minuku.config.SharedVariables.dumpData_response_number;
import static labelingStudy.nctu.minuku.config.SharedVariables.getReadableTimeLong;
import static labelingStudy.nctu.minuku.config.SharedVariables.todayMCount;
import static labelingStudy.nctu.minuku.config.SharedVariables.videoCount;

//import io.fabric.sdk.android.Fabric;

/**
 * Created by chiaenchiang on 23/11/2018.
 */

public class PhoneStateChecker extends BroadcastReceiver {
    private final String TAG = "UploadToServer";
    //context and database helper object
    private Context context;
    private appDatabase db;
    private  SharedPreferences sharedPrefs;
    Long lastSentHour = Long.valueOf(0);
    Long currentHour;
    Long nowSentHour;
    boolean first = true;

    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;

//        Fabric.with(this.context, new Crashlytics());
        db = appDatabase.getDatabase(context);
        Log.d(TAG,"on receive");
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        sharedPrefs = context.getSharedPreferences(Constants.sharedPrefString, context.MODE_PRIVATE);
        dumpData_finish = false;
        //if there is a network
        if (activeNetwork != null) {
            //if connected to wifi or mobile data plan
//            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
            if(isOnline(context)){
                Log.d(TAG,"ready to send dump data");
                //getting all the unsynced names
                if(db.isOpen()) {
                    Log.d(TAG,"DataBase open");
                    Log.d("UploadToServer", "Start sending dump data");
                    dumpData_response_number = 0;
                    dumpData_interval = 0;
//                    try {
//                        url = new URL(URL_SAVE_DUMP);
//                        if (url.getProtocol().toLowerCase().equals("https")) {
//                            Log.d("UploadToServer", "[postJSON] [using https]");
//                            trustAllHosts();
//                            HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
//                            https.setHostnameVerifier(DO_NOT_VERIFY);
//                            conn = https;
//                        } else {
//                            conn = (HttpURLConnection) url.openConnection();
//                        }
//
//                        //TODO testing to solve the SocketTimeoutException issue
//                        conn.setReadTimeout(MY_SOCKET_TIMEOUT_MS);
//                        conn.setConnectTimeout(MY_SOCKET_TIMEOUT_MS);
//
//                        conn.setRequestMethod("POST");
//                        conn.setDoInput(true);
//                        conn.setDoOutput(true);
//
//                        SSLContext sc;
//                        sc = SSLContext.getInstance("TLS");
//                        sc.init(null, null, new java.security.SecureRandom());
//
//                        //TODO might need to use long instead of int is for the larger size but restricted to the api level should over 19
////            conn.setFixedLengthStreamingMode(json.getBytes().length);
//                        conn.setRequestProperty("Content-Type", "application/json");
//                        conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
//                        conn.setRequestProperty("Connection", "keep-alive");
//                        conn.connect();
//                    }
//                    catch (java.net.SocketTimeoutException e) {
//                        Log.d("UploadToServer", "SocketTimeoutException EE", e);
//                        CSVHelper.storeToCSV(CSV_Dump, "SocketTimeoutException");
//                    }
//                    catch(Exception e){
//                        e.printStackTrace();
//                    }
                    sendToAWS task = new sendToAWS();
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    //sendingDumpData();
//                    sendingUserData();
                }
                else {
                    Log.d(TAG,"DataBase not open");
                    dumpData_finish = true;
                }
            }
            else{
                dumpData_finish = true;
            }
        }
        else{
            dumpData_finish = true;
        }

    }
    public boolean isOnline(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //should check null because in airplane mode it will be null
        return (netInfo != null && netInfo.isConnected());
    }

    /*
     * method taking two arguments
     * name that is to be saved and id of the name from SQLite
     * if the name is successfully sent
     * we will update the status as synced in SQLite
     * */
    URL url = null;
    HttpURLConnection conn = null;
    private void saveData(JSONObject multipleRows) {
//        StringRequest stringRequest = new StringRequest(Request.Method.POST, MainActivity.URL_SAVE_DUMP,
//                new ResponseResult.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        try {
//                            JSONObject obj = new JSONObject(response);
//                            if (!obj.getBoolean("error")) {
//                                //updating the status in sqlite
//                                db.accessibilityDataRecordDao().updateDataStatus(creationTime, MainActivity.DATA_SYNCED_WITH_SERVER);
//
//                                //sending the broadcast to refresh the list
//                                context.sendBroadcast(new Intent(MainActivity.DATA_SAVED_BROADCAST));
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                },
//                new ResponseResult.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//
//                    }
//                }) {
//            @Override
//            protected Map<String, String> getParams() throws AuthFailureError {
//                Map<String, String> params = new HashMap<>();
//              //  params.put("name", name);
//                return params;
//            }
//        };

        //Log.d(TAG," one row : "+multipleRows.toString());
        Log.d("UploadToServer", "In SaveData");
        InputStream inputStream = null;
        String result = "";
        HttpURLConnection conn = null;

        try {

//            conn = (HttpURLConnection) url.openConnection();
            String json = multipleRows.toString();
            Log.d(TAG, "[postJSON] testbackend connecting to " + URL_SAVE_DUMP);

            URL url = new URL(URL_SAVE_DUMP);
            if (url.getProtocol().toLowerCase().equals("https")) {
                Log.d("UploadToServer", "[postJSON] [using https]");
                trustAllHosts();
                HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
                https.setHostnameVerifier(DO_NOT_VERIFY);
                conn = https;
            } else {
                conn = (HttpURLConnection) url.openConnection();
            }

            SSLContext sc;
            sc = SSLContext.getInstance("TLS");
            sc.init(null, null, new java.security.SecureRandom());

            //TODO testing to solve the SocketTimeoutException issue
            conn.setReadTimeout(5*60*1000);
            conn.setConnectTimeout(MY_SOCKET_TIMEOUT_MS);

            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            //TODO might need to use long instead of int is for the larger size but restricted to the api level should over 19
            conn.setFixedLengthStreamingMode(json.getBytes().length);
            conn.setRequestProperty("Content-Type","application/json");
            conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
            conn.setRequestProperty("Connection", "keep-alive");
            conn.connect();

            OutputStreamWriter wr= new OutputStreamWriter(conn.getOutputStream());
            wr.write(json);
            wr.close();

//            Log.d(TAG, "Post:\t" + dataType + "\t" + "for lastSyncTime:" + lastSyncTime);

            int responseCode = conn.getResponseCode();

            if(responseCode != HttpsURLConnection.HTTP_OK){

                CSVHelper.storeToCSV(CSV_Dump, "fail to connect to the server, error code: "+responseCode);
                CSVHelper.storeToCSV(CSV_Dump, "going to throw IOException");

                throw new IOException("HTTP error code: " + responseCode);
            } else {

                CSVHelper.storeToCSV(CSV_Dump, "connected to the server successfully");

                inputStream = conn.getInputStream();
            }
            result = convertInputStreamToString(inputStream);

            JSONObject JsonResponse = new JSONObject(result);

            String device_id = "";
            long returnTime = 0;
            device_id = JsonResponse.getString("device_id");
            returnTime = JsonResponse.getLong("detectTimeHour");
            if(device_id.toString().contains(Constants.DEVICE_ID)) {
                Log.d("UploadToServer", " repsonse : " + JsonResponse.toString());
                CSVHelper.storeToCSV(CSV_Dump, "Get dump data response: " + returnTime);
                responseFormAWS task = new responseFormAWS();
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, JsonResponse);
                sharedPrefs.edit().putLong("lastSentHour",ReadableTimeAddHour(returnTime, 1)).apply();
                //updateAllData(response);
            }
            else {
                CSVHelper.storeToCSV(CSV_Dump, "Get Dump data error response ");
                Log.d("UploadToServer","ErrorResponse");
            }

            Log.d("UploadToServer", "[postJSON] the result response code is " + responseCode);
            Log.d("UploadToServer", "[postJSON] the result is " + result);

        }
        catch (NoSuchAlgorithmException e) {

            Log.d(TAG, "NoSuchAlgorithmException", e);
            CSVHelper.storeToCSV(CSV_Dump, "NoSuchAlgorithmException");
        } catch (KeyManagementException e) {
            e.printStackTrace();
            Log.d(TAG, "KeyManagementException", e);
            CSVHelper.storeToCSV(CSV_Dump, "KeyManagementException");
        }
        catch (ProtocolException e) {
            Log.d(TAG, "ProtocolException", e);
            CSVHelper.storeToCSV(CSV_Dump, "ProtocolException");
        } catch (MalformedURLException e) {
            Log.d(TAG, "MalformedURLException", e);
            CSVHelper.storeToCSV(CSV_Dump, "MalformedURLException");
        } catch (java.net.SocketTimeoutException e){
            Log.d("UploadToServer", "SocketTimeoutException EE", e);
            CSVHelper.storeToCSV(CSV_Dump, "SocketTimeoutException");
        } catch (IOException e) {
            e.printStackTrace();
            CSVHelper.storeToCSV(CSV_Dump, "IOException");
        } catch(JSONException e){
            Log.d(TAG, "JSONException", e);
            CSVHelper.storeToCSV(CSV_Dump, "JSONException");
        }finally {

            CSVHelper.storeToCSV(CSV_Dump, "connection is not null ? "+(conn != null));

            if (conn != null) {

                CSVHelper.storeToCSV(CSV_Dump, "going to disconnect");

                conn.disconnect();

                try{
                    Thread.sleep(100);
                }catch(Exception e){
                    e.printStackTrace();
                }

                CSVHelper.storeToCSV(CSVHelper.CSV_Dump, "disconnected successfully");
            }
        }
//        10/29 嘗試HTTP connection
//        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, URL_SAVE_DUMP, multipleRows, new Response.Listener<JSONObject>() {
//            @androidx.annotation.RequiresApi(api = Build.VERSION_CODES.N)
//            @Override
//            public void onResponse(JSONObject response) {
//                if(response!=null) {
//                    String device_id = "";
//                    long returnTime = 0;
//                    try {
//                        device_id = response.getString("device_id");
//                        returnTime = response.getLong("detectTimeHour");
//                        Log.d(TAG, "id : " + device_id);
//                        Log.d(TAG, "detectTimeHour : " + returnTime);
////                        String access_time = response.getString("Accessibility");
////                        Log.d(TAG,"access_time : "+access_time);
//
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                    catch(Exception e) {
//                        e.printStackTrace();
//                    }
//                    Log.d(TAG,"device Id : "+device_id.toString());
//                    Log.d(TAG,"detect time : "+ returnTime);
//
//                    if(device_id.toString().contains(Constants.DEVICE_ID)) {
//                        Log.d(TAG, " repsonse : " + response.toString());
//                        dumpDataResponse++;
//                        CSVHelper.storeToCSV("DumpData.csv", "Response number: " + dumpDataResponse);
//                        responseFormAWS task = new responseFormAWS();
//                        task.execute(response);
//                        sharedPrefs.edit().putLong("lastSentHour",ReadableTimeAddHour(returnTime, 1)).apply();
//                        //updateAllData(response);
//                    }
//                }
//                Log.d(TAG, "SaveData Finish");
////                //TODO: handle success
//
////                try {
////                    JSONObject obj = response;
////                    Log.d(TAG," repsonse : "+response.toString());
////
////                    if (obj.getString("error")=="false") {  //沒有錯誤
////                        //updating the status in sqlite
////                        Log.d(TAG," repsonse : error = false");
////
////                        db.accessibilityDataRecordDao().updateDataStatus(creationTime.get(0), DATA_SYNCED_WITH_SERVER);
////
////                        //sending the broadcast to refresh the list
////                        context.sendBroadcast(new Intent(DATA_SAVED_BROADCAST));
////                    }else{
////                        db.accessibilityDataRecordDao().updateDataStatus(creationTime.get(0), DATA_NOT_SYNCED_WITH_SERVER);
////
////                    }
////
////                } catch (Exception e) {
////                    e.printStackTrace();
////                }
//
//                // refreshAllContent(60*1000*10); // TODO 10min->1hr
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                error.printStackTrace();
//                dumpDataResponse++;
//                CSVHelper.storeToCSV("DumpData.csv", "Error response number: " + dumpDataResponse);
//                CSVHelper.storeToCSV("DumpData.csv", "Error: " + error);
//                Log.d(TAG,"ErrorResponse");
//                //TODO: handle failure
//            }
//        });
//        jsonRequest.setRetryPolicy(new DefaultRetryPolicy(
//                MY_SOCKET_TIMEOUT_MS,
//                1,  //0
//                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//
//        VolleySingleton.getInstance(context).addToRequestQueue(jsonRequest);
    }

    private String convertInputStreamToString(InputStream inputStream) throws IOException{

        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null){
//            Log.d(LOG_TAG, "[syncWithRemoteDatabase] " + line);
            result += line;
        }

        inputStream.close();
        return result;

    }
    private void trustAllHosts() {

        X509TrustManager easyTrustManager = new X509TrustManager() {

            public void checkClientTrusted(
                    X509Certificate[] chain,
                    String authType) throws CertificateException {
            }

            public void checkServerTrusted(
                    X509Certificate[] chain,
                    String authType) throws CertificateException {
            }

            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

        };

        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] {easyTrustManager};

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");

            sc.init(null, trustAllCerts, new java.security.SecureRandom());

            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {

        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    private void saveNewsData(JSONObject multipleRows) {
        Log.d(TAG, "In SaveData");
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, URL_SAVE_NEWS, multipleRows, new Response.Listener<JSONObject>() {
            @androidx.annotation.RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onResponse(JSONObject response) {

                if(response!=null) {
                    String device_id = "";
                    try {
                        device_id = response.getString("device_id");
                        Log.d(TAG,"id : "+device_id);
//                        String access_time = response.getString("Accessibility");
//                        Log.d(TAG,"access_time : "+access_time);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG,"device Id : "+device_id.toString());
                    Log.d(TAG,"device Id constant : "+Constants.DEVICE_ID);

                    if(true) {
                        try {
                            Log.d(TAG, "Newsdata Response string: " + response.getString("SessionID"));
                            JSONArray responseID = response.getJSONArray("SessionID");
                            CSVHelper.storeToCSV("DumpData.csv", "get newsdata response: " + responseID.toString());
                            for(int i = 0; i < responseID.length(); i++){
                                db.SessionDataRecordDao().updateDataStatus(((Number)responseID.get(i)).longValue(), DATA_SYNCED_WITH_SERVER);
                                db.NewsDataRecordDao().updateDataStatusBySessionID(((Number)responseID.get(i)).longValue(), DATA_SYNCED_WITH_SERVER);
                            }
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                        catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                Log.d(TAG, "SaveData Finish");
//                //TODO: handle success

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                CSVHelper.storeToCSV("DumpData.csv", "newsdata error response: " + error.getMessage());
                //TODO: handle failure
            }
        });
        jsonRequest.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                0,  //0
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleySingleton.getInstance(context).addToRequestQueue(jsonRequest);
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public List<Long> getLongList(String creationTime) {

        String[] target = {"[", "]", "-", "\""};
        for (String temp : target) {
            creationTime = creationTime.replace(temp, "");
        }
        Log.d(TAG, " create : " + creationTime);
        if (creationTime != "") {
            final List<Long> longs = Arrays
                    .stream(creationTime.split(","))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            for (Long tmp : longs) {
                Log.d(TAG, " long : " + tmp);
            }
            return longs;
        }
        return null;
    }

    public List<String> getStringList(String creationTime) {

        String[] target = {"[", "]", "-", "\""};
        for (String temp : target) {
            creationTime = creationTime.replace(temp, "");
        }
        Log.d(TAG, " create : " + creationTime);
        if (creationTime != "") {
            final List<String> longs = Arrays
                    .stream(creationTime.split(","))
                    .collect(Collectors.toList());
            for (String tmp : longs) {
                Log.d(TAG, " string : " + tmp);
            }
            return longs;
        }
        return null;
    }

    class sendToAWS extends AsyncTask<Void,Void,Void> {

        @androidx.annotation.RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected Void doInBackground(Void ...voids) {
            sendingNewsData();
            sendingDumpData();
            return null;
        }
        protected void onPreExecute() {
            super.onPreExecute();
//            deleteAllSyncData();
        }
//        protected void onPostExecute(Integer result){
//            super.onPostExecute(result);
//        }
    }

    public void deleteAllSyncData(){
        Log.d(TAG,"delete all sync data");
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
        db.finalAnswerDao().deleteSyncData(1);
    }
    class responseFormAWS extends AsyncTask<JSONObject,Void,Void> {

        @androidx.annotation.RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected Void doInBackground(JSONObject ...jsonObjects) {
            updateAllData(jsonObjects[0]);
            return null;
        }
    }



    @RequiresApi(api = Build.VERSION_CODES.N)
    public void updateAllData(JSONObject response){
        // have inserted
        String HIAppTimes = "";
        String HIAccessibilty = "";
        String HIActivityRecognition = "";
        String HIBattery = "";
        String HIConnectivity = "";
        String HILocation = "";
        String HIMobileCrowdsource = "";
        String HINotification = "";
        String HIRinger = "";
        String HITransportation= "";
        String HIAppusage = "";
        String HITelephony = "";
        String HISensor = "";
        String HIFinal = "";
        String HIResponse = "";
        Iterator keysToCopyIterator = response.keys();
        List<String> keysList = new ArrayList<String>();
        while(keysToCopyIterator.hasNext()) {
            String key = (String) keysToCopyIterator.next();
            keysList.add(key);
            Log.d(TAG,"key  : "+key);
        }
        for(String key : keysList) {
            try {
                String result = response.getString(key);
                if(key.equals("myAccessibility"))HIAccessibilty = result;
                else if(key.equals("ActivityRecognition"))HIActivityRecognition = result;
                else if(key.equals("Battery"))HIBattery = result;
                else if(key.equals("Connectivity"))HIConnectivity = result;
                else if(key.equals("Location"))HILocation = result;
                else if(key.equals("MobileCrowdsource"))HIMobileCrowdsource = result;
                else if(key.equals("Notification"))HINotification = result;
                else if(key.equals("Ringer"))HIRinger = result;
                else if(key.equals("TransportationMode"))HITransportation = result;
                else if(key.equals("AppUsage"))HIAppusage = result;
                else if(key.equals("Telephony"))HITelephony = result;
                else if(key.equals("Sensor"))HISensor = result;
                else if(key.equals("QuestionnaireAns"))HIFinal = result;
                else if(key.equals("Response"))HIResponse = result;
                else if(key.equals("ScreenShotDecider"))HIAppTimes = result;


//                HIAccessibilty = response.getString("Accessibility");
//                HIActivityRecognition = response.getString("ActivityRecognition");
//                HIBattery = response.getString("Battery");
//                HIConnectivity = response.getString("Connectivity");
//                HILocation = response.getString("Location");
//                HIMobileCrowdsource = response.getString("MobileCrowdsource");
//                HINotification = response.getString("Notification");
//                HIRinger = response.getString("Ringer");
//                HITransportation = response.getString("TransportationMode");
//                HIAppusage = response.getString("Appusage");
//                HITelephony = response.getString("Telephony");
//                HISensor = response.getString("Sensor");
//                HIFinal = response.getString("QuestionnaireAns");
//            Log.d(TAG,"HIAccess : "+HIAccessibilty);
//            Log.d(TAG,"HIActivityRecognition : "+HIActivityRecognition);
//            Log.d(TAG,"HIBattery : "+HIBattery);
//            Log.d(TAG,"HIConnectivity : "+HIConnectivity);
//            Log.d(TAG,"HILocation : "+HILocation);
//            Log.d(TAG,"HIMobileCrowdsource : "+HIMobileCrowdsource);
//            Log.d(TAG,"HINotification : "+HINotification);
//            Log.d(TAG,"HIRinger : "+HIRinger);
//            Log.d(TAG,"HITransportation : "+HITransportation);
//            Log.d(TAG,"HIAppusage : "+HIAppusage);
//            Log.d(TAG,"HITelephony : "+HITelephony);
//            Log.d(TAG,"HISensor : "+HISensor);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(HIAppTimes!="")
            updateAppTimesDataRecord(HIAppTimes);
        if(HIAccessibilty!="")
            updateAccessibilityDataRecord(HIAccessibilty);
        if(HIActivityRecognition!="")
            updateActivityRecognitionDataRecord(HIActivityRecognition);
        if(HIAppusage!="")
            updateAppUsageDataRecord(HIAppusage);
        if(HIBattery!="")
            updateBatteryDataRecord(HIBattery);
        if(HIConnectivity!="")
            updateConnectivityDataRecord(HIConnectivity);
        if(HIRinger!="")
            updateRingerDataRecord(HIRinger);
        if(HILocation!="")
            updateLocationDataRecord(HILocation);
        if(HITransportation!="")
            updateTransportationModeDataRecord(HITransportation);
        if(HITelephony!="")
            updateTelephonyDataRecord(HITelephony);
        if(HISensor!="")
            updateSensorDataRecord(HISensor);
        if(HINotification!="")
            updateNotificationDataRecord(HINotification);
        if(HIMobileCrowdsource!="")
            updateMobileCrowdsourceDataRecord(HIMobileCrowdsource);
        if(HIFinal!="")
            updateFinalAnswerDataRecord(HIFinal);
        if(HIResponse!=""){
            updateResponseDataRecord(HIResponse);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void updateActivityRecognitionDataRecord(String creationTime){
        try {
            List<Long> creationTimeLongList = getLongList(creationTime);
            if(creationTimeLongList!=null) {
                for (Long temp : creationTimeLongList) {
                    Log.d(TAG, "Activityrecog : " + temp.toString());
                    if (temp != null) {
                        db.activityRecognitionDataRecordDao().updateDataStatus(temp, DATA_SYNCED_WITH_SERVER);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void updateAppTimesDataRecord(String creationTime){
        try {
            List<Long> creationTimeLongList = getLongList(creationTime);
            if(creationTimeLongList!=null) {
                for (Long temp : creationTimeLongList) {
                    if (temp != null) {
                        db.AppTimesDataRecordDao().updateDataStatus(temp, DATA_SYNCED_WITH_SERVER);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void updateAccessibilityDataRecord(String creationTime){
        try {
            List<Long> creationTimeLongList = getLongList(creationTime);
            if(creationTimeLongList!=null) {
                for (Long temp : creationTimeLongList) {
                    if (temp != null) {
                        db.MyDataRecordDao().updateDataStatus(temp, DATA_SYNCED_WITH_SERVER);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void updateAppUsageDataRecord(String creationTime){
        try {
            List<Long> creationTimeLongList = getLongList(creationTime);
            if(creationTimeLongList!=null) {
                for (Long temp : creationTimeLongList) {
                    if (temp != null) {
                        db.appUsageDataRecordDao().updateDataStatus(temp, DATA_SYNCED_WITH_SERVER);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void updateBatteryDataRecord(String creationTime){
        try {
            List<Long> creationTimeLongList = getLongList(creationTime);
            if(creationTimeLongList!=null) {
                for (Long temp : creationTimeLongList) {
                    if (temp != null) {
                        db.batteryDataRecordDao().updateDataStatus(temp, DATA_SYNCED_WITH_SERVER);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void updateConnectivityDataRecord(String creationTime){
        try {
            List<Long> creationTimeLongList = getLongList(creationTime);
            if(creationTimeLongList!=null) {
                for (Long temp : creationTimeLongList) {
                    if (temp != null) {
                        db.connectivityDataRecordDao().updateDataStatus(temp, DATA_SYNCED_WITH_SERVER);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void updateRingerDataRecord(String creationTime){
        try {
            List<Long> creationTimeLongList = getLongList(creationTime);
            if(creationTimeLongList!=null) {
                for (Long temp : creationTimeLongList) {
                    if (temp != null) {
                        db.ringerDataRecordDao().updateDataStatus(temp, DATA_SYNCED_WITH_SERVER);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void updateLocationDataRecord(String creationTime){
        try {
            List<Long> creationTimeLongList = getLongList(creationTime);
            if(creationTimeLongList!=null) {
                for (Long temp : creationTimeLongList) {
                    if (temp != null) {
                        db.locationDataRecordDao().updateDataStatus(temp, DATA_SYNCED_WITH_SERVER);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void updateTransportationModeDataRecord(String creationTime){
        try {
            List<Long> creationTimeLongList = getLongList(creationTime);
            if(creationTimeLongList!=null) {
                for (Long temp : creationTimeLongList) {
                    if (temp != null) {
                        db.transportationModeDataRecordDao().updateDataStatus(temp, DATA_SYNCED_WITH_SERVER);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void updateResponseDataRecord(String creationTime){
        try {
            List<Long> creationTimeLongList = getLongList(creationTime);
            if(creationTimeLongList!=null) {
                for (Long temp : creationTimeLongList) {
                    if (temp != null) {
                        db.repsonseDataRecordDao().updateDataStatus(temp, DATA_SYNCED_WITH_SERVER);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void updateSensorDataRecord(String creationTime){
        try {
            List<Long> creationTimeLongList = getLongList(creationTime);
            if(creationTimeLongList!=null) {
                for (Long temp : creationTimeLongList) {
                    if (temp != null) {
                        db.sensorDataRecordDao().updateDataStatus(temp, DATA_SYNCED_WITH_SERVER);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void updateTelephonyDataRecord(String creationTime){
        try {
            List<Long> creationTimeLongList = getLongList(creationTime);
            if(creationTimeLongList!=null) {
                for (Long temp : creationTimeLongList) {
                    if (temp != null) {
                        db.telephonyDataRecordDao().updateDataStatus(temp, DATA_SYNCED_WITH_SERVER);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void updateNotificationDataRecord(String creationTime){
        try {
            List<Long> creationTimeLongList = getLongList(creationTime);
            if(creationTimeLongList!=null) {
                for (Long temp : creationTimeLongList) {
                    if (temp != null) {
                        db.notificationDataRecordDao().updateDataStatus(temp, DATA_SYNCED_WITH_SERVER);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void updateMobileCrowdsourceDataRecord(String creationTime){
        try {
            List<Long> creationTimeLongList = getLongList(creationTime);
            if(creationTimeLongList!=null) {
                for (Long temp : creationTimeLongList) {
                    if (temp != null) {
                        db.mobileCrowdsourceDataRecordDao().updateDataStatus(temp, DATA_SYNCED_WITH_SERVER);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void updateFinalAnswerDataRecord(String creationTime){
        try {
            List<String> creationTimeLongList = getStringList(creationTime);
            if(creationTimeLongList!=null) {
                for (String temp : creationTimeLongList) {
                    String related_id = temp.split("\\+")[0];
                    String question_id = temp.split("\\+")[1];
                    if (temp != null) {
                        db.finalAnswerDao().updateDataStatusWithID(related_id, question_id, DATA_SYNCED_WITH_SERVER);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean sendingUserData(){
        JSONObject UserJson = new JSONObject();
//        Integer todayMCount = sharedPrefs.getInt("todayMCount",0);
//        Integer todayNCount = sharedPrefs.getInt("todayNCount",0);
//        Integer allMCount = sharedPrefs.getInt("allMCount",0);
//        Integer allNCount = sharedPrefs.getInt("allNCount",0);


        try {
            UserJson.put("device_id",Constants.DEVICE_ID);
            UserJson.put("todayMCount",todayMCount);
            //UserJson.put("todayNCount",todayNCount);
            UserJson.put("allMCount",allMCount);
            //UserJson.put("allNCount",allNCount);
            UserJson.put("videoCount",videoCount);
            UserJson.put("total",allMCount);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG,"user :"+UserJson.toString());
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, URL_SAVE_USER, UserJson, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG," repsonse : "+response.toString());
                //updateAllData(response);
//                //TODO: handle success

//                try {
//                    JSONObject obj = response;
//                    Log.d(TAG," repsonse : "+response.toString());
//
//                    if (obj.getString("error")=="false") {  //沒有錯誤
//                        //updating the status in sqlite
//                        Log.d(TAG," repsonse : error = false");
//
//                        db.accessibilityDataRecordDao().updateDataStatus(creationTime.get(0), DATA_SYNCED_WITH_SERVER);
//
//                        //sending the broadcast to refresh the list
//                        context.sendBroadcast(new Intent(DATA_SAVED_BROADCAST));
//                    }else{
//                        db.accessibilityDataRecordDao().updateDataStatus(creationTime.get(0), DATA_NOT_SYNCED_WITH_SERVER);
//
//                    }
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
                refreshUser(60*1000*60*2); // TODO 2hr
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();

                //TODO: handle failure
            }
        });
        jsonRequest.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                0,  //0
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


        VolleySingleton.getInstance(context).addToRequestQueue(jsonRequest);

        return true;
    }
    public void sendingNewsData(){
        Cursor transCursor = null;
        Log.d(TAG, "sending New data");
        try {
            JSONObject jobject = new JSONObject();
            JSONArray jarray = new JSONArray();

            transCursor = db.SessionDataRecordDao().getUnsyncedData(0);

            int rows = transCursor.getCount();

            CSVHelper.storeToCSV(CSV_Dump, "Newsdata rows: " + rows);

            if(rows!=0){
                transCursor.moveToFirst();
                for(int i=0;i<rows;i++) {
                    JSONObject oneRow = new JSONObject();

                    Long SessionID = transCursor.getLong(0);
                    Long StartTimestamp = transCursor.getLong(1);
                    Long EndTimestamp = transCursor.getLong(6);
                    String startTime = transCursor.getString(2);
                    String endTime = transCursor.getString(3);
                    String dataType = transCursor.getString(4);
                    String appName = transCursor.getString(7);
                    String PhoneSessionID = String.valueOf(transCursor.getLong(9));

                    UserDataRecord userRecord = db.userDataRecordDao().getLastRecord();
                    String UserID = "NA";
                    if(userRecord != null) {
                        UserID = userRecord.getUserId();
                    }
//                    String UserID = sharedPrefs.getString("UserNum","NA");
                    Log.d(TAG,"timestamp : "+StartTimestamp+" startTime : "+startTime+" endTime : "+endTime+" SessionID : "+SessionID);

                    oneRow.put("PhoneSessionID",PhoneSessionID);
                    oneRow.put("AppSessionID",SessionID);
                    oneRow.put("startTime",startTime);
                    oneRow.put("endTime",endTime);
                    oneRow.put("StartTimestamp",StartTimestamp);
                    oneRow.put("EndTimestamp",EndTimestamp);
                    oneRow.put("dataType",dataType);
                    oneRow.put("appName",  appName);
                    oneRow.put("readable",getReadableTimeLong(StartTimestamp));
                    oneRow.put("device_id", Constants.DEVICE_ID);
                    oneRow.put("user_id", UserID);

                    JSONArray multiRows = new JSONArray();
                    List<NewsDataRecord> newsarr = db.SessionDataRecordDao().getNewsData(SessionID);
                    for(int j = 0; j < newsarr.size(); j++){
                        JSONObject dataobject = new JSONObject();
                        Long creationtime = newsarr.get(j).getCreationTime();
                        String fileName = newsarr.get(j).getfileName();
                        String filePath = newsarr.get(j).getfilePath();
                        String content = newsarr.get(j).getcontent();

                        dataobject.put("timestamp", creationtime);
                        dataobject.put("fileName",  fileName);
                        dataobject.put("filePath",  filePath);
                        dataobject.put("content",  content);

                        multiRows.put(dataobject);
                    }
                    oneRow.put("data", multiRows);
                    jarray.put(oneRow);
                    transCursor.moveToNext();
                }
                jobject.put("NewsData", jarray);
                Log.d(TAG,"data : "+ jobject.toString());
                if (!Constants.DEVICE_ID.equals("NA") || jobject.length() != 0){
                    CSVHelper.storeToCSV(CSV_Dump, "sending newsdata to server");
                    saveNewsData(jobject);
                }
            }
        }catch (JSONException e){
            e.printStackTrace();
        }catch(NullPointerException e){
            e.printStackTrace();
        }
        finally {
            if(transCursor != null){
                transCursor.close();
            }
        }
    }

    public boolean sendingDumpData() {
        Log.d(TAG, "in dump data");
        JSONObject dataInJson;
//        Integer atMostnumberOfRows = 10;
//        storeAccessibility(dataInJson,atMostnumberOfRows);

        Long nowTime = new Date().getTime();
        nowSentHour = getReadableTimeLong(nowTime);

        Log.d(TAG, "nowSentHour : " + nowSentHour);

        lastSentHour = sharedPrefs.getLong("lastSentHour", 0); //這個小時的data也要傳
        Log.d(TAG, "lastSendHour : " + lastSentHour);



        /*Log.d(TAG,"startAppHour : "+startAppHour);
        if(lastSentHour==Long.valueOf(0)||lastSentHour<startAppHour) {   //nowsent = lastSentHour = start = 2019090411
            sharedPrefs.edit().putLong("lastSentHour", startAppHour).apply();
            lastSentHour = startAppHour;
            nowSentHour = lastSentHour;
        }
        else {
            lastSentHour = sharedPrefs.getLong("lastSentHour", startAppHour);  // current = 2019090412 last = 2019090411  //current = 2019090413 last = 201909411
            //error handling

            if(nowSentHour/1000000L>0) {
                nowSentHour = lastSentHour + 1;         //nowSent = 2019090412   // 2019090412
                if(nowSentHour%100 == 24){
                    if(currentHour>nowSentHour){      //  12 == 12  // 13 > 20190412
                        nowSentHour = (currentHour/100)*100;
                    }
                }
            }else{
                nowSentHour = currentHour;
            }
        }
        // app crash handle
        if(nowSentHour == Long.valueOf(0)){
            nowSentHour = currentHour;
            lastSentHour = sharedPrefs.getLong("lastSentHour", startAppHour);
            if(lastSentHour == Long.valueOf(0)){
                sharedPrefs.edit().putLong("lastSentHour",nowSentHour).apply();
            }
        }


        Log.d(TAG,"currentHour : "+currentHour);
        Log.d(TAG,"nowSentHour : "+nowSentHour);*/
        Long i = getFirstDataTime(nowSentHour);

        dumpData_interval = DumpDataInterval(i, nowSentHour);
        Log.d("UploadToServer", "Dump data interval: " + dumpData_interval);

//        Long i = appStartHour;
//        first = context.getSharedPreferences("test", Context.MODE_PRIVATE).getBoolean("sendfirst", true);
//        if (first) {
//            i = appStartHour;
//            if (appStartHour >= nowSentHour)
//                sharedPrefs.edit().putLong("lastSentHour", nowSentHour).apply();
//            CSVHelper.storeToCSV("netWork.csv", "appStartHour :" + appStartHour);
//            context.getSharedPreferences("test", Context.MODE_PRIVATE).edit().putBoolean("sendfirst", false).apply();
//        }

        if (i / 1000000 < 2020) return false;

        CSVHelper.storeToCSV("DumpData.csv", "While loop start: " + i + " " + nowSentHour);

        while (i < nowSentHour) {
//            try{
//                Thread.sleep(60*1000);
//            }
//            catch(Exception e){
//                e.printStackTrace();
//            }
            dataInJson = new JSONObject();
            //Log.d(TAG, "While loop hour: " + i + " " + nowSentHour);
            storeAppTimes(dataInJson, i);
            storeAccessibility(dataInJson, i);
            storeTransporatation(dataInJson, i);
            storeLocation(dataInJson, i);
            storeActivityRecognition(dataInJson, i);
            storeRinger(dataInJson, i);
            storeConnectivity(dataInJson, i);
            storeBattery(dataInJson, i);
            storeAppUsage(dataInJson, i);
            storeTelephony(dataInJson, i);
            storeSensor(dataInJson, i);
            storeNotification(dataInJson, i);
            //storeMobileCrowdsource(dataInJson);
            storeQuestionnaireAnswer(dataInJson, i);
            //storeResponse(dataInJson);

            UserDataRecord userRecord = db.userDataRecordDao().getLastRecord();
            String UserID = "NA";
            if(userRecord != null) {
                UserID = userRecord.getUserId();
            }
//            String UserID = sharedPrefs.getString("UserNum", "NA");
            if(dataInJson.length() == 0)
            {
                // 7/21
                if(i < lastSentHour) {
                    CSVHelper.storeToCSV("DumpData.csv", "nowSentHour: " + nowSentHour + " dataSendHour: " + i + " lastSendHour: " + lastSentHour + " , " + "已上傳");
//                    CSVHelper.storeToCSV("netWork.csv", dataInJson.toString());
                    i = ReadableTimeAddHour(i, 1);
                    dumpData_response_number++;
                    Log.d("UploadToServer", "dumpData_response_number: " + dumpData_response_number);

                    continue;
                }
            }
            CSVHelper.storeToCSV("DumpData.csv", "nowSentHour: " + nowSentHour + " dataSendHour: " + i + " lastSendHour: " + lastSentHour );
//            CSVHelper.storeToCSV("netWork.csv", dataInJson.toString());
            try {
                String currentTimeString = ScheduleAndSampleManager.getCurrentTimeString();
                dataInJson.put("device_id", Constants.DEVICE_ID);
                dataInJson.put("user_id", UserID);
                dataInJson.put("timeString", currentTimeString);
                dataInJson.put("detectTimeHour", i);
                dataInJson.put("uploadTimeHour", nowSentHour);
                try {
                    dataInJson.put("_id", Constants.DEVICE_ID + "_" + System.currentTimeMillis());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (!Constants.DEVICE_ID.equals("NA") || dataInJson.length() != 0){
//                CSVHelper.storeToCSV("DumpData.csv", "saving dump data (" + dumpDataNumber + ")");
                CSVHelper.storeToCSV("DumpData.csv", "Sending the dump data(" + dumpData_response_number + ")");
                saveData(dataInJson);
                dumpData_response_number++;
                Log.d("UploadToServer", "dumpData_response_number: " + dumpData_response_number);


//                while(dumpDataNumber != dumpDataResponse){
//                    try{
//                        Thread.sleep(500);
//                    }catch (Exception e){
//                        e.printStackTrace();
//                    }
//                }
//                CSVHelper.storeToCSV("DumpData.csv", "Get the number " + dumpDataResponse + " response");
            }
            i = ReadableTimeAddHour(i, 1);
        }

        //start upload img
//        conn.disconnect();
        dumpData_finish = true;
        Log.d("UploadToServer", "dumpData_finish: " + dumpData_finish);

        sharedPrefs.edit().putLong("lastSentHour", nowSentHour).apply();

        return true;
    }


    /*public void sendAllTodayData(){
        Long nowTime = new Date().getTime() ;
        currentHour = getReadableTimeLong(nowTime);
        lastSentHour = sharedPrefs.getLong("lastSentHour", startAppHour);
        nowSentHour = lastSentHour+1;
        while(nowSentHour<currentHour){
            //send  data till currentHour
            JSONObject dataInJson = new JSONObject();
            storeAccessibility(dataInJson);
            storeTransporatation(dataInJson);
            storeLocation(dataInJson);
            storeActivityRecognition(dataInJson);
            storeRinger(dataInJson);
            storeConnectivity(dataInJson);
            storeBattery(dataInJson);
            storeAppUsage(dataInJson);
            storeTelephony(dataInJson);
            storeSensor(dataInJson);
            storeNotification(dataInJson);
            storeMobileCrowdsource(dataInJson);
            storeQuestionnaireAnswer(dataInJson);
            storeResponse(dataInJson);
            if(Constants.DEVICE_ID!="NA"||dataInJson.length() != 0)
                //saveData(dataInJson);
            nowSentHour +=1;
        }
        sharedPrefs.edit().putLong("lastSentHour",nowSentHour).apply();
    }*/

    private int DumpDataInterval(long StartTime, long EndTime){
        int Year_S = (int)StartTime/1000000;
        StartTime = StartTime % 1000000;

        int Month_S = (int)StartTime/10000;
        StartTime = StartTime % 10000;

        int Day_S = (int)StartTime/100;
        StartTime = StartTime % 100;

        int Hour_S = (int)StartTime;

        int Year_E = (int)EndTime/1000000;
        EndTime = EndTime % 1000000;

        int Month_E = (int)EndTime/10000;
        EndTime = EndTime % 10000;

        int Day_E = (int)EndTime/100;
        EndTime = EndTime % 100;

        int Hour_E = (int)EndTime;

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, Year_S);
        cal.set(Calendar.MONTH, Month_S - 1);
        cal.set(Calendar.DAY_OF_MONTH, Day_S);
        cal.set(Calendar.HOUR_OF_DAY, Hour_S);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        Calendar cal2 = Calendar.getInstance();
        cal2.set(Calendar.YEAR, Year_E);
        cal2.set(Calendar.MONTH, Month_E - 1);
        cal2.set(Calendar.DAY_OF_MONTH, Day_E);
        cal2.set(Calendar.HOUR_OF_DAY, Hour_E);
        cal2.set(Calendar.MINUTE, 0);
        cal2.set(Calendar.SECOND, 0);

        return (int)((cal2.getTimeInMillis() - cal.getTimeInMillis()) / (60 * 60 * 1000.0));
    }

    private long getFirstDataTime(long nowSendHour){
        Long appstart = 0L;
        UserDataRecord userRecord = db.userDataRecordDao().getLastRecord();
        if(userRecord != null){
            appstart = userRecord.getApp_start();
        }
        long readable_time = appstart;
        Cursor transCursor = null;
        try {
            transCursor = db.appUsageDataRecordDao().getFirstData(1, 0);
            int rows = transCursor.getCount();
            if(rows!=0){
                transCursor.moveToFirst();
                if(nowSendHour != transCursor.getLong(7))
                    readable_time = transCursor.getLong(7);
//              Log.d(TAG,"timestamp : "+timestamp+" ScreenStatus : "+ScreenStatus+" Latest_Used_App : "+Latest_Used_App+" Latest_Foreground_Activity : "+Latest_Foreground_Activity);

            }
        }catch(NullPointerException e){
            e.printStackTrace();
        }
        finally {
            if(transCursor != null){
                transCursor.close();
            }
        }
        Log.d(TAG, "first data time: " + appstart);
        return appstart;
    }


    private void storeAppTimes(JSONObject data, Long data_hour){
        Cursor transCursor = null;

        Log.d(TAG, "storeApptimes");

        try {

            JSONArray multiRows = new JSONArray();

            transCursor = db.AppTimesDataRecordDao().getUnsyncedData(0, data_hour);

            int allRows = transCursor.getCount();

            Log.d(TAG, "rows : "+allRows);

            if(allRows!=0){
                transCursor.moveToFirst();
                for(int i=0;i<allRows;i++) {
                    JSONObject oneRow = new JSONObject();
                    Long timestamp = transCursor.getLong(1);
                    String[] d = {"","","FacebookAgree", "FacebookCancel", "",
                     "YoutubeAgree", "YoutubeCancel", "InstagramAgree",
                     "InstagramCancel", "NewsappAgree", "NewsappCancel",
                     "", "LinetodayAgree", "LinetodayCancel",
                     "", "GooglenowAgree", "GooglenowCancel",
                    "ChromeAgree", "ChromeCancel"};
                    for(int j = 2; j < d.length; j++)
                    {
                        if(!d[j].equals("")) {
                            oneRow.put(d[j], transCursor.getInt(j));
                        }
                    }
                    String PhoneSessionID = String.valueOf(transCursor.getLong(22));
                    String ScreenShot = transCursor.getString(23);
                    String ImageName = transCursor.getString(24);
                    String SessionID = transCursor.getString(25);
//                    String AccessibilityUrl = transCursor.getString(25);
//                    String NotificationUrl = transCursor.getString(26);

                    oneRow.put("PhoneSessionID",PhoneSessionID);
                    oneRow.put("ScreenShot",ScreenShot);
                    oneRow.put("ImageName",ImageName);
                    oneRow.put("AppSessionID",SessionID);
//                    oneRow.put("AccessibilityUrl",AccessibilityUrl);
//                    oneRow.put("Notification",NotificationUrl);
                    oneRow.put("timestamp",timestamp);
                    oneRow.put("detect_time",ScheduleAndSampleManager.getTimeString(timestamp));
                    multiRows.put(oneRow);
                    transCursor.moveToNext();
                }

                data.put("ScreenShotDecider",multiRows); //改成ScreenShotDecider 1/14
                Log.d(TAG, data.toString());
            }else{

            }
        } catch(JSONException e){
            e.printStackTrace();
        }
        catch(NullPointerException e){
            e.printStackTrace();
        }
        finally {
            if(transCursor != null){
                transCursor.close();
            }
        }


    }


    private void storeAccessibility(JSONObject data, Long data_hour){

        Log.d(TAG, "storeAccessibility");
        Cursor transCursor = null;
        try {

            JSONArray multiRows = new JSONArray();


            transCursor = db.MyDataRecordDao().getUnsyncedData(0,data_hour);

            int allRows = transCursor.getCount();

            Log.d(TAG, "rows : "+allRows);

            if(allRows!=0){
                transCursor.moveToFirst();
                for(int i=0;i<allRows;i++) {
                    JSONObject oneRow = new JSONObject();
                    Long timestamp = transCursor.getLong(1);
                    String id = transCursor.getString(2);
                    String packagename = transCursor.getString(3);
                    String myevent = transCursor.getString(4);
                    String eventtext = transCursor.getString(5);
                    String eventtype = transCursor.getString(6);
                    String extra = transCursor.getString(7);
                    String SessionID = transCursor.getString(10);
                    String PhoneSessionID = String.valueOf(transCursor.getLong(11));
                    String ScreenShot = transCursor.getString(12);
                    String ImageName = transCursor.getString(13);
                    String ESM_ID = transCursor.getString(14);
                    String NewsApp = transCursor.getString(15);
//                    String AccessibilityUrl = transCursor.getString(14);
//                    String NotificationUrl = transCursor.getString(15);

                    oneRow.put("PhoneSessionID",PhoneSessionID);
                    oneRow.put("AppSessionID",SessionID);
                    oneRow.put("ScreenShot",ScreenShot);
                    oneRow.put("PackageName",packagename);
                    oneRow.put("MyEventText",myevent);
                    oneRow.put("EventText",eventtext);
                    oneRow.put("Extra",extra);
                    oneRow.put("EventType",eventtype);
                    oneRow.put("ESM_ID",ESM_ID);
                    oneRow.put("ImageName",ImageName);
                    oneRow.put("NewsApp",NewsApp);
//                    oneRow.put("AccessibilityUrl",AccessibilityUrl);
//                    oneRow.put("Notification",NotificationUrl);

                    oneRow.put("timestamp",timestamp);
                    oneRow.put("detect_time",ScheduleAndSampleManager.getTimeString(timestamp));
                    multiRows.put(oneRow);
                    transCursor.moveToNext();
                }

                data.put("myAccessibility",multiRows);

            }else{

            }
        } catch(JSONException e){
            e.printStackTrace();
        }
        catch(NullPointerException e){
            e.printStackTrace();
        }
        finally {
            if(transCursor != null){
                transCursor.close();
            }
        }

    }

    private void storeMobileCrowdsource(JSONObject data){

        Log.d(TAG, "storeMobileCrowdsource");

        try {
            JSONArray multiRows = new JSONArray();
            Cursor transCursor = null;
            transCursor = db.mobileCrowdsourceDataRecordDao().getUnsyncedData(0,nowSentHour, lastSentHour);

            int rows = transCursor.getCount();

            Log.d(TAG, "rows : "+rows);

            if(rows!=0){
                transCursor.moveToFirst();
                for(int i=0;i<rows;i++) {
                    // columne 1 : id
                    JSONObject oneRow = new JSONObject();
                    Long creationTime = transCursor.getLong(1);
                    String app = transCursor.getString(2);

                    String ifSentNoti = transCursor.getString(3);
                    String startTasktime = transCursor.getString(4);
                    String endTasktime = transCursor.getString(5);
                    String userActions = transCursor.getString(6);
                    Integer accessId = transCursor.getInt(7);


                    oneRow.put("timestamp",creationTime);
                    oneRow.put("app",app);
                    oneRow.put("if_clicked_noti",ifSentNoti);
                    oneRow.put("start_task_time",startTasktime);
                    oneRow.put("end_task_time",endTasktime);
                    oneRow.put("user_actions",userActions);
                    oneRow.put("related_id",accessId);
                    oneRow.put("detect_time",ScheduleAndSampleManager.getTimeString(creationTime));

                    multiRows.put(oneRow);
                    transCursor.moveToNext();
                }

                //    mobileCrowdsourceAndtimestampsJson.put("tasktype_cols",tasktype_cols);

                data.put("MobileCrowdsource",multiRows);

            }

        }catch (JSONException e){
            e.printStackTrace();
        }catch(NullPointerException e){
            e.printStackTrace();
        }

        Log.d(TAG,"data : "+ data.toString());

    }

    private void storeTransporatation(JSONObject data, Long data_hour){

        Log.d(TAG, "storeTransporatation");
        Cursor transCursor = null;
        try {

            JSONArray multiRows = new JSONArray();

            transCursor = db.transportationModeDataRecordDao().getUnsyncedData(0, data_hour);

            int rows = transCursor.getCount();


            if(rows!=0){
                transCursor.moveToFirst();
                for(int i=0;i<rows;i++) {
                    JSONObject oneRow = new JSONObject();
                    Long timestamp = transCursor.getLong(1);
                    String transportation = transCursor.getString(2);
                    String SessionID = transCursor.getString(8);
                    String PhoneSessionID = String.valueOf(transCursor.getLong(9));
                    String ScreenShot = transCursor.getString(10);
                    String ImageName = transCursor.getString(11);
//                    String AccessibilityUrl = transCursor.getString(12);
//                    String NotificationUrl = transCursor.getString(13);

                    Log.d(TAG,"transportation : "+transportation+" timestamp : "+timestamp);

                    oneRow.put("PhoneSessionID",PhoneSessionID);
                    oneRow.put("AppSessionID",SessionID);
                    oneRow.put("ScreenShot",ScreenShot);
                    oneRow.put("ImageName",ImageName);
//                    oneRow.put("AccessibilityUrl",AccessibilityUrl);
//                    oneRow.put("Notification",NotificationUrl);
                    oneRow.put("transportation",transportation);
                    oneRow.put("timestamp",timestamp);
                    oneRow.put("detect_time",ScheduleAndSampleManager.getTimeString(timestamp));
                    multiRows.put(oneRow);
                    transCursor.moveToNext();
                }

                data.put("TransportationMode",multiRows);

            }
        }catch (JSONException e){
            e.printStackTrace();
        }catch(NullPointerException e){
            e.printStackTrace();
        }
        finally {
            if(transCursor != null){
                transCursor.close();
            }
        }
        Log.d(TAG,"data : "+ data.toString());

    }

    private void storeLocation(JSONObject data, Long data_hour){
        Cursor transCursor = null;
        try {
            JSONArray multiRows = new JSONArray();

            transCursor = db.locationDataRecordDao().getUnsyncedData(0,data_hour);

            int rows = transCursor.getCount();

            Log.d(TAG, "rows : "+rows);

            if(rows!=0){
                transCursor.moveToFirst();
                for(int i=0;i<rows;i++) {
                    JSONObject oneRow = new JSONObject();
                    Long timestamp = transCursor.getLong(1);
                    String latitude = transCursor.getString(3);
                    String longtitude = transCursor.getString(4);
                    String accuracy = transCursor.getString(5);
                    String SessionID = transCursor.getString(2);
                    String PhoneSessionID = String.valueOf(transCursor.getLong(12));
                    String ScreenShot = transCursor.getString(13);

                    Log.d(TAG,"timestamp : "+timestamp+" latitude : "+latitude+" longtitude : "+longtitude+" accuracy : "+accuracy);
                    oneRow.put("PhoneSessionID",PhoneSessionID);
                    oneRow.put("AppSessionID",SessionID);
                    oneRow.put("ScreenShot",ScreenShot);
                    oneRow.put("accuracy",accuracy);
                    oneRow.put("latitude",latitude);
                    oneRow.put("longtitude",longtitude);
                    oneRow.put("timestamp",timestamp);
                    oneRow.put("detect_time",ScheduleAndSampleManager.getTimeString(timestamp));
                    multiRows.put(oneRow);
                    transCursor.moveToNext();
                }

                data.put("Location",multiRows);

            }
        }catch (JSONException e){
            e.printStackTrace();
        }catch(NullPointerException e){
            e.printStackTrace();
        }finally {
            if(transCursor != null){
                transCursor.close();
            }
        }

        Log.d(TAG,"data : "+ data.toString());

    }

    private void storeActivityRecognition(JSONObject data, Long data_hour){
        Cursor transCursor = null;
        try {
            JSONArray multiRows = new JSONArray();

            transCursor = db.activityRecognitionDataRecordDao().getUnsyncedData(0,data_hour);
            int rows = transCursor.getCount();

            if(rows!=0){
                transCursor.moveToFirst();
                for(int i=0;i<rows;i++) {
                    JSONObject oneRow = new JSONObject();
                    Long timestamp = transCursor.getLong(1);
                    if(timestamp>0) {
                        String mostProbableActivity = transCursor.getString(2);
                        String probableActivities = transCursor.getString(3);
                        String Detectedtime = transCursor.getString(4);
                        String SessionID = transCursor.getString(7);
                        String PhoneSessionID = String.valueOf(transCursor.getLong(11));
                        String ScreenShot = transCursor.getString(12);
                        String ImageName = transCursor.getString(13);
//                        String AccessibilityUrl = transCursor.getString(14);
//                        String NotificationUrl = transCursor.getString(15);

                        oneRow.put("PhoneSessionID",PhoneSessionID);
                        oneRow.put("AppSessionID",SessionID);
                        oneRow.put("ScreenShot",ScreenShot);
                        oneRow.put("ImageName",ImageName);
//                        oneRow.put("AccessibilityUrl",AccessibilityUrl);
//                        oneRow.put("Notification",NotificationUrl);
                        oneRow.put("most_probable_activity",mostProbableActivity);
                        oneRow.put("probable_activities",probableActivities);
                        oneRow.put("detected_time",Detectedtime);
                        oneRow.put("timestamp",timestamp);
                        oneRow.put("detect_time",ScheduleAndSampleManager.getTimeString(timestamp));

                        multiRows.put(oneRow);

                    }else{
                        db.activityRecognitionDataRecordDao().updateDataStatus(timestamp, DATA_SYNCED_WITH_SERVER);
                    }

                    transCursor.moveToNext();
                }

                data.put("ActivityRecognition",multiRows);

            }

        }catch (JSONException e){
            e.printStackTrace();
        }catch(NullPointerException e){
            e.printStackTrace();
        }
        finally {
            if(transCursor != null){
                transCursor.close();
            }
        }
        Log.d(TAG,"data : "+ data.toString());

    }

    private void storeRinger(JSONObject data, Long data_hour){
        Cursor transCursor = null;
        Log.d(TAG, "storeRinger");

        try {
            JSONArray multiRows = new JSONArray();
            transCursor = db.ringerDataRecordDao().getUnsyncedData(0,data_hour);
            int rows = transCursor.getCount();

//            Log.d(TAG, "rows : "+rows);

            if(rows!=0){
                transCursor.moveToFirst();
                for(int i=0;i<rows;i++) {
                    JSONObject oneRow = new JSONObject();

                    Long timestamp = transCursor.getLong(1);
                    String RingerMode = transCursor.getString(2);
                    String AudioMode = transCursor.getString(3);
                    String SessionID = transCursor.getString(9) ;
                    String PhoneSessionID = String.valueOf(transCursor.getLong(12));
                    String ScreenShot = transCursor.getString(13);
                    String ImageName = transCursor.getString(14);
//                    String AccessibilityUrl = transCursor.getString(15);
//                    String NotificationUrl = transCursor.getString(16);

                    oneRow.put("PhoneSessionID",PhoneSessionID);
                    oneRow.put("AppSessionID",SessionID);
                    oneRow.put("ScreenShot",ScreenShot);
                    oneRow.put("ImageName",ImageName);
//                    oneRow.put("AccessibilityUrl",AccessibilityUrl);
//                    oneRow.put("Notification",NotificationUrl);
                    oneRow.put("ringer_mode",RingerMode);
                    oneRow.put("audio_mode",AudioMode);
                    oneRow.put("timestamp",timestamp);
                    oneRow.put("detect_time",ScheduleAndSampleManager.getTimeString(timestamp));
                    multiRows.put(oneRow);
                    transCursor.moveToNext();
                }
                data.put("Ringer",multiRows);

            }
        }catch (JSONException e){
            e.printStackTrace();
        }catch(NullPointerException e){
            e.printStackTrace();
        }
        finally {
            if(transCursor != null){
                transCursor.close();
            }
        }
        Log.d(TAG,"data : "+ data.toString());

    }

    private void storeConnectivity(JSONObject data, Long data_hour){
        Cursor transCursor = null;
        try {

            JSONArray multiRows = new JSONArray();

            transCursor = db.connectivityDataRecordDao().getUnsyncedData(0,data_hour);

            int rows = transCursor.getCount();

            if(rows!=0){
                transCursor.moveToFirst();
                for(int i=0;i<rows;i++) {
                    JSONObject oneRow = new JSONObject();
                    Long timestamp = transCursor.getLong(1);
                    String NetworkType = transCursor.getString(2);
                    String IsNetworkAvailable = transCursor.getString(3);
                    String IsConnected = transCursor.getString(4);
                    String IsWifiAvailable = transCursor.getString(5);
                    String IsMobileAvailable = transCursor.getString(6);
                    String IsWifiConnected = transCursor.getString(7);
                    String IsMobileConnected = transCursor.getString(8);
                    String SessionID = transCursor.getString(9);
                    String PhoneSessionID = String.valueOf(transCursor.getLong(12));
                    String ScreenShot = transCursor.getString(13);
                    String ImageName = transCursor.getString(14);
//                    String AccessibilityUrl = transCursor.getString(15);
//                    String NotificationUrl = transCursor.getString(16);
//                    Log.d(TAG,"timestamp : "+timestamp+" NetworkType : "+NetworkType+" IsNetworkAvailable : "+IsNetworkAvailable
//                            +" IsConnected : "+IsConnected+" IsWifiAvailable : "+IsWifiAvailable
//                            +" IsMobileAvailable : "+IsMobileAvailable +" IsWifiConnected : "+IsWifiConnected
//                            +" IsMobileConnected : "+IsMobileConnected);
                    oneRow.put("PhoneSessionID",PhoneSessionID);
                    oneRow.put("AppSessionID",SessionID);
                    oneRow.put("ScreenShot",ScreenShot);
                    oneRow.put("ImageName",ImageName);
//                    oneRow.put("AccessibilityUrl",AccessibilityUrl);
//                    oneRow.put("Notification",NotificationUrl);
                    oneRow.put("is_mobile_connected",IsMobileConnected);
                    oneRow.put("is_wifi_connected",IsWifiConnected);
                    oneRow.put("is_mobile_available",IsMobileAvailable);
                    oneRow.put("is_wifi_available",IsWifiAvailable);
                    oneRow.put("is_connected",IsConnected);
                    oneRow.put("is_network_available",IsNetworkAvailable);
                    oneRow.put("network_type",NetworkType);
                    oneRow.put("timestamp",timestamp);
                    oneRow.put("detect_time",ScheduleAndSampleManager.getTimeString(timestamp));
                    multiRows.put(oneRow);

                    transCursor.moveToNext();
                }

                data.put("Connectivity",multiRows);

            }
        }catch (JSONException e){
            e.printStackTrace();
        }catch(NullPointerException e){
            e.printStackTrace();
        }
        finally {
            if(transCursor != null){
                transCursor.close();
            }
        }
        Log.d(TAG,"data : "+ data.toString());

    }

    private void storeBattery(JSONObject data, Long data_hour){

        Log.d(TAG, "storeBattery");
        Cursor transCursor = null;
        try {
            JSONArray multiRows = new JSONArray();

            transCursor = db.batteryDataRecordDao().getUnsyncedData(0,data_hour);

            int rows = transCursor.getCount();

            if(rows!=0){
                transCursor.moveToFirst();
                for(int i=0;i<rows;i++) {
                    JSONObject oneRow = new JSONObject();
                    Long timestamp = transCursor.getLong(1);
                    String BatteryLevel = transCursor.getString(2);
                    String BatteryPercentage = transCursor.getString(3);
                    String BatteryChargingState = transCursor.getString(4);
                    String isCharging = transCursor.getString(5);
                    String SessionID = transCursor.getString(6);
                    String PhoneSessionID = String.valueOf(transCursor.getLong(10));
                    String ScreenShot = transCursor.getString(11);
                    String ImageName = transCursor.getString(12);
//                    String AccessibilityUrl = transCursor.getString(13);
//                    String NotificationUrl = transCursor.getString(14);

                    oneRow.put("PhoneSessionID",PhoneSessionID);
                    oneRow.put("AppSessionID",SessionID);
                    oneRow.put("ScreenShot",ScreenShot);
                    oneRow.put("ImageName",ImageName);
//                    oneRow.put("AccessibilityUrl",AccessibilityUrl);
//                    oneRow.put("Notification",NotificationUrl);
                    oneRow.put("timestamp",timestamp);
                    oneRow.put("battery_level",BatteryLevel);
                    oneRow.put("battery_percentage",BatteryPercentage);
                    oneRow.put("battery_charging_state",BatteryChargingState);
                    oneRow.put("is_charging",isCharging);
                    oneRow.put("detect_time",ScheduleAndSampleManager.getTimeString(timestamp));
                    multiRows.put(oneRow);
                    transCursor.moveToNext();
                }
                data.put("Battery",multiRows);
            }
        }catch (JSONException e){
            e.printStackTrace();
        }catch(NullPointerException e){
            e.printStackTrace();
        }
        finally {
            if(transCursor != null){
                transCursor.close();
            }
        }
//        Log.d(TAG,"data : "+ data.toString());

    }

    private void storeAppUsage(JSONObject data, Long data_hour){

        Log.d(TAG, "storeAppUsage");
        Cursor transCursor = null;
        try {
            JSONArray multiRows = new JSONArray();

            transCursor = db.appUsageDataRecordDao().getUnsyncedData(0,data_hour);
            int rows = transCursor.getCount();

//            Log.d(TAG, "rows : "+rows);

            if(rows!=0){
                transCursor.moveToFirst();
                for(int i=0;i<rows;i++) {
                    JSONObject oneRow = new JSONObject();
                    Long timestamp = transCursor.getLong(1);
                    String ScreenStatus = transCursor.getString(2);
                    String Latest_Used_App = transCursor.getString(3);
                    String Latest_Used_App_Time = transCursor.getString(4);
                    String SessionID = transCursor.getString(5);
                    String PhoneSessionID = String.valueOf(transCursor.getLong(9));
                    String ScreenShot = transCursor.getString(10);
                    String ImageName = transCursor.getString(11);
//                    String AccessibilityUrl = transCursor.getString(12);
//                    String NotificationUrl = transCursor.getString(13);

//                    Log.d(TAG,"timestamp : "+timestamp+" ScreenStatus : "+ScreenStatus+" Latest_Used_App : "+Latest_Used_App+" Latest_Foreground_Activity : "+Latest_Foreground_Activity);

                    oneRow.put("PhoneSessionID",PhoneSessionID);
                    oneRow.put("AppSessionID",SessionID);
                    oneRow.put("ScreenShot",ScreenShot);
                    oneRow.put("ImageName",ImageName);
//                    oneRow.put("AccessibilityUrl",AccessibilityUrl);
//                    oneRow.put("Notification",NotificationUrl);
                    oneRow.put("timestamp",timestamp);
                    oneRow.put("screen_status",ScreenStatus);
                    oneRow.put("latest_used_app",Latest_Used_App);
                    oneRow.put("latest_used_app_time",Latest_Used_App_Time);
                    oneRow.put("detect_time",ScheduleAndSampleManager.getTimeString(timestamp));
                    multiRows.put(oneRow);
                    transCursor.moveToNext();
                }


                data.put("AppUsage",multiRows);

            }
        }catch (JSONException e){
            e.printStackTrace();
        }catch(NullPointerException e){
            e.printStackTrace();
        }
        finally {
            if(transCursor != null){
                transCursor.close();
            }
        }
//        Log.d(TAG,"data : "+ data.toString());

    }

    private void storeTelephony(JSONObject data, Long data_hour){
        Cursor transCursor = null;
        try {
            JSONArray multiRows = new JSONArray();
            JSONObject telephonyAndtimestampsJson = new JSONObject();
            transCursor = db.telephonyDataRecordDao().getUnsyncedData(0,data_hour);

            int rows = transCursor.getCount();

            if(rows!=0){
                transCursor.moveToFirst();
                for(int i=0;i<rows;i++) {
                    JSONObject oneRow = new JSONObject();
                    Long timestamp = transCursor.getLong(1);
                    String NetworkOperatorName = transCursor.getString(2);
                    String CallState = transCursor.getString(3);
                    String PhoneSignalType = transCursor.getString(4);
                    String GsmSignalStrength = transCursor.getString(5);
                    String LTESignalStrength = transCursor.getString(6);
                    String CdmaSignalStrengthLevel = transCursor.getString(7);
                    String SessionID = transCursor.getString(8);
                    String PhoneSessionID = String.valueOf(transCursor.getLong(11));
                    String ScreenShot = transCursor.getString(12);
                    String ImageName = transCursor.getString(13);
//                    String AccessibilityUrl = transCursor.getString(14);
//                    String NotificationUrl = transCursor.getString(15);
//                    Log.d(TAG,"timestamp : "+timestamp+" NetworkOperatorName : "+NetworkOperatorName+" CallState : "+CallState+" PhoneSignalType : "+PhoneSignalType+" GsmSignalStrength : "+GsmSignalStrength+" LTESignalStrength : "+LTESignalStrength+" CdmaSignalStrengthLevel : "+CdmaSignalStrengthLevel );

                    oneRow.put("PhoneSessionID",PhoneSessionID);
                    oneRow.put("AppSessionID",SessionID);
                    oneRow.put("ScreenShot",ScreenShot);
                    oneRow.put("ImageName",ImageName);
//                    oneRow.put("AccessibilityUrl",AccessibilityUrl);
//                    oneRow.put("Notification",NotificationUrl);
                    oneRow.put("network_operator_name",NetworkOperatorName);
                    oneRow.put("call_state",CallState);
                    oneRow.put("phone_signal_type",PhoneSignalType);
                    oneRow.put("lte_signal_strength",LTESignalStrength);
                    oneRow.put("timestamp",timestamp);
                    oneRow.put("detect_time",ScheduleAndSampleManager.getTimeString(timestamp));
                    multiRows.put(oneRow);
                    transCursor.moveToNext();
                }
                data.put("Telephony",multiRows);
            }
        }catch (JSONException e){
            e.printStackTrace();
        }catch(NullPointerException e){
            e.printStackTrace();
        }
        finally {
            if(transCursor != null){
                transCursor.close();
            }
        }
//        Log.d(TAG,"data : "+ data.toString());

    }

    private void storeSensor(JSONObject data, Long data_hour){

//        Log.d(TAG, "storeSensor");
        Cursor transCursor = null;
        try {
            JSONArray multiRows = new JSONArray();

            transCursor = db.sensorDataRecordDao().getUnsyncedData(0,data_hour);
            int rows = transCursor.getCount();

//            Log.d(TAG, "rows : "+rows);
            if(rows!=0){
                transCursor.moveToFirst();
                for(int i=0;i<rows;i++) {
                    JSONObject oneRow = new JSONObject();
                    Long timestamp = transCursor.getLong(1);
                    String PROXIMITY = transCursor.getString(2);
                    String LIGHT = transCursor.getString(3);
                    String SessionID = transCursor.getString(6);
                    String PhoneSessionID = String.valueOf(transCursor.getLong(7));
                    String ScreenShot = transCursor.getString(8);
                    String ImageName = transCursor.getString(9);
//                    String AccessibilityUrl = transCursor.getString(10);
//                    String NotificationUrl = transCursor.getString(11);

                    oneRow.put("PhoneSessionID",PhoneSessionID);
                    oneRow.put("AppSessionID",SessionID);
                    oneRow.put("ScreenShot",ScreenShot);
                    oneRow.put("ImageName",ImageName);
//                    oneRow.put("AccessibilityUrl",AccessibilityUrl);
//                    oneRow.put("Notification",NotificationUrl);
                    oneRow.put("light",LIGHT);
                    oneRow.put("proximity",PROXIMITY);
                    oneRow.put("timestamp",timestamp);
                    oneRow.put("detect_time",ScheduleAndSampleManager.getTimeString(timestamp));

                    multiRows.put(oneRow);
                    transCursor.moveToNext();
                }

                data.put("Sensor",multiRows);

            }
        }catch (JSONException e){
            e.printStackTrace();
        }catch(NullPointerException e){
            e.printStackTrace();
        }
        finally {
            if(transCursor != null){
                transCursor.close();
            }
        }
//        Log.d(TAG,"data : "+ data.toString());

    }

    private void storeNotification(JSONObject data, Long data_hour){

        Log.d(TAG, "storeNotification");
        Cursor transCursor = null;
        try {
            JSONArray multiRows = new JSONArray();

            transCursor = db.notificationDataRecordDao().getUnsyncedData(0,data_hour);

            int rows = transCursor.getCount();

            Log.d(TAG, "rows : "+rows);

            if(rows!=0){
                transCursor.moveToFirst();
                for(int i=0;i<rows;i++) {
                    JSONObject oneRow = new JSONObject();
                    Long timestamp = transCursor.getLong(1);
                    String title_col = transCursor.getString(2);
                    String n_text_col = transCursor.getString(3);
                    String subText_col = transCursor.getString(4);
                    String tickerText_col = transCursor.getString(5);
                    String app_col = transCursor.getString(6);
//                    Integer relatedId = transCursor.getInt(7);
                    String reason = transCursor.getString(8);
                    String SessionID = transCursor.getString(11);
                    String PhoneSessionID = String.valueOf(transCursor.getLong(12));
                    String ScreenShot = transCursor.getString(13);
                    String ImageName = transCursor.getString(14);
//                    Integer SenderID = transCursor.getInt(15);
//                    String AccessibilityUrl = transCursor.getString(15);
//                    String NotificationUrl = transCursor.getString(16);

                    oneRow.put("PhoneSessionID",PhoneSessionID);
                    oneRow.put("AppSessionID",SessionID);
                    oneRow.put("ScreenShot",ScreenShot);
                    oneRow.put("ImageName",ImageName);
//                    oneRow.put("SenderID",SenderID);
//                    oneRow.put("AccessibilityUrl",AccessibilityUrl);
//                    oneRow.put("Notification",NotificationUrl);
                    oneRow.put("title",title_col);
                    oneRow.put("text",n_text_col);
                    oneRow.put("subtext",subText_col);
                    oneRow.put("tickertext",tickerText_col);
                    oneRow.put("app",app_col);
                    oneRow.put("reason",reason);
                    oneRow.put("timestamp",timestamp);
                    oneRow.put("detect_time",ScheduleAndSampleManager.getTimeString(timestamp));
                    multiRows.put(oneRow);

                    transCursor.moveToNext();
                }

                data.put("Notification",multiRows);

            }
        }catch (JSONException e){
            e.printStackTrace();
        }catch(NullPointerException e){
            e.printStackTrace();
        }
        finally {
            if(transCursor != null){
                transCursor.close();
            }
        }
    }


    private void storeQuestionnaireAnswer(JSONObject data, Long data_hour){
        Log.d(TAG, "storeQuestionnaire");
        Cursor transCursor = null;
        Cursor RelatedCursor = null;
        Cursor QuestionCursor = null;
        Cursor MetaDataCursor = null;

        try {
            JSONArray multiRows = new JSONArray();

            transCursor = db.finalAnswerDao().getUnsyncedData(0,data_hour);

            Set<String> quesionnaireId = new LinkedHashSet<>();

            int rows = transCursor.getCount();
            if(rows!=0){
                transCursor.moveToFirst();
                for(int i = 0; i < rows; i++){
                    String relatedId = transCursor.getString(2);
                    quesionnaireId.add(relatedId);
                    transCursor.moveToNext();
                }
            }

            Iterator<String> linkedSetIntIt = quesionnaireId.iterator();
            while(linkedSetIntIt.hasNext()) {
                JSONObject oneRow = new JSONObject();
                String relatedid = linkedSetIntIt.next();

                RelatedCursor = db.finalAnswerDao().getUnSyncedDataWithRelatedid(0,data_hour, relatedid);
                MetaDataCursor = db.finalAnswerDao().getUnSyncedDataWithRelatedid(0,data_hour, relatedid);
                rows = RelatedCursor.getCount();
                JSONArray Questions = new JSONArray();
                JSONObject Question = new JSONObject();
                if(rows!=0){
                    RelatedCursor.moveToFirst();
                    MetaDataCursor.moveToLast();

                    String relatedId = MetaDataCursor.getString(2);
                    Long respondTime = MetaDataCursor.getLong(11);
                    Long submitTime = MetaDataCursor.getLong(12);
                    Long generateTime = MetaDataCursor.getLong(14);
                    String isFinish = MetaDataCursor.getString(13);
                    String ReplyCount = MetaDataCursor.getString(15);
                    String TotalCount = MetaDataCursor.getString(16);
                    String Questionnaire_type = MetaDataCursor.getString(17);

                    oneRow.put("questionnaire_id",relatedId);
                    oneRow.put("isGenerate", generateTime);//ESM發送時間
                    oneRow.put("generateTime", ScheduleAndSampleManager.getTimeString(generateTime));
                    oneRow.put("isRespond", respondTime);//打開ESM時間
                    oneRow.put("respondTime", ScheduleAndSampleManager.getTimeString(respondTime));
                    oneRow.put("isSubmit", submitTime);
                    oneRow.put("submitTime",ScheduleAndSampleManager.getTimeString(submitTime));
                    oneRow.put("isFinish",isFinish);
                    oneRow.put("ReplyCount",ReplyCount);
                    oneRow.put("TotalCount",TotalCount);
                    oneRow.put("QuestionnaireType",Questionnaire_type);
                    for(int i=0;i<rows; i++) {
                        Log.d(TAG, "i = " + i);
                        String questionId = RelatedCursor.getString(3);
                        QuestionCursor = db.finalAnswerDao().getUnSyncedDataWithQuestionid(0,data_hour, relatedid, questionId);
                        int q_rows = QuestionCursor.getCount();
                        if(q_rows != 0){
                            QuestionCursor.moveToFirst();
                            Question = new JSONObject();
                            Long timestamp = QuestionCursor.getLong(1);
                            String detectedTime = QuestionCursor.getString(7);
                            String answerId = QuestionCursor.getString(4);

                            Question.put("question_id",questionId);
                            Question.put("timestamp",timestamp);
                            Question.put("detected_time",detectedTime);
                            String answers = "";
                            for(int j = 0; j < q_rows; j++){
                                String answerChoicePossForCheck = QuestionCursor.getString(5);
                                String answerChoiceState = QuestionCursor.getString(6);
                                String ansChoice = QuestionCursor.getString(8);
                                if(ansChoice.equals("Cue Recall") || ansChoice.equals("查證或搜尋則數") || ansChoice.equals("轉傳新聞數量") ||
                                        ansChoice.equals("查證新聞標題") || (ansChoice.contains("重要事件") &&
                                        !ansChoice.contains("對這個主題沒有")) || ansChoice.contains("沒有關聯～極度相關")){
                                    answers = answers + answerChoiceState + "   ";
                                }else if (ansChoice.equals("其他")){
                                    answers = answers + answerChoicePossForCheck + "   ";
                                }
                                else if(ansChoice.contains("小時")){
                                    answers = answers + answerChoiceState + "小時";
                                }
                                else if(ansChoice.contains("分鐘")){
                                    answers = answers + answerChoiceState + "分鐘" + "   ";
                                }
                                else {
                                    answers = answers + ansChoice + "   ";
                                }
                                Log.d(TAG, "Answer: " + answers);
                                QuestionCursor.moveToNext();
                                RelatedCursor.moveToNext();
                                i++;
                            }
                            i--;
                            Question.put("answer_choice", answers);
                        }
                        Questions.put(Question);
                    }
                }
                oneRow.put("Questions", Questions);
                multiRows.put(oneRow);
            }
            if(multiRows.length() != 0) {
                data.put("QuestionnaireAns", multiRows);
            }

//            Log.d(TAG, "rows : "+rows);
//
//            if(rows!=0){
//                transCursor.moveToFirst();
//                String last_relatedID = "-1";
//                boolean NextQuestionnaire = false;
//                String last_questionID = "-1";
//                boolean NextQuestion = false;
//                String answers = "";
//
//
//                for(int i=0;i<rows;i++) {
//                    String relatedId = transCursor.getString(2);
//                    Long timestamp = transCursor.getLong(1);
//                    String questionId = transCursor.getString(3);
//                    String answerId = transCursor.getString(4);
//                    String answerChoicePossForCheck = transCursor.getString(5);
//                    String answerChoiceState = transCursor.getString(6);
//                    String detectedTime = transCursor.getString(7);
//                    String ansChoice = transCursor.getString(8);
//                    Long respondTime = transCursor.getLong(11);
//                    Long submitTime = transCursor.getLong(12);
//                    boolean isFinish = transCursor.getInt(13) > 0;
//
//                    if(!relatedId.equals(last_relatedID)) {
//                        NextQuestionnaire = true;
//                        if(!last_relatedID.equals("-1")) {
//                            oneRow.put("Questions", Questions);
//                            multiRows.put(oneRow);
//                            Log.d("QuestionnaireAns","OneRow: " + oneRow.toString());
//                            oneRow = new JSONObject();
//                            last_questionID = "-1";
//                        }
//                        Log.d("QuestionnaireAns","Related id: " + relatedId);
//                        last_relatedID = relatedId;
//                    }
//                    if(NextQuestionnaire){
//                        oneRow.put("questionnaire_id",relatedId);
//                        oneRow.put("respondTime",respondTime);
//                        oneRow.put("submitTime",submitTime);
//                        oneRow.put("isFinish",isFinish);
//                        Log.d("QuestionnaireAns","OneRow: " + oneRow.toString());
//                        NextQuestionnaire = false;
//                    }
//
//                    if(!questionId.equals(last_questionID)) {
//                        NextQuestion = true;
//                        if(!last_questionID.equals("-1")) {
//                            Question.put("answer_choice", answers);// Q15答案
//                            Log.d("QuestionnaireAns","Question: " + Question.toString());
//                            Questions.put(Question);
//                            Log.d("QuestionnaireAns","Questions: " + Questions.toString());
//                            Question = new JSONObject();
//                            answers = "";
//                        }
//                        Log.d("QuestionnaireAns","Question id: " + questionId);
//                        last_questionID = questionId;
//                    }
//                    if(NextQuestion){
//                        Question.put("question_id",questionId);
//                        Question.put("timestamp",timestamp);
//                        Question.put("detected_time",ScheduleAndSampleManager.getTimeString(timestamp));
//                        Log.d("QuestionnaireAns","Question id: " + questionId);
//                        NextQuestion = false;
//                    }
//
//                    if(ansChoice.equals("Cue Recall")){
//                        answers = answers + answerChoiceState + "   ";
//                    }else {
//                        answers = answers + ansChoice + "   ";
//                    }
//                    //Question.put("answer_choice_state",answerChoiceState);
//                    //multiRows.put(oneRow);
//
//                    transCursor.moveToNext();
//                }
//                data.put("QuestionnaireAns",multiRows);
//                Log.d("QuestionnaireAns",multiRows.toString());
//            }
        }catch (JSONException e){
            e.printStackTrace();
        }catch(NullPointerException e){
            e.printStackTrace();
        }
        finally {
            if(transCursor != null){
                transCursor.close();
            }
            if(RelatedCursor != null){
                RelatedCursor.close();
            }
            if(QuestionCursor != null){
                QuestionCursor.close();
            }
        }
    }
    private void storeResponse(JSONObject data){
        try {

            JSONArray multiRows = new JSONArray();
            JSONObject responseAndtimestampsJson = new JSONObject();

            Cursor transCursor = null;
            transCursor = db.repsonseDataRecordDao().getUnsyncedData(0,nowSentHour, lastSentHour);


            int rows = transCursor.getCount();


            if(rows!=0){
                transCursor.moveToFirst();
                for(int i=0;i<rows;i++) {
                    JSONObject oneRow = new JSONObject();

                    Long timestamp = transCursor.getLong(1);
                    String relatedId = transCursor.getString(2);
                    String qGenerateTime = transCursor.getString(3);
                    String  type = transCursor.getString(4);
                    String startAnswerTime = transCursor.getString(5);
                    String finishTime = transCursor.getString(6);
                    String ifComplete = transCursor.getString(7);

//                    Log.d(TAG,"timestamp : "+timestamp+" NetworkOperatorName : "+NetworkOperatorName+" CallState : "+CallState+" PhoneSignalType : "+PhoneSignalType+" GsmSignalStrength : "+GsmSignalStrength+" LTESignalStrength : "+LTESignalStrength+" CdmaSignalStrengthLevel : "+CdmaSignalStrengthLevel );

                    oneRow.put("related_id",relatedId);
                    oneRow.put("q_generate_time",qGenerateTime);
                    oneRow.put("type",type);
                    oneRow.put("start_answer_time",startAnswerTime);
                    oneRow.put("finish_time",finishTime);
                    oneRow.put("if_complete",ifComplete);
                    oneRow.put("readable",getReadableTimeLong(timestamp));
                    oneRow.put("timestamp",timestamp);

                    multiRows.put(oneRow);
                    transCursor.moveToNext();
                }

                data.put("Response",multiRows);

            }
        }catch (JSONException e){
            e.printStackTrace();
        }catch(NullPointerException e){
            e.printStackTrace();
        }

//        Log.d(TAG,"data : "+ data.toString());

    }


    public void refreshAllContent(final long timetoupdate) {
        new CountDownTimer(timetoupdate, 1000) {
            public void onTick(long millisUntilFinished) {
            }
            public void onFinish() {
                Log.i("SCROLLS ", "UPDATE CONTENT HERE ");
                sendingDumpData();
            }
        }.start();
    }
    public void refreshUser(final long timetoupdate) {
        new CountDownTimer(timetoupdate, 1000) {
            public void onTick(long millisUntilFinished) {
            }
            public void onFinish() {
                Log.i("SCROLLS ", "UPDATE CONTENT HERE ");
                sendingUserData();
            }
        }.start();
    }

}
