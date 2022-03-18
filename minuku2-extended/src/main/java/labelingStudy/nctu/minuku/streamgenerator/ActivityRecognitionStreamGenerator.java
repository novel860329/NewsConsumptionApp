package labelingStudy.nctu.minuku.streamgenerator;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import labelingStudy.nctu.minuku.DBHelper.appDatabase;
import labelingStudy.nctu.minuku.Utilities.ScheduleAndSampleManager;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.manager.MinukuStreamManager;
import labelingStudy.nctu.minuku.model.DataRecord.ActivityRecognitionDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.TransportationModeDataRecord;
import labelingStudy.nctu.minuku.service.ActivityRecognitionService;
import labelingStudy.nctu.minuku.stream.ActivityRecognitionStream;
import labelingStudy.nctu.minukucore.exception.StreamAlreadyExistsException;
import labelingStudy.nctu.minukucore.exception.StreamNotFoundException;
import labelingStudy.nctu.minukucore.stream.Stream;

//import com.google.android.gms.auth.api.signin.GoogleSignInClient;
//import com.google.android.gms.location.ActivityRecognitionClient;

/**
 * Created by Lawrence on 2017/5/15.
 */

public class ActivityRecognitionStreamGenerator extends AndroidStreamGenerator<ActivityRecognitionDataRecord> implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    public final static String TAG = "ActivityRecognition";
    private String LastImageName = "";
    private PendingIntent mActivityRecognitionPendingIntent;

    public static final int RC_SIGN_IN = 9001;

