package labelingStudy.nctu.minuku.dao;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import labelingStudy.nctu.minuku.model.DataRecord.MyDataRecord;

@Dao
public interface MyDataRecrodDao {
    @Query("SELECT * FROM MyDataRecord")
    List<MyDataRecord> getAll();

    @Insert
    void insertAll(MyDataRecord myDataRecord);

    @Query("SELECT * FROM MyDataRecord WHERE creationTime BETWEEN :start AND :end")
    Cursor getRecordBetweenTimes(long start, long end);

    @Query("DELETE FROM MyDataRecord WHERE creationTime BETWEEN :start AND :end")
    void deleteRecordBetweenTimes( long start, long end);

    @Query("SELECT * FROM MyDataRecord  ORDER BY _id DESC LIMIT 0 , 1")
    MyDataRecord getLastRecord();

//    @Query("SELECT * FROM MyDataRecord  WHERE sycStatus =:notSyncInt and readable BETWEEN :lastHour AND :targetHour GROUP BY creationTime")
//    Cursor getUnsyncedData(int notSyncInt,Long targetHour, Long lastHour);

    @Query("SELECT * FROM MyDataRecord  WHERE sycStatus =:notSyncInt and readable= :targetHour GROUP BY creationTime")
    Cursor getUnsyncedData(int notSyncInt, Long targetHour);

    @Query("UPDATE  MyDataRecord SET sycStatus = :status WHERE creationTime = :creationT")
    int updateDataStatus(long creationT, int status);

    @Query("DELETE FROM MyDataRecord WHERE sycStatus = :status")
    void deleteSyncData(int status);

    @Query("UPDATE  MyDataRecord SET ImageName = :newName, ESM_ID = :ESM_ID WHERE ImageName = :fileName")
    int updateFilenameByFileName(String fileName, String newName, String ESM_ID);

}
