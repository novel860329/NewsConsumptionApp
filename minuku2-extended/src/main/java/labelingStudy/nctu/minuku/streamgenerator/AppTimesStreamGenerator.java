package labelingStudy.nctu.minuku.streamgenerator;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.RequiresApi;

import org.greenrobot.eventbus.EventBus;

import labelingStudy.nctu.minuku.DBHelper.appDatabase;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minuku.model.DataRecord.AppTimesDataRecord;
import labelingStudy.nctu.minuku.stream.AccessibilityStream;
import labelingStudy.nctu.minukucore.exception.StreamAlreadyExistsException;
import labelingStudy.nctu.minukucore.exception.StreamNotFoundException;
import labelingStudy.nctu.minukucore.stream.Stream;

import static labelingStudy.nctu.minuku.manager.MinukuStreamManager.getInstance;

public class AppTimesStreamGenerator extends AndroidStreamGenerator<AppTimesDataRecord> {

    private final String TAG = "AppTimesStreamGenerator";
    private final String room = "room";
    private Stream mStream;
    private Context mContext;
    private SharedPreferences sharedPrefs;
//    private String LastImageName = "";
    private int FacebookOpenTimes;private int FacebookScreenTimes;private int MessengerURLTimes;
    private int YoutubeOpenTimes;private int YoutubeScreenTimes;private int InstagramOpenTimes;
    private int InstagramScreenTimes;private int NewsappOpenTimes;private int NewsappScreenTimes;
    private int  PPTtitleTimes;private int LinetodayOpenTimes;private int LinetodayScreenTimes;
    private int LineUrlTimes;private int GooglenowOpenTimes;private int ChromeOpen;
    private int GooglenowScreenTimes;private int ChromeScreen;
    boolean ReadNews;
    appDatabase db;
    AppTimesDataRecord myAppTimesDataRecord;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public AppTimesStreamGenerator(Context applicationContext){
        super(applicationContext);
        this.mContext = applicationContext;
        this.mStream = new AccessibilityStream(Constants.DEFAULT_QUEUE_SIZE);

        //mobileAccessibilityService = new MyAccessibilityService(this);
        FacebookOpenTimes = FacebookScreenTimes = MessengerURLTimes =
        YoutubeOpenTimes = YoutubeScreenTimes = InstagramOpenTimes =
        InstagramScreenTimes = NewsappOpenTimes = NewsappScreenTimes =
        PPTtitleTimes = LinetodayOpenTimes = LinetodayScreenTimes =
        LineUrlTimes =GooglenowOpenTimes =
        GooglenowScreenTimes = ChromeOpen = ChromeScreen = 0;

        db = appDatabase.getDatabase(applicationContext);
        sharedPrefs = mContext.getSharedPreferences(Constants.sharedPrefString, Context.MODE_PRIVATE);

        this.register();

    }

    @Override
    public void register() {
        Log.d(TAG, "Registring with StreamManage");

        try {
            Log.d(TAG, "success");
            getInstance().register(mStream, AppTimesDataRecord.class, this);
        } catch (StreamNotFoundException streamNotFoundException) {
            Log.e(TAG, "One of the streams on which" +
                    "AppTimesDataRecord/AccessibilityStream depends in not found.");
        } catch (StreamAlreadyExistsException streamAlreadyExistsException) {
            Log.e(TAG, "Another stream which provides" +
                    " AppTimesDataRecordDataRecord/AccessibilityStream is already registered.");
        }
    }

//    private void activateAccessibilityService() {
//
//        Log.d(TAG, "testing logging task and requested activateAccessibilityService");
//        Intent intent = new Intent(mContext, MobileAccessibilityService.class);
//        mContext.startService(intent);
//
//    }


    @Override
    public Stream<AppTimesDataRecord> generateNewStream() {
        return mStream;
    }

