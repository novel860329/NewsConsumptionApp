package labelingStudy.nctu.minuku.model.DataRecord;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

import labelingStudy.nctu.minukucore.model.DataRecord;

import static labelingStudy.nctu.minuku.config.SharedVariables.getReadableTimeLong;

/**
 * Created by Lawrence on 2017/7/22.
 */
@Entity(tableName = "SensorDataRecord")
public class SensorDataRecord implements DataRecord {


    @PrimaryKey(autoGenerate = true)
    public long _id;
    @ColumnInfo(name = "creationTime")
    public long creationTime;

    @ColumnInfo(name = "mProximity_str")
    public String mProximity_str;
    @ColumnInfo(name = "mLight_str")
    public String mLight_str;

    @ColumnInfo(name = "readable")
    public Long readable;
    @ColumnInfo(name = "sycStatus")
    public Integer syncStatus;

    @ColumnInfo(name = "sessionid")
    private String sessionid;

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

    public SensorDataRecord(String mProximity_str,  String mLight_str,
                            String sessionid, long phone_sessionid, String screenshot,String ImageName){

        this.creationTime = new Date().getTime();

        this.mProximity_str = mProximity_str;
        this.mLight_str = mLight_str;

        this.sessionid = sessionid;
        this.readable = getReadableTimeLong(this.creationTime);
        this.syncStatus = 0;
        this.phone_sessionid = phone_sessionid;
        this.screenshot = screenshot;
        this.ImageName = ImageName;
//        this.AccessibilityUrl = AccessibilityUrl;
//        this.NotificationUrl = NotificationUrl;
    }

    public String getSessionid() {
        return sessionid;
    }


    public void setID(long id){
        _id = id;
    }

    public Long get_id() {
        return _id;
    }


    @Override
    public long getCreationTime() {

        return creationTime;
    }


    public String getmProximity_str() {return this.mProximity_str;}


    public String getmLight_str() {return mLight_str;}


    public void setsyncStatus(Integer syncStatus){
        this.syncStatus = syncStatus;
    }

    public Integer getsyncStatus(){
        return this.syncStatus;
    }
    public Long getReadable(){
        return this.readable;
    }

}
