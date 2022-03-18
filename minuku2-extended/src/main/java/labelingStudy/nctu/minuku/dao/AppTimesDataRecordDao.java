package labelingStudy.nctu.minuku.dao;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import labelingStudy.nctu.minuku.model.DataRecord.AppTimesDataRecord;

@Dao
public interface AppTimesDataRecordDao {
    @Query("SELECT * FROM AppTimesDataRecord")
    List<AppTimesDataRecord> getAll();

    @Insert
    void insertAll(AppTimesDataRecord myDataRecord);

    @Query("SELECT * FROM AppTimesDataRecord WHERE creationTime BETWEEN :start AND :end")
    Cursor getRecordBetweenTimes(long start, long end);

    @Query("DELETE FROM AppTimesDataRecord WHERE creationTime BETWEEN :start AND :end")
    void deleteRecordBetweenTimes(long start, long end);

    @Query("SELECT * FROM AppTimesDataRecord  ORDER BY _id DESC LIMIT 0 , 1")
    AppTimesDataRecord getLastRecord();

//    @Query("SELECT * FROM AppTimesDataRecord  WHERE sycStatus =:notSyncInt and readable BETWEEN :lastHour AND :targetHour GROUP BY creationTime")
//    Cursor getUnsyncedData(int notSyncInt, Long targetHour, Long lastHour);
    @Query("SELECT * FROM AppTimesDataRecord  WHERE sycStatus =:notSyncInt and readable= :targetHour GROUP BY creationTime")
    Cursor getUnsyncedData(int notSyncInt, Long targetHour);

    @Query("UPDATE  AppTimesDataRecord SET sycStatus = :status WHERE creationTime = :creationT")
    int updateDataStatus(long creationT, int status);

    @Query("UPDATE  AppTimesDataRecord SET ImageName = :newName WHERE ImageName = :fileName")
    int updateFilenameByFileName(String fileName, String newName);

    @Query("DELETE FROM AppTimesDataRecord WHERE sycStatus = :status")
    void deleteSyncData(int status);
}
