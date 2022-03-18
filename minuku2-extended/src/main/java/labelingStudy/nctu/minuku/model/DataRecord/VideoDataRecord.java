package labelingStudy.nctu.minuku.model.DataRecord;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

import labelingStudy.nctu.minukucore.model.DataRecord;

import static labelingStudy.nctu.minuku.config.SharedVariables.getReadableTimeLong;

/**
 * Created by chiaenchiang on 07/12/2018.
 */
@Entity(tableName = "VideoDataRecord")
public class VideoDataRecord implements DataRecord {
    @PrimaryKey(autoGenerate = true)
    private long _id;

    @ColumnInfo(name = "creationTime")
    public long creationTime;

    @ColumnInfo(name = "relatedId")
    public int relatedId;

    @ColumnInfo(name = "fileName")
    public String fileName;

    @ColumnInfo(name = "startTime")
    public String startTime;

    @ColumnInfo(name = "endTime")
    public String endTime;

    @ColumnInfo(name = "app")
    public String app;

    @ColumnInfo(name = "readable")
    public Long readable;

    @ColumnInfo(name = "sycStatus")
    public Integer syncStatus;


    public VideoDataRecord(int relatedId, String fileName,String app){
        this.creationTime = new Date().getTime();
        this.fileName = fileName;
        this.relatedId = relatedId;
        this.app = app;
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

    public int getRelatedId(){
        return this.relatedId;
    }
    public void setRelatedId(int relatedId){
        this.relatedId = relatedId;
    }
    public String getFileName(){
        return this.fileName;
    }
    public void setFileName(String fileName){
        this.fileName = fileName;
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
    public String getapp(){
        return this.app;
    }
    public void setapp(String app){
        this.app = app;
    }



}
