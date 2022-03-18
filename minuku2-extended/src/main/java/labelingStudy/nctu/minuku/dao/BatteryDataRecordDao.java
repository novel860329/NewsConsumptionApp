package labelingStudy.nctu.minuku.dao;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import labelingStudy.nctu.minuku.model.DataRecord.BatteryDataRecord;

/**
 * Created by Lawrence on 2017/8/22.
 */
@Dao
public interface BatteryDataRecordDao {
    @Query("SELECT * FROM BatteryDataRecord")
    List<BatteryDataRecord> getAll();

    @Query("SELECT * FROM BatteryDataRecord WHERE creationTime BETWEEN :start AND :end")
    Cursor getRecordBetweenTimes(long start, long end);

    @Insert
    void insertAll(BatteryDataRecord batteryDataRecord);

    @Query("DELETE FROM BatteryDataRecord WHERE creationTime BETWEEN :start AND :end")
    void deleteRecordBetweenTimes( long start, long end);

//    @Query("SELECT * FROM BatteryDataRecord  WHERE sycStatus =:notSyncInt and readable BETWEEN :lastHour AND :targetHour GROUP BY creationTime")
//    Cursor getUnsyncedData(int notSyncInt,Long targetHour, Long lastHour);
    @Query("SELECT * FROM BatteryDataRecord  WHERE sycStatus =:notSyncInt and readable=:targetHour GROUP BY creationTime")
    Cursor getUnsyncedData(int notSyncInt,Long targetHour);

    @Query("UPDATE  BatteryDataRecord SET sycStatus = :status WHERE creationTime = :creationT")
    int updateDataStatus(long creationT, int status);

    @Query("UPDATE  BatteryDataRecord SET ImageName = :newName WHERE ImageName = :fileName")
    int updateFilenameByFileName(String fileName, String newName);

    @Query("SELECT * FROM BatteryDataRecord  WHERE sycStatus = :status and creationTime = :creationT")
    Cursor getData( int status,long creationT);

    @Query("DELETE FROM BatteryDataRecord WHERE sycStatus = :status")
    void deleteSyncData( int status);

}