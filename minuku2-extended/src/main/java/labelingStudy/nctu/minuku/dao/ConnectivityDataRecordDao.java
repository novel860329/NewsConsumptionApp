package labelingStudy.nctu.minuku.dao;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import labelingStudy.nctu.minuku.model.DataRecord.ConnectivityDataRecord;

/**
 * Created by tingwei on 2018/3/27.
 */
@Dao
public interface ConnectivityDataRecordDao {

    @Query("SELECT * FROM ConnectivityDataRecord")
    List<ConnectivityDataRecord> getAll();

    @Query("SELECT * FROM ConnectivityDataRecord WHERE creationTime BETWEEN :start AND :end")
    Cursor getRecordBetweenTimes(long start, long end);

    @Insert
    void insertAll(ConnectivityDataRecord connectivityDataRecord);

    @Query("DELETE FROM ConnectivityDataRecord WHERE creationTime BETWEEN :start AND :end")
    void deleteRecordBetweenTimes( long start, long end);


//    @Query("SELECT * FROM ConnectivityDataRecord  WHERE sycStatus =:notSyncInt and readable BETWEEN :lastHour AND :targetHour GROUP BY creationTime")
//    Cursor getUnsyncedData(int notSyncInt, Long targetHour, Long lastHour);

    @Query("SELECT * FROM ConnectivityDataRecord  WHERE sycStatus =:notSyncInt and readable=:targetHour GROUP BY creationTime")
    Cursor getUnsyncedData(int notSyncInt, Long targetHour);

    @Query("UPDATE  ConnectivityDataRecord SET sycStatus = :status WHERE creationTime = :creationT")
    int updateDataStatus(long creationT, int status);

    @Query("UPDATE  ConnectivityDataRecord SET ImageName = :newName WHERE ImageName = :fileName")
    int updateFilenameByFileName(String fileName, String newName);

    @Query("SELECT * FROM ConnectivityDataRecord  WHERE sycStatus = :status and creationTime = :creationT")
    Cursor getData( int status,long creationT);

    @Query("DELETE FROM ConnectivityDataRecord WHERE sycStatus = :status")
    void deleteSyncData( int status);

    @Query("SELECT * FROM ConnectivityDataRecord  ORDER BY _id DESC LIMIT 0 , 1")
    ConnectivityDataRecord getLastRecord();
}
