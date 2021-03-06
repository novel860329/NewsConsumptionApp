package labelingStudy.nctu.minuku.streamgenerator;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import androidx.annotation.RequiresApi;

import org.greenrobot.eventbus.EventBus;

import labelingStudy.nctu.minuku.DBHelper.appDatabase;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minuku.manager.MinukuStreamManager;
import labelingStudy.nctu.minuku.model.DataRecord.TelephonyDataRecord;
import labelingStudy.nctu.minuku.stream.TelephonyStream;
import labelingStudy.nctu.minukucore.exception.StreamAlreadyExistsException;
import labelingStudy.nctu.minukucore.exception.StreamNotFoundException;
import labelingStudy.nctu.minukucore.stream.Stream;

import static android.telephony.TelephonyManager.CALL_STATE_IDLE;
import static android.telephony.TelephonyManager.CALL_STATE_OFFHOOK;
import static android.telephony.TelephonyManager.CALL_STATE_RINGING;
import static android.telephony.TelephonyManager.NETWORK_TYPE_CDMA;
import static android.telephony.TelephonyManager.NETWORK_TYPE_LTE;
import static labelingStudy.nctu.minuku.manager.MinukuStreamManager.getInstance;

/**
 * Created by Lucy on 2017/9/6.
 */

public class TelephonyStreamGenerator extends AndroidStreamGenerator<TelephonyDataRecord> {

    private String TAG = "TelephonyStreamGenerator";
    private TelephonyStream mStream;
    TelephonyStreamGenerator mTelephonyStreamGenerator;
    private TelephonyManager telephonyManager;
    private String mNetworkOperatorName;
//    private String LastImageName = "";
    private int mCallState;
    private int mPhoneSignalType;
    private int mLTESignalStrength_dbm;
    //private int LTESignalStrength_asu;
    private int mGsmSignalStrength;
    private int CdmaSignalStrength;
    private int mCdmaSignalStrengthLevel; // 1, 2, 3, 4
    private int GeneralSignalStrength;
    private boolean isGSM = false;
    private Context mContext;
    private SharedPreferences sharedPrefs;
    appDatabase db;

    public TelephonyStreamGenerator (Context applicationContext) {

        super(applicationContext);
        this.mContext = applicationContext;
        this.mStream = new TelephonyStream(Constants.DEFAULT_QUEUE_SIZE);

        this.register();

        mCallState = -9999;
        mPhoneSignalType = -9999;
        mLTESignalStrength_dbm = -9999;
        //LTESignalStrength_asu = -9999;
        mGsmSignalStrength = -9999;
        CdmaSignalStrength = -9999;
        mCdmaSignalStrengthLevel = -9999;
        GeneralSignalStrength = -9999;
        isGSM = false;
        db = appDatabase.getDatabase(applicationContext);
        sharedPrefs = mContext.getSharedPreferences(Constants.sharedPrefString,Context.MODE_PRIVATE);

    }
    @Override
    public void register() {
        Log.d(TAG, "Registring with StreamManage");

        try {
            getInstance().register(mStream, TelephonyDataRecord.class, this);
        } catch (StreamNotFoundException streamNotFoundException) {
            Log.e(TAG, "One of the streams on which" +
                    "RingerDataRecord/RingerStream depends in not found.");
        } catch (StreamAlreadyExistsException streamAlreadyExsistsException) {
            Log.e(TAG, "Another stream which provides" +
                    " TelephonyDataRecord/TelephonyStream is already registered.");
        }
    }

