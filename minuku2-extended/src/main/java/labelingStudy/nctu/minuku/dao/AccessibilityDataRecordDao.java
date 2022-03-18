package labelingStudy.nctu.minuku.dao;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import labelingStudy.nctu.minuku.model.DataRecord.AccessibilityDataRecord;
/**
 * Created by tingwei on 2018/7/19.
 */

@Dao
public interface AccessibilityDataRecordDao {
    @Query("SELECT * FROM AccessibilityDataRecord")
    List<AccessibilityDataRecord> getAll();

    @Insert
    void insertAll(AccessibilityDataRecord accessibilityDataRecord);



    @Query("SELECT * FROM AccessibilityDataRecord WHERE creationTime BETWEEN :start AND :end ")
    Cursor getRecordBetweenTimes(long start, long end);

    @Query("DELETE FROM AccessibilityDataRecord WHERE creationTime BETWEEN :start AND :end")
    void deleteRecordBetweenTimes( long start, long end);

    @Query("SELECT * FROM AccessibilityDataRecord  WHERE sycStatus =:notSyncInt and readable BETWEEN :lastHour AND :targetHour GROUP BY creationTime")
    Cursor getUnsyncedData(int notSyncInt,Long targetHour, Long lastHour);


    @Query("UPDATE  AccessibilityDataRecord SET sycStatus = :status WHERE creationTime = :creationT")
    int updateDataStatus(long creationT, int status);

    @Query("SELECT * FROM AccessibilityDataRecord  WHERE sycStatus = :status and creationTime = :creationT")
    Cursor getData( int status,long creationT);

    @Query("DELETE FROM AccessibilityDataRecord WHERE sycStatus = :status")
    void deleteSyncData(int status);


}


