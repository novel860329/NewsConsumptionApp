package labelingStudy.nctu.minuku.streamgenerator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;

import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import labelingStudy.nctu.minuku.DBHelper.appDatabase;
import labelingStudy.nctu.minuku.Utilities.CSVHelper;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minuku.manager.MinukuStreamManager;
import labelingStudy.nctu.minuku.model.DataRecord.ConnectivityDataRecord;
import labelingStudy.nctu.minuku.stream.ConnectivityStream;
import labelingStudy.nctu.minukucore.exception.StreamAlreadyExistsException;
import labelingStudy.nctu.minukucore.exception.StreamNotFoundException;
import labelingStudy.nctu.minukucore.stream.Stream;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;
import static labelingStudy.nctu.minuku.Utilities.CSVHelper.CSV_Img;
import static labelingStudy.nctu.minuku.config.Constants.PICTURE_DIRECTORY_PATH;
import static labelingStudy.nctu.minuku.config.Constants.URL_SAVE_IMG;

/**
 * Created by Lawrence on 2017/8/22.
 */

public class ConnectivityStreamGenerator extends AndroidStreamGenerator<ConnectivityDataRecord> {

    private final String TAG = "ConnectivityStreamGenerator";
    ConnectivityStreamGenerator mConnectivityStreamGenerator;

    private Context mContext;
    private String[] esm_image;
    public static String NETWORK_TYPE_WIFI = "Wifi";
    public static String NETWORK_TYPE_MOBILE = "Mobile";
//    private String LastImageName = "";
    private static boolean mIsNetworkAvailable = false;
    private static boolean mIsConnected = false;
    private static boolean mIsWifiAvailable = false;
    private static boolean mIsMobileAvailable = false;
    public static boolean mIsWifiConnected = false;
    public static boolean mIsMobileConnected = false;

    public static String mNetworkType = "NA";

    public static int mainThreadUpdateFrequencyInSeconds = 5;
    public static long mainThreadUpdateFrequencyInMilliseconds = mainThreadUpdateFrequencyInSeconds *Constants.MILLISECONDS_PER_SECOND;

    private static Handler mMainThread;

    private static ConnectivityManager mConnectivityManager;

    private ConnectivityStream mStream;

    appDatabase db;
    private SharedPreferences sharedPrefs;

    public ConnectivityStreamGenerator(Context applicationContext){
        super(applicationContext);

        this.mContext = applicationContext;
        this.mStream = new ConnectivityStream(Constants.DEFAULT_QUEUE_SIZE);

        sharedPrefs = mContext.getSharedPreferences(Constants.sharedPrefString, MODE_PRIVATE);

        mConnectivityManager = (ConnectivityManager)mContext.getSystemService(mContext.CONNECTIVITY_SERVICE);
        db = appDatabase.getDatabase(applicationContext);
        this.register();
    }

    @Override
    public void register() {
        Log.d(TAG, "Registering with StreamManager.");
        try {
            MinukuStreamManager.getInstance().register(mStream, ConnectivityDataRecord.class, this);
        } catch (StreamNotFoundException streamNotFoundException) {
            Log.e(TAG, "One of the streams on which ConnectivityDataRecord depends in not found.");
        } catch (StreamAlreadyExistsException streamAlreadyExistsException) {
            Log.e(TAG, "Another stream which provides ConnectivityDataRecord is already registered.");
        }
    }

    @Override
    public Stream<ConnectivityDataRecord> generateNewStream() {
        return mStream;
    }

