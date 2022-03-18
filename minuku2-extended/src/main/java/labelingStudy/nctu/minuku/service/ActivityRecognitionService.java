package labelingStudy.nctu.minuku.service;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.opencsv.CSVWriter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import labelingStudy.nctu.minuku.manager.MinukuStreamManager;
import labelingStudy.nctu.minuku.model.DataRecord.ActivityRecognitionDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.TransportationModeDataRecord;
import labelingStudy.nctu.minuku.streamgenerator.ActivityRecognitionStreamGenerator;
import labelingStudy.nctu.minuku.streamgenerator.TransportationModeStreamGenerator;
import labelingStudy.nctu.minukucore.exception.StreamNotFoundException;

/**
 * Created by Lawrence on 2017/5/22.
 */

public class ActivityRecognitionService extends IntentService {

    private final String TAG = "ActivityRecognitionS";

    private String Latest_mMostProbableActivitytype;
    private DetectedActivity mMostProbableActivity;
    private List<DetectedActivity> mProbableActivities;
    private CSVWriter csv_writer = null;


    //for saving a set of activity records
    private static ArrayList<ActivityRecognitionDataRecord> mActivityRecognitionRecords;

    public static ActivityRecognitionStreamGenerator mActivityRecognitionStreamGenerator;
    public static TransportationModeStreamGenerator mTransportationModeStreamGenerator;
    private Timer ARRecordExpirationTimer;
    private Timer ReplayTimer;
    private TimerTask ARRecordExpirationTimerTask;
    private TimerTask ReplayTimerTask;

    //private static String detectedtime;
    private long detectedtime;

    private Boolean updateOrNot = false;

    private static Context serviceInstance = null;

    private SharedPreferences sharedPrefs;

    public ActivityRecognitionService() {
        super("ActivityRecognitionService");

        Log.d(TAG, "ActivityRecognitionService");
        serviceInstance = this;

        //mActivityRecognitionManager = ContextManager.getActivityRecognitionManager();

//        startReplayARRecordTimer();
//        startARRecordExpirationTimer();
    }

