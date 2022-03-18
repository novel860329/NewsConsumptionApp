package labelingStudy.nctu.minuku.streamgenerator;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.RequiresApi;

import org.greenrobot.eventbus.EventBus;

import labelingStudy.nctu.minuku.DBHelper.appDatabase;
import labelingStudy.nctu.minuku.Utilities.ScheduleAndSampleManager;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minuku.model.DataRecord.MyDataRecord;
import labelingStudy.nctu.minuku.service.MobileAccessibilityService;
import labelingStudy.nctu.minuku.stream.AccessibilityStream;
import labelingStudy.nctu.minukucore.exception.StreamAlreadyExistsException;
import labelingStudy.nctu.minukucore.exception.StreamNotFoundException;
import labelingStudy.nctu.minukucore.stream.Stream;

import static labelingStudy.nctu.minuku.manager.MinukuStreamManager.getInstance;

/**
 * Created by chiaenchiang on 08/03/2018.
 */

public class AccessibilityStreamGenerator extends AndroidStreamGenerator<MyDataRecord> {

    private final String TAG = "AccessibilityStreamGenerator";
    private final String room = "room";
    private Stream mStream;
    private Context mContext;
    private SharedPreferences sharedPrefs;
    MobileAccessibilityService mobileAccessibilityService;

    private long creation_time;
    private String device_id;
    private String text;
    private String type;
    private String extra;
    private String package_name;
    private String myevent;
    private String NewsApp;
    private String LastImageName = "";
    appDatabase db;



    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public AccessibilityStreamGenerator(Context applicationContext){
        super(applicationContext);
        this.mContext = applicationContext;
        this.mStream = new AccessibilityStream(Constants.DEFAULT_QUEUE_SIZE);

        //mobileAccessibilityService = new MyAccessibilityService(this);
        package_name = text = type = device_id = extra = myevent = "";
        creation_time = 0;

        db = appDatabase.getDatabase(applicationContext);
        sharedPrefs = mContext.getSharedPreferences(Constants.sharedPrefString,Context.MODE_PRIVATE);

        this.register();

    }

    @Override
    public void register() {
        Log.d(TAG, "Registring with StreamManage");

        try {
            Log.d(TAG, "success");
            getInstance().register(mStream, MyDataRecord.class, this);
        } catch (StreamNotFoundException streamNotFoundException) {
            Log.e(TAG, "One of the streams on which" +
                    "MyDataRecord/AccessibilityStream depends in not found.");
        } catch (StreamAlreadyExistsException streamAlreadyExistsException) {
            Log.e(TAG, "Another stream which provides" +
                    " MyDataRecordDataRecord/AccessibilityStream is already registered.");
        }
    }

    private void activateAccessibilityService() {

        Log.d(TAG, "testing logging task and requested activateAccessibilityService");
        Intent intent = new Intent(mContext, MobileAccessibilityService.class);
        mContext.startService(intent);

    }


    @Override
    public Stream<MyDataRecord> generateNewStream() {
        return mStream;
    }

    @Override
    public boolean updateStream() {
        Log.d(TAG, "updateStream called");
        Log.d(TAG, "pack: "+package_name+" event text: "+text + "event type: " + type + " MyEvent: "+ myevent + " extra: " + extra);

        long session_id;
        long phone_session_id = sharedPrefs.getLong("Phone_SessionID", 1);
        String screenshot = sharedPrefs.getString("ScreenShot", "0");
        String ImageName = sharedPrefs.getString("CaptureImgName", "");
//        String AccessibilityUrl = sharedPrefs.getString("AccessibilityUrl", "");
//        String NotificationUrl = sharedPrefs.getString("NotificationUrl", "");


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

        boolean readnews = mContext.getSharedPreferences("test",Context.MODE_PRIVATE).getBoolean("ReadNews",false);
        if(readnews) {
            session_id = sharedPrefs.getLong("SessionID", Constants.INVALID_INT_VALUE);
        }
        else{
            session_id = -1;
        }

        MyDataRecord myDataRecord =
                new MyDataRecord(creation_time, device_id, package_name, text, type , myevent, extra,
                        String.valueOf(session_id),phone_session_id, screenshot, ImageName, NewsApp);
        String str = ScheduleAndSampleManager.getTimeString(creation_time) + " " + package_name + " " + myevent + " " + text + " "
                    + type + " " + extra + " " + screenshot;
//        CSVHelper.storeToCSV("MyDataRecord.csv", str);
        //if(!package_name.trim().isEmpty()&&!app_name.trim().isEmpty()) {
        mStream.add(myDataRecord);
        Log.d(TAG, "Accessibility to be sent to event bus" + myDataRecord);
        Log.d("creationTime : ", "accessData : " + myDataRecord.getCreationTime());
        // also post an event.
        EventBus.getDefault().post(myDataRecord);
        try {

            db.MyDataRecordDao().insertAll(myDataRecord);
       /* List<MyDataRecord> accessibilityDataRecords = db.MyDataRecordDao().getAll();
//
        for (MyDataRecord a : accessibilityDataRecords) {
            Log.d(room, "AccessPack : "+a.getPackeageName());}*/
//
//
//            }


        } catch (NullPointerException e) { //Sometimes no data is normal
            e.printStackTrace();
            return false;
        }

        try {
//            if (!AccessibilityUrl.equals("")) {
////                Log.d(TAG, "Accessibility initial");
//                sharedPrefs.edit().putString("AccessibilityUrl", "").apply();
//            }
//            if (!NotificationUrl.equals("")) {
////                Log.d(TAG, "Accessibility initial");
//                sharedPrefs.edit().putString("NotificationUrl", "").apply();
//            }
//            if(!ImageName.equals("")){
//                sharedPrefs.edit().putString("CaptureImgName", "").apply();
//            }
        }catch (Exception e){
            e.printStackTrace();
        }
        package_name = text = type = device_id = extra = myevent = "";
        creation_time = 0;

        return false;
    }


    @Override
    public long getUpdateFrequency() {
        return 1;
    }

    @Override
    public void sendStateChangeEvent() {

    }



    public void setLatestInAppAction(long creationTime, String DeviceID, String PackeageName, String EventText, String EventType, String MyEventText , String extra, String NewsApp){

        this.creation_time = creationTime;
        this.device_id = DeviceID;
        this.package_name = PackeageName;
        this.text = EventText;
        if(!EventType.equals("NA"))
            this.type = EventType;
        this.myevent = MyEventText;
        this.extra = extra;
        this.NewsApp = NewsApp;
//        Log.d(TAG, "pack, "+pack+"text "+text+"type "+type+"extra "+extra);

    }

    @Override
    public void onStreamRegistration() {

        //activateAccessibilityService();

    }

    @Override
    public void offer(MyDataRecord dataRecord) {

    }

}

