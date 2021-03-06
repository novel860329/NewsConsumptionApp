package labelingStudy.nctu.minuku.streamgenerator;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;

import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import labelingStudy.nctu.minuku.DBHelper.appDatabase;
import labelingStudy.nctu.minuku.Utilities.ScheduleAndSampleManager;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minuku.config.SharedVariables;
import labelingStudy.nctu.minuku.manager.MinukuStreamManager;
import labelingStudy.nctu.minuku.model.DataRecord.AppUsageDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.UserDataRecord;
import labelingStudy.nctu.minuku.stream.AppUsageStream;
import labelingStudy.nctu.minukucore.exception.StreamAlreadyExistsException;
import labelingStudy.nctu.minukucore.exception.StreamNotFoundException;
import labelingStudy.nctu.minukucore.stream.Stream;

import static android.content.Context.POWER_SERVICE;
import static labelingStudy.nctu.minuku.streamgenerator.ActivityRecognitionStreamGenerator.getCurrentTimeInMillis;

/**
 * Created by Jimmy on 2017/8/8.
 */

public class AppUsageStreamGenerator extends AndroidStreamGenerator<AppUsageDataRecord>{

    private Context mContext;
    private AppUsageStream mStream;
    private SharedPreferences sharedPrefs;
    private String TAG = "AppUsageStreamGenerator";
    private PowerManager mPowerManager;
    private static ActivityManager mActivityManager;

    private static HashMap<String, String> mAppPackageNameHmap;

    private static Handler mMainThread;
    AppUsageStreamGenerator mAppUsageStreamGenerator;
    /**Table Names**/
    public static final String RECORD_TABLE_NAME_APPUSAGE = "Record_Table_AppUsage";

    public static int mainThreadUpdateFrequencyInSeconds = 15;
    public static long mainThreadUpdateFrequencyInMilliseconds = mainThreadUpdateFrequencyInSeconds *Constants.MILLISECONDS_PER_SECOND;

    /** Applicaiton Usage Access **/
    //how often we get the update
    public static int mApplicaitonUsageUpdateFrequencyInSeconds = mainThreadUpdateFrequencyInSeconds;
    public static long mApplicaitonUsageUpdateFrequencyInMilliseconds = mApplicaitonUsageUpdateFrequencyInSeconds *Constants.MILLISECONDS_PER_SECOND;

    //how far we look back
    public static int mApplicaitonUsageSinceLastDurationInSeconds = mApplicaitonUsageUpdateFrequencyInSeconds;
    public static long mApplicaitonUsageSinceLastDurationInMilliseconds = mApplicaitonUsageSinceLastDurationInSeconds *Constants.MILLISECONDS_PER_SECOND;

    /** context measure **/
    public static final String CONTEXT_SOURCE_MEASURE_APPUSAGE_SCREEN_STATUS = "ScreenStatus";
    public static final String CONTEXT_SOURCE_MEASURE_APPUSAGE_LATEST_USED_APP = "LatestUsedApp";
    public static final String CONTEXT_SOURCE_MEASURE_APPUSAGE_USED_APPS_STATS_IN_RECENT_HOUR = "RecentApps";

    /**Properties for Record**/
    public static final String RECORD_DATA_PROPERTY_APPUSAGE_SCREEN_STATUS = "Screen_Status";
    public static final String RECORD_DATA_PROPERTY_APPUSAGE_LATEST_USED_APP = "Latest_Used_App";
    public static final String RECORD_DATA_PROPERTY_APPUSAGE_LATEST_USED_APP_TIME = "Latest_Used_App_Time";
    public static final String RECORD_DATA_PROPERTY_APPUSAGE_LATEST_FOREGROUND_ACTIVITY = "Latest_Foreground_Activity";
    public static final String RECORD_DATA_PROPERTY_APPUSAGE_USED_APPS_STATS_IN_RECENT_HOUR = "Recent_Apps";
    public static final String RECORD_DATA_PROPERTY_APPUSAGE_APP_USE_DURATION_IN_LAST_CERTAIN_TIME = "AppUseDurationInLastCertainTime";
    public static final String RECORD_DATA_PROPERTY_APPUSAGE_USER_USING = "Users";

