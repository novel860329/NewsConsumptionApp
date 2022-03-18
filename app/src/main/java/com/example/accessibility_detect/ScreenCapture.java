package com.example.accessibility_detect;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import labelingStudy.nctu.minuku.DBHelper.appDatabase;
import labelingStudy.nctu.minuku.Utilities.CSVHelper;
import labelingStudy.nctu.minuku.manager.MinukuStreamManager;
import labelingStudy.nctu.minuku.model.DataRecord.NewsDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.SensorDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.SessionDataRecord;
import labelingStudy.nctu.minuku.receiver.NotificationHandleReceiver;
import labelingStudy.nctu.minuku.service.NotificationListenService;
import labelingStudy.nctu.minuku.streamgenerator.SensorStreamGenerator;

import static com.example.accessibility_detect.MyAccessibilityService.Accessibility_ID;
import static labelingStudy.nctu.minuku.config.Constants.ALLIMG;
import static labelingStudy.nctu.minuku.config.Constants.PICTURE_DIRECTORY_PATH;
import static labelingStudy.nctu.minuku.config.Constants.RECORDING_NOTIFICATION_ID;
import static labelingStudy.nctu.minuku.config.Constants.STOP_RECORD;
import static labelingStudy.nctu.minuku.config.SharedVariables.ifRecordingRightNow;

public class ScreenCapture extends Service {

    private final static String TAG = "ScreenShotService";
    private static NotificationListenService notificationListenService = new NotificationListenService();
    MinukuStreamManager streamManager;
    private SessionDataRecord sessionDataRecord;
    private String t = "";
    private Bitmap myBitmap;
    private File directory;

    private Pattern p;
    private Pattern p2;
    private String existfile = "";
    private List<String> web = new ArrayList<>();
    private static Intent resultIntent;
    Handler mMainThread = new Handler();
    public  SensorStreamGenerator sensorStreamGenerator;
    private  static MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;

    private SharedPreferences pref;
    private String date;
    private String time;
    private boolean flag = false;
    private ImageReader imageReader;
    private Handler handler;
    private String TriggerApp = "";
    private long SessionID = 0;
    private long lastid = 0;
    private long ESMinterval = 60 * 60;