    @Override
    public boolean updateStream() {
        Log.d(TAG, "updateStream called");
        long session_id;
        long phone_session_id = mContext.getSharedPreferences(Constants.sharedPrefString, Context.MODE_PRIVATE).getLong("Phone_SessionID", 1);
        String screenshot = mContext.getSharedPreferences(Constants.sharedPrefString, Context.MODE_PRIVATE).getString("ScreenShot", "0");
        String ImageName =  mContext.getSharedPreferences(Constants.sharedPrefString, Context.MODE_PRIVATE).getString("CaptureImgName", "");
//        String AccessibilityUrl = mContext.getSharedPreferences(Constants.sharedPrefString, Context.MODE_PRIVATE).getString("AccessibilityUrl", "");
//        String NotificationUrl = mContext.getSharedPreferences(Constants.sharedPrefString, Context.MODE_PRIVATE).getString("NotificationUrl", "");
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
                    mContext.getSharedPreferences(Constants.sharedPrefString, Context.MODE_PRIVATE).edit().putString("CaptureImgName", "").apply();
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

        AppTimesDataRecord myAppTimesDataRecord;
        myAppTimesDataRecord = new AppTimesDataRecord(FacebookOpenTimes, FacebookScreenTimes, MessengerURLTimes,
                YoutubeOpenTimes, YoutubeScreenTimes, InstagramOpenTimes,
                InstagramScreenTimes, NewsappOpenTimes, NewsappScreenTimes,
                PPTtitleTimes, LinetodayOpenTimes, LinetodayScreenTimes,
                LineUrlTimes, GooglenowOpenTimes,
                GooglenowScreenTimes, ChromeOpen, ChromeScreen, ReadNews, phone_session_id, String.valueOf(session_id), screenshot, ImageName);
        //if(!package_name.trim().isEmpty()&&!app_name.trim().isEmpty()) {
        {
            mStream.add(myAppTimesDataRecord);
            Log.d(TAG, "Accessibility to be sent to event bus" + myAppTimesDataRecord);
            Log.d("creationTime : ", "accessData : " + myAppTimesDataRecord.getCreationTime());
            // also post an event.
            EventBus.getDefault().post(myAppTimesDataRecord);
            try {
                if(session_id != -1) {
                    db.AppTimesDataRecordDao().insertAll(myAppTimesDataRecord);
                }
//            List<AppTimesDataRecord> accessibilityDataRecords = db.AppTimesDataRecordDao().getAll();
//
            /*for (AppTimesDataRecord a : accessibilityDataRecords) {
                Log.d(room, "facebook open : "+a.getFacebookOpenTimes());
                Log.d(room, "line open : "+a.getLineUrlTimes());
                Log.d(room, "facebook screen : "+a.getFacebookScreenTimes());
                Log.d(TAG,"-----------------------------------------------");
            }*/
//
//
//            }


            } catch (NullPointerException e) { //Sometimes no data is normal
                e.printStackTrace();
                return false;
            }
        }
//        FacebookOpenTimes = FacebookScreenTimes = MessengerURLTimes =
//                YoutubeOpenTimes = YoutubeScreenTimes = InstagramOpenTimes =
//                        InstagramScreenTimes = NewsappOpenTimes = NewsappScreenTimes =
//                                PPTtitleTimes = LinetodayOpenTimes = LinetodayScreenTimes =
//                                        LineUrlTimes = GooglenowOpenTimes =
//         GooglenowScreenTimes = ChromeOpen = ChromeScreen = 0;

        return false;
    }


    @Override
    public long getUpdateFrequency() {
        return 1;
    }

