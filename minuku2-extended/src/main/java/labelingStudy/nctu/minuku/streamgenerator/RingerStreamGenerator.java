package labelingStudy.nctu.minuku.streamgenerator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import labelingStudy.nctu.minuku.DBHelper.appDatabase;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.manager.MinukuStreamManager;
import labelingStudy.nctu.minuku.model.DataRecord.RingerDataRecord;
import labelingStudy.nctu.minuku.receiver.SettingsContentObserver;
import labelingStudy.nctu.minuku.stream.RingerStream;
import labelingStudy.nctu.minukucore.exception.StreamAlreadyExistsException;
import labelingStudy.nctu.minukucore.exception.StreamNotFoundException;
import labelingStudy.nctu.minukucore.stream.Stream;

/**
 * Created by Lawrence on 2017/8/22.
 */

public class RingerStreamGenerator extends AndroidStreamGenerator<RingerDataRecord> {

    private String TAG = "RingerStreamGenerator";
    SettingsContentObserver mSettingsContentObserver;
    RingerStreamGenerator mRingerStreamGenerator;
    private RingerStream mStream;

    //audio and ringer
    public static final String RINGER_MODE_NORMAL = "Normal";
    public static final String RINGER_MODE_VIBRATE = "Vibrate";
    public static final String RINGER_MODE_SILENT = "Silent";

    public static final String MODE_CURRENT = "Current";
    public static final String MODE_INVALID = "Invalid";
    public static final String MODE_IN_CALL = "InCall";
    public static final String MODE_IN_COMMUNICATION = "InCommunicaiton";
    public static final String MODE_NORMAL = "Normal";
    public static final String MODE_RINGTONE = "Ringtone";
    appDatabase db;
    //after api 23
    public static AudioDeviceInfo[] mAllAudioDevices;

    private String mRingerMode = "NA";
    private String mAudioMode = "NA";
//    private String LastImageName = "";
    private int mStreamVolumeMusic = -9999;
    private int mStreamVolumeNotification = -9999;
    private int mStreamVolumeRing = -9999;
    private int mStreamVolumeVoicecall = -9999;
    private int mStreamVolumeSystem = -9999;
//    private static int mStreamVolumeDTMF = -9999;

    private static AudioManager mAudioManager;

    private SharedPreferences sharedPrefs;

    public static int mainThreadUpdateFrequencyInSeconds = 10;
    public static long mainThreadUpdateFrequencyInMilliseconds = mainThreadUpdateFrequencyInSeconds *Constants.MILLISECONDS_PER_SECOND;

    private Context mContext;

    private static Handler mMainThread;

    public RingerStreamGenerator (Context applicationContext) {
        super(applicationContext);

        this.mContext = applicationContext;
        this.mStream = new RingerStream(Constants.DEFAULT_QUEUE_SIZE);


        sharedPrefs = mContext.getSharedPreferences(Constants.sharedPrefString,Context.MODE_PRIVATE);

        mAudioManager = (AudioManager)mContext.getSystemService(mContext.AUDIO_SERVICE);
        db = appDatabase.getDatabase(applicationContext);
        this.register();
    }
    @Override
    public void register() {
        Log.d(TAG, "Registring with StreamManage");

        try {
            MinukuStreamManager.getInstance().register(mStream, RingerDataRecord.class, this);
        } catch (StreamNotFoundException streamNotFoundException) {
            Log.e(TAG, "One of the streams on which" +
                    "RingerDataRecord/RingerStream depends in not found.");
        } catch (StreamAlreadyExistsException streamAlreadyExsistsException) {
            Log.e(TAG, "Another stream which provides" +
                    " RingerDataRecord/RingerStream is already registered.");
        }
    }

