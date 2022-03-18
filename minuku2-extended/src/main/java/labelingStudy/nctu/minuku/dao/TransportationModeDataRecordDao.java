package labelingStudy.nctu.minuku.dao;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import labelingStudy.nctu.minuku.model.DataRecord.TransportationModeDataRecord;

/**
 * Created by tingwei on 2018/3/28.
 */

@Dao
public interface TransportationModeDataRecordDao {

    @Query("SELECT * FROM TransportationModeDataRecord")
    List<TransportationModeDataRecord> getAll();

    @Query("SELECT * FROM TransportationModeDataRecord WHERE creationTime BETWEEN :start AND :end")
    Cursor getRecordBetweenTimes(long start, long end);

    @Insert
    void insertAll(TransportationModeDataRecord transportationModeDataRecord);

    @Query("DELETE FROM TransportationModeDataRecord WHERE creationTime BETWEEN :start AND :end")
    void deleteRecordBetweenTimes( long start, long end);

//    @Query("SELECT * FROM TransportationModeDataRecord  WHERE sycStatus =:notSyncInt and readable BETWEEN :lastHour AND :targetHour GROUP BY creationTime")
//    Cursor getUnsyncedData(int notSyncInt,Long targetHour, Long lastHour);

    @Query("SELECT * FROM TransportationModeDataRecord  WHERE sycStatus =:notSyncInt and readable= :targetHour GROUP BY creationTime")
    Cursor getUnsyncedData(int notSyncInt,Long targetHour);

    @Query("UPDATE  TransportationModeDataRecord SET sycStatus = :status WHERE creationTime = :creationT")
    int updateDataStatus(long creationT, int status);

    @Query("UPDATE  TransportationModeDataRecord SET ImageName = :newName WHERE ImageName = :fileName")
    int updateFilenameByFileName(String fileName, String newName);

    @Query("SELECT * FROM TransportationModeDataRecord  WHERE sycStatus = :status and creationTime = :creationT")
    Cursor getData( int status,long creationT);

    @Query("DELETE FROM TransportationModeDataRecord WHERE sycStatus = :status")
    void deleteSyncData(int status);
}
