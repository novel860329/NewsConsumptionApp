package labelingStudy.nctu.minuku.model.DataRecord;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import labelingStudy.nctu.minukucore.model.DataRecord;

import static labelingStudy.nctu.minuku.config.SharedVariables.getReadableTimeLong;

@Entity(tableName = "MyDataRecord")
public class MyDataRecord implements DataRecord {

    @PrimaryKey(autoGenerate = true)
    public long _id;

    @ColumnInfo(name = "creationTime")
    public long creationTime;

    @ColumnInfo(name = "DeviceID")
    public String DeviceID;

    @ColumnInfo(name = "PackageName")
    public String PackeageName;

    @ColumnInfo(name = "MyEventText")
    private String MyEventText;

    @ColumnInfo(name = "EventText")
    public String EventText;

    @ColumnInfo(name = "EventType")
    public String EventType;

    @ColumnInfo(name = "Extra")
    public String Extra;

    @ColumnInfo(name = "readable")
    public long readable;

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

    @ColumnInfo(name = "ESM_ID")
    public String ESM_ID;

    @ColumnInfo(name = "NewsApp")
    public String NewsApp;
//    @ColumnInfo(name = "AccessibilityUrl")
//    public String AccessibilityUrl;
//
//    @ColumnInfo(name = "NotificationUrl")
//    public String NotificationUrl;

    public MyDataRecord(long creationTime, String DeviceID, String PackeageName, String EventText, String EventType, String MyEventText,
                        String Extra, String sessionid, long phone_sessionid, String screenshot, String ImageName, String NewsApp) {
        this.creationTime = System.currentTimeMillis();
        this.DeviceID = DeviceID;
        this.PackeageName = PackeageName;
        this.EventText = EventText;
        this.EventType = EventType;
        this.MyEventText = MyEventText;
        this.Extra = Extra;
        this.readable = getReadableTimeLong(this.creationTime);
        this.syncStatus = 0;
        this.sessionid = sessionid;
        this.phone_sessionid = phone_sessionid;
        this.screenshot = screenshot;
        this.ImageName = ImageName;
        this.ESM_ID = "";
        this.NewsApp = NewsApp;
//        this.AccessibilityUrl = AccessibilityUrl;
//        this.NotificationUrl = NotificationUrl;
    }
    public long getCreationTime() {
        return this.creationTime ;
    }
    public String getDeviceID() {
        return this.DeviceID ;
    }
    public String getPackeageName() {
        return this.PackeageName ;
    }
    public String getEventText() {
        return this.EventText ;
    }
    public String getEventType() {
        return this.EventType ;
    }
    public String getExtra() {
        return this.Extra ;
    }
    public String getMyEventText() {
        return this.MyEventText;
    }
    public long getReadable() {
        return this.readable  ;
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
}