    private NewsDataRecord newsDataRecord;
    private appDatabase db;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {

            if(sensorStreamGenerator != null) {
                sensorStreamGenerator.updateStream();
            }
            Log.d(TAG, "Which one is running??");
            mMainThread.postDelayed(this, 5 * 1000);
        }
    };

    public ScreenCapture() {
        Log.d(TAG, "ScreenShotService");
    }

    public class LocalBinder extends Binder {
        public ScreenCapture getService() {
            return ScreenCapture.this;
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "OnCreate Start");
//        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
//        Fabric.with(this, new Crashlytics());
        streamManager = MinukuStreamManager.getInstance();

        CSVHelper.storeToCSV("AccessibilityDetect.csv", "ScreenCapture is start!!");

        pref = getSharedPreferences("test", MODE_PRIVATE);
        pref.edit().putString("ScreenShot", "1").apply();
        PendingIntent stopRecordingPendingIntent = null;
        NotificationManager notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String id = RECORDING_NOTIFICATION_ID;

        p = Pattern.compile("(.*(http|https):)|((http|https):)");



        db = appDatabase.getDatabase(getApplicationContext());

        try {
            sensorStreamGenerator = (SensorStreamGenerator) MinukuStreamManager.getInstance().getStreamGeneratorFor(SensorDataRecord.class);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        Utils.cancel = false;
        CSVHelper.storeToCSV("AccessibilityDetect.csv", "Set cancel flag to " + Utils.cancel);

        TriggerApp = pref.getString("TriggerApp", "");

        Intent stopRecordingIntent = new Intent(this, NotificationHandleReceiver.class);
        stopRecordingIntent.setAction(STOP_RECORD);
        notificationListenService.stopRecordingNotification(this, Accessibility_ID, TriggerApp, true, stopRecordingIntent);
        CSVHelper.storeToCSV("AccessibilityDetect.csv", "Send " + TriggerApp + " screen shot stop notification");

//        storeSession(TriggerApp, "Image");
//        NewsRelated(true);

        SessionID = pref.getLong("SessionID", 0);
        Log.d(TAG, "SharePerference session id:" + SessionID);

        StartProjection();
//        stopRecordingPendingIntent = PendingIntent.getBroadcast(this, requestCodeRecordActionRecording, stopRecordingIntent, PendingIntent.FLAG_CANCEL_CURRENT);
//        NotificationCompat.Action stopRecordingAction = new NotificationCompat.Action.Builder(R.mipmap.ic_launcher, "停止截圖", stopRecordingPendingIntent).build();
//        if (notifManager == null) {
//            notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            int importance = NotificationManager.IMPORTANCE_HIGH;
//            NotificationChannel mChannel = notifManager.getNotificationChannel(id);
//            if (mChannel == null) {
//                mChannel = new NotificationChannel(id, RECORDING_TITLE, importance);
//                mChannel.enableVibration(true);
//                mChannel.setVibrationPattern(new long[]{0, 300});
//                //mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
//                notifManager.createNotificationChannel(mChannel);
//            }
//            AccessibilityBuilder = new NotificationCompat.Builder(this, id);
//
//
//            AccessibilityBuilder.setContentTitle(SCRRENSHOT_TITLE_CONTENT)                             // required
//                    .setSmallIcon(R.drawable.ic_stat_name)   // required
//                    .setContentText("已開始截圖...") // required
//                    .setDefaults(Notification.DEFAULT_ALL)
//                    .setContentIntent(null)
//                    .setOnlyAlertOnce(true)
//                    .setAutoCancel(false)
////                    .addAction(startRecordingAction)
////                    .addAction(stopRecordingAction)
//                    //.setTicker(null)
//                    //.setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
//                    .setVibrate(new long[]{0, 200});
//            //acquireScreenshotPermission();
//        }
//        else {
//            AccessibilityBuilder = new NotificationCompat.Builder(this, id);
//            AccessibilityBuilder.setContentTitle(SCRRENSHOT_TITLE_CONTENT)                            // required
//                    .setSmallIcon(R.drawable.ic_stat_name)   // required
//                    .setContentText("已開始截圖...") // required
//                    .setDefaults(Notification.DEFAULT_ALL)
//                    .setContentIntent(null)
//                    .setOnlyAlertOnce(true)
////                    .addAction(startRecordingAction)
////                    .addAction(stopRecordingAction)
//                    .setVibrate(new long[] {0, 200})
//                    .setAutoCancel(false)
////                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
//                    .setPriority(Notification.PRIORITY_HIGH);
//        }
        Log.d(TAG,"recording title : 已開始截圖");
        Toast.makeText(getApplicationContext(),"開始截圖", Toast.LENGTH_SHORT).show();

//        AccessibilityBuilder.addAction(stopRecordingAction);
//        ScreenCapture_manager.notify(Accessibility_ID, AccessibilityBuilder.build());
    }

    private WindowManager windowManager =null;
    private ImageView igv = null;
    private WindowManager.LayoutParams params;

    private int screenWidth,screenHeight,screenDensity;

    public void StartProjection()
    {
        Log.d(TAG, "initWindow");
        initWindow();

        Log.d(TAG, "initHandler");
        initHandler();

        Log.d(TAG, "createImageReader");
        createImageReader();

        Log.d(TAG, "initMediaProjection");
        initMediaProjection();


        Log.d(TAG, "start screen shot");
        startScreenShot();
        handler.postDelayed(runnable, 1000);
    }
    public void startPreview(){
        Log.d(TAG, "createImageReader");
        createImageReader();

        flag = true;
        Log.d(TAG, "initMediaProjection");
        initMediaProjection();
    }
    public void initWindow(){

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
//        CSVHelper.storeToCSV("ScreenCapture.csv", "initWindow");

        //取得螢幕的各項參數
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        screenDensity = metrics.densityDpi;
        screenWidth = metrics.widthPixels / 2;
        screenHeight = metrics.heightPixels / 2;

        Log.d(TAG, "original: " + screenWidth + " " + screenHeight);
        int decrease = 800;
//        double screenRatio = (double)metrics.widthPixels/metrics.heightPixels;
//        screenHeight = screenHeight - decrease;
//        screenWidth = screenWidth - (int)(decrease*screenRatio);

        Log.d(TAG,"density:"+screenDensity+", width:" + screenWidth + ", height:" + screenHeight);

        igv = new ImageView(this);
        igv.setImageResource(R.mipmap.ic_launcher);


//        int layout_parms;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
//
//        {
//            layout_parms = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
//
//        }
//
//        else {
//
//            layout_parms = WindowManager.LayoutParams.TYPE_PHONE;
//
//        }

//        params = new WindowManager.LayoutParams(
//                WindowManager.LayoutParams.WRAP_CONTENT,
//                WindowManager.LayoutParams.WRAP_CONTENT,
//                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
//                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
//                PixelFormat.TRANSLUCENT);
//
//        params.gravity = Gravity.TOP | Gravity.LEFT;
//        params.x = 0;
//        params.y = screenHeight/4;


//        igv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startScreenShot();
//            }
//        });
//        windowManager.addView(igv, params);
    }

    public void initHandler(){
        handler = new Handler();
    }

    //建立imageReader
    public void createImageReader() {
        imageReader = ImageReader.newInstance(screenWidth, screenHeight, PixelFormat.RGBA_8888, 2);
//        CSVHelper.storeToCSV("ScreenCapture.csv", "createImageReader");
    }

    public static void setMediaProjection(MediaProjection mediaProjection_clone) {
        mediaProjection = mediaProjection_clone;
    }

    public void initMediaProjection(){

        //透過MediaProjectionManager取得MediaProjection

        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mediaProjection = mediaProjectionManager.getMediaProjection(Activity.RESULT_OK, resultIntent);

        //呼叫mediaProjection.createVirtualDisplay()
        if(mediaProjection != null) {
//            CSVHelper.storeToCSV("ScreenCapture.csv", "Init Media Projection");
            virtualDisplay = mediaProjection.createVirtualDisplay("screen-mirror",
                    screenWidth, screenHeight, screenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    imageReader.getSurface(), null, handler);
        }
        else
        {
            return;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) { // 9/1
        super.onConfigurationChanged(newConfig);
        //Your handling
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        screenDensity = metrics.densityDpi;
        screenWidth = metrics.widthPixels / 2;
        screenHeight = metrics.heightPixels / 2;

        Log.e(TAG, "original: " + screenWidth + " " + screenHeight);
        int decrease = 800;

//        if(screenWidth > screenHeight) {
//            double screenRatio = (double) metrics.heightPixels / metrics.widthPixels;
//            screenHeight = screenHeight - (int) (decrease * screenRatio);
//            screenWidth = screenWidth - decrease;
//        }
//        else{
//            double screenRatio = (double)metrics.widthPixels/metrics.heightPixels;
//            screenHeight = screenHeight - decrease;
//            screenWidth = screenWidth - (int)(decrease*screenRatio);
//        }

        Log.e(TAG, "onConfigurationChanged : "+ screenWidth +", " + screenHeight +", "+ screenDensity);
        if (virtualDisplay == null) {
            // Capturer is stopped, the virtual display will be created in startCaptuer().
            Log.e(TAG, "virtual display is null");
            return;
        }
        // Create a new virtual display on the surfaceTextureHelper thread to avoid interference
        // with frame processing, which happens on the same thread (we serialize events by running
        // them on the same thread).
        if(imageReader !=null)
        {
            Log.e(TAG, "imageReader is close");
            imageReader.close();
        }
        createImageReader();
        if(virtualDisplay !=null) {
            virtualDisplay.release();
            virtualDisplay = mediaProjection.createVirtualDisplay("screen-mirror-nctu",
                    screenWidth, screenHeight, screenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    imageReader.getSurface(), null, handler);
            Log.e(TAG, "virtual display is recreate");
        }
    }

    public static void setResultIntent(Intent it){
        resultIntent = it;
    }

    public void startScreenShot(){

        igv.setVisibility(View.GONE);
        Log.d(TAG, "startScreenShot called");
//        CSVHelper.storeToCSV("ScreenCapture.csv", "start ScreenShot");

//        handler.postDelayed(new Runnable() {
//            public void run() {
//                startCapture();
//            }
//        },5000);

        runnable = new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, 1000);
                startCapture();
                //延时1秒post
            }
        };
    }

    private void startCapture() {
        Image image = null;
        Log.d(TAG, "Call startCapture");
        //呼叫image.acquireLatestImage()，取得image
        try {
            //startPreview();
            image = imageReader.acquireNextImage();
            Log.d(TAG, "image take: " + image.toString());
            Log.d(TAG, "Get image");
            time_interval = System.currentTimeMillis();
//            CSVHelper.storeToCSV("ScreenCapture.csv", "Screen shot time: " + System.currentTimeMillis());
        }
        catch(Exception e)
        {
            Log.d(TAG,"ImageReader Exception: " + e.toString());
            CSVHelper.storeToCSV("ScreenCapture.csv", "ImageReader Exception: " + e.toString());
        }
        finally {
            if(image != null){
                final Image.Plane[] planes = image.getPlanes();
                final ByteBuffer buffer = planes[0].getBuffer();

                int pixelStride = planes[0].getPixelStride();
                int rowStride = planes[0].getRowStride();

//                Log.e(TAG, "image attribute: " + image.getWidth() + " " + image.getHeight());
                int rowPadding = rowStride - pixelStride * image.getWidth();
                Bitmap bitmap = Bitmap.createBitmap(image.getWidth() + rowPadding / pixelStride, image.getHeight(), Bitmap.Config.ARGB_8888);
                bitmap.copyPixelsFromBuffer(buffer);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, image.getWidth(), image.getHeight());
                buffer.clear();
                image.close();
//                int rowPadding = rowStride - pixelStride * screenWidth;
//                Bitmap bitmap = Bitmap.createBitmap(screenWidth + rowPadding / pixelStride, screenHeight, Bitmap.Config.ARGB_8888);
//                bitmap.copyPixelsFromBuffer(buffer);
//                bitmap = Bitmap.createBitmap(bitmap, 0, 0, screenWidth, screenHeight);
//                buffer.clear();
//                File fileImage;
//                if (bitmap != null) {
//                    try {
//                        fileImage = getScreenShotsFile();
//                        FileOutputStream out = new FileOutputStream(fileImage);
//                        if (out != null) {
//                            bitmap.compress(Bitmap.CompressFormat.JPEG, 5, out);
//                            out.flush();
//                            out.close();
//                            Log.d(TAG, fileImage.toString() + " is saved");
//
//                            long sessionID = SessionID;
//                        /*String dataType = "Image";
//                        String appName = TriggerApp;*/
//                            String filePath = fileImage.toString();
//                            String[] temp = filePath.split("/");
//                            String fileName = temp[temp.length - 1];
//
//                            newsDataRecord = new NewsDataRecord(sessionID, fileName, filePath, "Null");
//                            db.NewsDataRecordDao().insertAll(newsDataRecord);
//
//                            existfile = fileImage.toString();
//                        }
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }finally {
//                        bitmap.recycle();
//                    }
//                }
//                igv.setVisibility(View.VISIBLE)
                Log.d(TAG, "Save Task");
//                new SaveTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, bitmap);

                File fileImage;
                if (bitmap != null) {
                    try {
                        fileImage = getScreenShotsFile();
                        FileOutputStream out = new FileOutputStream(fileImage);
                        if (out != null) {
                            String filePath = fileImage.toString();
                            String[] temp = filePath.split("/");
                            String fileName = temp[temp.length - 2] + "/" + temp[temp.length - 1];

                            pref.edit().putString("CaptureImgName", fileName).apply(); // 看能不能搬到照片產生的那個時候
                            Log.d(TAG, "Capture img name is: " + fileName);

                            long share_time = System.currentTimeMillis();
//                        CSVHelper.storeToCSV("ScreenCapture.csv", "save to share preference time: " + System.currentTimeMillis());

                            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out);
                            out.flush();
                            out.close();

//                        CSVHelper.storeToCSV("ScreenCapture.csv", "save image time: " + System.currentTimeMillis());
//                            CSVHelper.storeToCSV("ScreenCapture.csv", "image name: " + fileName + " " + String.valueOf(share_time - time_interval) +
//                                    " " + String.valueOf(System.currentTimeMillis() - time_interval));

                            Log.d(TAG, fileImage.toString() + " is saved");

                            long sessionID = SessionID;
                        /*String dataType = "Image";
                        String appName = TriggerApp;*/

                            newsDataRecord = new NewsDataRecord(sessionID, fileName, filePath, "Null");
                            db.NewsDataRecordDao().insertAll(newsDataRecord);

                            existfile = fileImage.toString();
                        }
                        else{
                            Log.d(TAG, "out is null");
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }finally {
                        bitmap.recycle();
                    }
                }
                else{
                    Log.d(TAG, "bitmap is null");
                }
//                new SaveTask().execute(bitmap);
            }
                //startCapture();
            else{
                Log.d(TAG, "image is null");
            }
        }
        //Log.d(TAG, "StartCapture");
    }
    private long time_interval = 0L;
    public class SaveTask extends AsyncTask<Bitmap, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Bitmap... params) {
            Log.d(TAG, "Save Task doInBackground");
            if (params == null || params.length < 1 || params[0] == null) {
                Log.d(TAG, "Save Task return false");
                return false;
            }

            Boolean success = false;

            Bitmap bitmap = params[0];
//            Image image = params[0];
//            //處理影像並儲存到手機
//            final Image.Plane[] planes = image.getPlanes();
//            final ByteBuffer buffer = planes[0].getBuffer();
//
//            int pixelStride = planes[0].getPixelStride();
//            int rowStride = planes[0].getRowStride();
////            final ByteBuffer buffer = params[0].buffer;
////
////            int pixelStride = params[0].pixelStride;
////            int rowStride = params[0].rowStride;
//
//            int rowPadding = rowStride - pixelStride * screenWidth;
//            Bitmap bitmap = Bitmap.createBitmap(screenWidth + rowPadding / pixelStride, screenHeight, Bitmap.Config.ARGB_8888);
//            bitmap.copyPixelsFromBuffer(buffer);
//            bitmap = Bitmap.createBitmap(bitmap, 0, 0, screenWidth, screenHeight);
//            buffer.clear();
//            image.close();
//
            File fileImage;
            if (bitmap != null) {
                try {
                    fileImage = getScreenShotsFile();
                    FileOutputStream out = new FileOutputStream(fileImage);
                    if (out != null) {
                        String filePath = fileImage.toString();
                        String[] temp = filePath.split("/");
                        String fileName = temp[temp.length - 2] + "/" + temp[temp.length - 1];

                        pref.edit().putString("CaptureImgName", fileName).apply(); // 看能不能搬到照片產生的那個時候
                        Log.d(TAG, "Capture img name is: " + fileName);

                        long share_time = System.currentTimeMillis();
//                        CSVHelper.storeToCSV("ScreenCapture.csv", "save to share preference time: " + System.currentTimeMillis());

                        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out);
                        out.flush();
                        out.close();

//                        CSVHelper.storeToCSV("ScreenCapture.csv", "save image time: " + System.currentTimeMillis());
//                        CSVHelper.storeToCSV("ScreenCapture.csv", "image name: " + fileName + " " + String.valueOf(share_time - time_interval) +
//                                            " " + String.valueOf(System.currentTimeMillis() - time_interval));

                        Log.d(TAG, fileImage.toString() + " is saved");

                        long sessionID = SessionID;
                        /*String dataType = "Image";
                        String appName = TriggerApp;*/

                        newsDataRecord = new NewsDataRecord(sessionID, fileName, filePath, "Null");
                        db.NewsDataRecordDao().insertAll(newsDataRecord);

                        existfile = fileImage.toString();
                        success = true;
                    }
                    else{
                        Log.d(TAG, "out is null");
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    bitmap.recycle();
                }
            }
            else{
                Log.d(TAG, "bitmap is null");
            }
            return success;
        }

        @Override
        protected void onPostExecute(Boolean bool) {
            super.onPostExecute(bool);

            if (bool) {
                //Toast.makeText(getApplicationContext(),"Got it",Toast.LENGTH_SHORT).show();
            }
            else{
                //startCapture();
            }
            igv.setVisibility(View.VISIBLE);
        }
    }
    void StoreAllImgName(String imgName){
        try {
            FileWriter fw = new FileWriter(Environment.getExternalStorageDirectory().getPath() + PICTURE_DIRECTORY_PATH + date + "/" + ALLIMG, true);
            BufferedWriter bw = new BufferedWriter(fw); //將BufferedWeiter與FileWrite物件做連結
            bw.write(imgName);
            bw.newLine();
            bw.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public File getScreenShotsFile(){
        date = Utils.getTimeString(Utils.DATE_FORMAT_NOW_DAY);
        time = Utils.getTimeString(Utils.DATE_FORMAT_HOUR_MIN_SECOND);

        directory = new File(Environment.getExternalStorageDirectory().getPath() + PICTURE_DIRECTORY_PATH + date);

        if (!directory.exists() || !directory.isDirectory()) {
            directory.mkdirs();
        }
        File file = new File(Environment.getExternalStorageDirectory().getPath() + PICTURE_DIRECTORY_PATH + date + "/" +
                time  + "-" + SessionID + "-" + TriggerApp  +".jpg"); // 9/23
        StoreAllImgName(date + "-" + time  + "-" + SessionID + "-" + TriggerApp  +".jpg");
        return file;
    }

//    private void runTextRecog(Bitmap bitmap) {
//        //File f = getScreenShotsFile();
//        //String path = f.toString();
//        //String path = existfile;
//        //Bitmap bitmap = BitmapFactory.decodeFile(path);
//        //t = path + " result is:\n";
//        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
//        // [START get_detector_default]
//        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
//                .getOnDeviceTextRecognizer();
//        // [END get_detector_default]
//
//        // [START run_detector]
//        Task<FirebaseVisionText> result =
//                detector.processImage(image)
//                        .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
//                            @Override
//                            public void onSuccess(FirebaseVisionText firebaseVisionText) {
//                                // Task completed successfully
//                                // [START_EXCLUDE]
//                                // [START get_text]
//                                for (FirebaseVisionText.TextBlock block : firebaseVisionText.getTextBlocks()) {
//                                    Rect boundingBox = block.getBoundingBox();
//                                    Point[] cornerPoints = block.getCornerPoints();
//                                    String txt = block.getText();
//                                    t = t + txt + " ";
//                                    for (FirebaseVisionText.Line line: block.getLines()) {
//                                        // ...
//                                        for (FirebaseVisionText.Element element: line.getElements()) {
//                                            // ...
//                                        }
//                                    }
//                                }
//                                t = t.replaceAll("\r|\n", "");
//                                Log.d(TAG, t);
//                                if(takeURL) {
//                                    iswebsite(t);
//                                }
//                                // [END get_text]
//                                // [END_EXCLUDE]
//                            }
//                        })
//                        .addOnFailureListener(
//                                new OnFailureListener() {
//                                    @Override
//                                    public void onFailure(@NonNull Exception e) {
//                                        // Task failed with an exception
//                                        // ...
//                                    }
//                                });
//        Log.d(TAG, "End Recognize");
//        // [END run_detector]
//    }
    public void iswebsite(String txt)
    {
        String[] txt_split = txt.split(" ");
        //Log.d(TAG, "After split: ");
        for(int i = 0; i< txt_split.length; i++)
        {
            //Log.d(TAG, txt_split[i]);
            txt_split[i] = txt_split[i].replaceAll("\r|\n", "");
            //Log.d(TAG, "After split: " + txt_split[i]);
            if(p.matcher(txt_split[i]).lookingAt()) {
                try {
                    SharedPreferences pref = getSharedPreferences("URL", MODE_PRIVATE);
                    Set<String> UrlSet = pref.getStringSet("UrlSet", new HashSet<String>());
                    List<String> TitleAndWeb = new ArrayList<String>(UrlSet);

                    if(!TitleAndWeb.contains(txt_split[i])) {
                        TitleAndWeb.add(txt_split[i]);
                        txt_split[i] = txt_split[i].replaceAll("\r|\n", "");
                        FileWriter fw = new FileWriter(Environment.getExternalStorageDirectory().getPath() + PICTURE_DIRECTORY_PATH + date + "/MesUrl.txt", true);
                        BufferedWriter bw = new BufferedWriter(fw); //將BufferedWeiter與FileWrite物件做連結
                        bw.write(txt_split[i]);
                        bw.newLine();
                        bw.close();

                        UrlSet = new HashSet<String>(TitleAndWeb);
                        pref.edit()
                                .putStringSet("UrlSet", UrlSet)
                                .commit();
                    }
                    //Log.d(TAG, "URL: " + TitleAndWeb);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void isnews(String txt)
    {
        StringBuilder NewsTitle = new StringBuilder("");
        String SplitToken = "[0-9]{1,2}/[0-9]{1,2}";
        String[] txt_split = txt.split(SplitToken);
        //Log.d(TAG, "After split: ");
        for(int i = 0; i< txt_split.length; i++)
        {
            if(txt_split[i].contains("新聞"))
            {
                NewsTitle.append(txt_split[i] + "\n");
            }
        }
        Log.d(TAG, "Here is News: " + NewsTitle);
    }
//    private void runTextRecogCloud(){
//        t = "";
//        String path = existfile;
//        Bitmap bitmap = BitmapFactory.decodeFile(path);
//        t = path + " result is:\n";
//
//        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
//        // [START set_detector_options_cloud]
//        FirebaseVisionCloudTextRecognizerOptions options = new FirebaseVisionCloudTextRecognizerOptions.Builder()
//                .setLanguageHints(Arrays.asList("en", "hi", "zh"))
//                .build();
//        // [END set_detector_options_cloud]
//
//        // [START get_detector_cloud]
//        //FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
//        //       .getCloudTextRecognizer();
//        // Or, to change the default settings:
//        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
//                .getCloudTextRecognizer(options);
//        // [END get_detector_cloud]
//
//        // [START run_detector_cloud]
//        Task<FirebaseVisionText> result = detector.processImage(image)
//                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
//                    @Override
//                    public void onSuccess(FirebaseVisionText result) {
//                        // Task completed successfully
//                        // [START_EXCLUDE]
//                        // [START get_text_cloud]
//                        for (FirebaseVisionText.TextBlock block : result.getTextBlocks()) {
//                            Rect boundingBox = block.getBoundingBox();
//                            Point[] cornerPoints = block.getCornerPoints();
//                            String txt = block.getText();
//                            t = t + txt + " ";
//
//                            for (FirebaseVisionText.Line line : block.getLines()) {
//                                // ...
//                                //String txt = line.getText();
//                                //t = t + txt + " ";
//                                for (FirebaseVisionText.Element element : line.getElements()) {
//                                    // ...
//                                }
//                            }
//                        }
//                        t = t.replaceAll("\r|\n", "");
//                        Log.d(TAG, t);
//                        if(takeURL) {
//                            t = t.replaceAll("\r|\n", "");
//                            iswebsite(t);
//                        }
//                        else if(takeNews)
//                        {
//                            t = t.replaceAll("\r|\n", "");
//                            isnews(t);
//                        }
//                        t = "";
//                        //iswebsite(t);
//                        // [END get_text_cloud]
//                        // [END_EXCLUDE]
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        // Task failed with an exception
//                        // ...
//                    }
//                });
//        // [END run_detector_cloud]
//    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy");
        handler.removeCallbacks(runnable);
//        pref.edit().putString("CaptureImgName", "").apply(); // 9/24
//        Log.d(TAG, "Capture img name is empty");

        pref.edit().putString("ScreenShot", "0").apply();
//        lastid = pref.getLong("SessionID", 0);
//        db.SessionDataRecordDao().updateSession(lastid, ScheduleAndSampleManager.getCurrentTimeString(), System.currentTimeMillis());
//        NewsRelated(false);

        if(ifRecordingRightNow) {
            Log.d(TAG, "ifRecordingRightNow "  + TriggerApp + " " + MyAccessibilityService.intent);
            MyAccessibilityService.notificationListenService.startRecordingNotification(this, Accessibility_ID, TriggerApp, true, MyAccessibilityService.intent);
        }

//        Long lastESMtime = pref.getLong("LastESMTime", 0);
//        Long now = System.currentTimeMillis();
//        Calendar c = Calendar.getInstance();
//        c.setTimeInMillis(now);
//
//        int MinHour = pref.getInt("MinHour", 9);
//        int MaxHour = pref.getInt("MaxHour", 22);
//
//        Random_session_counter++;
//        Log.d(TAG, "Random session number = " + Random_session_num);
//        Log.d(TAG, "Random session counter = " + Random_session_counter);
//        if(Random_session_counter >= Random_session_num) {
//            CSVHelper.storeToCSV("ESM_random_number.csv", "counter now / random number: " + Random_session_counter + " / " + Random_session_num  + " (" + TriggerApp + ")");
//            Random_session_counter = 0;
//            if(c.get(Calendar.HOUR_OF_DAY) >= MinHour && c.get(Calendar.HOUR_OF_DAY) < MaxHour) {
//                if (now - lastESMtime > ESMinterval * 1000) {
//                    CSVHelper.storeToCSV("ESM_random_number.csv", "ESM is delivered");
//                    Intent intent = new Intent(this, AlarmReceiver.class);
//                    intent.setAction(ESM_ALARM);
//                    sendBroadcast(intent);
//                    pref.edit().putLong("LastESMTime", System.currentTimeMillis()).apply();
//                    Random_session_num = (int) (Math.random() * 3 + 1);
//                }
//                else{
//                    CSVHelper.storeToCSV("ESM_random_number.csv", "ESM is not delivered, not exceed one hour");
//                }
//            }
//            else{
//                CSVHelper.storeToCSV("ESM_random_number.csv", "ESM is not delivered, now is out of setting time");
//            }
//            CSVHelper.storeToCSV("ESM_random_number.csv", "Reset random number to: " + Random_session_num);
//        }
//        else{
//            CSVHelper.storeToCSV("ESM_random_number.csv", "counter now / random number: " + Random_session_counter + " / " + Random_session_num + " (" + TriggerApp + ")");
//        }


        if (virtualDisplay != null) {
            virtualDisplay.release();
            virtualDisplay = null;
        }

        if (mediaProjection != null) {
            mediaProjection.stop();
            mediaProjection = null;
        }

        CSVHelper.storeToCSV("AccessibilityDetect.csv", "ScreenCapture is stop!!");
        CSVHelper.storeToCSV("AccessibilityDetect.csv", "-------------------------");
        Toast.makeText(getApplicationContext(),"停止截圖", Toast.LENGTH_LONG).show();
    }

    public void storeSession(String appname, String data_type){
//        CSVHelper.storeToCSV("AccessibilityDetect.csv", appname + " is screen shot");
//        long phone_session_id = pref.getLong("Phone_SessionID", 0);
//        sessionDataRecord = new SessionDataRecord(ScheduleAndSampleManager.getCurrentTimeString(), "NA", data_type, appname, phone_session_id);
//        db.SessionDataRecordDao().insertAll(sessionDataRecord);
//        pref.edit().putLong("SessionID", db.SessionDataRecordDao().getLastRecord().get_id()).apply();
    }

    public void NewsRelated(boolean open)
    {
        Log.d(TAG,"NewsRelated: " + open);
        getSharedPreferences("test", MODE_PRIVATE).edit().putBoolean("ReadNews",open).apply();
        runPhoneStatusMainThread(open);
        try {
            Log.d(TAG, "Stream update runnable: " + System.currentTimeMillis());
//            streamManager.updateStreamGenerators();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void runPhoneStatusMainThread(boolean start){

        labelingStudy.nctu.minuku.logger.Log.d(TAG, "runSensorMainThread") ;

        if(start) {
            Log.d(TAG, "Start Thread");
            mMainThread.post(runnable);
        }
        else{
            Log.d(TAG, "Stop Thread");
            mMainThread.removeCallbacksAndMessages(null);
        }
    }
}
