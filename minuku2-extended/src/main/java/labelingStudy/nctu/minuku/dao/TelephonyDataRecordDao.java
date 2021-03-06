package labelingStudy.nctu.minuku.dao;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import labelingStudy.nctu.minuku.model.DataRecord.TelephonyDataRecord;

/**
 * Created by tingwei on 2018/3/26.
 */

@Dao
public interface TelephonyDataRecordDao {
    @Query("SELECT * FROM TelephonyDataRecord")
    List<TelephonyDataRecord> getAll();

    @Query("SELECT * FROM TelephonyDataRecord WHERE creationTime BETWEEN :start AND :end")
    Cursor getRecordBetweenTimes(long start, long end);

    @Insert
    void insertAll(TelephonyDataRecord telephonyDataRecord);



    @Query("DELETE FROM TelephonyDataRecord WHERE creationTime BETWEEN :start AND :end")
    void deleteRecordBetweenTimes( long start, long end);

    @Query("SELECT * FROM TelephonyDataRecord  WHERE sycStatus =:notSyncInt and readable= :targetHour GROUP BY creationTime")
    Cursor getUnsyncedData(int notSyncInt,Long targetHour);

    @Query("UPDATE  TelephonyDataRecord SET sycStatus = :status WHERE creationTime = :creationT")
    int updateDataStatus(long creationT, int status);

    @Query("UPDATE  TelephonyDataRecord SET ImageName = :newName WHERE ImageName = :fileName")
    int updateFilenameByFileName(String fileName, String newName);

    @Query("SELECT * FROM TelephonyDataRecord  WHERE sycStatus = :status and creationTime = :creationT")
    Cursor getData( int status,long creationT);

    @Query("DELETE FROM TelephonyDataRecord WHERE sycStatus = :status")
    void deleteSyncData(int status);
}