    @Override
    public void sendStateChangeEvent() {

    }
    public void updateapptimes(String app)
    {
        int FacebookOpen = 0;
        int FacebookScreen = 0;
        int MessengerURL = 0;
        int YoutubeOpen = 0;
        int YoutubeScreen = 0;
        int InstagramOpen = 0;
        int InstagramScreen = 0;
        int NewsappOpen = 0;
        int NewsappScreen = 0;
        int PPTtitle = 0;
        int LinetodayOpen = 0;
        int LinetodayScreen = 0;
        int LineUrlTimes = 0;
        int GooglenowOpen = 0;
        int GooglenowScreen = 0;
        int ChromeOpen = 0;
        int ChromeScreen = 0;
//        AppTimesDataRecord last =  db.AppTimesDataRecordDao().getLastRecord();
//        {
//             FacebookOpen = 0;
//             FacebookScreen = 0;
//             MessengerURL = 0;
//             YoutubeOpen = 0;
//             YoutubeScreen = 0;
//             InstagramOpen = 0;
//             InstagramScreen = 0;
//             NewsappOpen = 0;
//             NewsappScreen = 0;
//             PPTtitle = 0;
//             LinetodayOpen = 0;
//             LinetodayScreen = 0;
//             LineUrlTimes = 0;
//             GooglenowOpen = 0;
//             GooglenowScreen = 0;
//            ChromeOpen = 0;
//            ChromeScreen = 0;
//            setLatestInAppAction(FacebookOpen, FacebookScreen, MessengerURL,
//                    YoutubeOpen, YoutubeScreen, InstagramOpen, InstagramScreen, NewsappOpen, NewsappScreen,
//                    PPTtitle, LinetodayOpen, LinetodayScreen, LineUrlTimes, GooglenowOpen,
//                    GooglenowScreen, ChromeOpen, ChromeScreen);
//            updateStream();
//        }
//        last =  db.AppTimesDataRecordDao().getLastRecord();
//             FacebookOpen = 0;
//             FacebookScreen = 0;
//             MessengerURL = 0;
//             YoutubeOpen = 0;
//             YoutubeScreen = 0;
//             InstagramOpen = 0;
//             InstagramScreen = 0;
//             NewsappOpen = 0;
//             NewsappScreen = 0;
//             PPTtitle = 0;
//             LinetodayOpen = 0;
//             LinetodayScreen = 0;
//             LineUrlTimes = 0;
//             GooglenowOpen = 0;
//             GooglenowScreen = 0;
//            ChromeOpen = 0;
//            ChromeScreen = 0;
            if (app.equals("FacebookOpen")) FacebookOpen++;
            else if (app.equals("FacebookScreen")) FacebookScreen++;
            else if (app.equals("MessengerURL")) MessengerURL++;
            else if (app.equals("YoutubeOpen")) YoutubeOpen++;
            else if (app.equals("YoutubeScreen")) YoutubeScreen++;
            else if (app.equals("InstagramOpen")) InstagramOpen++;
            else if (app.equals("InstagramScreen")) InstagramScreen++;
            else if (app.equals("NewsappOpen")) NewsappOpen++;
            else if (app.equals("NewsappScreen")) NewsappScreen++;
            else if (app.equals("PPTtitle")) PPTtitle++;
            else if (app.equals("LinetodayOpen")) LinetodayOpen++;
            else if (app.equals("LinetodayScreen")) LinetodayScreen++;
            else if (app.equals("LineUrl")) LineUrlTimes++;
            else if (app.equals("GooglenowOpen")) GooglenowOpen++;
            else if (app.equals("GooglenowScreen")) GooglenowScreen++;
            else if(app.equals("ChromeOpen"))ChromeOpen++;
            else if(app.equals("ChromeScreen"))ChromeScreen++;
         setLatestInAppAction(FacebookOpen, FacebookScreen, MessengerURL,
                    YoutubeOpen, YoutubeScreen, InstagramOpen, InstagramScreen, NewsappOpen, NewsappScreen,
                    PPTtitle, LinetodayOpen, LinetodayScreen, LineUrlTimes, GooglenowOpen,
                    GooglenowScreen, ChromeOpen, ChromeScreen);
         updateStream();
    }
    public void setLatestInAppAction(int FacebookOpenTimes,int FacebookScreenTimes,int MessengerURLTimes,
                                     int YoutubeOpenTimes,int YoutubeScreenTimes,int InstagramOpenTimes,
                                     int InstagramScreenTimes,int NewsappOpenTimes,int NewsappScreenTimes,
                                     int  PPTtitleTimes,int LinetodayOpenTimes,int LinetodayScreenTimes,
                                     int LineUrlTimes, int GooglenowOpenTimes,
                                     int GooglenowScreenTimes, int ChromeOpen, int ChromeScreen){
            this.FacebookOpenTimes = FacebookOpenTimes;
            this.FacebookScreenTimes = FacebookScreenTimes;
            this.MessengerURLTimes = MessengerURLTimes;
            this.YoutubeOpenTimes = YoutubeOpenTimes;
            this.YoutubeScreenTimes = YoutubeScreenTimes;
            this.InstagramOpenTimes = InstagramOpenTimes;
            this.InstagramScreenTimes = InstagramScreenTimes;
            this.NewsappOpenTimes = NewsappOpenTimes;
            this.NewsappScreenTimes = NewsappScreenTimes;
            this.PPTtitleTimes = PPTtitleTimes;
            this.LinetodayOpenTimes = LinetodayOpenTimes;
            this.LinetodayScreenTimes = LinetodayScreenTimes;
            this.LineUrlTimes = LineUrlTimes;
            this.GooglenowOpenTimes = GooglenowOpenTimes;
            this.GooglenowScreenTimes = GooglenowScreenTimes;
            this.ChromeOpen = ChromeOpen;
            this.ChromeScreen = ChromeScreen;
//        Log.d(TAG, "pack, "+pack+"text "+text+"type "+type+"extra "+extra);
        }
    @Override
    public void onStreamRegistration() {

        //activateAccessibilityService();

    }

    @Override
    public void offer(AppTimesDataRecord dataRecord) {

    }

}