//    public static GoogleSignInClient mSignInClient;
    private static GoogleApiClient mGoogleApiClient;

    appDatabase db;
    /**label **/
    public static final String STRING_DETECTED_ACTIVITY_IN_VEHICLE = "in_vehicle";
    public static final String STRING_DETECTED_ACTIVITY_ON_FOOT = "on_foot";
    public static final String STRING_DETECTED_ACTIVITY_WALKING = "walking";
    public static final String STRING_DETECTED_ACTIVITY_RUNNING = "running";
    public static final String STRING_DETECTED_ACTIVITY_TILTING = "tilting";
    public static final String STRING_DETECTED_ACTIVITY_STILL = "still";
    public static final String STRING_DETECTED_ACTIVITY_ON_BICYCLE = "on_bicycle";
    public static final String STRING_DETECTED_ACTIVITY_UNKNOWN = "unknown";
    public static final String STRING_DETECTED_ACTIVITY_NA = "NA";
    public static final int NO_ACTIVITY_TYPE = -1;

    /**Properties for Record**/
    public static final String RECORD_DATA_PROPERTY_NAME = "DetectedActivities";

    protected long recordCount;

    private Context mContext;
    private ActivityRecognitionStream mStream;

    private ActivityRecognitionDataRecord activityRecognitionDataRecord;

    /** KeepAlive **/
    protected int KEEPALIVE_MINUTE = 3;
    protected long sKeepalive;

    public static List<DetectedActivity> sProbableActivities;
    public static DetectedActivity sMostProbableActivity;
    public static boolean ReadNews;
    private static long sLatestDetectionTime;

    public static int ACTIVITY_RECOGNITION_DEFAULT_UPDATE_INTERVAL_IN_SECONDS = 0;
    public static long ACTIVITY_RECOGNITION_DEFAULT_UPDATE_INTERVAL =
            ACTIVITY_RECOGNITION_DEFAULT_UPDATE_INTERVAL_IN_SECONDS * Constants.MILLISECONDS_PER_SECOND;

    public static ArrayList<ActivityRecognitionDataRecord> mLocalRecordPool;

    private static ActivityRecognitionStreamGenerator instance;

    private SharedPreferences sharedPrefs;

    public ActivityRecognitionStreamGenerator(Context applicationContext) { //,Context mContext
        super(applicationContext);
        //this.mContext = mMainServiceContext;
        Log.d(TAG, "ActivityRecognitionStreamGenerator");
        this.mContext = applicationContext;
        this.mStream = new ActivityRecognitionStream(Constants.LOCATION_QUEUE_SIZE);
        db = appDatabase.getDatabase(applicationContext);

        mLocalRecordPool = new ArrayList<ActivityRecognitionDataRecord>();

        sharedPrefs = mContext.getSharedPreferences(Constants.sharedPrefString, mContext.MODE_PRIVATE);

        ActivityRecognitionStreamGenerator.instance = this;

        sLatestDetectionTime = Constants.INVALID_TIME_VALUE;

        recordCount = 0;
        sKeepalive = KEEPALIVE_MINUTE * Constants.MILLISECONDS_PER_MINUTE;

        this.register();
    }

    public static ActivityRecognitionStreamGenerator getInstance(Context applicationContext) {

        if(ActivityRecognitionStreamGenerator.instance == null) {
            try {
                Log.d(TAG,"creating new ActivityRecognitionStreamGenerator.");
                ActivityRecognitionStreamGenerator.instance = new ActivityRecognitionStreamGenerator(applicationContext);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ActivityRecognitionStreamGenerator.instance;
    }

    @Override
    public void register() {
        Log.d(TAG, "Registering with StreamManager.");
        try {
            MinukuStreamManager.getInstance().register(mStream, ActivityRecognitionDataRecord.class, this);
        } catch (StreamNotFoundException streamNotFoundException) {
            Log.e(TAG, "One of the streams on which LocationDataRecord depends in not found.");
        } catch (StreamAlreadyExistsException streamAlreadyExistsException) {
            Log.e(TAG, "Another stream which provides LocationDataRecord is already registered.");
        }
    }

    @Override
    public void onStreamRegistration() {

        buildGoogleApiClient();
    }

    protected synchronized void buildGoogleApiClient() {

        if (mGoogleApiClient==null){

            mGoogleApiClient =
                    new GoogleApiClient.Builder(mApplicationContext) // "mApplicationContext" is inspired by LocationStreamGenerator,it might not wrong.
                            .addApi(ActivityRecognition.API)
                            .addConnectionCallbacks(this)
                            .addOnConnectionFailedListener(this)
                            .build();

            mGoogleApiClient.connect();
        }
    }

    @Override
    public Stream<ActivityRecognitionDataRecord> generateNewStream() {
        return mStream;
    }

    @SuppressLint("LongLogTag")
    @Override
    public boolean updateStream() {
        Log.d(TAG, "Update stream called.");

        ReadNews = mContext.getSharedPreferences("test",Context.MODE_PRIVATE).getBoolean("ReadNews",false);
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
                ImageName = "";
                sharedPrefs.edit().putString("CaptureImgName", "").apply();
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

        activityRecognitionDataRecord = new ActivityRecognitionDataRecord(sMostProbableActivity, sProbableActivities, sLatestDetectionTime, String.valueOf(session_id), phone_session_id, screenshot, ImageName);


        //if there don't have any updates for 10 minutes, add the NA one to represent it
        if((ScheduleAndSampleManager.getCurrentTimeInMillis() - sLatestDetectionTime) >= Constants.MILLISECONDS_PER_MINUTE * 10
                && (sLatestDetectionTime != Constants.INVALID_TIME_VALUE)){

            DetectedActivity initialDetectedActivity = getInitialDetectedActivity();

            ArrayList<DetectedActivity> initialDetectedActivities = new ArrayList<>();
            initialDetectedActivities.add(initialDetectedActivity);

            ActivityRecognitionDataRecord activityRecognitionDataRecord;
            activityRecognitionDataRecord = new ActivityRecognitionDataRecord(initialDetectedActivity, initialDetectedActivities, ScheduleAndSampleManager.getCurrentTimeInMillis(), String.valueOf(session_id), phone_session_id, screenshot, ImageName);

            mStream.add(activityRecognitionDataRecord);
            try {
                //Log.d(TAG, "Before insert to db:" + sMostProbableActivity + " " + sProbableActivities + " " + sLatestDetectionTime + " " + String.valueOf(session_id));
                db.activityRecognitionDataRecordDao().insertAll(activityRecognitionDataRecord);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        MinukuStreamManager.getInstance().setActivityRecognitionDataRecord(activityRecognitionDataRecord);

        if(activityRecognitionDataRecord!=null) {
//            CSVHelper.storeToCSV(CSV_Act, "In Activity update stream");
            mStream.add(activityRecognitionDataRecord);
//            Log.e(TAG, "Activity to be sent to event bus" + activityRecognitionDataRecord);

            EventBus.getDefault().post(activityRecognitionDataRecord);
            try {
                //Log.d(TAG, "Before insert to db:" + sMostProbableActivity + " " + sProbableActivities + " " + sLatestDetectionTime + " " + String.valueOf(session_id));
                db.activityRecognitionDataRecordDao().insertAll(activityRecognitionDataRecord);
            } catch (Exception e) {
                Log.e(TAG, "DAOException", e);
                e.printStackTrace();
                return false;
            }
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
    public void offer(ActivityRecognitionDataRecord dataRecord) {
        Log.e(TAG, "Offer for ActivityRecognition data record does nothing!");
    }

    @Override
    public void onConnected(Bundle bundle) {

        Log.e(TAG,"onConnected");
//        int resultCode =
//                GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(mContext);
//        // If Google Play services is available
//        if (ConnectionResult.SUCCESS == resultCode) {
//            // In debug mode, log the status
//            Log.d("ActivityRecognition",
//                    "Google Play services is available.");
//        } else {
//            // Get the error dialog from Google Play services
//            Log.d("ActivityRecognition",
//                    "Google Play services is unavailable.");
//        }

        startActivityRecognitionUpdates();
    }

    private void startActivityRecognitionUpdates() {

        Log.d(TAG, "[startActivityRecognitionUpdates]");

        Log.d(TAG, "mGoogleApiClient: " + mGoogleApiClient);
        Log.d(TAG, "ActivityRecognitionService is running: " + ActivityRecognitionService.isServiceRunning());
        mActivityRecognitionPendingIntent = createRequestPendingIntent();
        //request activity recognition update
        if (com.google.android.gms.location.ActivityRecognition.ActivityRecognitionApi!=null && !ActivityRecognitionService.isServiceRunning()){
            Log.d(TAG, "request activity recognition update");
//            ActivityRecognitionClient activityRecognitionClient = ActivityRecognition.getClient(mApplicationContext);
//            mActivityRecognitionPendingIntent = createRequestPendingIntent();
//            Task task = activityRecognitionClient.requestActivityUpdates(ACTIVITY_RECOGNITION_DEFAULT_UPDATE_INTERVAL, mActivityRecognitionPendingIntent);
//            task.addOnSuccessListener(new OnSuccessListener<Void>() {
//                @Override
//                public void onSuccess(Void result) {
//                    Log.d(TAG, "task success listen");
//                    mActivityRecognitionPendingIntent = createRequestPendingIntent();
//                }
//            });
//            task.addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    Log.d(TAG, "task fail listen");
//                }
//            });
            com.google.android.gms.location.ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                    mGoogleApiClient,
                    ACTIVITY_RECOGNITION_DEFAULT_UPDATE_INTERVAL,//detectionIntervalMillis
                    mActivityRecognitionPendingIntent);   //callbackIntent
        }
    }

    private PendingIntent createRequestPendingIntent() {
        Log.d(TAG, "createRequestPendingIntent");
        // If the PendingIntent already exists
        if (mActivityRecognitionPendingIntent != null) {
            Log.d(TAG, "IntentService not null");
            return mActivityRecognitionPendingIntent;
            // If no PendingIntent exists
        } else {
            // Create an Intent pointing to the IntentService
            Log.d(TAG, "Create an Intent pointing to the IntentService");
            Intent intent = new Intent(
                    mApplicationContext, ActivityRecognitionService.class);

            PendingIntent pendingIntent =
                    PendingIntent.getService(mApplicationContext, //mApplicationContext || mContext
                            0,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT);

            mActivityRecognitionPendingIntent = pendingIntent;
            return pendingIntent;
        }
    }

    @SuppressLint("LongLogTag")
    public void setActivitiesandDetectedtime (List<DetectedActivity> probableActivities, DetectedActivity mostProbableActivity, long detectedtime) {
        //set activities

        //set a list of probable activities
        setProbableActivities(probableActivities);
        //set the most probable activity
        setMostProbableActivity(mostProbableActivity);

        setDetectedtime(detectedtime);

        Log.d(TAG,detectedtime+"||"+ mostProbableActivity);
//        CSVHelper.storeToCSV(CSV_Act,"set Activity and detect time");

        // Assume isRequested.
        if(probableActivities!=null&&mostProbableActivity!=null)
            saveRecordToLocalRecordPool(mostProbableActivity,detectedtime);

    }

    public void saveRecordToLocalRecordPool(DetectedActivity MostProbableActivity,long Detectedtime) {
        /** create a Record to save timestamp, session it belongs to, and Data**/
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
        ActivityRecognitionDataRecord record;
        record = new ActivityRecognitionDataRecord(MostProbableActivity, Detectedtime, String.valueOf(session_id), phone_session_id, screenshot, ImageName);

        record.setProbableActivities(sProbableActivities);

        JSONObject data = new JSONObject();

        //also set data:
        JSONArray activitiesJSON = new JSONArray();

        //add all activities to JSONArray
        for (int i=0; i<sProbableActivities.size(); i++){
            DetectedActivity detectedActivity =  sProbableActivities.get(i);
            String activityAndConfidence = getActivityNameFromType(detectedActivity.getType()) + Constants.ACTIVITY_DELIMITER + detectedActivity.getConfidence();
            activitiesJSON.put(activityAndConfidence);
        }

        //add activityJSON Array to data
        try {
            data.put(RECORD_DATA_PROPERTY_NAME, activitiesJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        /**we set data in Record**/
        record.setData(data);
        record.setTimestamp(sLatestDetectionTime);

        Log.d(TAG, "testing saving records at " + record.getTimeString() + " data: " + record.getData());

//        CSVHelper.storeToCSV(CSV_Act,"saveRecordToLocalRecordPool data: " + record.getData().toString());

        addRecord(record, session_id, phone_session_id, screenshot, ImageName);

    }

    private DetectedActivity getInitialDetectedActivity(){

        return new DetectedActivity(-1, 100);
    }

    protected void addRecord(ActivityRecognitionDataRecord activityRecognitionDataRecord, long session_id, long phone_session_id, String screenshot, String ImageName) {

        /**1. add record to the local pool **/
        long id = recordCount++;
        activityRecognitionDataRecord.setID(id);
        Log.d(TAG,"CreateTime:" + activityRecognitionDataRecord.getCreationTime()+ " MostProbableActivity:"+activityRecognitionDataRecord.getMostProbableActivity());

        mLocalRecordPool.add(activityRecognitionDataRecord); //it's working.
        Log.d(TAG, "[test logging]add record " + "logged at " + activityRecognitionDataRecord.getTimeString() );

        /**2. check whether we should remove old record **/
        removeOutDatedRecord();
        //**** update the latest ActivityRecognitionDataRecord in mLocalRecordPool to MinukuStreamManager;
        mLocalRecordPool.get(mLocalRecordPool.size()-1).setID(999);
        Log.d(TAG,"size : "+mLocalRecordPool.size());
        MinukuStreamManager.getInstance().setActivityRecognitionDataRecord(mLocalRecordPool.get(mLocalRecordPool.size()-1));
        Log.d(TAG,"CreateTime:" + mLocalRecordPool.get(mLocalRecordPool.size()-1).getCreationTime()+ " MostProbableActivity:"+mLocalRecordPool.get(mLocalRecordPool.size()-1).getMostProbableActivity());


        this.activityRecognitionDataRecord = activityRecognitionDataRecord;

        //TODO: now update the value to the TransportationMode every 5 seconds
        try {

            TransportationModeStreamGenerator transportationModeStreamGenerator
                    = (TransportationModeStreamGenerator) MinukuStreamManager.getInstance().getStreamGeneratorFor(TransportationModeDataRecord.class);

            transportationModeStreamGenerator.examineTransportation(activityRecognitionDataRecord);

            sharedPrefs.edit().putInt("CurrentState", TransportationModeStreamGenerator.mCurrentState).apply();
            sharedPrefs.edit().putInt("ConfirmedActivityType", TransportationModeStreamGenerator.mConfirmedActivityType).apply();

//            CSVHelper.storeToCSV(CSVHelper.CSV_CHECK_TRANSPORTATION,
//                    ScheduleAndSampleManager.getCurrentTimeString(),
//                    TransportationModeStreamGenerator.getConfirmedActivityString(),
//                    ScheduleAndSampleManager.getTimeString(TransportationModeStreamGenerator.getSuspectTime()),
//                    getActivityNameFromType(TransportationModeStreamGenerator.getSuspectedStartActivityType()),
//                    getActivityNameFromType(TransportationModeStreamGenerator.getSuspectedStopActivityType()),
//                    activityRecognitionDataRecord.getMostProbableActivity().toString(),
//                    activityRecognitionDataRecord.getProbableActivities().toString());

            String suspectedStartActivity = getActivityNameFromType(transportationModeStreamGenerator.getSuspectedStartActivityType());
            String suspectedEndActivity = getActivityNameFromType(transportationModeStreamGenerator.getSuspectedStopActivityType());

            TransportationModeDataRecord transportationModeDataRecord =
                    new TransportationModeDataRecord(transportationModeStreamGenerator.getConfirmedActivityString(),
                            transportationModeStreamGenerator.getSuspectTime(),
                            suspectedStartActivity, suspectedEndActivity, String.valueOf(session_id), phone_session_id, screenshot, ImageName);

            MinukuStreamManager.getInstance().setTransportationModeDataRecord(transportationModeDataRecord, mContext, sharedPrefs);

        }catch (StreamNotFoundException e){
            e.printStackTrace();
             // CSVHelper.storeToCSV(CSVHelper.CSV_CHECK_SESSION, " StreamNotFoundException : "+ Utils.getStackTrace(e));
        }

    }

    /**
     * this function remove old record (depending on the maximum size of the local pool)
     */
    protected void removeOutDatedRecord() {

        for (int i=0; i<mLocalRecordPool.size(); i++) {

            ActivityRecognitionDataRecord record = mLocalRecordPool.get(i);

            //calculate time difference
            long diff =  getCurrentTimeInMillis() - mLocalRecordPool.get(i).getTimestamp();

            //remove outdated records.
            if (diff >= sKeepalive){
                mLocalRecordPool.remove(record);
                //Log.d(TAG, "[test logging]remove record " + record.getSource() + record.getID() + " logged at " + record.getTimeString() + " to " + this.getName());
                Log.e(TAG,"sKeepalive");
                i--;
            }
        }
    }

    //TODO might be useless
    public static ArrayList<ActivityRecognitionDataRecord> getLocalRecordPool(){
        return mLocalRecordPool;
    }

    public static ActivityRecognitionDataRecord getLastSavedRecord(){
        if(mLocalRecordPool==null){
            Log.e("getLastSavedRecord","null");
            return null;
        }
        if (mLocalRecordPool.size()>0)
            return mLocalRecordPool.get(mLocalRecordPool.size()-1);
        else{
            Log.e("getLastSavedRecord","mLocalRecordPool.size()<0");
            return null;
        }
    }

    /**get the current time in milliseconds**/
    public static long getCurrentTimeInMillis(){
        //get timzone
        TimeZone tz = TimeZone.getDefault();
        Calendar cal = Calendar.getInstance(tz);
        long t = cal.getTimeInMillis();
        return t;
    }


    public void setProbableActivities(List<DetectedActivity> probableActivities) {
        sProbableActivities = probableActivities;

    }

    public void setMostProbableActivity(DetectedActivity mostProbableActivity) {
        sMostProbableActivity = mostProbableActivity;

    }

    public void setDetectedtime(long detectedtime){
        sLatestDetectionTime = detectedtime;

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @SuppressLint("LongLogTag")
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            // Log.d(LOG_TAG,"[onConnectionFailed] Conntection to Google Play services is failed");

        } else {
            Log.e(TAG, "[onConnectionFailed] No Google Play services is available, the error code is "
                    + connectionResult.getErrorCode());
        }
    }

    public static int getActivityTypeFromName(String activityName) {

        if (activityName.equals(STRING_DETECTED_ACTIVITY_IN_VEHICLE)) {
            return DetectedActivity.IN_VEHICLE;
        }else if(activityName.equals(STRING_DETECTED_ACTIVITY_ON_BICYCLE)) {
            return DetectedActivity.ON_BICYCLE;
        }else if(activityName.equals(STRING_DETECTED_ACTIVITY_ON_FOOT)) {
            return DetectedActivity.ON_FOOT;
        }else if(activityName.equals(STRING_DETECTED_ACTIVITY_STILL)) {
            return DetectedActivity.STILL;
        }else if(activityName.equals(STRING_DETECTED_ACTIVITY_UNKNOWN)) {
            return DetectedActivity.UNKNOWN ;
        }else if(activityName.equals(STRING_DETECTED_ACTIVITY_RUNNING)) {
            return DetectedActivity.RUNNING ;
        }else if (activityName.equals(STRING_DETECTED_ACTIVITY_WALKING)){
            return DetectedActivity.WALKING;
        }else if(activityName.equals(STRING_DETECTED_ACTIVITY_TILTING)) {
            return DetectedActivity.TILTING;
        }else {
            return NO_ACTIVITY_TYPE;
        }

    }

    public static String getActivityNameFromType(int activityType) {
        switch(activityType) {
            case DetectedActivity.IN_VEHICLE:
                return STRING_DETECTED_ACTIVITY_IN_VEHICLE;
            case DetectedActivity.ON_BICYCLE:
                return STRING_DETECTED_ACTIVITY_ON_BICYCLE;
            case DetectedActivity.ON_FOOT:
                return STRING_DETECTED_ACTIVITY_ON_FOOT;
            case DetectedActivity.STILL:
                return STRING_DETECTED_ACTIVITY_STILL;
            case DetectedActivity.RUNNING:
                return STRING_DETECTED_ACTIVITY_RUNNING;
            case DetectedActivity.WALKING:
                return STRING_DETECTED_ACTIVITY_WALKING;
            case DetectedActivity.UNKNOWN:
                return STRING_DETECTED_ACTIVITY_UNKNOWN;
            case DetectedActivity.TILTING:
                return STRING_DETECTED_ACTIVITY_TILTING;
            case NO_ACTIVITY_TYPE:
                return STRING_DETECTED_ACTIVITY_NA;
        }
        return "NA";
    }
}
