package labelingStudy.nctu.minuku.dao;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import labelingStudy.nctu.minuku.model.DataRecord.NewsDataRecord;

@Dao
public interface NewsDataRecrodDao {
    @Query("SELECT * FROM NewsDataRecord")
    List<NewsDataRecord> getAll();

    @Insert
    void insertAll(NewsDataRecord newsDataRecord);

    @Query("SELECT * FROM NewsDataRecord WHERE creationTime BETWEEN :start AND :end")
    Cursor getRecordBetweenTimes(long start, long end);

    @Query("DELETE FROM NewsDataRecord WHERE creationTime BETWEEN :start AND :end")
    void deleteRecordBetweenTimes( long start, long end);

    @Query("SELECT * FROM NewsDataRecord  ORDER BY _id DESC LIMIT 0 , 1")
    NewsDataRecord getLastRecord();

    @Query("SELECT * FROM NewsDataRecord  WHERE sycStatus =:notSyncInt and readable BETWEEN :lastHour AND :targetHour GROUP BY creationTime")
    Cursor getUnsyncedData(int notSyncInt,Long targetHour, Long lastHour);

    @Query("UPDATE  NewsDataRecord SET sycStatus = :status WHERE creationTime = :creationT")
    int updateDataStatus(long creationT, int status);

    @Query("UPDATE  NewsDataRecord SET sycStatus = :status WHERE sessionID = :id")
    void updateDataStatusBySessionID(long id, int status);

    @Query("UPDATE  NewsDataRecord SET fileName = :newName WHERE fileName = :fileName")
    int updateFilenameByFileName(String fileName, String newName);

    @Query("SELECT * From NewsDataRecord  WHERE sycStatus = :status GROUP BY creationTime")
    Cursor getSyncNewsData(int status);

    @Query("SELECT * FROM NewsDataRecord WHERE sessionID=:sessionID")
    List<NewsDataRecord> findRepositoriesForUser(final long sessionID);

    @Query("DELETE FROM NewsDataRecord WHERE sycStatus = :status")
    void deleteSyncData(int status);
}