    /**latest running app **/
    private static String mLastestForegroundActivity= "NA"; //Latest_Foreground_Activity
    private static String mLastestForegroundPackage= "NA"; //Latest_Used_App
    private static String mLastestForegroundPackageTime= "NA";
    private static String mRecentUsedAppsInLastHour= "NA";
    private String[] NewsPack;
    private String[] NewsName;
    private String[] WebPack;
    private String[] WebEvent;
    private String[] HomePackage;
    private String[] AppPack;
    private String[] PttPack;
//    private String LastImageName = "";
    //screen on and off
    private String Screen_Status;
    private static final String STRING_SCREEN_OFF = "Screen_off";
    private static final String STRING_SCREEN_ON = "Screen_on";
    private static final String STRING_INTERACTIVE = "Interactive";
    private static final String STRING_NOT_INTERACTIVE = "Not_Interactive";
    appDatabase db;
    private UserDataRecord userRecord;
    private boolean first = true;
    private SharedPreferences pref;
    public static AppUsageDataRecord toCheckFamiliarOrNotLocationDataRecord;

    public AppUsageStreamGenerator(Context applicationContext){
        super(applicationContext);

        //load app XML
        mAppPackageNameHmap = new HashMap<String, String>();
        //loadAppAndPackage();

        mContext = applicationContext;
        this.mStream = new AppUsageStream(Constants.LOCATION_QUEUE_SIZE);
        db = appDatabase.getDatabase(applicationContext);
        sharedPrefs = mContext.getSharedPreferences(Constants.sharedPrefString,Context.MODE_PRIVATE);

        mPowerManager = (PowerManager) applicationContext.getSystemService(POWER_SERVICE);

        this.register();
    }

    @Override
    public void register() {
        Log.d(TAG, "Registering with StreamManager.");
        Resources res = mContext.getResources();
        NewsPack = res.getStringArray(labelingStudy.nctu.minuku.R.array.NewsPack);
        NewsName = res.getStringArray(labelingStudy.nctu.minuku.R.array.NewsName);
        HomePackage = res.getStringArray(labelingStudy.nctu.minuku.R.array.HomePackage);
        AppPack = res.getStringArray(labelingStudy.nctu.minuku.R.array.AppPackage);
        Log.d(TAG, "Get Nes package: " + AppPack.toString());
        WebPack = res.getStringArray(labelingStudy.nctu.minuku.R.array.WebPackage);
        WebEvent = res.getStringArray(labelingStudy.nctu.minuku.R.array.WebText);
        PttPack = res.getStringArray(labelingStudy.nctu.minuku.R.array.PttPackage);
//        pref = mApplicationContext.getSharedPreferences("test", Context.MODE_PRIVATE);
        try {
            MinukuStreamManager.getInstance().register(mStream, AppUsageDataRecord.class, this);
        } catch (StreamNotFoundException streamNotFoundException) {
            Log.e(TAG, "One of the streams on which AppUsageDataRecord depends in not found.");
        } catch (StreamAlreadyExistsException streamAlreadyExistsException) {
            Log.e(TAG, "Another stream which provides AppUsageDataRecord is already registered.");
        }
    }

    @Override
    public Stream<AppUsageDataRecord> generateNewStream() {
        return mStream;
    }

    @Override
    public boolean updateStream() {
        Log.d(TAG, "Update stream called.");
//        getScreenStatus();
//        getAppUsageUpdate();
        //appUsageDataRecord.setCreationTime();
        boolean usageAccessPermissionGranted = checkApplicationUsageAccess();

        if (!usageAccessPermissionGranted) {
            Log.d(TAG, "[testing app] user has not granted permission, need to bring them to the setting");
        }else {
            getScreenStatus();
            getAppUsageUpdate();
        }

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
        }        Log.d(TAG,"Screen_Status : "+Screen_Status+" LastestForegroundPackage : "+mLastestForegroundPackage+" LastestForegroundActivity : "+mLastestForegroundActivity);
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