    public ActivityRecognitionService(String name) {
        super(name);

        Log.d(TAG, "ActivityRecognitionService");
        serviceInstance = this;
    }
    public String getActivityString(Context context, int detectedActivityType) {
        Resources resources = context.getResources();
        switch(detectedActivityType) {
            case DetectedActivity.IN_VEHICLE:
                return "in_vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "on_bicycle";
            case DetectedActivity.ON_FOOT:
                return "on_foot";
            case DetectedActivity.RUNNING:
                return "running";
            case DetectedActivity.STILL:
                return "still";
            case DetectedActivity.TILTING:
                return "tilting";
            case DetectedActivity.UNKNOWN:
                return "unknown";
            case DetectedActivity.WALKING:
                return "walking";
            default:
                return "undefined";
        }
    }
    @SuppressLint("LongLogTag")
    @Override
    protected void onHandleIntent(final Intent intent) {

        Log.d(TAG, "[test replay] entering onHandleIntent");
//        CSVHelper.storeToCSV(CSV_Act, "entering onHandleIntent");
        /**  move to TriggerManager  **/
        //TODO triggerManager situationManager, triggerManager: replace ModeWork.work. , situationManager: replace ModeWork.condition. 放transportationManager(In Minuku).

        if(ActivityRecognitionResult.hasResult(intent)) {
            try {
                mActivityRecognitionStreamGenerator = (ActivityRecognitionStreamGenerator) MinukuStreamManager.getInstance().getStreamGeneratorFor(ActivityRecognitionDataRecord.class);
                mTransportationModeStreamGenerator = (TransportationModeStreamGenerator) MinukuStreamManager.getInstance().getStreamGeneratorFor(TransportationModeDataRecord.class);
            }catch (StreamNotFoundException e){
                e.printStackTrace();
            }

            ActivityRecognitionResult activity = ActivityRecognitionResult.extractResult(intent);

            mProbableActivities = activity.getProbableActivities();
            mMostProbableActivity = activity.getMostProbableActivity();
            detectedtime = new Date().getTime(); //TODO might be wrong, be aware for it!!

//            CSVHelper.storeToCSV(CSV_Act,mProbableActivities.toString());
//            CSVHelper.storeToCSV(CSV_Act,mMostProbableActivity.toString());

            ArrayList<DetectedActivity> detectedActivities = (ArrayList) activity.getProbableActivities();
            for (DetectedActivity da: detectedActivities) {
                Log.i(TAG, getActivityString(
                        getApplicationContext(),
                        da.getType()) + " " + da.getConfidence() + "%"
                );
            }
            Log.d(TAG, "There are activity result");


            //            ActivityRecognitionDataRecord record = new ActivityRecognitionDataRecord(mMostProbableActivity,mProbableActivities,detectedtime,"", ReadNews);
            //
            //            record.setProbableActivities(mProbableActivities);
            //            record.setMostProbableActivity(mMostProbableActivity);
            //            record.setDetectedtime(detectedtime);



            //            Log.d(TAG, "[test replay] [test ActivityRecognition]" +   mMostProbableActivity.toString());
            Log.d(TAG, "[test replay] [test ActivityRecognition]" +   mProbableActivities.toString());

            try {
                if (mProbableActivities != null && mMostProbableActivity != null){
                    //                    Log.d("in if loop", String.valueOf(mProbableActivities));


                    /*  cancel setting because we want to directly feed activity data in the test file */
                    mActivityRecognitionStreamGenerator.setActivitiesandDetectedtime(mProbableActivities, mMostProbableActivity, detectedtime);

                    Log.d(TAG,"Before Update Stream");
//                    CSVHelper.storeToCSV(CSV_Act,"Before Update Stream");

//                    mActivityRecognitionStreamGenerator.updateStream();
//                    mTransportationModeStreamGenerator.updateStream();

                    Log.d("2222222222", String.valueOf(mMostProbableActivity));

                    Log.d(TAG, "[test replay] before store to CSV in AR Service");
                    //write transportation mode with the received activity data
                    //StoreToCSV(new Date().getTime(), record, record);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
//            new Thread(new Runnable() {
//                public void run() {
//                    try {
//                        mActivityRecognitionStreamGenerator = (ActivityRecognitionStreamGenerator) MinukuStreamManager.getInstance().getStreamGeneratorFor(ActivityRecognitionDataRecord.class);
//                        mTransportationModeStreamGenerator = (TransportationModeStreamGenerator) MinukuStreamManager.getInstance().getStreamGeneratorFor(TransportationModeDataRecord.class);
//                    }catch (StreamNotFoundException e){
//                        e.printStackTrace();
//                    }
//
//
//        //            ActivityRecognitionDataRecord record = new ActivityRecognitionDataRecord(mMostProbableActivity,mProbableActivities,detectedtime,"", ReadNews);
//        //
//        //            record.setProbableActivities(mProbableActivities);
//        //            record.setMostProbableActivity(mMostProbableActivity);
//        //            record.setDetectedtime(detectedtime);
//
//
//
//        //            Log.d(TAG, "[test replay] [test ActivityRecognition]" +   mMostProbableActivity.toString());
//                    Log.d(TAG, "[test replay] [test ActivityRecognition]" +   mProbableActivities.toString());
//
//                    try {
//                        if (mProbableActivities != null && mMostProbableActivity != null){
//        //                    Log.d("in if loop", String.valueOf(mProbableActivities));
//
//
//                             /*  cancel setting because we want to directly feed activity data in the test file */
//                            mActivityRecognitionStreamGenerator.setActivitiesandDetectedtime(mProbableActivities, mMostProbableActivity, detectedtime);
//
//                            Log.d(TAG,"Before Update Stream");
//                            CSVHelper.storeToCSV(CSV_Act,"Before Update Stream");
//
//                            mActivityRecognitionStreamGenerator.updateStream();
//                            mTransportationModeStreamGenerator.updateStream();
//
//                            Log.d("2222222222", String.valueOf(mMostProbableActivity));
//
//                            Log.d(TAG, "[test replay] before store to CSV in AR Service");
//                            //write transportation mode with the received activity data
//                            //StoreToCSV(new Date().getTime(), record, record);
//                        }
//                    }catch(Exception e){
//                        e.printStackTrace();
//                    }
//                }
//            }).start();
        }
    }

//    public void RePlayActivityRecordTimerTask() {
//
//
//        ReplayTimerTask = new TimerTask() {
//
//            int activityRecordCurIndex = 0;
//            int sec = 0;
//            public void run() {
//
//                sec++;
//
//                //for every 5 seconds and if we still have more AR labels in the list to reply, we will set an AR label to the streamgeneratro
//                if(sec%5 == 0 && mActivityRecognitionRecords.size()>0 && activityRecordCurIndex < mActivityRecognitionRecords.size()-1){
//
//                    try {
//                        mActivityRecognitionStreamGenerator = (ActivityRecognitionStreamGenerator) MinukuStreamManager.getInstance().getStreamGeneratorFor(ActivityRecognitionDataRecord.class);
//
//
//                        ActivityRecognitionDataRecord activityRecognitionDataRecord = mActivityRecognitionRecords.get(activityRecordCurIndex);
//
//                        mProbableActivities = activityRecognitionDataRecord.getProbableActivities();
//                        mMostProbableActivity = activityRecognitionDataRecord.getMostProbableActivity();
//                        detectedtime = new Date().getTime(); //TODO might be wrong, be aware for it!!
//
//                        Log.d("ARService", "[test replay] test trip going to feed " +   activityRecognitionDataRecord.getDetectedtime() +  " :"  +  activityRecognitionDataRecord.getProbableActivities()  +  " : " +activityRecognitionDataRecord.getMostProbableActivity()    + " at index " + activityRecordCurIndex  + " to the AR streamgenerator");
//
//                        //user the record from mActivityRecognitionRecords to update the  mActivityRecognitionStreamGenerator
//                        mActivityRecognitionStreamGenerator.setActivitiesandDetectedtime(mProbableActivities, mMostProbableActivity, detectedtime);
//
//                        //move on to the next activity Record
//                        activityRecordCurIndex++;
//
//
//
//                    }catch (StreamNotFoundException e){
//                        e.printStackTrace();
//                    }
//                }
//            }
//        };
//    }


    /** create NA activity label when it's over 10 minutes not receiving an AR label
     * the timeer is reset when the onHandleEvent receives a label**/
    public void initializeARRecordExpirationTimerTask() {

        ARRecordExpirationTimerTask = new TimerTask() {

            int sec = 0;
            public void run() {

                sec++;

                //if counting until ten minutes
                if(sec == 10*60){

                    Log.d("ARService", "[test replay] it's time to create NA activity because not receiving for a long time"  );
                    try {
                        mActivityRecognitionStreamGenerator = (ActivityRecognitionStreamGenerator) MinukuStreamManager.getInstance().getStreamGeneratorFor(ActivityRecognitionDataRecord.class);
                    }catch (StreamNotFoundException e){
                        e.printStackTrace();
                    }

                    ActivityRecognitionDataRecord activityRecognitionDataRecord
                            = new ActivityRecognitionDataRecord();
                    //update the empty AR to MinukuStreamManager
                    MinukuStreamManager.getInstance().setActivityRecognitionDataRecord(activityRecognitionDataRecord);

                }
            }
        };
    }

    public void startARRecordExpirationTimer() {

        //set a new Timer
        ARRecordExpirationTimer = new Timer();

        //initialize the TimerTask's job
        initializeARRecordExpirationTimerTask();

        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
        ARRecordExpirationTimer.schedule(ARRecordExpirationTimerTask,0,1000);

    }

    public void stopARRecordExpirationTimer() {
        //stop the timer, if it's not already null
        if (ARRecordExpirationTimer != null) {
            ARRecordExpirationTimer.cancel();
            ARRecordExpirationTimer = null;
        }
    }

//    public void startReplayARRecordTimer() {
//
//        //set a new Timer
//        ReplayTimer = new Timer();
//
//        //start the timertask for replay
//        RePlayActivityRecordTimerTask();
//
//        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
//        ReplayTimer.schedule(ReplayTimerTask,0,1000);
//
//    }




    public static boolean isServiceRunning() {
        return serviceInstance != null;
    }

    public long getCurrentTimeInMillis(){
        //get timzone
        TimeZone tz = TimeZone.getDefault();
        Calendar cal = Calendar.getInstance(tz);
        long t = cal.getTimeInMillis();
        return t;
    }

    public static void addActivityRecognitionRecord(ActivityRecognitionDataRecord record) {
        getActivityRecognitionRecords().add(record);
//        Log.d("ARService", "[test replay] adding " +   record.toString()  + " to ActivityRecognitionRecords in ActivityRecognitionService");
    }

    public static ArrayList<ActivityRecognitionDataRecord> getActivityRecognitionRecords() {

        if (mActivityRecognitionRecords==null){
            mActivityRecognitionRecords = new ArrayList<ActivityRecognitionDataRecord>();
        }
        return mActivityRecognitionRecords;

    }

    /**
     * write receive AR and latest AR to the transportation log
     * @param timestamp
     * @param received_AR
     * @param latest_AR
     */
//    @SuppressLint("LongLogTag")
//    public void StoreToCSV(long timestamp, ActivityRecognitionDataRecord received_AR, ActivityRecognitionDataRecord latest_AR){
//
//        String sFileName = "TransportationMode.csv";
//        Log.d("ARService", "[test replay] StoreToCSV entering StoreToCSV ");
//
//        try{
//            File root = new File(Environment.getExternalStorageDirectory() + Constants.PACKAGE_DIRECTORY_PATH);
//            Log.d("ARService", "[test replay] StoreToCSV after root");
//            if (!root.exists()) {
//                root.mkdirs();
//            }
//
//            csv_writer = new CSVWriter(new FileWriter(Environment.getExternalStorageDirectory()+ Constants.PACKAGE_DIRECTORY_PATH+sFileName,true));
//
//            List<String[]> data = new ArrayList<String[]>();
//
//            String timeString = ScheduleAndSampleManager.getTimeString(timestamp);
//
//            Log.d("ARService", "[test replay] StoreToCSV before definint string");
//            String rec_AR_String = "";
//            String latest_AR_String = "";
//            String transportation = "";
//            String state = "";
//
//            Log.d("ARService", "[test replay] StoreToCSV receive AR is " + received_AR.toString());
//
//            if (received_AR!=null){
//
//                for (int i=0; i<received_AR.getProbableActivities().size(); i++){
//
//                    if (i!=0){
//                        rec_AR_String+= Constants.ACTIVITY_DELIMITER;
//                    }
//                    DetectedActivity activity =  received_AR.getProbableActivities().get(i);
//                    rec_AR_String += ActivityRecognitionStreamGenerator.getActivityNameFromType(activity.getType());
//                    rec_AR_String += Constants.ACTIVITY_CONFIDENCE_CONNECTOR;
//                    rec_AR_String += activity.getConfidence();
//
//                }
//
//                Log.d("ARService", "[test replay] StoreToCSV writing receive AR CSV " +  rec_AR_String);
//            }
//
//            if (latest_AR!=null){
//                for (int i=0; i<latest_AR.getProbableActivities().size(); i++){
//
//                    if (i!=0){
//                        latest_AR_String+= Constants.ACTIVITY_DELIMITER;
//                    }
//                    DetectedActivity activity =  latest_AR.getProbableActivities().get(i);
//                    latest_AR_String += ActivityRecognitionStreamGenerator.getActivityNameFromType(activity.getType());
//                    latest_AR_String += Constants.ACTIVITY_CONFIDENCE_CONNECTOR;
//                    latest_AR_String += activity.getConfidence();
//
//                }
//                Log.d("ARService", "[test replay] StoreToCSV writing latest AR data to CSV " + latest_AR_String);
//            }
//
//            Log.d("ARService", "[test replay] StoreToCSV writing data to CSV");
//
//            //write transportation mode
//            data.add(new String[]{String.valueOf(timestamp), timeString, rec_AR_String, latest_AR_String, transportation, state, "", "", ""});
//
//            csv_writer.writeAll(data);
//
//            csv_writer.close();
//
//        }catch (Exception e){
//            e.printStackTrace();
//            Log.e(TAG, "exception", e);
//        }
//    }


}
