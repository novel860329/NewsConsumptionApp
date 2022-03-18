package labelingStudy.nctu.minuku.dao;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import labelingStudy.nctu.minuku.model.DataRecord.CrashAppDataRecord;

@Dao
public interface CrashAppDataRecordDao {
    @Query("SELECT * FROM CrashAppDataRecord")
    List<CrashAppDataRecord> getAll();

    @Insert
    void insertAll(CrashAppDataRecord myCrashAppDataRecord);

    @Query("SELECT * FROM CrashAppDataRecord WHERE creationTime BETWEEN :start AND :end")
    Cursor getRecordBetweenTimes(long start, long end);

    @Query("DELETE FROM CrashAppDataRecord WHERE creationTime BETWEEN :start AND :end")
    void deleteRecordBetweenTimes( long start, long end);

    @Query("SELECT * FROM CrashAppDataRecord ORDER BY _id DESC LIMIT 0 , 1")
    CrashAppDataRecord getLastRecord();

    @Query("SELECT * FROM CrashAppDataRecord  WHERE sycStatus =:notSyncInt and readable BETWEEN :lastHour AND :targetHour GROUP BY creationTime")
    Cursor getUnsyncedData(int notSyncInt,Long targetHour, Long lastHour);

    @Query("UPDATE  CrashAppDataRecord SET sycStatus = :status WHERE creationTime = :creationT")
    int updateDataStatus(long creationT, int status);
}
