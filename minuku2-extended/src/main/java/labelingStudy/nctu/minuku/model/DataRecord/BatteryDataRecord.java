package labelingStudy.nctu.minuku.model.DataRecord;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Calendar;
import java.util.Date;

import labelingStudy.nctu.minuku.Utilities.ScheduleAndSampleManager;
import labelingStudy.nctu.minukucore.model.DataRecord;

import static labelingStudy.nctu.minuku.config.SharedVariables.getReadableTimeLong;

/**
 * Created by Lawrence on 2017/7/22.
 */
@Entity(tableName = "BatteryDataRecord")
public class BatteryDataRecord implements DataRecord{



    @PrimaryKey(autoGenerate = true)
    public long _id;

    @ColumnInfo(name = "creationTime")
    public long creationTime;

    @ColumnInfo(name = "BatteryLevel")
    public int BatteryLevel;

    @ColumnInfo(name = "BatteryPercentage")
    public float BatteryPercentage;

    @ColumnInfo(name = "BatteryChargingState")
    private String BatteryChargingState = "NA";

    @ColumnInfo(name = "isCharging")
    public boolean isCharging;

    @ColumnInfo(name = "sessionid")
    private String sessionid;

    @ColumnInfo(name = "timeString")
    public String timeString;
    @ColumnInfo(name = "readable")
    public Long readable;
    @ColumnInfo(name = "sycStatus")
    public Integer syncStatus;

    @ColumnInfo(name = "phone_sessionid")
    public Long phone_sessionid;
    @ColumnInfo(name = "screenshot")
    public String screenshot;

    @ColumnInfo(name = "ImageName")
    public String ImageName;

//    @ColumnInfo(name = "AccessibilityUrl")
//    public String AccessibilityUrl;
//
//    @ColumnInfo(name = "NotificationUrl")
//    public String NotificationUrl;

    public BatteryDataRecord(int BatteryLevel, float BatteryPercentage, String BatteryChargingState, boolean isCharging, String sessionid,
                             long creationTime, long phone_sessionid, String screenshot, String ImageName){
        this.creationTime = new Date().getTime();
        this.BatteryLevel = BatteryLevel;
        this.BatteryPercentage = BatteryPercentage;
        this.BatteryChargingState = BatteryChargingState;
        this.isCharging = isCharging;
        this.sessionid = sessionid;
        this.syncStatus = 0;
        this.readable = getReadableTimeLong(this.creationTime);
        this.timeString = ScheduleAndSampleManager.getCurrentTimeString();
        this.phone_sessionid = phone_sessionid;
        this.screenshot = screenshot;
        this.ImageName = ImageName;
//        this.AccessibilityUrl = AccessibilityUrl;
//        this.NotificationUrl = NotificationUrl;
    }

    public void setsyncStatus(Integer syncStatus){
        this.syncStatus = syncStatus;
    }
    public Integer getsyncStatus(){
        return this.syncStatus;
    }
    public String getSessionid() {
        return sessionid;
    }


    private String getmillisecondToDateWithTime(long timeStamp){

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStamp);

        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH)+1;
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);
        int mhour = calendar.get(Calendar.HOUR);
        int mMin = calendar.get(Calendar.MINUTE);
        int mSec = calendar.get(Calendar.SECOND);

        return addZero(mYear)+"/"+addZero(mMonth)+"/"+addZero(mDay)+" "+addZero(mhour)+":"+addZero(mMin)+":"+addZero(mSec);

    }

    private String addZero(int date){
        if(date<10)
            return String.valueOf("0"+date);
        else
            return String.valueOf(date);
    }


    public Long getReadable(){
        return this.readable;
    }
    @Override
    public long getCreationTime() {
        return creationTime;
    }

    public int getBatteryLevel(){
        return BatteryLevel;
    }

    public float getBatteryPercentage(){
        return BatteryPercentage;
    }

    public String getBatteryChargingState(){
        return BatteryChargingState;
    }

    public boolean getisCharging(){
        return isCharging;
    }
}
