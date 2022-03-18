package labelingStudy.nctu.minuku.dao;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import labelingStudy.nctu.minuku.model.DataRecord.ActivityRecognitionDataRecord;


/**
 * Created by Lawrence on 2017/5/22.
 */
@Dao
public interface ActivityRecognitionDataRecordDao  {

    @Query("SELECT * FROM ActivityRecognitionDataRecord")
    List<ActivityRecognitionDataRecord> getAll();
    @Query("SELECT * FROM ActivityRecognitionDataRecord WHERE creationTime BETWEEN :start AND :end")
    Cursor getRecordBetweenTimes(long start, long end);

    @Insert
    void insertAll(ActivityRecognitionDataRecord activityRecognitionDataRecord);

    @Query("DELETE FROM ActivityRecognitionDataRecord WHERE creationTime BETWEEN :start AND :end")
    void deleteRecordBetweenTimes( long start, long end);

//    @Query("SELECT * FROM ActivityRecognitionDataRecord  WHERE sycStatus =:notSyncInt and readable BETWEEN :lastHour AND :targetHour GROUP BY creationTime")
//    Cursor getUnsyncedData(int notSyncInt,Long targetHour, Long lastHour);
    @Query("SELECT * FROM ActivityRecognitionDataRecord  WHERE sycStatus =:notSyncInt and readable= :targetHour GROUP BY creationTime")
    Cursor getUnsyncedData(int notSyncInt,Long targetHour);

    @Query("UPDATE ActivityRecognitionDataRecord SET sycStatus = :status WHERE creationTime = :creationT")
    int updateDataStatus(long creationT, int status);

    @Query("UPDATE  ActivityRecognitionDataRecord SET ImageName = :newName WHERE ImageName = :fileName")
    int updateFilenameByFileName(String fileName, String newName);

    @Query("SELECT * FROM ActivityRecognitionDataRecord  WHERE sycStatus = :status and creationTime = :creationT")
    Cursor getData( int status,long creationT);

    @Query("DELETE FROM ActivityRecognitionDataRecord WHERE sycStatus = :status")
    void deleteSyncData(int status);

}