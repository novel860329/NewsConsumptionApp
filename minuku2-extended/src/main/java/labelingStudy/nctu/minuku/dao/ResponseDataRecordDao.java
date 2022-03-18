package labelingStudy.nctu.minuku.dao;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import labelingStudy.nctu.minuku.model.DataRecord.ResponseDataRecord;

/**
 * Created by chiaenchiang on 21/11/2018.
 */
@Dao
public interface ResponseDataRecordDao

{
    @Query("SELECT * FROM ResponseDataRecord")
    List<ResponseDataRecord> getAll();

    @Query("SELECT * FROM ResponseDataRecord WHERE creationTime BETWEEN :start AND :end")
    Cursor getRecordBetweenTimes(long start, long end);

    @Query("SELECT * FROM ResponseDataRecord WHERE creationTime =:creationTime ")
    Cursor getRecordWithCreationTime(long creationTime);

    @Insert
     void insertAll(ResponseDataRecord responseDataRecord);

    @Query("DELETE FROM ResponseDataRecord WHERE creationTime BETWEEN :start AND :end")
    void deleteRecordBetweenTimes(long start, long end);

    @Query("SELECT * FROM ResponseDataRecord  WHERE sycStatus =:notSyncInt and readable BETWEEN :lastHour AND :targetHour GROUP BY creationTime")
    Cursor getUnsyncedData(int notSyncInt, Long targetHour, Long lastHour);

    @Query("UPDATE ResponseDataRecord SET start_answer_time = :start, finish_time =:end, ifComplete=:ifcomplete WHERE related_id = :relatedId")
    void updateData(long relatedId, String start,String end,boolean ifcomplete);


    @Query("UPDATE ResponseDataRecord SET sycStatus = :status WHERE creationTime = :creationT")
    int updateDataStatus(long creationT, int status);


    @Query("SELECT * FROM ResponseDataRecord  WHERE sycStatus = :status and creationTime = :creationT")
    Cursor getData( int status,long creationT);

    @Query("DELETE FROM ResponseDataRecord WHERE sycStatus = :status")
    void deleteSyncData(int status);




}
