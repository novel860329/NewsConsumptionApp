package labelingStudy.nctu.minuku.model.DataRecord;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Calendar;
import java.util.Date;

import labelingStudy.nctu.minukucore.model.DataRecord;

import static labelingStudy.nctu.minuku.config.SharedVariables.getReadableTimeLong;

/**
 * Created by Lawrence on 2017/7/22.
 */

@Entity(tableName = "RingerDataRecord")
public class RingerDataRecord implements DataRecord {


    @PrimaryKey(autoGenerate = true)
    public long _id;

    @ColumnInfo(name = "creationTime")
    public long creationTime;


    @ColumnInfo(name = "RingerMode")
    public String RingerMode = "NA";
    @ColumnInfo(name = "AudioMode")
    public String AudioMode = "NA";
    @ColumnInfo(name = "StreamVolumeMusic")
    public int StreamVolumeMusic = -9999;
    @ColumnInfo(name = "StreamVolumeNotification")
    public int StreamVolumeNotification = -9999;
    @ColumnInfo(name = "StreamVolumeRing")
    public int StreamVolumeRing = -9999;
    @ColumnInfo(name = "StreamVolumeVoicecall")
    public int StreamVolumeVoicecall = -9999;
    @ColumnInfo(name = "StreamVolumeSystem")
    public int StreamVolumeSystem = -9999;
    @ColumnInfo(name = "sessionid")
    public String sessionid;

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

    public RingerDataRecord(String RingerMode, String AudioMode, int StreamVolumeMusic
            , int StreamVolumeNotification, int StreamVolumeRing, int StreamVolumeVoicecall,
                            int StreamVolumeSystem, String sessionid, long phone_sessionid,
                            String screenshot, String ImageName){
        this.creationTime = new Date().getTime();
//        this.taskDayCount = Constants.TaskDayCount;
//        this.hour_range_1_5 = getmillisecondToHour(creationTime);
        this.RingerMode = RingerMode;
        this.AudioMode = AudioMode;
        this.StreamVolumeMusic = StreamVolumeMusic;
        this.StreamVolumeNotification = StreamVolumeNotification;
        this.StreamVolumeRing = StreamVolumeRing;
        this.StreamVolumeVoicecall = StreamVolumeVoicecall;
        this.StreamVolumeSystem = StreamVolumeSystem;
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

    private long getmillisecondToHour(long timeStamp){

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStamp);

        long mhour = calendar.get(Calendar.HOUR_OF_DAY);

        return mhour;

    }

    public Long getReadable(){
        return this.readable;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    public String getRingerMode(){
        return RingerMode;
    }

    public String getAudioMode(){
        return AudioMode;
    }

    public int getStreamVolumeMusic(){
        return StreamVolumeMusic;
    }

    public int getStreamVolumeNotification(){
        return StreamVolumeNotification;
    }

    public int getStreamVolumeRing(){
        return StreamVolumeRing;
    }

    public int getStreamVolumeVoicecall(){
        return StreamVolumeVoicecall;
    }

    public int getStreamVolumeSystem(){
        return StreamVolumeSystem;
    }

    public void setsyncStatus(Integer syncStatus){
        this.syncStatus = syncStatus;
    }

    public Integer getsyncStatus(){
        return this.syncStatus;
    }

}
