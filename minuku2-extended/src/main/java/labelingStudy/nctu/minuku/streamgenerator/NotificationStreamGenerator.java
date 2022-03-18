package labelingStudy.nctu.minuku.streamgenerator;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.greenrobot.eventbus.EventBus;

import labelingStudy.nctu.minuku.DBHelper.appDatabase;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.manager.MinukuStreamManager;
import labelingStudy.nctu.minuku.model.DataRecord.NotificationDataRecord;
import labelingStudy.nctu.minuku.service.NotificationListenService;
import labelingStudy.nctu.minuku.stream.NotificationStream;
import labelingStudy.nctu.minukucore.exception.StreamAlreadyExistsException;
import labelingStudy.nctu.minukucore.exception.StreamNotFoundException;
import labelingStudy.nctu.minukucore.stream.Stream;

/**
 * Created by chiaenchiang on 18/11/2018.
 */

public class NotificationStreamGenerator extends AndroidStreamGenerator<NotificationDataRecord> {

    private Context mContext;
    String TAG = "NotificationStreamGenerator";
    String room = "room";
    private NotificationStream mStream;
    private SharedPreferences sharedPrefs;
//    private String LastImageName = "";
    private static NotificationManager notificationManager;
    public static String mNotificaitonTitle = "";
    public static String mNotificaitonText = "";
    public static String mNotificaitonSubText = "";
    public static String mNotificationTickerText = "";
    public static  String mNotificaitonPackageName ="";
    public static  String mReason ="";
    public static int SenderID = 0;

    private NotificationListenService notificationlistener;
    public static Integer accessid=-1;
    appDatabase db;


    public NotificationStreamGenerator(){
        super();
    }

    @Override
    public void register() {
        Log.d(TAG, "Registering with Stream Manager");
        try {
            MinukuStreamManager.getInstance().register(mStream, NotificationDataRecord.class, this);
        } catch (StreamNotFoundException streamNotFoundException) {
            Log.e(TAG, "One of the streams on which NotificationDataRecord/NotificationStream depends in not found.");
        } catch (StreamAlreadyExistsException streamAlreadyExsistsException) {
            Log.e(TAG, "Another stream which provides NotificationDataRecord/NotificationStream is already registered.");
        }

    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @SuppressLint("ServiceCast")
    public NotificationStreamGenerator(Context applicationContext) {
        super(applicationContext);

        mContext = applicationContext;
        notificationlistener = new NotificationListenService(this);
        sharedPrefs = mContext.getSharedPreferences(Constants.sharedPrefString,Context.MODE_PRIVATE);

        this.mStream = new NotificationStream(Constants.DEFAULT_QUEUE_SIZE);

        //  notificationManager = (android.app.NotificationManager)mContext.getSystemService(mContext.NOTIFICATION_SERVICE);

        db = appDatabase.getDatabase(applicationContext);
        mNotificaitonTitle = "Default";
        mNotificaitonText = "Default";
        mNotificaitonSubText = "Default";
        mNotificationTickerText = "Default";
        mNotificaitonPackageName ="Default";
        mReason = "Default";
        accessid = -1;
        this.register();
    }



    @Override
    public Stream<NotificationDataRecord> generateNewStream() {
        return null;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public boolean updateStream() {
        // setRandom notification
        long session_id;
        long phone_session_id = sharedPrefs.getLong("Phone_SessionID", 1);
        String screenshot = sharedPrefs.getString("ScreenShot", "0");
        String ImageName = sharedPrefs.getString("CaptureImgName", "");
//        String AccessibilityUrl = sharedPrefs.getString("AccessibilityUrl", "");
//        String NotificationUrl = sharedPrefs.getString("NotificationUrl", "");

//        Log.d(TAG, "AccessibilityUrl: " + AccessibilityUrl);
//        Log.d(TAG, "NotificationUrl: " + NotificationUrl);

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
        Log.d("checkRandomNoti","updateStream");
        NotificationDataRecord notificationDataRecord;
        notificationDataRecord =
                new NotificationDataRecord(mNotificaitonTitle, mNotificaitonText, mNotificaitonSubText
                        , mNotificationTickerText, mNotificaitonPackageName, accessid, mReason, String.valueOf(session_id),
                        phone_session_id, screenshot, ImageName, SenderID);

        mStream.add(notificationDataRecord);
        Log.d(TAG, "Check notification to be sent to event bus" + notificationDataRecord);
        // also post an event.
        Log.d("creationTime : ", "notiData : "+notificationDataRecord.getCreationTime());

        EventBus.getDefault().post(notificationDataRecord);

        try {
            db.notificationDataRecordDao().insertAll(notificationDataRecord);
//            List<NotificationDataRecord> NotificationDataRecords = db.notificationDataRecordDao().getAll();
//            for (NotificationDataRecord n : NotificationDataRecords) {
//                Log.d(room,"Notification: "+n.getNotificaitonPackageName());
//            }

        } catch (NullPointerException e) { //Sometimes no data is normal
            e.printStackTrace();
            return false;
        }
        return false;
    }

    @Override
    public long getUpdateFrequency() {
        return -1;
    }

    @Override
    public void sendStateChangeEvent() {

    }

    @Override
    public void onStreamRegistration() {
        activateNotiListenService();

    }
    private void activateNotiListenService() {

        labelingStudy.nctu.minuku.logger.Log.d(TAG, "testing logging task and requested activateAccessibilityService");
        Intent intent = new Intent(mContext, NotificationListenService.class);
        mContext.startService(intent);

    }

    public void setNotificationDataRecord(String title, String text, String subText, String tickerText,String pack,Integer accessid,String reason, int SenderID){

        this.mNotificaitonTitle = title;
        this.mNotificaitonText = text;
        this.mNotificaitonSubText = subText;
        this.mNotificationTickerText = tickerText;
        this.mNotificaitonPackageName = pack;
        this.accessid = accessid;
        this.mReason = reason;
        this.SenderID = SenderID;
        Log.d(TAG, "title:"+title+" text: "+text + " subText: "+subText+" ticker: "+tickerText+" pack: "+pack);

    }


    @Override
    public void offer(NotificationDataRecord dataRecord) {

    }
}