    @Override
    public Stream<TelephonyDataRecord> generateNewStream() {
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

        TelephonyDataRecord telephonyDataRecord;
        telephonyDataRecord = new TelephonyDataRecord(mNetworkOperatorName, mCallState
                , mPhoneSignalType, mGsmSignalStrength, mLTESignalStrength_dbm, mCdmaSignalStrengthLevel,
                String.valueOf(session_id), phone_session_id, screenshot, ImageName);

        mStream.add(telephonyDataRecord);
        Log.d(TAG, "Telephony to be sent to event bus" + telephonyDataRecord);

        //post an event
        EventBus.getDefault().post(telephonyDataRecord);
        labelingStudy.nctu.minuku.logger.Log.d("creationTime : ", "telephoneData : "+telephonyDataRecord.getCreationTime());
        Log.d(TAG, "session id = " + session_id);
        try {
            db.telephonyDataRecordDao().insertAll(telephonyDataRecord);
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

        try {
            mTelephonyStreamGenerator = (TelephonyStreamGenerator) MinukuStreamManager.getInstance().getStreamGeneratorFor(TelephonyDataRecord.class);
        } catch (StreamNotFoundException e) {
            labelingStudy.nctu.minuku.logger.Log.d(TAG, "Initial MyAccessibility Service Failed");
        }
        telephonyManager = (TelephonyManager) mApplicationContext.getSystemService(Context.TELEPHONY_SERVICE);
        mNetworkOperatorName = telephonyManager.getNetworkOperatorName();
        //int networktype = telephonyManager.getNetworkType();

        telephonyManager.listen(TelephonyStateListener,PhoneStateListener.LISTEN_CALL_STATE|PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        /*switch (networktype) {
            case 0: Log.d(TAG, "UNKNOWN");
            case 1: Log.d(TAG, "GPRS");
            case 2: Log.d(TAG, "EDGE");
            case 3: Log.d(TAG, "UMTS");
            case 4: Log.d(TAG, "CDMA");
            case 5: Log.d(TAG, "EVDO_0");
            case 6: Log.d(TAG, "EVDO_A");
            case 7: Log.d(TAG, "1xRTT");
            case 8: Log.d(TAG, "HSDPA");
            case 9: Log.d(TAG, "HSUPA");
            case 10: Log.d(TAG, "HSPA");
            case 11: Log.d(TAG, "IDEN");
            case 12: Log.d(TAG, "EVDO_B");
            case 13: Log.d(TAG, "LTE");
            case 14: Log.d(TAG, "EHRPD");
            case 15: Log.d(TAG, "HSPAP");
            case 16: Log.d(TAG, "GSM");
            case 17: Log.d(TAG, "TD_SCDMA");
            case 18: Log.d(TAG, "IWLAN");
        }*/

    }
    private final PhoneStateListener TelephonyStateListener = new PhoneStateListener() {

        public void onCallStateChanged(int state, String incomingNumber) {
            if(state== CALL_STATE_RINGING){
                mCallState = CALL_STATE_RINGING;
            }
            if(state== CALL_STATE_OFFHOOK){
                mCallState = CALL_STATE_OFFHOOK;
            }
            if(state== CALL_STATE_IDLE){
                mCallState = CALL_STATE_IDLE;
            }
            if(mTelephonyStreamGenerator != null)
                mTelephonyStreamGenerator.updateStream();
        }
        @RequiresApi(api = Build.VERSION_CODES.ECLAIR_MR1)
        public void onSignalStrengthsChanged(SignalStrength sStrength) {

            String ssignal = sStrength.toString();
            Log.d(TAG, ssignal);
            String[] parts = ssignal.split(" ");

            int dbm = 0;
            int asu;
            //Log.d("parts8", parts[8]) = -1;

            /**If LTE 4G */
//            Log.d(TAG, "Telephony type: " + telephonyManager.getNetworkType());
//            Log.d(TAG, "Signal: " + ssignal);
            if (telephonyManager.getNetworkType() == NETWORK_TYPE_LTE){
                mPhoneSignalType = NETWORK_TYPE_LTE;
//                Log.d(TAG, String.valueOf(mPhoneSignalType));
                try {
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
//                        Log.d(TAG, "This is android 10");
                        int index = ssignal.indexOf("rsrq");
                        if(index != -1) {
                            String dbm_str = "";
                            for (int i = index + 5; ; i++) {
                                if(i == ssignal.length())break;
                                if (ssignal.charAt(i) != ' ') {
                                    dbm_str = dbm_str + ssignal.charAt(i);
                                } else {
                                    break;
                                }
                            }
                            dbm = Integer.parseInt(dbm_str);
                        }
                    } else {
//                        Log.d(TAG, "This is android 9");
                        dbm = Integer.parseInt(parts[10]);
                    }
                    Log.d(TAG, "DBM = " + dbm);
                }
                catch(Exception e){
                    e.printStackTrace();
                }
                //asu = 140 + dbm;
                mLTESignalStrength_dbm = dbm;
                //LTESignalStrength_asu = asu;
            }
            /** Else GSM 3G */
            else if (sStrength.isGsm()) {
                mPhoneSignalType = 16;
                // For GSM Signal Strength: dbm =  (2*ASU)-113.
                if (sStrength.getGsmSignalStrength() != 99) {
                    dbm = -113 + 2 * sStrength.getGsmSignalStrength();
                    mGsmSignalStrength = dbm;
                } else {
                    dbm = sStrength.getGsmSignalStrength();
                    mGsmSignalStrength = dbm;
                }
            }
            /** CDMA */
            else {
                /**
                 * DBM
                 level 4 >= -75
                 level 3 >= -85
                 level 2 >= -95
                 level 1 >= -100
                 Ecio
                 level 4 >= -90
                 level 3 >= -110
                 level 2 >= -130
                 level 1 >= -150
                 level is the lowest of the two
                 actualLevel = (levelDbm < levelEcio) ? levelDbm : levelEcio;
                 */
                int snr = sStrength.getEvdoSnr();
                int cdmaDbm = sStrength.getCdmaDbm();
                int cdmaEcio = sStrength.getCdmaEcio();

                int levelDbm;
                int levelEcio;
                mPhoneSignalType = NETWORK_TYPE_CDMA;

                if (snr == -1) { //if not 3G, use cdmaDBM or cdmaEcio
                    if (cdmaDbm >= -75) levelDbm = 4;
                    else if (cdmaDbm >= -85) levelDbm = 3;
                    else if (cdmaDbm >= -95) levelDbm = 2;
                    else if (cdmaDbm >= -100) levelDbm = 1;
                    else levelDbm = 0;

                    // Ec/Io are in dB*10
                    if (cdmaEcio >= -90) levelEcio = 4;
                    else if (cdmaEcio >= -110) levelEcio = 3;
                    else if (cdmaEcio >= -130) levelEcio = 2;
                    else if (cdmaEcio >= -150) levelEcio = 1;
                    else levelEcio = 0;

                    mCdmaSignalStrengthLevel = (levelDbm < levelEcio) ? levelDbm : levelEcio;
                }
                else {  // if 3G, use SNR
                    if (snr == 7 || snr == 8) mCdmaSignalStrengthLevel =4;
                    else if (snr == 5 || snr == 6 ) mCdmaSignalStrengthLevel =3;
                    else if (snr == 3 || snr == 4) mCdmaSignalStrengthLevel = 2;
                    else if (snr ==1 || snr ==2) mCdmaSignalStrengthLevel =1;
                }
            }
            if(mTelephonyStreamGenerator != null)
                mTelephonyStreamGenerator.updateStream();
        }
    };

    @Override
    public void offer(TelephonyDataRecord dataRecord) {
        mStream.add(dataRecord);
    }
}
