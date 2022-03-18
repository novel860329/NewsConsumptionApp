package labelingStudy.nctu.minuku.dao;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import labelingStudy.nctu.minuku.model.DataRecord.NewsDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.SessionDataRecord;

@Dao
public interface SessionDataRecordDao {
    @Query("SELECT * FROM SessionDataRecord")
    Cursor getAll();

    @Insert
    void insertAll(SessionDataRecord sessionDataRecord);

    @Query("UPDATE SessionDataRecord SET endTime = :endTime, endTimestamp = :endTimestamp WHERE _id = :Id")
    void updateSession(long Id,String endTime, Long endTimestamp);

    @Query("UPDATE SessionDataRecord SET endTime = :endTime, endTimestamp = :endTimestamp WHERE endTime = :NA")
    void updateNASession(String NA,String endTime, Long endTimestamp);

    @Query("SELECT * FROM SessionDataRecord  ORDER BY _id DESC LIMIT 0 , 1")
    SessionDataRecord getLastRecord();

    @Query("SELECT * FROM SessionDataRecord  WHERE phone_sessionid <> -1 ORDER BY phone_sessionid DESC LIMIT 0 , 1")
    SessionDataRecord getLastPhoneSession();

    @Query("SELECT * FROM SessionDataRecord  WHERE sycStatus =:notSyncInt GROUP BY StartTimestamp")
    Cursor getUnsyncedData(int notSyncInt);

    @Query("UPDATE  SessionDataRecord SET sycStatus = :status WHERE _id = :id")
    int updateDataStatus(long id, int status);

    @Query("SELECT * FROM NewsDataRecord WHERE sessionID=:sessionID")
    List<NewsDataRecord> getNewsData(Long sessionID);
}
