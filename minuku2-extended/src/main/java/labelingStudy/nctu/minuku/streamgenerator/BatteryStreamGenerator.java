package labelingStudy.nctu.minuku.streamgenerator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import labelingStudy.nctu.minuku.DBHelper.appDatabase;
import labelingStudy.nctu.minuku.Utilities.ScheduleAndSampleManager;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.manager.MinukuStreamManager;
import labelingStudy.nctu.minuku.model.DataRecord.BatteryDataRecord;
import labelingStudy.nctu.minuku.stream.BatteryStream;
import labelingStudy.nctu.minukucore.exception.StreamAlreadyExistsException;
import labelingStudy.nctu.minukucore.exception.StreamNotFoundException;
import labelingStudy.nctu.minukucore.stream.Stream;

/**
 * Created by Lawrence on 2017/8/22.
 */

public class BatteryStreamGenerator extends AndroidStreamGenerator<BatteryDataRecord> {

    private final String TAG = "BatteryStreamGenerator";
//    private String LastImageName = "";
    private BatteryStream mStream;
    BatteryStreamGenerator mBatteryStreamGenerator;
    appDatabase db;
    public static int mBatteryLevel= -1;
    public static float mBatteryPercentage = -1;
    private static String mBatteryChargingState = "NA";
    public static boolean isCharging = false;
    private long detectedTime = Constants.INVALID_TIME_VALUE;
    private Context mContext;

    private SharedPreferences sharedPrefs;

    public BatteryStreamGenerator(Context applicationContext){
        super(applicationContext);

        this.mContext = applicationContext;
        this.mStream = new BatteryStream(Constants.DEFAULT_QUEUE_SIZE);
        sharedPrefs = mContext.getSharedPreferences(Constants.sharedPrefString, Context.MODE_PRIVATE);
        db = appDatabase.getDatabase(applicationContext);

        this.register();
    }


    @Override
    public void register() {
        Log.d(TAG, "Registring with StreamManage");

        try {
            MinukuStreamManager.getInstance().register(mStream, BatteryDataRecord.class, this);
        } catch (StreamNotFoundException streamNotFoundException) {
            Log.e(TAG, "One of the streams on which" +
                    "BatteryDataRecord/BatteryStream depends in not found.");
        } catch (StreamAlreadyExistsException streamAlreadyExistsException) {
            Log.e(TAG, "Another stream which provides" +
                    " BatteryDataRecord/BatteryStream is already registered.");
        }
    }

    @Override
    public Stream<BatteryDataRecord> generateNewStream() {
        return mStream;
    }

