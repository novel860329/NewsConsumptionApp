package labelingStudy.nctu.minuku.dao;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import labelingStudy.nctu.minuku.model.DataRecord.AppUsageDataRecord;

/**
 * Created by Jimmy on 2017/8/8.
 */
@Dao
public interface AppUsageDataRecordDao {
    @Query("SELECT * FROM AppUsageDataRecord")
    List<AppUsageDataRecord> getAll();

    @Insert
    void insertAll(AppUsageDataRecord appUsageDataRecord);

    @Query("SELECT * FROM AppUsageDataRecord WHERE creationTime BETWEEN :start AND :end")
    Cursor getRecordBetweenTimes(long start, long end);

    @Query("DELETE FROM AppUsageDataRecord WHERE creationTime BETWEEN :start AND :end")
    void deleteRecordBetweenTimes( long start, long end);

    @Query("SELECT * FROM AppUsageDataRecord  WHERE sycStatus =:notSyncInt and readable= :targetHour GROUP BY creationTime")
    Cursor getUnsyncedData(int notSyncInt,Long targetHour);

    @Query("UPDATE  AppUsageDataRecord SET sycStatus = :status WHERE creationTime = :creationT")
    int updateDataStatus(long creationT, int status);

    @Query("UPDATE  AppUsageDataRecord SET ImageName = :newName WHERE ImageName = :fileName")
    int updateFilenameByFileName(String fileName, String newName);

    @Query("SELECT * FROM AppUsageDataRecord  WHERE sycStatus = :status and creationTime = :creationT")
    Cursor getData( int status,long creationT);

    @Query("SELECT * FROM AppUsageDataRecord WHERE sycStatus = :notSyncInt LIMIT :n")
    Cursor getFirstData(int n, int notSyncInt);

    @Query("DELETE FROM AppUsageDataRecord WHERE sycStatus = :status")
    void deleteSyncData( int status);
}