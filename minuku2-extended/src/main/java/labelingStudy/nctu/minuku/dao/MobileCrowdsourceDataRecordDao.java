package labelingStudy.nctu.minuku.dao;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import labelingStudy.nctu.minuku.model.DataRecord.MobileCrowdsourceDataRecord;

/**
 * Created by chiaenchiang on 04/11/2018.
 */
@Dao
public interface MobileCrowdsourceDataRecordDao {
    @Query("SELECT * FROM MobileCrowdsourceDataRecord")
    List<MobileCrowdsourceDataRecord> getAll();

    @Query("SELECT * FROM MobileCrowdsourceDataRecord WHERE creationTime BETWEEN :start AND :end")
    Cursor getRecordBetweenTimes(long start, long end);

    @Insert
    void insertAll(MobileCrowdsourceDataRecord mobileCrowdsourceDataRecord);

    @Query("DELETE FROM MobileCrowdsourceDataRecord WHERE creationTime BETWEEN :start AND :end")
    void deleteRecordBetweenTimes( long start, long end);

    @Query("SELECT * FROM MobileCrowdsourceDataRecord  WHERE sycStatus =:notSyncInt and readable BETWEEN :lastHour AND :targetHour  GROUP BY creationTime")
    Cursor getUnsyncedData(int notSyncInt,Long targetHour, Long lastHour);

    @Query("UPDATE  MobileCrowdsourceDataRecord SET sycStatus = :status WHERE creationTime = :creationT")
    int updateDataStatus(long creationT, int status);

    @Query("SELECT * FROM MobileCrowdsourceDataRecord  WHERE sycStatus = :status and creationTime = :creationT")
    Cursor getData( int status,long creationT);

    @Query("DELETE FROM MobileCrowdsourceDataRecord WHERE sycStatus = :status")
    void deleteSyncData( int status);

}
