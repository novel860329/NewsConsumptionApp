package labelingStudy.nctu.minuku.dao;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import labelingStudy.nctu.minuku.model.DataRecord.RingerDataRecord;

/**
 * Created by tingwei on 2018/3/26.
 */

@Dao
public interface RingerDataRecordDao {
    @Query("SELECT * FROM RingerDataRecord")
    List<RingerDataRecord> getAll();

    @Query("SELECT * FROM RingerDataRecord WHERE creationTime BETWEEN :start AND :end")
    Cursor getRecordBetweenTimes(long start, long end);

    @Insert
    void insertAll(RingerDataRecord ringerDataRecord);

    @Query("DELETE FROM RingerDataRecord WHERE creationTime BETWEEN :start AND :end")
    void deleteRecordBetweenTimes( long start, long end);

    @Query("SELECT * FROM RingerDataRecord  WHERE sycStatus =:notSyncInt and readable=:targetHour  GROUP BY creationTime")
    Cursor getUnsyncedData(int notSyncInt,Long targetHour);

    @Query("UPDATE  RingerDataRecord SET sycStatus = :status WHERE creationTime = :creationT")
    int updateDataStatus(long creationT, int status);

    @Query("UPDATE  RingerDataRecord SET ImageName = :newName WHERE ImageName = :fileName")
    int updateFilenameByFileName(String fileName, String newName);

    @Query("SELECT * FROM RingerDataRecord  WHERE sycStatus = :status and creationTime = :creationT")
    Cursor getData( int status,long creationT);

    @Query("DELETE FROM RingerDataRecord WHERE sycStatus = :status")
    void deleteSyncData( int status);
}