    @Override
    public Stream<RingerDataRecord> generateNewStream() {
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
        RingerDataRecord ringerDataRecord;
        ringerDataRecord = new RingerDataRecord(mRingerMode, mAudioMode, mStreamVolumeMusic
                , mStreamVolumeNotification, mStreamVolumeRing, mStreamVolumeVoicecall, mStreamVolumeSystem,
                String.valueOf(session_id), phone_session_id, screenshot, ImageName);

        mStream.add(ringerDataRecord);
        Log.d(TAG, "Ringer to be sent to event bus" + ringerDataRecord);
        // also post an event.
        EventBus.getDefault().post(ringerDataRecord);
        labelingStudy.nctu.minuku.logger.Log.d("creationTime : ", "ringerData : "+ringerDataRecord.getCreationTime());

        try {

            db.ringerDataRecordDao().insertAll(ringerDataRecord);
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
//        new Threading().start();
        Log.e(TAG,"onStreamRegistration");
        try {
            mRingerStreamGenerator = (RingerStreamGenerator) MinukuStreamManager.getInstance().getStreamGeneratorFor(RingerDataRecord.class);
        } catch (StreamNotFoundException e) {
            labelingStudy.nctu.minuku.logger.Log.d(TAG, "Initial MyAccessibility Service Failed");
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
        mApplicationContext.registerReceiver(RingerModeReceiver, filter);

        mSettingsContentObserver = new SettingsContentObserver( new Handler() );
        mContext.getContentResolver().registerContentObserver(
                android.provider.Settings.System.CONTENT_URI, true,
                mSettingsContentObserver );
        //runPhoneStatusMainThread();
    }
    private BroadcastReceiver RingerModeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(AudioManager.RINGER_MODE_CHANGED_ACTION)) {
                Log.d(TAG, "RingerModeBroadcast");
                getAudioRingerUpdate();
            }
        }
    };
    public void runPhoneStatusMainThread(){

        Log.d(TAG, "runPhoneStatusMainThread") ;

        mMainThread = new Handler();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                getAudioRingerUpdate();

                mMainThread.postDelayed(this, mainThreadUpdateFrequencyInMilliseconds);

            }
        };

        mMainThread.post(runnable);
    }

    public void getAudioRingerUpdate() {
        if (mAudioManager.getRingerMode()==AudioManager.RINGER_MODE_NORMAL)
            mRingerMode = RINGER_MODE_NORMAL;
        else if (mAudioManager.getRingerMode()==AudioManager.RINGER_MODE_VIBRATE)
            mRingerMode = RINGER_MODE_VIBRATE;
        else if (mAudioManager.getRingerMode()==AudioManager.RINGER_MODE_SILENT)
            mRingerMode = RINGER_MODE_SILENT;

        int mode = mAudioManager.getMode();
//        Log.d(LOG_TAG, "[getAudioRingerUpdate] ringer mode: " + mRingerMode + " mode: " + mode);

        mStreamVolumeMusic= mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mStreamVolumeNotification= mAudioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
        mStreamVolumeRing= mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
        mStreamVolumeVoicecall = mAudioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        mStreamVolumeSystem= mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);

        mAudioMode = getAudioMode(mAudioManager.getMode());

        Log.d(TAG,"mRingerMode : "+ mRingerMode +" | mAudioMode : "+ mAudioMode+" | mStreamVolumeMusic : "+ mStreamVolumeMusic
                +" | mStreamVolumeNotification : "+ mStreamVolumeNotification+" | mStreamVolumeRing : "+ mStreamVolumeRing
                +" | mStreamVolumeVoicecall : "+ mStreamVolumeVoicecall +" | mStreamVolumeSystem : "+ mStreamVolumeSystem);

        //android 6
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            mAllAudioDevices = mAudioManager.getDevices(AudioManager.GET_DEVICES_ALL);
        }

        mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM);

        if(mRingerStreamGenerator != null)
            mRingerStreamGenerator.updateStream();
    }

    public String getAudioMode(int mode) {

        if (mode==AudioManager.MODE_CURRENT)
            return MODE_CURRENT;
        else if (mode==AudioManager.MODE_IN_CALL)
            return MODE_IN_CALL;
        else if (mode==AudioManager.MODE_IN_COMMUNICATION)
            return MODE_IN_COMMUNICATION;
        else if (mode==AudioManager.MODE_INVALID)
            return MODE_INVALID;

        else if (mode==AudioManager.MODE_NORMAL)
            return MODE_NORMAL;

        else if (mode==AudioManager.MODE_RINGTONE)
            return MODE_RINGTONE;
        else
            return "NA";
    }

    @Override
    public void offer(RingerDataRecord ringerdataRecord) {
        mStream.add(ringerdataRecord);
    }

}