    @Override
    public boolean updateStream() {

        Log.d(TAG, "Update stream called");
        //        int session_id = SessionManager.getOngoingSessionId();

        long session_id;
        long phone_session_id = sharedPrefs.getLong("Phone_SessionID", 1);
        String screenshot = sharedPrefs.getString("ScreenShot", "0");
        String ImageName = sharedPrefs.getString("CaptureImgName", "");
//        String AccessibilityUrl = sharedPrefs.getString("AccessibilityUrl", "");
//        String NotificationUrl = sharedPrefs.getString("NotificationUrl", "");
        boolean readnews = mContext.getSharedPreferences("test",Context.MODE_PRIVATE).getBoolean("ReadNews",false);
        if(readnews) {
            session_id = sharedPrefs.getLong("SessionID", Constants.INVALID_INT_VALUE);
        }
        else{
            session_id = -1;
        }
        try {
            if (screenshot.equals("0")) {
                if(!ImageName.equals(""))
                    sharedPrefs.edit().putString("CaptureImgName", "").apply();
                ImageName = "";
            }
//            else{
//                if(LastImageName.equals(ImageName)){
//                    ImageName = "";
//                }
//                else{
//                    LastImageName = ImageName;
//                }
//            }
        }catch (Exception e){
            e.printStackTrace();
        }

        if(isOnline(mContext)){
            String esm_img = sharedPrefs.getString("ESM_Image", "");
            esm_image = esm_img.split(",");
            Log.d(TAG, "esm_image: " + esm_img);
            if(!esm_img.equals("")) {
                new UploadESMAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
        ConnectivityDataRecord connectivityDataRecord;
        //TODO get service data
        connectivityDataRecord =
                new ConnectivityDataRecord(mNetworkType, mIsNetworkAvailable, mIsConnected, mIsWifiAvailable,
                        mIsMobileAvailable, mIsWifiConnected, mIsMobileConnected, String.valueOf(session_id),
                        phone_session_id, screenshot, ImageName);

        mStream.add(connectivityDataRecord);
        Log.d(TAG, "CheckFamiliarOrNot to be sent to event bus" + connectivityDataRecord);
        // also post an event.
        EventBus.getDefault().post(connectivityDataRecord);
        try {
            ConnectivityDataRecord lastdata = db.connectivityDataRecordDao().getLastRecord();
            if(lastdata!=null) {
                //ConnectivityDataRecord lastdata = db.connectivityDataRecordDao().getLastRecord();
                if (lastdata.getNetworkType().equals(mNetworkType) && lastdata.getIsNetworkAvailable() == mIsNetworkAvailable
                        && lastdata.getIsConnected() == mIsConnected) {
                } else {
                    String data = mNetworkType + " " + (mIsNetworkAvailable ? 1 : 0) + " " + (mIsConnected ? 1 : 0);
                    CSVHelper.storeToCSV("connect.csv", data);
                }
            }
            db.connectivityDataRecordDao().insertAll(connectivityDataRecord);
        }catch (NullPointerException e){ //Sometimes no data is normal
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public long getUpdateFrequency() {
        return 1;
    }

    @Override
    public void sendStateChangeEvent() {

    }

    @Override
    public void onStreamRegistration() {
        Log.e(TAG,"onStreamRegistration");
        try {
            mConnectivityStreamGenerator = (ConnectivityStreamGenerator) MinukuStreamManager.getInstance().getStreamGeneratorFor(ConnectivityDataRecord.class);
        } catch (StreamNotFoundException e) {
            Log.d(TAG, "Initial MyAccessibility Service Failed");
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        mApplicationContext.registerReceiver(networkChangeReceiver, filter);
        //runPhoneStatusMainThread();
    }

    private BroadcastReceiver networkChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                getNetworkConnectivityUpdate();
            }
        }
    };

    public void runPhoneStatusMainThread(){

        Log.d(TAG, "runPhoneStatusMainThread") ;

        mMainThread = new Handler();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                getNetworkConnectivityUpdate();

                mMainThread.postDelayed(this, mainThreadUpdateFrequencyInMilliseconds);
            }
        };

        mMainThread.post(runnable);
    }

