package labelingStudy.nctu.minuku.model.DataRecord;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

import labelingStudy.nctu.minukucore.model.DataRecord;

import static labelingStudy.nctu.minuku.config.SharedVariables.getReadableTimeLong;

@Entity(tableName = "SessionDataRecord")
public class SessionDataRecord implements DataRecord {
    @PrimaryKey(autoGenerate = true)
    private long _id;

    @ColumnInfo(name = "startTimestamp")
    public long startTimestamp;

    @ColumnInfo(name = "startTime") //getReadableTime
    public String startTime;

    @ColumnInfo(name = "endTime")
    public String endTime;

    @ColumnInfo(name = "dataType")
    public String dataType;

    @ColumnInfo(name = "readable")
    public Long readable;

    @ColumnInfo(name = "endTimestamp")
    public long endTimestamp;

    @ColumnInfo(name = "appName")
    public String appName;

    @ColumnInfo(name = "sycStatus")
    public Integer syncStatus;

    @ColumnInfo(name = "phone_sessionid")
    public Long phone_sessionid;


    public SessionDataRecord(String startTime, String endTime, String dataType, String appName, long phone_sessionid)
    {
        this.startTimestamp = new Date().getTime();
        this.startTime = startTime;
        this.endTime = endTime;
        this.dataType = dataType;
        this.appName = appName;
        this.syncStatus = 0;
        this.readable = getReadableTimeLong(this.startTimestamp);
        this.phone_sessionid = phone_sessionid;
    }

    @Override
    public long getCreationTime() {
        return startTimestamp;
    }

    public String getStartTime(){
        return this.startTime;
    }
    public void setStartTime(String startTime){
        this.startTime = startTime;
    }

    public String getEndTime(){
        return this.endTime;
    }
    public void setEndTime(String endTime){
        this.endTime = endTime;
    }

    public long get_id() {
        return _id;
    }
    public void set_id(long _id) {
        this._id = _id;
    }

    public long get_phoneid() {
        return phone_sessionid;
    }
}
