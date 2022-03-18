package labelingStudy.nctu.minuku.model.DataRecord;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.util.Date;

import labelingStudy.nctu.minukucore.model.DataRecord;

import static androidx.room.ForeignKey.CASCADE;
import static labelingStudy.nctu.minuku.config.SharedVariables.getReadableTimeLong;

@Entity(foreignKeys = @ForeignKey(entity = SessionDataRecord.class,
        parentColumns = "_id",
        childColumns = "sessionID",
        onDelete = CASCADE))
public class NewsDataRecord implements DataRecord {
    @PrimaryKey(autoGenerate = true)
    private long _id;

    @ColumnInfo(name = "sessionID", index = true)
    public long sessionID;

    @ColumnInfo(name = "creationTime")
    public long creationTime;

    @ColumnInfo(name = "readable")
    public Long readable;

    /*@ColumnInfo(name = "dataType")
    public String dataType;

    @ColumnInfo(name = "appName")
    public String appName;*/

    @ColumnInfo(name = "fileName")
    public String fileName;

    @ColumnInfo(name = "filePath")
    public String filePath;

    @ColumnInfo(name = "content")
    public String content;

    @ColumnInfo(name = "sycStatus")
    public Integer syncStatus;

    public NewsDataRecord(long sessionID, String fileName, String filePath, String content){
        this.sessionID = sessionID;
        this.creationTime = new Date().getTime();
        this.fileName = fileName;
        this.filePath = filePath;
        this.content = content;
        this.syncStatus = 0;
        this.readable = getReadableTimeLong(this.creationTime);
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

    public long get_sessionID() {
        return sessionID;
    }
    public void set_sessionID(long sessionID) {
        this.sessionID = sessionID;
    }

    /*public String getdataType(){
        return this.dataType;
    }
    public void setdataType(String dataType){
        this.dataType = dataType;
    }

    public String getappName(){
        return this.appName;
    }
    public void setappName(String appName){
        this.appName = appName;
    }*/

    public String getfileName(){
        return this.fileName;
    }
    public void setfileName(String fileName){
        this.fileName = fileName;
    }

    public String getfilePath(){
        return this.filePath;
    }
    public void setfilePath(String filePath){
        this.filePath = filePath;
    }

    public String getcontent(){
        return this.content;
    }
    public void setcontent(String content){
        this.content = content;
    }
}