    private void getNetworkConnectivityUpdate(){

        mIsNetworkAvailable = false;
        mIsConnected = false;
        mIsWifiAvailable = false;
        mIsMobileAvailable = false;
        mIsWifiConnected = false;
        mIsMobileConnected = false;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Network[] networks = mConnectivityManager.getAllNetworks();

            NetworkInfo activeNetwork;
            for (Network network : networks) {
                if(network != null) {
                    NetworkCapabilities nc = mConnectivityManager.getNetworkCapabilities(network);
                    if (nc != null) {
                        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
                        if (nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                            mIsMobileAvailable = nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
                            mIsMobileConnected = (mNetworkInfo != null);
                            mIsWifiAvailable = false;
                            mIsWifiConnected = false;
                        }
                        else if (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                            mIsWifiAvailable = nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
                            mIsWifiConnected = (mNetworkInfo != null);
                            mIsMobileAvailable = false;
                            mIsMobileConnected = false;
                        }
                    }
                /*activeNetwork = mConnectivityManager.getNetworkInfo(network);

                //if there is no default network
                if(activeNetwork == null){

                    break;
                }

                if (activeNetwork.getType()== ConnectivityManager.TYPE_WIFI){
                    mIsWifiAvailable = activeNetwork.isAvailable();
                    mIsWifiConnected = activeNetwork.isConnected();
                } else if (activeNetwork.getType()==ConnectivityManager.TYPE_MOBILE){
                    mIsMobileAvailable = activeNetwork.isAvailable();
                    mIsMobileConnected = activeNetwork.isConnected();
                }*/
                }
            }

            if (mIsWifiConnected) {
                mNetworkType = NETWORK_TYPE_WIFI;
            }
            else if (mIsMobileConnected) {
                mNetworkType = NETWORK_TYPE_MOBILE;
            }
            else{
                mNetworkType = "Other";
            }

            mIsNetworkAvailable = mIsWifiAvailable | mIsMobileAvailable;
            mIsConnected = mIsWifiConnected | mIsMobileConnected;


            Log.d(TAG, "[test save records] connectivity change available? WIFI: available " + mIsWifiAvailable  +
                    "  mIsConnected: " + mIsWifiConnected + " Mobile: available: " + mIsMobileAvailable + " mIsconnected: " + mIsMobileConnected
                    +" network type: " + mNetworkType + ",  mIs connected: " + mIsConnected + " mIs network available " + mIsNetworkAvailable);


        }
        else{

            Log.d(TAG, "[test save records] api under lollipop " );


            if (mConnectivityManager!=null) {

                NetworkInfo activeNetworkWifi = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                NetworkInfo activeNetworkMobile = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

                boolean isWiFi = activeNetworkWifi.getType() == ConnectivityManager.TYPE_WIFI;
                boolean isMobile = activeNetworkMobile.getType() == ConnectivityManager.TYPE_MOBILE;

                Log.d(TAG, "[test save records] connectivity change available? " + isWiFi);


                if(activeNetworkWifi !=null) {

                    mIsWifiConnected = activeNetworkWifi != null &&
                            activeNetworkWifi.isConnected();
                    mIsMobileConnected = activeNetworkWifi != null &&
                            activeNetworkMobile.isConnected();

                    mIsConnected = mIsWifiConnected | mIsMobileConnected;

                    mIsWifiAvailable = activeNetworkWifi.isAvailable();
                    mIsMobileAvailable = activeNetworkMobile.isAvailable();

                    mIsNetworkAvailable = mIsWifiAvailable | mIsMobileAvailable;


                    if (mIsWifiConnected) {
                        mNetworkType = NETWORK_TYPE_WIFI;
                    }

                    else if (mIsMobileConnected) {
                        mNetworkType = NETWORK_TYPE_MOBILE;
                    }


                    //assign value
//
                    Log.d(TAG, "[test save records] connectivity change available? WIFI: available " + mIsWifiAvailable  +
                            "  mIsConnected: " + mIsWifiConnected + " Mobile: available: " + mIsMobileAvailable + " mIs connected: " + mIsMobileConnected
                            +" network type: " + mNetworkType + ",  mIs connected: " + mIsConnected + " mIs network available " + mIsNetworkAvailable);

                }
            }
        }

//        if(mConnectivityStreamGenerator!= null)
//            mConnectivityStreamGenerator.updateStream();
    }

    private boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //should check null because in airplane mode it will be null
        return (netInfo != null && netInfo.isConnected());
    }

    private class UploadESMAsync extends AsyncTask<Void, Integer, Integer> {
        protected Integer doInBackground(Void...params) {
            Bitmap bitmap = null;
            String postUrl = URL_SAVE_IMG;
            String UID = sharedPrefs.getString("UserID", Constants.DEVICE_ID);
            for(int i = 0; i < esm_image.length; i++) {
                Log.d(TAG, "ESM questionnaire image:" + esm_image[i]);
                String[] esm_split = esm_image[i].split("/");
                if (esm_split.length >= 4) {
                    String dir = esm_split[esm_split.length - 2];
                    String image_name = dir + "-" + esm_split[esm_split.length - 1];
                    String[] takeDateFormat = image_name.split("-");

                    String imgDateFormat = "";
                    String img = "";
                    String session = "";
                    String trigger = "";

                    if (image_name.contains("Upload")) {
                        imgDateFormat = takeDateFormat[0] + "-" + takeDateFormat[1] + "-" + takeDateFormat[2] + "-" +
                                takeDateFormat[4] + "-" + takeDateFormat[5] + "-" + takeDateFormat[6]; // 9/23
                        img = image_name.substring(11, 26);// Upload-20-10-11
                        session = takeDateFormat[7];
                        if (image_name.contains("crop")) {
                            trigger = takeDateFormat[10].split("\\.")[0];// 9/23
                        } else if (image_name.contains("ESM")) {
                            trigger = takeDateFormat[9].split("\\.")[0];// 9/23
                        } else {
                            trigger = takeDateFormat[8].split("\\.")[0];// 9/23
                        }
                    } else {
                        imgDateFormat = takeDateFormat[0] + "-" + takeDateFormat[1] + "-" + takeDateFormat[2] + "-" +
                                takeDateFormat[3] + "-" + takeDateFormat[4] + "-" + takeDateFormat[5]; // 9/23
                        img = image_name.substring(11, 19);// 10/7
                        session = takeDateFormat[6];
                        if (image_name.contains("crop")) {
                            trigger = takeDateFormat[9].split("\\.")[0];// 9/23
                        } else if (image_name.contains("ESM")) {
                            trigger = takeDateFormat[8].split("\\.")[0];// 9/23
                        } else {
                            trigger = takeDateFormat[7].split("\\.")[0];// 9/23
                        }
                    }

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
                    Date date = null;
                    try {
                        date = dateFormat.parse(imgDateFormat);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    long timestamp = 0L;
                    if (date != null) {
                        timestamp = date.getTime();
                    }

                    Log.d(TAG, image_name + " " + timestamp + " " + dir + " " + img + " " + trigger + " " + session);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.RGB_565;
                    try {
                        // Read BitMap by file path
                        bitmap = BitmapFactory.decodeFile(esm_image[i], options);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        byte[] byteArray = stream.toByteArray();
                        CSVHelper.storeToCSV(CSV_Img, "upload: " + esm_image[i]);
                        MultipartBody.Builder multipartBody = new MultipartBody.Builder().setType(MultipartBody.FORM);

                        multipartBody.addFormDataPart("image", image_name, RequestBody.create(MediaType.parse("image/*jpg"), byteArray))
                                .addFormDataPart("filename", image_name)
                                .addFormDataPart("timestamp", String.valueOf(timestamp))
                                .addFormDataPart("date", dir);
//                                .addFormDataPart("time", img)
//                                .addFormDataPart("trigger", trigger)
//                                .addFormDataPart("session_id", session);
                        RequestBody postBodyImage = multipartBody.addFormDataPart("userid", UID).build();
                        postRequest(postUrl, postBodyImage);
                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    } finally {
                        if (bitmap != null) {
                            bitmap.recycle();
                        }
                    }
                }
            }
            sharedPrefs.edit().putString("ESM_Image", "").apply();
            return null;
        }
        protected void onPreExecute() {
            super.onPreExecute();
        }
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
        }
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
                CSVHelper.storeToCSV(CSV_Img, "img upload error: ");
//                latch.countDown();
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
                        CSVHelper.storeToCSV(CSV_Img, responseName + " upload success");
                        updateImgName(responseName);
                    }
                    else{
                        CSVHelper.storeToCSV(CSV_Img, responseName + " equal 0");
                    }
                }
                else{
                    CSVHelper.storeToCSV(CSV_Img, responseName + " contain upload");
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
            if(imgfile == null) Log.d(TAG, "imgfile is null");

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

    @Override
    public void offer(ConnectivityDataRecord dataRecord) {

    }
}
