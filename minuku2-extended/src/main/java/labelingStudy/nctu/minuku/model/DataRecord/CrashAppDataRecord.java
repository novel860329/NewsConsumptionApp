package labelingStudy.nctu.minuku.model.DataRecord;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import labelingStudy.nctu.minukucore.model.DataRecord;

import static labelingStudy.nctu.minuku.config.SharedVariables.getReadableTimeLong;

@Entity(tableName = "CrashAppDataRecord")
public class CrashAppDataRecord implements DataRecord {

    @PrimaryKey(autoGenerate = true)
    public long _id;

    @ColumnInfo(name = "creationTime")
    public long creationTime;

    @ColumnInfo(name = "DeviceID")
    public String DeviceID;

    @ColumnInfo(name = "PackageName")
    public String PackeageName;

    @ColumnInfo(name = "SystemApp")
    private boolean SystemApp;

    @ColumnInfo(name = "AppName")
    public String AppName;

    @ColumnInfo(name = "readable")
    public long readable;

    @ColumnInfo(name = "ApplicationVersion")
    public int ApplicationVersion;

    @ColumnInfo(name = "ErrorShort")
    public String ErrorShort;

    @ColumnInfo(name = "ErrorLong")
    public String ErrorLong;

    @ColumnInfo(name = "ErrorCondition")
    public int ErrorCondition;

    @ColumnInfo(name = "sycStatus")
    public Integer syncStatus;

    public CrashAppDataRecord(long creationTime, String DeviceID, String PackeageName, String AppName, boolean SystemApp, int ApplicationVersion, String ErrorShort, String ErrorLong, int ErrorCondition) {
        this.creationTime = System.currentTimeMillis();
        this.DeviceID = DeviceID;
        this.PackeageName = PackeageName;
        this.AppName = AppName;
        this.SystemApp = SystemApp;
        this.readable = getReadableTimeLong(this.creationTime);
        this.ApplicationVersion = ApplicationVersion;
        this.ErrorShort = ErrorShort;
        this.ErrorLong = ErrorLong;
        this.ErrorCondition = ErrorCondition;
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
    public String getAppName() {
        return this.AppName ;
    }
    public boolean getSystemApp() {
        return this.SystemApp;
    }
    public long getReadable() {
        return this.readable  ;
    }
    public int Version() {
        return this.ApplicationVersion ;
    }
    public String getErrorShort() {
        return this.ErrorShort ;
    }
    public String getErrorLong() {
        return this.ErrorLong ;
    }
    public int getErrorCondition() {
        return this.ErrorCondition ;
    }
    public void setsyncStatus(Integer syncStatus){
        this.syncStatus = syncStatus;
    }
    public Integer getsyncStatus(){
        return this.syncStatus;
    }
}