    @Override
    public boolean updateStream() {
        Log.d(TAG, "updateStream called");
//        int session_id = SessionManager.getOngoingSessionId();
        long session_id;
        long phone_session_id = sharedPrefs.getLong("Phone_SessionID", 1);
        String screenshot = sharedPrefs.getString("ScreenShot", "0");
        String ImageName = sharedPrefs.getString("CaptureImgName", "");
//        String AccessibilityUrl = sharedPrefs.getString("AccessibilityUrl", "");
//        String NotificationUrl = sharedPrefs.getString("NotificationUrl", "");

        Log.d(TAG, "Phone session id: " + phone_session_id);
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
        //TODO get service data
        BatteryDataRecord batteryDataRecord;
        batteryDataRecord
                = new BatteryDataRecord(mBatteryLevel, mBatteryPercentage, mBatteryChargingState, isCharging, String.valueOf(session_id), detectedTime, phone_session_id, screenshot, ImageName);

        if((ScheduleAndSampleManager.getCurrentTimeInMillis() - detectedTime) >= Constants.MILLISECONDS_PER_MINUTE * 10
                && (detectedTime != Constants.INVALID_TIME_VALUE)) {

            batteryDataRecord = new BatteryDataRecord(-1,
                        -1, "NA", false, String.valueOf(session_id), detectedTime, phone_session_id, screenshot, ImageName);
        }

        mStream.add(batteryDataRecord);
        labelingStudy.nctu.minuku.logger.Log.d("creationTime : ", "batteryData : "+batteryDataRecord.getCreationTime());

        Log.d(TAG, "CheckFamiliarOrNot to be sent to event bus" + batteryDataRecord);
        // also post an event.
        EventBus.getDefault().post(batteryDataRecord);
        try {
            db.batteryDataRecordDao().insertAll(batteryDataRecord);
        }catch (NullPointerException e){ //Sometimes no data is normal
            e.printStackTrace();
            return false;
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
        try {
            mBatteryStreamGenerator = (BatteryStreamGenerator) MinukuStreamManager.getInstance().getStreamGeneratorFor(BatteryDataRecord.class);
        } catch (StreamNotFoundException e) {
            labelingStudy.nctu.minuku.logger.Log.d(TAG, "Initial MyAccessibility Service Failed");
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        mApplicationContext.registerReceiver(mBroadcastReceiver, filter);

        Log.d(TAG, "Stream " + TAG + " registered successfully");

    }

    @Override
    public void offer(BatteryDataRecord dataRecord) {

    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {

                detectedTime = ScheduleAndSampleManager.getCurrentTimeInMillis();

                int status = intent.getIntExtra("status", -1);
                //int health = intent.getIntExtra("health", 0);
                //boolean present = intent.getBooleanExtra("present",false);
                //int mBatteryLevel = intent.getIntExtra("mBatteryLevel", 0);
                mBatteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

                //boolean
                isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL;

                mBatteryPercentage = mBatteryLevel / (float)scale;
//                int icon_small = intent.getIntExtra("icon-small", 0);
//                int plugged = intent.getIntExtra("plugged", 0);
//                int voltage = intent.getIntExtra("voltage", 0);
                int temperature = intent.getIntExtra("temperature",0);
                //String technology = intent.getStringExtra("technology");

                int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

                String statusString = "";
                switch (status) {
                    case BatteryManager.BATTERY_STATUS_UNKNOWN:
                        statusString = "unknown";
                        break;
                    case BatteryManager.BATTERY_STATUS_CHARGING:
                        statusString = "charging";
                        break;
                    case BatteryManager.BATTERY_STATUS_DISCHARGING:
                        statusString = "discharging";
                        break;
                    case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                        statusString = "not charging";
                        break;
                    case BatteryManager.BATTERY_STATUS_FULL:
                        statusString = "full";
                        break;
                }

                if (!isCharging){
                    mBatteryChargingState = "not charging";
                }else if (chargePlug==BatteryManager.BATTERY_PLUGGED_USB){
                    mBatteryChargingState = "usb charging";
                }else if (chargePlug==BatteryManager.BATTERY_PLUGGED_AC){
                    mBatteryChargingState = "ac charging";
                }

//                if(mBatteryStreamGenerator != null)
//                    mBatteryStreamGenerator.updateStream();

//                String healthString = "";
//                switch (health) {
//                    case BatteryManager.BATTERY_HEALTH_UNKNOWN:
//                        healthString = "unknown";
//                        break;
//                    case BatteryManager.BATTERY_HEALTH_GOOD:
//                        healthString = "good";
//                        break;
//                    case BatteryManager.BATTERY_HEALTH_OVERHEAT:
//                        healthString = "overheat";
//                        break;
//                    case BatteryManager.BATTERY_HEALTH_DEAD:
//                        healthString = "dead";
//                        break;
//                    case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
//                        healthString = "voltage";
//                        break;
//                    case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
//                        healthString = "unspecified failure";
//                        break;
//                }
//                String acString = "";
//                switch (plugged) {
//                    case BatteryManager.BATTERY_PLUGGED_AC:
//                        acString = "plugged ac";
//                        break;
//                    case BatteryManager.BATTERY_PLUGGED_USB:
//
//                        acString = "plugged usb";
//                        break;
//                }
                Log.d("Batterystatus", statusString);
                //Log.d("Batteryhealth", healthString);
                //Log.d("Batterypresent", String.valueOf(present));
                Log.d("mBatteryLevel", String.valueOf(mBatteryLevel));
                Log.d("BatteryScale", String.valueOf(scale));
                Log.d("mBatteryPercentage", String.valueOf(mBatteryPercentage));
                //Log.d("Batteryicon_small", String.valueOf(icon_small));

                Log.d("IsCharging",String.valueOf(isCharging));

                Log.d("BatteryChargingState",mBatteryChargingState);

                //Log.d("Batteryplugged", acString);
                //Log.d("Batteryvoltage", String.valueOf(voltage));
                Log.d("Batterytemperature", String.valueOf(temperature));
                //Log.d("Batterytechnology", technology);
            }
        }
    };
}
