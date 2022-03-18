package labelingStudy.nctu.minuku.dao;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import labelingStudy.nctu.minuku.model.DataRecord.NotificationDataRecord;

/**
 * Created by chiaenchiang on 27/10/2018.
 */
@Dao
public interface NotificationDataRecordDao {
    @Query("SELECT * FROM NotificationDataRecord")
    Cursor getAll();

    @Query("SELECT * FROM NotificationDataRecord WHERE creationTime BETWEEN :start AND :end")
    Cursor getRecordBetweenTimes(long start, long end);

    @Insert
    void insertAll(NotificationDataRecord notificationDataRecord);

    @Query("DELETE FROM NotificationDataRecord WHERE creationTime BETWEEN :start AND :end")
    void deleteRecordBetweenTimes( long start, long end);

    @Query("SELECT * FROM NotificationDataRecord  WHERE sycStatus =:notSyncInt and readable=:targetHour GROUP BY creationTime")
    Cursor getUnsyncedData(int notSyncInt,Long targetHour);

    @Query("SELECT * FROM NotificationDataRecord  WHERE sycStatus =:notSyncInt GROUP BY creationTime")
    Cursor getUnsyncedDataAll(int notSyncInt);

    @Query("UPDATE  NotificationDataRecord SET sycStatus = :status WHERE creationTime = :creationT")
    int updateDataStatus(long creationT, int status);

    @Query("UPDATE  NotificationDataRecord SET ImageName = :newName WHERE ImageName = :fileName")
    int updateFilenameByFileName(String fileName, String newName);

    @Query("SELECT * FROM NotificationDataRecord  WHERE sycStatus = :status and creationTime = :creationT")
    Cursor getData( int status,long creationT);

    @Query("DELETE FROM NotificationDataRecord WHERE sycStatus = :status")
    void deleteSyncData( int status);

}