        AppUsageDataRecord appUsageDataRecord;
        appUsageDataRecord = new AppUsageDataRecord(Screen_Status, mLastestForegroundPackage, mLastestForegroundActivity, mLastestForegroundPackageTime,
                String.valueOf(session_id), phone_session_id, screenshot, ImageName);

//        Log.d(TAG, "lastest package: " + mLastestForegroundPackage);
//        Log.d(TAG, "Is our target: " + isOurTarget(mLastestForegroundPackage));
        if(true) {
            if (appUsageDataRecord != null) {

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
        //                    AppUsageDataRecord newappUsageDataRecord = new AppUsageDataRecord("NA","NA","NA","NA");
        //                    Log.e(TAG, "Here:" + appUsageDataRecord);
        //                            newappUsageDataRecord.getScreenStatus();
        //                            newappUsageDataRecord.getLatestUsedApp();
        //                            newappUsageDataRecord.getLatestUsedAppTime();
        //                            newappUsageDataRecord.getRecentApps();
                    //appUsageDataRecord.getUsers());

                    mStream.add(appUsageDataRecord);
                    Log.e(TAG, "AppUsage to be sent to event bus" + appUsageDataRecord);
                    //Log.e(TAG, "ScreenStatus:" + getScreen());

                    EventBus.getDefault().post(appUsageDataRecord);

                    try {
                        db.appUsageDataRecordDao().insertAll(appUsageDataRecord);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }

                } else {
                    //AppUsageDataRecord newappUsageDataRecord = new AppUsageDataRecord();

        //                    appUsageDataRecord.getScreenStatus();
        //                            appUsageDataRecord.getLatestUsedApp();
        //                            appUsageDataRecord.getLatestForegroundActivity();
                    //appUsageDataRecord.getUsers());

                    mStream.add(appUsageDataRecord);
                    Log.e(TAG, "AppUsage to be sent to event bus" + appUsageDataRecord);

                    EventBus.getDefault().post(appUsageDataRecord);

                    try {
                        db.appUsageDataRecordDao().insertAll(appUsageDataRecord);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }
            }
            else{
                return true;
            }
        }
        return true;
    }

    public boolean isOurTarget(String pkgName){
        if(Arrays.asList(AppPack).contains(pkgName) || Arrays.asList(WebPack).contains(pkgName)
                || Arrays.asList(NewsPack).contains(pkgName) || Arrays.asList(PttPack).contains(pkgName)){
            return true;
        }
        return false;
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
        /** if we will update apps. first check if we have the permission**/
        userRecord = db.userDataRecordDao().getLastRecord();
        try {
            mAppUsageStreamGenerator = (AppUsageStreamGenerator) MinukuStreamManager.getInstance().getStreamGeneratorFor(AppUsageDataRecord.class);
        } catch (StreamNotFoundException e) {
            Log.d(TAG, "Initial MyAccessibility Service Failed");
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {

            //we first check the user has granted the permission of usage access. We need it for Android 5.0 and above
            boolean usageAccessPermissionGranted = checkApplicationUsageAccess();

            runAppUsageMainThread();

            if (!usageAccessPermissionGranted) {
                Log.d(TAG, "[testing app] user has not granted permission, need to bring them to the setting");
                //ask user to grant permission to app.
                //TODO: we only do this when the app information Is requested

//                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                mContext.startActivity(intent);

//                try{
//                    // delay 5 second, wait for user confirmed.
//                    Thread.sleep(5000);
//
//                } catch(InterruptedException e){
//                    e.printStackTrace();
//                }
//
//                onStreamRegistration();

            }
        }
    }

    public void runAppUsageMainThread(){

        Log.d(TAG, "runAppUsageMainThread") ;

        mMainThread = new Handler();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                boolean usageAccessPermissionGranted = checkApplicationUsageAccess();

                if (!usageAccessPermissionGranted) {
                    Log.d(TAG, "[testing app] user has not granted permission, need to bring them to the setting");
                }else {
                    getScreenStatus();
//                    getAppUsageUpdate();
//                    if(mAppUsageStreamGenerator != null)
//                        mAppUsageStreamGenerator.updateStream();
                }
                mMainThread.postDelayed(this, 1 * 1000);
            }
        };

        mMainThread.post(runnable);
    }

    @Override
    public void offer(AppUsageDataRecord dataRecord) {
        Log.e(TAG, "Offer for AppUsage data record does nothing!");
    }

    /**
     * check the current foreground activity
     *
     * IMPORTANT NOTE:
     * Since Android API 5.0 APIS (sdk 21), Android changes the way we can get app information
     * Since API 21 we're not able to use getRunningTasks to get the top acitivty.
     * Instead, we need to use XXX to get recent statistics of app use.
     *
     * So below we'll check the sdk level of the phone to find out how we can get app information
     */

    private boolean checkApplicationUsageAccess() {
        boolean granted = false;

        //check whether the user has granted permission to Usage Access....If not, we direct them to the Usage Setting
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            try {
                PackageManager packageManager = mContext.getPackageManager();
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(mContext.getPackageName(), 0);
                AppOpsManager appOpsManager = (AppOpsManager) mContext.getSystemService(Context.APP_OPS_SERVICE);

                int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                        android.os.Process.myUid(), mContext.getPackageName());

                granted = mode == AppOpsManager.MODE_ALLOWED;
                Log.d(TAG, "[test source being requested]checkApplicationUsageAccess mode mIs : " + mode + " granted: " + granted);

            } catch (PackageManager.NameNotFoundException e) {
                Log.d(TAG, "[testing app]checkApplicationUsageAccess somthing mIs wrong");
            }
        }
        return granted;
    }

    protected void getAppUsageUpdate() {

        Log.d(TAG, "test source being requested [testing app]: getAppUsageUpdate");
        String currentApp = "NA";

        /**
         * we have to check whether the phone mIs above API 21 or not.
         */
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {

            //UsageStatsManager mIs available after Lollipop
            UsageStatsManager usm = (UsageStatsManager) mContext.getSystemService(Service.USAGE_STATS_SERVICE);

            List<UsageStats> appList = null;

            Log.d(TAG, "test source being requested [testing app] API 21 query usage between:  " +
                    String.valueOf( getCurrentTimeInMillis() - mApplicaitonUsageSinceLastDurationInMilliseconds)
                    + " and " + getCurrentTimeInMillis());


            //get the application usage statistics
            appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
                    //start time
                    getCurrentTimeInMillis()- mApplicaitonUsageSinceLastDurationInMilliseconds,
                    //end time: until now
                    getCurrentTimeInMillis());

            mRecentUsedAppsInLastHour = "";


            //if there's an app list
            if (appList != null && appList.size() > 0) {

                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                for (UsageStats usageStats : appList) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                    /*Log.d(TAG, "test app:  " + "ScheduleAndSampleManager.getTimeString(usageStats.getLastTimeUsed())" +
                            " usage stats " + usageStats.getPackageName() + " total time in foreground " + usageStats.getTotalTimeInForeground()/60000
                            + " between " + "ScheduleAndSampleManager.getTimeString(usageStats.getFirstTimeStamp())" + " and " + "ScheduleAndSampleManager.getTimeString(usageStats.getLastTimeStamp())");
*/
                }

                if (mySortedMap != null && !mySortedMap.isEmpty()) {

                    mLastestForegroundPackage = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                    mLastestForegroundPackageTime = ScheduleAndSampleManager.getTimeString(mySortedMap.get(mySortedMap.lastKey()).getLastTimeUsed());

                    Log.d(TAG, "test app: "  +  mLastestForegroundPackage + " time " +
                            mLastestForegroundPackageTime);
                }


                //create a string for mRecentUsedAppsInLastHour
                for(Map.Entry<Long, UsageStats> entry : mySortedMap.entrySet()) {
                    long key = entry.getKey();
                    UsageStats stats = entry.getValue();

                    //mRecentUsedAppsInLastHour += stats.getPackageName() + ":" + ScheduleAndSampleManager.getTimeString(key);
                    if (key!=mySortedMap.lastKey())
                        mRecentUsedAppsInLastHour += "::";

                }


            }
        }


        else {
            getForegroundActivityBeforeAPI21();
        }

    }

    protected void getForegroundActivityBeforeAPI21(){

        String curRunningForegrndActivity="";
        String curRunningForegrndPackNamge="";
        /** get the info from the currently foreground running activity **/
        List<ActivityManager.RunningTaskInfo> taskInfo=null;

        //get the latest (or currently running) foreground activity and package name
        if ( mActivityManager!=null){

            taskInfo = mActivityManager.getRunningTasks(1);

            curRunningForegrndActivity = taskInfo.get(0).topActivity.getClassName();
            curRunningForegrndPackNamge = taskInfo.get(0).topActivity.getPackageName();

            Log.d(TAG, "test app os version " +android.os.Build.VERSION.SDK_INT + " under 21 "
                    + curRunningForegrndActivity + " " + curRunningForegrndPackNamge );

            //store the running activity and its package name in the Context Extractor
            if(taskInfo!=null){
                setCurrentForegroundActivityAndPackage(curRunningForegrndActivity, curRunningForegrndPackNamge);
            }

        }

    }

    public void setCurrentForegroundActivityAndPackage(String curForegroundActivity, String curForegroundPackage) {

        mLastestForegroundActivity=curForegroundActivity;
        mLastestForegroundPackage=curForegroundPackage;

        Log.d(TAG, "[setCurrentForegroundActivityAndPackage] the current running package mIs " + mLastestForegroundActivity + " and the activity mIs " + mLastestForegroundPackage);
    }

    public String getScreenStatus() {
        Log.e(TAG, "GetScreenStatus called.");
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {

            //use isInteractive after api 20

            if (mPowerManager.isInteractive()) {
                if(first) {
                    first = false;
                    userRecord = db.userDataRecordDao().getLastRecord();
                    if(userRecord!=null) {
                        long _id = userRecord.get_id();
                        SharedVariables.phone_session = userRecord.getPhoneSession();
                        SharedVariables.phone_session++;
                        sharedPrefs.edit().putLong("Phone_SessionID", SharedVariables.phone_session).apply();
                        db.userDataRecordDao().updatePhoneSessionID(_id, SharedVariables.phone_session);
                    }
                    android.util.Log.d(TAG, "Phone session id: " + SharedVariables.phone_session);
                }
                Log.d(TAG, "Screen_Status: Interactive");
                Screen_Status = STRING_INTERACTIVE;
            }
            else {
                if(!first) {
//                    SharedVariables.phone_session = -1L;
                    sharedPrefs.edit().putLong("Phone_SessionID", -1).apply();
                    first = true;
                    android.util.Log.d(TAG, "Phone session id: -1");
                }
                Log.d(TAG, "Screen_Status: SCREEN_OFF");
                Screen_Status = STRING_SCREEN_OFF;
            }
        }
        //before API20, we use screen on or off
        else {
            if(mPowerManager.isScreenOn())
                Screen_Status = STRING_SCREEN_ON;
            else
                Screen_Status = STRING_SCREEN_OFF;

        }

        Log.e(TAG, "test source being requested [testing app] SCREEN:  " + Screen_Status);

        return Screen_Status;
    }

//    private void loadAppAndPackage() {
//
//        if (mAppPackageNameHmap==null){
//            mAppPackageNameHmap = new HashMap<String, String>();
//        }
//
//        Resources res = mContext.getResources();
//
//        String[] appNames = res.getStringArray(R.array.app);
//
//        for (int i=0; i<appNames.length; i++){
//
//            String app_package = appNames[i];
//
//            String [] strs = app_package.split(":");
//
//            String appName = strs[0];
//            String packageName = strs[1];
//            Log.d(TAG, "the app names are puting key: " + packageName + " value: " + appName);
//            mAppPackageNameHmap.put(packageName, appName);
//
//        }
//
//
//    }


}