package labelingStudy.nctu.minuku.model.DataRecord;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

import labelingStudy.nctu.minukucore.model.DataRecord;

import static labelingStudy.nctu.minuku.config.SharedVariables.getReadableTimeLong;

/**
 * Created by Lawrence on 2017/5/22.
 */

@Entity(tableName = "TransportationModeDataRecord")
public class TransportationModeDataRecord implements DataRecord {


    @PrimaryKey(autoGenerate = true)
    private long _id;

    @ColumnInfo(name = "creationTime")
    public long creationTime;

    @ColumnInfo(name = "ConfirmedActivityString")
    public String ConfirmedActivityString; //
    @ColumnInfo(name = "SuspectTime")
    public Long SuspectTime; //
    @ColumnInfo(name = "suspectedStartActivity")
    public String suspectedStartActivity; //
    @ColumnInfo(name = "suspectedEndActivity")
    public String suspectedEndActivity; //

    @ColumnInfo(name = "readable")
    public Long readable;
    @ColumnInfo(name = "sycStatus")
    public Integer syncStatus;

    @ColumnInfo(name = "sessionid")
    public String sessionid;

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

    public TransportationModeDataRecord(String ConfirmedActivityString,Long SuspectTime,String suspectedStartActivity,String suspectedEndActivity,
                                        String sessionid, long phone_sessionid, String screenshot, String ImageName){
        this.creationTime = new Date().getTime();
        this.ConfirmedActivityString = ConfirmedActivityString;
        this.SuspectTime = SuspectTime;
        this.suspectedStartActivity = suspectedStartActivity;
        this.suspectedEndActivity = suspectedEndActivity;
        this.readable = getReadableTimeLong(this.creationTime);
        this.syncStatus = 0;
        this.sessionid = sessionid;
        this.phone_sessionid = phone_sessionid;
        this.screenshot = screenshot;
        this.ImageName = ImageName;
//        this.AccessibilityUrl = AccessibilityUrl;
//        this.NotificationUrl = NotificationUrl;
    }

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    public String getConfirmedActivityString(){
        return ConfirmedActivityString;
    }
    public Long getSuspectTime(){
        return SuspectTime;
    }
    public String getsuspectedStartActivity(){
        return suspectedStartActivity;
    }
    public String getsuspectedEndActivity(){
        return suspectedEndActivity;
    }

    public void setsyncStatus(Integer syncStatus){
        this.syncStatus = syncStatus;
    }

    public Integer getsyncStatus(){
        return this.syncStatus;
    }

    public Long getReadable(){
        return this.readable;
    }

    public String getSessionid() {
        return sessionid;
    }
}
