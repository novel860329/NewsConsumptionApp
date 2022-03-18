package labelingStudy.nctu.minuku.dao;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import labelingStudy.nctu.minuku.model.DataRecord.VideoDataRecord;

/**
 * Created by chiaenchiang on 07/12/2018.
 */
@Dao
public interface VideoDataRecordDao {
    @Insert
    void insertAll(VideoDataRecord videoDataRecordDao);

    @Query("SELECT * FROM VideoDataRecord  WHERE sycStatus =:notSyncInt  GROUP BY creationTime")
    Cursor getUnsyncedData(int notSyncInt);

    @Query("SELECT * FROM VideoDataRecord  WHERE sycStatus =:notSyncInt and readable BETWEEN :lastHour AND :targetHour GROUP BY creationTime")
    Cursor getUnsyncedData(int notSyncInt,Long targetHour, Long lastHour);

    @Query("DELETE FROM VideoDataRecord WHERE sycStatus = :status")
    void deleteSyncData(int status);
    @Query("UPDATE  VideoDataRecord SET sycStatus = :status WHERE creationTime = :creationT")
    int updateDataStatus(long creationT, int status);
    @Query("UPDATE  VideoDataRecord SET sycStatus = :status WHERE fileName = :fileName")
    int updateDataStatusByFileName(String fileName, int status);
    @Query("SELECT * From VideoDataRecord  WHERE sycStatus = :status GROUP BY creationTime")
    Cursor getSyncVideoData(int status);
    @Query("DELETE FROM VideoDataRecord WHERE fileName = :fileName")
    int deleteDataByFileName(String fileName);



}
